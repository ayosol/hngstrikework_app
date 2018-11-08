package com.example.solom.hotel_csv_app;

import android.content.Context;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enyason on 11/6/18.
 */

public class CsvParser {


    public static List<DataCsv> readcsv(Context context) {

        List<DataCsv> data = new ArrayList<>();
        CSVReader reader;
        String[] nextLine;

        try {

            reader = new CSVReader(new InputStreamReader(context.getResources().openRawResource(R.raw.hng)));
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
//                System.out.println(nextLine[0] + nextLine[1] + "etc...");
//                Log.i("CSV", nextLine[0] + nextLine[1]);
                data.add(new DataCsv("0" + nextLine[0], nextLine[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;

    }


}
