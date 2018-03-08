package c16fld.cs.umu.se.trackme;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Frans-Lukas on 2018-03-05.
 */

@Database(entities = {NodeEntity.class}, version = 2)
public abstract class NodeDB extends RoomDatabase{
    public abstract NodeDao nodeDao();
}
