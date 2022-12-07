package com.xpansiv.wf.user_management;

import java.util.ArrayList;

public class ServiceManagementActivityTestImpl implements ServiceManagementActivity {


    public ArrayList<UserNotification> receivedNotifications = new ArrayList<>();


    @Override
    public void notifyUser(UserNotification notification) {
        System.out.println("UserNotification got:" + notification );
        receivedNotifications.add(notification);

    }

    @Override
    public void cancelSubscription(SubscriptionRequest sr) {
        System.out.println("Cancelling Subscription = " + sr);
    }

    @Override
    public void chargeServiceFee(PaymentInfo paymentInfo) {
        System.out.println("Charge user using paymentInfo = " + paymentInfo);
    }
}
