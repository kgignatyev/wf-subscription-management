package com.xpansiv.wf.user_management;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.ContinueAsNewOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

import static com.xpansiv.wf.user_management.ServiceManagementActivity.QUEUE_SERVICE_MANAGEMENT;

public class WFSubscriptionManagementImpl implements WFSubscriptionManagement {

    private SubscriptionStatus subscriptionStatus;
    private Date nextPaymentDue;
    private boolean cancellationRequested = false;
    private PaymentInfo paymentInfo;
    private final ActivityOptions svcManagementOptions = buildActivityOptions(QUEUE_SERVICE_MANAGEMENT);

    private final ServiceManagementActivity serviceManagementActivity =
            Workflow.newActivityStub(ServiceManagementActivity.class, svcManagementOptions);


    public ActivityOptions buildActivityOptions(String queueName) {
        return ActivityOptions.newBuilder()
                .setTaskQueue(queueName)
                .setStartToCloseTimeout(Duration.ofSeconds(5)).build();
    }

    @Override
    public void subscribeForService(SubscriptionRequest sr, PaymentInfo pi) {
        subscriptionStatus = SubscriptionStatus.TRIAL;
        if( pi == null) {//skipping because it is a continuation
            nextPaymentDue = new Date(Workflow.currentTimeMillis() + daysAsMillis(10));
            Workflow.await(Duration.ofMillis(daysAsMillis(11)), () -> cancellationRequested || (paymentInfo != null));
            if (!cancellationRequested && paymentInfo == null) {
                serviceManagementActivity.notifyUser(new UserNotification()
                        .setUserId(sr.userId)
                        .setMessage("Trial expired, please pay"));
            }
            Workflow.await(Duration.ofMillis(daysAsMillis(3)), () -> cancellationRequested || (paymentInfo != null));
        }else {
            paymentInfo = pi;
        }
        if ( paymentInfo == null) {
            serviceManagementActivity.cancelSubscription( sr );
        }else{
            int paymentsCounter = 0;
            while( !cancellationRequested ){
                System.out.println("paymentsCounter = " + paymentsCounter);
                serviceManagementActivity.chargeServiceFee( paymentInfo );
                //let's charge every 5 seconds to demonstrate continuity
                Workflow.await(Duration.ofMillis(5000),()->false );
                paymentsCounter++;
                if( paymentsCounter == 5 ){
                    Workflow.continueAsNew(
                            null,//important! means we do not override current settings
                            sr, paymentInfo );
                }
            }
            if( cancellationRequested ){
                serviceManagementActivity.cancelSubscription( sr );
            }
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
        this.paymentInfo = pi;
    }

    @Override
    public void cancel() {
        cancellationRequested = true;
    }
}
