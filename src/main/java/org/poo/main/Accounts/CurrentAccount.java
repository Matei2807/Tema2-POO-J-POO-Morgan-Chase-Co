package org.poo.main.Accounts;

import org.poo.fileio.CommandInput;
import org.poo.main.Users.User;

public final class CurrentAccount extends Account {
    public CurrentAccount(final CommandInput command, User user) {
        super(command, user);
    }

    @Override
    public void addInterest() {
        // No interest for current accounts
    }
}
