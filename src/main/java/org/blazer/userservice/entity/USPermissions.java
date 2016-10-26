package org.blazer.userservice.entity;

public class USPermissions {

	private Integer id;
	private Integer systemId;
	private Integer parentId;
	private String permissionsName;
	private String url;
	private String remark;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getSystemId() {
		return systemId;
	}

	public void setSystemId(Integer systemId) {
		this.systemId = systemId;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public String getPermissionsName() {
		return permissionsName;
	}

	public void setPermissionsName(String permissionsName) {
		this.permissionsName = permissionsName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public String toString() {
		return "USPermissions [id=" + id + ", systemId=" + systemId + ", parentId=" + parentId + ", permissionsName=" + permissionsName + ", url=" + url
				+ ", remark=" + remark + "]";
	}

}
