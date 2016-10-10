package org.blazer.dataservice.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
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

public class PermissionsFilter implements Filter {

	private static final String COOKIE_KEY = "MYSESSIONID";
	private static final String COOKIE_PATH = "/";

	private String systemName = null;
	private String serviceUrl = null;
	private String innerServiceUrl = null;
	private String noPermissionsPage = null;
	private HashSet<String> ignoreUrlsMap = null;
	private Integer cookieSeconds = null;
	private boolean onOff = false;

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		if (!onOff) {
			chain.doFilter(req, resp);
			return;
		}
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String url = request.getRequestURI();
		if (!"".equals(request.getContextPath())) {
			url = url.replaceFirst(request.getContextPath(), "");
		}
		System.out.println("action url : " + url);
		String sessionid = getSessionId(request);
		// 由于是本系统，因此过滤掉
		if ("/login.html".equals(url)) {
			chain.doFilter(req, resp);
			return;
		}
		// 过滤
		if (url.startsWith("/userservice")) {
			chain.doFilter(req, resp);
			return;
		}
		// web.xml配置的过滤页面
		if (ignoreUrlsMap.contains(url)) {
			chain.doFilter(req, resp);
			return;
		}
		try {
			StringBuilder requestUrl = new StringBuilder(innerServiceUrl);
			requestUrl.append("/userservice/checkurl.do?");
			requestUrl.append(COOKIE_KEY).append("=").append(sessionid);
			requestUrl.append("&").append("systemName").append("=").append(systemName);
			requestUrl.append("&").append("url").append("=").append(url);
			String content = executeGet(requestUrl.toString());
			System.out.println("请求checkurl.do返回结果：" + content);
			String[] contents = content.split(",", 3);
			if (contents.length != 3) {
				System.err.println("请求checkurl.do返回：长度不对。");
			}
			delay(response, request, contents[2]);
			// no login
			if ("false".equals(contents[0])) {
				System.err.println("请求checkurl.do返回：没有登录。");
				System.err.println(serviceUrl + "/login.html?url=" + URLEncoder.encode(request.getRequestURL().toString(), "UTF-8"));
				response.sendRedirect(serviceUrl + "/login.html?url=" + URLEncoder.encode(request.getRequestURL().toString(), "UTF-8"));
				return;
			}
			// no permissions
			if ("false".equals(contents[1])) {
				System.err.println("请求checkurl.do返回：没有权限。");
				if (noPermissionsPage == null) {
					System.err.println("noPermissionsPage没有配置。");
					response.sendRedirect(serviceUrl + "/nopermissions.html");
					return;
				}
				response.sendRedirect(noPermissionsPage);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("验证userservice出现错误。。。");
			response.sendRedirect(noPermissionsPage);
			return;
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
		String domain = findOneStrByReg(request.getRequestURL().toString(), "[http|https]://.*([.][a-zA-Z0-9]*[.][a-zA-Z0-9]*)/*.*");
		if (domain == null) {
			System.out.println("delay error ~ domain is null ~ new session : " + newSession);
			return;
		}
		System.out.println("delay ~ [" + domain + "] ~ new session : " + newSession);
		Cookie cookie = new Cookie(COOKIE_KEY, newSession);
		cookie.setPath(COOKIE_PATH);
		cookie.setDomain(domain);
		cookie.setMaxAge(cookieSeconds);
		response.addCookie(cookie);
	}

	private String getSessionId(HttpServletRequest request) {
		String sessionValue = request.getParameter(COOKIE_KEY);
		if (sessionValue != null) {
			System.out.println("cookie : request params[" + COOKIE_KEY + "] | " + sessionValue);
			return sessionValue;
		}
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			System.out.println("cookie : " + COOKIE_KEY + " | " + null);
			return null;
		}
		for (Cookie cookie : cookies) {
			if (COOKIE_KEY.equals(cookie.getName())) {
				System.out.println("cookie : " + COOKIE_KEY + " | " + cookie.getValue());
				return cookie.getValue();
			}
		}
		System.out.println("cookie : " + COOKIE_KEY + " | " + null);
		return null;
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		systemName = filterConfig.getInitParameter("systemName");
		serviceUrl = filterConfig.getInitParameter("serviceUrl");
		innerServiceUrl = filterConfig.getInitParameter("innerServiceUrl");
		if (innerServiceUrl == null) {
			innerServiceUrl = serviceUrl;
		}
		noPermissionsPage = filterConfig.getInitParameter("noPermissionsPage");
		onOff = "1".equals(filterConfig.getInitParameter("on-off"));
		try {
			cookieSeconds = Integer.parseInt(filterConfig.getInitParameter("cookieSeconds"));
		} catch (Exception e) {
			System.err.println("初始化cookie时间出错。");
		}
		ignoreUrlsMap = new HashSet<String>();
		String ignoreUrls = filterConfig.getInitParameter("ignoreUrls");
		if (ignoreUrls != null && !"".equals(ignoreUrls)) {
			String[] urls = ignoreUrls.split(",");
			for (String url : urls) {
				ignoreUrlsMap.add(url);
			}
		}
		System.out.println("init filter systemName : " + systemName);
		System.out.println("init filter serviceUrl : " + serviceUrl);
		System.out.println("init filter innerServiceUrl : " + innerServiceUrl);
		System.out.println("init filter noPermissionsPage : " + noPermissionsPage);
		System.out.println("init filter cookieSeconds : " + cookieSeconds);
		System.out.println("init filter on-off : " + onOff);
		System.out.println("init filter ignoreUrls : " + ignoreUrls);
		System.out.println("init filter ignoreUrlsMap : " + ignoreUrlsMap);
	}

	public void destroy() {
	}

	public static String findOneStrByReg(final String str, final String reg) {
		try {
			return findStrByReg(str, reg).get(0);
		} catch (IndexOutOfBoundsException e) {
		}
		return null;
	}

	public static List<String> findStrByReg(final String str, final String reg) {
		List<String> list = new ArrayList<String>();
		if (str == null || reg == null) {
			return list;
		}
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(str);
		while (m.find()) {
			for (int i = 1; i <= m.groupCount(); i++) {
				list.add(m.group(i));
			}
		}
		return list;
	}

}
