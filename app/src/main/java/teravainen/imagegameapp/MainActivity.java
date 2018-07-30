package teravainen.imagegameapp;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ListView missionListView;
    public static AppDatabase appDatabase;
    private List<tehtava> itemList = new ArrayList<tehtava>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hot to make screen orientation in portrait mode always, needs to be included in all activities where we want it to be locked to portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final TextView missionView = findViewById(R.id.displayMission);
        missionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change mission when clicked
                getMission();
              // loadMission();
            }
        });


        final Button button = findViewById(R.id.debugButton);
        //onclick listener for button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //this is executed when the user presses button with the id.button
                //runs the function to open the second activity
                launchDebug();
            }
        });


        final Button camerabutton = findViewById(R.id.cameraButton);
        //onclick listener for camerabutton
        camerabutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //open the camera API when button is clicked
                launchProtoPicture();
            }
        });

        final Button jsonButton = findViewById(R.id.JsonTest);
        jsonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchJson();
            }
        });

        final Button navTestButton = findViewById(R.id.startBottomTest);
        navTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchNavTest();
            }
        });

        //aina kun appi laitetaan päälle, ladataan score updateInfo:lla joka ottaa argumentiksi numeron, jolla nostaa scorea
        updateInfo(0);
        //ladataan current mission textviewiin
        loadMission();

        appDatabase = Room.databaseBuilder(getApplicationContext(),AppDatabase.class, "missionDB").allowMainThreadQueries().build();
    }

    @Override
    protected void onResume(){
        //aina kun palataan aktivityyn, päivitetään käyttäjän pisteet updateInfo funktion avulla
        super.onResume();
        updateInfo(0);
        loadMission();
    }

    //opens the second activity
    private void launchDebug(){
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void launchProtoPicture(){
        Intent intent = new Intent(this, ThirdActivity.class);
        startActivity(intent);
    }

    private void launchJson(){
        Intent intent = new Intent(this, JsonActivity.class);
        startActivity(intent);
    }

    private void launchNavTest(){
        Intent intent = new Intent(this, bottomNavigation.class);
        startActivity(intent);
    }

    public void updateInfo(int points){

        //Siirretään tämä onCreateen ja otetaan pois klikkailu

        //Get apps shared preferences
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //value for the score
        int score = mySharedPref.getInt("counter", 0);

        //update textview
        TextView textView = (TextView) findViewById(R.id.scoreView);
        textView.setText("Score: " + score);

        //increment the counter
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putInt("counter", (score + points));
        editor.apply();

        GlobalVariables.score = score;
    }


    public void loadMission(){
        //Haetaan tehtävät käyttäjälle
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        TextView txtView = (TextView)findViewById(R.id.displayMission);

        String name = mySharedPref.getString("Mname","");
        String difficulty = mySharedPref.getString("Mdifficulty","");
        int points = mySharedPref.getInt("Mpoints", 0);
        boolean progress = mySharedPref.getBoolean("Mprogress", false);
        String description = mySharedPref.getString("Mdescription", "");

        if(description.equals("")){
            //vaihda css tausta jos tehtävää ei ole
            //vaihda eri drawable mieluummin?
           // txtView.setBackgroundColor(Color.parseColor("#90FF8C00"));
            txtView.setBackgroundResource(R.drawable.mission_view_hard);

        }
        //then set the content for the mission box

        txtView.setText("Kuvaa: " +name + "\n"
                +"Label name:  "+ description + "\n"
                +"Vaikeus: " + difficulty + "\n"
                +"Pisteet: " + points + "\n"
                +"Suoritettu: " +progress);

    }

    public void getMission(){
        //Displays all the entries in the database
        List<Mission> missions = appDatabase.myDao().getMissions();

        String info = "";

        //jokaiselle missionille tietokannassa
        for(Mission mis : missions){
            //tehdään jokaisesta uusi objekti?
            //valitaan yksi objekti satunnaisesti
            int id = mis.getId();
            String name = mis.getName();
            String diffic = mis.getDifficulty();
            int pong = mis.getPoints();
            String desc = mis.getDescription();

            //tässä voidaan luoda uusi objekti käyttäen edellä olevia muuttujia
            tehtava uusiTehtava = new tehtava(name, diffic, pong, desc);
            itemList.add(uusiTehtava);
            //info = info+"\n\n"+ "Id : " + id + "\n" + "Name : " + name + "\n" + "Difficulty : " + diffic + "\n" + "Points : " +  pong;
        }
        //itemList sisältää jokaista tietokannan tehtävää kohden yhden objektin, voidaan valita mielivaltainen objekti randomilla ja luoda siitä tehtävä
        // suoritetaan get(X), X itemin numero listassa ja siihen getit eri muuttujiin Mname, Mdifficulty, Mpoints ja Mprogress

        Random generator = new Random();
        int index = generator.nextInt(itemList.size());


        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String currentName = mySharedPref.getString("Mname","");
        //check if the current item is the same as the current mission and change it. Create a fallback if there is only one item in the list
        if(itemList.get(index).Mname.equals(currentName) && itemList.size() > 0){
            getMission();
        }
        else{
            info =  "Kuvaa: " + itemList.get(index).Mname + "\n"
                    + "Label name: "+ itemList.get(index).Mdescription + "\n"
                    + "Vaikeus: " + itemList.get(index).Mdifficulty + "\n"
                    + "Pisteet: " + itemList.get(index).Mpoints + "\n"
                    + "Suoritettu: "+ itemList.get(index).Mprogress;

            //tallennetaan nykyinen teravainen.imagegameapp.Tehtava, jotta sitä voidaan käyttää muissa osissa ohjelmaa ja se voidaan suorittaa
            // SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = mySharedPref.edit();
            editor.putString("Mname", itemList.get(index).Mname);
            editor.putString("Mdifficulty", itemList.get(index).Mdifficulty);
            editor.putInt("Mpoints", itemList.get(index).Mpoints);
            editor.putBoolean("Mprogress", itemList.get(index).Mprogress);
            editor.putString("Mdescription", itemList.get(index).Mdescription);
            editor.apply();

            //Näytetään textview:ssa satunnainen missio listasta
            TextView tv = (TextView)findViewById(R.id.displayMission);
            tv.setText(info);
        }

    }

    public class tehtava{
        public boolean Mprogress = false;
        public String Mname;
        public String Mdifficulty;
        public int Mpoints;
        public String Mdescription;

        //constructor
        public tehtava(String nimi, String vaikeus, int pisteet, String kuvaus){
            Mprogress = false;
            Mname = nimi;
            Mdifficulty = vaikeus;
            Mpoints = pisteet;
            Mdescription = kuvaus;
        }

    }
}

