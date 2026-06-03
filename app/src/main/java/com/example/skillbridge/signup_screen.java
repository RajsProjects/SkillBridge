package com.example.skillbridge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.skillbridge.databinding.ActivitySingupScreenBinding;
import com.example.skillbridge.databinding.ActivitySingupScreenBinding;

public class singup_screen extends AppCompatActivity {

    private ActivitySingupScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Transparent Bars Setup (Light background = true for dark icons)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);
        windowInsetsController.setAppearanceLightNavigationBars(true);

        // 2. View Binding Setup
        binding = ActivitySingupScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 3. Click Listeners

        // "Already have an account? Log In" text click
        binding.tvAlreadyAccount.setOnClickListener(v -> {
            goToLogin();
        });

        // Top toggle "Log In" click
        binding.tvToggleLogin.setOnClickListener(v -> {
            goToLogin();
        });

        // Main Sign Up Button click
        binding.btnSignup.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(singup_screen.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(singup_screen.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(singup_screen.this, Login_screen.class);
        startActivity(intent);
        finish();
    }
}