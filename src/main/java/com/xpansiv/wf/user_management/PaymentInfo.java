package com.xpansiv.wf.user_management;

public class PaymentInfo {
    public long amountCents;

    public String creditCardNumber;

    public PaymentInfo setAmountCents(long amountCents) {
        this.amountCents = amountCents;
        return this;
    }

    public PaymentInfo setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
        return this;
    }

    @Override
    public String toString() {
        return "PaymentInfo{" +
                "amountCents=" + amountCents +
                ", creditCardNumber='" + creditCardNumber + '\'' +
                '}';
    }
}
