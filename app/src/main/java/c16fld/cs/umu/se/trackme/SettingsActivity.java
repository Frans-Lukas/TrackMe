package c16fld.cs.umu.se.trackme;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

public class SettingsActivity extends Activity {

    public static final String KEY_SHOULD_TRACK_PREFERENCE = "switch_preference_track_me";
    public static final String KEY_MIN_DISTANCE_PREFERENCE = "edit_text_preference_node_distance";
    public static final String KEY_INTERVAL_PREFERENCE = "edit_text_preference_location_time";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
