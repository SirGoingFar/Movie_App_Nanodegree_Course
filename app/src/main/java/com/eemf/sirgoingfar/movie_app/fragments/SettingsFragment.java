package com.eemf.sirgoingfar.movie_app.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.eemf.sirgoingfar.movie_app.R;


public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.pref_settings);

        //set the default preference summary
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = preferenceScreen.getSharedPreferences();
        int prefCount = preferenceScreen.getPreferenceCount();

        for (int i = 0; i < prefCount; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            String prefValue = sharedPreferences.getString(preference.getKey(), "");

            if (!(preference instanceof CheckBoxPreference))
                setPreferenceSummary(preference, prefValue);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);

        //exclude CheckBoxPreference - it returns a boolean value and it's summary is set using
        // its summaryOn and summaryOff attributes
        //exclude EditTextPrefence - it is not needed here
        if (preference instanceof ListPreference)
            setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
    }

    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefValueIndex = listPreference.findIndexOfValue(value);

            if (prefValueIndex >= 0)
                listPreference.setSummary(listPreference.getEntries()[prefValueIndex]);
        }
    }
}
