package teravainen.imagegameapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MyDao {

    @Insert
    public void addMission(Mission mission);

    @Query("select * from missions")
    public List<Mission> getMissions();

    @Delete
    public void deleteMission(Mission mission);

    @Update
    public void updateMission(Mission mission);
}
