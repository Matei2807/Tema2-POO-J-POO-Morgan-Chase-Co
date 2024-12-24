package org.poo.main.Accounts;

import org.poo.fileio.CommandInput;

import java.util.Arrays;
import java.util.OptionalInt;

public final class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(final CommandInput command) {
        super(command);
        this.interestRate = command.getInterestRate();
    }

    @Override
    public void addInterest() {
        balance += balance * interestRate;
    }

    public void setInterestRate(final double interestRate) {
        this.interestRate = interestRate;
    }
}
