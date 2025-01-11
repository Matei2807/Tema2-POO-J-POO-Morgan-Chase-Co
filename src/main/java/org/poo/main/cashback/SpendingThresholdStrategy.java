package org.poo.main.cashback;

import org.poo.main.accounts.Account;
import org.poo.main.users.User;

import java.util.Map;

public class SpendingThresholdStrategy implements CashbackStrategy {
    static final int SMALL_SPENT_THRESHOLD = 100;
    static final int MEDIUM_SPENT_THRESHOLD = 300;
    static final int LARGE_SPENT_THRESHOLD = 500;
    static final double SMALL_MIN_CASHBACK = 0.001;
    static final double SMALL_MED_CASHBACK = 0.002;
    static final double SMALL_MAX_CASHBACK = 0.0025;
    static final double MEDIUM_MIN_CASHBACK = 0.003;
    static final double MEDIUM_MED_CASHBACK = 0.004;
    static final double MEDIUM_MAX_CASHBACK = 0.005;
    static final double LARGE_MIN_CASHBACK = 0.005;
    static final double LARGE_MED_CASHBACK = 0.0055;
    static final double LARGE_MAX_CASHBACK = 0.007;

    /**
     * This method calculates the cashback for a user based on the spent amount and the user's plan
     * @param info
     * @param commerciantType
     * @param account
     * @param user
     * @param amount
     * @return
     */
    @Override
    public double getCashback(final TransactionInfoForCashback info, final String commerciantType,
                              final Account account, final User user, final double amount) {
        String plan = user.getPlan();
        account.addTransactionInfoForSpendingThreshold(amount);
        double spentAmount = account.getTransactionInfoForSpendingThreshold().getSpentAmount();

        Map<String, Double> cashbackMap = user.getCashbackMap();
        double addedCashback = 0.0;
        if (cashbackMap.containsKey(commerciantType)) {
            double cashback = cashbackMap.get(commerciantType);
            if (cashback != 0.0) {
                // cashback was not used, but set to a value, we return that value, and set as used
                cashbackMap.put(commerciantType, -1.0);
                addedCashback = cashback;
            } // else the cashback is 0.0, so we continue
        }

        double cashback = 0.0;
        switch (plan) {
            case "standard", "student" -> {
                if (spentAmount >= LARGE_SPENT_THRESHOLD) {
                    cashback = SMALL_MAX_CASHBACK;
                } else if (spentAmount >= MEDIUM_SPENT_THRESHOLD) {
                    cashback = SMALL_MED_CASHBACK;
                } else if (spentAmount >= SMALL_SPENT_THRESHOLD) {
                    cashback = SMALL_MIN_CASHBACK;
                }
            }
            case "silver" -> {
                if (spentAmount >= LARGE_SPENT_THRESHOLD) {
                    cashback = MEDIUM_MAX_CASHBACK;
                } else if (spentAmount >= MEDIUM_SPENT_THRESHOLD) {
                    cashback = MEDIUM_MED_CASHBACK;
                } else if (spentAmount >= SMALL_SPENT_THRESHOLD) {
                    cashback = MEDIUM_MIN_CASHBACK;
                }
            }
            case "gold" -> {
                if (spentAmount >= LARGE_SPENT_THRESHOLD) {
                    cashback = LARGE_MAX_CASHBACK;
                } else if (spentAmount >= MEDIUM_SPENT_THRESHOLD) {
                    cashback = LARGE_MED_CASHBACK;
                } else if (spentAmount >= SMALL_SPENT_THRESHOLD) {
                    cashback = LARGE_MIN_CASHBACK;
                }
            }
            default -> {
                System.out.println("Invalid plan");
            }
        }
        return cashback + addedCashback;
    }
}
