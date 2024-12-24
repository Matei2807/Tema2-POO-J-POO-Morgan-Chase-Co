package org.poo.main.Cashback;

public class TransactionInfoForCashback {
    int nrOfTransactions; // number of transactions at the commerciant
    double spentAmount; // total amount spent at the commerciant in RON

    public TransactionInfoForCashback() {
        this.nrOfTransactions = 0;
        this.spentAmount = 0;
    }

    public void addTransaction(double amount) {
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
