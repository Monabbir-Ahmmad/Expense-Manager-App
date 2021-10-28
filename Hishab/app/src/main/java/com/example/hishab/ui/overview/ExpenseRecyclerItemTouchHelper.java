package com.example.hishab.ui.overview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hishab.R;

public abstract class ExpenseRecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private final Context context;

    //Constructor
    public ExpenseRecyclerItemTouchHelper(Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;

    }

    @Override
    public abstract void onSwiped(RecyclerView.ViewHolder viewHolder, int direction);

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;

        Drawable background = ContextCompat.getDrawable(context, R.drawable.shape_rounded_rectangle);
        background.setTint(Color.parseColor("#FF432C"));
        background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());

        Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        icon.setTint(Color.WHITE);

        int iconMargin = itemView.getHeight() / 4;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();
        int iconLeft, iconRight;

        if (dX > 0) { // Swiping to the right
            iconLeft = itemView.getLeft() + iconMargin;
            iconRight = iconLeft + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

        } else if (dX < 0) { // Swiping to the left
            iconRight = itemView.getRight() - iconMargin;
            iconLeft = iconRight - icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(canvas);
        icon.draw(canvas);
    }
}
