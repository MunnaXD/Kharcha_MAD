package com.example.kharcha;

public class IncomeCategoryModel {
    private String name;
    private double amount;

    public IncomeCategoryModel(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public double getPercentage(double total) {
        return total > 0 ? (amount / total) * 100 : 0;
    }
}
