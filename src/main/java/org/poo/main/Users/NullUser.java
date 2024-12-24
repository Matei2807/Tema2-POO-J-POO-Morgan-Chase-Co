package org.poo.main.Users;

import org.poo.fileio.UserInput;

public class NullUser extends User {
    public NullUser() {
        super(new UserInput());
        setNull();
    }
}
