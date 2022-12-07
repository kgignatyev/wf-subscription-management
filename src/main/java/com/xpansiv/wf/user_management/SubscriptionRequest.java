package com.xpansiv.wf.user_management;

public class SubscriptionRequest {
    public String userId;
    public String product;

    public SubscriptionRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public SubscriptionRequest setProduct(String product) {
        this.product = product;
        return this;
    }
}
