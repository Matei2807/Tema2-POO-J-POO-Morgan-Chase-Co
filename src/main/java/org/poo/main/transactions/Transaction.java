package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;

public abstract class Transaction {
    private String transactionType;
    private int timestamp;
    private String description;
    private String originAccount;

    public Transaction(final int timestamp, final String description, final String originAccount) {
        this.timestamp = timestamp;
        this.description = description;
        this.originAccount = originAccount == null ? "" : originAccount;
    }

    /**
     * @return the transaction type
     */
    public String getTransactionType() {
        return transactionType;
    }

    /**
     * @param transactionType the transaction type to set
     */
    public void setTransactionType(final String transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * @return the origin account
     */
    public String getOriginAccount() {
        return originAccount;
    }

    /**
     * @return the timestamp
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * print the transaction as a JSON object
     */
    public abstract void printJSONObject(ArrayNode transactionsArray);

    /**
     * @param accountNumber the account number to check
     * @return true if the transaction is valid for the account
     */
    public abstract boolean checkTransactionForAccount(String accountNumber);

    /**
     * @param numberOfAccounts the number of accounts to split the amount
     */
    public abstract void splitAmount(int numberOfAccounts);
}
