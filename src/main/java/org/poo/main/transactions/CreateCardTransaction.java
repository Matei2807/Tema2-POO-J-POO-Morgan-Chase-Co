package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public final class CreateCardTransaction extends Transaction {
    private String cardNumber;
    private String email;
    private String account;

    public CreateCardTransaction(final CommandInput command) {
        super(command.getTimestamp(), "New card created", command.getAccount());
        setTransactionType("createCard");
        this.cardNumber = command.getCardNumber();
        this.email = command.getEmail();
        this.account = command.getAccount();
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transactionObject = transactionsArray.addObject();
        transactionObject.put("timestamp", getTimestamp());
        transactionObject.put("description", getDescription());
        transactionObject.put("account", account);
        transactionObject.put("card", cardNumber);
        transactionObject.put("cardHolder", email);
    }

    @Override
    public boolean checkTransactionForAccount(final String accountNumber) {
        return account.equals(accountNumber) || accountNumber.equals(getOriginAccount());
    }

    @Override
    public void splitAmount(final int numberOfAccounts) {
        // Do nothing
    }
}
