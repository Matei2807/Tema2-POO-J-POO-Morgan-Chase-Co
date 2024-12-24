package org.poo.main.Accounts;

import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;

public final class BusinessAccount extends Account {
    private Account owner;
    private List<Account> managers;
    private List<Account> employees;

    public BusinessAccount(final CommandInput command) {
        super(command);
        managers = new ArrayList<>();
        employees = new ArrayList<>();
        owner = null;
    }

    @Override
    public void addInterest() {
        // No interest for business accounts
    }

    public void setOwner(Account owner) {
        this.owner = owner;
    }

    public void addManager(Account manager) {
        managers.add(manager);
    }

    public void addEmployee(Account employee) {
        employees.add(employee);
    }

    public Account getOwner() {
        return owner;
    }

    public List<Account> getManagers() {
        return managers;
    }

    public List<Account> getEmployees() {
        return employees;
    }
}
