package c16fld.cs.umu.se.trackme;

import android.app.Activity;
import android.arch.persistence.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;

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
