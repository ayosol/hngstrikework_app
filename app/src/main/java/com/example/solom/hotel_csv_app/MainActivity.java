package com.example.solom.hotel_csv_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
//<<<<<<< wisdom
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
=======
import android.widget.ListView;

import java.io.InputStream;
import java.util.List;
//>>>>>>> master

public class MainActivity extends AppCompatActivity {

    private ItemArrayAdapter itemArrayAdapter;

    private static final int SMS_PERMISSION_CODE = 102;
    private static final String TAG ="PERMISSION" ;
  @BindView(R.id.test)
    TextView test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this,ResponseActivity.class);
                startActivity(intent);
            }
        });

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
