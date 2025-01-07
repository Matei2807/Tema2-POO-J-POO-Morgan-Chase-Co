package org.poo.main.Transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Bank;

public final class CommerciantTransaction extends Transaction {
    private double amount;
    private String commerciant;
    private String senderIBAN;

    public CommerciantTransaction(final CommandInput command) {
        super(command.getTimestamp(), "Card payment", command.getAccount());
        setTransactionType("commerciantTransaction");
        this.amount = command.getAmount();
        this.commerciant = command.getCommerciant();
        this.senderIBAN = command.getAccount();
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transactionObject = transactionsArray.addObject();
        transactionObject.put("timestamp", getTimestamp());
        transactionObject.put("description", getDescription());
        transactionObject.put("amount", Bank.roundToTwoDecimals(amount));
        transactionObject.put("commerciant", commerciant);
    }

    @Override
    public boolean checkTransactionForAccount(final String accountNumber) {
        return senderIBAN.equals(accountNumber) || accountNumber.equals(getOriginAccount());
    }

    public double getAmount() {
        return amount;
    }

    public String getCommerciant() {
        return commerciant;
    }

    @Override
    public void splitAmount(final int numberOfAccounts) {
        // Do nothing
    }
}
