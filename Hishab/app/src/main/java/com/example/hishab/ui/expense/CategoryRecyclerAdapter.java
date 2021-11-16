package com.example.hishab.ui.expense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hishab.R;
import com.example.hishab.data.DataItem;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.RecyclerViewHolder> {

    private final Context context;
    private final ArrayList<DataItem> dataSet;
    private int clickedPosition = -1;
    private OnItemClickListener listener;


    //Constructor
    public CategoryRecyclerAdapter(ArrayList<DataItem> dataSet, Context context) {
        this.dataSet = dataSet;
        this.context = context;
    }

    // Set recycler item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    //Refresh item position
    public void resetClickedPosition() {
        clickedPosition = -1;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pick_category, parent, false);

        return new RecyclerViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.ivIcon.setImageResource(dataSet.get(position).getIcon());
        holder.tvLabel.setText(dataSet.get(position).getCategory());

        // Clear highlighted items that are not selected
        if (clickedPosition == position) {
            holder.cardContainer.setStrokeWidth(5);
        } else {
            holder.cardContainer.setStrokeWidth(0);

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
    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        public final ImageView ivIcon;
        public final TextView tvLabel;
        public final MaterialCardView cardContainer;

        //Inner classConstructor
        public RecyclerViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);

            //Find views
            ivIcon = itemView.findViewById(R.id.rec_categoryIcon);
            tvLabel = itemView.findViewById(R.id.rec_categoryLabel);
            cardContainer = itemView.findViewById(R.id.rec_categoryContainer);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();

                    // On item click highlight that item
                    cardContainer.setStrokeWidth(5);
                    cardContainer.setBackground(new MaterialCardView(context).getBackground());

                    if (position != RecyclerView.NO_POSITION && clickedPosition != position) {
                        notifyItemChanged(clickedPosition);
                        clickedPosition = position;
                        listener.onItemClick(position);
                    }
                }
            });
        }

    }


}
