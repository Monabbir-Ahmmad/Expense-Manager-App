package com.example.hishab.data;

import android.content.Context;
import android.content.res.TypedArray;

import com.example.hishab.R;

import java.util.Arrays;

public class DataItem {

    public static final String INCOME = "Income";
    public static final String EXPENSE = "Expense";

    private final Context context;
    private int id;
    private String transactionType;
    private String category;
    private float amount;
    private String note;
    private int icon;
    private long timestamp;

    //Constructor
    public DataItem(Context context) {
        this.context = context;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getIcon() {
        return icon;
    }

    //Use transaction type and category to set icon
    public void setIcon(String transactionType, String category) {
        String[] categoryArray = context.getResources().getStringArray(
                transactionType.equals(EXPENSE) ? R.array.expenseCategoryArray : R.array.incomeCategoryArray);

        TypedArray iconArray = context.getResources().obtainTypedArray(
                transactionType.equals(EXPENSE) ? R.array.expenseIconArray : R.array.incomeIconArray);

        int index = Arrays.asList(categoryArray).indexOf(category);
        this.icon = iconArray.getResourceId(index, 0);
    }

    @Override
    public String toString() {
        return "DataItem{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", note='" + note + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
