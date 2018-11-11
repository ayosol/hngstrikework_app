package com.example.solom.hotel_csv_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    String maxRecentKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extra = getIntent().getExtras();
        addPreferencesFromResource(R.xml.pref_general);
//        getActionBar().setDisplayHomeAsUpEnabled(true);

        maxRecentKey = SettingsActivity.this.getString(R.string.pref_max_recent_files);
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();

        for (int i = 0; i < count; i++) {
            Preference pref = prefScreen.getPreference(i);
            if (!(pref instanceof SwitchPreference) && !(pref instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(pref.getKey(), "");
                setPreferenceSummary(pref, value);
            }
        }
    }

    private void setPreferenceSummary(Preference pref, String value) {
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            int prefIndex = listPref.findIndexOfValue(value);
            if (prefIndex >= 0) {
                listPref.setSummary(listPref.getEntries()[prefIndex]);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (null != pref) {
            if (!(pref instanceof SwitchPreference) && !(pref instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(pref.getKey(), "");
                setPreferenceSummary(pref, value);
            }
            if (key.equals(maxRecentKey)) {

                if (sharedPreferences.getBoolean(key, false)) {
                    findPreference(maxRecentKey).setEnabled(true);
                    Toast.makeText(SettingsActivity.this, "enabled", Toast.LENGTH_SHORT).show();
                } else {
                    findPreference(maxRecentKey).setEnabled(false);
                    Toast.makeText(SettingsActivity.this, "disabled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
