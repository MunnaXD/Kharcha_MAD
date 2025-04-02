package com.example.kharcha;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class IncomeAnalyticsActivity extends AppCompatActivity {
    private static final String TAG = "IncomeAnalyticsActivity";
    private FirebaseFirestore db;
    private Calendar currentMonth;
    private TextView tvSelectedMonth, tvIncomeAmount, tvExpenseAmount, tvBalanceAmount;
    private ImageView btnPrevMonth, btnNextMonth;
    private LinearLayout weeklyTrendContainer;
    private RecyclerView rvIncomeCategories;
    private IncomeCategoryAdapter incomeCategoryAdapter;
    private List<IncomeCategoryModel> incomeCategories;
    private BottomNavigationView bottomNavigationView;
    private LinearLayout expenseview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_screen_income);

        // Initialize UI components
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);
        tvIncomeAmount = findViewById(R.id.tvIncomeAmount);
        tvExpenseAmount = findViewById(R.id.tvExpenseAmount);
        tvBalanceAmount = findViewById(R.id.tvBalanceAmount);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        weeklyTrendContainer = findViewById(R.id.weeklyTrendContainer);
        rvIncomeCategories = findViewById(R.id.rvIncomeCategories);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        expenseview = findViewById(R.id.expensell);

        expenseview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IncomeAnalyticsActivity.this, AnalyticsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up the current month
        currentMonth = Calendar.getInstance();
        updateMonthDisplay();

        // Set up RecyclerView
        incomeCategories = new ArrayList<>();
        incomeCategoryAdapter = new IncomeCategoryAdapter(this, incomeCategories);
        rvIncomeCategories.setLayoutManager(new LinearLayoutManager(this));
        rvIncomeCategories.setAdapter(incomeCategoryAdapter);

        // Load initial data
        loadAnalyticsData();

        // Set up navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_transaction) {
                startActivity(new Intent(IncomeAnalyticsActivity.this, TransactionActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_analytics) {
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(IncomeAnalyticsActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_analytics);

        // Set up month navigation buttons
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadAnalyticsData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadAnalyticsData();
        });
    }

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvSelectedMonth.setText(sdf.format(currentMonth.getTime()));
    }

    private void loadAnalyticsData() {
        Log.d(TAG, "Loading income analytics data from Firestore");

        int selectedYear = currentMonth.get(Calendar.YEAR);
        int selectedMonth = currentMonth.get(Calendar.MONTH);

        db.collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalIncome = 0;
                    double totalExpense = 0;
                    Map<Integer, Double> weeklyIncome = new HashMap<>();
                    Map<String, Double> categoryIncome = new HashMap<>();

                    if (queryDocumentSnapshots.isEmpty()) {
                        updateSummary(0, 0);
                        updateWeeklyChart(new HashMap<>());
                        updateCategoryBreakdown(new HashMap<>());
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Map<String, Object> data = document.getData();
                            String title = getString(data, "title");
                            Double amount = getDouble(data, "amount");
                            String type = getString(data, "type");
                            Date date = getDate(data, "date");

                            if (title == null || amount == null || type == null || date == null) {
                                continue;
                            }

                            Calendar transactionCal = Calendar.getInstance();
                            transactionCal.setTime(date);
                            int transactionYear = transactionCal.get(Calendar.YEAR);
                            int transactionMonth = transactionCal.get(Calendar.MONTH);
                            int transactionWeek = transactionCal.get(Calendar.WEEK_OF_MONTH);

                            if (transactionYear == selectedYear && transactionMonth == selectedMonth) {
                                if ("Income".equals(type)) {
                                    totalIncome += amount;

                                    // Add to weekly income
                                    weeklyIncome.put(transactionWeek,
                                            weeklyIncome.getOrDefault(transactionWeek, 0.0) + amount);

                                    // Add to category income
                                    String incomeCategory = title != null ? title : "Other";
                                    categoryIncome.put(incomeCategory,
                                            categoryIncome.getOrDefault(incomeCategory, 0.0) + amount);
                                } else if ("Expense".equals(type)) {
                                    totalExpense += amount;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing document", e);
                        }
                    }

                    final double finalTotalIncome = totalIncome;
                    final double finalTotalExpense = totalExpense;

                    runOnUiThread(() -> {
                        updateSummary(finalTotalIncome, finalTotalExpense);
                        updateWeeklyChart(weeklyIncome);
                        updateCategoryBreakdown(categoryIncome);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting income analytics data", e);
                    Toast.makeText(this, "Error loading analytics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateSummary(double income, double expense) {
        double balance = income - expense;
        tvIncomeAmount.setText("₹" + String.format("%.2f", income));
        if (tvExpenseAmount != null) {
            tvExpenseAmount.setText("₹" + String.format("%.2f", expense));
        }
        if (tvBalanceAmount != null) {
            tvBalanceAmount.setText("₹" + String.format("%.2f", balance));
        }
    }

    private void updateWeeklyChart(Map<Integer, Double> weeklyIncome) {
        weeklyTrendContainer.removeAllViews();

        if (weeklyIncome.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No data available for this month");
            emptyText.setTextColor(Color.WHITE);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            weeklyTrendContainer.addView(emptyText);
            return;
        }

        double maxIncome = Collections.max(weeklyIncome.values());

        for (int week = 1; week <= 5; week++) {
            double weekAmount = weeklyIncome.getOrDefault(week, 0.0);

            LinearLayout weekColumn = new LinearLayout(this);
            weekColumn.setOrientation(LinearLayout.VERTICAL);
            weekColumn.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            columnParams.setMargins(4, 0, 4, 0);
            weekColumn.setLayoutParams(columnParams);

            // Bar height as percentage of max
            int barHeight = weekAmount > 0
                    ? (int)(100 * weekAmount / maxIncome)
                    : 5; // Minimum height for visibility

            // Create the bar
            View bar = new View(this);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0, 100 - barHeight);
            bar.setLayoutParams(barParams);

            // Create the colored bar
            View coloredBar = new View(this);
            coloredBar.setBackgroundColor(Color.parseColor("#4CAF50")); // Green for income
            LinearLayout.LayoutParams coloredBarParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0, barHeight);
            coloredBar.setLayoutParams(coloredBarParams);

            // Add week label
            TextView weekLabel = new TextView(this);
            weekLabel.setText("W" + week);
            weekLabel.setTextColor(Color.WHITE);
            weekLabel.setGravity(Gravity.CENTER);
            weekLabel.setTextSize(10);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            weekLabel.setLayoutParams(labelParams);

            // Add all views to the column
            weekColumn.addView(bar);
            weekColumn.addView(coloredBar);
            weekColumn.addView(weekLabel);

            // Add column to container
            weeklyTrendContainer.addView(weekColumn);
        }
    }

    private void updateCategoryBreakdown(Map<String, Double> categoryIncome) {
        incomeCategories.clear();

        for (Map.Entry<String, Double> entry : categoryIncome.entrySet()) {
            incomeCategories.add(new IncomeCategoryModel(entry.getKey(), entry.getValue()));
        }

        // Sort categories by income amount (descending)
        Collections.sort(incomeCategories, (c1, c2) ->
                Double.compare(c2.getAmount(), c1.getAmount()));

        incomeCategoryAdapter.notifyDataSetChanged();
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
        }
        return null;
    }

    private Date getDate(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) value).toDate();
        } else if (value instanceof Date) {
            return (Date) value;
        } else if (value instanceof Long) {
            return new Date((Long) value);
        }
        return null;
    }
}