package org.blazer.userservice.entity;

public class USRole {

	protected Integer id;
	protected String roleName;
	protected String remark;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public String toString() {
		return "USRole [id=" + id + ", roleName=" + roleName + ", remark=" + remark + "]";
	}

}
