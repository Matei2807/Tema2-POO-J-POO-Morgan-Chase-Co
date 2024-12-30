package org.poo.main.Transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public class CardDestroyedTransaction extends Transaction{
    private String cardNumber;
    private String cardHolder;

    public CardDestroyedTransaction(CommandInput command) {
        super(command.getTimestamp(), "The card has been destroyed", command.getAccount());
        setTransactionType("cardDestroyed");
        this.cardNumber = command.getCardNumber();
        this.cardHolder = command.getEmail();
    }

    @Override
    public void printJSONObject(ArrayNode transactionsArray) {
        ObjectNode transaction = transactionsArray.addObject();
        transaction.put("timestamp", getTimestamp());
        transaction.put("description", getDescription());
        transaction.put("card", cardNumber);
        transaction.put("cardHolder", cardHolder);
        transaction.put("account", getOriginAccount());
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
