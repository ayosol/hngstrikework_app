package com.example.solom.hotel_csv_app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ItemArrayAdapter extends ArrayAdapter<String[]>{

    public List<String[]> scoreList = new ArrayList<String[]>();

    static class  ItemViewHolder{
        TextView phone_numbers;
        TextView messages;
    }

    public ItemArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public void add(String[] object){
        scoreList.add(object);
        super.add(object);
    }

    @Override
    public int getCount(){
        return this.scoreList.size();
    }

    @Override
    public String[] getItem(int position){
        return this.scoreList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View row = convertView;
        ItemViewHolder viewHolder;
        if(row == null){
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.single_list_item, parent, false);
            viewHolder = new ItemViewHolder();
            viewHolder.phone_numbers = row.findViewById(R.id.name);
            viewHolder.messages = row.findViewById(R.id.score);
            row.setTag(viewHolder);
        } else {
            viewHolder = (ItemViewHolder) row.getTag();
        }
        String[] stat = getItem(position);
        viewHolder.phone_numbers.setText(stat[0]);
        viewHolder.messages.setText(stat[1]);
        return row;
    }
}
