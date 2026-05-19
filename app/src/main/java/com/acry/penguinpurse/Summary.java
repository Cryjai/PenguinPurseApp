package com.acry.penguinpurse;

public class Summary {
    public final String label;
    public final double income;
    public final double expense;
    public final double net;

    public Summary(String label, double income, double expense) {
        this.label = label;
        this.income = income;
        this.expense = expense;
        this.net = income - expense;
    }
}
