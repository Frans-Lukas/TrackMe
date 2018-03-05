package c16fld.cs.umu.se.trackme;

import android.location.Location;

/**
 * Created by Frans-Lukas on 2018-03-05.
 */

public class LocatioNode {
    private Location location;

    public LocatioNode(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}

