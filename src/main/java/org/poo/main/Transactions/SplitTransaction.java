package org.poo.main.Transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Accounts.Account;
import org.poo.main.Bank;

import java.util.List;

public final class SplitTransaction extends Transaction {
    private double amount;
    private String currency;
    private String transferType;
    private List<String> involvedAccounts;
    private List<Double> amountsForAccounts;
    private String error;
    private String type;

    public SplitTransaction(final CommandInput command, final String transferType) {
        super(command.getTimestamp(),
    "Split payment of " + String.format("%.2f", command.getAmount()) + " " + command.getCurrency(),
              command.getAccount());
        setTransactionType("splitTransaction");
        this.amount = command.getAmount();
        this.currency = command.getCurrency();
        this.transferType = transferType;
        this.involvedAccounts = command.getAccounts();
        this.amountsForAccounts = command.getAmountForUsers();
        this.type = command.getSplitPaymentType();
        if (transferType.equals("errorNoFunds")) {
            this.error = "Account " + command.getAccount()
                         + " has insufficient funds for a split payment.";
        } else if (transferType.equals("errorRejected")) {
            this.error = "One user rejected the payment.";
        }
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transactionObject = transactionsArray.addObject();
        transactionObject.put("timestamp", getTimestamp());
        transactionObject.put("description", getDescription());
        transactionObject.put("currency", currency);
        ArrayNode involvedAccountsArray = transactionObject.putArray("involvedAccounts");
        for (String account : involvedAccounts) {
            involvedAccountsArray.add(account);
        }

        if (type.equals("equal")) {
            transactionObject.put("amount", amount);
        } else {
            ArrayNode amountsForAccountsArray = transactionObject.putArray("amountForUsers");
            for (Double amount : amountsForAccounts) {
                amountsForAccountsArray.add(amount);
            }
        }

        transactionObject.put("splitPaymentType", type);
        if (transferType.equals("errorNoFunds") || transferType.equals("errorRejected")) {
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
