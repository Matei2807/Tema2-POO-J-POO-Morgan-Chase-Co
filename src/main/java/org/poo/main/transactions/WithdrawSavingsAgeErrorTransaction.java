package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public final class WithdrawSavingsAgeErrorTransaction extends Transaction {
    public WithdrawSavingsAgeErrorTransaction(final CommandInput command) {
        super(command.getTimestamp(), "You don't have the minimum age required.",
                command.getAccount());
        setTransactionType("withdrawSavingsAgeError");
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transactionObject = transactionsArray.addObject();
        transactionObject.put("timestamp", getTimestamp());
        transactionObject.put("description", getDescription());
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
