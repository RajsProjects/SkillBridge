package com.example.skillbridge;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileSettingsActivity extends AppCompatActivity {

    private ActivityResultLauncher<Void> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ImageView ivProfileSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        ivProfileSettings = findViewById(R.id.ivProfileSettings);
        EditText etFullName = findViewById(R.id.etFullName);
        EditText etCity = findViewById(R.id.etCity);
        EditText etBio = findViewById(R.id.etBio);
        EditText etCollege = findViewById(R.id.etCollege);
        EditText etHourlyRate = findViewById(R.id.etHourlyRate);
        EditText etSkills = findViewById(R.id.etSkills);
        EditText etPortfolio = findViewById(R.id.etPortfolio);
        EditText etLinkedIn = findViewById(R.id.etLinkedIn);
        Switch switchPublic = findViewById(R.id.switchPublic);

        Button btnSave = findViewById(R.id.btnSave);
        Button btnAddSkill = findViewById(R.id.btnAddSkill);
        Button btnPreview = findViewById(R.id.btnPreview);
        Button btnLogout = findViewById(R.id.btnLogout);

        SharedPreferences prefs = getSharedPreferences("SkillBridgePrefs", MODE_PRIVATE);
        etFullName.setText(prefs.getString("userName", ""));
        etCity.setText(prefs.getString("city", ""));
        etBio.setText(prefs.getString("bio", ""));
        etCollege.setText(prefs.getString("college", ""));
        etHourlyRate.setText(prefs.getString("rate", ""));
        etSkills.setText(prefs.getString("skills", ""));
        etPortfolio.setText(prefs.getString("portfolio", ""));
        etLinkedIn.setText(prefs.getString("linkedin", ""));
        switchPublic.setChecked(prefs.getBoolean("isPublic", true));

        // 2. SAVE BUTTON
        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("userName", etFullName.getText().toString());
            editor.putString("city", etCity.getText().toString());
            editor.putString("bio", etBio.getText().toString());
            editor.putString("college", etCollege.getText().toString());
            editor.putString("rate", etHourlyRate.getText().toString());
            editor.putString("skills", etSkills.getText().toString());
            editor.putString("portfolio", etPortfolio.getText().toString());
            editor.putString("linkedin", etLinkedIn.getText().toString());
            editor.putBoolean("isPublic", switchPublic.isChecked());
            editor.apply();

            Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });

        // 3. ADD SKILL BUTTON
        btnAddSkill.setOnClickListener(v -> {
            String newSkill = etSkills.getText().toString();
            if(!newSkill.isEmpty()) {
                Toast.makeText(this, "Skill '" + newSkill + "' added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please type a skill first", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. PREVIEW BUTTON
        btnPreview.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Public Profile Preview...", Toast.LENGTH_SHORT).show();
        });

        // 5. LOGOUT BUTTON
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // 6. Photo Change Logic
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> { if (bitmap != null) ivProfileSettings.setImageBitmap(bitmap); }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) ivProfileSettings.setImageURI(uri); }
        );

        ivProfileSettings.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Change Photo")
                    .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                        if (which == 0) cameraLauncher.launch(null);
                        else galleryLauncher.launch("image/*");
                    }).show();
        });
    }
}