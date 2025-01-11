package org.poo.main.accounts;

import org.poo.fileio.CommandInput;
import org.poo.main.transactions.BusinessAccountTransaction;
import org.poo.main.users.User;

import java.util.ArrayList;
import java.util.List;

public final class BusinessAccount extends Account {
    private String owner; // Email of the owner
    private List<String> managers;
    private List<String> employees;
    private double spendingLimit;
    private double depositLimit;
    private List<BusinessAccountTransaction> businessTrasactions;

    public BusinessAccount(final CommandInput command, final User user) {
        super(command, user);
        managers = new ArrayList<>();
        employees = new ArrayList<>();
        owner = user.getEmail();
        businessTrasactions = new ArrayList<>();
    }

    @Override
    public void addInterest() {
        // No interest for business accounts
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public List<String> getManagers() {
        return managers;
    }

    public List<String> getEmployees() {
        return employees;
    }

    public double getSpendingLimit() {
        return spendingLimit;
    }

    public void setSpendingLimit(final double spendingLimit) {
        this.spendingLimit = spendingLimit;
    }

    public double getDepositLimit() {
        return depositLimit;
    }

    public void setDepositLimit(final double depositLimit) {
        this.depositLimit = depositLimit;
    }

    /**
     * Add a manager to the business account
     * @param manager
     */
    public void addManager(final String manager) {
        managers.add(manager);
    }

    /**
     * Add an employee to the business account
     * @param employee
     */
    public void addEmployee(final String employee) {
        employees.add(employee);
    }

    /**
     * Add a business transaction to the account
     * @param transaction
     */
    public void addBusinessTransaction(final BusinessAccountTransaction transaction) {
        businessTrasactions.add(transaction);
    }

    /**
     * Check if a user is part of the business account
     * @param email
     * @return
     */
    public boolean hasUser(final String email) {
        return owner.equals(email) || managers.contains(email) || employees.contains(email);
    }

    /**
     * Get the deposit for a user in a given time interval
     * @param email
     * @param startTimestamp
     * @param endTimestamp
     * @return
     */
    public double getUserDeposit(final String email, final int startTimestamp,
                                 final int endTimestamp) {
        double deposit = 0;
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getSenderEmail().equals(email) && transaction.getSpentAmount() < 0
                && transaction.getTimestamp() >= startTimestamp
                && transaction.getTimestamp() <= endTimestamp) {
                deposit += transaction.getSpentAmount();
            }
        }
        return -deposit;
    }

    /**
     * Get the spent amount for a user in a given time interval
     * @param email
     * @param startTimestamp
     * @param endTimestamp
     * @return
     */
    public double getUserSpentAmount(final String email, final int startTimestamp,
                                     final int endTimestamp) {
        double spentAmount = 0;
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getSenderEmail().equals(email) && transaction.getSpentAmount() > 0
                && transaction.getTimestamp() >= startTimestamp
                && transaction.getTimestamp() <= endTimestamp) {
                spentAmount += transaction.getSpentAmount();
            }
        }
        return spentAmount;
    }

    /**
     * Get all the paid commerciants in a given time interval
     * @param startTimestamp
     * @param endTimestamp
     * @return
     */
    public List<String> getAllPaidCommerciants(final int startTimestamp, final int endTimestamp) {
        List<String> paidCommerciants = new ArrayList<>();
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getSpentAmount() > 0 && transaction.getTimestamp() >= startTimestamp
                && transaction.getTimestamp() <= endTimestamp
                && !paidCommerciants.contains(transaction.getReceiverCommerciant())
                && !transaction.getReceiverCommerciant().isEmpty()
                && !transaction.getSenderEmail().equals(owner)) {
                paidCommerciants.add(transaction.getReceiverCommerciant());
            }
        }
        //sort the list
        paidCommerciants.sort(String::compareTo);
        return paidCommerciants;
    }

    /**
     * Get the spent amount for a commerciant in a given time interval
     * @param commerciant
     * @param startTimestamp
     * @param endTimestamp
     * @return
     */
    public double getCommerciantSpentAmount(final String commerciant,
                                            final int startTimestamp,
                                            final int endTimestamp) {
        double spentAmount = 0;
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getReceiverCommerciant().equals(commerciant)
                && transaction.getSpentAmount() > 0 && transaction.getTimestamp() >= startTimestamp
                && transaction.getTimestamp() <= endTimestamp
                && !transaction.getSenderEmail().equals(owner)) {
                spentAmount += transaction.getSpentAmount();
            }
        }
        return spentAmount;
    }

    /**
     * Get the managers for a commerciant in a given time interval
     * @param commerciant
     * @param startTimestamp
     * @param endTimestamp
     * @return
     */
    public List<String> getManagersForCommerciant(final String commerciant,
                                                  final int startTimestamp,
                                                  final int endTimestamp) {
        List<String> managersForCommerciant = new ArrayList<>();
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getReceiverCommerciant().equals(commerciant)
                && managers.contains(transaction.getSenderEmail())
                && transaction.getTimestamp() >= startTimestamp
                && transaction.getTimestamp() <= endTimestamp) {
                managersForCommerciant.add(transaction.getSenderEmail());
            }
        }
        return managersForCommerciant;
    }

    /**
     * Get the employees for a commerciant in a given time interval
     * @param commerciant
     * @param startTimestamp
     * @param endTimestamp
     * @return
     */
    public List<String> getEmployeesForCommerciant(final String commerciant,
                                                   final int startTimestamp,
                                                   final int endTimestamp) {
        List<String> employeesForCommerciant = new ArrayList<>();
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getReceiverCommerciant().equals(commerciant)
                && employees.contains(transaction.getSenderEmail())
                && transaction.getTimestamp() >= startTimestamp
                && transaction.getTimestamp() <= endTimestamp) {
                employeesForCommerciant.add(transaction.getSenderEmail());
            }
        }
        return employeesForCommerciant;
    }
}
