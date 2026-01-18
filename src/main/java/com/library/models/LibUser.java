package com.library.models;

public class LibUser {
	private final int IdUser;
	private final String Username;
	private final String UserPass;
	private final boolean FirstLogin;
	private final boolean IsAdmin;

	public LibUser(int IdUser, String Username, String UserPass, boolean FirstLogin, boolean IsAdmin) {
		this.IdUser = IdUser;
		this.Username = Username;
		this.UserPass = UserPass;
		this.FirstLogin = FirstLogin;
		this.IsAdmin = IsAdmin;
	}
    public int getIdUser() { return IdUser; }
    public String getUsername() { return Username; }
    public String getUserPass() { return UserPass; }
    public boolean isFirstLogin() { return FirstLogin; }
    public boolean isAdmin() { return IsAdmin; }
}
