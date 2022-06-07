package com.hindbyte.velocity.search;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import com.hindbyte.velocity.R;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<SearchModel> searchModelList;

    private ItemClickListener mClickListener;
    public SearchAdapter(List<SearchModel> searchModelList) {
        this.searchModelList = searchModelList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder viewHolder = (MyViewHolder) holder;
        SearchModel searchModel = searchModelList.get(position);
        viewHolder.title.setText(searchModel.getTitle());
    }

    @Override
    public int getItemCount() {
        return searchModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView next;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            next = view.findViewById(R.id.next);
            title.setOnClickListener(v -> {
                if (mClickListener != null) {
                    mClickListener.onItemClick(searchModelList.get(getLayoutPosition()).getTitle());
                }
            });
            next.setOnClickListener(v -> {
                if (mClickListener != null) {
                    mClickListener.onNextClick(searchModelList.get(getLayoutPosition()).getTitle());
                }
            });
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(String searchText);
        void onNextClick(String searchText);
    }
}