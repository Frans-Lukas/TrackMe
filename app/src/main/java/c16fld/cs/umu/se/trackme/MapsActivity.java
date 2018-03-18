package c16fld.cs.umu.se.trackme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import c16fld.cs.umu.se.trackme.Database.NodeDB;
import c16fld.cs.umu.se.trackme.Database.NodeEntity;

public class MapsActivity extends AppCompatActivity
                            implements OnMapReadyCallback{

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final float NODE_CIRCLE_RADIUS = 6.0f;
    private static final int mSecond = 1000;
    private static final int mMinute = mSecond * 60;

    public static final String ADDRESS_KEY = "address";
    public static final String TIME_KEY = "time";
    public static int DEFAULT_TRACK_INTERVAL = 5;
    public static int DEFAULT_NODE_DISTANCE = 100;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;

    private boolean mLocationPermissionGranted = false;
    private boolean mDataBaseHasBeenSetUp = false;

    private boolean mShouldTrackUser = false;
    private int mTrackInterval = 0;
    private int mNodeDistance = 100;

    private NodeDB mNodeDatabase;
    private ArrayList<NodeEntity> nodes = null;
    private Location mLastKnownLocation  = null;
    private CameraPosition mCameraPosition;

    private SharedPreferences mSharedPref;

    private ArrayList<Polyline> polylines;
    private ArrayList<Circle> circles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //restore lastKnownLocation and cameraPostion if restart of app happens.
        if(savedInstanceState != null){
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_maps);

        //set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        circles = new ArrayList<>();
        polylines = new ArrayList<>();

        loadPreferences();

        //Set up location client.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //load nodes and set up database
        new DataBaseSetUp().execute();

        checkFineLocationPermission();
        //If user has set to not be tracked, don't start service.
        if(mShouldTrackUser && mLocationPermissionGranted) {
            startLocationService();
        }
    }

    /**
     * Load preferences.
     */
    private void loadPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //Get preference object loader
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //Load settings.
        mShouldTrackUser = mSharedPref.getBoolean(
                getString(R.string.trackMeKey),
                false);
        Log.d("loadPreferences", "mShouldTrackUser: " + mShouldTrackUser);

        //Try loading track interval from settings
        try {
            mTrackInterval = Integer.parseInt(mSharedPref.getString(
                    getString(R.string.intervalKey),
                    Integer.toString(mTrackInterval))) * mMinute;
            Log.d("loadPreferences", "mTrackInterval: " + mTrackInterval);
        } catch (ClassCastException e){
            //Settings somehow set to non numeric value. Use defaults.
            mTrackInterval = DEFAULT_TRACK_INTERVAL * mMinute;
            Log.e("loadPreferences","Failed to load tracking interval. " + e);
        }

        //try loading node distance from settings.
        try{
            mNodeDistance = Integer.parseInt(mSharedPref.getString(
                    getString(R.string.minDistanceKey),
                    Integer.toString(mNodeDistance)));
            Log.d("loadPreferences", "Success loading pref mNode distance is: " + mNodeDistance);
        } catch (ClassCastException e){
            //Settings somehow set to non numeric value. Use defaults.
            mNodeDistance = DEFAULT_NODE_DISTANCE;
            Log.e("loadPreferences","Failed to load node distance. " + e);
        }
    }

    /**
     * Set up the menu bar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    /**
     * React to user selection of menu bar item.
     * @param item
     * @return
     */
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

    /**
     * Go to the settings menu.
     */
    private void goToSettingsMenu() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Start the location service that will track the user.
     */
    private void startLocationService() {
        Intent intent = new Intent(this, GPSNodeService.class);
        intent.putExtra(getString(R.string.intervalKey), mTrackInterval);
        intent.putExtra(getString(R.string.minDistanceKey), mNodeDistance);
        startService(intent);
    }

    /**
     * Set up map ui.
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(mLocationPermissionGranted) {
            updateLocationUI();
            getDeviceLocation();
            setMapsCameraPosition();

        }
    }

    /**
     * Set the camera position to the last known location or node.
     */
    private void setMapsCameraPosition() {
        if(mMap != null) {
            if (mCameraPosition != null) {
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            } else if (mDataBaseHasBeenSetUp
                    && nodes != null
                    && nodes.size() > 0) {
                //Set location to last recorded database location.
                LatLng location = new LatLng(
                        nodes.get(nodes.size() - 1).getLatitude(),
                        nodes.get(nodes.size() - 1).getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
            }
        }
    }

    /**
     * Save camera position and last known location.
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Check and ask for fine location access.
     */
    private void checkFineLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            mLocationPermissionGranted = true;
        }
    }

    /**
     * Set up database on seperate thread.
     */
    private class DataBaseSetUp extends AsyncTask<Void, Void, Void>{
        public DataBaseSetUp() {
            super();
        }

        /**
         * Set up database, load and sort nodes.
         * @param voids
         * @return
         */
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
            sortNodes(nodes);
            return null;
        }

        /**
         * Draw trail and tell set database to being set up.
         * @param aVoid
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //draw poly lines on map
            drawTrail();
            mDataBaseHasBeenSetUp = true;
            setMapsCameraPosition();
        }
    }

    /**
     * Load nodes from database.
     */
    private class LoadNodes extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            nodes = (ArrayList<NodeEntity>) mNodeDatabase.nodeDao().getAll();
            sortNodes(nodes);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            drawTrail();
        }
    }

    /**
     * Sort nodes based on date in a specific format. If incorrect format, order
     * will be according to database ordering.
     * @param nodes
     */
    private void sortNodes(ArrayList<NodeEntity> nodes) {
        Collections.sort(nodes, new Comparator<NodeEntity>() {
            @Override
            public int compare(NodeEntity entity1, NodeEntity entity2) {
                //Need to keep a standard date format to allow parsing.
                @SuppressLint("SimpleDateFormat") DateFormat sdf =
                        new SimpleDateFormat(getString(R.string.dateFormat));
                try {
                    Date d1 = sdf.parse(entity1.getTime());
                    Date d2 = sdf.parse(entity2.getTime());
                    return d1.compareTo(d2);
                } catch (ParseException e) {
                    Log.e("sortNodes", "Invalid date format.");
                }
                return 1;
            }
        });
    }

    /**
     * Load preferences and set position.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadPreferences();
        new LoadNodes().execute();
    }

    /**
     * Draw trail on map according to distance between nodes.
     * As well as circles around nodes.
     */
    private void drawTrail() {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        if(nodes != null && nodes.size() > 0 && mMap != null) {
            //Only record nodes within a specific distance between each other.

            getNodesToDraw(latLngs);

            polylines.add(mMap.addPolyline(new PolylineOptions().addAll(latLngs).color(Color.BLUE)));

            drawCircles(latLngs);

            //Add click listeners.
            mMap.setOnCircleClickListener(new MyOnCircleClickListener());
            mMap.setOnMapLongClickListener(new MyCircleRemovalListener());
        }
    }

    /**
     * Finds all nodes that are a specific distance between each other and adds them to the
     * latLngs list.
     * @param latLngs The nodes to check if they are the distance between each other,
     */
    private void getNodesToDraw(ArrayList<LatLng> latLngs) {
        for (NodeEntity node : nodes) {
            LatLng newNode = new LatLng(node.getLatitude(), node.getLongitude());
            if (latLngs.size() > 0) {
                //don't add nodes that are close to each other.
                LatLng prevNode = latLngs.get(latLngs.size() - 1);

                //Create location objects for easier distance checking.
                Location prevLocation = new Location(LocationManager.GPS_PROVIDER);
                prevLocation.setLatitude(prevNode.latitude);
                prevLocation.setLongitude(prevNode.longitude);

                Location newLocation = new Location(LocationManager.GPS_PROVIDER);
                newLocation.setLatitude(newNode.latitude);
                newLocation.setLongitude(newNode.longitude);

                if (newLocation.distanceTo(prevLocation) > mNodeDistance) {
                    latLngs.add(newNode);
                }
            } else {
                latLngs.add(newNode);
            }
        }
    }

    /**
     * Draw circles on locations recorded.
     * @param latLngs
     */
    private void drawCircles(ArrayList<LatLng> latLngs) {
        if(latLngs.size() > 0) {
            for (LatLng latLng : latLngs) {
                circles.add(mMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(NODE_CIRCLE_RADIUS)
                        .fillColor(Color.BLUE)
                        .strokeColor(Color.BLUE)
                        .clickable(true)));

            }
        }
    }

    /**
     * clear all circles and lines drawn on map.
     */
    private void clearLinesAndCircles(){
        for (Polyline polyline : polylines) {
            polyline.remove();
        }
        polylines = new ArrayList<>();
        for (Circle circle : circles) {
            circle.remove();
        }
        circles = new ArrayList<>();
    }


    /**
     * A long click listener for the circles on the map. This listener
     * will remove all circles long clicked on. Both from the map and the database.
     */
    private class MyCircleRemovalListener implements GoogleMap.OnMapLongClickListener{

        @Override
        public void onMapLongClick(LatLng latLng) {
            //Find the node the user clicked on, if it exists.
            for (NodeEntity node : nodes) {
                float[] distance = new float[2];
                Location.distanceBetween(latLng.latitude, latLng.longitude, node.getLatitude(), node.getLongitude(), distance);
                //Remove the node if the user clicks on it.
                if(distance[0] < NODE_CIRCLE_RADIUS){
                    new DeleteFromDatabase(node).execute();
                    nodes.remove(node);
                    clearLinesAndCircles();
                    new LoadNodes().execute();

                    //Tell the user the node was removed.
                    Toast.makeText(MapsActivity.this, "Removed node", Toast.LENGTH_SHORT).show();

                    break;
                }
            }
        }
    }

    /**
     * A circle listener for opening information about the clicked node.
     */
    private class MyOnCircleClickListener implements GoogleMap.OnCircleClickListener{
        @Override
        public void onCircleClick(Circle circle) {
            boolean foundMatch = false;
            //Find the circle the user clicked on.
            for (NodeEntity node : nodes) {
                if(circle.getCenter().latitude == node.getLatitude() &&
                        circle.getCenter().longitude == node.getLongitude()){
                    Intent intent = new Intent(MapsActivity.this, LocationInformationActivity.class);
                    intent.putExtra(ADDRESS_KEY, node.getAddress());
                    intent.putExtra(TIME_KEY, node.getTime());
                    startActivity(intent);
                    //For debugging if the node was not found.
                    foundMatch = true;
                    break;
                }
            }
        }
    }

    /**
     * When the user gives permission to the app to track location, update the ui to the
     * current location.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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
        //Update ui according to what the result  is.
        updateLocationUI();
    }

    /**
     * Update the location ui.
     */
    private void updateLocationUI() {
        if(mMap == null){
            return;
        }
        try{
            //Enable or disable location button.
            if(mLocationPermissionGranted){
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch(SecurityException e){
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * get the last known location.
     */
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

    /**
     * Delete the given node from the database.
     */
    private class DeleteFromDatabase extends AsyncTask<Void, Void, Void> {
        private NodeEntity entityToDelete;
        public DeleteFromDatabase(NodeEntity entityToInsert) {
            this.entityToDelete = entityToInsert;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(mDataBaseHasBeenSetUp) {
                mNodeDatabase.nodeDao().delete(entityToDelete);
            }
            return null;
        }
    }

}
