package com.example.hishab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.example.hishab.ui.overview.OverviewFragment;
import com.example.hishab.ui.statistics.StatisticsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find views
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        NavigationView sideNavView = findViewById(R.id.nav_view);
        BottomNavigationView bottomNavBar = findViewById(R.id.bottomNav);
        FloatingActionButton fabAddRecord = findViewById(R.id.bottomNav_addRecord);
        drawer = findViewById(R.id.drawer);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        sideNavView.setNavigationItemSelectedListener(this);
        bottomNavBar.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
        bottomNavBar.setOnNavigationItemReselectedListener(item -> { //DO nothing
        });

        fabAddRecord.setOnClickListener(v -> startActivity(new Intent(this,
                DataInputActivity.class)));

        if (savedInstanceState == null) {
            //This sets the default fragment and bottom nav button on startup
            bottomNavBar.setSelectedItemId(R.id.bottomNav_overview);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OverviewFragment()).commit();
        }

        useDarkMode();

    }


    //Close side drawer on back press
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    //This is the side and bottom navigation selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.bottomNav_overview) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OverviewFragment()).commit();

        } else if (item.getItemId() == R.id.bottomNav_statistics) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new StatisticsFragment()).commit();

        } else if (item.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);

        } else if (item.getItemId() == R.id.nav_feedback) {
            sendFeedback();
            drawer.closeDrawer(GravityCompat.START);

        }

        return true;
    }


    //This method uses dark mode based on settings
    private void useDarkMode() {
        SharedPreferences themePref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = themePref.getString("theme", "System default");
        switch (theme) {
            case "System default":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "Light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }


    //This will let users send feedback to the developers
    private void sendFeedback() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"monabbir.ahmmad@yahoo.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback: Hishab App ");
            startActivity(Intent.createChooser(intent, "Send email using"));

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email client installed on your device.",
                    Toast.LENGTH_SHORT).show();
        }
    }


}