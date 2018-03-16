package c16fld.cs.umu.se.trackme;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/**
 * Created by Frans-Lukas on 2018-03-11.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);


        //update summary when first starting settings fragment
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        onSharedPreferenceChanged(sharedPrefs, getString(R.string.intervalKey));
        onSharedPreferenceChanged(sharedPrefs, getString(R.string.minDistanceKey));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals(getString(R.string.minDistanceKey))){
            Preference pref = findPreference(s);
            pref.setSummary(sharedPreferences.getString(s, ""));
        } else if(s.equals(getString(R.string.intervalKey))){
            Preference pref = findPreference(s);
            pref.setSummary(sharedPreferences.getString(s, ""));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        //Unregister preferncelistener.
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

    }

}
