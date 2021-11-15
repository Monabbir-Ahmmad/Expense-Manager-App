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

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ExpenseRecyclerAdapter extends RecyclerView.Adapter<ExpenseRecyclerAdapter.RecyclerViewHolder> {

    private final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    private final Context context;
    private final ArrayList<DataItem> dataSet;
    private final DateTimeUtil dateTimeUtil;
    private final String currency;
    private OnItemClickListener listener;

    //Constructor
    public ExpenseRecyclerAdapter(ArrayList<DataItem> dataSet, Context context) {
        this.dataSet = dataSet;
        this.context = context;
        dateTimeUtil = new DateTimeUtil();
        currency = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("currency", "$");

    }

    // Set recycler item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
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
        String symbol = "-";
        if (dataSet.get(position).getTransactionType().equals(DataItem.INCOME)) {
            symbol = "+";
            holder.tvAmount.setTextColor(context.getColor(R.color.light_green));
        } else if (dataSet.get(position).getTransactionType().equals(DataItem.EXPENSE)) {
            symbol = "-";
            holder.tvAmount.setTextColor(context.getColor(R.color.light_red));
        }

        holder.ivIcon.setImageResource(dataSet.get(position).getIcon());
        holder.tvCategory.setText(dataSet.get(position).getCategory());
        holder.tvAmount.setText(String.format("%s%s%s", symbol, currency, decimalFormat.format(dataSet.get(position).getAmount())));
        holder.tvDateTime.setText(dateTimeUtil.getTimeAgo(dataSet.get(position).getTimestamp()));
        if (StringUtils.isBlank(dataSet.get(position).getNote())) {
            holder.tvNote.setVisibility(View.GONE);
        } else {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText(String.format("%s", dataSet.get(position).getNote()));
        }

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    //Interface for onItemClickListener
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    //Inner view holder class
    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        public final ImageView ivIcon;
        public final TextView tvCategory, tvAmount, tvDateTime, tvNote;

        //Inner classConstructor
        public RecyclerViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);

            //Find views
            ivIcon = itemView.findViewById(R.id.rec_listIcon);
            tvCategory = itemView.findViewById(R.id.rec_listCategory);
            tvAmount = itemView.findViewById(R.id.rec_listAmount);
            tvDateTime = itemView.findViewById(R.id.rec_listDateTime);
            tvNote = itemView.findViewById(R.id.rec_listNote);

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
