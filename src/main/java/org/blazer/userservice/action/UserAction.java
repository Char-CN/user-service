package org.blazer.userservice.action;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blazer.userservice.body.Body;
import org.blazer.userservice.body.PageBody;
import org.blazer.userservice.cache.UserCache;
import org.blazer.userservice.entity.USUser;
import org.blazer.userservice.model.UserModel;
import org.blazer.userservice.service.UserService;
import org.blazer.userservice.util.HMap;
import org.blazer.userservice.util.IntegerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller(value = "userAction")
@RequestMapping("/user")
public class UserAction extends BaseAction {

	private static Logger logger = LoggerFactory.getLogger(UserAction.class);

	@Autowired
	UserService userService;

	@Autowired
	UserCache userCache;

	@ResponseBody
	@RequestMapping("/findWaitingUpdateUserCache")
	public LinkedBlockingQueue<UserModel> findWaitingUpdateUserCache(HttpServletRequest request, HttpServletResponse response) {
		return userCache.getQueue();
	}

	@ResponseBody
	@RequestMapping("/findUserByPage")
	public PageBody<USUser> findUserByPage(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			return userService.findUserByPage(getParamMap(request));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new PageBody<USUser>();
	}

	@ResponseBody
	@RequestMapping("/findUserById")
	public HashMap<String, Object> findUserById(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {
			HashMap<String, String> params = getParamMap(request);
			map.put("user", userService.findUserById(params));
			map.put("role_ids", userService.findRoleByUserId(IntegerUtil.getInt0(params.get("id"))));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return map;
	}

	@ResponseBody
	@RequestMapping("/saveUser")
	public Body saveUser(@RequestBody HashMap<String, Object> params) throws Exception {
		USUser user = HMap.to(params.get("user"), USUser.class);
		String roleIds = (String) params.get("roleIds");
		logger.debug("user : " + user);
		logger.debug("user : " + roleIds);
		try {
			userService.saveUser(user, roleIds);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Body().error().setMessage("保存失败：" + e.getMessage());
		}
		return new Body().setMessage("保存成功！");
	}

	@ResponseBody
	@RequestMapping("/delUser")
	public Body delUser(@RequestParam Integer id) throws Exception {
		logger.debug("userid : " + id);
		try {
			userService.delUser(id);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Body().error().setMessage("删除失败：" + e.getMessage());
		}
		return new Body().setMessage("删除成功！");
	}

	@ResponseBody
	@RequestMapping("/initPwd")
	public Body initPwd(@RequestParam Integer id) throws Exception {
		logger.debug("userid : " + id);
		try {
			userService.initPwd(id);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Body().error().setMessage("初始化密码失败：" + e.getMessage());
		}
		return new Body().setMessage("初始化密码成功！");
	}

}
