package com.example.skillbridge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FinancesFragment extends Fragment {

    public FinancesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Purana finances XML
        View view = inflater.inflate(R.layout.activity_finances, container, false);

        ImageView btnBack = view.findViewById(R.id.btnBack);
        Button btnRequestPayout = view.findViewById(R.id.btnRequestPayout);

        btnBack.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Back clicked", Toast.LENGTH_SHORT).show();
        });

        btnRequestPayout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Payout request initiated via UPI.", Toast.LENGTH_LONG).show();
        });

        return view;
    }
}