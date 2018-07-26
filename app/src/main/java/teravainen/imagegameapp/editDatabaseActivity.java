package teravainen.imagegameapp;

import android.arch.persistence.room.Room;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class editDatabaseActivity extends AppCompatActivity {

    public static AppDatabase appDatabase;
    private List<tehtava> itemList = new ArrayList<tehtava>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_database);

        final Button createButton = findViewById(R.id.submitEntryButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDBEntry();
            }
        });

        final Button deleteButton = findViewById(R.id.deleteEntryButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDBEntry();
            }
        });

        final Button editButton = findViewById(R.id.editEntryButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDBEntry();
            }
        });

        final Button newMissionButton = findViewById(R.id.getMission);
        newMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMission();
            }
        });

        final Button toggleSubmitButton = findViewById(R.id.showAddLayout);
        toggleSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAddVisibility();
            }
        });

        final Button toggleDeleteButton = findViewById(R.id.showDeleteLayout);
        toggleDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleDeleteVisibility();
            }
        });

        final Button showDatabaseButton = findViewById(R.id.showDatabase);
        showDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatabase();
            }
        });

        final Button toggleEditButton = findViewById(R.id.showEditLayout);
        toggleEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleEditVisibility();
            }
        });


        appDatabase = Room.databaseBuilder(getApplicationContext(),AppDatabase.class, "missionDB").allowMainThreadQueries().build();
    }



    public void createDBEntry(){
        //otetaan valuet uuteen Database entryyn
        //luodaan uusi entry databaseen

        EditText createName = (EditText)findViewById(R.id.Mname);
        EditText createDifficulty = (EditText)findViewById(R.id.Mdifficulty);
        EditText createScore = (EditText)findViewById(R.id.Mpoints);
        EditText createDescription = (EditText)findViewById(R.id.Mdescription);

        String entryName = createName.getText().toString();
        String entryDifficulty = createDifficulty.getText().toString();
        String entryDescription = createDescription.getText().toString();

        //Piste entryllä on pakko olla jokin pistemäärä, muut kentät voivat olla tyhjiä
        if(TextUtils.isEmpty(createScore.getText())){
            Toast.makeText(this, "Give a score!", Toast.LENGTH_LONG).show();
        }
        else{
            int entryPoints = Integer.parseInt(createScore.getText().toString());


            //sama kaava kuin SecondActivity.java:ssa, luodaan uusi Mission objekti, käytetään setName, setDifficulty ja setPoints jolla asetetaan tietokanta entryn tiedot
            Mission mission = new Mission();

            mission.setName(entryName);
            mission.setDifficulty(entryDifficulty);
            mission.setPoints(entryPoints);
            mission.setDescription(entryDescription);
            editDatabaseActivity.appDatabase.myDao().addMission(mission);

            Toast.makeText(this, "Created a new entry: " +entryName, Toast.LENGTH_LONG).show();
        }
    }

    public void deleteDBEntry(){
        //delete database entry based on the id the user gives in the editText
        EditText idToDelete = (EditText)findViewById(R.id.dID);

        //check if a number is given, otherwise tell the user to give one
        if(TextUtils.isEmpty(idToDelete.getText())){
            Toast.makeText(this, "Give an ID!", Toast.LENGTH_LONG).show();
        }
        else{
            //assign the int value from the edittext to an integer, then delete the entry with that number. if the number doesn't exist, doesn't really do anything
            int deleteMe = Integer.parseInt(idToDelete.getText().toString());

            Mission mission = new Mission();
            mission.setId(deleteMe);
            appDatabase.myDao().deleteMission(mission);

            Toast.makeText(this, ("Deleted user with the id : " + deleteMe), Toast.LENGTH_LONG).show();
        }
    }

    public void editDBEntry(){
        EditText thisId = (EditText)findViewById(R.id.editID);
        EditText editName = (EditText)findViewById(R.id.editName);
        EditText editDifficulty = (EditText)findViewById(R.id.editDifficulty);
        EditText editScore = (EditText)findViewById(R.id.editScore);
        EditText editDescription = (EditText)findViewById(R.id.editDescription);

        if(TextUtils.isEmpty(thisId.getText())) {
            Toast.makeText(this, "Give an ID!", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(editScore.getText())){
            Toast.makeText(this, "Give a score!", Toast.LENGTH_LONG).show();
        }
        else{
            int editMe = Integer.parseInt(thisId.getText().toString());
            String name = editName.getText().toString();
            String difficulty = editDifficulty.getText().toString();
            int score = Integer.parseInt(editScore.getText().toString());
            String description = editDescription.getText().toString();

            Mission mission = new Mission();
            mission.setId(editMe);
            mission.setName(name);
            mission.setDifficulty(difficulty);
            mission.setPoints(score);
            mission.setDescription(description);

            appDatabase.myDao().updateMission(mission);
            Toast.makeText(this, "Entry "+ editMe + " Updated", Toast.LENGTH_LONG).show();
        }
    }


    public void toggleAddVisibility(){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.addEntry);
        //Toggle visibility
        if(linearLayout.getVisibility() == View.VISIBLE)
            linearLayout.setVisibility(View.GONE);
        else
            linearLayout.setVisibility(View.VISIBLE);
    }

    public void toggleDeleteVisibility(){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.deleteEntry);
        //Toggle visibility
        if(linearLayout.getVisibility() == View.VISIBLE)
            linearLayout.setVisibility(View.GONE);
        else
            linearLayout.setVisibility(View.VISIBLE);
    }

    public void toggleEditVisibility(){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.editEntry);
        //Toggle visibility
        if(linearLayout.getVisibility() == View.VISIBLE)
            linearLayout.setVisibility(View.GONE);
        else
            linearLayout.setVisibility(View.VISIBLE);
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

        info = itemList.get(index).Mname + "\n" + itemList.get(index).Mdifficulty + "\n" + itemList.get(index).Mpoints + "\n" + itemList.get(index).Mdescription + "\n";

        //tallennetaan nykyinen teravainen.imagegameapp.Tehtava, jotta sitä voidaan käyttää muissa osissa ohjelmaa ja se voidaan suorittaa
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putString("Mname", itemList.get(index).Mname);
        editor.putString("Mdifficulty", itemList.get(index).Mdifficulty);
        editor.putInt("Mpoints", itemList.get(index).Mpoints);
        editor.putBoolean("Mprogress", itemList.get(index).Mprogress);
        editor.putString("Mdescription", itemList.get(index).Mdescription);
        editor.apply();


        //Näytetään textview:ssa satunnainen missio listasta
        TextView tv = (TextView)findViewById(R.id.myDataList);
        tv.setText(info);
    }

    public void showDatabase(){

        List<Mission> missions = appDatabase.myDao().getMissions();

        String info = "";

        for(Mission mis : missions){
            int id = mis.getId();
            String name = mis.getName();
            String diffic = mis.getDifficulty();
            int pong = mis.getPoints();
            String desc = mis.getDescription();

            info = info+"\n\n"+ "Id : " + id + "\n"
                    + "Name : " + name + "\n"
                    + "Difficulty : " + diffic + "\n"
                    + "Points : " +  pong + "\n"
                    + "Description: " + desc;
        }

        TextView textView = (TextView)findViewById(R.id.myDatabaseView);
        textView.setText(info);

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
