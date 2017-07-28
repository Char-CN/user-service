package org.blazer.userservice.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blazer.userservice.cache.PermissionsCache;
import org.blazer.userservice.cache.SystemCache;
import org.blazer.userservice.cache.UserCache;
import org.blazer.userservice.model.CacheModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller(value = "cacheAction")
@RequestMapping("/cache")
public class CacheAction extends BaseAction {

	private static Logger logger = LoggerFactory.getLogger(CacheAction.class);

	@Autowired
	UserCache userCache;

	@Autowired
	SystemCache systemCache;

	@Autowired
	PermissionsCache permissionsCache;

	@ResponseBody
	@RequestMapping("/space")
	public List<CacheModel> list(HttpServletRequest request, HttpServletResponse response) {
		List<CacheModel> list = new ArrayList<CacheModel>();
		list.add(userCache.getCalcCacheModel());
		list.add(systemCache.getCalcCacheModel());
		list.add(permissionsCache.getCalcCacheModel());
		logger.debug("Use Space : " + list.toString());
		return list;
	}

}
