package com.taskadapter.webui.service;


public abstract class CurrentUserInfo {

    public CurrentUserInfo() {
        super();
    }

    public abstract void addChangeEventListener(LoginEventListener listener);

    public abstract String getUserName();

    public abstract boolean isLoggedIn();

}