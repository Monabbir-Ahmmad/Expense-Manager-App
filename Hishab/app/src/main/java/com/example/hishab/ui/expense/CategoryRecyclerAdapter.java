package com.example.hishab.ui.expense;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
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
    private final TypedValue colorPrimary, bgColor;
    private int checkedPosition = -1;
    private onItemClickListener listener;


    //Constructor
    public CategoryRecyclerAdapter(ArrayList<DataItem> dataSet, Context context) {
        this.dataSet = dataSet;
        this.context = context;
        colorPrimary = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);
        bgColor = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.bgColor, bgColor, true);
    }

    // Set recycler item click listener
    public void setOnItemClickListener(onItemClickListener listener) {
        this.listener = listener;
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
        holder.imageViewIcon.setImageResource(dataSet.get(position).getIcon());
        holder.textViewLabel.setText(dataSet.get(position).getCategory());

        // Clear highlighted items that are not selected
        if (checkedPosition == position) {
            holder.cardContainer.setCardBackgroundColor(colorPrimary.data);
            holder.imageViewIcon.setColorFilter(Color.WHITE);
            holder.textViewLabel.setTextColor(Color.WHITE);
        } else {
            holder.cardContainer.setCardBackgroundColor(bgColor.data);
            holder.imageViewIcon.setColorFilter(colorPrimary.data);
            holder.textViewLabel.setTextColor(colorPrimary.data);
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
    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        public final ImageView imageViewIcon;
        public final TextView textViewLabel;
        public final MaterialCardView cardContainer;

        //Inner classConstructor
        public RecyclerViewHolder(View itemView, onItemClickListener listener) {
            super(itemView);

            //Find views
            imageViewIcon = itemView.findViewById(R.id.rec_categoryIcon);
            textViewLabel = itemView.findViewById(R.id.rec_categoryLabel);
            cardContainer = itemView.findViewById(R.id.rec_categoryContainer);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();

                    // On item click highlight that item
                    cardContainer.setCardBackgroundColor(colorPrimary.data);
                    imageViewIcon.setColorFilter(Color.WHITE);
                    textViewLabel.setTextColor(Color.WHITE);

                    if (position != RecyclerView.NO_POSITION && checkedPosition != position) {
                        notifyItemChanged(checkedPosition);
                        checkedPosition = position;
                        listener.onItemClick(position);
                    }
                }
            });
        }

    }


}
