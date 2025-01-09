package org.poo.main.Transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public class UpgradePlanErrorTransaction extends Transaction {
    public UpgradePlanErrorTransaction(CommandInput command, String error) {
        super(command.getTimestamp(), error.equals("samePlan") ? "The user already has the " + command.getNewPlanType() + " plan." : "", command.getAccount());
        setTransactionType("upgradePlanError");
    }

    @Override
    public void printJSONObject(ArrayNode transactionsArray) {
        ObjectNode transactionObject = transactionsArray.addObject();
        transactionObject.put("timestamp", getTimestamp());
        transactionObject.put("description", getDescription());
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
