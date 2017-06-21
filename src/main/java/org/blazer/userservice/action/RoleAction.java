package org.blazer.userservice.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blazer.userservice.body.Body;
import org.blazer.userservice.body.PageBody;
import org.blazer.userservice.body.RoleBody;
import org.blazer.userservice.entity.USRole;
import org.blazer.userservice.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller(value = "roleAction")
@RequestMapping("/role")
public class RoleAction extends BaseAction {

	private static Logger logger = LoggerFactory.getLogger(RoleAction.class);

	@Autowired
	RoleService roleService;

	/**
	 * 查询所有角色
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/findRoleAll")
	public List<USRole> findRoleAll(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			return roleService.findRoleAll();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<USRole>();
	}

	/**
	 * 分页查询角色
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/findRoleByPage")
	public PageBody<USRole> findRoleByPage(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			return roleService.findRoleByPage(getParamMap(request));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new PageBody<USRole>();
	}

	/**
	 * 根据id查询角色
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/findRoleById")
	public RoleBody findRoleById(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			HashMap<String, String> params = getParamMap(request);
			return roleService.findRoleById(params);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new RoleBody();
	}

	/**
	 * 保存角色
	 * 
	 * @param role
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/saveRole")
	public Body saveRole(@RequestBody RoleBody role) throws Exception {
		logger.debug("role : " + role);
		try {
			roleService.saveRole(role);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Body().error().setMessage("保存失败：" + e.getMessage());
		}
		return new Body().setMessage("保存成功！");
	}

	/**
	 * 删除角色
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/delRole")
	public Body delRole(@RequestParam Integer id) throws Exception {
		logger.debug("role id : " + id);
		try {
			roleService.delRole(id);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Body().error().setMessage("删除失败：" + e.getMessage());
		}
		return new Body().setMessage("删除成功！");
	}

}
