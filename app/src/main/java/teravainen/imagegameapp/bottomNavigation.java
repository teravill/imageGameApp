package teravainen.imagegameapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;

public class bottomNavigation extends AppCompatActivity{

    private FragmentAdapter mFragmentAdapter;
    private ViewPager mViewPager;



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setViewPager(0);
                    return true;
                case R.id.navigation_debug:
                    setViewPager(1);
                    return true;
                case R.id.navigation_json:
                    setViewPager(2);
                    return true;
                case R.id.navigation_about:
                    setViewPager(3);
                    return true;
            }
            return false;
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //do a switch statement for the item ids from toolbar_menu.xml
        switch (item.getItemId()){
            case R.id.mainScreen:
                //add something here
                //maybe settings menu?
            case R.id.toolbarHomeButton:
                setViewPager(0);
                break;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        //Hot to make screen orientation in portrait mode always, needs to be included in all activities where we want it to be locked to portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Create the toolbar
        android.support.v7.widget.Toolbar myToolBar = findViewById(R.id.myToolBar);
        setSupportActionBar(myToolBar);
        //myToolBar.setLogo("@drawable/");

        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        mViewPager =  findViewById(R.id.fragContainer);
        //setup pager
        setupViewPager(mViewPager);

        final BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //how to set the animations moving between different fragments
        //this only works properly with swiping between fragments, as the animation plays really fast if you use the bottom nav buttons
      //  mViewPager.setPageTransformer(true, new DepthPageTransformer());
       // mViewPager.setPageTransformer(true, new FadePageTransformer());


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

    private void setupViewPager(ViewPager viewPager){
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new Fragment1(), "Fragment 1");
        adapter.addFragment(new Fragment2(), "Fragment 2");
        adapter.addFragment(new Fragment3(), "Fragment 3");
        adapter.addFragment(new FragmentAbout(), "Fragment about");
        viewPager.setAdapter(adapter);
    }

    public void setViewPager(int fragmentNumber){
        //remove the false if you want animations when not swiping
        mViewPager.setCurrentItem(fragmentNumber,false);
    }


}
