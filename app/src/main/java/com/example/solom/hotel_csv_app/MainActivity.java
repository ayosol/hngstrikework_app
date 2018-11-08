package com.example.solom.hotel_csv_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    public static String EXTRAS_CSV_PATH_NAME="MainActivity.PathHolder";
    private static final int SMS_PERMISSION_CODE = 102;
    private static final String TAG = "PERMISSION";
    private static final int CSV_UPLOAD_REQUEST_CODE = 107;
    @BindView(R.id.readcsvfile)
    Button readCsvFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        readCsvFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, CSV_UPLOAD_REQUEST_CODE);
            }
        });

        if (!hasSendSmsPermission()) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CSV_UPLOAD_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    String PathHolder = data.getData().getPath();
                    Toast.makeText(getApplicationContext(), PathHolder, Toast.LENGTH_LONG).show();
                    Intent readAndDisplayIntent=new Intent(MainActivity.this, ReadAndDisplayActivity.class);
                    readAndDisplayIntent.putExtra(EXTRAS_CSV_PATH_NAME,PathHolder);
                    startActivity(readAndDisplayIntent);
                }
                break;
        }
    }


}
