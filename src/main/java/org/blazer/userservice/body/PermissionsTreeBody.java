package org.blazer.userservice.body;

public class PermissionsTreeBody {

	private Integer id;
	private Integer systemId;
	private String systemName;
	private Integer parentId;
	private String permissionsName;
	private String text; // permission easy ui
							// 是用的treegrid，为了兼容combotree此属性和permissionsName值一样
	private String url;
	private String remark;
	private String state;
	private String iconCls;

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

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getIconCls() {
		return iconCls;
	}

	public void setIconCls(String iconCls) {
		this.iconCls = iconCls;
	}

	@Override
	public String toString() {
		return "PermissionsTreeBody [id=" + id + ", systemId=" + systemId + ", systemName=" + systemName + ", parentId=" + parentId + ", permissionsName="
				+ permissionsName + ", text=" + text + ", url=" + url + ", remark=" + remark + ", state=" + state + ", iconCls=" + iconCls + "]";
	}

}
