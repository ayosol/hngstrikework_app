package com.example.solom.hotel_csv_app;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AltCsvParser {


    public static List<DataCsv> readCsv(Context context, String csv_path) throws IOException {
        List<DataCsv> data = new ArrayList<>();
        String[] csvLine;
        File file = new File(csv_path);

        if (file.exists()) {
            BufferedReader br;
            br = new BufferedReader(new FileReader(file));

            {
                String nextLine;

                while ((nextLine = br.readLine()) != null) {
                    csvLine = nextLine.split(",");
                    try {

                        data.add(new DataCsv("0" + csvLine[0], csvLine[1]));

                    } catch (Exception e) {
                        Log.e("Problem", e.toString());
                    }
                }
            }

        } else {
            Toast.makeText(context, "file not exists", Toast.LENGTH_SHORT).show();
        }
        return data;
    }
}