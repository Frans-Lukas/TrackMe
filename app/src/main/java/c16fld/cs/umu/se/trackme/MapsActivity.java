package c16fld.cs.umu.se.trackme;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final int MAP_ZOOM = 15;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private NodeDB mNodeDatabase;
    private ArrayList<NodeEntity> nodes = null;
    private LatLng lastKnownLocation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //load nodes and set up database
        new DataBaseSetUp().execute();

        //find last known location.
        checkFineLocationPermission();
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new MyOnSuccessListener());

        startLocationService();
    }
    private void startLocationService() {
        Intent intent = new Intent(this, GPSNodeService.class);
        startService(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void checkFineLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
        }
    }

    class MyOnSuccessListener implements OnSuccessListener<Location>{
        @Override
        public void onSuccess(Location location) {
            if(location != null) {
                lastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, MAP_ZOOM));
            }
        }
    }

    private class DataBaseSetUp extends AsyncTask<Void, Void, Void>{
        public DataBaseSetUp() {
            super();
        }


        @Override
        protected Void doInBackground(Void... voids) {
            mNodeDatabase = Room.databaseBuilder(
                    getApplicationContext(),
                    NodeDB.class,
                    getString(R.string.database_name))
                    .build();

            //load nodes from database.
            nodes = (ArrayList<NodeEntity>) mNodeDatabase.nodeDao().getAll();
            return null;
        }
    }
}
