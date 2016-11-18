package org.blazer.userservice.cache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.blazer.userservice.model.PermissionsModel;
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

@Component(value = "permissionsCache")
public class PermissionsCache extends BaseCache implements InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(PermissionsCache.class);

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
		String sql = "select up.id,up.permissions_name,up.url,us.system_name,us.id as system_id,up.parent_id from us_permissions up inner join us_system us on up.system_id=us.id where us.enable=1 and up.enable=1";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		for (Map<String, Object> map : list) {
			PermissionsModel permissionsModel = new PermissionsModel();
			permissionsModel.setId(IntegerUtil.getInt0(map.get("id")));
			permissionsModel.setPermissionsName(StringUtil.getStrEmpty(map.get("permissions_name")));
			permissionsModel.setUrl(StringUtil.getStrEmpty(map.get("url")));
			permissionsModel.setSystemId(IntegerUtil.getInt0(map.get("system_id")));
			permissionsModel.setSystemName(StringUtil.getStrEmpty(map.get("system_name")));
			permissionsModel.setParentId(IntegerUtil.getInt0(map.get("parent_id")));
			this.add(permissionsModel);
			logger.info("init permissions : " + permissionsModel);
		}
		logger.info("init permissions size : " + list.size());
	}

	public void init(Integer id) {
		// 查询所有权限
		try {
			String sql = "select up.id,up.permissions_name,up.url,us.system_name,us.id as system_id,up.parent_id from us_permissions up inner join us_system us on up.system_id=us.id where us.enable=1 and up.enable=1 and up.id=?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, id);
			if (list.size() == 0) {
				throw new SQLException("not found permissions id : " + id);
			}
			Map<String, Object> map = list.get(0);
			PermissionsModel permissionsModel = new PermissionsModel();
			permissionsModel.setId(IntegerUtil.getInt0(map.get("id")));
			permissionsModel.setPermissionsName(StringUtil.getStrEmpty(map.get("permissions_name")));
			permissionsModel.setUrl(StringUtil.getStrEmpty(map.get("url")));
			permissionsModel.setSystemId(IntegerUtil.getInt0(map.get("system_id")));
			permissionsModel.setSystemName(StringUtil.getStrEmpty(map.get("system_name")));
			permissionsModel.setParentId(IntegerUtil.getInt0(map.get("parent_id")));
			this.add(permissionsModel);
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

	public void add(PermissionsModel pModel) {
		logger.debug("add : " + pModel);
//		getCache().put(pModel.getId(), pModel);
//		getCache().put(pModel.getSystemName() + "_" + pModel.getUrl(), pModel.getId());
		getCache().put(new Element(pModel.getId(), pModel));
		if (StringUtils.isNotBlank(pModel.getUrl())) {
			getCache().put(new Element(pModel.getSystemName() + "_" + pModel.getUrl(), pModel.getId()));
		}
	}

	public void remove(Integer id) {
		getCache().remove(id);
	}

	public void remove(String systemName_Url) {
		getCache().remove(systemName_Url);
	}

	public PermissionsModel get(Integer id) {
		if (!contains(id)) {
			if (id == null) {
				return null;
			}
			this.init(id);
		}
//		return (PermissionsModel) getCache().get(id).get();
		return (PermissionsModel) getCache().get(id).getObjectValue();
	}

	public PermissionsModel getBySystemNameAndUrl(String systemName_Url) {
		if (!contains(systemName_Url)) {
			return null;
		}
		return get((Integer) getCache().get(systemName_Url).getObjectValue());
	}

	public boolean contains(Integer id) {
		return getCache().get(id) != null;
	}

	public boolean contains(String systemName_Url) {
		return getCache().get(systemName_Url) != null;
	}

	public List<PermissionsModel> findByParentIdAndSystemName(String systemName, Integer parentId) {
		List<PermissionsModel> list = new ArrayList<PermissionsModel>();
		for (Object obj : getCache().getKeys()) {
			if (obj instanceof java.lang.Integer) {
				PermissionsModel pm = get((Integer) obj);
				if (pm.getSystemName().equals(systemName) && pm.getParentId() == parentId) {
					list.add(pm);
				}
			}
		}
		return list;
	}

	@Override
	public String getCacheName() {
		return "permissions_cache";
	}

}
