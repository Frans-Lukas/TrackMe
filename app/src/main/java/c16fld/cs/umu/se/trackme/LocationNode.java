package c16fld.cs.umu.se.trackme;

import android.location.Location;

/**
 * Created by Frans-Lukas on 2018-03-05.
 */

public class LocationNode {
    private Location location;
    private LocationNode prevLocation;

    /**
     * Only use this constructor if it is the first node created.
     * @param location
     */
    public LocationNode(Location location) {
        this.location = location;
        prevLocation = this;
    }

    /**
     * Use this constructor for all other nodes created. So that the controller class
     * can draw the nodes linked together.
     * @param location
     * @param prevLocation
     */
    public LocationNode(Location location, LocationNode prevLocation) {
        this.location = location;
        this.prevLocation = prevLocation;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}

