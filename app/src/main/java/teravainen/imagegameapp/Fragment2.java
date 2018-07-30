package teravainen.imagegameapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.arch.persistence.room.Room;
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

import java.util.List;

public class Fragment2 extends android.support.v4.app.Fragment  {

    private Button editDBButton;
    private Button resetMissionButton;
    private Button resetScoreButton;
    private Button showDBButton;

    public static AppDatabase appDatabase;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_two, container, false);
        editDBButton = (Button)view.findViewById(R.id.OpenDBEdit);
        resetMissionButton = (Button)view.findViewById(R.id.resetMissionButton);
        resetScoreButton = (Button)view.findViewById(R.id.resetScoreButton);
        showDBButton = (Button)view.findViewById(R.id.showDatabase);



        editDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //avaa uusi fragmentti fragment_editdb, käyttämällä bottomNavigationista löytyvää setViewPager funktiota
                ((bottomNavigation)getActivity()).setViewPager(3);
            }
        });

        resetMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UtilityFunctions.resetScore(getActivity());
            }
        });

        resetScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetScore();

            }
        });

        showDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDataBase();
            }
        });


        appDatabase = Room.databaseBuilder(getActivity(),AppDatabase.class, "missionDB").allowMainThreadQueries().build();

        return view;
    }

    public void resetScore(){
        //Editoidaan sharedpreferenciin tallennettua counter integeriä, jolla pidetään kirjaa käyttäjän pisteistä.
        // Tällä voidaan palauttaa käyttäjän pisteet takaisin nollaan
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        int score = 0;

        //Laitetaan muuttuja score editorin kautta counterin uudeksi arvoksi
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putInt("counter", score);
        editor.apply();

        Toast.makeText(getContext(), "The score has been reset", Toast.LENGTH_LONG).show();

        //Tässä pitäisi päivittää Fragment1 tulostaulu
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

            info = info+"Id : " + id + "\n" + "Name : " + name + "\n" + "Difficulty : " + diffic + "\n" + "Points : " +  pong + "\n" + "Description: " + desc + "\n\n";
        }

        TextView databaseData = (TextView)getView().findViewById(R.id.DatabaseData);
        databaseData.setText(info);

        //Toggle visibility for the textview containing the data
        if(databaseData.getVisibility() == View.VISIBLE)
            databaseData.setVisibility(View.GONE);
        else
            databaseData.setVisibility(View.VISIBLE);

    }


}
