package org.poo.main.Transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

import java.util.List;

public final class SplitTransaction extends Transaction {
    private double amount;
    private String currency;
    private String transferType;
    private List<String> involvedAccounts;
    private String error;

    public SplitTransaction(final CommandInput command, final String transferType) {
        super(command.getTimestamp(),
    "Split payment of " + String.format("%.2f", command.getAmount()) + " " + command.getCurrency(),
              command.getAccount());
        setTransactionType("splitTransaction");
        this.amount = command.getAmount();
        this.currency = command.getCurrency();
        this.transferType = transferType;
        this.involvedAccounts = command.getAccounts();
        if (transferType.equals("error")) {
            this.error = "Account " + command.getAccount()
                         + " has insufficient funds for a split payment.";
        }
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transactionObject = transactionsArray.addObject();
        transactionObject.put("timestamp", getTimestamp());
        transactionObject.put("description", getDescription());
        transactionObject.put("currency", currency);
        transactionObject.put("amount", amount);
        ArrayNode involvedAccountsArray = transactionObject.putArray("involvedAccounts");
        for (String account : involvedAccounts) {
            involvedAccountsArray.add(account);
        }
        if (transferType.equals("error")) {
            transactionObject.put("error", error);
        }
    }

    @Override
    public boolean checkTransactionForAccount(final String accountNumber) {
        return involvedAccounts.contains(accountNumber) || accountNumber.equals(getOriginAccount());
    }

    /**
     * Splits the amount of the transaction between the involved accounts.
     * @param numberOfAccounts the number of accounts involved in the transaction
     */
    public void splitAmount(final int numberOfAccounts) {
        this.amount /= numberOfAccounts;
    }
}
