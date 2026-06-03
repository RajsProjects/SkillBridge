package com.example.skillbridge;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class signup_screen extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private TextView tvToggleLogin;
    private Button btnSignup;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_singup_screen);


        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvToggleLogin = findViewById(R.id.tvToggleLogin);
        btnSignup = findViewById(R.id.btnSignup);


        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(signup_screen.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                Intent signup = new Intent(signup_screen.this , LandingActivity.class);
                startActivity(signup);
                finish();
            }
        });


        tvToggleLogin.setOnClickListener(v -> {
            Intent intent = new Intent(signup_screen.this, Login_screen.class);
            startActivity(intent);
            finish();
        });
    }
}