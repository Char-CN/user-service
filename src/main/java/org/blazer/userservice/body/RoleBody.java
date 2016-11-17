package org.blazer.userservice.body;

import java.util.List;

import org.blazer.userservice.entity.USRole;

/**
 * 为了兼容保存角色时候的body类
 * 
 * @author hyy
 *
 */
public class RoleBody extends USRole {

	private List<Integer> ids;

	public List<Integer> getIds() {
		return ids;
	}

	public void setIds(List<Integer> ids) {
		this.ids = ids;
	}

	@Override
	public String toString() {
		return "RoleBody [ids=" + ids + ", id=" + id + ", roleName=" + roleName + ", remark=" + remark + "]";
	}

}
