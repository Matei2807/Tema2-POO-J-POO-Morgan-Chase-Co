package org.poo.main;

import org.poo.fileio.CommerciantInput;
import org.poo.main.cashback.CashbackStrategy;
import org.poo.main.cashback.NrOfTransactionsStrategy;
import org.poo.main.cashback.SpendingThresholdStrategy;

public final class Commerciant {
    private final String name;
    private int id;
    private String account;
    private String type;
    private double totalSales;
    private CashbackStrategy cashbackStrategy;

    public Commerciant(final String name) { // simple constructor for usage in spending report
        this.name = name;
        this.totalSales = 0;
    }

    public Commerciant(final CommerciantInput commerciantInput) {
        name = commerciantInput.getCommerciant();
        id = commerciantInput.getId();
        account = commerciantInput.getAccount();
        type = commerciantInput.getType();
        cashbackStrategy = commerciantInput.getCashbackStrategy().equals("nrOfTransactions")
                ? new NrOfTransactionsStrategy() : new SpendingThresholdStrategy();
        totalSales = 0;
    }

    /**
     * Add a sale to the total sales of the commerciant.
     * @param sale the amount of the sale
     */
    public void addSale(final double sale) {
        totalSales += sale;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }

    public String getType() {
        return type;
    }

    public CashbackStrategy getCashbackStrategy() {
        return cashbackStrategy;
    }
}
