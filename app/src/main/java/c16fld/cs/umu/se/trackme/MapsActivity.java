package c16fld.cs.umu.se.trackme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import c16fld.cs.umu.se.trackme.Database.NodeDB;
import c16fld.cs.umu.se.trackme.Database.NodeEntity;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    public static final int MIN_DISTANCE_BETWEEN_NODES = 100;
    public static final float NODE_CIRCLE_RADIUS = 4.0f;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mLocationPermissionGranted = false;
    private boolean mDataBaseHasBeenSetUp = false;

    private NodeDB mNodeDatabase;
    private ArrayList<NodeEntity> nodes = null;
    private Location mLastKnownLocation  = null;
    private CameraPosition mCameraPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_maps);

        //set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //load nodes and set up database
        new DataBaseSetUp().execute();

        startLocationService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                goToSettingsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToSettingsMenu() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startLocationService() {
        Intent intent = new Intent(this, GPSNodeService.class);
        startService(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateLocationUI();
        getDeviceLocation();
        setCameraPosition();
    }

    private void setCameraPosition() {
        if(mCameraPosition != null){
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    private void checkFineLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
                    .fallbackToDestructiveMigration()
                    .build();

            //load nodes from database.
            nodes = (ArrayList<NodeEntity>) mNodeDatabase.nodeDao().getAll();


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //draw poly lines on map
            drawTrail();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        drawTrail();
    }

    private void drawTrail() {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        if(nodes != null && nodes.size() > 0 && mMap != null) {
            for (NodeEntity node : nodes) {
                LatLng newNode = new LatLng(node.getLatitude(), node.getLongitude());
                if (latLngs.size() > 0) {
                    //don't add nodes that are close to each other to the poly line.
                    LatLng prevNode = latLngs.get(latLngs.size() - 1);
                    Location prevLocation = new Location(LocationManager.GPS_PROVIDER);
                    prevLocation.setLatitude(prevNode.latitude);
                    prevLocation.setLongitude(prevNode.longitude);

                    Location newLocation = new Location(LocationManager.GPS_PROVIDER);
                    newLocation.setLatitude(newNode.latitude);
                    newLocation.setLongitude(newNode.longitude);

                    if (newLocation.distanceTo(prevLocation) > MIN_DISTANCE_BETWEEN_NODES) {
                        latLngs.add(newNode);
                    }
                } else {
                    latLngs.add(newNode);
                }

            }
            Toast.makeText(this, "Added new line with " + latLngs.size() + " nodes.", Toast.LENGTH_SHORT).show();
            mMap.addPolyline(new PolylineOptions().addAll(latLngs).color(Color.BLUE));

            for (LatLng latLng : latLngs) {
                mMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(NODE_CIRCLE_RADIUS)
                        .fillColor(Color.BLUE)
                        .strokeColor(Color.BLUE)
                        .clickable(true));
            }

            mMap.setOnCircleClickListener(new MyOnCircleClickListener());
        }
    }

    private class MyOnCircleClickListener implements GoogleMap.OnCircleClickListener{
        @Override
        public void onCircleClick(Circle circle) {
            boolean foundMatch = false;
            for (NodeEntity node : nodes) {
                if(circle.getCenter().latitude == node.getLatitude() &&
                        circle.getCenter().longitude == node.getLongitude()){
                    Toast.makeText(MapsActivity.this, "Clicked on node with lat: " + node.getLatitude() +
                                            ", long: " + node.getLongitude(), Toast.LENGTH_SHORT).show();
                    foundMatch = true;
                    break;
                }
            }
            if(!foundMatch) {
                Toast.makeText(MapsActivity.this, "Could not find node.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode){
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {

        if(mMap == null){
            return;
        }
        try{
            if(mLocationPermissionGranted){
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                checkFineLocationPermission();
            }
        } catch(SecurityException e){
            Log.e("Exception: %s", e.getMessage());
        }
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if(mDataBaseHasBeenSetUp) {
                    //drawTrail();
                }
                return false;
            }
        });
    }

    private void getDeviceLocation() {
        try{
            if(mLocationPermissionGranted){
                //we already check if we have permission above.
                @SuppressLint("MissingPermission") Task locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            // Set the maps camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()
                                    ), DEFAULT_ZOOM)
                            );
                        } else{
                            Log.e("getDeviceLocation",
                                    "Current location is null. Not moving camera.");
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }

                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

}
