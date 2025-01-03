package org.poo.main.Cashback;

import org.poo.main.Accounts.Account;
import org.poo.main.Commerciant;
import org.poo.main.Users.User;

import java.util.Map;

public class SpendingThresholdStrategy implements CashbackStrategy {
    @Override
    public double getCashback(TransactionInfoForCashback info, String commerciantType, User user, double spentAmount) {
        String plan = user.getPlan();
        user.addTransactionInfoForSpendingThreshold(spentAmount); // add the spent amount to the user's transaction info
        spentAmount = user.getTransactionInfoForSpendingThreshold().getSpentAmount(); // get the total spent amount

        Map<String, Double> cashbackMap = user.getCashbackMap();
        double addedCashback = 0.0;
        if (cashbackMap.containsKey(commerciantType)) {
            double cashback = cashbackMap.get(commerciantType);
            if (cashback != 0.0) { // if the cashback was not used, but it set to a value, we return that value, and set as used
                cashbackMap.put(commerciantType, -1.0);
                addedCashback = cashback;
            } // else the cashback is 0.0, so we continue
        }

        double cashback = 0.0;
        switch (plan) {
            case "standard", "student" -> {
                if (spentAmount >= 500) {
                    cashback = 0.0025;
                } else if (spentAmount >= 300) {
                    cashback = 0.002;
                } else if (spentAmount >= 100) {
                    cashback = 0.001;
                }
            }
            case "silver" -> {
                if (spentAmount >= 500) {
                    cashback = 0.005;
                } else if (spentAmount >= 300) {
                    cashback = 0.004;
                } else if (spentAmount >= 100) {
                    cashback = 0.003;
                }
            }
            case "gold" -> {
                if (spentAmount >= 500) {
                    cashback = 0.007;
                } else if (spentAmount >= 300) {
                    cashback = 0.0055;
                } else if (spentAmount >= 100) {
                    cashback = 0.005;
                }
            }
        }
        return cashback + addedCashback;
    }
}
