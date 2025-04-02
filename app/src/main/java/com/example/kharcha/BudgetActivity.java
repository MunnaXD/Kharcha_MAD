package com.example.kharcha;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class BudgetActivity extends AppCompatActivity {

    private TextInputEditText etBudgetAmount;
    private Button btnSaveBudget, btnCancelBudget;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "KharchaPrefs";
    private static final String KEY_BUDGET = "monthly_budget";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Initialize UI components
        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        btnCancelBudget = findViewById(R.id.btnCancelBudget);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load existing budget if available
        float existingBudget = sharedPreferences.getFloat(KEY_BUDGET, 0);
        if (existingBudget > 0) {
            etBudgetAmount.setText(String.valueOf(existingBudget));
        }

        // Set up button click listeners
        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBudget();
            }
        });

        btnCancelBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });
    }

    private void saveBudget() {
        // Get the budget amount from the EditText
        String budgetText = etBudgetAmount.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(budgetText)) {
            etBudgetAmount.setError("Please enter a budget amount");
            return;
        }

        try {
            // Convert to float and save to SharedPreferences
            float budgetAmount = Float.parseFloat(budgetText);

            // Ensure budget is a positive number
            if (budgetAmount <= 0) {
                etBudgetAmount.setError("Budget must be greater than 0");
                return;
            }

            // Save the budget
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat(KEY_BUDGET, budgetAmount);
            editor.apply();

            // Show success message
            Toast.makeText(this, "Budget set successfully!", Toast.LENGTH_SHORT).show();

            // Close activity and return to previous screen
            finish();

        } catch (NumberFormatException e) {
            etBudgetAmount.setError("Invalid number format");
        }
    }

    // Static method to get the budget amount from SharedPreferences
    public static float getBudget(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(KEY_BUDGET, 0);
    }
}