package c16fld.cs.umu.se.trackme;

import android.arch.persistence.room.Room;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.List;

import c16fld.cs.umu.se.trackme.Database.NodeDB;
import c16fld.cs.umu.se.trackme.Database.NodeEntity;

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
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

    }

    private class DataBaseSetUp extends AsyncTask<Void, Void, Void> {
        NodeDB mNodeDatabase;

        @Override
        protected Void doInBackground(Void... voids) {
            NodeDB mNodeDatabase = Room.databaseBuilder(
                    getActivity().getApplicationContext(),
                    NodeDB.class,
                    getString(R.string.database_name))
                    .fallbackToDestructiveMigration()
                    .build();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //draw poly lines on map
            List<NodeEntity> allNodes = mNodeDatabase.nodeDao().getAll();
            for (NodeEntity node : allNodes) {
                mNodeDatabase.nodeDao().delete(node);
            }
        }
    }


}
