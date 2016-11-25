package org.blazer.userservice.action;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.blazer.userservice.body.Body;
import org.blazer.userservice.body.LoginBody;
import org.blazer.userservice.cache.PermissionsCache;
import org.blazer.userservice.cache.UserCache;
import org.blazer.userservice.core.filter.PermissionsFilter;
import org.blazer.userservice.core.model.LoginType;
import org.blazer.userservice.core.model.SessionModel;
import org.blazer.userservice.core.util.DesUtil;
import org.blazer.userservice.core.util.SessionUtil;
import org.blazer.userservice.entity.USUser;
import org.blazer.userservice.model.PermissionsModel;
import org.blazer.userservice.model.UserModel;
import org.blazer.userservice.service.UserService;
import org.blazer.userservice.util.SqlUtil;
import org.blazer.userservice.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 对公共系统提供的接口
 * 
 * @author hyy
 *
 */
@Controller
@RequestMapping("/userservice")
public class UserServiceAction extends BaseAction {

	private static Logger logger = LoggerFactory.getLogger(UserServiceAction.class);

	@Autowired
	UserCache userCache;

	@Autowired
	UserService userService;

	@Autowired
	PermissionsCache permissionsCache;

	@RequestMapping("/getlogin")
	public String getLogin(HttpServletRequest request, HttpServletResponse response) {
		return "/login.html";
	}

	@ResponseBody
	@RequestMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		String sessionId = PermissionsFilter.getSessionId(request);
		logger.debug("logout session id : " + sessionId);
		return output(true, "");
	}

	@ResponseBody
	@RequestMapping("/login")
	public Body login(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		String sessionStr = PermissionsFilter.getSessionId(request);
		SessionModel sessionModel = SessionUtil.decode(sessionStr);
		if (checkUser(sessionModel)) {
			newSessionId(sessionModel, response, request);
			String un = getUser(sessionModel).getUserName();
			return new Body().setMessage("您好，" + (un == null ? "" : un) + "，您已经登录，无需再次登录");
		}
		HashMap<String, String> params = getParamMap(request);
		logger.debug("user name : " + params.get("userName"));
		if (!params.containsKey("userName")) {
			// 参数中找不到用户名，登录失败
			return new Body().setStatus("201").setMessage("登录失败。");
		}
		UserModel um = userCache.get(params.get("userName").toString());
		if (um == null) {
			// 找不到账号，登录失败
			return new Body().setStatus("201").setMessage("登录失败。");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("user password : " + um.getPassword());
			logger.debug("params password : " + DesUtil.encrypt(params.get("password")));
		}
		if (um.getPassword().equals(DesUtil.encrypt(params.get("password")))) {
			String sessionId = SessionUtil.encode(getExpire(), um.getId(), um.getUserName(), um.getUserNameCn(), um.getEmail(), um.getPhoneNumber(),
					LoginType.userName.index);
			logger.debug(SqlUtil.Show(SessionUtil.FORMAT.replaceAll("%s", "?"), getExpire(), um.getId(), um.getUserName(), um.getUserNameCn(), um.getEmail(), um.getPhoneNumber(),
					LoginType.userName.index));
			// String sessionId = DesUtil.encrypt(String.format(LOGGIN_FORMAT,
			// LoginType.userName.index, um.getId(), um.getUserName(),
			// getExpire()));
			PermissionsFilter.delay(request, response, sessionId);
			return new LoginBody().setSessionId(sessionId).setMessage("登录成功。");
		}
		// 密码不正确，登录失败
		return new Body().setStatus("201").setMessage("登录失败。");
	}

	@ResponseBody
	@RequestMapping("/getuser")
	public String getUser(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html;charset=utf-8");
		String sessionStr = PermissionsFilter.getSessionId(request);
		SessionModel sessionModel = SessionUtil.decode(sessionStr);
		// 检查用户
		boolean flag = checkUser(sessionModel);
		if (!flag) {
			return output(flag, "", "", "", "", "");
		}
		UserModel um = getUser(sessionModel);
		if (um == null) {
			return output(flag, "", "", "", "", "");
		}
		// 延期session
		String newSession = newSessionId(sessionModel, response, request);
		if (newSession == null) {
			return output(false, null, um.getUserName(), um.getUserNameCn(), um.getPhoneNumber(), um.getEmail());
		}
		// 返回
		return output(true, newSession, um.getUserName(), um.getUserNameCn(), um.getPhoneNumber(), um.getEmail());
	}

	@ResponseBody
	@RequestMapping("/checkuser")
	public String checkUser(HttpServletRequest request, HttpServletResponse response) {
		String sessionStr = PermissionsFilter.getSessionId(request);
		SessionModel sessionModel = SessionUtil.decode(sessionStr);
		boolean flag = checkUser(sessionModel);
		return output(flag, (flag ? newSessionId(sessionModel, response, request) : ""));
	}

	@ResponseBody
	@RequestMapping("/delay")
	public String delay(HttpServletRequest request, HttpServletResponse response) {
		String sessionStr = PermissionsFilter.getSessionId(request);
		SessionModel sessionModel = SessionUtil.decode(sessionStr);
		String newSession = newSessionId(sessionModel, response, request);
		return output(newSession != null, newSession);
	}

	@ResponseBody
	@RequestMapping("/checkurl")
	public String checkUrl(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("request:" + request.toString());
		String sessionStr = PermissionsFilter.getSessionId(request);
		SessionModel sessionModel = SessionUtil.decode(sessionStr);
		HashMap<String, String> params = getParamMap(request);
		String newSession = newSessionId(sessionModel, response, request);
		// if (!checkUser(sessionModel)) {
		// logger.debug("checkurl by 1 false");
		// return output(false, false, newSession);
		// }
		UserModel um = getUser(sessionModel);
		logger.debug("um : " + um);
		if (um == null) {
			logger.debug("checkurl user is not invalid");
			return output(false, false, newSession);
		}
		PermissionsModel permissionsModel = permissionsCache.getBySystemNameAndUrl(getSystemName_Url(params));
		logger.debug("url key : " + getSystemName_Url(params));
		logger.debug("permissionsModel : " + permissionsModel);
		logger.debug("bitmap : " + um.getPermissionsBitmap());
		// 系统存了该URL并且该URL的bitmap是没有值的
		if (permissionsModel != null && um.getPermissionsBitmap() != null && !um.getPermissionsBitmap().contains(permissionsModel.getId())) {
			logger.debug("checkurl permissions is not invalid");
			return output(true, false, newSession);
		}
		logger.debug("checkurl true");
		return output(true, true, newSession);
	}

	private UserModel getUser(SessionModel sessionModel) {
		if (!sessionModel.isValid()) {
			return null;
		}
		if (sessionModel.getExpireTime() < System.currentTimeMillis()) {
			return null;
		}
		UserModel um = userCache.get(sessionModel.getUserName());
		return um;
	}

	private boolean checkUser(SessionModel sessionModel) {
		if (!sessionModel.isValid()) {
			return false;
		}
		if (sessionModel.getExpireTime() < System.currentTimeMillis()) {
			return false;
		}
		UserModel um = userCache.get(sessionModel.getUserName());
		if (um == null) {
			logger.debug("check user is null...");
			return false;
		}
		return true;
	}

	public String newSessionId(SessionModel sessionModel, HttpServletResponse response, HttpServletRequest request) {
		UserModel um = getUser(sessionModel);
		if (um == null) {
			return null;
		}
		String domain = StringUtil.findOneStrByReg(request.getRequestURL().toString(), "[http|https]://([a-zA-Z0-9.]*).*");
		logger.debug("domain : " + domain);
		// String newSessionId = DesUtil.encrypt(String.format(LOGGIN_FORMAT,
		// LoginType.userName.index, um.getId(), um.getUserName(),
		// getExpire()));
		String newSessionId = SessionUtil.encode(getExpire(), um.getId(), um.getUserName(), um.getUserNameCn(), um.getEmail(), um.getPhoneNumber(),
				LoginType.userName.index);
		logger.info("sessionid length : " + newSessionId.length());
		if (newSessionId.length() > 2048) {
			logger.warn("[NOTICE] : sessionid > 2k , please you check...");
		}
		return newSessionId;
	}

	private long getExpire() {
//		return COOKIE_SECONDS * 1000 + System.currentTimeMillis();
		System.out.println(PermissionsFilter.getCookieSeconds());
		return PermissionsFilter.getCookieSeconds() + System.currentTimeMillis();
	}

	private String getSystemName_Url(HashMap<String, String> params) {
		return getSystemName(params) + "_" + getUrl(params);
	}

	private String getSystemName(HashMap<String, String> params) {
		return params.get("systemName");
	}

	private String getUrl(HashMap<String, String> params) {
		return params.get("url");
	}

