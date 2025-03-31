package com.example.kharcha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionActivity extends AppCompatActivity {
    private static final String TAG = "TransactionActivity";
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<TransactionModel> transactionList;
    private FirebaseFirestore db;
    private TextView tvIncome, tvExpenses, tvTotal, tvSelectedMonth;
    private ImageView btnPrevMonth, btnNextMonth;
    private FloatingActionButton fabAddExpense, fabAddIncome;
    private Calendar currentMonth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_screen);


        rvTransactions = findViewById(R.id.rvTransactions);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpenses = findViewById(R.id.tvExpenses);
        tvTotal = findViewById(R.id.tvTotal);
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        fabAddExpense = findViewById(R.id.fabAddExpense);
        fabAddIncome = findViewById(R.id.fabAddIncome);
        bottomNavigationView = findViewById(R.id.bottom_navigation);


        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);


        db = FirebaseFirestore.getInstance();


        currentMonth = Calendar.getInstance();
        updateMonthDisplay();


        loadTransactions();


        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_transaction) {
                return true;
            } else if (item.getItemId() == R.id.nav_analytics) {
                startActivity(new Intent(TransactionActivity.this, AnalyticsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(TransactionActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_transaction);


        fabAddExpense.setOnClickListener(v -> startActivity(new Intent(TransactionActivity.this, ExpenseActivity.class)));


        fabAddIncome.setOnClickListener(v -> startActivity(new Intent(TransactionActivity.this, IncomeActivity.class)));


        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadTransactions();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadTransactions();
        });
    }

    private void loadTransactions() {
        Log.d(TAG, "Loading transactions from Firestore");


        db.collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TransactionModel> tempList = new ArrayList<>();
                    double totalIncome = 0;
                    double totalExpense = 0;

                    Log.d(TAG, "Retrieved " + queryDocumentSnapshots.size() + " documents");

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(TransactionActivity.this, "No transactions found", Toast.LENGTH_SHORT).show();
                        updateSummary(0, 0);
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {

                            Map<String, Object> data = document.getData();
                            Log.d(TAG, "Document data: " + data.toString());


                            String title = getString(data, "title");
                            Double amount = getDouble(data, "amount");
                            String type = getString(data, "type");
                            Date date = getDate(data, "date");

                            if (title == null || amount == null || type == null) {
                                Log.w(TAG, "Skipping document " + document.getId() + " due to missing required fields");
                                continue;
                            }


                            TransactionModel model = new TransactionModel(title, amount, type, date != null ? date : new Date());


                            tempList.add(model);


                            if ("Income".equals(type)) {
                                totalIncome += amount;
                            } else if ("Expense".equals(type)) {
                                totalExpense += amount;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing document: " + document.getId(), e);
                        }
                    }

                    Log.d(TAG, "Processed " + tempList.size() + " transactions");


                    final List<TransactionModel> finalList = tempList;
                    final double finalTotalIncome = totalIncome;
                    final double finalTotalExpense = totalExpense;


                    runOnUiThread(() -> {
                        transactionList.clear();
                        transactionList.addAll(finalList);
                        adapter.notifyDataSetChanged();
                        updateSummary(finalTotalIncome, finalTotalExpense);

                        // Show feedback to user
                        if (finalList.isEmpty()) {
                            Toast.makeText(TransactionActivity.this, "No valid transactions found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TransactionActivity.this, "Loaded " + finalList.size() + " transactions", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting transactions", e);
                    Toast.makeText(TransactionActivity.this,
                            "Error loading transactions: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }


    private String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : null;
    }

    private Double getDouble(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Date getDate(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value instanceof Date ? (Date) value : null;
    }

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvSelectedMonth.setText(sdf.format(currentMonth.getTime()));
    }

    private void updateSummary(double income, double expense) {
        double total = income - expense;
        tvIncome.setText("Income\n₹" + String.format("%.2f", income));
        tvExpenses.setText("Expenses\n₹" + String.format("%.2f", expense));
        tvTotal.setText("Total\n₹" + String.format("%.2f", total));
    }
}