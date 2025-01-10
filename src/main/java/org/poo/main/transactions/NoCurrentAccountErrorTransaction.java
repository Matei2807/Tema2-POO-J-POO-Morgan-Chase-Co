package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public class NoCurrentAccountErrorTransaction extends Transaction {
    public NoCurrentAccountErrorTransaction(CommandInput command) {
        super(command.getTimestamp(), "You do not have a classic account.", command.getAccount());
        setTransactionType("noCurrentAccountError");
    }

    @Override
    public void printJSONObject(ArrayNode transactionsArray) {
        ObjectNode transaction = transactionsArray.addObject();
        transaction.put("timestamp", getTimestamp());
        transaction.put("description", getDescription());
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
