package com.example.solom.hotel_csv_app.models;

/**
 * Created by enyason on 11/6/18.
 */

public class DataCsv {

    String phone;
    String message;

    public DataCsv(String phone, String message) {
        this.phone = phone;
        this.message = message;
    }

    public String getPhone() {
        return phone;
    }

    public String getMessage() {
        return message;
    }
}
