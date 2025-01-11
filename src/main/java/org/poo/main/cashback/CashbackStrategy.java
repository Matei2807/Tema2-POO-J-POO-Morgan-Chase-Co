package org.poo.main.cashback;

import org.poo.main.accounts.Account;
import org.poo.main.users.User;

public interface CashbackStrategy {
    /**
     * Method that calculates the cashback for a transaction
     * @param info
     * @param commerciantType
     * @param account
     * @param user
     * @param spentAmount
     * @return
     */
    double getCashback(TransactionInfoForCashback info, String commerciantType,
                       Account account, User user, double spentAmount);
}
