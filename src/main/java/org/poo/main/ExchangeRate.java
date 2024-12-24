package org.poo.main;

import org.poo.fileio.ExchangeInput;

public final class ExchangeRate {
    private final String from;
    private final String to;
    private final double rate;

    public ExchangeRate(final ExchangeInput exchangeInput) {
        from = exchangeInput.getFrom();
        to = exchangeInput.getTo();
        rate = exchangeInput.getRate();
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getRate() {
        return rate;
    }
}
