package org.blazer.userservice.cache;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.blazer.userservice.model.SystemModel;
import org.blazer.userservice.util.IntegerUtil;
import org.blazer.userservice.util.StringUtil;
import org.blazer.userservice.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import net.sf.ehcache.Element;

@Component(value = "systemCache")
public class SystemCache extends BaseCache2 implements InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(SystemCache.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	public void afterPropertiesSet() throws Exception {
		logger.debug("系统权限开始加载");
		TimeUtil timeUtil = TimeUtil.createAndPoint().setLogger(logger);
		//////////////////////// 加载用户和权限 ////////////////////////
		init();
		timeUtil.printMs("加载权限");
	}

	private void init() {
		// 先清空
		this.clear();
		// 查询所有权限
		String sql = "select * from us_system where enable=1";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		for (Map<String, Object> map : list) {
			SystemModel systemModel = new SystemModel();
			systemModel.setId(IntegerUtil.getInt0(map.get("id")));
			systemModel.setSystemName(StringUtil.getStrEmpty(map.get("system_name")));
			this.add(systemModel);
			logger.info("init system : " + systemModel);
		}
		logger.info("init system size : " + list.size());
	}

	private void init(Integer id) {
		// 查询所有权限
		try {
			String sql = "select * from us_system where enable=1 and id=?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, id);
			if (list.size() == 0) {
				throw new SQLException("not found system id : " + id);
			}
			Map<String, Object> map = list.get(0);
			SystemModel systemModel = new SystemModel();
			systemModel.setId(IntegerUtil.getInt0(map.get("id")));
			systemModel.setSystemName(StringUtil.getStrEmpty(map.get("system_name")));
			this.add(systemModel);
			logger.info("init system : " + systemModel);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void reload() {
		try {
			afterPropertiesSet();
		} catch (Exception e) {
			logger.error("重载数据失败", e);
		}
	}

	public void clear() {
//		getCache().clear();
		getCache().removeAll();
	}

	public void add(SystemModel systemModel) {
		logger.debug("add : " + systemModel);
//		getCache().put(systemModel.getId(), systemModel);
		getCache().put(new Element(systemModel.getId(), systemModel));
	}

	public void remove(Integer id) {
//		getCache().evict(id);
		getCache().remove(id);
	}

	public SystemModel get(Integer id) {
		if (!contains(id)) {
			if (id == null) {
				return null;
			}
			this.init(id);
		}
//		return (SystemModel) getCache().get(id).get();
		return (SystemModel) getCache().get(id).getObjectValue();
	}

	public boolean contains(Integer id) {
		return getCache().get(id) != null;
	}

	@Override
	public String getCacheName() {
		return "system_cache";
	}

}
