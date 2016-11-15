package org.blazer.userservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.blazer.userservice.body.PageBody;
import org.blazer.userservice.entity.USSystem;
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

@Service(value = "systemService")
public class SystemService {

	private static Logger logger = LoggerFactory.getLogger(SystemService.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	public List<USSystem> findSystemAll() {
		try {
			String sql = "select * from us_system where enable=1";
			List<Map<String, Object>> rst = jdbcTemplate.queryForList(sql);
			List<USSystem> list = HMap.toList(rst, USSystem.class);
			logger.debug("rst size : " + rst.size());
			if (list.size() > 0) {
				logger.debug("system : " + list.get(0));
			}
			return list;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<USSystem>();
	}

	public PageBody<USSystem> findSystemByPage(HashMap<String, String> params) {
		PageBody<USSystem> pb = new PageBody<USSystem>();
		String where = " where 1=1 and enable=1 ";
		String systemName = StringUtil.getStr(params.get("systemName"));
		if (systemName != null) {
			where += String.format(" and (system_name like '%%%s%%')", systemName);
		}
		String sql = "select * from us_system " + where + " limit ?,?";
		int start = (IntegerUtil.getInt1(params.get("page")) - 1) * IntegerUtil.getInt0(params.get("rows"));
		int end = IntegerUtil.getInt0(params.get("rows"));
		logger.debug("start : " + start);
		logger.debug("end : " + end);
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, start, end);
		logger.debug("list size : " + list.size());
		List<USSystem> systemList = new ArrayList<USSystem>();
		for (Map<String, Object> map : list) {
			USSystem system = new USSystem();
			system.setId(IntegerUtil.getInt0(map.get("id")));
			system.setIndexUrl(StringUtil.getStrEmpty(map.get("index_url")));
			system.setSystemName(StringUtil.getStrEmpty(map.get("system_name")));
			system.setRemark(StringUtil.getStrEmpty(map.get("remark")));
			systemList.add(system);
		}
		pb.setTotal(IntegerUtil.getInt0(jdbcTemplate.queryForList("select count(0) as ct from us_system " + where).get(0).get("ct")));
		pb.setRows(systemList);
		logger.debug(pb.toString());
		return pb;
	}

	public void saveSystem(USSystem system) {
		logger.debug("system " + system);
		if (system.getId() == null) {
			// enable 数据库默认值1
			String sql = "insert into us_system(system_name,index_url,remark) values(?,?,?)";
			jdbcTemplate.update(sql, system.getSystemName(), system.getIndexUrl(), system.getRemark());
		} else {
			String sql = "update us_system set system_name=?,index_url=?,remark=? where id=?";
			jdbcTemplate.update(sql, system.getSystemName(), system.getIndexUrl(), system.getRemark(), system.getId());
		}
	}

	public void delSystem(Integer id) throws Exception {
		logger.debug("systemId " + id);
		String sql = "select count(0) as ct from us_permissions where enable=1 and system_id=?";
		logger.debug(SqlUtil.Show(sql, id));
		Integer count = IntegerUtil.getInt0(jdbcTemplate.queryForList(sql, id).get(0).get("ct"));
		logger.debug("permissions count : " + count);
		if (count != 0) {
			throw new NotAllowDeleteException("该系统下有[" + count + "]个权限，不能删除。");
		}
		sql = "update us_system set enable=0 where id=?";
		logger.debug(SqlUtil.Show(sql, id));
		jdbcTemplate.update(sql, id);
	}

	public USSystem findSystemById(Integer id) {
		String sql = "select * from us_system where id=? and enable=1";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, id);
		USSystem system = new USSystem();
		if (list.size() == 0) {
			return system;
		}
		Map<String, Object> map = list.get(0);
		system.setId(IntegerUtil.getInt0(map.get("id")));
		system.setSystemName(StringUtil.getStrEmpty(map.get("system_name")));
		system.setIndexUrl(StringUtil.getStrEmpty(map.get("index_url")));
		system.setRemark(StringUtil.getStrEmpty(map.get("remark")));
		return system;
	}

	public USSystem findSystemById(HashMap<String, String> params) {
		return findSystemById(IntegerUtil.getInt0(params.get("id")));
	}

}
