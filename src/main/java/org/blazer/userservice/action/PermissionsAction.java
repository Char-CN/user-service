package org.blazer.userservice.action;

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

	@ResponseBody
	@RequestMapping("/findPermissionsByParentID")
	public List<PermissionsTreeBody> findPermissionsByParentID(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		return permissionsService.findPermissionsByParentID(getParamMap(request));
	}

	@ResponseBody
	@RequestMapping("/findPermissionsByID")
	public USPermissions findPermissionsByID(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		return permissionsService.findPermissionsById(getParamMap(request));
	}

	@ResponseBody
	@RequestMapping("/savePermissions")
	public Body savePermissions(@RequestBody USPermissions permissions) throws Exception {
		logger.debug("permissions : " + permissions);
		try {
			permissionsService.savePermissions(permissions);
		} catch (Exception e) {
			return new Body().error().setMessage("保存失败：" + e.getMessage());
		}
		return new Body().setMessage("保存成功！");
	}

	@ResponseBody
	@RequestMapping("/delPermissions")
	public Body delPermissions(@RequestParam Integer id) throws Exception {
		logger.debug("permissions id : " + id);
		try {
			permissionsService.delPermissions(id);
		} catch (Exception e) {
			return new Body().error().setMessage("删除失败：" + e.getMessage());
		}
		return new Body().setMessage("删除成功！");
	}

}
