package c16fld.cs.umu.se.trackme;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

/**
 * Created by Frans-Lukas on 2018-03-05.
 */

public class MapModel {
    private FusedLocationProviderClient mFusedLocationClient;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private ArrayList<NodeEntity> nodes;
    private Activity activityToAskPermission;

    public MapModel(Activity activityToAskPermission) {
        this.activityToAskPermission = activityToAskPermission;
        nodes = new ArrayList<>();
        loadNodes();
    }

    private void loadNodes() {
    }

    public ArrayList<NodeEntity> getNodes() {
        return nodes;
    }
}
