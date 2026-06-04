package com.example.skillbridge;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class activity_student_host extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_host);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        com.example.skillbridge.ViewPagerAdapter adapter = new com.example.skillbridge.ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.nav_gigs);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.nav_messages);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.nav_finances);
                        break;
                }
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                viewPager.setCurrentItem(0);
            } else if (id == R.id.nav_gigs) {
                viewPager.setCurrentItem(1);
            } else if (id == R.id.nav_messages) {
                viewPager.setCurrentItem(2);
            } else if (id == R.id.nav_finances) {
                viewPager.setCurrentItem(3);
            }
            return true;
        });
    }
}