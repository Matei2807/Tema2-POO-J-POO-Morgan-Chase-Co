package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public final class CardDestroyedTransaction extends Transaction {
    private String cardNumber;
    private String cardHolder;

    public CardDestroyedTransaction(final CommandInput command) {
        super(command.getTimestamp(), "The card has been destroyed", command.getAccount());
        setTransactionType("cardDestroyed");
        this.cardNumber = command.getCardNumber();
        this.cardHolder = command.getEmail();
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transaction = transactionsArray.addObject();
        transaction.put("timestamp", getTimestamp());
        transaction.put("description", getDescription());
        transaction.put("card", cardNumber);
        transaction.put("cardHolder", cardHolder);
        transaction.put("account", getOriginAccount());
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
