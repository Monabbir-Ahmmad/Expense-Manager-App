package com.example.hishab.ui.overview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hishab.DateTimeUtil;
import com.example.hishab.R;
import com.example.hishab.data.DataItem;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ExpenseRecyclerAdapter extends RecyclerView.Adapter<ExpenseRecyclerAdapter.RecyclerViewHolder> {

    private final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    private final Context context;
    private final ArrayList<DataItem> dataSet;
    private final DateTimeUtil dateTimeUtil;
    private final String currency;
    private onItemClickListener listener;

    //Constructor
    public ExpenseRecyclerAdapter(ArrayList<DataItem> dataSet, Context context) {
        this.dataSet = dataSet;
        this.context = context;
        dateTimeUtil = new DateTimeUtil();
        currency = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("currency", "$");

    }

    // Set recycler item click listener
    public void setOnItemClickListener(onItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_expense_list, parent, false);

        return new RecyclerViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.imageViewIcon.setImageResource(dataSet.get(position).getIcon());
        holder.textViewCategory.setText(dataSet.get(position).getCategory());
        holder.textViewAmount.setText(String.format("%s%s", currency, decimalFormat.format(dataSet.get(position).getAmount())));
        holder.textViewDateTime.setText(dateTimeUtil.getTimeAgo(dataSet.get(position).getTimestamp()));
        if (dataSet.get(position).getNote() != null) {
            holder.textViewNote.setVisibility(View.VISIBLE);
            holder.textViewNote.setText(String.format("%s", dataSet.get(position).getNote()));
        } else {
            holder.textViewNote.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    //Interface for onItemClickListener
    public interface onItemClickListener {
        void onItemClick(int position);
    }

    //Inner view holder class
    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        public final ImageView imageViewIcon;
        public final TextView textViewCategory;
        public final TextView textViewAmount;
        public final TextView textViewDateTime;
        public final TextView textViewNote;

        //Inner classConstructor
        public RecyclerViewHolder(View itemView, onItemClickListener listener) {
            super(itemView);

            //Find views
            imageViewIcon = itemView.findViewById(R.id.recycleView_icon);
            textViewCategory = itemView.findViewById(R.id.recycleView_category);
            textViewAmount = itemView.findViewById(R.id.recycleView_amount);
            textViewDateTime = itemView.findViewById(R.id.recycleView_dateTime);
            textViewNote = itemView.findViewById(R.id.recycleView_note);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }


}
