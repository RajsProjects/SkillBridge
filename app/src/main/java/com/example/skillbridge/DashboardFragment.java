package com.example.skillbridge;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment {

    // Camera aur Gallery ke liye variables declare kiye
    private ActivityResultLauncher<Void> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ImageView ivProfile;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_student_dashboard, container, false);

        ivProfile = view.findViewById(R.id.ivProfile);
        TextView tvEditProfile = view.findViewById(R.id.tvEditProfile);
        EditText etSearchGigs = view.findViewById(R.id.etSearchGigs);

        // 1. Camera se photo click hone ke baad ka action
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        ivProfile.setImageBitmap(bitmap);
                    }
                }
        );

        // 2. Gallery se photo select hone ke baad ka action
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        ivProfile.setImageURI(uri);
                    }
                }
        );

        // 3. Profile picture par click karne par Menu show karna
        ivProfile.setOnClickListener(v -> {
            String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};

            new AlertDialog.Builder(requireContext())
                    .setTitle("Change Profile Photo")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Asli Camera open hoga
                            cameraLauncher.launch(null);
                        } else if (which == 1) {
                            // Asli Mobile Gallery open hogi
                            galleryLauncher.launch("image/*");
                        } else {
                            // Photo remove karke default icon set karega
                            ivProfile.setImageResource(android.R.drawable.ic_menu_myplaces);
                        }
                    })
                    .show();
        });

        tvEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Opening Edit Profile...", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}