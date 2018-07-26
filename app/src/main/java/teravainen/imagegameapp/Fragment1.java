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

public class Fragment1 extends android.support.v4.app.Fragment {

    private Button cameraButton;
    private TextView scoreView;
    private TextView missionView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_one, container, false);
        cameraButton = (Button)view.findViewById(R.id.cameraButton);
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
        //Päivitetään score sharefpreferenseistä
        updateText(view);
        loadMission(view);

        return view;
    }

    public void updateText(View v){
        scoreView = (TextView)v.findViewById(R.id.scoreView);

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

}
