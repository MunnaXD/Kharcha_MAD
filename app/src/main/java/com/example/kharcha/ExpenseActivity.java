package com.example.kharcha;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpenseActivity extends AppCompatActivity {
    // Declaring all variables we will need
    private EditText etDate;
    private EditText etAmount;
    private EditText etNote;
    private Spinner spinnerCategory;
    private Button btnAddExpense;
    private ImageButton btnBack;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout file for this activity
        setContentView(R.layout.activity_expense);

        // Find all views from layout
        etDate = findViewById(R.id.etDate);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnBack = findViewById(R.id.btnBack);

        // Set up the date formatting
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        calendar = Calendar.getInstance();
        String currentDate = dateFormatter.format(calendar.getTime());
        etDate.setText(currentDate);

        // Create array of categories
        String[] categories = new String[]{
                "Food/Eating out",
                "Transportation",
                "Shopping",
                "Bills",
                "Entertainment",
                "Others"
        };

        // Create and set adapter for spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Set up date field click
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get current date values
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Create and show date picker dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ExpenseActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // When date is selected, update calendar
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                // Update date field with selected date
                                String selectedDate = dateFormatter.format(calendar.getTime());
                                etDate.setText(selectedDate);
                            }
                        },
                        year,
                        month,
                        day
                );
                datePickerDialog.show();
            }
        });

        // Set up back button click
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  // Go back to previous screen
            }
        });

        // Set up add expense button click
        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get all values from fields
                String amountText = etAmount.getText().toString().trim();
                String noteText = etNote.getText().toString().trim();
                String categoryText = spinnerCategory.getSelectedItem().toString();
                Date selectedDate = calendar.getTime();

                // Check if amount is empty
                if (amountText.isEmpty()) {
                    etAmount.setError("Please enter amount");
                    Toast.makeText(ExpenseActivity.this, "Please enter amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Try to convert amount to number
                try {
                    // Convert amount text to number
                    double amount = Double.parseDouble(amountText);

                    // If note is empty, use category as title
                    String title;
                    if (noteText.isEmpty()) {
                        title = categoryText;
                    } else {
                        title = noteText;
                    }

                    // Create new transaction
                    Transaction transaction = new Transaction(
                            title,          // Title (note or category)
                            amount,         // Amount
                            "Expense",      // Type
                            selectedDate    // Date
                    );

                    // Create intent to send data back
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("transaction", transaction);
                    setResult(RESULT_OK, resultIntent);

                    // Show success message
                    Toast.makeText(ExpenseActivity.this, "Expense added successfully!", Toast.LENGTH_SHORT).show();

                    // Close this screen
                    finish();

                } catch (NumberFormatException e) {
                    // If amount is not a valid number
                    etAmount.setError("Please enter valid amount");
                    Toast.makeText(ExpenseActivity.this, "Please enter valid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}