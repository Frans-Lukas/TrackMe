package c16fld.cs.umu.se.trackme;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Frans-Lukas on 2018-03-05.
 */

@Entity
public class NodeEntity {
    @PrimaryKey
    private int id;

    @ColumnInfo(name = "latitude")
    private String latitude;

    @ColumnInfo(name = "longitude")
    private String longitude;

    @ColumnInfo(name = "prev_node_id")
    private int prevID;

    public int getId() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public int getPrevID() {
        return prevID;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setPrevID(int prevID) {
        this.prevID = prevID;
    }
}
