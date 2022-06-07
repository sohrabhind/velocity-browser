package com.hindbyte.velocity.home;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import com.hindbyte.velocity.R;
import com.hindbyte.velocity.activity.BrowserActivity;


public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    private final List<HomeModel> itemList;
    private final Context context;

    public HomeAdapter(Context context, List<HomeModel> itemList) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.itemList = itemList;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.home_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final HomeModel item = itemList.get(position);
        holder.myIconText.setText(item.getTitle().substring(0,1));
        holder.myTextView.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView myIconText;
        TextView myTextView;
        ViewHolder(View itemView) {
            super(itemView);
            myIconText = itemView.findViewById(R.id.icon_text);
            myTextView = itemView.findViewById(R.id.info_text);
            itemView.setOnClickListener(v -> ((BrowserActivity) context).onItemClick(getAdapterPosition()));
            itemView.setOnLongClickListener(v -> {
                ((BrowserActivity) context).onItemLongClick(getAdapterPosition());
                return false;
            });
        }
    }
}