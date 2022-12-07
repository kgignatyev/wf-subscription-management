package com.xpansiv.wf.user_management;


import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface UserNotificationActivity {
    void notifyUser(UserNotification notification);
}
