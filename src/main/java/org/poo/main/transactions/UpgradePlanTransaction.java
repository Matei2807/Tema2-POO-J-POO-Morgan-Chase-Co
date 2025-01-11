package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public final class UpgradePlanTransaction extends Transaction {
    private final String newPlan;

    public UpgradePlanTransaction(final CommandInput command) {
        super(command.getTimestamp(), "Upgrade plan", command.getAccount());
        setTransactionType("upgradePlan");
        this.newPlan = command.getNewPlanType();
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transactionObject = transactionsArray.addObject();
        transactionObject.put("timestamp", getTimestamp());
        transactionObject.put("description", getDescription());
        transactionObject.put("newPlanType", newPlan);
        transactionObject.put("accountIBAN", getOriginAccount());
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
