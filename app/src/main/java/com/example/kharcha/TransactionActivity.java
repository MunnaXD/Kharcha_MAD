package com.example.kharcha;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

        // Initialize UI components
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
        createNotificationChannel();
        checkNotificationPermission();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
            scheduleDailyNotification();
        }

        // Set up RecyclerView
        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up the current month
        currentMonth = Calendar.getInstance();
        updateMonthDisplay();

        // Load initial transactions
        loadTransactions();

        // Set up navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
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

        // Set up FAB click listeners
        fabAddExpense.setOnClickListener(v -> startActivity(new Intent(TransactionActivity.this, ExpenseActivity.class)));

        fabAddIncome.setOnClickListener(v -> startActivity(new Intent(TransactionActivity.this, IncomeActivity.class)));

        // Set up month navigation buttons
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
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "dailyReminder",
                    "Daily Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Sends a daily reminder to record transactions.");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        100);
            }
        }
    }
    private void scheduleDailyNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 20);  // Set the reminder time (e.g., 8 PM)
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
        Log.d("NotificationTest", "Notification scheduled at: " + calendar.getTime());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {  // The same request code used in checkNotificationPermission()
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, schedule notifications
                scheduleDailyNotification();
            } else {
                // Permission denied, inform user
                Toast.makeText(this, "Notification permission is required for daily reminders",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadTransactions() {
        Log.d(TAG, "Loading transactions from Firestore");

        // Get the selected year and month
        int selectedYear = currentMonth.get(Calendar.YEAR);
        int selectedMonth = currentMonth.get(Calendar.MONTH);

        Log.d(TAG, "Filtering for year: " + selectedYear + ", month: " + selectedMonth);

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
                        transactionList.clear();
                        adapter.notifyDataSetChanged();
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

                            if (title == null || amount == null || type == null || date == null) {
                                Log.w(TAG, "Skipping document " + document.getId() + " due to missing required fields");
                                continue;
                            }

                            // Extract transaction's year and month
                            Calendar transactionCalendar = Calendar.getInstance();
                            transactionCalendar.setTime(date);
                            int transactionYear = transactionCalendar.get(Calendar.YEAR);
                            int transactionMonth = transactionCalendar.get(Calendar.MONTH);

                            // Debug log for dates
                            Log.d(TAG, "Transaction date: " + date + ", Year: " + transactionYear + ", Month: " + transactionMonth);

                            // Filter transactions by selected month and year
                            if (transactionYear == selectedYear && transactionMonth == selectedMonth) {
                                TransactionModel model = new TransactionModel(title, amount, type, date);
                                tempList.add(model);

                                if ("Income".equals(type)) {
                                    totalIncome += amount;
                                } else if ("Expense".equals(type)) {
                                    totalExpense += amount;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing document: " + document.getId(), e);
                        }
                    }

                    Log.d(TAG, "Filtered " + tempList.size() + " transactions for " +
                            new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.getTime()));

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
                            Toast.makeText(TransactionActivity.this,
                                    "No transactions found for " +
                                            new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.getTime()),
                                    Toast.LENGTH_SHORT).show();
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
            // Handle timestamp stored as milliseconds since epoch
            return new Date((Long) value);
        }
        return null;
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