package com.reflektt.reflektt;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.reflektt.reflektt.HomeFragments.AddPost.AddPost_Phase1;
import com.reflektt.reflektt.HomeFragments.HomeFragment;
import com.reflektt.reflektt.HomeFragments.NotificationsFragment;
import com.reflektt.reflektt.HomeFragments.ProfileFragment;
import com.reflektt.reflektt.NavigationDrawer.NavDrawerFragment;
import com.reflektt.reflektt.SlidingTabRaw.SlidingTabLayout;

import java.util.Locale;


public class HomeActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private PagerAdapter pAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        pAdapter =new PagerAdapter(getSupportFragmentManager());
        Toolbar_NavDrawer();
        Create_Tabs();
    }

    private void Create_Tabs() {
        mPager = (ViewPager) findViewById(R.id.pager);
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        //setting adapter to create tabs contents
        mPager.setAdapter(pAdapter);
        //to save the updated fragments from transitions between fragments
        mPager.setOffscreenPageLimit(2);
        //opening tab is the home (first tab)
        mPager.setCurrentItem(0);
        // Icons for the tab
        mTabs.setCustomTabView(R.layout.tab_bar, R.id.tab_text);
        //weight = 1 for all tabs
        mTabs.setDistributeEvenly(true);
        //setting background color
        mTabs.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mTabs.setSelectedIndicatorColors(ContextCompat.getColor(this, R.color.colorAccent));
        //set the View pager to the tabs to attach them
        mTabs.setViewPager(mPager);
    }

    private void Toolbar_NavDrawer() {
        //creating toolbar in activity
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        setSupportActionBar(toolbar);

        //navigation drawer creation
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        NavDrawerFragment navDrawerFragment = (NavDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.nav_drawer);
        navDrawerFragment.setUp(R.id.nav_drawer, drawerLayout, toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search){
            Intent intent = new Intent(this,SearchActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    //View Pager adapter to create the tabs contents
    private class PagerAdapter extends FragmentStatePagerAdapter {
        int[] icons = {R.drawable.ic_home,R.drawable.ic_add, R.drawable.ic_person};// add this to add notifications, R.drawable.ic_notifications};

        PagerAdapter(FragmentManager fm) {
            super(fm);
            if(getApplicationContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL){
                int[] newIcons = new int[icons.length];
                int i = icons.length-1;
                for (int icon:icons)
                    newIcons[i--]=icon;

                icons = newIcons;
                }
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new AddPost_Phase1();
                case 2:
                    return new ProfileFragment();
                case 3:
                    return new NotificationsFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            Drawable drawable = ResourcesCompat.getDrawable(getResources(), icons[position], null);
            drawable.setBounds(0, 0, 24, 24);
            ImageSpan imageSpan = new ImageSpan(drawable);
            SpannableString spannableString = new SpannableString(String.format(new Locale("en")," "));
            spannableString.setSpan(imageSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            return spannableString;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public void loadedPost(){
        pAdapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pAdapter);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.exit_q))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final ProgressDialog exit = new ProgressDialog(HomeActivity.this);
                            exit.setMessage("Exiting...");
                            exit.show();
                            Backendless.Persistence.of(BackendlessUser.class).save(BackgroundService.getService().getUser(),
                                    new AsyncCallback<BackendlessUser>() {
                                        @Override
                                        public void handleResponse(BackendlessUser response) {
                                            exit.dismiss();
                                            HomeActivity.this.finish();
                                        }

                                        @Override
                                        public void handleFault(BackendlessFault fault) {

                                        }
                                    });
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.no), null)
                    .show();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

}
