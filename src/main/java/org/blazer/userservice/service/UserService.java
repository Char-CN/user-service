package org.blazer.userservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.blazer.userservice.body.PageBody;
import org.blazer.userservice.cache.SystemCache;
import org.blazer.userservice.cache.UserCache;
import org.blazer.userservice.core.util.DesUtil;
import org.blazer.userservice.entity.USUser;
import org.blazer.userservice.exception.DuplicateKeyException;
import org.blazer.userservice.model.UserModel;
import org.blazer.userservice.util.HMap;
import org.blazer.userservice.util.IntegerUtil;
import org.blazer.userservice.util.SqlUtil;
import org.blazer.userservice.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service(value = "userService")
public class UserService implements InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	UserCache userCache;

	@Autowired
	SystemCache systemCache;

	@Value("#{systemProperties.new_user_defalut_password}")
	private String _newUserDefaultPassword;

	private String newUserDefaultPassword;

	/**
	 * 读取配置文件初始化新密码
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		newUserDefaultPassword = DesUtil.encrypt(_newUserDefaultPassword);
	}

	/**
	 * 分页查找用户
	 * 
	 * @param params
	 * @return
	 */
	public PageBody<USUser> findUserByPage(HashMap<String, String> params) {
		PageBody<USUser> pb = new PageBody<USUser>();
		String where = " where 1=1 and enable!=0 ";
		String userName = StringUtil.getStr(params.get("userName"));
		if (userName != null) {
			where += String.format(" and (user_name like '%%%s%%' or user_name_cn like '%%%s%%' or phone_number like '%%%s%%' or email like '%%%s%%')", userName,
					userName, userName, userName);
		}
		String sql = "select * from us_user " + where + " limit ?,?";
		int start = (IntegerUtil.getInt1(params.get("page")) - 1) * IntegerUtil.getInt0(params.get("rows"));
		int end = IntegerUtil.getInt0(params.get("rows"));
		logger.debug("start : " + start);
		logger.debug("end : " + end);
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, start, end);
		logger.debug("list size : " + list.size());
		List<USUser> userList = new ArrayList<USUser>();
		for (Map<String, Object> map : list) {
			USUser user = new USUser();
			user.setId(IntegerUtil.getInt0(map.get("id")));
			user.setUserName(StringUtil.getStrEmpty(map.get("user_name")));
			user.setUserNameCn(StringUtil.getStrEmpty(map.get("user_name_cn")));
			// user.setPassword(StringUtil.getStrEmpty(map.get("password")));
			user.setPhoneNumber(StringUtil.getStrEmpty(map.get("phone_number")));
			user.setEmail(StringUtil.getStrEmpty(map.get("email")));
			user.setRemark(StringUtil.getStrEmpty(map.get("remark")));
			user.setEnable(IntegerUtil.getInt0(map.get("enable")));
			userList.add(user);
		}
		pb.setTotal(IntegerUtil.getInt0(jdbcTemplate.queryForList("select count(0) as ct from us_user " + where).get(0).get("ct")));
		pb.setRows(userList);
		logger.debug(pb.toString());
		return pb;
	}

	/**
	 * 根据id查找用户
	 * 
	 * @param params
	 * @return
	 */
	public USUser findUserById(HashMap<String, String> params) {
		String sql = "select * from us_user where id = ? and enable=1";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, IntegerUtil.getInt0(params.get("id")));
		USUser user = new USUser();
		if (list.size() == 0) {
			return user;
		}
		Map<String, Object> map = list.get(0);
		user.setId(IntegerUtil.getInt0(map.get("id")));
		user.setUserName(StringUtil.getStrEmpty(map.get("user_name")));
		user.setUserNameCn(StringUtil.getStrEmpty(map.get("user_name_cn")));
		// user.setPassword(StringUtil.getStrEmpty(map.get("password")));
		user.setPhoneNumber(StringUtil.getStrEmpty(map.get("phone_number")));
		user.setEmail(StringUtil.getStrEmpty(map.get("email")));
		user.setRemark(StringUtil.getStrEmpty(map.get("remark")));
		return user;
	}

	/**
	 * 获取拥有该权限的所有用户 参数systemName和url 代表为某system的url是否有权限
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List<org.blazer.userservice.core.model.UserModel> findUserBySystemAndUrl(HashMap<String, String> params) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT uu.id, uu.user_name, uu.email, uu.`password` as `password`, uu.phone_number, uu.user_name_cn, group_concat(up.id) as permissions_ids");
		sql.append(" FROM us_user uu");
		sql.append(" INNER JOIN us_user_role uur ON uu.id = uur.user_id");
		sql.append(" INNER JOIN (SELECT * FROM us_role WHERE `enable`=1 and role_name not in('超级管理员')) ur ON ur.id = uur.role_id");
		sql.append(" INNER JOIN us_role_permissions urp ON urp.role_id = ur.id");
		sql.append(" INNER JOIN (SELECT * FROM us_permissions WHERE `enable`=1 and url=?) up ON urp.permissions_id = up.id");
		sql.append(" INNER JOIN (SELECT * FROM us_system WHERE `enable`=1 and system_name=?) us ON up.system_id = us.id");
		sql.append(" WHERE uu.`enable`=1 GROUP BY uu.id");
		// logger.debug(SqlUtil.Show(sql.toString(), params.get("url"),
		// params.get("systemName")));
		logger.debug(params.get("url"));
		logger.debug(params.get("systemName"));
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), StringUtil.getStrEmpty(params.get("url")),
				StringUtil.getStrEmpty(params.get("systemName")));
		List<org.blazer.userservice.core.model.UserModel> rst = HMap.toList(list, org.blazer.userservice.core.model.UserModel.class);
		return rst;
	}

	/**
	 * 保存用户
	 * 
	 * @param user
	 * @param roleIds
	 * @throws DuplicateKeyException
	 */
	public void saveUser(USUser user, String roleIds) throws DuplicateKeyException {
		// 验证是否重名、重复电话号码、重复邮箱
		Integer userId = user.getId();
		if (userId == null) {
			// String checkSql = "select 1 from us_user where enable=1 and
			// (user_name=? or email=? or phone_number=?)";
			String checkSql = "select sum(case when user_name='' then 0 when user_name=? then 1 else 0 end) as r1,"
					+ " sum(case when email='' then 0 when email=? then 1 else 0 end) as r2,"
					+ " sum(case when phone_number='' then 0 when phone_number=? then 1 else 0 end) as r3" + " from us_user where enable=1"
					+ " and (user_name=? or email=? or phone_number=?)";
			List<Map<String, Object>> rst = jdbcTemplate.queryForList(checkSql, user.getUserName(), user.getEmail(), user.getPhoneNumber(), user.getUserName(),
					user.getEmail(), user.getPhoneNumber());
			Integer r1 = IntegerUtil.getInt0(rst.get(0).get("r1"));
			Integer r2 = IntegerUtil.getInt0(rst.get(0).get("r2"));
			Integer r3 = IntegerUtil.getInt0(rst.get(0).get("r3"));
			if (r1 != 0 || r2 != 0 || r3 != 0) {
				throw new DuplicateKeyException(String.format("存在[%s]个重复用户名，存在[%s]个重复邮箱，存在[%s]个重复手机号码。", r1, r2, r3));
			}
			// enable 数据库默认值1
			String sql = "insert into us_user(user_name,user_name_cn,password,email,phone_number,remark) values(?,?,?,?,?,?)";
			jdbcTemplate.update(sql, user.getUserName(), user.getUserNameCn(), newUserDefaultPassword, user.getEmail(), user.getPhoneNumber(), user.getRemark());
			userId = IntegerUtil.getInt0(jdbcTemplate.queryForMap("select max(id) as id from us_user where enable=1").get("id"));
		} else {
			// String checkSql = "select 1 from us_user where enable=1 and
			// user_name=? and id != ?";
			String checkSql = "select sum(case when user_name='' then 0 when user_name=? then 1 else 0 end) as r1,"
					+ " sum(case when email='' then 0 when email=? then 1 else 0 end) as r2,"
					+ " sum(case when phone_number='' then 0 when phone_number=? then 1 else 0 end) as r3" + " from us_user where enable=1" + " and id != ?"
					+ " and (user_name=? or email=? or phone_number=?)";
			List<Map<String, Object>> rst = jdbcTemplate.queryForList(checkSql, user.getUserName(), user.getEmail(), user.getPhoneNumber(), user.getId(),
					user.getUserName(), user.getEmail(), user.getPhoneNumber());
			Integer r1 = IntegerUtil.getInt0(rst.get(0).get("r1"));
			Integer r2 = IntegerUtil.getInt0(rst.get(0).get("r2"));
			Integer r3 = IntegerUtil.getInt0(rst.get(0).get("r3"));
			if (r1 != 0 || r2 != 0 || r3 != 0) {
				throw new DuplicateKeyException(String.format("存在[%s]个重复用户名，存在[%s]个重复邮箱，存在[%s]个重复手机号码。", r1, r2, r3));
			}
			String sql = "update us_user set user_name=?,user_name_cn=?,email=?,phone_number=?,remark=? where id=? and enable=1";
			jdbcTemplate.update(sql, user.getUserName(), user.getUserNameCn(), user.getEmail(), user.getPhoneNumber(), user.getRemark(), user.getId());
		}
		addUserRole(userId, roleIds);
		// userCache.init(userId);
		UserModel um = new UserModel();
		um.setId(userId);
		um.setUserName(user.getUserName());
		userCache.addQueue(um);
	}

	/**
	 * 更新密码
	 * 
	 * @param user
	 */
	public void updatePwd(USUser user) {
		String sql = "update us_user set password=? where id=? and enable=1";
		jdbcTemplate.update(sql, user.getPassword(), user.getId());
		userCache.init(user.getId());
	}

	/**
	 * 初始化密码
	 * 
	 * @param userId
	 */
	public void initPwd(Integer userId) {
		String sql = "update us_user set password=? where id=? and enable=1";
		jdbcTemplate.update(sql, newUserDefaultPassword, userId);
		userCache.init(userId);
	}

	/**
	 * 删除该用户，软删除
	 * 
	 * @param id
	 */
	public void delUser(Integer id) {
		logger.debug("userId " + id);
		String sql = "update us_user set enable=0 where id=?";
		jdbcTemplate.update(sql, id);
		// 删除用户角色关系
		// delUserRoleByUserId(id);
		userCache.remove(id);
	}

	/**
	 * 禁用该用户
	 * 
	 * @param id
	 */
	public void disableUser(Integer id) {
		logger.debug("userId " + id);
		String sql = "update us_user set enable=2 where id=?";
		jdbcTemplate.update(sql, id);
		userCache.disable(id);
	}

	/**
	 * 启用该用户
	 * 
	 * @param id
	 */
	public void enableUser(Integer id) {
		logger.debug("userId " + id);
		String sql = "update us_user set enable=1 where id=?";
		jdbcTemplate.update(sql, id);
		userCache.enable(id);
	}

	/**
	 * 根据用户id删除该用户关联的角色
	 * 
	 * @param id
	 */
	public void delUserRoleByUserId(Integer id) {
		logger.debug("userId " + id);
		String sql = "delete from us_user_role where user_id=?";
		jdbcTemplate.update(sql, id);
	}

	/**
	 * 增加用户角色关系
	 * 
	 * @param userId
	 * @param roleIds
	 */
	public void addUserRole(Integer userId, String roleIds) {
		logger.debug("userId " + userId);
		logger.debug("roleIds " + roleIds);
		delUserRoleByUserId(userId);
		String sql = "insert into us_user_role(user_id, role_id) values(?, ?)";
		if (StringUtils.isNotEmpty(roleIds)) {
			for (String id : StringUtils.split(roleIds, ",")) {
				if (IntegerUtil.getInt(id) != null) {
					logger.debug(SqlUtil.Show(sql, userId, IntegerUtil.getInt(id)));
					jdbcTemplate.update(sql, userId, IntegerUtil.getInt(id));
				}
			}
		}
	}

	/**
	 * 根据用户id查找角色id，用逗号','分割
	 * 
	 * @param userId
	 * @return
	 */
	public String findRoleByUserId(Integer userId) {
		logger.debug("userId " + userId);
		String sql = "select GROUP_CONCAT(role_id) as roleids from us_user_role where user_id=? group by user_id";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId);
		String roleIds = list.size() == 0 ? "" : StringUtil.getStrEmpty(list.get(0).get("roleids"));
		logger.debug(roleIds);
		return roleIds;
	}

	/**
	 * 根据角色id更新用户缓存。一般用于修改角色后更新用户的权限信息。
	 * 
	 * @param roleId
	 */
	public void updateUserCacheByRoleId(Integer roleId) {
		logger.debug("roleId " + roleId);
		String sql = "select uur.user_id, uu.user_name from us_user_role uur inner join us_user uu on uu.id=uur.user_id where uur.role_id=?";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, roleId);
		StringBuilder userIds = new StringBuilder();
		for (Map<String, Object> map : list) {
			UserModel um = new UserModel();
			String userName = StringUtil.getStrEmpty(map.get("user_name"));
			Integer userId = IntegerUtil.getInt0(map.get("user_id"));
			um.setUserName(userName);
			um.setId(userId);
			userCache.addQueue(um);
			userIds.append(userId).append(",");
		}
		if (userIds.length() != 0) {
			userIds.deleteCharAt(userIds.length() - 1);
		}
		logger.info("userIds : " + userIds);
	}

}
