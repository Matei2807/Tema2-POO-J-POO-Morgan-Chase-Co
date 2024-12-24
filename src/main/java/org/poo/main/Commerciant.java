package org.poo.main;

import org.poo.fileio.CommerciantInput;

public final class Commerciant {
    private final String name;
    private int id;
    private String account;
    private String type;
    private String cashbackStrategy; // nrOfTransactions / spendingThreshold(in RON)
    private double totalSales;

    public Commerciant(final String name) { // TODO: Will probably need to remove this constructor
        this.name = name;
        this.totalSales = 0;
    }

    public Commerciant(CommerciantInput commerciantInput) {
        name = commerciantInput.getCommerciant();
        id = commerciantInput.getId();
        account = commerciantInput.getAccount();
        type = commerciantInput.getType();
        cashbackStrategy = commerciantInput.getCashbackStrategy();
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

    public String getCashbackStrategy() {
        return cashbackStrategy;
    }
}
