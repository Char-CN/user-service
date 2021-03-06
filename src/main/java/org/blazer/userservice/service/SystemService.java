package org.blazer.userservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.blazer.userservice.body.PageBody;
import org.blazer.userservice.body.TreeBody;
import org.blazer.userservice.cache.PermissionsCache;
import org.blazer.userservice.entity.USSystem;
import org.blazer.userservice.exception.DuplicateKeyException;
import org.blazer.userservice.exception.NotAllowDeleteException;
import org.blazer.userservice.model.PermissionsModel;
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

	@Autowired
	PermissionsCache permissionsCache;

	public List<TreeBody> findSystemAndPermissionsTree() {
		List<USSystem> list = findSystemAll();
		List<TreeBody> treeList = new ArrayList<TreeBody>();
		for (USSystem system : list) {
			TreeBody body = new TreeBody();
			String systemName = system.getSystemName();
			body.setId(null);
			body.setText(systemName);
			body.setState("open");
			List<TreeBody> childList = findByParentIdAndSystemName(systemName, -1);
			body.setChildren(childList);
			if (childList.size() == 0) {
				body.setIconCls("fa fa-pied-piper fa-1x fa-c");
			} else {
				body.setIconCls("fa fa-pagelines fa-1x fa-c");
			}
			treeList.add(body);
		}
		logger.debug(treeList.toString());
		return treeList;
	}

	public List<TreeBody> findByParentIdAndSystemName(String systemName, Integer parentId) {
		List<PermissionsModel> list = permissionsCache.findByParentIdAndSystemName(systemName, parentId);
		List<TreeBody> treeList = new ArrayList<TreeBody>();
		for (PermissionsModel pm : list) {
			TreeBody body = new TreeBody();
			body.setId(pm.getId());
			body.setText(pm.getPermissionsName());
			body.setState("open");
			List<TreeBody> childList = findByParentIdAndSystemName(systemName, pm.getId());
			body.setChildren(childList);
			if (childList.size() == 0) {
				body.setIconCls("fa fa-pied-piper fa-1x fa-c");
			} else {
				body.setIconCls("fa fa-pagelines fa-1x fa-c");
			}
			treeList.add(body);
		}
		return treeList;
	}

	public List<USSystem> findSystemAll() {
		try {
			String sql = "select * from us_system where enable=1";
			List<Map<String, Object>> rst = jdbcTemplate.queryForList(sql);
			List<USSystem> list = HMap.toList(rst, USSystem.class);
			logger.debug("rst size : " + rst.size());
			return list;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<USSystem>();
	}

	public List<USSystem> findSystemAllByDisplay() {
		try {
			String sql = "select * from us_system where enable=1 and display=1";
			List<Map<String, Object>> rst = jdbcTemplate.queryForList(sql);
			List<USSystem> list = HMap.toList(rst, USSystem.class);
			logger.debug("rst size : " + rst.size());
			return list;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<USSystem>();
	}

	public PageBody<USSystem> findSystemByPage(HashMap<String, String> params) throws Exception {
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
		List<USSystem> systemList = HMap.toList(list, USSystem.class);
//		for (Map<String, Object> map : list) {
//			USSystem system = new USSystem();
//			system.setId(IntegerUtil.getInt0(map.get("id")));
//			system.setIndexUrl(StringUtil.getStrEmpty(map.get("index_url")));
//			system.setSystemName(StringUtil.getStrEmpty(map.get("system_name")));
//			system.setRemark(StringUtil.getStrEmpty(map.get("remark")));
//			systemList.add(system);
//		}
		pb.setTotal(IntegerUtil.getInt0(jdbcTemplate.queryForList("select count(0) as ct from us_system " + where).get(0).get("ct")));
		pb.setRows(systemList);
		logger.debug(pb.toString());
		return pb;
	}

	public void saveSystem(USSystem system) throws DuplicateKeyException {
		logger.debug("system " + system);
		if (system.getId() == null) {
			String checkSql = "select 1 from us_system where enable=1 and system_name=?";
			List<Map<String, Object>> rst = jdbcTemplate.queryForList(checkSql, system.getSystemName());
			if (rst != null && rst.size() != 0) {
				throw new DuplicateKeyException("已经存在该系统名！");
			}
			// enable 数据库默认值1
			String sql = "insert into us_system(system_name,title,index_url,remark) values(?,?,?,?)";
			jdbcTemplate.update(sql, system.getSystemName(), system.getTitle(), system.getIndexUrl(), system.getRemark());
		} else {
			String checkSql = "select 1 from us_system where enable=1 and system_name=? and id != ?";
			List<Map<String, Object>> rst = jdbcTemplate.queryForList(checkSql, system.getSystemName(), system.getId());
			if (rst != null && rst.size() != 0) {
				throw new DuplicateKeyException("已经存在该系统名！");
			}
			String sql = "update us_system set system_name=?,title=?,index_url=?,remark=? where id=?";
			jdbcTemplate.update(sql, system.getSystemName(), system.getTitle(), system.getIndexUrl(), system.getRemark(), system.getId());
		}
	}

	public void delSystem(Integer id) throws Exception {
		logger.debug("systemId " + id);
		String sql = "select count(0) as ct from us_permissions where enable=1 and system_id=?";
		logger.debug(SqlUtil.Show(sql, id));
		Integer count = IntegerUtil.getInt0(jdbcTemplate.queryForList(sql, id).get(0).get("ct"));
		logger.debug("permissions count : " + count);
		if (count != 0) {
			throw new NotAllowDeleteException("该系统下有[" + count + "]个权限，不能删除。如需删除必先解除关联的权限。");
		}
		sql = "update us_system set enable=0 where id=?";
		logger.debug(SqlUtil.Show(sql, id));
		jdbcTemplate.update(sql, id);
	}

	public USSystem findSystemById(Integer id) throws Exception {
		String sql = "select * from us_system where id=? and enable=1";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, id);
		USSystem system = new USSystem();
		if (list.size() == 0) {
			return system;
		}
		List<USSystem> systemList = HMap.toList(list, USSystem.class);
//		Map<String, Object> map = list.get(0);
//		system.setId(IntegerUtil.getInt0(map.get("id")));
//		system.setTitle(StringUtil.getStrEmpty(map.get("title")));
//		system.setSystemName(StringUtil.getStrEmpty(map.get("system_name")));
//		system.setIndexUrl(StringUtil.getStrEmpty(map.get("index_url")));
//		system.setRemark(StringUtil.getStrEmpty(map.get("remark")));
		return systemList.get(0);
	}

	public USSystem findSystemById(HashMap<String, String> params) throws Exception {
		return findSystemById(IntegerUtil.getInt0(params.get("id")));
	}

}
