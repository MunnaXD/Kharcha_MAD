package com.example.kharcha;

import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

public class Transaction implements Serializable {
    private String title;
    private double amount;
    private String type; // "Income" or "Expense"

    @ServerTimestamp
    private Date date;

    // Empty constructor required for Firestore
    public Transaction() {}

    public Transaction(String title, double amount, String type, Date date) {
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    // Getters
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public Date getDate() { return date; }
}
