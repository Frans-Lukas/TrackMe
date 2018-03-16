package c16fld.cs.umu.se.trackme.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * The Room persistance library database object.
 */
@Database(entities = {NodeEntity.class}, version = 4)
public abstract class NodeDB extends RoomDatabase{
    public abstract NodeDao nodeDao();
}
