package org.poo.main.cashback;

import org.poo.main.users.User;

public interface CashbackStrategy {
    double getCashback(TransactionInfoForCashback info, String commerciantType, User user, double spentAmount);
}