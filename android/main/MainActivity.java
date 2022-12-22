package com.mobitant.yesterdaytv;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.mobitant.yesterdaytv.item.MemberInfoItem;
import com.mobitant.yesterdaytv.lib.GoLib;

public class MainActivity extends AppCompatActivity {

    MemberInfoItem memberInfoItem;
    TextView toolbartitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        memberInfoItem = ((MyApp)getApplication()).getMemberInfoItem();
        setToolbar();

        GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main,
                TvInfoListFragment.newInstance());
    }

    private void setToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbartitle  = (TextView)findViewById(R.id.toolbar_title);
        final ActionBar actionBar = getSupportActionBar();


        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    public void changeTitle(String text){
        toolbartitle.setText(text);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_list:
                    GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main, TvInfoListFragment.newInstance());
                    return true;
                case R.id.navigation_search:
                    GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main, TvInfoSearchFragment.newInstance());
                    return true;
                case R.id.navigation_map:
                    GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main, TvInfoMapFragment.newInstance());
                    return true;
                case R.id.navigation_recommend:
                    GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main, TvInfoRecFragment.newInstance());
                    return true;
                case R.id.navigation_etc:
                    GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main, TvInfoMenuFragment.newInstance());
                    return true;

            }
            return false;
        }
    };



}
