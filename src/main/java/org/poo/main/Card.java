package org.poo.main;

import org.poo.utils.Utils;

public final class Card {
    private final String cardNumber;
    private String status;
    private final boolean oneTime;

    public Card(final boolean oneTime) {
        cardNumber = Utils.generateCardNumber();
        status = "active";
        this.oneTime = oneTime;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public boolean isOneTime() {
        return oneTime;
    }
}
