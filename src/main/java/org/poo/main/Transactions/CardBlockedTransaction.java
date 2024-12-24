package org.poo.main.Transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public final class CardBlockedTransaction extends Transaction {

    public CardBlockedTransaction(final CommandInput command) {
        super(command.getTimestamp(), "The card is frozen", command.getAccount());
        setTransactionType("cardBlocked");
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
