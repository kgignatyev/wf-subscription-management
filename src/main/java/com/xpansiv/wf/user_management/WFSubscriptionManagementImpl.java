package com.xpansiv.wf.user_management;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Date;

public class WFSubscriptionManagementImpl implements WFSubscriptionManagement {

    private SubscriptionStatus subscriptionStatus;
    private SubscriptionRequest subscriptionRequest;
    private Date nextPaymentDue;
    private boolean cancellationRequested = false;
    private PaymentInfo paymentInfo;
    private final ActivityOptions notificationOptions = buildActivityOptions("USER_NOTIFICATION_ACT");

    private final UserNotificationActivity userNotificationActivity =
            Workflow.newActivityStub(UserNotificationActivity.class, notificationOptions);


    public ActivityOptions buildActivityOptions(String queueName) {
        return ActivityOptions.newBuilder()
                .setTaskQueue(queueName)
                .setStartToCloseTimeout(Duration.ofSeconds(5)).build();
    }

    @Override
    public void subscribeForService(SubscriptionRequest sr) {
        subscriptionStatus = SubscriptionStatus.TRIAL;
        nextPaymentDue = new Date(Workflow.currentTimeMillis() + daysAsMillis(10));
        Workflow.await(Duration.ofMillis(daysAsMillis(11)), () -> cancellationRequested || (paymentInfo != null));
        if (!cancellationRequested && paymentInfo == null) {
            userNotificationActivity.notifyUser(new UserNotification()
                    .setUserId(sr.userId)
                    .setMessage("Trial expired, please pay"));
        }
    }

    private long daysAsMillis(int days) {
        return days * 24 * 3600000L;
    }

    @Override
    public SubscriptionStatus status() {
        return subscriptionStatus;
    }

    @Override
    public Date nextPaymentDue() {
        return nextPaymentDue;
    }

    @Override
    public void payment(PaymentInfo pi) {

    }

    @Override
    public void cancel() {

    }
}
