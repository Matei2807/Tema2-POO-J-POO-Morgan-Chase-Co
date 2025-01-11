package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public final class UpgradePlanErrorTransaction extends Transaction {
    public UpgradePlanErrorTransaction(final CommandInput command, final String error) {
        super(command.getTimestamp(), error.equals("samePlan")
                ? "The user already has the " + command.getNewPlanType() + " plan."
                : "", command.getAccount());
        setTransactionType("upgradePlanError");
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
