package org.blazer.userservice.model;

import java.io.Serializable;

import org.roaringbitmap.buffer.MutableRoaringBitmap;

public class UserModel implements Serializable {

	private static final long serialVersionUID = 4901063117205666555L;
	private Integer id;
	private String userName;
	private String password;
	private String userNameCn;
	private String phoneNumber;
	private String email;
	private Integer enable;
	private MutableRoaringBitmap rolesBitmap;
	private MutableRoaringBitmap permissionsBitmap;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserNameCn() {
		return userNameCn;
	}

	public void setUserNameCn(String userNameCn) {
		this.userNameCn = userNameCn;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public MutableRoaringBitmap getRolesBitmap() {
		return rolesBitmap;
	}

	public void setRolesBitmap(MutableRoaringBitmap rolesBitmap) {
		this.rolesBitmap = rolesBitmap;
	}

	public MutableRoaringBitmap getPermissionsBitmap() {
		return permissionsBitmap;
	}

	public void setPermissionsBitmap(MutableRoaringBitmap permissionsBitmap) {
		this.permissionsBitmap = permissionsBitmap;
	}

	public Integer getEnable() {
		return enable;
	}

	public void setEnable(Integer enable) {
		this.enable = enable;
	}

	@Override
	public String toString() {
		return "UserModel [id=" + id + ", userName=" + userName + ", userNameCn=" + userNameCn + ", phoneNumber=" + phoneNumber + ", email="
				+ email + ", enable=" + enable + ", rolesBitmap=" + rolesBitmap + ", permissionsBitmap=" + permissionsBitmap + "]";
	}

}
