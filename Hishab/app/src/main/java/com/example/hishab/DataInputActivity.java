package com.example.hishab;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hishab.data.DataItem;
import com.example.hishab.database.DatabaseHelper;
import com.example.hishab.ui.expense.CategoryRecyclerAdapter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class DataInputActivity extends AppCompatActivity implements View.OnClickListener {

    private final ArrayList<DataItem> dataSet = new ArrayList<>();
    private TextInputEditText etAmount, etNote;
    private AutoCompleteTextView etDate, etTime;
    private RecyclerView recyclerView;
    private CategoryRecyclerAdapter recyclerAdapter;
    private DateTimeUtil dateTimeUtil;
    private boolean isUpdate;
    private String category = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input);

        //Check if the call is for adding new date or update existing data
        isUpdate = getIntent().getBooleanExtra("update", false);

        //Find views
        Toolbar toolbar = findViewById(R.id.toolbar_dataInput);
        etAmount = findViewById(R.id.editText_amount);
        etNote = findViewById(R.id.editText_note);
        etDate = findViewById(R.id.editText_date);
        etTime = findViewById(R.id.editText_time);
        recyclerView = findViewById(R.id.horizontal_recyclerview);

        //Setup toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());

        dateTimeUtil = new DateTimeUtil();

        createRecyclerView();

        etDate.setOnClickListener(this);
        etTime.setOnClickListener(this);

        //Set toolbar title and check if it's for record update or not
        if (!isUpdate) { //Add new record
            setTitle("Add record");
            setViewsNew();

        } else { //Edit existing record
            setTitle("Edit record");
            setViewsUpdate();
        }
    }

    // Toolbar menu inflate
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        menu.findItem(R.id.menu_filter).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    // Toolbar menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            saveData();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance();

        if (v.getId() == R.id.editText_date) { // Show date picker
            MaterialDatePicker datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.show(getSupportFragmentManager(), "Date picker");

            datePicker.addOnPositiveButtonClickListener(
                    (MaterialPickerOnPositiveButtonClickListener<Long>) selection -> {
                        etDate.setText(dateTimeUtil.getDate(selection));
                    }
            );

        } else if (v.getId() == R.id.editText_time) { // Show time picker
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                    .setMinute(calendar.get(Calendar.MINUTE))
                    .setTitleText("Select time")
                    .build();
            timePicker.show(getSupportFragmentManager(), "Time picker");

            timePicker.addOnPositiveButtonClickListener(view -> {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                etTime.setText(dateTimeUtil.getTime(calendar.getTimeInMillis()));
            });
        }
    }

    //This creates the RecyclerView
    private void createRecyclerView() {
        for (String s : getResources().getStringArray(R.array.categoryArray)) {
            DataItem dataItem = new DataItem(this);
            dataItem.setCategory(s);
            dataSet.add(dataItem);
        }

        recyclerAdapter = new CategoryRecyclerAdapter(dataSet, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setOnItemClickListener(position ->
                category = dataSet.get(position).getCategory());

    }


    //Set expense category, date, time, note on create
    private void setViewsNew() {
        etDate.setText(dateTimeUtil.getDate(new Date().getTime()));
        etTime.setText(dateTimeUtil.getTime(new Date().getTime()));
    }


    //Set expense category, amount, date, time, note on create to update data
    private void setViewsUpdate() {
        long timestamp = getIntent().getLongExtra("timestamp", 0);
        category = getIntent().getStringExtra("category");
        int pos = Arrays.asList(getResources().getStringArray(R.array.categoryArray)).indexOf(category);
        try {
            recyclerView.scrollToPosition(pos);
            recyclerView.post(() -> Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(pos)).itemView.performClick());
        } catch (Exception e) {
            Log.e("Category Picker Error", e.getMessage());
        }

        etAmount.setText(getIntent().getStringExtra("amount"));
        etDate.setText(dateTimeUtil.getDate(timestamp));
        etTime.setText(dateTimeUtil.getTime(timestamp));
        etNote.setText(getIntent().getStringExtra("note"));

    }


    //This saves data on button click
    private void saveData() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        String amountText = etAmount.getText().toString();

        if (amountText.isEmpty() || Float.parseFloat(amountText) <= 0) { //When amount is invalid
            Toast.makeText(getApplicationContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();

        } else if (category == null) { //When no category selected
            Toast.makeText(getApplicationContext(), "Please select a category", Toast.LENGTH_SHORT).show();

        } else { //When amount is valid and category is selected

            float amount = Float.parseFloat(amountText);
            String note = etNote.getText().toString();
            Long timestamp = dateTimeUtil.getTimestamp(etDate.getText().toString(), etTime.getText().toString());

            if (note.trim().isEmpty())
                note = null;

            //If not update data, insert new data
            if (!isUpdate) {
                databaseHelper.insertData(category, amount, note, timestamp);
            }
            //If update, update existing data
            else {
                int id = getIntent().getIntExtra("id", -1);
                databaseHelper.updateData(id, category, amount, note, timestamp);
            }
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }

    }


}