package com.example.solom.hotel_csv_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.solom.hotel_csv_app.adapter.RecentlyOpenedRvAdapter;
import com.example.solom.hotel_csv_app.models.RecentlyOpened;
import com.example.solom.hotel_csv_app.utils.PathUtil;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static String EXTRAS_CSV_PATH_NAME = "com.example.solom.hotel_csv_app.MainActivity.PathHolder";
    public static String PREFS_CSV_PATH_NAMES = "com.example.solom.hotel_csv_app.MainActivity.PathHolder";
    public static String SHARED_PREFERENCE_NAME = "com.example.solom.hotel_csv_app.MainActivity.SharedPrefs";
    private static final String TAG = "PERMISSION";
    private String appFolder;
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
        appFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.example.solom.hotel_csv_app";
        gson = new Gson();
        recentFiles = new ArrayList<>();
        sharedPrefs = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = sharedPrefs.getBoolean("IS_FIRST_LAUNCH", true);
        if (isFirstLaunch)
            showTapTarget(R.id.upload_fab, "Get Started!", "Click this button to upload a .csv file");
        //Reading the show recent files preference from settings
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        showRecentFiles = defaultPrefs.getBoolean(this.getString(R.string.pref_show_recent), false);
        max_recent_files = Integer.parseInt(defaultPrefs.getString(this.getString(R.string.pref_max_recent_files), "5"));
        defaultPrefs.registerOnSharedPreferenceChangeListener(this);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showRecentFiles) readAndDisplayRecentFiles();
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
                final RecentlyOpenedRvAdapter adapter = new RecentlyOpenedRvAdapter(recentFiles, this);
                recentFilesRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
                recentFilesRv.setHasFixedSize(true);
                recentFilesRv.setLayoutManager(new LinearLayoutManager(this));

                RecentlyOpenedRvAdapter.OnItemClickListener onItemClickListener = new RecentlyOpenedRvAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int adapterPosition) {
                        Intent readAndDisplayIntent = new Intent(MainActivity.this, ReadAndDisplayActivity.class);
                        String path = recentFiles.get(adapterPosition).getmPath();
                        String csvFileName = path.substring(path.lastIndexOf('/') + 1);
                        readAndDisplayIntent.putExtra(EXTRAS_CSV_PATH_NAME, path);
                        readAndDisplayIntent.putExtra(EXTRAS_CSV_FILE_NAME, csvFileName);
                        startActivity(readAndDisplayIntent);
                    }
                };
                RecentlyOpenedRvAdapter.OnItemLongClickListener onItemLongClickListener = new RecentlyOpenedRvAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(View view, final int adapterPosition) {
                        String fileName = recentFiles.get(adapterPosition).getmPath().substring(recentFiles.get(adapterPosition).getmPath().lastIndexOf("/") + 1);
                        String txt = "Remove This File?";
                        TextView dialogView = new TextView(MainActivity.this);
                        dialogView.setText(fileName);
                        dialogView.setTextSize(18);
                        dialogView.setPadding(16, 16, 16, 16);
                        dialogView.setGravity(Gravity.CENTER);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(txt).setView(dialogView).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteRecentFile(adapterPosition);
                                adapter.notifyDataSetChanged();
                                if (recentFiles.isEmpty()) {
                                    findViewById(R.id.recently_opened_layout).setVisibility(View.GONE);
                                    findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
                                }
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                };
                adapter.setOnItemClickListener(onItemClickListener);
                adapter.setOnLongClickListener(onItemLongClickListener);
                recentFilesRv.setAdapter(adapter);

            } else {
                findViewById(R.id.recently_opened_layout).setVisibility(View.GONE);
                findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
            }
        } else {
            prefsEditor = sharedPrefs.edit();
            prefsEditor.putString(PREFS_CSV_PATH_NAMES, gson.toJson(recentFiles));
            prefsEditor.apply();
        }
    }

    public boolean deleteRecentFile(int pos) {
        File file = new File(recentFiles.get(pos).getmPath());
        recentFiles.remove(pos);
        prefsEditor = sharedPrefs.edit();
        prefsEditor.putString(PREFS_CSV_PATH_NAMES, gson.toJson(recentFiles));
        prefsEditor.apply();
        return file.delete();
    }

    private void saveRecentFiles(String filePath, String fileName) {
        //TODO: Check length of recently saved array
        //TODO: If greater than max_recent_files
        //TODO: Copy file from current path to our own directory
        //TODO: Add path to array

        Date date = new Date();
        @SuppressLint("SimpleDateFormat")
        String fileDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
        DateFormat df = new SimpleDateFormat("hh:mm a");
        String fileTime = df.format(date);
        @SuppressLint("SimpleDateFormat")
        String filePrefix = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
        String pathToStoreRecent = appFolder + "/" + filePrefix + fileName;
        try {
            copyFile(filePath, pathToStoreRecent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (recentFiles.size() == max_recent_files) {
            boolean isDeleted = deleteRecentFile(0);
            if (isDeleted) {
                Log.d(EXTRAS_CSV_FILE_NAME, recentFiles.get(0).getmPath() + " is deleted");
            } else {
                Log.d(EXTRAS_CSV_FILE_NAME, recentFiles.get(0).getmPath() + " is NOT deleted");
            }

            recentFiles.add(new RecentlyOpened(pathToStoreRecent, fileDate, fileTime));
        } else {
            recentFiles.add(new RecentlyOpened(pathToStoreRecent, fileDate, fileTime));
        }
        //TODO: Save the array to Shared Preference
        prefsEditor = sharedPrefs.edit();
        prefsEditor.putString(PREFS_CSV_PATH_NAMES, gson.toJson(recentFiles));
        prefsEditor.apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(this.getString(R.string.pref_show_recent))) {
            showRecentFiles = sharedPreferences.getBoolean(this.getString(R.string.pref_show_recent), false);
            if (showRecentFiles) readAndDisplayRecentFiles();
        } else if (key.equals(this.getString(R.string.pref_max_recent_files))) {
            max_recent_files = Integer.parseInt(sharedPreferences.getString(this.getString(R.string.pref_max_recent_files), "5"));
        }
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

    private void copyFile(String from, String to) throws IOException {
        File source = new File(from);
        File destination = new File(to);
        //Delete File if it already exist
        for (int i = 0; i < recentFiles.size(); i++) {
            if (FileUtils.contentEquals(source, new File(recentFiles.get(i).getmPath()))) {
                deleteRecentFile(i);
            }
        }

        if (!FileUtils.contentEquals(source, destination)) {
            FileUtils.copyFile(source, destination);
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
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
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


    private void showTapTarget(int id, String title, String description) {
        final SpannableString desc = new SpannableString(description);

        TapTargetView.showFor(this, TapTarget.forView(findViewById(id), title, desc)
                .cancelable(false)
                .drawShadow(true)
                .tintTarget(false)
                .dimColor(android.R.color.black)
                .outerCircleColor(R.color.dialog_blue)
                .targetCircleColor(android.R.color.white)
                .transparentTarget(true), new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                view.dismiss(true);
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, CSV_UPLOAD_REQUEST_CODE)) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent, CSV_UPLOAD_REQUEST_CODE);
                }
            }

            @Override
            public void onOuterCircleClick(TapTargetView view) {
                super.onOuterCircleClick(view);
                Toast.makeText(view.getContext(), "Click the glowing button", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                Log.d("TapTargetViewSample", "You dismissed me :(");
            }
        });

    }


}
