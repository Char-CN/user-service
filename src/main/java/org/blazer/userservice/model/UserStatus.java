package org.blazer.userservice.model;

public enum UserStatus {

	DELETE(0, "删除"), ENABLE(1, "启用"), DISABLE(2, "停止");

	private Integer id;

	private String statusName;

	public Integer getId() {
		return id;
	}

	public String getStatusName() {
		return statusName;
	}

	private UserStatus(Integer id, String statusName) {
		this.id = id;
		this.statusName = statusName;
	}

	public static boolean isDelete(Integer statusId) {
		return UserStatus.DELETE.id == statusId;
	}

	public static boolean isEnable(Integer statusId) {
		return UserStatus.ENABLE.id == statusId;
	}

	public static boolean isDisable(Integer statusId) {
		return UserStatus.DISABLE.id == statusId;
	}

	public static UserStatus get(Integer statusId) {
		if (statusId == UserStatus.DELETE.id)
			return UserStatus.DELETE;
		if (statusId == UserStatus.ENABLE.id)
			return UserStatus.ENABLE;
		if (statusId == UserStatus.DISABLE.id)
			return UserStatus.DISABLE;
		return UserStatus.DELETE;
	}

	public static void main(String[] args) {
		System.out.println(ENABLE.id);
		System.out.println(UserStatus.ENABLE.getStatusName());
		System.out.println(get(2));
	}

}
