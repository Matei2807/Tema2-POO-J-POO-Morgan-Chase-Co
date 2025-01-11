package org.poo.fileio;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public final class CommerciantInput {
    private String commerciant;
    private int id;
    private String account;
    private String type;
    private String cashbackStrategy;

    public int getId() {
        return id;
    }

    public String getCommerciant() {
        return commerciant;
    }

    public String getAccount() {
        return account;
    }

    public String getType() {
        return type;
    }

    public String getCashbackStrategy() {
        return cashbackStrategy;
    }
}
