package c16fld.cs.umu.se.trackme;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by Frans-Lukas on 2018-03-05.
 */

@Dao
public interface NodeDao {

    @Query("SELECT * FROM nodeentity")
    List<NodeEntity> getAll();

    @Insert
    void insertAll(NodeEntity... entities);

    @Delete
    void delete(NodeEntity entity);
}
