package org.poo.main.Cashback;

import org.poo.main.Accounts.Account;
import org.poo.main.Commerciant;
import org.poo.main.Users.User;

public interface CashbackStrategy {
    double getCashback(TransactionInfoForCashback info, String commerciantType, User user, double spentAmount);
}