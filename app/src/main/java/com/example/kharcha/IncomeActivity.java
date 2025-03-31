package com.example.kharcha;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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

public class IncomeActivity extends AppCompatActivity {
    private EditText etDate, etAmount, etNote;
    private Spinner spinnerCategory;
    private Button btnAddIncome;
    private ImageButton btnBack;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        // Initialize views
        etDate = findViewById(R.id.etDate);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAddIncome = findViewById(R.id.btnAddIncome);
        btnBack = findViewById(R.id.btnBack);

        // Setup date
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        calendar = Calendar.getInstance();
        etDate.setText(dateFormatter.format(calendar.getTime()));

        // Setup category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.income_categories, // Your string array
                R.layout.spinner_item // Custom layout with black text
        );

        // Set dropdown layout
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply adapter to Spinner
        spinnerCategory.setAdapter(categoryAdapter);

        // Setup date picker
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        IncomeActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                etDate.setText(dateFormatter.format(calendar.getTime()));
                            }
                        },
                        year, month, day
                );
                datePickerDialog.show();
            }
        });

        // Setup back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Just finish the activity, don't start a new one
            }
        });

        // Setup add income button
        btnAddIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountText = etAmount.getText().toString().trim();
                String noteText = etNote.getText().toString().trim();
                String categoryText = spinnerCategory.getSelectedItem().toString();
                Date selectedDate = calendar.getTime();

                if (amountText.isEmpty()) {
                    etAmount.setError("Please enter amount");
                    Toast.makeText(IncomeActivity.this, "Please enter amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountText);
                    String title = noteText.isEmpty() ? categoryText : noteText;

                    Transaction transaction = new Transaction(title, amount, "Income", selectedDate);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("transaction", transaction);
                    setResult(RESULT_OK, resultIntent);

                    Toast.makeText(IncomeActivity.this, "Income added successfully!", Toast.LENGTH_SHORT).show();
                    finish();

                } catch (NumberFormatException e) {
                    etAmount.setError("Please enter valid amount");
                    Toast.makeText(IncomeActivity.this, "Please enter valid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}