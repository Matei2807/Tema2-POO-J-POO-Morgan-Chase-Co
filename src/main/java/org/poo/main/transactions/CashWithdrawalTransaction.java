package org.poo.main.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public final class CashWithdrawalTransaction extends Transaction {
    private final double amount;

    public CashWithdrawalTransaction(final CommandInput command) {
        super(command.getTimestamp(), "Cash withdrawal of " + command.getAmount(),
                command.getAccount());
        setTransactionType("cashWithdrawal");
        this.amount = command.getAmount();
    }

    @Override
    public void printJSONObject(final ArrayNode transactionsArray) {
        ObjectNode transaction = transactionsArray.addObject();
        transaction.put("timestamp", getTimestamp());
        transaction.put("description", getDescription());
        transaction.put("amount", amount);
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
