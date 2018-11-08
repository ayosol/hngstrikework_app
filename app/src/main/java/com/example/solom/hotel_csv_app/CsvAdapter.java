package com.example.solom.hotel_csv_app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by enyason on 11/6/18.
 */

public class CsvAdapter extends RecyclerView.Adapter<CsvAdapter.ViewHolderCSV> {


    List<DataCsv> csvList;
    Context context;

    public CsvAdapter(List<DataCsv> csvList, Context context) {
        this.csvList = csvList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderCSV onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.csv_row_item, viewGroup, false);

        return new ViewHolderCSV(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderCSV viewHolderCSV, int i) {
        DataCsv data = csvList.get(i);

        viewHolderCSV.tvPhone.setText(data.getPhone());
        viewHolderCSV.tvMsg.setText(data.getMessage());


    }

    @Override
    public int getItemCount() {

//        if (!csvList.isEmpty() && csvList != null) {
        return csvList.size();
//        }
//        return 0;
    }

    class ViewHolderCSV extends RecyclerView.ViewHolder {


        TextView tvPhone, tvMsg;

        public ViewHolderCSV(@NonNull View itemView) {
            super(itemView);

            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvMsg = itemView.findViewById(R.id.tv_msg);


        }
    }
}
