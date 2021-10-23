package com.example.hishab.database;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.hishab.DateTimeUtil;
import com.example.hishab.R;
import com.example.hishab.data.DataItem;
import com.opencsv.CSVReader;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LocalBackupDB {

    private final Context context;
    private final DatabaseHelper db;

    public LocalBackupDB(Context context) {
        this.context = context;
        db = new DatabaseHelper(context);
    }

    //Method to backup data form database
    public void backupData(Uri fileUri) {
        ArrayList<DataItem> dataset = db.getAllData();
        Collections.reverse(dataset);

        if (dataset.size() > 0) { // When there is data
            DateTimeUtil dateTimeUtil = new DateTimeUtil();

            try {
                //Write CSV file
                OutputStream fileOutputStream = context.getContentResolver().openOutputStream(fileUri);

                //The first row are the headers
                fileOutputStream.write("CATEGORY,AMOUNT,DATE,TIME,NOTE\n".getBytes());

                for (DataItem dataItem : dataset) {
                    fileOutputStream.write(String.format("%s,%s,%s,%s,\"%s\"\n",
                            dataItem.getCategory(),
                            dataItem.getAmount(),
                            dateTimeUtil.getDate(dataItem.getTimestamp()),
                            dateTimeUtil.getTime(dataItem.getTimestamp()),
                            dataItem.getNote() == null ? "" : dataItem.getNote()
                    ).getBytes());
                }

                fileOutputStream.flush();
                fileOutputStream.close();

                Toast.makeText(context, "Backup file created", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Nothing to backup", Toast.LENGTH_LONG).show();
        }
    }

    //Method to restore data form backup file
    public boolean restoreData(Uri fileUri) {
        try {
            CSVReader csvReader = new CSVReader(new InputStreamReader(context.getContentResolver().openInputStream(fileUri)));

            //Skip the first row because the are the headers
            String[] nextLine = csvReader.readNext();

            DateTimeUtil dateTimeUtil = new DateTimeUtil();
            List<String> categoryArray = Arrays.asList(context.getResources().getStringArray(R.array.categoryArray));

            // Read each row in the file
            while ((nextLine = csvReader.readNext()) != null) {
                String category = nextLine[0];
                float amount = Float.parseFloat(nextLine[1]);
                long timestamp = dateTimeUtil.getTimestamp(nextLine[2], nextLine[3]);
                String note = null;
                if (nextLine[4].trim().length() > 0)
                    note = nextLine[4];

                if (categoryArray.contains(category))
                    db.insertData(category, amount, note, timestamp);
            }

            csvReader.close();

            Toast.makeText(context, "Data restored", Toast.LENGTH_LONG).show();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
