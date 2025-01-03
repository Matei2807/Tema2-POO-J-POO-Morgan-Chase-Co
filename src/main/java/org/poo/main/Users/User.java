package org.poo.main.Users;

import org.poo.fileio.UserInput;
import org.poo.main.Accounts.Account;
import org.poo.main.Accounts.NullAccount;
import org.poo.main.Cashback.TransactionInfoForCashback;
import org.poo.main.Date;
import org.poo.main.Transactions.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private Date birthDate;
    private String occupation;
    private List<Account> accounts;
    private List<Transaction> transactions;
    private boolean isNull = false;
    private String plan;
    private int paymentsOver300RON; // for upgrading the plan
    private Map<String, Double> cashbackMap; // comerciantType -> cashback
    private TransactionInfoForCashback transactionInfoForSpendingThreshold;

    public User(final UserInput userInput) {
        firstName = userInput.getFirstName();
        lastName = userInput.getLastName();
        email = userInput.getEmail();
        accounts = new ArrayList<>();
        transactions = new ArrayList<>();
        birthDate = new Date(userInput.getBirthDate());
        occupation = userInput.getOccupation();
        plan = occupation.equals("student") ? "student" : "standard";
        cashbackMap = new HashMap<>();
        cashbackMap.put("Food", 0.0);
        cashbackMap.put("Clothes", 0.0);
        cashbackMap.put("Tech", 0.0);
        paymentsOver300RON = 0;
        transactionInfoForSpendingThreshold = new TransactionInfoForCashback();
    }

    public User() { // empty constructor for NullUser
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
        return new NullAccount();
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

    public String getOccupation() {
        return occupation;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public int getAge() {
        return birthDate.getAge();
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getPlan() {
        return plan;
    }

    public void checkPlanUpgrade(double amount) {
        if (plan.equals("silver")) {
            paymentsOver300RON += (amount >= 300) ? 1 : 0;
            plan = paymentsOver300RON >= 5 ? "gold" : "silver";
        }
    }

    public Map<String, Double> getCashbackMap() {
        return cashbackMap;
    }

    public TransactionInfoForCashback getTransactionInfoForSpendingThreshold() {
        return transactionInfoForSpendingThreshold;
    }

    public void addTransactionInfoForSpendingThreshold(final double amount) {
        transactionInfoForSpendingThreshold.addTransaction(amount);
    }

    public boolean hasAccount(String accountNumber) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return true;
            }
        }
        return false;
    }
}

