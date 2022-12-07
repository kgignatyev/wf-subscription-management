package com.xpansiv.wf.user_management;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.Date;

@WorkflowInterface
public interface WFSubscriptionManagement {

    String WF_TASKS_QUEUE = "subscription_management";

    @WorkflowMethod
    void subscribeForService(SubscriptionRequest sr, PaymentInfo pi);


    @QueryMethod
    SubscriptionStatus status();

    @QueryMethod
    Date nextPaymentDue();

    @SignalMethod
    void payment(PaymentInfo pi);

    @SignalMethod
    void cancel();

}
