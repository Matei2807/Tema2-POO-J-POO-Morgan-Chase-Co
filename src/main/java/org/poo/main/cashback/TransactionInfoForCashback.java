package org.poo.main.cashback;

public final class TransactionInfoForCashback {
    private int nrOfTransactions; // number of transactions at the commerciant
    private double spentAmount; // total amount spent at the commerciant in RON

    public TransactionInfoForCashback() {
        this.nrOfTransactions = 0;
        this.spentAmount = 0;
    }

    /**
     * Add a transaction to the current commerciant
     * @param amount
     */
    public void addTransaction(final double amount) {
        this.nrOfTransactions++;
        this.spentAmount += amount;
    }

    public int getNrOfTransactions() {
        return nrOfTransactions;
    }

    public double getSpentAmount() {
        return spentAmount;
    }
}
