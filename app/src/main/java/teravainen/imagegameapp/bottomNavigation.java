package teravainen.imagegameapp;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class bottomNavigation extends AppCompatActivity{

    private FragmentAdapter mFragmentAdapter;
    private ViewPager mViewPager;


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

        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //how to set the animations moving between different fragments
        mViewPager.setPageTransformer(true, new DepthPageTransformer());


        //Change the active menu in the bottom after swiping, does not currently work
        //position is known inside the app as the active page, same as fragment position
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
          @Override
          public void onPageSelected(int position){
              //when swiping between pages, select the right fragment/tab
              //Log.e("pageChange", "Page is: " + position);
              //navigation.setSelectedItemId(position);
              switch (position){
                  case 0:
                      navigation.setSelectedItemId(R.id.navigation_home);
                      break;
                  case 1:
                      navigation.setSelectedItemId(R.id.navigation_debug);
                      break;
                  case 2:
                      navigation.setSelectedItemId(R.id.navigation_json);
                      break;
                  case 3:
                      navigation.setSelectedItemId(R.id.navigation_about);
                      break;
              }
          }
        });


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
        setViewPager(3);
    }

    private void setupViewPager(ViewPager viewPager){
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new Fragment1(), "Fragment 1");
        adapter.addFragment(new Fragment2(), "Fragment 2");
        adapter.addFragment(new Fragment3(), "Fragment 3");
        adapter.addFragment(new FragmentAbout(), "Fragment about");
        viewPager.setAdapter(adapter);
    }

    public void setViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }


}
