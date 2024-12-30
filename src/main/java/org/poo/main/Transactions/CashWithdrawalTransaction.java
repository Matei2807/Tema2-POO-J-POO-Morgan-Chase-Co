package org.poo.main.Transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public class CashWithdrawalTransaction extends Transaction {
    private final double amount;

    public CashWithdrawalTransaction(CommandInput command) {
        super(command.getTimestamp(), "Cash withdrawal of " + command.getAmount(), command.getAccount());
        setTransactionType("cashWithdrawal");
        this.amount = command.getAmount();
    }

    @Override
    public void printJSONObject(ArrayNode transactionsArray) {
        ObjectNode transaction = transactionsArray.addObject();
        transaction.put("timestamp", getTimestamp());
        transaction.put("description", getDescription());
        transaction.put("amount", amount);
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
