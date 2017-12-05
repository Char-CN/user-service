package org.blazer.userservice.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.blazer.userservice.model.UserModel;
import org.blazer.userservice.model.UserStatus;
import org.blazer.userservice.util.IntegerUtil;
import org.blazer.userservice.util.StringUtil;
import org.roaringbitmap.buffer.MutableRoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import net.sf.ehcache.Element;

/**
 * disk cache
 * 
 * 流程：
 * 
 * 1.缓存用户与所对应权限Bigmap
 * 
 * 2.缓存权限的map
 * 
 * 3.缓存用户-角色、角色-权限
 * 
 * @author hyy
 *
 */
@Component(value = "userCache")
public class UserCache extends BaseCache implements InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(UserCache.class);

	/**
	 * 等待更新的用户队列
	 */
	private LinkedBlockingQueue<UserModel> queue = new LinkedBlockingQueue<UserModel>();

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Value("${systemProperties.thread_scan_sleep_millisecond:10000}")
	private Integer thread_scan_sleep_millisecond;

	@Value("${systemProperties.thread_update_cache_sleep_millisecond:10}")
	private Integer thread_update_cache_sleep_millisecond;

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
		// 轮询检查更新用户(主要是权限信息),一般是修改用户角色需要重新更新权限
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (queue.isEmpty()) {
							Thread.sleep(thread_scan_sleep_millisecond);
						} else {
							UserModel um = queue.element();
							logger.debug("Queue Update User : [{}] [{}]", um.getId(), um.getUserName());
							init(um.getId());
							queue.remove();
							Thread.sleep(thread_update_cache_sleep_millisecond);
						}
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
		t.start();
	}

	public void addQueue(UserModel um) {
		queue.add(um);
	}

	public LinkedBlockingQueue<UserModel> getQueue() {
		return queue;
	}

	public void init() {
		// 先清空
		this.clear();
		// 查询所有权限
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT uu.id, uu.user_name, uu.email, uu.`password` as `password`, uu.phone_number, uu.user_name_cn, uu.enable, group_concat(up.id) as permissions_ids, group_concat(ur.id) as roles_ids");
		sql.append(" FROM us_user uu");
		sql.append(" LEFT JOIN us_user_role uur ON uu.id = uur.user_id");
		sql.append(" LEFT JOIN (SELECT * FROM us_role WHERE `enable`=1) ur ON ur.id = uur.role_id");
		sql.append(" LEFT JOIN us_role_permissions urp ON urp.role_id = ur.id");
		sql.append(" LEFT JOIN (SELECT * FROM us_permissions WHERE `enable`=1) up ON urp.permissions_id = up.id");
		sql.append(" WHERE 1=1 GROUP BY uu.id");
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString());
		for (Map<String, Object> map : list) {
			UserModel userModel = new UserModel();
			userModel.setId(IntegerUtil.getInt0(map.get("id")));
			userModel.setUserName(StringUtil.getStrEmpty(map.get("user_name")));
			userModel.setUserNameCn(StringUtil.getStrEmpty(map.get("user_name_cn")));
			userModel.setPassword(StringUtil.getStrEmpty(map.get("password")));
			userModel.setEmail(StringUtil.getStrEmpty(map.get("email")));
			userModel.setPhoneNumber(StringUtil.getStrEmpty(map.get("phone_number")));
			userModel.setEnable(IntegerUtil.getInt0(map.get("enable")));
			String permissions_ids = StringUtil.getStrEmpty(map.get("permissions_ids"));
			MutableRoaringBitmap permissionsBitmap = new MutableRoaringBitmap();
			for (String id : StringUtils.splitPreserveAllTokens(permissions_ids, ",")) {
				permissionsBitmap.add(IntegerUtil.getInt0(id));
			}
			userModel.setPermissionsBitmap(permissionsBitmap);
			String roles_ids = StringUtil.getStrEmpty(map.get("roles_ids"));
			MutableRoaringBitmap rolesBitmap = new MutableRoaringBitmap();
			for (String id : StringUtils.splitPreserveAllTokens(roles_ids, ",")) {
				rolesBitmap.add(IntegerUtil.getInt0(id));
			}
			userModel.setRolesBitmap(rolesBitmap);
			logger.debug("init user : " + userModel);
			this.add(userModel);
		}
		logger.info("init user size : " + list.size());
	}

	public void init(Integer userId) {
		if (userId == null) {
			logger.error("userId is null.");
			return;
		}
		// 查询所有权限
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT uu.id, uu.user_name, uu.email, uu.`password` as `password`, uu.phone_number, uu.user_name_cn, uu.enable, group_concat(up.id) as permissions_ids, group_concat(ur.id) as roles_ids");
		sql.append(" FROM us_user uu");
		sql.append(" LEFT JOIN us_user_role uur ON uu.id = uur.user_id");
		sql.append(" LEFT JOIN (SELECT * FROM us_role WHERE `enable`=1) ur ON ur.id = uur.role_id");
		sql.append(" LEFT JOIN us_role_permissions urp ON urp.role_id = ur.id");
		sql.append(" LEFT JOIN (SELECT * FROM us_permissions WHERE `enable`=1) up ON urp.permissions_id = up.id");
		sql.append(" WHERE 1=1 AND uu.id=? GROUP BY uu.id");
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), userId);
		if (list.size() == 0) {
			logger.error("init userId error, not found " + userId);
			return;
		}
		Map<String, Object> map = list.get(0);
		UserModel userModel = new UserModel();
		userModel.setId(IntegerUtil.getInt0(map.get("id")));
		userModel.setUserName(StringUtil.getStrEmpty(map.get("user_name")));
		userModel.setUserNameCn(StringUtil.getStrEmpty(map.get("user_name_cn")));
		userModel.setPassword(StringUtil.getStrEmpty(map.get("password")));
		userModel.setEmail(StringUtil.getStrEmpty(map.get("email")));
		userModel.setPhoneNumber(StringUtil.getStrEmpty(map.get("phone_number")));
		userModel.setEnable(IntegerUtil.getInt0(map.get("enable")));
		String permissions_ids = StringUtil.getStrEmpty(map.get("permissions_ids"));
		MutableRoaringBitmap permissionsBitmap = new MutableRoaringBitmap();
		for (String id : StringUtils.splitPreserveAllTokens(permissions_ids, ",")) {
			permissionsBitmap.add(IntegerUtil.getInt0(id));
		}
		userModel.setPermissionsBitmap(permissionsBitmap);
		String roles_ids = StringUtil.getStrEmpty(map.get("roles_ids"));
		MutableRoaringBitmap rolesBitmap = new MutableRoaringBitmap();
		for (String id : StringUtils.splitPreserveAllTokens(roles_ids, ",")) {
			rolesBitmap.add(IntegerUtil.getInt0(id));
		}
		userModel.setRolesBitmap(rolesBitmap);
		logger.debug("init user : " + userModel);
		this.add(userModel);
	}

	public void init(String userName) {
		if (userName == null) {
			logger.error("userName is null.");
			return;
		}
		// 查询所有权限
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT uu.id, uu.user_name, uu.email, uu.`password` as `password`, uu.phone_number, uu.user_name_cn, uu.enable, group_concat(up.id) as permissions_ids, group_concat(ur.id) as roles_ids");
		sql.append(" FROM us_user uu");
		sql.append(" LEFT JOIN us_user_role uur ON uu.id = uur.user_id");
		sql.append(" LEFT JOIN (SELECT * FROM us_role WHERE `enable`=1) ur ON ur.id = uur.role_id");
		sql.append(" LEFT JOIN us_role_permissions urp ON urp.role_id = ur.id");
		sql.append(" LEFT JOIN (SELECT * FROM us_permissions WHERE `enable`=1) up ON urp.permissions_id = up.id");
		sql.append(" WHERE 1=1 AND uu.user_name=? GROUP BY uu.id");
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), userName);
		if (list.size() == 0) {
			logger.error("init userId error, not found " + userName);
			return;
		}
		Map<String, Object> map = list.get(0);
		UserModel userModel = new UserModel();
		userModel.setId(IntegerUtil.getInt0(map.get("id")));
		userModel.setUserName(StringUtil.getStrEmpty(map.get("user_name")));
		userModel.setUserNameCn(StringUtil.getStrEmpty(map.get("user_name_cn")));
		userModel.setPassword(StringUtil.getStrEmpty(map.get("password")));
		userModel.setEmail(StringUtil.getStrEmpty(map.get("email")));
		userModel.setPhoneNumber(StringUtil.getStrEmpty(map.get("phone_number")));
		userModel.setEnable(IntegerUtil.getInt0(map.get("enable")));
		String permissions_ids = StringUtil.getStrEmpty(map.get("permissions_ids"));
		MutableRoaringBitmap permissionsBitmap = new MutableRoaringBitmap();
		for (String id : StringUtils.splitPreserveAllTokens(permissions_ids, ",")) {
			permissionsBitmap.add(IntegerUtil.getInt0(id));
		}
		userModel.setPermissionsBitmap(permissionsBitmap);
		String roles_ids = StringUtil.getStrEmpty(map.get("roles_ids"));
		MutableRoaringBitmap rolesBitmap = new MutableRoaringBitmap();
		for (String id : StringUtils.splitPreserveAllTokens(roles_ids, ",")) {
			rolesBitmap.add(IntegerUtil.getInt0(id));
		}
		userModel.setRolesBitmap(rolesBitmap);
		logger.debug("init user : " + userModel);
		this.add(userModel);
	}

	private void clear() {
		getCache().removeAll();
	}

	public void add(UserModel userModel) {
		logger.debug("add user cache : " + userModel);
		getCache().put(new Element(userModel.getUserName(), userModel));
		getCache().put(new Element(userModel.getId(), userModel));
	}

	public void remove(String userName) {
		logger.debug("remove user name : " + userName);
		UserModel um = get(userName);
		// 软删除 enable为0
		if (um != null) {
			um.setEnable(0);
			add(um);
		}
	}

	public void remove(Integer id) {
		logger.debug("remove user id : " + id);
		UserModel um = get(id);
		// 软删除 enable为0
		if (um != null) {
			um.setEnable(0);
			add(um);
		}
	}

	public void disable(String userName) {
		logger.debug("disable user name : " + userName);
		UserModel um = get(userName);
		// 禁用 enable为2
		if (um != null) {
			um.setEnable(2);
			add(um);
		}
	}

	public void disable(Integer id) {
		logger.debug("disable user id : " + id);
		UserModel um = get(id);
		// 禁用 enable为2
		if (um != null) {
			um.setEnable(2);
			add(um);
		}
	}

	public void enable(String userName) {
		logger.debug("enable user name : " + userName);
		UserModel um = get(userName);
		// 启用 enable为1
		if (um != null) {
			um.setEnable(1);
			add(um);
		}
	}

	public void enable(Integer id) {
		logger.debug("enable user id : " + id);
		UserModel um = get(id);
		// 启用 enable为1
		if (um != null) {
			um.setEnable(1);
			add(um);
		}
	}

	public boolean contains(Integer id) {
		if (id == null) {
			return false;
		}
		// 将user_cache全部缓存到磁盘，永久保存。by:20170621
		// 由于user_cache是缓存到磁盘，有过期时间，故需要重新初始化。
		// if (getCache().get(id) == null) {
		// init(id);
		// }
		return getCache().get(id) != null;
	}

	public boolean contains(String userName) {
		if (userName == null) {
			return false;
		}
		// 将user_cache全部缓存到磁盘，永久保存。by:20170621
		// 由于user_cache是缓存到磁盘，有过期时间，故需要重新初始化。
		// if (getCache().get(userName) == null) {
		// init(userName);
		// }
		return getCache().get(userName) != null;
	}

	public UserModel get(Integer id) {
		return contains(id) ? (UserModel) getCache().get(id).getObjectValue() : null;
	}

	public UserModel get(String userName) {
		return contains(userName) ? (UserModel) getCache().get(userName).getObjectValue() : null;
	}

	public List<org.blazer.userservice.core.model.UserModel> getAll() {
		List<org.blazer.userservice.core.model.UserModel> list = new ArrayList<org.blazer.userservice.core.model.UserModel>();
		for (Object key : getCache().getKeys()) {
			UserModel old = get(key.toString());
			// 过滤删除状态的用户
			if (UserStatus.isDelete(old.getEnable())) {
				continue;
			}
			org.blazer.userservice.core.model.UserModel um = new org.blazer.userservice.core.model.UserModel();
			um.setEmail(old.getEmail());
			um.setId(old.getId());
			um.setPassword(old.getPassword());
			um.setPhoneNumber(old.getPhoneNumber());
			um.setUserName(old.getUserName());
			um.setUserNameCn(old.getUserNameCn());
			list.add(um);
		}
		return list;
	}

	@Override
	public String getCacheName() {
		return "user_cache";
	}

}
