package org.poo.main;

public final class Date {
    static final int CURRENT_YEAR = 2025;

    private int day;
    private int month;
    private int year;

    public Date(final String date) {
        // date is in format yyyy-mm-dd
        String[] dateParts = date.split("-");
        year = Integer.parseInt(dateParts[0]);
        month = Integer.parseInt(dateParts[1]);
        day = Integer.parseInt(dateParts[2]);
    }

    public int getAge() {
        return CURRENT_YEAR - year;
    }
}
