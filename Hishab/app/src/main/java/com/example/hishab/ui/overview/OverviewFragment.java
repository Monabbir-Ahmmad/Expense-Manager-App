package com.example.hishab.ui.overview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hishab.R;
import com.example.hishab.data.DataItem;
import com.example.hishab.database.DatabaseHelper;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class OverviewFragment extends Fragment {

    private TextView tvTotalExpense, tvTotalIncome, tvBalanceLeft;
    private LinearLayout noDataLayout;
    private AppBarLayout appBarLayout;
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
        tvTotalIncome = view.findViewById(R.id.textView_totalIncome);
        tvBalanceLeft = view.findViewById(R.id.textView_balanceLeft);
        recyclerView = view.findViewById(R.id.recyclerView);
        noDataLayout = view.findViewById(R.id.noDataLayout);
        appBarLayout = view.findViewById(R.id.appBarLayout_overview);

        databaseHelper = new DatabaseHelper(getActivity());
        dataSet = databaseHelper.getAllData();

        //This calculates the top panel values on startup
        refreshTopViews();

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
        //Open filter dialog
        if (item.getItemId() == R.id.menu_filter) {
            FilterDialog filterDialog = new FilterDialog();
            //Filter the dataset
            filterDialog.setOnPositiveButtonClickListener((categoryArray, sortBy, startTimestamp, endTimestamp) -> {
                dataSet.clear();
                dataSet.addAll(databaseHelper.getFilteredData(categoryArray, sortBy, startTimestamp, endTimestamp));
                recyclerAdapter.notifyDataSetChanged();
                appBarLayout.setExpanded(true, true);
                refreshTopViews();
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
        refreshTopViews();
        databaseHelper.deleteData(dataItem.getId(), 1);

        //SnackBar for undoing item delete
        Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG).setAction("Undo", view -> {
            dataSet.add(position, dataItem);
            recyclerAdapter.notifyItemInserted(position);
            recyclerView.smoothScrollToPosition(position);
            refreshTopViews();
            databaseHelper.deleteData(dataItem.getId(), 0);
        }).show();
    }


    //This calculates the top panel values and show no data found view if needed
    private void refreshTopViews() {
        double totalExpense = 0, totalIncome = 0, balanceLeft;
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        String currency = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("currency", "$");

        for (DataItem dataItem : dataSet) {
            if (dataItem.getTransactionType().equals(DataItem.EXPENSE))
                totalExpense += dataItem.getAmount();
            else if (dataItem.getTransactionType().equals(DataItem.INCOME))
                totalIncome += dataItem.getAmount();
        }
        balanceLeft = totalIncome - totalExpense;

        //This will set the total expense, income and balance left
        tvTotalExpense.setText(String.format("%s%s", currency, decimalFormat.format(totalExpense)));
        tvTotalIncome.setText(String.format("%s%s", currency, decimalFormat.format(totalIncome)));
        tvBalanceLeft.setText(String.format("%s%s%s", balanceLeft < 0 ? "-" : "", currency, decimalFormat.format(Math.abs(balanceLeft))));

        //Show recycler view or no data found based on data found or not
        recyclerView.setVisibility(dataSet.isEmpty() ? View.GONE : View.VISIBLE);
        noDataLayout.setVisibility(dataSet.isEmpty() ? View.VISIBLE : View.GONE);

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


    //RecyclerView swipe gesture
    private void createRecyclerViewSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ExpenseRecyclerItemTouchHelper(getActivity()) {
            //Swipe gesture listener
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                deleteItem(viewHolder.getAdapterPosition());
            }
        });

        //Attach itemTouchHelper to recyclerView
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


}