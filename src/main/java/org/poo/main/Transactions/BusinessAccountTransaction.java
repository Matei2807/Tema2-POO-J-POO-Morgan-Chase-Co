package org.poo.main.Transactions;

public class BusinessAccountTransaction {
    private final int timestamp;
    private final String senderEmail;
    private final String receiverCommerciant; // the name of the commerciant / empty if it is a user
    private final double spentAmount; // if it is negative, it is a deposit

    public BusinessAccountTransaction(int timestamp, String senderEmail, String receiverCommerciant, double spentAmount) {
        this.timestamp = timestamp;
        this.senderEmail = senderEmail;
        this.receiverCommerciant = receiverCommerciant;
        this.spentAmount = spentAmount;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getReceiverCommerciant() {
        return receiverCommerciant;
    }

    public double getSpentAmount() {
        return spentAmount;
    }
}
