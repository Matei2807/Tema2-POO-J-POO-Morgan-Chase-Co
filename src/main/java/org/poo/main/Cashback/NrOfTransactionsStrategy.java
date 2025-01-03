package org.poo.main.Cashback;

import org.poo.main.Accounts.Account;
import org.poo.main.Commerciant;
import org.poo.main.Users.User;

import java.util.*;

public class NrOfTransactionsStrategy implements CashbackStrategy {
    @Override
    public double getCashback(TransactionInfoForCashback info, String commerciantType, User user, double spentAmount) {
        int nrOfTransactions = info.getNrOfTransactions();
        Map<String, Double> cashbackMap = user.getCashbackMap();

        if (cashbackMap.containsKey(commerciantType)) {
            double cashback = cashbackMap.get(commerciantType);
            if (cashback == -1.0) { // if the cashback was already used, we return 0.0
                return 0.0;
            } else if (cashback != 0.0) { // if the cashback was not used, but it set to a value, we return that value, and set as used
                cashbackMap.put(commerciantType, -1.0);
                return cashback;
            } // else the cashback is 0.0, so we continue
        }

        if (nrOfTransactions >= 10) {
            if (commerciantType.equals("Food")) {
                cashbackMap.put("Food", 0.02);
                return 0.0;
            } else if (commerciantType.equals("Clothes")) {
                cashbackMap.put("Clothes", 0.05);
                return 0.0;
            } else if (commerciantType.equals("Tech")) {
                cashbackMap.put("Tech", 0.1);
                return 0.0;
            } else {
                return 0.0;
            }
        } else if (nrOfTransactions >= 5) {
            if (commerciantType.equals("Food")) {
                cashbackMap.put("Food", 0.02);
                return 0.0;
            } else if (commerciantType.equals("Clothes")) {
                cashbackMap.put("Clothes", 0.05);
                return 0.0;
            } else {
                return 0.0;
            }
        } else if (nrOfTransactions >= 2) {
            if (commerciantType.equals("Food")) {
                cashbackMap.put("Food", 0.02);
                return 0.0;
            } else {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }
}
