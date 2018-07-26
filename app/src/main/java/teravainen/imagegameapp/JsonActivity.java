package teravainen.imagegameapp;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class JsonActivity extends AppCompatActivity {

    Button start;
    TextView textview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json);

        start = (Button) findViewById(R.id.startBtn);
        textview = (TextView) findViewById(R.id.contentTextview);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myJson();
            }
        });

    }


    void myJson(){
        //toinen json tapa
        //Avataan inputsreamiin raw resursseista oma json file
        //tehdään bufferi johon luetaan json file ja tallennetaan sisältö stringiin
        //Ensimmmäisessä for loopissa käydään kaikki entryt läpi, tässä tapauksessa on ainoastaan labelAnnotations
        //toisessa for loopissa sen sisällä käydään labelAnnotationin tiettyjä entryjä
        textview = (TextView) findViewById(R.id.contentTextview);

        try{
            InputStream is = getResources().openRawResource(R.raw.studenttest);
            byte[] buffer = new byte[is.available()];
            while(is.read(buffer) != -1);
            String jsontext = new String(buffer);
            JSONObject entries = new JSONObject(jsontext);


            int i;
            for (i=0;i<entries.length();i++){
               JSONArray post = entries.getJSONArray("labelAnnotations");
              String jsonMessage = post.toString();

              String midi = "";
              //post.getJSONObject(0).getString("mid");
                for(int u = 0; u < post.length(); u++){
                    midi += post.getJSONObject(u).getString("description") + "\n";

                    //check if some specific word appears, do it with a new function
                    String checkValue = post.getJSONObject(u).getString("description");
                    missionCheck(checkValue);

                }
                 textview.setText(midi);
                //voisi tallentaa listan sharedpreferensseihin, jotta sitä voi käyttää muualla ohjelmassa tarvitsematta aina käyttää tätä funktiota
                SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = mySharedPref.edit();
                editor.putString("descriptionList", midi);
                editor.apply();
                }
            }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public void missionCheck(String testValue){

        //comparisonValuen arvot voisi saada sharedpreferensin kautta, jolloin ne olisivat aina vain tietyt. Normaalisti arvoja ei ole hirveän montaa, mutta ne voivat muuttua
        //jos löytyy, niin sharedpreferenssistä voi poistaa valuen ja käyttäjälle pitää antaa pisteet


        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String comparisonValue = mySharedPref.getString("Mdescription","");
        int awardPoints = mySharedPref.getInt("Mpoints", 0);
        boolean completionStatus = mySharedPref.getBoolean("Mprogress", false);


            if(testValue.equals(comparisonValue) && completionStatus == false ){
                //check mission as complete and awards user with points if the given testvalue is the same as comparisonvalue and the mission is not completed
                //change the mission status to completed (true) and award the points to user
                SharedPreferences.Editor editor = mySharedPref.edit();
                int score = mySharedPref.getInt("counter", 0);
                editor.putInt("counter", score + awardPoints);
                editor.putBoolean("Mprogress", true);
                editor.apply();

                Toast.makeText(getApplicationContext(), "Awarded with " + awardPoints + " points", Toast.LENGTH_LONG).show();

            }
            //muuten ei tarvitse tehdä mitään, sillä välitetään vain siitä onko arvoa kuvassa
        }



}
