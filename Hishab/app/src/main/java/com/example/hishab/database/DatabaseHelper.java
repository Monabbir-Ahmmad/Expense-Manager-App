package com.example.hishab.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.example.hishab.data.DataItem;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ExpenseManager.db";
    private static final int VERSION_NUMBER = 1;
    private static final String TABLE_NAME = "Transactions";
    private static final String ID = "ID";
    private static final String CATEGORY = "Category";
    private static final String AMOUNT = "Amount";
    private static final String NOTE = "Note";
    private static final String TIMESTAMP = "Timestamp";
    private static final String DELETED = "Deleted";

    private final Context context;
    private SQLiteDatabase database;


    //Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_NUMBER);
        this.context = context;
    }


    //This creates the database for the 1st time
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CATEGORY + " TEXT, "
                + AMOUNT + " INTEGER, "
                + NOTE + " TEXT, "
                + TIMESTAMP + " INTEGER, "
                + DELETED + " INTEGER DEFAULT 0);";

        try {
            db.execSQL(createTable);

        } catch (Exception e) {
            Toast.makeText(context, "Exception: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    //This checks if a table exists and upgrades table version when called
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        try {
            Toast.makeText(context, "Table Upgraded", Toast.LENGTH_SHORT).show();

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);

        } catch (Exception e) {
            Toast.makeText(context, "Exception: " + e, Toast.LENGTH_SHORT).show();
        }

    }

    //This inserts data into table
    public void insertData(String category, float amount, String note, Long timestamp) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(CATEGORY, category);
        contentValues.put(AMOUNT, amount);
        contentValues.put(NOTE, note);
        contentValues.put(TIMESTAMP, timestamp);

        long rowID = database.insert(TABLE_NAME, null, contentValues);
        database.close();

        if (rowID == -1) {
            Toast.makeText(context, "Failed to insert", Toast.LENGTH_SHORT).show();
        }

    }

    //This updates existing data
    public void updateData(int id, String category, float amount, String note, Long timestamp) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(CATEGORY, category);
        contentValues.put(AMOUNT, amount);
        contentValues.put(NOTE, note);
        contentValues.put(TIMESTAMP, timestamp);
        long rowID = database.update(TABLE_NAME, contentValues, ID + "= ?", new String[]{String.valueOf(id)});
        database.close();

        if (rowID == -1) {
            Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show();
        }
    }

    //This removes a table and creates new table
    public void deleteTable() {
        database = this.getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTs " + TABLE_NAME);

        Toast.makeText(context, "All data cleared", Toast.LENGTH_SHORT).show();
        onCreate(database);

    }

    //This removes specific row form table
    public void deleteData(int id, int isDeleted) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DELETED, isDeleted);

        long rowID = database.update(TABLE_NAME, contentValues, ID + "= ?", new String[]{String.valueOf(id)});
        database.close();

        if (rowID == -1) {
            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
        }
    }

    //This queries all data from table
    public ArrayList<DataItem> getAllData() {
        ArrayList<DataItem> allData = new ArrayList<>();
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
                + DELETED + " = 0" + " ORDER BY " + ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                DataItem dataItem = new DataItem(context);

                dataItem.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                dataItem.setCategory(cursor.getString(cursor.getColumnIndex(CATEGORY)));
                dataItem.setAmount(cursor.getFloat(cursor.getColumnIndex(AMOUNT)));
                dataItem.setNote(cursor.getString(cursor.getColumnIndex(NOTE)));
                dataItem.setTimestamp(cursor.getLong(cursor.getColumnIndex(TIMESTAMP)));

                allData.add(dataItem);

            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        return allData;
    }

    //This queries filtered data from table
    public ArrayList<DataItem> getFilteredData(String category, String sortBy, long startTimestamp, long endTimestamp) {
        String order = ID, orderBy = "DESC";

        if (category.contains("All"))
            category = "";
        if (sortBy.contains("ASC") || sortBy.contains("Oldest"))
            orderBy = "ASC";
        if (sortBy.contains("Amount"))
            order = AMOUNT;
        else if (sortBy.contains("Date"))
            order = TIMESTAMP;

        ArrayList<DataItem> allData = new ArrayList<>();
        database = this.getReadableDatabase();
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + CATEGORY
                    + " LIKE '" + category + "%' AND " + TIMESTAMP + " BETWEEN " + startTimestamp
                    + " AND " + endTimestamp + " AND " + DELETED + " = 0" + " ORDER BY " + order
                    + " " + orderBy, null);

            if (cursor.moveToFirst()) {
                do {
                    DataItem dataItem = new DataItem(context);

                    dataItem.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                    dataItem.setCategory(cursor.getString(cursor.getColumnIndex(CATEGORY)));
                    dataItem.setAmount(cursor.getFloat(cursor.getColumnIndex(AMOUNT)));
                    dataItem.setNote(cursor.getString(cursor.getColumnIndex(NOTE)));
                    dataItem.setTimestamp(cursor.getLong(cursor.getColumnIndex(TIMESTAMP)));

                    allData.add(dataItem);

                } while (cursor.moveToNext());
            }
            cursor.close();
            database.close();

        } catch (Exception e) {
            Toast.makeText(context, "Exception: " + e, Toast.LENGTH_SHORT).show();
        }
        return allData;
    }

    //This queries filtered sum from table
    public float getFilteredSum(String category, long startTimestamp, long endTimestamp) {
        if (category.contains("All"))
            category = "";

        float sum = 0;
        database = this.getReadableDatabase();
        try {
            Cursor cursor = database.rawQuery("SELECT SUM(" + AMOUNT + ") FROM " + TABLE_NAME + " WHERE " + CATEGORY
                    + " LIKE '" + category + "%' AND " + TIMESTAMP + " BETWEEN " + startTimestamp
                    + " AND " + endTimestamp + " AND " + DELETED + " = 0", null);

            if (cursor.moveToFirst()) {
                sum = cursor.getFloat(0);
            } else {
                sum = 0;
            }
            cursor.close();
            database.close();

        } catch (Exception e) {
            Toast.makeText(context, "Exception: " + e, Toast.LENGTH_SHORT).show();
        }

        return sum;
    }

}
