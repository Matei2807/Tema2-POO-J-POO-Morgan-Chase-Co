package org.poo.main.transactions;

public final class BusinessAccountTransaction {
    private final int timestamp;
    private final String senderEmail;
    private final String receiverCommerciant;
    private final double spentAmount; // if it is negative, it is a deposit

    public BusinessAccountTransaction(final int timestamp, final String senderEmail,
                                      final String receiverCommerciant, final double spentAmount) {
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
