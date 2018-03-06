package c16fld.cs.umu.se.trackme;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by Frans-Lukas on 2018-03-05.
 */

public class GPSNodeService extends Service {
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private boolean hasPermissionToTrack = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private int lastUsedID = 0;

    private NodeDB mDataBase;

    @Override
    public void onCreate() {
        super.onCreate();
        mDataBase = Room.databaseBuilder(
                getApplicationContext(),
                NodeDB.class,
                "Node-database")
                .build();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public void getLocation(){
    }

    class MySuccessListener implements OnSuccessListener<Location>{


        @Override
        public void onSuccess(Location location) {
            NodeEntity node = new NodeEntity(
                    lastUsedID + 1,
                    location.getLatitude(),
                    location.getLongitude(),
                    lastUsedID);
            mDataBase.nodeDao().insertAll(node);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!hasPermissionToTrack){
            Toast.makeText(this,
                    "Does not have permission to track. Stopping.",
                    Toast.LENGTH_SHORT).
                    show();

            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);

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
    }
}
