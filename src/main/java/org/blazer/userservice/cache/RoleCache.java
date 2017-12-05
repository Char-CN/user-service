package org.blazer.userservice.cache;

import java.util.List;
import java.util.Map;

import org.blazer.userservice.model.RoleModel;
import org.blazer.userservice.util.HMap;
import org.blazer.userservice.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import net.sf.ehcache.Element;

@Component(value = "roleCache")
public class RoleCache extends BaseCache implements InitializingBean {

	private static Logger	logger	= LoggerFactory.getLogger(RoleCache.class);

	@Autowired
	JdbcTemplate			jdbcTemplate;

	public void afterPropertiesSet() throws Exception {
		logger.debug("角色开始加载");
		TimeUtil timeUtil = TimeUtil.createAndPoint().setLogger(logger);
		//////////////////////// 加载所有角色 ////////////////////////
		init();
		timeUtil.printMs("加载角色");
	}

	private void init() {
		// 先清空
		this.clear();
		// 查询所有角色
		String sql = "select * from us_role where enable=1";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		try {
			List<RoleModel> roles = HMap.toList(list, RoleModel.class);
			for (RoleModel role : roles) {
				this.add(role);
				logger.info("init role : " + role);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.info("init role size : " + list.size());
	}

	private void init(Integer id) {
		// 查询所有权限
		try {
			String sql = "select * from us_role where enable=1 and id=?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, id);
			RoleModel role = HMap.to(list.get(0), RoleModel.class);
			this.add(role);
			logger.info("init role : " + role);
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
		getCache().removeAll();
	}

	public void add(RoleModel roleModel) {
		logger.debug("add : " + roleModel);
		getCache().put(new Element(roleModel.getRoleName(), roleModel));
		getCache().put(new Element(roleModel.getId(), roleModel));
	}

	public void remove(Integer id) {
		RoleModel role = get(id);
		getCache().remove(id);
		getCache().remove(role.getRoleName());
	}

	public RoleModel get(String roleName) {
		if (!contains(roleName)) {
			return null;
		}
		return (RoleModel) getCache().get(roleName).getObjectValue();
	}

	public RoleModel get(Integer id) {
		if (!contains(id)) {
			if (id == null) {
				return null;
			}
			this.init(id);
		}
		return (RoleModel) getCache().get(id).getObjectValue();
	}

	public boolean contains(Integer id) {
		return getCache().get(id) != null;
	}

	public boolean contains(String roleName) {
		return getCache().get(roleName) != null;
	}

	@Override
	public String getCacheName() {
		return "role_cache";
	}

}
