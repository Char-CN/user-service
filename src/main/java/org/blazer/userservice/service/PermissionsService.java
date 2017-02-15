package org.blazer.userservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.blazer.userservice.body.PermissionsTreeBody;
import org.blazer.userservice.cache.PermissionsCache;
import org.blazer.userservice.entity.USPermissions;
import org.blazer.userservice.exception.DuplicateKeyException;
import org.blazer.userservice.exception.NotAllowDeleteException;
import org.blazer.userservice.util.HMap;
import org.blazer.userservice.util.IntegerUtil;
import org.blazer.userservice.util.SqlUtil;
import org.blazer.userservice.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service(value = "permissionsService")
public class PermissionsService {

	private static Logger logger = LoggerFactory.getLogger(PermissionsService.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	PermissionsCache permissionsCache;

	@Autowired
	SystemService systemService;

	public void savePermissions(USPermissions permissions) throws Exception {
		String systemName = systemService.findSystemById(permissions.getSystemId()).getSystemName();
		Integer permissionsId = permissions.getId();
		if (permissionsId == null) {
			String checkSql = "select 1 from us_permissions where enable=1 and system_id=? and url=?";
			List<Map<String, Object>> rst = jdbcTemplate.queryForList(checkSql, permissions.getSystemId(), permissions.getUrl());
			if (rst != null && rst.size() != 0) {
				throw new DuplicateKeyException("已经存在该路径地址！");
			}
			// enable 数据库默认值1
			String sql = "insert into us_permissions(system_id,parent_id,permissions_name,url,remark) values(?,?,?,?,?)";
			jdbcTemplate.update(sql, permissions.getSystemId(), permissions.getParentId(), permissions.getPermissionsName(), permissions.getUrl(), permissions.getRemark());
			logger.debug(SqlUtil.Show(sql, permissions.getSystemId(), permissions.getParentId(), permissions.getPermissionsName(), permissions.getUrl(), permissions.getRemark()));
			permissionsId = IntegerUtil.getInt0(jdbcTemplate.queryForMap("select max(id) as id from us_permissions where enable=1").get("id"));
		} else {
			// 清除原来url的cache
			String oldUrl = StringUtil.getStrEmpty(jdbcTemplate.queryForMap("select url from us_permissions where enable=1 and id=?", permissionsId).get("url"));
			String systemName_Url = systemName + "_" + oldUrl;
			if (StringUtils.isNotBlank(oldUrl)) {
				permissionsCache.remove(systemName_Url);
			}
			String sql = "update us_permissions set permissions_name=?,url=?,remark=? where id=?";
			jdbcTemplate.update(sql, permissions.getPermissionsName(), permissions.getUrl(), permissions.getRemark(), permissions.getId());
			logger.debug(SqlUtil.Show(sql, permissions.getPermissionsName(), permissions.getUrl(), permissions.getRemark(), permissions.getId()));
		}
		// 初始化cache，存在则覆盖
		permissionsCache.init(permissionsId);
	}

	public void delPermissions(Integer id) throws NotAllowDeleteException {
		logger.debug("del permissions id " + id);
		String sql = "select count(0) as ct from (select * from us_permissions where enable=1) up inner join us_role_permissions urp on urp.permissions_id=up.id where up.id=?";
		logger.debug(SqlUtil.Show(sql, id));
		Integer count = IntegerUtil.getInt0(jdbcTemplate.queryForList(sql, id).get(0).get("ct"));
		logger.debug("role count : " + count);
		if (count != 0) {
			throw new NotAllowDeleteException("该权限被分配[" + count + "]个角色，不能删除。如需删除必先解除关联的角色。");
		}
		sql = "update us_permissions set enable=0 where id=?";
		logger.debug(SqlUtil.Show(sql, id));
		jdbcTemplate.update(sql, id);
		permissionsCache.remove(id);
	}

	public List<PermissionsTreeBody> findPermissionsByParentID(HashMap<String, String> params) {
		Integer parentId = IntegerUtil.getInt(params.get("parentId"));
		Integer systemId = IntegerUtil.getInt(params.get("systemId"));
		if (parentId == null || systemId == null) {
			return new ArrayList<PermissionsTreeBody>();
		}
		String sql = "select *, permissions_name as text from us_permissions where enable=1 and parent_id=? and system_id=?";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, parentId, systemId);
		logger.debug(SqlUtil.Show(sql, parentId, systemId));
		List<PermissionsTreeBody> rst = null;
		try {
			rst = HMap.toList(list, PermissionsTreeBody.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (PermissionsTreeBody body : rst) {
			body.setState("closed");
			// fa-c 兼容easyui的自定义fa-c
			if (parentId == -1) {
				body.setIconCls("fa fa-pagelines fa-1x fa-c");
			} else {
				body.setIconCls("fa fa-pied-piper fa-1x fa-c");
			}
		}
		logger.debug(rst.toString());
		return rst;
	}

	public USPermissions findPermissionsById(HashMap<String, String> params) throws Exception {
		String sql = "select * from us_permissions where id=? and enable=1";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, IntegerUtil.getInt0(params.get("id")));
		USPermissions permissions = new USPermissions();
		if (list.size() == 0) {
			return permissions;
		}
		return HMap.to(list.get(0), USPermissions.class);
	}

}
