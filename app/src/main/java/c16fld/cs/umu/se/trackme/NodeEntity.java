package c16fld.cs.umu.se.trackme;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Frans-Lukas on 2018-03-05.
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

    public NodeEntity(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public NodeEntity() {
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
}
