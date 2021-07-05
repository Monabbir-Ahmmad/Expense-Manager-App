package com.example.hishab.ui.overview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hishab.R;
import com.example.hishab.data.DataItem;
import com.example.hishab.database.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class OverviewFragment extends Fragment {

    private TextView tvTotalExpense, tvRecordsFound;
    private RecyclerView recyclerView;
    private ExpenseRecyclerAdapter recyclerAdapter;
    private DatabaseHelper databaseHelper;
    private ArrayList<DataItem> dataSet;


    public OverviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        setHasOptionsMenu(true);
        getActivity().setTitle("Overview");


        //Find views
        tvTotalExpense = view.findViewById(R.id.textView_totalExpense);
        tvRecordsFound = view.findViewById(R.id.textView_recordsFound);
        recyclerView = view.findViewById(R.id.recyclerView);

        databaseHelper = new DatabaseHelper(getActivity());
        dataSet = databaseHelper.getAllData();

        //This calculates the top panel values on startup
        topPanelCalculation();

        //This creates the RecyclerView
        createRecyclerView();
        createRecyclerViewSwipe();

        return view;
    }


    //Inflate the toolbar menus
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        menu.findItem(R.id.menu_save).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Toolbar menu item click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_filter) { //Open filter dialog
            FilterDialog filterDialog = new FilterDialog();
            filterDialog.setOnPositiveButtonClickListener((category, sortBy, startTimestamp, endTimestamp) -> {
                dataSet = databaseHelper.getFilteredData(category, sortBy, startTimestamp, endTimestamp);
                createRecyclerView();
                topPanelCalculation();
            });
            filterDialog.show(getActivity().getSupportFragmentManager(), "FilterDialog");
        }

        return super.onOptionsItemSelected(item);
    }


    //Delete data when delete gesture is used and show snackBar to undo
    private void deleteItem(int position) {
        DataItem dataItem = dataSet.get(position);
        dataSet.remove(position);
        recyclerAdapter.notifyItemRemoved(position);
        topPanelCalculation();
        databaseHelper.deleteData(dataItem.getId(), 1);

        //SnackBar for undoing item delete
        Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG).setAction("Undo", view -> {
            dataSet.add(position, dataItem);
            recyclerAdapter.notifyItemInserted(position);
            recyclerView.smoothScrollToPosition(position);
            topPanelCalculation();
            databaseHelper.deleteData(dataItem.getId(), 0);
        }).show();
    }


    //This calculates the top panel values on startup
    private void topPanelCalculation() {
        double totalExpense = 0;
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        String currency = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("currency", "$");

        for (int i = 0; i < dataSet.size(); i++) {
            totalExpense += dataSet.get(i).getAmount();
        }

        //This will set the current total expense
        tvTotalExpense.setText(String.format("%s%s", currency, decimalFormat.format(totalExpense)));
        tvRecordsFound.setText(String.valueOf(dataSet.size()));
    }


    //This creates the RecyclerView
    private void createRecyclerView() {
        recyclerAdapter = new ExpenseRecyclerAdapter(dataSet, getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(recyclerAdapter);

        recyclerAdapter.setOnItemClickListener(position -> {
            //This opens a bottom sheet with details from recyclerView item
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(dataSet.get(position));
            bottomSheetDialog.show(getActivity().getSupportFragmentManager(), "BottomDialog");
        });

    }


    ////RecyclerView swipe gesture
    private void createRecyclerViewSwipe() {

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            //Swipe gesture listener
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                deleteItem(viewHolder.getAdapterPosition());
            }

            //Draw background with icon for recyclerView swipe gesture
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState,
                                    boolean isCurrentlyActive) {

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;

                Drawable background = ContextCompat.getDrawable(getActivity(), R.drawable.shape_rounded_rectangle);
                background.setTint(Color.parseColor("#FF432C"));
                background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());

                Drawable icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_delete);
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

                background.draw(c);
                icon.draw(c);
            }
        });

        //Attach itemTouchHelper to recyclerView
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }


}