package com.hindbyte.velocity.tabs;

import android.view.View;

public interface TabManager {
    View getTabView();

    void activate();

    void deactivate();
}