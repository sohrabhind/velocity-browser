package com.hindbyte.velocity.tabs;

import java.util.ArrayList;

public class TabListManager {

    /*
    LinkedList is fast for adding and deleting elements, but slow to access a specific element.
    ArrayList is fast for accessing a specific element but can be slow to add to either end, and
    especially slow to delete in the middle.
     */


    public ArrayList<TabManager> tabList = new ArrayList<>();

    public TabManager getController(int index) {
        return tabList.get(index);
    }

    public synchronized void add(TabManager controller) {
        tabList.add(controller);
    }

    public synchronized void add(TabManager controller, int index) {
        tabList.add(index, controller);
    }

    public synchronized void remove(TabManager controller) {
        tabList.remove(controller);
    }

    public int getItemPosition(TabManager controller) {
        return tabList.indexOf(controller);
    }

    public int getSize() {
        return tabList.size();
    }

    public synchronized void clear() {
        tabList.clear();
    }
}