package com.example.hishab.database;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.hishab.R;
import com.example.hishab.data.DataItem;
import com.example.hishab.mutilities.DateTimeUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LocalBackupDB {

    private final Context context;
    private final DatabaseHelper db;

    //Constructor
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
                CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(context.getContentResolver().openOutputStream(fileUri, "wt")));

                //The first row are the headers
                csvWriter.writeNext(new String[]{"TRANSACTION_TYPE", "CATEGORY", "AMOUNT", "DATE", "TIME", "NOTE"}, true);

                //Write each row in the file
                for (DataItem dataItem : dataset) {
                    csvWriter.writeNext(new String[]{dataItem.getTransactionType(),
                            dataItem.getCategory(),
                            String.valueOf(dataItem.getAmount()),
                            dateTimeUtil.getDate(dataItem.getTimestamp()),
                            dateTimeUtil.getTime(dataItem.getTimestamp()),
                            dataItem.getNote() == null ? "" : dataItem.getNote()}, true);
                }

                csvWriter.close();

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

            //Skip the first row because they are the headers
            String[] nextLine = csvReader.readNext();

            DateTimeUtil dateTimeUtil = new DateTimeUtil();
            List<String> categoryArray = new ArrayList<>();
            categoryArray.addAll(Arrays.asList(context.getResources().getStringArray(R.array.expenseCategoryArray)));
            categoryArray.addAll(Arrays.asList(context.getResources().getStringArray(R.array.incomeCategoryArray)));

            //Read each row in the file
            while ((nextLine = csvReader.readNext()) != null) {
                String transactionType = nextLine[0];
                String category = nextLine[1];
                float amount = Float.parseFloat(nextLine[2]);
                long timestamp = dateTimeUtil.getTimestamp(nextLine[3], nextLine[4]);
                String note = null;
                if (nextLine[5].trim().length() > 0)
                    note = nextLine[5];

                if (categoryArray.contains(category))
                    db.insertData(transactionType, category, amount, note, timestamp);
            }

            csvReader.close();

            Toast.makeText(context, "Data restored", Toast.LENGTH_LONG).show();
            return true;

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }

}
