package teravainen.imagegameapp;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class SecondActivity extends AppCompatActivity {

    //database yhteys
    public static AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);


        final Button showDatabaseButton = findViewById(R.id.showDatabase);
        showDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDataBase();
            }
        });


        final Button openDBView = findViewById(R.id.OpenDBEdit);
        openDBView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewDBView();
            }
        });

        final Button showSharedPref = findViewById(R.id.ShowSharedPref);
        showSharedPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showThePref();
            }
        });

        final Button completeMission = findViewById(R.id.completeMissionButton);
        completeMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                completeMission();
            }
        });

        final Button resetScore = findViewById(R.id.resetScore);
        resetScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetScore();
            }
        });

        appDatabase = Room.databaseBuilder(getApplicationContext(),AppDatabase.class, "missionDB").allowMainThreadQueries().build();
       // generateDatabase();
    }



    public void showDataBase(){
        //Displays all the entries in the database
        List<Mission> missions = appDatabase.myDao().getMissions();

        String info = "";

        for(Mission mis : missions){
            int id = mis.getId();
            String name = mis.getName();
            String diffic = mis.getDifficulty();
            int pong = mis.getPoints();
            String desc = mis.getDescription();

            info = info+"\n\n"+ "Id : " + id + "\n" + "Name : " + name + "\n" + "Difficulty : " + diffic + "\n" + "Points : " +  pong + "\n" + "Description: " + desc;
        }
            TextView tv = (TextView)findViewById(R.id.DatabaseData);
            tv.setText(info);

        //Toggle visibility for the textview containing the data
        TextView textView = (TextView) findViewById(R.id.DatabaseData);
        if(textView.getVisibility() == View.VISIBLE)
            textView.setVisibility(View.GONE);
        else
            textView.setVisibility(View.VISIBLE);

    }



    public void openNewDBView(){
        //avataan uusi activity jossa voidaan editoida databasen entryjä
        Intent intent = new Intent(this, editDatabaseActivity.class);
        startActivity(intent);
    }

    public void showThePref(){
        TextView txtView = (TextView)findViewById(R.id.textFromPref);

        // get stuff from sharedpref
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String name = mySharedPref.getString("Mname","");
        String difficulty = mySharedPref.getString("Mdifficulty","");
        int points = mySharedPref.getInt("Mpoints", 0);
        boolean progress = mySharedPref.getBoolean("Mprogress", false);
        String description = mySharedPref.getString("Mdescription", "");


        //jos tehtävä on tekemättä == false
        if (progress == false){
            //käytä parsecoloria niin voi käyttää fiksuja css värejä
            txtView.setTextColor(Color.parseColor("#000000"));
            txtView.setBackgroundColor(Color.parseColor("#E0FFFF"));
        }
        else if(progress == true) {
           // txtView.setTextAppearance(R.style.completeMission);
            txtView.setTextColor(Color.WHITE);
            txtView.setBackgroundColor(Color.GRAY);
        }

        txtView.setText(name + "\n"
                + description + "\n"
                + difficulty + "\n"
                + points + "\n"
                + progress);
    }


    public void completeMission(){
        TextView txtView = (TextView)findViewById(R.id.textFromPref);

        // get stuff from sharedpref
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String name = mySharedPref.getString("Mname","");
        String difficulty = mySharedPref.getString("Mdifficulty","");
        int points = mySharedPref.getInt("Mpoints", 0);
        boolean progress = mySharedPref.getBoolean("Mprogress", false);

        int score = mySharedPref.getInt("counter", 0);

        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putBoolean("Mprogress", true);

        //laitetaan pisteet global variable scoreen
        //annetaan käyttäjälle pisteet
        editor.putInt("counter", (score + points));
        editor.apply();

        //päivitetään current missionin ulkonäky
        showThePref();

        Toast.makeText(this, "Mission completed, awarded " + points + " points", Toast.LENGTH_LONG).show();
    }

    public void resetScore(){
        //Editoidaan sharedpreferenciin tallennettua counter integeriä, jolla pidetään kirjaa käyttäjän pisteistä.
        // Tällä voidaan palauttaa käyttäjän pisteet takaisin nollaan
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int score = 0;

        //Laitetaan muuttuja score editorin kautta counterin uudeksi arvoksi
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putInt("counter", score);
        editor.apply();

        Toast.makeText(this, "The score has been reset", Toast.LENGTH_LONG).show();
    }

}

