package org.poo.main.cashback;

import org.poo.main.accounts.Account;
import org.poo.main.users.User;

import java.util.Map;

public class NrOfTransactionsStrategy implements CashbackStrategy {
    static final int SMALL_NR_OF_TRANSACTIONS = 2;
    static final int MEDIUM_NR_OF_TRANSACTIONS = 5;
    static final int LARGE_NR_OF_TRANSACTIONS = 10;
    static final double FOOD_CASHBACK = 0.02;
    static final double CLOTHES_CASHBACK = 0.05;
    static final double TECH_CASHBACK = 0.1;

    /**
     * This method calculates the cashback for a transaction based on the number of transactions
     * at this type of commerciant
     * @param info
     * @param commerciantType
     * @param account
     * @param user
     * @param spentAmount
     * @return
     */
    @Override
    public double getCashback(final TransactionInfoForCashback info, final String commerciantType,
                              final Account account, final User user, final double spentAmount) {
        int nrOfTransactions = info.getNrOfTransactions();
        Map<String, Double> cashbackMap = user.getCashbackMap();

        if (cashbackMap.containsKey(commerciantType)) {
            double cashback = cashbackMap.get(commerciantType);
            if (cashback == -1.0) {
                // if the cashback was already used, we return 0.0
                return 0.0;
            } else if (cashback != 0.0) {
                // if is was not used, but is set to a value, we return the value, and set to used
                cashbackMap.put(commerciantType, -1.0);
                return cashback;
            } // else the cashback is 0.0, so we continue
        }

        if (nrOfTransactions >= LARGE_NR_OF_TRANSACTIONS) {
            if (commerciantType.equals("Food")) {
                cashbackMap.put("Food", FOOD_CASHBACK);
                return 0.0;
            } else if (commerciantType.equals("Clothes")) {
                cashbackMap.put("Clothes", CLOTHES_CASHBACK);
                return 0.0;
            } else if (commerciantType.equals("Tech")) {
                cashbackMap.put("Tech", TECH_CASHBACK);
                return 0.0;
            } else {
                return 0.0;
            }
        } else if (nrOfTransactions >= MEDIUM_NR_OF_TRANSACTIONS) {
            if (commerciantType.equals("Food")) {
                cashbackMap.put("Food", FOOD_CASHBACK);
                return 0.0;
            } else if (commerciantType.equals("Clothes")) {
                cashbackMap.put("Clothes", CLOTHES_CASHBACK);
                return 0.0;
            } else {
                return 0.0;
            }
        } else if (nrOfTransactions >= SMALL_NR_OF_TRANSACTIONS) {
            if (commerciantType.equals("Food")) {
                cashbackMap.put("Food", FOOD_CASHBACK);
                return 0.0;
            } else {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }
}
