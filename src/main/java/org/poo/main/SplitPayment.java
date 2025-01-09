package org.poo.main;

import org.poo.fileio.CommandInput;
import org.poo.main.Users.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SplitPayment {
    List<String> accounts;
    List<Double> amountsForAccounts;
    String currency;
    double amount;
    String splitPaymentType; // equal/custom
    Set<String> acceptedAccounts;
    int timestamp;

    public SplitPayment(CommandInput commandInput) {
        this.accounts = commandInput.getAccounts();
        this.amountsForAccounts = commandInput.getAmountForUsers();
        this.currency = commandInput.getCurrency();
        this.amount = commandInput.getAmount();
        this.splitPaymentType = commandInput.getSplitPaymentType();
        this.timestamp = commandInput.getTimestamp();
        this.acceptedAccounts = new HashSet<>();
    }

    public boolean hasUser(User user) {
        for (String account : accounts) {
            if (user.hasAccount(account)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAccepted(User user) {
        for (String acceptedEmail : acceptedAccounts) {
            if (user.getEmail().equals(acceptedEmail)) {
                return true;
            }
        }
        return false;
    }

    public void acceptAccount(String email) {
        acceptedAccounts.add(email);
    }

    public boolean isAccepted() {
        return acceptedAccounts.size() == accounts.size();
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public List<Double> getAmountsForAccounts() {
        return amountsForAccounts;
    }

    public String getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public String getSplitPaymentType() {
        return splitPaymentType;
    }

    public int getTimestamp() {
        return timestamp;
    }
}
