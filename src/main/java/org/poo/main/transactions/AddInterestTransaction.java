package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public class AddInterestTransaction extends Transaction{
    private double amount;
    private String currency;

    public AddInterestTransaction(CommandInput command) {
        super(command.getTimestamp(), "Interest rate income", command.getAccount());
        setTransactionType("addInterest");
        this.amount = command.getAmount();
        this.currency = command.getCurrency();
    }

    @Override
    public void printJSONObject(ArrayNode transactionsArray) {
        ObjectNode transaction = transactionsArray.addObject();
        transaction.put("timestamp", getTimestamp());
        transaction.put("description", getDescription());
        transaction.put("amount",amount);
        transaction.put("currency", currency);
    }

    @Override
    public boolean checkTransactionForAccount(String accountNumber) {
        return accountNumber.equals(getOriginAccount());
    }

    @Override
    public void splitAmount(int numberOfAccounts) {
        // Do nothing
    }
}
