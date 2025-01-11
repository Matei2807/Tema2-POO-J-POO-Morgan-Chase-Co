package org.poo.main.accounts;

import org.poo.fileio.CommandInput;
import org.poo.main.users.User;

public final class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(final CommandInput command, final User user) {
        super(command, user);
        this.interestRate = command.getInterestRate();
    }

    @Override
    public void addInterest() {
        balance += balance * interestRate;
    }

    @Override
    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(final double interestRate) {
        this.interestRate = interestRate;
    }
}
