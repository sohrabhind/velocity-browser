package com.hindbyte.velocity.tabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hindbyte.velocity.R;
import com.hindbyte.velocity.activity.BrowserActivity;


public class TabLayout {

    private final Context context;

    private View tabView;
    public View getTabView() {
        return tabView;
    }

    private LinearLayout tabView2;
    private TextView tabTitle;
    public TextView tabUrl;
    private ImageView tabClose;

    public void setTabTitle(String title) {
        tabTitle.setText(title);
    }

    public void setUrl(String url) {
        if (url.equals("file:///android_asset/source.html")) {
            tabUrl.setText("");
        } else {
            tabUrl.setText(url);
        }
    }

    public TabManager tabManager;
    public TabLayout(Context context, TabManager tabManager) {
        this.context = context;
        this.tabManager = tabManager;
        initUI();
    }

    @SuppressLint("InflateParams")
    private void initUI() {
        tabView = LayoutInflater.from(context).inflate(R.layout.tab_item, null, false);
        tabView2 = tabView.findViewById(R.id.tab_view);
        tabTitle = tabView.findViewById(R.id.tab_title);
        tabUrl = tabView.findViewById(R.id.tab_url);
        tabClose = tabView.findViewById(R.id.close_tab);

        tabClose.setOnClickListener(v -> ((BrowserActivity) context).removeTab(tabManager));

        tabView.setOnTouchListener(new SwipeToDismissListener(tabView, () ->
                ((BrowserActivity) context).removeTab(tabManager)
        ));

        tabView.setOnClickListener(v ->
                ((BrowserActivity) context).showTab(tabManager,true)
        );
    }

    public void activate() {
        tabView2.setBackgroundColor(Color.parseColor("#0099BC"));
        tabClose.setBackgroundColor(Color.parseColor("#0099BC"));
    }

    public void deactivate() {
        tabView2.setBackgroundColor(Color.parseColor("#BDBDBD"));
        tabClose.setBackgroundColor(Color.parseColor("#BDBDBD"));
    }
}
