package c16fld.cs.umu.se.trackme;

import android.Manifest;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by Frans-Lukas on 2018-03-05.
 */

public class GPSNodeService extends Service  {
    private int mSecond = 1000;
    private int mMinute = mSecond * 60;
    private int mHalfHour = mMinute * 30;
    private int mMinDistance = 200;
    private int lastUsedID = 0;

    private boolean databaseIsSetUp = false;

    private LocationManager mLocationManager;
    private LocationListener[] mLocationListeners;
    private NodeDB mDataBase;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate GPSService");
        new DataBaseSetUp().execute();

        mLocationManager = (LocationManager)
                getApplicationContext().
                getSystemService(Context.LOCATION_SERVICE);

        mLocationListeners = new LocationListener[]{
                new MyLocationListener()
        };

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkFineLocationPermission();
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                mHalfHour,
                mMinDistance,
                mLocationListeners[0]);
        return START_NOT_STICKY;
    }



    private void checkFineLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "Does not have permission to track. Stopping.",
                    Toast.LENGTH_SHORT).
                    show();
            stopSelf();
        }
    }

    private class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            if(location != null) {
                NodeEntity node = new NodeEntity(
                        lastUsedID + 1,
                        location.getLatitude(),
                        location.getLongitude(),
                        lastUsedID);

                new InsertIntoDatabase(node).execute();

            } else {
                Toast.makeText(GPSNodeService.this,
                        "Tried storing node but was null.",
                        Toast.LENGTH_LONG).show();
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

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(mLocationListeners[0]);
    }

    private class InsertIntoDatabase extends AsyncTask<Void, Void, Void> {
        private NodeEntity entityToInsert;
        public InsertIntoDatabase(NodeEntity entityToInsert) {
            this.entityToInsert = entityToInsert;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(databaseIsSetUp && mDataBase != null) {
                mDataBase.nodeDao().insertAll(entityToInsert);
            } else{
                Toast.makeText(GPSNodeService.this,
                        "Database not found. Could not insert.",
                        Toast.LENGTH_LONG).show();
            }
            //load nodes from database.
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e(TAG, "onPostExecute InsertIntoDatabase");

            Toast.makeText(GPSNodeService.this,
                    "Stored location @ lat: "
                            + entityToInsert.getLatitude()
                            + ", long: "
                            + entityToInsert.getLongitude(),
                    Toast.LENGTH_LONG).show();
        }
    }


    private class DataBaseSetUp extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            mDataBase = Room.databaseBuilder(
                    getApplicationContext(),
                    NodeDB.class,
                    getString(R.string.database_name))
                    .build();

            updateLastUsedDBID();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            databaseIsSetUp = true;
        }

        private void updateLastUsedDBID() {
            ArrayList<NodeEntity> nodeEntities = (ArrayList<NodeEntity>) mDataBase.nodeDao().getAll();

            boolean foundNewId = false;
            while(!foundNewId){

                foundNewId = true;
                for (NodeEntity nodeEntity : nodeEntities) {
                    if(nodeEntity.getId() == lastUsedID){
                        lastUsedID++;
                        foundNewId = false;
                    }
                }

            }
        }
    }
}
