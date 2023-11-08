package br.com.fiap.festeat.sts.request;

public class UserLoginRequestPayload {

    public UserLoginRequestPayload(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public UserLoginRequestPayload() {
    }

    private String userName;
    private String password;
    private String newPassword;

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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}