package org.poo.main.Accounts;

import org.poo.fileio.CommandInput;

public final class CurrentAccount extends Account {
    public CurrentAccount(final CommandInput command) {
        super(command);
    }

    @Override
    public void addInterest() {
        // No interest for current accounts
    }
}
