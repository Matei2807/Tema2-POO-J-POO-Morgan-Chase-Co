package org.poo.main.Users;

import org.poo.fileio.UserInput;
import org.poo.main.Accounts.Account;
import org.poo.main.Transactions.Transaction;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private List<Account> accounts;
    private List<Transaction> transactions;
    private boolean isNull = false;

    public User(final UserInput userInput) {
        firstName = userInput.getFirstName();
        lastName = userInput.getLastName();
        email = userInput.getEmail();
        accounts = new ArrayList<>();
        transactions = new ArrayList<>();
    }

    /**
     * Add a transaction to the user's list of transactions.
     * @param transaction the transaction to be added
     */
    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }

    /**
     * Add an account to the user's list of accounts.
     * @param account the account to be added
     */
    public void addAccount(final Account account) {
        accounts.add(account);
    }

    /**
     * Delete an account from the user's list of accounts.
     * @param accountNumber the account number of the account to be deleted
     */
    public void deleteAccount(final String accountNumber) {
        Account account = getAccount(accountNumber);
        if (account != null) {
            accounts.remove(account);
        }
    }

    /**
     * Get an account by its account number.
     * @param accountNumber the account number of the account to be retrieved
     * @return the account with the given account number, or null if it does not exist
     */
    public Account getAccount(final String accountNumber) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Get the user's first name.
     * @return the user's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Get the user's last name.
     * @return the user's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Get the user's email.
     * @return the user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get the user's list of accounts.
     * @return the user's list of accounts
     */
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Get the user's list of transactions.
     * @return the user's list of transactions
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Set the user as a null user.
     */
    public void setNull() {
        isNull = true;
    }

    /**
     * Check if the user is a null user.
     * @return true if the user is a null user, false otherwise
     */
    public boolean isNull() {
        return isNull;
    }
}

