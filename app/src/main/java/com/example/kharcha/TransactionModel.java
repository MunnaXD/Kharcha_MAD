package com.example.kharcha;

import java.util.Date;

public class TransactionModel {
    private String title;
    private double amount;
    private String type;
    private Date date;

    public TransactionModel() {
        // Empty constructor required for Firestore
    }

    public TransactionModel(String title, double amount, String type, Date date) {
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }
}
