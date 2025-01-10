package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public class WithdrawSavingsTransaction extends Transaction{
    private String reciverAccount;
    private double amount;
    public WithdrawSavingsTransaction(CommandInput command) {
        super(command.getTimestamp(), "Savings withdrawal", command.getAccounts().getFirst());
        reciverAccount = command.getAccounts().get(1);
        amount = command.getAmount();
        setTransactionType("withdrawSavings");
    }

    @Override
    public void printJSONObject(ArrayNode transactionsArray) {
        ObjectNode transactionObject = transactionsArray.addObject();
        transactionObject.put("timestamp", getTimestamp());
        transactionObject.put("description", getDescription());
        transactionObject.put("amount", amount);
        transactionObject.put("savingsAccountIBAN", getOriginAccount());
        transactionObject.put("classicAccountIBAN", reciverAccount);
    }

    @Override
    public boolean checkTransactionForAccount(String accountNumber) {
        return accountNumber.equals(getOriginAccount()) || accountNumber.equals(reciverAccount);
    }

    @Override
    public void splitAmount(int numberOfAccounts) {
        // Do nothing
    }
}
