package org.blazer.userservice.action;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blazer.userservice.body.Body;
import org.blazer.userservice.body.PageBody;
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
	 * TODO : 角色相关
	 */

	@ResponseBody
	@RequestMapping("/findRoleAll")
	public List<USRole> findRoleAll(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		return roleService.findRoleAll();
	}

	@ResponseBody
	@RequestMapping("/findRoleByPage")
	public PageBody<USRole> findRoleByPage(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		return roleService.findRoleByPage(getParamMap(request));
	}

	@ResponseBody
	@RequestMapping("/findRoleById")
	public USRole findRoleById(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		HashMap<String, String> params = getParamMap(request);
		return roleService.findRoleById(params);
	}

	@ResponseBody
	@RequestMapping("/saveRole")
	public Body saveRole(@RequestBody USRole role) throws Exception {
		logger.debug("role : " + role);
		try {
			roleService.saveRole(role);
		} catch (Exception e) {
			return new Body().error().setMessage("保存失败：" + e.getMessage());
		}
		return new Body().setMessage("保存成功！");
	}

	@ResponseBody
	@RequestMapping("/delRole")
	public Body delRole(@RequestParam Integer id) throws Exception {
		logger.debug("role id : " + id);
		try {
			roleService.delRole(id);
		} catch (Exception e) {
			return new Body().error().setMessage("删除失败：" + e.getMessage());
		}
		return new Body().setMessage("删除成功！");
	}

}
