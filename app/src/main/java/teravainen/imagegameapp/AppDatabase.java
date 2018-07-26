package teravainen.imagegameapp;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


@Database(entities = {Mission.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase
{

    public abstract MyDao myDao();

}


