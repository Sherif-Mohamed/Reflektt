package com.reflektt.reflektt.HomeFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.reflektt.reflektt.BackgroundService;
import com.reflektt.reflektt.Constants;
import com.reflektt.reflektt.Fragments.PostFragment;
import com.reflektt.reflektt.Fragments.ProductsFragment;
import com.reflektt.reflektt.Fragments.UsersFragment;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.Tables.Posts;
import com.reflektt.reflektt.Tables.Products;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileFragment extends Fragment {

    @BindView(R.id.name)TextView name;
    @BindView(R.id.username)TextView userName;
    @BindView(R.id.bio)TextView biography;
    @BindView(R.id.favourites)TextView favourites;
    @BindView(R.id.following)TextView following;
    @BindView(R.id.followers)TextView followers;
    @BindView(R.id.follow_button)Button followButton;
    @BindView(R.id.followers_view)View followers_menu;
    @BindView(R.id.following_view)View following_menu;
    @BindView(R.id.profile_pic)CircularImageView profilePic;
    @BindView(R.id.favorites_menu)View favorites_menu;
    @BindView(R.id.profile_grid)RecyclerView profile_grid;
    @BindView(R.id.privacy)TextView privacyText;
    @BindView(R.id.refresh_profile)SwipeRefreshLayout mRefresh;

    private boolean isFollowed = false;
    private BackendlessUser openedUser;
    public static String id = Backendless.UserService.loggedInUser();

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, v);
        //openedUser = new BackendlessUser();
        id = getArguments() != null ? getArguments().getString("id") : Backendless.UserService.loggedInUser();
        if (id.equals(Backendless.UserService.loggedInUser())) {
            followButton.setEnabled(false);
        }
        mRefresh.setColorSchemeResources(android.R.color.holo_red_light,android.R.color.holo_green_light,android.R.color.holo_blue_light,android.R.color.holo_orange_light);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ProfileFragment f = new ProfileFragment();
                Bundle b = new Bundle();
                b.putString("id",id);
                f.setArguments(b);
                ft.replace(R.id.profile_fragment,f).commit();
                mRefresh.setRefreshing(false);
            }
        });
        getUser(id);
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followButton.setEnabled(false);
                followButton.setText(getResources().getString(R.string.processing));
                followButton.setBackgroundColor(Color.GRAY);
                followButton.setTextColor(Color.BLACK);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isDone;
                        if (isFollowed)
                            isDone=BackgroundService.getService().unfollow(openedUser.getObjectId());
                        else
                            isDone=BackgroundService.getService().follow(openedUser.getObjectId());

                        if (isDone){
                            mRefresh.post(new Runnable() {
                                @Override
                                public void run() {
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ProfileFragment f = new ProfileFragment();
                                    Bundle b = new Bundle();
                                    b.putString("id",id);
                                    f.setArguments(b);
                                    ft.replace(R.id.profile_fragment,f).commit();
                                }
                            });
                        }

                        else Toast.makeText(getContext(),R.string.error,Toast.LENGTH_SHORT).show();
                    }
                }).start();

            }
        });
        followers_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object[] property = (Object[]) openedUser.getProperty("followers");
                UsersFragment fragment = new UsersFragment();
                if (property.length != 0) {
                    fragment.users = (BackendlessUser[]) property;
                    Bundle b = new Bundle();
                    b.putInt("callerId", R.id.profile_fragment);
                    fragment.setArguments(b);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.profile_fragment, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                } else
                    Toast.makeText(getContext(), getString(R.string.empty), Toast.LENGTH_SHORT).show();


            }
        });
        following_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object[] property = (Object[]) openedUser.getProperty("followings");
                UsersFragment fragment = new UsersFragment();

                if (property.length != 0) {
                    fragment.users = (BackendlessUser[]) property;
                    Bundle b = new Bundle();
                    b.putInt("callerId", R.id.profile_fragment);
                    fragment.setArguments(b);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.profile_fragment, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                } else
                    Toast.makeText(getContext(), getString(R.string.empty), Toast.LENGTH_SHORT).show();
            }
        });
        favorites_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object[] property = (Object[]) openedUser.getProperty("favorite_items");
                ProductsFragment fragment = new ProductsFragment();
                if (property.length != 0) {
                    List<Products> list = Arrays.asList((Products[]) property);
                    fragment.setData(list);
                    Bundle b = new Bundle();
                    b.putInt("callerId", R.id.profile_fragment);
                    fragment.setArguments(b);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.profile_fragment, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                } else
                    Toast.makeText(getContext(), getString(R.string.empty), Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    public void getUser(final String id) {
        final ProgressDialog progress = new ProgressDialog(getContext());
        progress.setMessage("Loading...");
        if (!id.equals(Backendless.UserService.loggedInUser())) {
            progress.show();
            BackendlessDataQuery query = new BackendlessDataQuery();
            QueryOptions queryOptions = new QueryOptions();
            queryOptions.addRelated("followings");
            queryOptions.addRelated("followers");
            queryOptions.addRelated("posts");

            queryOptions.addRelated("favorite_items");
            query.setQueryOptions( queryOptions );
            query.setWhereClause(String.format("objectId='%s'",id));
            Backendless.Persistence.of(BackendlessUser.class).find(query, new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
                @Override
                public void handleResponse(BackendlessCollection<BackendlessUser> response) {
                    progress.dismiss();
                    openedUser = response.getData().get(0);
                    Picasso.with(getContext())
                            .load(Constants.PROFILE_PATH + openedUser.getProperty("username") + ".jpg")
                            .into(profilePic);
                    boolean isPrivate = (boolean) openedUser.getProperty("privacy");

                    name.setText((String) openedUser.getProperty("name"));
                    //set username
                    CharSequence i = "@" + openedUser.getProperty("username");
                    userName.setText(i);

                    //get followers and following data from backendless
                    Object[] followersObjects, followingObjects, favoriteItems;
                    try {
                        followersObjects = (Object[]) openedUser.getProperty("followers");
                    } catch (java.lang.ClassCastException e) {
                        followersObjects = null;
                        followers.setText("0");
                    }
                    try {
                        followingObjects = (Object[]) openedUser.getProperty("followings");
                    } catch (java.lang.ClassCastException e) {
                        followingObjects = null;
                        following.setText("0");
                    }
                    try {
                        favoriteItems = (Object[]) openedUser.getProperty("favorite_items");
                    } catch (java.lang.ClassCastException e) {
                        favoriteItems = null;
                        favourites.setText("0");
                    }
                    if (followingObjects != null) {
                        Object[] followings = (Object[]) BackgroundService.getService().getUser().getProperty("followings");
                        if (followings.length > 0) {
                            BackendlessUser[] followed = (BackendlessUser[]) followings;
                            for (BackendlessUser followedUser : followed) {
                                if (followedUser.getObjectId().equals(id) && !id.equals(Backendless.UserService.loggedInUser())) {
                                    followButton.setText(getResources().getString(R.string.unfollow));
                                    isFollowed = true;
                                    profile_grid.setLayoutManager(new GridLayoutManager(getContext(), Constants.calculateNoOfColumns(getContext(),100)));
                                    profile_grid.setNestedScrollingEnabled(false);
                                    profile_grid.setAdapter(new SimpleAdapter());
                                    break;
                                }
                            }
                            if (isPrivate) privacyText.setVisibility(View.VISIBLE);
                        }
                        else{
                            if (isPrivate) privacyText.setVisibility(View.VISIBLE);
                            else{
                                profile_grid.setLayoutManager(new GridLayoutManager(getContext(), Constants.calculateNoOfColumns(getContext(),100)));
                                profile_grid.setNestedScrollingEnabled(false);
                                profile_grid.setAdapter(new SimpleAdapter());
                            }
                        }
                        following.setText(String.valueOf(followingObjects.length));
                    }

                    if (followersObjects != null)
                        followers.setText(String.valueOf(followersObjects.length));

                    if (favoriteItems != null)
                        favourites.setText(String.valueOf(favoriteItems.length));
                }

                @Override
                public void handleFault(BackendlessFault fault) {

                }
            });
        } else {
            openedUser = BackgroundService.getService().getUser();
            if (BackgroundService.getService().getProfilePicture() == null)
                Picasso.with(getActivity())
                        .load(Constants.PROFILE_PATH + openedUser.getProperty("username") + ".jpg")
                        .into(profilePic, new Callback() {
                            @Override
                            public void onSuccess() {
                                BackgroundService.getService().setProfilePicture(((BitmapDrawable) profilePic.getDrawable()).getBitmap());
                            }

                            @Override
                            public void onError() {

                            }
                        });
            else
                profilePic.setImageBitmap(BackgroundService.getService().getProfilePicture());

            profile_grid.setLayoutManager(new GridLayoutManager(getContext(), Constants.calculateNoOfColumns(getContext(),100)));
            profile_grid.setNestedScrollingEnabled(false);
            profile_grid.setAdapter(new SimpleAdapter());
            name.setText((String) openedUser.getProperty("name"));
            followButton.setVisibility(View.GONE);
            //set username
            userName.setText(String.format("@%s", openedUser.getProperty("username")));

            //get followers and following data from backendless
            Object[] followersObjects, followingObjects, favoriteItems;
            try {
                followersObjects = (Object[]) openedUser.getProperty("followers");
                followers.setText(String.valueOf(followersObjects.length));
            } catch (java.lang.NullPointerException e) {
                followers.setText("0");
            }
            try {
                followingObjects = (Object[]) openedUser.getProperty("followings");
                this.following.setText(String.valueOf(followingObjects.length));
            } catch (java.lang.NullPointerException e) {
                following.setText("0");
            }
            try {
                favoriteItems = (Object[]) openedUser.getProperty("favorite_items");
                favourites.setText(String.valueOf(favoriteItems.length));
            } catch (java.lang.NullPointerException e) {
                favourites.setText("0");
            }
            biography.setText((CharSequence) openedUser.getProperty("biography"));
        }

    }

     class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleVH> {
        LayoutInflater inflater;
        Posts[] data;

        SimpleAdapter() {
            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Object[] temp = (Object[]) openedUser.getProperty("posts");
            if (temp != null && temp.length != 0)
                data = (Posts[]) temp;
        }

        @Override
        public SimpleVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.profile_post, parent, false);
            return new SimpleVH(view);
        }

        @Override
        public void onBindViewHolder(final SimpleVH holder, int position) {
            holder.progress.setVisibility(View.VISIBLE);
            Picasso.with(getContext()).load(Constants.PHOTO_PATH + data[position].getPictureName())
                    .into(holder.image, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {

                        }
                    });
        }

        @Override
        public int getItemCount() {
            if (data == null) return 0;
            return data.length;
        }

        class SimpleVH extends RecyclerView.ViewHolder {
            @BindView(R.id.profile_post)
            ImageView image;
            @BindView(R.id.progress)
            ProgressBar progress;

            SimpleVH(final View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PostFragment newFragment = new PostFragment();
                        newFragment.setLoadedPost(data[getAdapterPosition()]);
                        Bundle b = new Bundle();
                        b.putInt("callerId", R.id.profile_fragment);
                        newFragment.setArguments(b);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.profile_fragment, newFragment);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });
            }
        }
    }
}
