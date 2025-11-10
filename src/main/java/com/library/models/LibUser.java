package com.library.models;

public class LibUser {
	private int IdUser;
	private String Username;
	private String UserPass;

	public LibUser(int IdUser, String Username, String UserPass) {
		this.IdUser = IdUser;
		this.Username = Username;
		this.UserPass = UserPass;
	}
    public int getIdUser() { return IdUser; }
    public String getUsername() { return Username; }
    public String getUserPass() { return UserPass; }
}
