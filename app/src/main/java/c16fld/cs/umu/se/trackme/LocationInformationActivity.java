package c16fld.cs.umu.se.trackme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Activity to display information about the given node.
 */
public class LocationInformationActivity extends AppCompatActivity {
    private String time;
    private String address;
    private String timeSpent;

    /**
     * Load node information from intent.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_information);

        //Enable action bar and home button.
        setTitle("Location Info");
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        time = intent.getStringExtra(MapsActivity.TIME_KEY);
        address = intent.getStringExtra(MapsActivity.ADDRESS_KEY);

        setUpTextViews();
    }

    private void setUpTextViews() {
        ((TextView)findViewById(R.id.textViewAddress)).setText(address);
        ((TextView)findViewById(R.id.textViewDateTime)).setText(time);
    }
}
