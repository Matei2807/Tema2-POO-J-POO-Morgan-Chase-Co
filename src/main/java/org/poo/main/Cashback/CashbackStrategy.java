package org.poo.main.Cashback;

import org.poo.main.Accounts.Account;
import org.poo.main.Commerciant;

public interface CashbackStrategy {
    double getCashback(TransactionInfoForCashback info, String commerciantType, Account account);
}