//	private String getSessionId(HttpServletRequest request) {
//		String paramKey = getParamMap(request).get(SESSION_KEY);
//		if (StringUtils.isNotBlank(paramKey)) {
//			return paramKey;
//		}
//		Cookie[] cookies = request.getCookies();
//		Cookie sessionCookie = null;
//		if (cookies != null) {
//			for (Cookie cookie : cookies) {
//				if (SESSION_KEY.equals(cookie.getName())) {
//					logger.debug("cookie : " + cookie.getName() + " | " + cookie.getValue());
//					sessionCookie = cookie;
//					break;
//				}
//			}
//		}
//		return sessionCookie == null ? null : sessionCookie.getValue();
//	}

	@ResponseBody
	@RequestMapping("/uppwd")
	public String uppwd(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, String> params = getParamMap(request);
		logger.debug("user name : " + params.get("userName"));
		if (!params.containsKey("userName")) {
			return output(true, "修改失败");
		}
		UserModel um = userCache.get(params.get("userName").toString());
		if (um == null) {
			return output(true, "找不到账号，修改失败");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("user password : " + um.getPassword());
			logger.debug("params password : " + DesUtil.encrypt(params.get("password")));
		}
		if (um.getPassword().equals(DesUtil.encrypt(params.get("password")))) {
			String newPassword1 = DesUtil.encrypt(params.get("newPassword1"));
			USUser usUser = new USUser();
			usUser.setId(um.getId());
			usUser.setPassword(newPassword1);
			userService.updatePwd(usUser);
			return output(true, "修改成功");
		}
		return output(true, "密码错误，修改失败");
	}

	public static String output(Object... objs) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < objs.length; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(objs[i]);
		}
		return sb.toString();
	}

}
