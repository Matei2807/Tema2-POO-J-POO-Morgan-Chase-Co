package org.poo.main.Transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public class UpgradePlanTransaction extends Transaction {
    private final String newPlan;

    public UpgradePlanTransaction(CommandInput command) {
        super(command.getTimestamp(), "Upgrade plan", command.getAccount());
        setTransactionType("upgradePlan");
        this.newPlan = command.getNewPlanType();
    }

    @Override
    public void printJSONObject(ArrayNode transactionsArray) {
        ObjectNode transactionObject = transactionsArray.addObject();
        transactionObject.put("timestamp", getTimestamp());
        transactionObject.put("description", getDescription());
        transactionObject.put("newPlanType", newPlan);
        transactionObject.put("accountIBAN", getOriginAccount());
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
