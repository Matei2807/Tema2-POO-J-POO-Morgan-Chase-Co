package org.poo.main;

public final class Commerciant {
    private final String name;
    private double totalSales;

    public Commerciant(final String name) {
        this.name = name;
        this.totalSales = 0;
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
}
