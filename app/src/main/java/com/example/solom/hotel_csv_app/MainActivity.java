package com.example.solom.hotel_csv_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ItemArrayAdapter itemArrayAdapter;

    private static final int SMS_PERMISSION_CODE = 102;
    private static final String TAG ="PERMISSION" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listview);
        itemArrayAdapter = new ItemArrayAdapter(getApplicationContext(), R.layout.single_list_item);

        Parcelable state = listView.onSaveInstanceState();
        listView.setAdapter(itemArrayAdapter);
        listView.onRestoreInstanceState(state);

        InputStream inputStream = getResources().openRawResource(R.raw.stats);
        ReadAndDisplayDataActivity_Java csv = new ReadAndDisplayDataActivity_Java(inputStream);
        List<String[]> infoList = csv.read();

        for(String [] info : infoList){
            itemArrayAdapter.add(info);
        }

        if (!hasSendSmsPermission()){
            requestSendSmsPermission();
        }
    }




    private boolean hasSendSmsPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestSendSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            Log.d(TAG, "shouldShowRequestPermissionRationale(), no permission requested");
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_CODE);
    }

}
