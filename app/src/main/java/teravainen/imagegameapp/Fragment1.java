package teravainen.imagegameapp;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Fragment1 extends android.support.v4.app.Fragment {

    private Button cameraButton;
    private TextView scoreView;
    private TextView missionView;
    private Button rerollButton;

    public static AppDatabase appDatabase;
    private List<tehtava> itemList = new ArrayList<tehtava>();

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_one, container, false);
        cameraButton = (Button)view.findViewById(R.id.cameraButton);
        rerollButton = (Button)view.findViewById(R.id.rerollButton);
        //scoreView = (TextView)view.findViewById(R.id.scoreView);
        //scoreView.setText("Updated Text!");


        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fragmentissa joutuu käyttämään getActivity()
                Intent intent = new Intent(getActivity(), ThirdActivity.class);
                startActivity(intent);
            }
        });

        rerollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tee sama funktio kuin mainActivityssa
                updateText();
            }
        });

        //Päivitetään score sharefpreferenseistä
        updateText();
        loadMission(view);

        return view;
    }

    public void updateText(){
        scoreView = (TextView)view.findViewById(R.id.scoreView);

        //Get apps shared preferences
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //value for the score
        int score = mySharedPref.getInt("counter", 0);
        //update textview
        scoreView.setText("Score: " + score);
    }


    public void loadMission(View v){
        //Haetaan tehtävät käyttäjälle
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        missionView = (TextView)v.findViewById(R.id.displayMission);

        String name = mySharedPref.getString("Mname","");
        String difficulty = mySharedPref.getString("Mdifficulty","");
        int points = mySharedPref.getInt("Mpoints", 0);
        boolean progress = mySharedPref.getBoolean("Mprogress", false);
        String description = mySharedPref.getString("Mdescription", "");

        if(description.equals("")){
            //vaihda css tausta jos tehtävää ei ole
            missionView.setBackgroundResource(R.drawable.mission_view_hard);
        }
        //then set the content for the mission box
        missionView.setText("Kuvaa: " +name + "\n"
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


        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
            TextView tv = (TextView)getView().findViewById(R.id.displayMission);
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
