package com.example.bptracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private FragmentRecord fragmentRecord;
    private FragmentDashboard fragmentDashboard;
    private FragmentSettings fragmentSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.createNotificationChannel(this);
        NotificationHelper.scheduleNotification(this);

        if (savedInstanceState == null) {
            fragmentRecord = new FragmentRecord();
            fragmentDashboard = new FragmentDashboard();
            fragmentSettings = new FragmentSettings();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragmentSettings, "settings").hide(fragmentSettings)
                    .add(R.id.fragment_container, fragmentDashboard, "dashboard").hide(fragmentDashboard)
                    .add(R.id.fragment_container, fragmentRecord, "records")
                    .commit();
        } else {
            fragmentRecord = (FragmentRecord) getSupportFragmentManager().findFragmentByTag("records");
            fragmentDashboard = (FragmentDashboard) getSupportFragmentManager().findFragmentByTag("dashboard");
            fragmentSettings = (FragmentSettings) getSupportFragmentManager().findFragmentByTag("settings");
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_records:
                    showFragment(fragmentRecord);
                    return true;
                case R.id.nav_dashboard:
                    showFragment(fragmentDashboard);
                    fragmentDashboard.refreshDashboard();
                    return true;
                case R.id.nav_settings:
                    showFragment(fragmentSettings);
                    return true;
            }
            return false;
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .hide(fragmentRecord)
                .hide(fragmentDashboard)
                .hide(fragmentSettings)
                .show(fragment)
                .commit();
    }

    public void refreshRecords() {
        if (fragmentRecord != null) fragmentRecord.loadRecords();
    }
}