package org.poo.main.Cashback;

import org.poo.main.Accounts.Account;
import org.poo.main.Commerciant;

public class NrOfTransactionsStrategy implements CashbackStrategy {
    @Override
    public double getCashback(TransactionInfoForCashback info, String commerciantType, Account account) {
        int nrOfTransactions = info.getNrOfTransactions();
        if (nrOfTransactions >= 10) {
            return switch (commerciantType) {
                case "Food" -> 0.02;
                case "Clothes" -> 0.05;
                case "Tech" -> 0.1;
                default -> 0.0;
            };
        } else if (nrOfTransactions >= 5) {
            return switch (commerciantType) {
                case "Food" -> 0.02;
                case "Clothes" -> 0.05;
                default -> 0.0;
            };
        } else if (nrOfTransactions >= 2) {
            return switch (commerciantType) {
                case "Food" -> 0.02;
                default -> 0.0;
            };
        } else {
            return 0.0;
        }
    }
}
