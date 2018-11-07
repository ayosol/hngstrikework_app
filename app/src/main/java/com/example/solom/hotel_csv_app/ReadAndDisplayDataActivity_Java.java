package com.example.solom.hotel_csv_app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReadAndDisplayDataActivity_Java {

    //using inputstream to read data from a file
    InputStream inputStream;

    public ReadAndDisplayDataActivity_Java(InputStream is){
        this.inputStream = is;
    }

    public List<String[]> read(){
        List<String[]> resultList = new ArrayList<String[]>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String csvLine;
            while((csvLine = reader.readLine()) != null){
                String[] row = csvLine.split(",");
                resultList.add(row);
            }
        } catch(IOException ex){
            throw new RuntimeException("Error in reading CSV file:" + ex);
        } finally {
            try{
                inputStream.close();
            }catch (IOException e) {
                throw new RuntimeException("Error while closinge input stream:" + e);
            }
        }
        return resultList;
    }


}