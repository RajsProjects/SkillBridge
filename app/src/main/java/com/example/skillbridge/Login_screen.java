package com.example.skillbridge; //
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.skillbridge.databinding.ActivitySingupScreenBinding;

public class Login_screen extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView SingUp ;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);


        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        SingUp = findViewById(R.id.tvToggleSignup);


        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login_screen.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            }

            else {
                Intent login = new Intent(Login_screen.this , LandingActivity.class);
                startActivity(login);
                finish();
            }
        });

       SingUp.setOnClickListener(v -> {
            Intent intent = new Intent(Login_screen.this, signup_screen.class);
            startActivity(intent);
            finish();
        });
    }
}