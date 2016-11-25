package org.blazer.userservice.cache;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.blazer.userservice.model.UserModel;
import org.blazer.userservice.util.IntegerUtil;
import org.blazer.userservice.util.StringUtil;
import org.roaringbitmap.buffer.MutableRoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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

	private LinkedBlockingQueue<UserModel> queue = new LinkedBlockingQueue<UserModel>();

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
		// 轮询检查
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						if (queue.size() == 0) {
							Thread.sleep(2000);
						} else {
							Iterator<UserModel> iterator = queue.iterator();
							if (iterator.hasNext()) {
								UserModel um = iterator.next();
								logger.debug("init [{}] [{}]", um.getUserName(), um.getId());
								init(um.getId());
								iterator.remove();
							}
							Thread.sleep(100);
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
		sql.append("SELECT uu.id, uu.user_name, uu.email, uu.`password` as `password`, uu.phone_number, uu.user_name_cn, group_concat(up.id) as permissions_ids");
		sql.append(" FROM us_user uu");
		sql.append(" LEFT JOIN us_user_role uur ON uu.id = uur.user_id");
		sql.append(" LEFT JOIN (SELECT * FROM us_role WHERE `enable`=1) ur ON ur.id = uur.role_id");
		sql.append(" LEFT JOIN us_role_permissions urp ON urp.role_id = ur.id");
		sql.append(" LEFT JOIN (SELECT * FROM us_permissions WHERE `enable`=1) up ON urp.permissions_id = up.id");
		sql.append(" WHERE uu.`enable`=1 GROUP BY uu.id");
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString());
		for (Map<String, Object> map : list) {
			UserModel userModel = new UserModel();
			userModel.setId(IntegerUtil.getInt0(map.get("id")));
			userModel.setUserName(StringUtil.getStrEmpty(map.get("user_name")));
			userModel.setUserNameCn(StringUtil.getStrEmpty(map.get("user_name_cn")));
			userModel.setPassword(StringUtil.getStrEmpty(map.get("password")));
			userModel.setEmail(StringUtil.getStrEmpty(map.get("email")));
			userModel.setPhoneNumber(StringUtil.getStrEmpty(map.get("phone_number")));
			String permissions_ids = StringUtil.getStrEmpty(map.get("permissions_ids"));
			MutableRoaringBitmap bitmap = new MutableRoaringBitmap();
			for (String id : StringUtils.splitPreserveAllTokens(permissions_ids, ",")) {
				bitmap.add(IntegerUtil.getInt0(id));
			}
			userModel.setPermissionsBitmap(bitmap);
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
		sql.append("SELECT uu.id, uu.user_name, uu.email, uu.`password` as `password`, uu.phone_number, uu.user_name_cn, group_concat(up.id) as permissions_ids");
		sql.append(" FROM us_user uu");
		sql.append(" LEFT JOIN us_user_role uur ON uu.id = uur.user_id");
		sql.append(" LEFT JOIN (SELECT * FROM us_role WHERE `enable`=1) ur ON ur.id = uur.role_id");
		sql.append(" LEFT JOIN us_role_permissions urp ON urp.role_id = ur.id");
		sql.append(" LEFT JOIN (SELECT * FROM us_permissions WHERE `enable`=1) up ON urp.permissions_id = up.id");
		sql.append(" WHERE uu.`enable`=1 AND uu.id=? GROUP BY uu.id");
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), userId);
		Map<String, Object> map = list.get(0);
		UserModel userModel = new UserModel();
		userModel.setId(IntegerUtil.getInt0(map.get("id")));
		userModel.setUserName(StringUtil.getStrEmpty(map.get("user_name")));
		userModel.setUserNameCn(StringUtil.getStrEmpty(map.get("user_name_cn")));
		userModel.setPassword(StringUtil.getStrEmpty(map.get("password")));
		userModel.setEmail(StringUtil.getStrEmpty(map.get("email")));
		userModel.setPhoneNumber(StringUtil.getStrEmpty(map.get("phone_number")));
		String permissions_ids = StringUtil.getStrEmpty(map.get("permissions_ids"));
		MutableRoaringBitmap bitmap = new MutableRoaringBitmap();
		for (String id : StringUtils.splitPreserveAllTokens(permissions_ids, ",")) {
			bitmap.add(IntegerUtil.getInt0(id));
		}
		userModel.setPermissionsBitmap(bitmap);
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
		sql.append("SELECT uu.id, uu.user_name, uu.email, uu.`password` as `password`, uu.phone_number, uu.user_name_cn, group_concat(up.id) as permissions_ids");
		sql.append(" FROM us_user uu");
		sql.append(" LEFT JOIN us_user_role uur ON uu.id = uur.user_id");
		sql.append(" LEFT JOIN (SELECT * FROM us_role WHERE `enable`=1) ur ON ur.id = uur.role_id");
		sql.append(" LEFT JOIN us_role_permissions urp ON urp.role_id = ur.id");
		sql.append(" LEFT JOIN (SELECT * FROM us_permissions WHERE `enable`=1) up ON urp.permissions_id = up.id");
		sql.append(" WHERE uu.`enable`=1 AND uu.user_name=? GROUP BY uu.id");
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), userName);
		if (list.size() == 0) {
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
		String permissions_ids = StringUtil.getStrEmpty(map.get("permissions_ids"));
		MutableRoaringBitmap bitmap = new MutableRoaringBitmap();
		for (String id : StringUtils.splitPreserveAllTokens(permissions_ids, ",")) {
			bitmap.add(IntegerUtil.getInt0(id));
		}
		userModel.setPermissionsBitmap(bitmap);
		logger.debug("init user : " + userModel);
		this.add(userModel);
	}

	private void clear() {
//		getCache().clear();
		getCache().removeAll();
	}

	public void add(UserModel userModel) {
		logger.debug("add user cache : " + userModel);
//		getCache().put(userModel.getUserName(), userModel);
		getCache().put(new Element(userModel.getUserName(), userModel));
	}

	public void remove(String userName) {
		logger.debug("remove user : " + userName);
//		getCache().evict(userName);
		getCache().remove(userName);
	}

	public boolean contains(String userName) {
		return getCache().get(userName) != null;
	}

	public UserModel get(String userName) {
		if (!contains(userName)) {
			logger.debug("not found " + userName + ", init start ~");
			this.init(userName);
		}
		if (!contains(userName)) {
			return null;
		}
//		return (UserModel) getCache().get(userName).get();
		return (UserModel) getCache().get(userName).getObjectValue();
	}

	@Override
	public String getCacheName() {
		return "user_cache";
	}

}
