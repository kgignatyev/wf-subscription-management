package com.xpansiv.wf.user_management;


import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface ServiceManagementActivity {

    public static final String QUEUE_SERVICE_MANAGEMENT = "SERVICE_MANAGEMENT_TASKS";

    void notifyUser(UserNotification notification);

    void cancelSubscription(SubscriptionRequest sr);

    void chargeServiceFee(PaymentInfo paymentInfo);
}
