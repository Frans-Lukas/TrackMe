package c16fld.cs.umu.se.trackme;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Frans-Lukas on 2018-03-05.
 */

public class GPSNodeService extends Service {
    public final static int MINUTE = 1000 * 60;

    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private boolean hasPermissionToTrack = false;

    private Location lastLocation;

    private static final long MIN_DISTANCE_FOR_UPDATE = 10;

    private int TIME_BETWEEN_UPDATES = 1 * MINUTE;

    private LocationManager locationManager;


    

    @Override
    public void onCreate() {
        super.onCreate();
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
