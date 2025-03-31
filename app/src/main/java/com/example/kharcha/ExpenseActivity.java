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

public class ExpenseActivity extends AppCompatActivity {
    private EditText etDate, etAmount, etNote;
    private Spinner spinnerCategory;
    private Button btnAddExpense;
    private ImageButton btnBack;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        etDate = findViewById(R.id.etDate);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAddExpense = findViewById(R.id.btnAddIncome);
        btnBack = findViewById(R.id.btnBack);

        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        calendar = Calendar.getInstance();
        etDate.setText(dateFormatter.format(calendar.getTime()));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.expense_categories, // Your string array
                R.layout.spinner_item // Custom layout with black text
        );

        // Set dropdown layout
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply adapter to Spinner
        spinnerCategory.setAdapter(adapter);

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(ExpenseActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etDate.setText(dateFormatter.format(calendar.getTime()));
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpenseActivity.this, TransactionActivity.class);
                startActivity(intent);
                finish();  // Ensures the back stack doesn't keep the ExpenseActivity
            }
        });


        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountText = etAmount.getText().toString().trim();
                String noteText = etNote.getText().toString().trim();
                String categoryText = spinnerCategory.getSelectedItem().toString();
                Date selectedDate = calendar.getTime();

                if (amountText.isEmpty()) {
                    etAmount.setError("Please enter amount");
                    Toast.makeText(ExpenseActivity.this, "Please enter amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountText);
                    String title = noteText.isEmpty() ? categoryText : noteText;

                    Transaction transaction = new Transaction(title, amount, "Expense", selectedDate);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("transaction", transaction);
                    setResult(RESULT_OK, resultIntent);

                    Toast.makeText(ExpenseActivity.this, "Expense added successfully!", Toast.LENGTH_SHORT).show();
                    finish();

                } catch (NumberFormatException e) {
                    etAmount.setError("Please enter valid amount");
                    Toast.makeText(ExpenseActivity.this, "Please enter valid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
