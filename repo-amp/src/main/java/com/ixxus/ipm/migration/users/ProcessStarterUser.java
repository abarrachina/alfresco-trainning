package com.ixxus.ipm.migration.users;

public class ProcessStarterUser {
	
	private String newStartUser="";
	private String oldStartUser="";
	
	public ProcessStarterUser(String oldStartUser, String newStartUser) {
		this.newStartUser = newStartUser;
		this.oldStartUser = oldStartUser;
	}
	
	public String getNewStartUser() {
		return newStartUser;
	}
	public String getOldStartUser() {
		return oldStartUser;
	}
	
	public void setNewStartUser(String newStartUser) {
		this.newStartUser = newStartUser;
	}
	
	public void setOldStartUser(String oldStartUser) {
		this.oldStartUser = oldStartUser;
	}

}
