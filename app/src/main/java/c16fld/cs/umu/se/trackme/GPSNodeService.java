package c16fld.cs.umu.se.trackme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import c16fld.cs.umu.se.trackme.Database.NodeDB;
import c16fld.cs.umu.se.trackme.Database.NodeEntity;

import static android.content.ContentValues.TAG;


/**
 * Tracking service that stores the users location to the databse.
 */
public class GPSNodeService extends Service  {
    private static final int mSecond = 1000;
    private static final int mMinute = mSecond * 60;

    private int mTrackTime = mMinute * MapsActivity.DEFAULT_TRACK_INTERVAL;
    private int mMinDistance = MapsActivity.DEFAULT_NODE_DISTANCE;

    private boolean mDatabaseIsSetUp = false;
    private boolean mUniqueIdFound = false;

    private LocationManager mLocationManager;
    private LocationListener mLocationListeners;
    private NodeDB mDataBase;
    private Geocoder mGeocoder;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate GPSService");
        new DataBaseSetUp().execute();

        //db not guaranteed to be set up when starting service.
        mDatabaseIsSetUp = false;
        mUniqueIdFound = false;

        mLocationManager = (LocationManager)
                getApplicationContext().
                getSystemService(Context.LOCATION_SERVICE);

        mLocationListeners = new MyLocationListener();
        mGeocoder = new Geocoder(this, Locale.getDefault());
    }

    /**
     * Set up location requests and get settings.
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            mTrackTime = intent.getIntExtra(getString(R.string.intervalKey), mTrackTime);
            mMinDistance = intent.getIntExtra(getString(R.string.minDistanceKey), mMinDistance);
        }

        checkFineLocationPermission();
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                mTrackTime,
                mMinDistance,
                mLocationListeners);
        return START_STICKY;
    }

    /**
     * Check if the user has allowed the app to track them.
     */
    private void checkFineLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "Does not have permission to track. Stopping.",
                    Toast.LENGTH_SHORT).
                    show();
            stopSelf();
        }
    }

    /**
     * Listen for locations and add them to the database when found.
     */
    private class MyLocationListener implements LocationListener{

        /**
         * Create the database entity object to store.
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            if(location != null && mUniqueIdFound) {
                //Need to keep a standard date format to allow parsing.
                @SuppressLint("SimpleDateFormat") SimpleDateFormat df =
                        new SimpleDateFormat(getString(R.string.dateFormat));
                NodeEntity node = new NodeEntity(
                        location.getLatitude(),
                        location.getLongitude(),
                        df.format(Calendar.getInstance().getTime()),
                        findAddressFromLocation(location));

                new InsertIntoDatabase(node).execute();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "onStatusChanged: " + s);
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "onProviderEnabled: " + s);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d(TAG, "onProviderDisabled: " + s);
        }
    }

    /**
     * Find the closest address from the given location.
     * @param location the location to find the address from.
     * @return
     */
    private String findAddressFromLocation(Location location) {
        try {
            List<Address> addresses = mGeocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
            if(addresses != null && addresses.size() > 0){
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * When service is destroyed, remove updates from location manager.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(mLocationListeners);
    }

    /**
     * Thread to insert into database.
     */
    private class InsertIntoDatabase extends AsyncTask<Void, Void, Void> {
        private NodeEntity entityToInsert;
        public InsertIntoDatabase(NodeEntity entityToInsert) {
            this.entityToInsert = entityToInsert;
        }

        /**
         * Insert to given node to the database.
         * @param voids
         * @return
         */
        @Override
        protected Void doInBackground(Void... voids) {
            if(mDatabaseIsSetUp && mDataBase != null && mUniqueIdFound) {
                mDataBase.nodeDao().insertAll(entityToInsert);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e(TAG, "onPostExecute InsertIntoDatabase");
        }
    }


    private class DataBaseSetUp extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //set up database.
            mDataBase = Room.databaseBuilder(
                    getApplicationContext(),
                    NodeDB.class,
                    getString(R.string.database_name))
                    .build();

            return null;
        }

        //Database is set up, set booleans to show it.
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mDatabaseIsSetUp = true;
        }
    }
}
