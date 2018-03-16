package c16fld.cs.umu.se.trackme.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Database access object that can insert, delete and get data from the database.
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
