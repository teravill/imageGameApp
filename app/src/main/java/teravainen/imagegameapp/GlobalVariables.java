package teravainen.imagegameapp;

import android.app.Application;

public class GlobalVariables extends Application{

    //voit kutsua tätä muualta esim: GlobalVariables.someValue
    public static String someValue = "This is a global string";
    public static int score = 0;

    private static GlobalVariables singleton;

    public static GlobalVariables getInstance(){
        return singleton;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        singleton = this;
    }

}
