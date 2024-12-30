package org.poo.main.Cashback;

import org.poo.main.Accounts.Account;
import org.poo.main.Commerciant;

public class SpendingThresholdStrategy implements CashbackStrategy {
    @Override
    public double getCashback(TransactionInfoForCashback info, String commerciantType, Account account) {
        double spentAmount = info.getSpentAmount();
        String plan = account.getPlan();
        switch (plan) {
            case "standard", "student" -> {
                if (spentAmount >= 500) {
                    return 0.0025;
                } else if (spentAmount >= 300) {
                    return 0.002;
                } else if (spentAmount >= 100) {
                    return 0.001;
                } else {
                    return 0.0;
                }
            }
            case "silver" -> {
                if (spentAmount >= 500) {
                    return 0.005;
                } else if (spentAmount >= 300) {
                    return 0.004;
                } else if (spentAmount >= 100) {
                    return 0.003;
                } else {
                    return 0.0;
                }
            }
            case "gold" -> {
                if (spentAmount >= 500) {
                    return 0.007;
                } else if (spentAmount >= 300) {
                    return 0.0055;
                } else if (spentAmount >= 100) {
                    return 0.003;
                } else {
                    return 0.0;
                }
            }
        }
        return 0.0;
    }
}
