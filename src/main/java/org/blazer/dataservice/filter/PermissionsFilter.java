package org.blazer.dataservice.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.blazer.dataservice.util.StringUtil;

public class PermissionsFilter implements Filter {

	private static final String COOKIE_KEY = "MYSESSIONID";
	private static final String COOKIE_PATH = "/";

	private String systemName = null;
	private String serviceUrl = null;
	private String noPermissionsPage = null;
	private Integer cookieSeconds = null;
	private boolean onOff = false;

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String sessionid = getSessionId(request);
		System.out.println(request.getRequestURL());
		System.out.println(request.getRequestURI());
		System.out.println(request.getRemoteHost());
		System.out.println(request.getRemoteAddr());
		System.out.println(request.getContextPath());
		if (!onOff) {
			chain.doFilter(req, resp);
			return;
		}
		String url = request.getRequestURI();
		if (!"".equals(request.getContextPath())) {
			url = url.replaceFirst(request.getContextPath(), "");
		}
		System.out.println("action url : " + url);
		// 由于是本系统，因此过滤掉
		if ("/login.html".equals(url)) {
			chain.doFilter(req, resp);
			return;
		}
		if (url.startsWith("/userservice")) {
			chain.doFilter(req, resp);
			return;
		}
		try {
//			String content = executeGet(serviceUrl + "/getuser.do?" + SESSION_KEY + "=" + sessionid);
//			ObjectMapper mapper = new ObjectMapper();
//			UserModel um = mapper.readValue(content, UserModel.class);
//			System.out.println(um);
			StringBuilder requestUrl = new StringBuilder(serviceUrl);
			requestUrl.append("/userservice/checkurl.do?");
			requestUrl.append(COOKIE_KEY).append("=").append(sessionid);
			requestUrl.append("&").append("url").append("=").append(url);
			requestUrl.append("&").append("systemName").append("=").append(systemName);
			String content = executeGet(requestUrl.toString());
			System.out.println(content);
			String[] contents = content.split(",", 3);
			if (contents.length != 3) {
				System.err.println("checkurl.do result length is not valid... please check it...");
//				delay(response, request, contents[1]);
			}
			// no login
			if ("false".equals(contents[0])) {
				System.out.println("no login");
				System.out.println(serviceUrl + "/login.html");
//				RequestDispatcher rd = request.getRequestDispatcher(serviceUrl + "/login.html");
//				rd.forward(req, resp);
				response.sendRedirect(serviceUrl + "/login.html");
				return;
			}
			// no permissions
			if ("false".equals(contents[1])) {
				System.out.println("no permissions");
				RequestDispatcher rd = request.getRequestDispatcher(noPermissionsPage);
				rd.forward(req, resp);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		chain.doFilter(req, resp);
	}

	public String executeGet(String url) throws Exception {
		BufferedReader in = null;
		String content = null;
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpclient.execute(httpGet);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				if (sb.length() != 0) {
					sb.append(NL);
				}
				sb.append(line);
			}
			in.close();
			content = sb.toString();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return content;
	}

	private void delay(HttpServletResponse response, HttpServletRequest request, String newSession) {
		if ("".equals(newSession)) {
			newSession = null;
		}
		String domain = StringUtil.findOneStrByReg(request.getRequestURL().toString(), "[http|https]://([a-zA-Z0-9.]*).*");
		System.out.println("delay ~ new session : " + newSession);
		Cookie cookie = new Cookie(COOKIE_KEY, newSession);
		cookie.setPath(COOKIE_PATH);
		domain = ".blazer2.org";
		System.out.println("domain ~ " + domain);
		cookie.setDomain(domain);
		cookie.setMaxAge(cookieSeconds);
		response.addCookie(cookie);
	}

	private String getSessionId(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		Cookie sessionCookie = null;
		for (Cookie cookie : cookies) {
			if (COOKIE_KEY.equals(cookie.getName())) {
				System.out.println("cookie : " + cookie.getName() + " | " + cookie.getValue());
				sessionCookie = cookie;
				break;
			}
		}
		return sessionCookie == null ? null : sessionCookie.getValue();
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		systemName = filterConfig.getInitParameter("systemName");
		serviceUrl = filterConfig.getInitParameter("serviceUrl");
		noPermissionsPage = filterConfig.getInitParameter("noPermissionsPage");
		onOff = "1".equals(filterConfig.getInitParameter("on-off"));
		try {
			cookieSeconds = Integer.parseInt(filterConfig.getInitParameter("cookieSeconds"));
		} catch (Exception e) {
			System.err.println("初始化cookie时间出错。");
		}
		System.out.println("init filter systemName : " + systemName);
		System.out.println("init filter serviceUrl : " + serviceUrl);
		System.out.println("init filter noPermissionsPage : " + noPermissionsPage);
		System.out.println("init filter cookieSeconds : " + cookieSeconds);
		System.out.println("init filter on-off : " + onOff);
	}

	public void destroy() {
	}

//	private String systemName = null;
//
//	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
//		System.out.println(systemName);
//		if (systemName != null) {
//			HttpServletRequest request = (HttpServletRequest) req;
//			System.out.println(request.getRequestURL());
//			System.out.println(request.getRequestURI());
//			System.out.println(request.getRemoteHost());
//			System.out.println(request.getRemoteAddr());
//		}
//		chain.doFilter(req, resp);
//	}
//
//	public void init(FilterConfig filterConfig) throws ServletException {
//		systemName = filterConfig.getInitParameter("systemName");
//		System.out.println("init filter : " + systemName);
//	}
//
//	public void destroy() {
//	}

}
