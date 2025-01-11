package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public final class AddInterestTransaction extends Transaction {
    private double amount;
    private String currency;

    public AddInterestTransaction(final CommandInput command) {
        super(command.getTimestamp(), "Interest rate income", command.getAccount());
        setTransactionType("addInterest");
        this.amount = command.getAmount();
        this.currency = command.getCurrency();
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transaction = transactionsArray.addObject();
        transaction.put("timestamp", getTimestamp());
        transaction.put("description", getDescription());
        transaction.put("amount", amount);
        transaction.put("currency", currency);
    }

    @Override
    public boolean checkTransactionForAccount(final String accountNumber) {
        return accountNumber.equals(getOriginAccount());
    }

    @Override
    public void splitAmount(final int numberOfAccounts) {
        // Do nothing
    }
}
