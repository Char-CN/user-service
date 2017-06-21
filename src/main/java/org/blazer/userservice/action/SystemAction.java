package org.blazer.userservice.action;

import java.util.ArrayList;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blazer.userservice.body.Body;
import org.blazer.userservice.body.PageBody;
import org.blazer.userservice.body.TreeBody;
import org.blazer.userservice.entity.USSystem;
import org.blazer.userservice.service.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller(value = "systemAction")
@RequestMapping("/system")
public class SystemAction extends BaseAction {

	private static Logger logger = LoggerFactory.getLogger(SystemAction.class);

	@Autowired
	SystemService systemService;

	/**
	 * 查询所有系统
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/findSystemAll")
	public List<USSystem> findSystemAll(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			return systemService.findSystemAll();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<USSystem>();
	}

	/**
	 * 查询所有可见系统
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/findSystemAllByDisplay")
	public List<USSystem> findSystemAllByDisplay(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			return systemService.findSystemAllByDisplay();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<USSystem>();
	}

	/**
	 * 查询系统权限的树
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/findSystemAndPermissionsTree")
	public List<TreeBody> findSystemAndPermissionsTree(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			return systemService.findSystemAndPermissionsTree();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<TreeBody>();
	}

	/**
	 * 分页查询系统
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/findSystemByPage")
	public PageBody<USSystem> findSystemByPage(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			return systemService.findSystemByPage(getParamMap(request));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new PageBody<USSystem>();
	}

	/**
	 * 根据id查询系统
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/findSystemById")
	public USSystem findSystemById(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			return systemService.findSystemById(getParamMap(request));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new USSystem();
	}

	/**
	 * 保存系统
	 * 
	 * @param system
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/saveSystem")
	public Body saveSystem(@RequestBody USSystem system) throws Exception {
		logger.debug("system : " + system);
		try {
			systemService.saveSystem(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Body().error().setMessage("保存失败：" + e.getMessage());
		}
		return new Body().setMessage("保存成功！");
	}

	/**
	 * 根据id删除系统
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/delSystem")
	public Body delSystem(@RequestParam Integer id) throws Exception {
		logger.debug("system id : " + id);
		try {
			systemService.delSystem(id);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Body().error().setMessage("删除失败：" + e.getMessage());
		}
		return new Body().setMessage("删除成功！");
	}

}
