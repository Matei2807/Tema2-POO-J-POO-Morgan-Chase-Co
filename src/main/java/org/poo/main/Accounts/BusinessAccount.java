package org.poo.main.Accounts;

import org.poo.fileio.CommandInput;
import org.poo.main.Transactions.BusinessAccountTransaction;
import org.poo.main.Users.User;

import java.util.ArrayList;
import java.util.List;

public final class BusinessAccount extends Account {
    private String owner; // Email of the owner
    private List<String> managers;
    private List<String> employees;
    private double spendingLimit;
    private double depositLimit;
    private List<BusinessAccountTransaction> businessTrasactions;

    public BusinessAccount(final CommandInput command, User user) {
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

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void addManager(String manager) {
        managers.add(manager);
    }

    public void addEmployee(String employee) {
        employees.add(employee);
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

    public void setSpendingLimit(double spendingLimit) {
        this.spendingLimit = spendingLimit;
    }

    public double getDepositLimit() {
        return depositLimit;
    }

    public void setDepositLimit(double depositLimit) {
        this.depositLimit = depositLimit;
    }

    public void addBusinessTransaction(BusinessAccountTransaction transaction) {
        businessTrasactions.add(transaction);
    }

    public double getUserDeposit(String email, int startTimestamp, int endTimestamp) {
        double deposit = 0;
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getSenderEmail().equals(email) && transaction.getSpentAmount() < 0
                    && transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp) {
                deposit += transaction.getSpentAmount();
            }
        }
        return -deposit;
    }

    public double getUserSpentAmount(String email, int startTimestamp, int endTimestamp) {
        double spentAmount = 0;
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getSenderEmail().equals(email) && transaction.getSpentAmount() > 0
                    && transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp) {
                spentAmount += transaction.getSpentAmount();
            }
        }
        return spentAmount;
    }

    public List<String> getAllPaidCommerciants(int startTimestamp, int endTimestamp) {
        List<String> paidCommerciants = new ArrayList<>();
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getSpentAmount() > 0 && transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp
                && !paidCommerciants.contains(transaction.getReceiverCommerciant()) && !transaction.getReceiverCommerciant().isEmpty()) {
                paidCommerciants.add(transaction.getReceiverCommerciant());
            }
        }
        return paidCommerciants;
    }

    public double getCommerciantSpentAmount(String commerciant, int startTimestamp, int endTimestamp) {
        double spentAmount = 0;
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getReceiverCommerciant().equals(commerciant) && transaction.getSpentAmount() > 0
                    && transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp) {
                spentAmount += transaction.getSpentAmount();
            }
        }
        return spentAmount;
    }

    public List<String> getManagersForCommerciant(String commerciant, int startTimestamp, int endTimestamp) {
        List<String> managersForCommerciant = new ArrayList<>();
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getReceiverCommerciant().equals(commerciant) && managers.contains(transaction.getSenderEmail())
                    && transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp) {
                managersForCommerciant.add(transaction.getSenderEmail());
            }
        }
        return managersForCommerciant;
    }

    public List<String> getEmployeesForCommerciant(String commerciant, int startTimestamp, int endTimestamp) {
        List<String> employeesForCommerciant = new ArrayList<>();
        for (BusinessAccountTransaction transaction : businessTrasactions) {
            if (transaction.getReceiverCommerciant().equals(commerciant) && employees.contains(transaction.getSenderEmail())
                    && transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp) {
                employeesForCommerciant.add(transaction.getSenderEmail());
            }
        }
        return employeesForCommerciant;
    }
}
