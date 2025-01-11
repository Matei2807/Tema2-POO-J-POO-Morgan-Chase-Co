package org.poo.main;

import org.poo.fileio.CommandInput;
import org.poo.main.users.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SplitPayment {
    private List<String> accounts;
    private List<Double> amountsForAccounts;
    private String currency;
    private double amount;
    private String splitPaymentType; // equal/custom
    private Set<String> acceptedAccounts;
    private Set<String> rejectedAccounts;
    private int timestamp;

    public SplitPayment(final CommandInput commandInput) {
        this.accounts = commandInput.getAccounts();
        this.amountsForAccounts = commandInput.getAmountForUsers();
        this.currency = commandInput.getCurrency();
        this.amount = commandInput.getAmount();
        this.splitPaymentType = commandInput.getSplitPaymentType();
        this.timestamp = commandInput.getTimestamp();
        this.acceptedAccounts = new HashSet<>();
        this.rejectedAccounts = new HashSet<>();
    }

    /**
     * Checks if the user has an account in the split payment.
     * @param user
     * @return
     */
    public boolean hasUser(final User user) {
        for (String account : accounts) {
            if (user.hasAccount(account)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the user has accepted the split payment.
     * @param user
     * @return
     */
    public boolean hasAccepted(final User user) {
        for (String acceptedEmail : acceptedAccounts) {
            if (user.getEmail().equals(acceptedEmail)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the user has rejected the split payment.
     * @param user
     * @return
     */
    public boolean hasRejected(final User user) {
        for (String rejectedEmail : rejectedAccounts) {
            if (user.getEmail().equals(rejectedEmail)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Accepts an account from the split payment.
     * @param email
     */
    public void acceptAccount(final String email) {
        acceptedAccounts.add(email);
    }

    /**
     * Rejects an account from the split payment.
     * @param email
     */
    public void rejectAccount(final String email) {
        rejectedAccounts.add(email);
    }

    public boolean isAccepted() {
        return acceptedAccounts.size() == accounts.size();
    }

    public boolean isRejected() {
        return rejectedAccounts.size() + acceptedAccounts.size() == accounts.size()
                && !rejectedAccounts.isEmpty();
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
