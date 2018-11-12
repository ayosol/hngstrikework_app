package com.example.solom.hotel_csv_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//<<<<<<< ui-based-changes
//======
import android.widget.Button;
import android.widget.ImageButton;
//>>>>>>> master
import android.widget.Toast;

import com.example.solom.hotel_csv_app.utils.PathUtil;

import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    public static String EXTRAS_CSV_PATH_NAME = "MainActivity.PathHolder";
    public static final int SMS_PERMISSION_CODE = 102;
    private static final String TAG = "PERMISSION";
    private static final int CSV_UPLOAD_REQUEST_CODE = 107;
//<<<<<<< ui-based-changes
    @BindView(R.id.upload_fab)
    FloatingActionButton readCsvFile;

    public static final String EXTRAS_CSV_FILE_NAME = "MainActivity.filePath";
//=======
    @BindView(R.id.readcsvfile)
    ImageButton readCsvFile;
    public static final String EXTRAS_CSV_FILE_NAME="MainActivity.filePath";
//>>>>>>> master

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        readCsvFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, CSV_UPLOAD_REQUEST_CODE)) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent, CSV_UPLOAD_REQUEST_CODE);
                }
            }
        });
        // checkPermission(Manifest.permission.SEND_SMS, SMS_PERMISSION_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CSV_UPLOAD_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri pathUri = data.getData();
                    assert pathUri != null;
                    if (pathUri.toString().toLowerCase().contains(".csv")) {
                        String PathHolder = null;
                        try {
                            PathHolder = PathUtil.getPath(MainActivity.this, data.getData());
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        assert PathHolder != null;
                        String csvFileNname = PathHolder.substring(PathHolder.lastIndexOf('/') + 1);
                        Toast.makeText(getApplicationContext(), csvFileNname, Toast.LENGTH_LONG).show();
                        final Intent readAndDisplayIntent = new Intent(MainActivity.this, ReadAndDisplayActivity.class);
                        readAndDisplayIntent.putExtra(EXTRAS_CSV_PATH_NAME, PathHolder);
                        readAndDisplayIntent.putExtra(EXTRAS_CSV_FILE_NAME, csvFileNname);
                        startActivity(readAndDisplayIntent);

                    } else {
                        Toast.makeText(getApplicationContext(), "File is not a csv", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission(String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    permission) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
                return false;
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            if (requestCode == CSV_UPLOAD_REQUEST_CODE) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, CSV_UPLOAD_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_schedule_task:
                break;
            case R.id.menu_stat:
                break;
            case R.id.menu_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.menu_help:
                break;
            case R.id.menu_exit:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
