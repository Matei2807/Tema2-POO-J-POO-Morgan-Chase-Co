package org.poo.main.Accounts;

import org.poo.fileio.CommandInput;
import org.poo.main.Users.User;

import java.util.ArrayList;
import java.util.List;

public final class BusinessAccount extends Account {
    private String owner; // Email of the owner
    private List<String> managers;
    private List<String> employees;
    private double spendingLimit;
    private double depositLimit;

    public BusinessAccount(final CommandInput command, User user) {
        super(command, user);
        managers = new ArrayList<>();
        employees = new ArrayList<>();
        owner = user.getEmail();
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
}
