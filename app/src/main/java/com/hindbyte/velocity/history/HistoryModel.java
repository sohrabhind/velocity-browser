package com.hindbyte.velocity.history;

public class HistoryModel {

    private int id;
    private String title;
    private String link;
    private String date;
    private int viewType;

    HistoryModel(String title, String link, String date, int viewType) {
        this.title = title;
        this.link = link;
        this.date = date;
        this.viewType = viewType;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

}
