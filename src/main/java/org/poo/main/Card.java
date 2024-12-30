package org.poo.main;

import org.poo.utils.Utils;

public final class Card {
    private final String cardNumber;
    private String status;
    private final boolean oneTime;
    private String ownerEmail; // the email of the user who created the card

    public Card(final boolean oneTime) {
        cardNumber = Utils.generateCardNumber();
        status = "active";
        this.oneTime = oneTime;
        ownerEmail = "";
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

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(final String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
