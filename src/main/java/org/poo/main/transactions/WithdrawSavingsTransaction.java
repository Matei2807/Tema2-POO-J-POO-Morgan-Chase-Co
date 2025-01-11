package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public final class WithdrawSavingsTransaction extends Transaction {
    private String reciverAccount;
    private double amount;
    public WithdrawSavingsTransaction(final CommandInput command) {
        super(command.getTimestamp(), "Savings withdrawal", command.getAccounts().getFirst());
        reciverAccount = command.getAccounts().get(1);
        amount = command.getAmount();
        setTransactionType("withdrawSavings");
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transactionObject = transactionsArray.addObject();
        transactionObject.put("timestamp", getTimestamp());
        transactionObject.put("description", getDescription());
        transactionObject.put("amount", amount);
        transactionObject.put("savingsAccountIBAN", getOriginAccount());
        transactionObject.put("classicAccountIBAN", reciverAccount);
    }

    @Override
    public boolean checkTransactionForAccount(final String accountNumber) {
        return accountNumber.equals(getOriginAccount()) || accountNumber.equals(reciverAccount);
    }

    @Override
    public void splitAmount(final int numberOfAccounts) {
        // Do nothing
    }
}
