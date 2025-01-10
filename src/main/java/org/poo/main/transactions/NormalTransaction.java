package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public final class NormalTransaction extends Transaction {
    private String senderIBAN;
    private String receiverIBAN;
    private double amount;
    private String currency;
    private String transferType;

    public NormalTransaction(final CommandInput command, final String transferType) {
        super(command.getTimestamp(), command.getDescription(), command.getAccount());
        setTransactionType("normalTransaction");
        this.senderIBAN = command.getAccount();
        this.receiverIBAN = command.getReceiver();
        this.amount = command.getAmount();
        this.currency = command.getCurrency();
        this.transferType = transferType;
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transactionObject = transactionsArray.addObject();
        transactionObject.put("timestamp", getTimestamp());
        transactionObject.put("description", getDescription());
        transactionObject.put("senderIBAN", senderIBAN);
        transactionObject.put("receiverIBAN", receiverIBAN);
        transactionObject.put("amount", amount + " " + currency);
        transactionObject.put("transferType", transferType);
    }

    @Override
    public boolean checkTransactionForAccount(final String accountNumber) {
        return senderIBAN.equals(accountNumber)
               || receiverIBAN.equals(accountNumber)
               || accountNumber.equals(getOriginAccount());
    }

    @Override
    public void splitAmount(final int numberOfAccounts) {
        // Do nothing
    }
}
