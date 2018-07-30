package teravainen.imagegameapp;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class bottomNavigation extends AppCompatActivity{

    private FragmentAdapter mFragmentAdapter;
    private ViewPager mViewPager;

    TextView myTextView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startMain();
                    return true;
                case R.id.navigation_debug:
                    startDebug();
                    return true;
                case R.id.navigation_json:
                    startJson();
                    return true;
                case R.id.navigation_about:
                    startAbout();
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        //Hot to make screen orientation in portrait mode always, needs to be included in all activities where we want it to be locked to portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.fragContainer);
        //setup pager
        setupViewPager(mViewPager);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    public void startMain(){
        setViewPager(0);
    }

    public void startDebug(){
        setViewPager(1);
    }

    public void startJson(){
        setViewPager(2);
    }

    public void startAbout(){
        setViewPager(4);
    }

    private void setupViewPager(ViewPager viewPager){
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new Fragment1(), "Fragment 1");
        adapter.addFragment(new Fragment2(), "Fragment 2");
        adapter.addFragment(new Fragment3(), "Fragment 3");
        adapter.addFragment(new FragmentEditDB(), "Fragment 4");
        adapter.addFragment(new FragmentAbout(), "Fragment about");
        viewPager.setAdapter(adapter);
    }

    public void setViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
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
