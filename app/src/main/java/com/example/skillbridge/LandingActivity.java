package com.example.skillbridge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {

    private Button studentBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        studentBtn = findViewById(R.id.btnJoinStudent);

        studentBtn .setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this , activity_student_host.class);
            startActivity(intent);
            finish();
        });
    }
}