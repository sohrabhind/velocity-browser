package com.hindbyte.velocity.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hindbyte.velocity.R;
import com.hindbyte.velocity.activity.BrowserActivity;
import com.hindbyte.velocity.history.HistoryAdapter;
import com.hindbyte.velocity.history.HistoryData;
import com.hindbyte.velocity.history.HistoryModel;
import com.hindbyte.velocity.util.ToastWindow;

import java.util.List;

public class HistoryFragment extends Fragment implements HistoryAdapter.ItemClickListener{

    LinearLayout linearLayout;
    TextView clearAllHistory;
    HistoryAdapter adapter;

    BrowserInterface browserInterface;

    public HistoryFragment(BrowserInterface browserInterface) {
        this.browserInterface = browserInterface;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View fragmentView = inflater.inflate(R.layout.history_fragment, null);
        HistoryData db = new HistoryData(getContext());
        RecyclerView recycler_view = fragmentView.findViewById(R.id.recycler_view);
        clearAllHistory = fragmentView.findViewById(R.id.clear_all_history);
        linearLayout = fragmentView.findViewById(R.id.main_layout);
        List<HistoryModel> itemList = db.getItemDetails();
        recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(getContext(), itemList);
        adapter.setClickListener(this);
        recycler_view.setAdapter(adapter);
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearLayout.setOnClickListener(v-> Log.d("RUNNING", "Running"));
        clearAllHistory.setOnClickListener(v-> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setCancelable(false);
            builder.setMessage("Do you want to clear all history?");
            builder.setPositiveButton(R.string.dialog_button_positive, (dialog, which) -> {
                HistoryData db = new HistoryData(getContext());
                db.deleteData();
                ToastWindow toastWindow = new ToastWindow();
                toastWindow.makeText(getContext(), "History Cleared", 1500);
                onBackPress();
            });
            builder.setNegativeButton(R.string.dialog_button_negative, (dialog, which) -> {
            });
            builder.create().show();
        });
    }

    public void onBackPress() {
        FragmentTransaction fragmentTransaction;
        fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(this).commit();
    }

    @Override
    public void onItemClick(String link) {
        browserInterface.updateTab(link);
        onBackPress();
    }
}