package com.axibase.energinet.config;

public class Credentials {
    private String password;
    private String login;

    public Credentials(String password, String login) {
        this.password = password;
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }
}
