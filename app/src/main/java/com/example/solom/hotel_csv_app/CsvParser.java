package com.example.solom.hotel_csv_app;

import android.content.Context;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enyason on 11/6/18.
 */

public class CsvParser {

    public static List<DataCsv> readCsv(String csv_path) {

        List<DataCsv> data = new ArrayList<>();
        CSVReader reader;
        String[] nextLine;

        try {
            reader = new CSVReader(new FileReader(csv_path));
            while ((nextLine = reader.readNext()) != null) {
                data.add(new DataCsv("0" + nextLine[0], nextLine[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

}
