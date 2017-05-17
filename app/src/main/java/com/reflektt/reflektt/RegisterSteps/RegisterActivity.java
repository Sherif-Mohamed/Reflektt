package com.reflektt.reflektt.RegisterSteps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.reflektt.reflektt.BackgroundService;
import com.reflektt.reflektt.HomeActivity;
import com.reflektt.reflektt.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

/**
 * Created by Akshay Raj on 7/28/2016.
 * Snow Corporation Inc.
 * www.snowcorp.org
 */
public class RegisterActivity extends AppCompatActivity {

    int[] colorsActive;
    int[] colorsInactive;
    @BindView(R.id.reg_pager)
    RegisterViewPager viewPager;
    @BindView(R.id.layoutDots)
    LinearLayout dotsLayout;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.btn_back)
    Button btnBack;
    Boolean isRegistered = false;
    MyViewPagerAdapter myViewPagerAdapter;
    private BackendlessUser registeredUser;
    TextView[] dots;
    Fragment[] fragments;

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            // changing the next button text 'NEXT' / 'GOT IT'
            if (!isRegistered) {
                if (position == fragments.length - 1) {
                    // last page. make button text to GOT IT
                    addBottomDots(position);
                    btnNext.setText(R.string.finish);
                    btnNext.setBackgroundColor(ContextCompat.getColor(RegisterActivity.this, R.color.colorPrimaryDark));
                    btnBack.setVisibility(View.GONE);
                } else {
                    addBottomDots(position);
                    btnNext.setText(getResources().getString(R.string.next));
                    btnNext.setBackgroundColor(colorsInactive[position]);
                    btnBack.setBackgroundColor(colorsInactive[position]);
                    if (position != 0)
                        btnBack.setVisibility(View.VISIBLE);
                    else
                        btnBack.setVisibility(View.GONE);
                }
            } else {
                if (position == fragments.length - 1) {
                    // last page. make button text to GOT IT
                    btnNext.setText(R.string.finish);
                    btnNext.setBackgroundColor(ContextCompat.getColor(RegisterActivity.this, R.color.colorPrimaryDark));
                    btnBack.setVisibility(View.GONE);
                } else {
                    addBottomDots(position);
                    btnNext.setText(getResources().getString(R.string.next));
                    btnNext.setBackgroundColor(colorsInactive[position + 1]);
                    btnBack.setBackgroundColor(colorsInactive[position + 1]);
                    if (position != 0)
                        btnBack.setVisibility(View.VISIBLE);
                    else
                        btnBack.setVisibility(View.GONE);
                }
            }

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        colorsActive = getResources().getIntArray(R.array.array_dot_active);
        colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        //check if the register is from social media account or normal registration
        String loginType = getIntent().getStringExtra("login");
        //TODO:Add ready fragment after creating it
        if (loginType.equals("social")) {
            fragments = new Fragment[]{new StepTwo(), new StepThree(), new StepFour()};
            registeredUser = BackgroundService.getService().getUser();
            btnNext.setBackgroundColor(colorsInactive[1]);
            btnBack.setBackgroundColor(colorsInactive[1]);
            isRegistered = true;
        } else if (loginType.equals("normal")) {

            fragments = new Fragment[]{new StepOne(), new StepTwo(), new StepThree(), new StepFour()};
            btnNext.setBackgroundColor(colorsInactive[0]);
            btnBack.setBackgroundColor(colorsInactive[0]);
            isRegistered = false;
        }

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        final ProgressDialog prog = new ProgressDialog(this);

        // adding bottom dots
        addBottomDots(0);

        // linking view pager with the adapter
        myViewPagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = viewPager.getCurrentItem();
                if (current > 0) {
                    // move to back screen
                    viewPager.setCurrentItem(current - 1);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = viewPager.getCurrentItem();
                int x = 0;
                if (isRegistered) {
                    x = 1;
                }
                switch (current + x) {
                    case 0: {
                        StepOne fragment = (StepOne) fragments[0 - x];
                        BackendlessUser user = fragment.process();
                        if (user != null) {
                            registeredUser = user;
                            viewPager.setCurrentItem(current + 1);
                        }
                        break;
                    }
                    case 1: {
                        StepTwo fragment = (StepTwo) fragments[1 - x];
                        BackendlessUser user = fragment.process(registeredUser);
                        if (user != null) {
                            registeredUser = user;
                            viewPager.setCurrentItem(current + 1);
                        }
                        break;
                    }
                    case 2: {
                        StepThree fragment = (StepThree) fragments[2 - x];
                        BackendlessUser user = fragment.process(registeredUser);
                        if (user != null) {
                            registeredUser = user;
                            viewPager.setCurrentItem(current + 1);
                        }
                        break;
                    }
                    case 3: {
                        StepFour fragment = (StepFour) fragments[3 - x];
                        prog.setMessage(getResources().getString(R.string.please_wait));
                        prog.show();
                        BackendlessUser user = fragment.process(registeredUser);
                        //final int finalCurrent = current;
                        if (user != null) {
                            if (isRegistered) {
                                Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
                                    @Override
                                    public void handleResponse(BackendlessUser response) {
                                        //Registration Done
                                        prog.dismiss();
                                        BackgroundService.getService().setCurrentUser(response);
                                        //viewPager.setCurrentItem(finalCurrent + 1);
                                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                        finish();
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        //Registration Error
                                        prog.dismiss();
                                        Toast.makeText(RegisterActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
                                    @Override
                                    public void handleResponse(BackendlessUser response) {
                                        //Registration Done
                                        Backendless.UserService.login(response.getEmail(), response.getPassword(), new AsyncCallback<BackendlessUser>() {
                                            @Override
                                            public void handleResponse(BackendlessUser response) {
                                                BackendlessDataQuery query = new BackendlessDataQuery();
                                                QueryOptions queryOptions = new QueryOptions();
                                                queryOptions.addRelated("followings");
                                                queryOptions.addRelated("followers");
                                                queryOptions.addRelated("posts");
                                                queryOptions.addRelated("favorite_items");
                                                query.setQueryOptions(queryOptions);
                                                query.setWhereClause(String.format("objectId='%s'", response.getObjectId()));
                                                Backendless.Persistence.of(BackendlessUser.class).find(query, new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
                                                    @Override
                                                    public void handleResponse(BackendlessCollection<BackendlessUser> response) {
                                                        BackendlessUser user =response.getCurrentPage().get(0);
                                                        BackgroundService.getService().setCurrentUser(user);
                                                        prog.dismiss();
                                                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                        finish();
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {

                                                    }
                                                });
                                                BackgroundService.getService().setCurrentUser(response);

                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {
                                            }
                                        }, true);
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        //Registration Error
                                        prog.dismiss();
                                        Toast.makeText(RegisterActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                        break;
                    }
                    default:
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                        break;
                }
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[fragments.length];

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                dots[i].setText(Html.fromHtml("&#8226;", FROM_HTML_MODE_LEGACY));
            else
                dots[i].setText(Html.fromHtml("&#8226;"));

            dots[i].setTextSize(35);
            if (isRegistered)
                dots[i].setTextColor(colorsInactive[currentPage + 1]);
            else
                dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }
        if (dots.length > 0)
            if (isRegistered)
                dots[currentPage].setTextColor(colorsActive[currentPage + 1]);
            else
                dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    /**
     * View pager adapter
     */
    private class MyViewPagerAdapter extends FragmentPagerAdapter {

        MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }

}