package com.example.kharcha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TransactionActivity extends AppCompatActivity {
    private TextView tvSelectedMonth, tvIncome, tvExpenses, tvTotal;
    private ImageView btnPrevMonth, btnNextMonth;
    private RecyclerView rvTransactions;
    private FloatingActionButton fabAddExpense,fabAddIncome;
    private ArrayList<Transaction> transactionList;
    private TransactionAdapter adapter;
    private Calendar currentMonth;

    // Request codes for activities
    private static final int ADD_EXPENSE_REQUEST = 1;
    private static final int ADD_INCOME_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_screen);

        // Initialize views
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpenses = findViewById(R.id.tvExpenses);
        tvTotal = findViewById(R.id.tvTotal);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        rvTransactions = findViewById(R.id.rvTransactions);
        fabAddExpense = findViewById(R.id.fabAddExpense);
        fabAddIncome = findViewById(R.id.fabAddIncome);

        // Initialize data
        currentMonth = Calendar.getInstance();
        transactionList = new ArrayList<>();

        // Setup RecyclerView
        adapter = new TransactionAdapter(transactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);

        // Update month display
        updateMonthDisplay();

        // Setup click listeners for expense and income buttons
        fabAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransactionActivity.this, ExpenseActivity.class);
                startActivityForResult(intent, ADD_EXPENSE_REQUEST);
            }
        });

        fabAddIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransactionActivity.this, IncomeActivity.class);
                startActivityForResult(intent, ADD_INCOME_REQUEST);
            }
        });

        // Setup month navigation
        btnPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth.add(Calendar.MONTH, -1);
                updateMonthDisplay();
                filterTransactionsByMonth();
            }
        });

        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth.add(Calendar.MONTH, 1);
                updateMonthDisplay();
                filterTransactionsByMonth();
            }
        });
    }

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvSelectedMonth.setText(sdf.format(currentMonth.getTime()));
    }

    private void filterTransactionsByMonth() {
        ArrayList<Transaction> filteredList = new ArrayList<>();

        for (Transaction transaction : transactionList) {
            Calendar transactionDate = Calendar.getInstance();
            transactionDate.setTime(transaction.getDate());

            if (transactionDate.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                    transactionDate.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)) {
                filteredList.add(transaction);
            }
        }

        adapter.updateData(filteredList);
        calculateTotals(filteredList);
    }

    private void calculateTotals(ArrayList<Transaction> transactions) {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Income")) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += transaction.getAmount();
            }
        }

        double total = totalIncome - totalExpense;

        tvIncome.setText(String.format("Income\n₹%.2f", totalIncome));
        tvExpenses.setText(String.format("Expenses\n₹%.2f", totalExpense));
        tvTotal.setText(String.format("Total\n₹%.2f", total));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Transaction newTransaction = (Transaction) data.getSerializableExtra("transaction");
            if (newTransaction != null) {
                transactionList.add(newTransaction);
                filterTransactionsByMonth();
            }
        }
    }
}