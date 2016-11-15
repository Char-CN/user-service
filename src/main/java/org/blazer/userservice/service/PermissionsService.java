package org.blazer.userservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.blazer.userservice.body.PermissionsTreeBody;
import org.blazer.userservice.entity.USPermissions;
import org.blazer.userservice.exception.DuplicateKey;
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

	public void savePermissions(USPermissions permissions) throws DuplicateKey {
		if (permissions.getId() == null) {
			String checkSql = "select 1 from us_permissions where enable=1 and system_id=? and url=?";
			List<Map<String, Object>> rst = jdbcTemplate.queryForList(checkSql, permissions.getSystemId(), permissions.getUrl());
			if (rst != null && rst.size() != 0) {
				throw new DuplicateKey("已经存在该URL！");
			}
			// enable 数据库默认值1
			String sql = "insert into us_permissions(system_id,parent_id,permissions_name,url,remark) values(?,?,?,?,?)";
			jdbcTemplate.update(sql, permissions.getSystemId(), permissions.getParentId(), permissions.getPermissionsName(), permissions.getUrl(), permissions.getRemark());
			logger.debug(SqlUtil.Show(sql, permissions.getSystemId(), permissions.getParentId(), permissions.getPermissionsName(), permissions.getUrl(), permissions.getRemark()));
		} else {
			String sql = "update us_permissions set permissions_name=?,url=?,remark=? where id=?";
			jdbcTemplate.update(sql, permissions.getPermissionsName(), permissions.getUrl(), permissions.getRemark(), permissions.getId());
			logger.debug(SqlUtil.Show(sql, permissions.getPermissionsName(), permissions.getUrl(), permissions.getRemark(), permissions.getId()));
		}
	}

	public void delPermissions(Integer id) throws NotAllowDeleteException {
		logger.debug("del permissions id " + id);
		String sql = "select count(0) as ct from us_permissions up inner join us_role_permissions urp on urp.permissions_id=up.id where up.id=?";
		logger.debug(SqlUtil.Show(sql, id));
		Integer count = IntegerUtil.getInt0(jdbcTemplate.queryForList(sql, id).get(0).get("ct"));
		logger.debug("role count : " + count);
		if (count != 0) {
			throw new NotAllowDeleteException("该权限被分配[" + count + "]个角色，不能删除。");
		}
		sql = "update us_permissions set enable=0 where id=?";
		logger.debug(SqlUtil.Show(sql, id));
		jdbcTemplate.update(sql, id);
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

	public USPermissions findPermissionsById(HashMap<String, String> params) {
		String sql = "select * from us_permissions where id=? and enable=1";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, IntegerUtil.getInt0(params.get("id")));
		USPermissions permissions = new USPermissions();
		if (list.size() == 0) {
			return permissions;
		}
		Map<String, Object> map = list.get(0);
		permissions.setId(IntegerUtil.getInt0(map.get("id")));
		permissions.setSystemId(IntegerUtil.getInt0(map.get("system_id")));
		permissions.setParentId(IntegerUtil.getInt0(map.get("parent_id")));
		permissions.setPermissionsName(StringUtil.getStrEmpty(map.get("permissions_name")));
		permissions.setUrl(StringUtil.getStrEmpty(map.get("url")));
		permissions.setRemark(StringUtil.getStrEmpty(map.get("remark")));
		return permissions;
	}

}
