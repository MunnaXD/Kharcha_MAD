package com.example.kharcha;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class IncomeActivity extends AppCompatActivity {
    private EditText etDate, etAmount, etNote;
    private Spinner spinnerCategory;
    private Button btnAddIncome;
    private ImageButton btnBack;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etDate = findViewById(R.id.etDate);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAddIncome = findViewById(R.id.btnAddIncome);
        btnBack = findViewById(R.id.btnBack);

        // Setup date formatter
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        calendar = Calendar.getInstance();
        etDate.setText(dateFormatter.format(calendar.getTime()));

        // Setup category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.income_categories,
                R.layout.spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Date Picker Setup
        etDate.setOnClickListener(v -> {
            new DatePickerDialog(
                    IncomeActivity.this,
                    (view, year, month, day) -> {
                        calendar.set(year, month, day);
                        etDate.setText(dateFormatter.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Back Button
        btnBack.setOnClickListener(v -> finish());

        // Add Income Button with Firestore Integration
        btnAddIncome.setOnClickListener(v -> addIncome());
    }

    private void addIncome() {
        String amountText = etAmount.getText().toString().trim();
        String noteText = etNote.getText().toString().trim();
        String categoryText = spinnerCategory.getSelectedItem().toString();
        String selectedDateString = etDate.getText().toString();

        if (amountText.isEmpty()) {
            etAmount.setError("Please enter amount");
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            String title = noteText.isEmpty() ? categoryText : noteText;

            // Convert string date to Date object
            Date selectedDate;
            try {
                selectedDate = dateFormatter.parse(selectedDateString);
            } catch (ParseException e) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to Firestore
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("title", title);
            transaction.put("amount", amount);
            transaction.put("type", "Income");
            transaction.put("date", selectedDate);  // Store as Date object

            db.collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Income added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add income", Toast.LENGTH_SHORT).show()
                    );
            Intent intent=new Intent(IncomeActivity.this,TransactionActivity.class);
            startActivity(intent);

        } catch (NumberFormatException e) {
            etAmount.setError("Please enter a valid amount");
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
        }
    }
}