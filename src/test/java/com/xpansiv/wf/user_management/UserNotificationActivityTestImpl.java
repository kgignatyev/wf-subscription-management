package com.xpansiv.wf.user_management;

import java.util.ArrayList;

public class UserNotificationActivityTestImpl implements UserNotificationActivity {


    public ArrayList<UserNotification> receivedNotifications = new ArrayList<>();


    @Override
    public void notifyUser(UserNotification notification) {
        System.out.println("UserNotification got:" + notification );
        receivedNotifications.add(notification);

    }
}
