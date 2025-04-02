package com.example.kharcha;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.auth.FirebaseUser;

public class login_screen extends AppCompatActivity {
    EditText emailid, password;
    Button loginButton;
    TextView CreateAccount;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        emailid = findViewById(R.id.email);
        password = findViewById(R.id.password);
        CreateAccount = findViewById(R.id.createAcc);
        loginButton = findViewById(R.id.loginButton);



        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailid.getText().toString();
                String pass = password.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(login_screen.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(login_screen.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(login_screen.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(login_screen.this, TransactionActivity.class));
                                finish();
                            } else {
                                Toast.makeText(login_screen.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        CreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login_screen.this, register.class);
                startActivity(intent);
            }
        });


    }

}