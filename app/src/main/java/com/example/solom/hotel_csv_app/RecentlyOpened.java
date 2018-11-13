package com.example.solom.hotel_csv_app;

public class RecentlyOpened {
    private final String mDate;
    private final String mPath;

    RecentlyOpened(String path, String date) {
        this.mPath = path;
        this.mDate = date;
    }

    public String getmPath() {
        return mPath;
    }

    public String getmDate() {
        return mDate;
    }
}
