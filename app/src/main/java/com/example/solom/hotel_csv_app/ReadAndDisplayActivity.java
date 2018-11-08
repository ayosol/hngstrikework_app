package com.example.solom.hotel_csv_app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReadAndDisplayActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    List<DataCsv> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_and_display);
        Bundle extras = getIntent().getExtras();
        data = new ArrayList<>();
        recyclerView = findViewById(R.id.rv_csv);
        adapter = new CsvAdapter(data, this);
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        data.addAll(CsvParser.readcsv(this, extras.getString(MainActivity.EXTRAS_CSV_PATH_NAME)));
        adapter.notifyDataSetChanged();

    }
}
