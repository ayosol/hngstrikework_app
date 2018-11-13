package com.example.solom.hotel_csv_app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.solom.hotel_csv_app.R;
import com.example.solom.hotel_csv_app.RecentlyOpened;

import java.util.List;

public class RecentlyOpenedRvAdapter extends RecyclerView.Adapter<RecentlyOpenedRvAdapter.FileHolder> {

    private List<RecentlyOpened> mFileList;
    private Context context;
    private AdapterView.OnItemClickListener onItemClickListener;

    public RecentlyOpenedRvAdapter(List<RecentlyOpened> fileList, Context context) {
        this.mFileList = fileList;
        this.context = context;
    }

    @NonNull
    @Override
    public FileHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recently_opened_rv, viewGroup, false);
        return new FileHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileHolder fileHolder, int i) {
        RecentlyOpened recentFiles = mFileList.get(i);
        fileHolder.mTitle_tv.setText(recentFiles.getmPath().substring(mFileList.get(i).getmPath().lastIndexOf("/")));
        fileHolder.mDate_tv.setText(recentFiles.getmDate());
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }

    class FileHolder extends RecyclerView.ViewHolder {

        private final TextView mTitle_tv;
        private final TextView mDate_tv;

        public FileHolder(@NonNull View itemView) {
            super(itemView);
            mTitle_tv = itemView.findViewById(R.id.recently_opened_rv_item_title);
            mDate_tv = itemView.findViewById(R.id.recently_opened_rv_item_date);

        }
    }
}
