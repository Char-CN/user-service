package org.blazer.userservice.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blazer.userservice.body.Body;
import org.blazer.userservice.body.PermissionsTreeBody;
import org.blazer.userservice.entity.USPermissions;
import org.blazer.userservice.service.PermissionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller(value = "permissionsAction")
@RequestMapping("/permissions")
public class PermissionsAction extends BaseAction {

	private static Logger logger = LoggerFactory.getLogger(PermissionsAction.class);

	@Autowired
	PermissionsService permissionsService;

	/**
	 * 根据parentId和systemId查找权限
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/findPermissionsByParentID")
	public List<PermissionsTreeBody> findPermissionsByParentID(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			return permissionsService.findPermissionsByParentID(getParamMap(request));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<PermissionsTreeBody>();
	}

	/**
	 * 根据id查找权限
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/findPermissionsByID")
	public USPermissions findPermissionsByID(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			return permissionsService.findPermissionsById(getParamMap(request));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new USPermissions();
	}

	/**
	 * 保存权限
	 * 
	 * @param permissions
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/savePermissions")
	public Body savePermissions(@RequestBody USPermissions permissions) throws Exception {
		logger.debug("permissions : " + permissions);
		try {
			permissionsService.savePermissions(permissions);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Body().error().setMessage("保存失败：" + e.getMessage());
		}
		return new Body().setMessage("保存成功！");
	}

	/**
	 * 根据id删除权限
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/delPermissions")
	public Body delPermissions(@RequestParam Integer id) throws Exception {
		logger.debug("permissions id : " + id);
		try {
			permissionsService.delPermissions(id);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Body().error().setMessage("删除失败：" + e.getMessage());
		}
		return new Body().setMessage("删除成功！");
	}

}
