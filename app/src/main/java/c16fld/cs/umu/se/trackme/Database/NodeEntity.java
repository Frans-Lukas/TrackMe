package c16fld.cs.umu.se.trackme.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Entity used to store node information in a room database.
 */
@Entity
public class NodeEntity {
    @PrimaryKey(autoGenerate =  true)
    private int id;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "prev_node_id")
    private int prevID;

    @ColumnInfo(name = "time")
    private String time;

    @ColumnInfo(name="address")
    private String address;

    public NodeEntity(double latitude, double longitude, String time, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getPrevID() {
        return prevID;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setPrevID(int prevID) {
        this.prevID = prevID;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
