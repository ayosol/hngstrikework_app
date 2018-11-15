package com.example.solom.hotel_csv_app;

import com.example.solom.hotel_csv_app.models.DataCsv;
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
            int index = 0;
            while ((nextLine = reader.readNext()) != null) {
                //Skips CSV first rows (Column Headers/Titles)
                if (index != 0) data.add(new DataCsv(nextLine[0], nextLine[1]));
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

}
