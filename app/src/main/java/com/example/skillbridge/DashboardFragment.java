package com.example.skillbridge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment {


    private TextView tvUserName;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_student_dashboard, container, false);

        ImageView ivProfile = view.findViewById(R.id.ivProfile);


        tvUserName = view.findViewById(R.id.tvWelcomeName);

        TextView tvEditProfile = view.findViewById(R.id.tvEditProfile);


        View.OnClickListener openSettings = v -> {
            startActivity(new Intent(getActivity(), ProfileSettingsActivity.class));
        };

        ivProfile.setOnClickListener(openSettings);
        tvEditProfile.setOnClickListener(openSettings);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null && tvUserName != null) {

            SharedPreferences prefs = getActivity().getSharedPreferences("SkillBridgePrefs", Context.MODE_PRIVATE);
            String savedName = prefs.getString("userName", "Aria Sharma");

            tvUserName.setText(savedName);
        }
    }
}