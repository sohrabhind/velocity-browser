package com.hindbyte.velocity.history;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hindbyte.velocity.R;

import java.util.List;


public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private HistoryAdapter.ItemClickListener mClickListener;
    private final List<HistoryModel> itemList;
    private final Context context;

    public HistoryAdapter(Context context, List<HistoryModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch(viewType){
            case 1:{
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
                viewHolder = new MyViewHolder(v);
                break;
            }
            case 2: {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item_date, parent, false);
                viewHolder = new ViewHolderProgress(v);
                break;
            }
        }
        assert viewHolder != null;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        switch(holder.getItemViewType()){
            case 1:{
                MyViewHolder viewHolder = (MyViewHolder) holder;
                final HistoryModel item = itemList.get(position);
                viewHolder.myIconText.setText(item.getTitle());
                viewHolder.myTextView.setText(item.getLink());
                break;
            }
            case 2:{
                ViewHolderProgress viewHolder = (ViewHolderProgress) holder;
                final HistoryModel item = itemList.get(position);
                viewHolder.dateText.setText(item.getDate());
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getViewType();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout historyItem;
        TextView myIconText;
        TextView myTextView;
        ImageView deleteHistory;
        @SuppressLint("NotifyDataSetChanged")
        MyViewHolder(View itemView) {
            super(itemView);
            historyItem = itemView.findViewById(R.id.history_item);
            myIconText = itemView.findViewById(R.id.icon_text);
            myTextView = itemView.findViewById(R.id.info_text);
            deleteHistory = itemView.findViewById(R.id.delete_history_item);
            historyItem.setOnClickListener(v -> {
                if (mClickListener != null) {
                    mClickListener.onItemClick(itemList.get(getLayoutPosition()).getLink());
                }
            });
            deleteHistory.setOnClickListener(v -> {
                HistoryData db = new HistoryData(context);
                db.deleteItem(String.valueOf(itemList.get(getLayoutPosition()).getID()));
                itemList.remove(getLayoutPosition());
                notifyDataSetChanged();
            });
        }
    }

    public void setClickListener(HistoryAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(String link);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private class ViewHolderProgress extends RecyclerView.ViewHolder {
        TextView dateText;
        private ViewHolderProgress(View view) {
            super(view);
            dateText = itemView.findViewById(R.id.icon_text);
        }
    }

}