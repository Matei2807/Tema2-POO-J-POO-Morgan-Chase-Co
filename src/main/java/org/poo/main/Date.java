package org.poo.main;

public class Date {
    int day;
    int month;
    int year;

    public Date(String date) {
        // date is in format yyyy-mm-dd
        String[] dateParts = date.split("-");
        year = Integer.parseInt(dateParts[0]);
        month = Integer.parseInt(dateParts[1]);
        day = Integer.parseInt(dateParts[2]);
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
}
