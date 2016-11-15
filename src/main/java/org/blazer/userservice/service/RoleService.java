package org.blazer.userservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.blazer.userservice.body.PageBody;
import org.blazer.userservice.entity.USRole;
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

@Service(value = "roleService")
public class RoleService {

	private static Logger logger = LoggerFactory.getLogger(RoleService.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	public List<USRole> findRoleAll() {
		try {
			String sql = "select * from us_role where enable=1";
			List<Map<String, Object>> rst = jdbcTemplate.queryForList(sql);
			List<USRole> list = HMap.toList(rst, USRole.class);
			logger.debug("rst size : " + rst.size());
			if (list.size() > 0) {
				logger.debug("role : " + list.get(0));
			}
			return list;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<USRole>();
	}

	public PageBody<USRole> findRoleByPage(HashMap<String, String> params) {
		PageBody<USRole> pb = new PageBody<USRole>();
		String where = " where 1=1 and enable = 1 ";
		String roleName = StringUtil.getStr(params.get("roleName"));
		if (roleName != null) {
			where += String.format(" and (role_name like '%%%s%%')", roleName);
		}
		String sql = "select * from us_role " + where + " limit ?,?";
		int start = (IntegerUtil.getInt1(params.get("page")) - 1) * IntegerUtil.getInt0(params.get("rows"));
		int end = IntegerUtil.getInt0(params.get("rows"));
		logger.debug("start : " + start);
		logger.debug("end : " + end);
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, start, end);
		logger.debug("list size : " + list.size());
		List<USRole> roleList = new ArrayList<USRole>();
		for (Map<String, Object> map : list) {
			USRole role = new USRole();
			role.setId(IntegerUtil.getInt0(map.get("id")));
			role.setRoleName(StringUtil.getStrEmpty(map.get("role_name")));
			role.setRemark(StringUtil.getStrEmpty(map.get("remark")));
			roleList.add(role);
		}
		pb.setTotal(IntegerUtil.getInt0(jdbcTemplate.queryForList("select count(0) as ct from us_role " + where).get(0).get("ct")));
		pb.setRows(roleList);
		logger.debug(pb.toString());
		return pb;
	}

	public void saveRole(USRole role) {
		if (role.getId() == null) {
			// enable 数据库默认值1
			String sql = "insert into us_role(role_name,remark) values(?,?)";
			jdbcTemplate.update(sql, role.getRoleName(), role.getRemark());
		} else {
			String sql = "update us_role set role_name=?,remark=? where id=?";
			jdbcTemplate.update(sql, role.getRoleName(), role.getRemark(), role.getId());
		}
	}

	public void delRole(Integer id) throws Exception {
		logger.debug("roleId " + id);
		String sql = "select count(0) as ct from us_user_role ur inner join (select id from us_user where enable=1) u on ur.user_id=u.id where role_id=?";
		logger.debug(SqlUtil.Show(sql, id));
		Integer count = IntegerUtil.getInt0(jdbcTemplate.queryForList(sql, id).get(0).get("ct"));
		logger.debug("user count : " + count);
		if (count != 0) {
			throw new NotAllowDeleteException("该角色下有[" + count + "]个用户，不能删除。");
		}
		sql = "update us_role set enable=0 where id=?";
		logger.debug(SqlUtil.Show(sql, id));
		jdbcTemplate.update(sql, id);
		// 删除角色权限关系
		delRolePermissionsByRoleId(id);
	}

	public void delRolePermissionsByRoleId(Integer id) {
		String sql = "delete from us_role_permissions where role_id=?";
		logger.debug(SqlUtil.Show(sql, id));
		jdbcTemplate.update(sql, id);
	}

	public USRole findRoleById(HashMap<String, String> params) {
		String sql = "select * from us_role where id=? and enable=1";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, IntegerUtil.getInt0(params.get("id")));
		USRole role = new USRole();
		if (list.size() == 0) {
			return role;
		}
		Map<String, Object> map = list.get(0);
		role.setId(IntegerUtil.getInt0(map.get("id")));
		role.setRoleName(StringUtil.getStrEmpty(map.get("role_name")));
		role.setRemark(StringUtil.getStrEmpty(map.get("remark")));
		return role;
	}

}