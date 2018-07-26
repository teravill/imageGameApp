package teravainen.imagegameapp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UtilityFunctions extends Application{

    private UtilityFunctions(){

    }

    @SuppressLint("NewApi")
    public static void resetScore(Context ctx){
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putString("Mname", "No mission");
        editor.putString("Mdifficulty", "No difficulty");
        editor.putInt("Mpoints", 0);
        editor.putBoolean("Mprogress", false);
        editor.putString("Mdescription", "");
        editor.apply();
    }


}
