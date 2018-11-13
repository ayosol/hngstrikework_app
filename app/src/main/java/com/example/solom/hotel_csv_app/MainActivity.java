package com.example.solom.hotel_csv_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.solom.hotel_csv_app.adapter.RecentlyOpenedRvAdapter;
import com.example.solom.hotel_csv_app.utils.PathUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    public static String EXTRAS_CSV_PATH_NAME = "com.example.solom.hotel_csv_app.MainActivity.PathHolder";
    public static String PREFS_CSV_PATH_NAMES = "com.example.solom.hotel_csv_app.MainActivity.PathHolder";
    public static String SHARED_PREFERENCE_NAME = "com.example.solom.hotel_csv_app.MainActivity.SharedPrefs";
    public static final int SMS_PERMISSION_CODE = 102;
    private static final String TAG = "PERMISSION";
    private String appFolder;
    private SharedPreferences defaultPrefs;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor prefsEditor;
    private ArrayList<RecentlyOpened> recentFiles;
    private int max_recent_files;
    private static final int CSV_UPLOAD_REQUEST_CODE = 107;
    private Gson gson;
    @BindView(R.id.upload_fab)
    FloatingActionButton readCsvFile;
    @BindView(R.id.upload_fab_2)
    FloatingActionButton readCsvFile_2;

    public static final String EXTRAS_CSV_FILE_NAME = "MainActivity.filePath";
    private boolean showRecentFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        appFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.hotel_csv_app";
        gson = new Gson();
        recentFiles = new ArrayList<>();
        sharedPrefs = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);

        //Reading the show recent files preference from settings
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        showRecentFiles = defaultPrefs.getBoolean(this.getString(R.string.pref_show_recent), false);
        max_recent_files = Integer.parseInt(defaultPrefs.getString(this.getString(R.string.max_recent_files), "3"));

        if (showRecentFiles) {
            readAndDisplayRecentFiles();
        }

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

        //only when recent files are displaying
        readCsvFile_2.setOnClickListener(new View.OnClickListener() {
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

    private void readAndDisplayRecentFiles() {
        //TODO: Check if there are any saved recently opened file
        //TODO: Hide or Display Recently Saved RecyclerView

        String pathsJsonTxt = sharedPrefs.getString(PREFS_CSV_PATH_NAMES, "-1");
        if (!pathsJsonTxt.equalsIgnoreCase("-1")) {
            recentFiles = gson.fromJson(pathsJsonTxt, new TypeToken<ArrayList<RecentlyOpened>>() {
            }.getType());
            if (!recentFiles.isEmpty()) {
                findViewById(R.id.recently_opened_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.main_layout).setVisibility(View.GONE);

                RecyclerView recentFilesRv = findViewById(R.id.recently_opened_rv);
                recentFilesRv.setHasFixedSize(true);
                recentFilesRv.setLayoutManager(new LinearLayoutManager(this));
                RecentlyOpenedRvAdapter adapter = new RecentlyOpenedRvAdapter(recentFiles, this);
                RecentlyOpenedRvAdapter.OnItemClickListener onItemClickListener = new RecentlyOpenedRvAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int adapterPosition) {
                        final Intent readAndDisplayIntent = new Intent(MainActivity.this, ReadAndDisplayActivity.class);
                        String path = recentFiles.get(adapterPosition).getmPath();
                        String csvFileName = path.substring(path.lastIndexOf('/') + 1);
                        readAndDisplayIntent.putExtra(EXTRAS_CSV_PATH_NAME, path);
                        readAndDisplayIntent.putExtra(EXTRAS_CSV_FILE_NAME, csvFileName);
                        startActivity(readAndDisplayIntent);
                    }
                };
                adapter.setOnItemClickListener(onItemClickListener);
                recentFilesRv.setAdapter(adapter);

            } else {
                findViewById(R.id.recently_opened_layout).setVisibility(View.GONE);
                findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
                Toast.makeText(this, "Recent Files is empty", Toast.LENGTH_SHORT).show();
            }
        } else {
            prefsEditor = sharedPrefs.edit();
            prefsEditor.putString(PREFS_CSV_PATH_NAMES, gson.toJson(recentFiles));
            prefsEditor.apply();
        }
    }

    private void saveRecentFiles(String filePath, String fileName) {
        //TODO: Check length of recently saved array
        //TODO: If greater than max_recent_files
        //TODO: Copy file from current path to our own directory
        //TODO: Add path to array

        Date date = new Date();
        @SuppressLint("SimpleDateFormat")
        String fileDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
        @SuppressLint("SimpleDateFormat")
        String filePrefix = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
        String pathToStoreRecent = appFolder + "/" + filePrefix + fileName;
        copyFile(filePath, pathToStoreRecent);
        if (recentFiles.size() == max_recent_files) {
            File file = new File(recentFiles.get(0).getmPath());
            boolean isDeleted = file.delete();
            if (isDeleted) {
                Log.d(EXTRAS_CSV_FILE_NAME, recentFiles.get(0).getmPath() + " is deleted");
            } else {
                Log.d(EXTRAS_CSV_FILE_NAME, recentFiles.get(0).getmPath() + " is NOT deleted");
            }

            recentFiles.remove(0);

            recentFiles.add(new RecentlyOpened(pathToStoreRecent, fileDate));
        } else {
            recentFiles.add(new RecentlyOpened(pathToStoreRecent, fileDate));
        }
        //TODO: Save the array to Shared Preference
        prefsEditor = sharedPrefs.edit();
        prefsEditor.putString(PREFS_CSV_PATH_NAMES, gson.toJson(recentFiles));
        prefsEditor.apply();
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
                        String csvFileName = PathHolder.substring(PathHolder.lastIndexOf('/') + 1);
                        final Intent readAndDisplayIntent = new Intent(MainActivity.this, ReadAndDisplayActivity.class);
                        readAndDisplayIntent.putExtra(EXTRAS_CSV_PATH_NAME, PathHolder);
                        readAndDisplayIntent.putExtra(EXTRAS_CSV_FILE_NAME, csvFileName);

                        if (showRecentFiles) {
                            saveRecentFiles(PathHolder, csvFileName);
                        }
                        startActivity(readAndDisplayIntent);
                    } else {
                        Toast.makeText(getApplicationContext(), "File is not a csv", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void copyFile(String from, String to) {
        File source = new File(from);
        File destination = new File(to);
        try {
            FileUtils.copyFile(source, destination);
        } catch (IOException e) {
            e.printStackTrace();
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
