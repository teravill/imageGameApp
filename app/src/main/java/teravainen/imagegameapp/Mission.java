package teravainen.imagegameapp;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "missions")
public class Mission {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "mission_name")
    private String name;

    @ColumnInfo(name = "mission_difficulty")
    private String difficulty;

    @ColumnInfo(name = "mission_points")
    private int points;

    @ColumnInfo(name = "mission_description")
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

}
