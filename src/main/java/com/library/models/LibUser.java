package com.library.models;

public class LibUser {
	private final int IdUser;
	private final String Username;
	private final String UserPass;
	private final boolean FirstLogin;
	private final boolean IsAdmin;
	private final String ChosenPath;

	public LibUser(int IdUser, String Username, String UserPass, boolean FirstLogin, boolean IsAdmin, String ChosenPath) {
		this.IdUser = IdUser;
		this.Username = Username;
		this.UserPass = UserPass;
		this.FirstLogin = FirstLogin;
		this.IsAdmin = IsAdmin;
		this.ChosenPath = ChosenPath;
	}
    public int getIdUser() { return IdUser; }
    public String getUsername() { return Username; }
    public String getUserPass() { return UserPass; }
    public boolean isFirstLogin() { return FirstLogin; }
    public boolean isAdmin() { return IsAdmin; }
	public String getChosenPath() { return ChosenPath; }
}
