package com.example.hishab;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.hishab.database.LocalBackupDB;

public class SettingsActivity extends AppCompatActivity {

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


    //This will make sure the UI will refresh
    private void refreshUI() {
        this.refreshMainUI = true;
    }


    // Inner fragment class
    public static class SettingsFragment extends PreferenceFragmentCompat {

        //Permission request to backup data
        private final ActivityResultLauncher<String> backupReq = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), result -> backupData(result));

        //Permission request to restore data
        private final ActivityResultLauncher<String> restoreReq = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), result -> restoreData(result));

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
                backupReq.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return true;
            });

            // This is for when restore preference is clicked
            Preference restoreDatabase = findPreference("restore");
            restoreDatabase.setOnPreferenceClickListener(preference -> {
                restoreReq.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return true;
            });

        }


        // This is for backing up data
        private void backupData(boolean permission) {
            if (permission) {
                // Show warning alert dialog
                new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
                        .setTitle("Are you sure?")
                        .setMessage("This will override any existing backup file")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Ok", (dialog, which) -> { // When user confirms
                            LocalBackupDB localBackupDB = new LocalBackupDB(getActivity());
                            localBackupDB.backupData();
                        })
                        .show();
            } else {
                Toast.makeText(getActivity(), "Storage permission required", Toast.LENGTH_SHORT).show();
            }
        }


        // This is for restoring up data
        private void restoreData(boolean permission) {
            if (permission) {
                // Show warning alert dialog
                new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
                        .setTitle("Are you sure?")
                        .setMessage("This will merge backup data with existing data")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Ok", (dialog, which) -> { // When user confirms
                            LocalBackupDB localBackupDB = new LocalBackupDB(getActivity());
                            if (localBackupDB.restoreData())
                                ((SettingsActivity) getActivity()).refreshUI();
                        })
                        .show();
            } else {
                Toast.makeText(getActivity(), "Storage permission required", Toast.LENGTH_SHORT).show();
            }
        }

    }
}