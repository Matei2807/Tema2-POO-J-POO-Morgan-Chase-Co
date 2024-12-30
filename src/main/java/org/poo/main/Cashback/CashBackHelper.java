package org.poo.main.Cashback;

import org.poo.main.Accounts.Account;
import org.poo.main.Commerciant;

import java.util.HashMap;
import java.util.Map;

public class CashBackHelper { // TODO: delete this class
    /**
     * Get the cashback for future transactions
     * @param info the transaction info
     * @param commerciant the commerciant
     * @param account the account
     * @return the cashback as a map with the commerciant type as key and the cashback as value
     */
    public static Map<String, Double> getCashback(TransactionInfoForCashback info, Commerciant commerciant, Account account) {
        /*
        map: key - cashback
            "Food" - 0.XX - for nrOfTransactions
            "Clothes" - 0.XX - for nrOfTransactions
            "Tech" - 0.XX - for nrOfTransactions
            "All" - 0.XX - for spendingThreshold
        */
        Map<String, Double> cashbackMap = new HashMap<>();
        //String cashbackStrategy = commerciant.getCashbackStrategy();
        String cashbackStrategy = commerciant.getCashbackStrategy().getClass().getSimpleName();

        if (cashbackStrategy.equals("nrOfTransactions")) {
            int nrOfTransactions = info.getNrOfTransactions();
            if (nrOfTransactions >= 10) {
                cashbackMap.put("Food", 0.02);
                cashbackMap.put("Clothes", 0.05);
                cashbackMap.put("Tech", 0.1);
            } else if (nrOfTransactions >= 5) {
                cashbackMap.put("Food", 0.02);
                cashbackMap.put("Clothes", 0.05);
                cashbackMap.put("Tech", 0.0);
            } else if (nrOfTransactions >= 2) {
                cashbackMap.put("Food", 0.02);
                cashbackMap.put("Clothes", 0.0);
                cashbackMap.put("Tech", 0.0);
            } else {
                cashbackMap.put("Food", 0.0);
                cashbackMap.put("Clothes", 0.0);
                cashbackMap.put("Tech", 0.0);
            }
            cashbackMap.put("All", 0.0);
        } else if (cashbackStrategy.equals("spendingThreshold")) {
            double spentAmount = info.getSpentAmount();
            String plan = account.getPlan();
            switch (plan) {
                case "standard", "student" -> {
                    if (spentAmount >= 500) {
                        cashbackMap.put("All", 0.0025);
                    } else if (spentAmount >= 300) {
                        cashbackMap.put("All", 0.002);
                    } else if (spentAmount >= 100) {
                        cashbackMap.put("All", 0.001);
                    } else {
                        cashbackMap.put("All", 0.0);
                    }
                }
                case "silver" -> {
                    if (spentAmount >= 500) {
                        cashbackMap.put("All", 0.005);
                    } else if (spentAmount >= 300) {
                        cashbackMap.put("All", 0.004);
                    } else if (spentAmount >= 100) {
                        cashbackMap.put("All", 0.003);
                    } else {
                        cashbackMap.put("All", 0.0);
                    }
                }
                case "gold" -> {
                    if (spentAmount >= 500) {
                        cashbackMap.put("All", 0.007);
                    } else if (spentAmount >= 300) {
                        cashbackMap.put("All", 0.0055);
                    } else if (spentAmount >= 100) {
                        cashbackMap.put("All", 0.003);
                    } else {
                        cashbackMap.put("All", 0.0);
                    }
                }
            }
            cashbackMap.put("Food", 0.0);
            cashbackMap.put("Clothes", 0.0);
            cashbackMap.put("Tech", 0.0);
        }
        return cashbackMap;
    }

    public static Map<String, Double> getEmptyCashbackMap() {
        Map<String, Double> cashbackMap = new HashMap<>();
        cashbackMap.put("Food", 0.0);
        cashbackMap.put("Clothes", 0.0);
        cashbackMap.put("Tech", 0.0);
        cashbackMap.put("All", 0.0);
        return cashbackMap;
    }
}
