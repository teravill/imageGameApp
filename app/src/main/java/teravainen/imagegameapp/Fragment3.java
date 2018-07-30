package teravainen.imagegameapp;

import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class Fragment3 extends android.support.v4.app.Fragment {

    public static AppDatabase appDatabase;
    private List<tehtava> itemList = new ArrayList<tehtava>();

    Button showCreateButton;
    Button createDBEntryButton;

    Button showDeleteButton;
    Button deleteEntryButton;

    Button showEditButton;
    Button editEntryButton;

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_three, container, false);

        showCreateButton = (Button)view.findViewById(R.id.showCreateButton);
        createDBEntryButton = (Button)view.findViewById(R.id.submitEntryButton);

        showDeleteButton = (Button)view.findViewById(R.id.showDeleteButton);
        deleteEntryButton = (Button)view.findViewById(R.id.deleteEntryButton);

        showEditButton = (Button)view.findViewById(R.id.showEditButton);
        editEntryButton = (Button)view.findViewById(R.id.editEntryButton);


        showCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCreate();
            }
        });

        showDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleDelete();
            }
        });

        showEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleEdit();
            }
        });

        createDBEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDBEntry();
            }
        });

        deleteEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDBEntry();
            }
        });

        editEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDBEntry();
            }
        });


        appDatabase = Room.databaseBuilder(getActivity(),AppDatabase.class, "missionDB").allowMainThreadQueries().build();

        return view;
    }

    public void createDBEntry(){
        //otetaan valuet uuteen Database entryyn
        //luodaan uusi entry databaseen

        EditText createName = (EditText)getView().findViewById(R.id.Mname);
        EditText createDifficulty = (EditText)getView().findViewById(R.id.Mdifficulty);
        EditText createScore = (EditText)getView().findViewById(R.id.Mpoints);
        EditText createDescription = (EditText)getView().findViewById(R.id.Mdescription);

        String entryName = createName.getText().toString();
        String entryDifficulty = createDifficulty.getText().toString();
        String entryDescription = createDescription.getText().toString();

        //Piste entryllä on pakko olla jokin pistemäärä, muut kentät voivat olla tyhjiä
        if(TextUtils.isEmpty(createScore.getText())){
            Toast.makeText(getContext(), "Give a score!", Toast.LENGTH_LONG).show();
        }
        else{
            int entryPoints = Integer.parseInt(createScore.getText().toString());

            //luodaan uusi Mission objekti, käytetään setName, setDifficulty ja setPoints jolla asetetaan tietokanta entryn tiedot
            Mission mission = new Mission();

            mission.setName(entryName);
            mission.setDifficulty(entryDifficulty);
            mission.setPoints(entryPoints);
            mission.setDescription(entryDescription);
            Fragment3.appDatabase.myDao().addMission(mission);

            Toast.makeText(getContext(), "Created a new entry: " +entryName, Toast.LENGTH_LONG).show();
        }
    }

    public void deleteDBEntry(){
        //delete database entry based on the id the user gives in the editText
        EditText idToDelete = (EditText)getView().findViewById(R.id.dID);

        //check if a number is given, otherwise tell the user to give one
        if(TextUtils.isEmpty(idToDelete.getText())){
            Toast.makeText(getContext(), "Give an ID!", Toast.LENGTH_LONG).show();
        }
        else{
            //assign the int value from the edittext to an integer, then delete the entry with that number. if the number doesn't exist, doesn't really do anything
            int deleteMe = Integer.parseInt(idToDelete.getText().toString());

            Mission mission = new Mission();
            mission.setId(deleteMe);
            appDatabase.myDao().deleteMission(mission);

            Toast.makeText(getContext(), ("Deleted user with the id : " + deleteMe), Toast.LENGTH_LONG).show();
        }
    }

    public void editDBEntry(){
        EditText thisId = (EditText)getView().findViewById(R.id.editID);
        EditText editName = (EditText)getView().findViewById(R.id.editName);
        EditText editDifficulty = (EditText)getView().findViewById(R.id.editDifficulty);
        EditText editScore = (EditText)getView().findViewById(R.id.editScore);
        EditText editDescription = (EditText)getView().findViewById(R.id.editDescription);

        if(TextUtils.isEmpty(thisId.getText())) {
            Toast.makeText(getContext(), "Give an ID!", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(editScore.getText())){
            Toast.makeText(getContext(), "Give a score!", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getContext(), "Entry "+ editMe + " Updated", Toast.LENGTH_LONG).show();
        }
    }


    public void toggleCreate(){
        LinearLayout linearLayout = (LinearLayout)getView().findViewById(R.id.addEntry);
        //Toggle visibility
        if(linearLayout.getVisibility() == View.VISIBLE)
            linearLayout.setVisibility(View.GONE);
        else
            linearLayout.setVisibility(View.VISIBLE);
    }
    public void toggleDelete(){
        LinearLayout linearLayout = (LinearLayout)getView().findViewById(R.id.deleteEntry);
        //Toggle visibility
        if(linearLayout.getVisibility() == View.VISIBLE)
            linearLayout.setVisibility(View.GONE);
        else
            linearLayout.setVisibility(View.VISIBLE);
    }
    public void toggleEdit(){
        LinearLayout linearLayout = (LinearLayout)getView().findViewById(R.id.editEntry);
        //Toggle visibility
        if(linearLayout.getVisibility() == View.VISIBLE)
            linearLayout.setVisibility(View.GONE);
        else
            linearLayout.setVisibility(View.VISIBLE);
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
