package com.example.kharcha;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class login_screen extends AppCompatActivity {
    EditText emailid,password;
    Button loginButton;
    TextView forgotPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        emailid=findViewById(R.id.email);
        password=findViewById(R.id.password);
        loginButton=findViewById(R.id.loginButton);
        forgotPassword=findViewById(R.id.forgotPass);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailid.getText().toString();
                String pass = password.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(login_screen.this,"Enter Email",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(pass)){
                    Toast.makeText(login_screen.this,"Enter Password",Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create the intent
                Intent intent = new Intent(login_screen.this, TransactionActivity.class);
                startActivity(intent);
                finish(); // This will close the login activity
                Toast.makeText(login_screen.this,"Login Successful!",Toast.LENGTH_SHORT).show();
            }
        });

    }
}