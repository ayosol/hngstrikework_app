package com.example.solom.hotel_csv_app.models;

public class RecentlyOpened {
    private String mDate;
    private String mPath;
    private String mTime;

    public RecentlyOpened(String path, String date) {
        this.mPath = path;
        this.mDate = date;
    }

    public RecentlyOpened(String path, String date, String time) {
        this.mPath = path;
        this.mDate = date;
        this.mTime = time;
    }

    public String getmPath() {
        return mPath;
    }

    public String getmDate() {
        return mDate;
    }

    public String getmTime() {
        return mTime;
    }
}
