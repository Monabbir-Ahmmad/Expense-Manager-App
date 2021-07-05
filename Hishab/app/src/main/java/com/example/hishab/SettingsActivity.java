package com.example.hishab;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.hishab.database.LocalBackupDB;

public class SettingsActivity extends AppCompatActivity {

    private static final int STORAGE_REQUEST_CODE = 1;
    private boolean refreshMainUI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings, new SettingsFragment()).commit();
        }
        //Find views
        Toolbar toolbar = findViewById(R.id.toolbar_settings);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            if (refreshMainUI) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            } else {
                finish();
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (refreshMainUI) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } else {
            finish();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocalBackupDB localBackupDB = new LocalBackupDB(this);
                localBackupDB.backupData();
            } else {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //This will make sure the UI will refresh
    private void refreshUI() {
        this.refreshMainUI = true;
    }


    // Inner fragment class
    public static class SettingsFragment extends PreferenceFragmentCompat {

        private final String[] storagePer = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // This is for theme preference
            ListPreference themePref = findPreference("theme");
            themePref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.equals("System default")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

                } else if (newValue.equals("Light")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                } else if (newValue.equals("Dark")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                }

                return true;
            });

            // This is for currency preference
            ListPreference currencyPref = findPreference("currency");
            currencyPref.setOnPreferenceChangeListener((preference, newValue) -> {
                ((SettingsActivity) getActivity()).refreshUI();
                return true;
            });

            // This is for when backup preference is clicked
            Preference backupDatabase = findPreference("backup");
            backupDatabase.setOnPreferenceClickListener(preference -> {
                backupData();
                return true;
            });

        }


        // This is for backing up data
        private void backupData() {
            // Show warning alert dialog
            new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
                    .setTitle("Are you sure?")
                    .setMessage("This will override any existing backup file")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Ok", (dialog, which) -> { // When user confirms
                        boolean hasPermission = ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                        if (hasPermission) { //When there is permission
                            LocalBackupDB localBackupDB = new LocalBackupDB(getActivity());
                            localBackupDB.backupData();

                        } else { //Request permission when no permission found
                            ActivityCompat.requestPermissions(getActivity(), storagePer, STORAGE_REQUEST_CODE);
                        }
                    })
                    .show();
        }

    }
}