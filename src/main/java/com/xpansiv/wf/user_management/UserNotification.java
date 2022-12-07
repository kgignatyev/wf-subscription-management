package com.xpansiv.wf.user_management;

public class UserNotification {
    public String userId;
    public String message;

    public UserNotification setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public UserNotification setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return "UserNotification{" +
                "userId='" + userId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
