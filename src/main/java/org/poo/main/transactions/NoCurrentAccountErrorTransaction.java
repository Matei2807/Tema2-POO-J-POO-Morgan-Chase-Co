package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public final class NoCurrentAccountErrorTransaction extends Transaction {
    public NoCurrentAccountErrorTransaction(final CommandInput command) {
        super(command.getTimestamp(), "You do not have a classic account.", command.getAccount());
        setTransactionType("noCurrentAccountError");
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transaction = transactionsArray.addObject();
        transaction.put("timestamp", getTimestamp());
        transaction.put("description", getDescription());
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
