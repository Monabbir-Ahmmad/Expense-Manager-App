package com.example.hishab.database;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.hishab.data.DataItem;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

public class LocalBackupDB {

    private final Context context;
    private final DatabaseHelper db;

    //Backup file name
    private final String csvFileName = "Hishab_Backup.csv";

    public LocalBackupDB(Context context) {
        this.context = context;
        db = new DatabaseHelper(context);
    }

    public boolean restoreData() {
        //Backup folder name
        String filePathAndName = Environment.getExternalStorageDirectory() + "/HishabBackup/" + csvFileName;

        //Get the file
        File file = new File(filePathAndName);

        if (file.exists()) { //When backup exists
            try {
                CSVReader csvReader = new CSVReader(new FileReader(file.getAbsolutePath()));
                String[] nextLine;

                // Read each row in the file
                while ((nextLine = csvReader.readNext()) != null) {
                    int id = Integer.parseInt(nextLine[0]);
                    String category = nextLine[1];
                    float amount = Float.parseFloat(nextLine[2]);
                    long timestamp = Long.parseLong(nextLine[3]);
                    String note = null;
                    if (nextLine[4].trim().length() > 0)
                        note = nextLine[4];

                    if (id > 0)
                        db.insertData(category, amount, note, timestamp);
                }
                Toast.makeText(context, "Backup restored", Toast.LENGTH_LONG).show();
                return true;

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(context, "Backup file not found", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public void backupData() {
        //Backup folder name
        File folder = new File(Environment.getExternalStorageDirectory() + "/HishabBackup");

        //Create folder if not exists
        if (!folder.exists()) {
            folder.mkdir();
        }

        //Complete path and name
        String filePathAndName = folder.toString() + "/" + csvFileName;

        ArrayList<DataItem> dataset = db.getAllData();
        Collections.reverse(dataset);

        if (dataset.size() > 0) { // When there is data
            try {
                //Write CSV file
                FileWriter fileWriter = new FileWriter(filePathAndName);

                for (int i = 0; i < dataset.size(); i++) {
                    fileWriter.append(String.valueOf(dataset.get(i).getId()));
                    fileWriter.append(",");
                    fileWriter.append(dataset.get(i).getCategory());
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(dataset.get(i).getAmount()));
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(dataset.get(i).getTimestamp()));
                    fileWriter.append(",");
                    if (dataset.get(i).getNote() == null)
                        fileWriter.append("");
                    else
                        fileWriter.append(dataset.get(i).getNote());

                    fileWriter.append("\n");
                }
                fileWriter.flush();
                fileWriter.close();

                Toast.makeText(context, "Backup exported to:" + filePathAndName, Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Nothing to backup", Toast.LENGTH_LONG).show();
        }
    }

}
