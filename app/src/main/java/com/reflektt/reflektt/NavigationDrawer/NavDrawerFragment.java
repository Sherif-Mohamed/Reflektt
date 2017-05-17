package com.reflektt.reflektt.NavigationDrawer;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.reflektt.reflektt.BackgroundService;
import com.reflektt.reflektt.Constants;
import com.reflektt.reflektt.LoadingActivity;
import com.reflektt.reflektt.NavDrawerOtherActivities.AddProduct;
import com.reflektt.reflektt.NavDrawerOtherActivities.Settings;
import com.reflektt.reflektt.NavDrawerOtherActivities.SortActivity;
import com.reflektt.reflektt.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class NavDrawerFragment extends Fragment implements NavMenuAdapter.ClickListener {

    //saving data inside a file named :
    public static final String PREF_FILE_NAME = "testpref";

    //navigation drawer vars
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout mDrawerLayout;

    //recycler view for sorting the menu
    private RecyclerView recyclerView2;
    private NavMenuAdapter adapter;
    @BindView(R.id.username) TextView name;
    @BindView(R.id.email) TextView email;
    @BindView(R.id.profile_pic) CircularImageView profilePic;

    public NavDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nav_drawer, container, false);
        ButterKnife.bind(this, view);
        recyclerView2 = (RecyclerView) view.findViewById(R.id.nav_recycler);
        String[] data = getResources().getStringArray(R.array.nav_drawer);
        int[] icons = {R.drawable.ic_brands, R.drawable.ic_products, R.drawable.ic_addproduct, R.drawable.ic_settings, R.drawable.ic_logout};
        adapter = new NavMenuAdapter(getActivity(), data, icons);
        adapter.setClickListener(this);
        recyclerView2.setAdapter(adapter);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getActivity()));

        BackendlessUser user = BackgroundService.getService().getUser();
        name.setText((CharSequence) user.getProperty("name"));
        email.setText((CharSequence) user.getProperty("email"));
        Picasso.with(getContext())
                .load(Constants.PROFILE_PATH+user.getProperty("username")+".jpg")
                .into(profilePic);
        return view;
    }

    public void setUp(int id, DrawerLayout drawerLayout, final Toolbar toolbar) {
        mDrawerLayout = drawerLayout;
        drawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(drawerToggle);
        //to make the hamburger button appear
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (position) {
            case 0:{
                Intent intent = new Intent(getActivity(), SortActivity.class);
                intent.putExtra("sort","companyName");
                startActivity(intent);
                break;
            }
            case 1:{
                Intent intent = new Intent(getActivity(), SortActivity.class);
                intent.putExtra("sort","type");

                startActivity(intent);
                break;
            }
            case 2:{
                startActivity(new Intent(getActivity(), AddProduct.class));
                break;
            }
            case 3:{
                startActivity(new Intent(getActivity(), Settings.class));
                break;}
            case 4: {
                new AlertDialog.Builder(getActivity())
                        .setMessage(getResources().getString(R.string.logout_q))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Backendless.UserService.logout(new AsyncCallback<Void>() {
                                    @Override
                                    public void handleResponse(Void response) {
                                        startActivity(new Intent(getContext(), LoadingActivity.class));
                                        getActivity().finish();
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.no), null)
                        .show();
            }

        }

    }
}
