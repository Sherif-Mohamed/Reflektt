package com.reflektt.reflektt.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.reflektt.reflektt.Adapters.CommentsAdapter;
import com.reflektt.reflektt.BackgroundService;
import com.reflektt.reflektt.Constants;
import com.reflektt.reflektt.HomeFragments.ProfileFragment;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.Tables.Comments;
import com.reflektt.reflektt.Tables.Posts;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sherif on March,11th
 */

public class PostFragment extends Fragment {

    @BindView(R.id.profilePicture)
    CircularImageView profilePicture;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.username)
    TextView username;
    @BindView(R.id.time)
    TextView creationTime;
    @BindView(R.id.loadPhotoProgress)
    ProgressBar loadPhotoProgress;
    @BindView(R.id.post_pic)
    ImageView postPicture;
    @BindView(R.id.post_text)
    TextView post;
    @BindView(R.id.fav)
    ImageView favorite;
    @BindView(R.id.comment)
    ImageView comment;
    @BindView(R.id.fav_number)
    TextView likers;
    @BindView(R.id.commentsRecycler)
    RecyclerView commentsRecycler;
    LayoutInflater inflater;
    private Posts loadedPost = null;
    private int callerId;
    private boolean isLiked = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater = inflater;
        View v = inflater.inflate(R.layout.post_item, container, false);
        ButterKnife.bind(this, v);
        callerId = getArguments().getInt("callerId");
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("Please wait..");
        if (loadedPost.getPostedUser() == null) {
            dialog.show();
            Backendless.Data.of(Posts.class).findById(loadedPost.getObjectId(), new AsyncCallback<Posts>() {
                @Override
                public void handleResponse(Posts response) {
                    loadedPost = response;
                    dialog.dismiss();
                    loadPost(response);
                }

                @Override
                public void handleFault(BackendlessFault fault) {

                }
            });
        } else
            loadPost(loadedPost);

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Posts post = loadedPost;
                if (isLiked)
                    favorite.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fav));
                else
                    favorite.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fav_filled));
                Backendless.Persistence.of(Posts.class).findById(post, new AsyncCallback<Posts>() {
                    @Override
                    public void handleResponse(Posts response) {
                        if (!isLiked) {
                            loadedPost = response;
                            BackendlessUser[] oldLikers = response.getLikers();
                            BackendlessUser[] newLikers;
                            if (oldLikers != null) {
                                newLikers = new BackendlessUser[oldLikers.length + 1];
                                System.arraycopy(oldLikers, 0, newLikers, 0, oldLikers.length);
                            } else
                                newLikers = new BackendlessUser[1];
                            newLikers[newLikers.length - 1] = BackgroundService.getService().getUser();
                            loadedPost.setLikers(newLikers);
                            likers.setText(String.format(new Locale("en"),"%d",newLikers.length));
                            isLiked = true;
                        } else {
                            BackendlessUser[] oldLikers = loadedPost.getLikers();
                            isLiked = false;
                            BackendlessUser[] newLikers = new BackendlessUser[oldLikers.length - 1];
                            int i = 0;
                            for (BackendlessUser x : oldLikers)
                                if (!x.getObjectId().equals(BackgroundService.getService().getUser().getObjectId()))
                                    newLikers[i++] = x;
                            loadedPost.setLikers(newLikers);
                            likers.setText(String.format(new Locale("en"),"%d",newLikers.length));
                        }
                        Backendless.Data.of(Posts.class).save(loadedPost, new AsyncCallback<Posts>() {
                            @Override
                            public void handleResponse(Posts response) {

                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {

                            }
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {

                    }
                });
            }
        });
        comment.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                List<Comments> property = loadedPost.getComments();
                CommentsFragment fragment = new CommentsFragment();
                fragment.setData(property);
                fragment.setPost(loadedPost);
                Bundle b = new Bundle();
                b.putInt("callerId", callerId);
                fragment.setArguments(b);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(callerId, fragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        likers.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                BackendlessUser[] property = loadedPost.getLikers();
                if (property.length != 0) {
                    UsersFragment fragment = new UsersFragment();
                    fragment.users = property;
                    Bundle b = new Bundle();
                    b.putInt("callerId", callerId);
                    fragment.setArguments(b);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(callerId, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            }
        });
        name.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                BackendlessUser property = loadedPost.getPostedUser();
                String userId = property.getObjectId();
                Fragment newFragment = new ProfileFragment();
                Bundle b = new Bundle();
                b.putString("id", userId);
                newFragment.setArguments(b);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(callerId, newFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        username.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                BackendlessUser property = loadedPost.getPostedUser();
                String userId = property.getObjectId();
                Fragment newFragment = new ProfileFragment();
                Bundle b = new Bundle();
                b.putString("id", userId);
                newFragment.setArguments(b);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(callerId, newFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return v;
    }

    public void loadPost(Posts response) {
        BackendlessUser user = response.getPostedUser();
        //set Profile picture
        Picasso.with(getActivity()).load(Constants.PROFILE_PATH + user.getProperty("username") + ".jpg").into(profilePicture);
        //set name
        name.setText((CharSequence) user.getProperty("name"));
        //set username
        username.setText(String.format("@%s", user.getProperty("username")));
        //TODO: add time change
        //set post text
        post.setText(response.getPost());
        //set number of likers
        BackendlessUser[] likes = response.getLikers();
        if (likes.length != 0) {
            likers.setText(String.format(new Locale("en"),"%d",likes.length));
            // if LoggedInUser liked the post, fill the heart of like
            for (BackendlessUser x : likes) {
                if (x == BackgroundService.getService().getUser()) {
                    favorite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.fav_filled));
                    isLiked = true;
                    break;
                }
            }
        } else
            likers.setVisibility(View.GONE);
        //if comments existed, show view all comments
        List<Comments> comments = response.getComments();
        if (comments.size() > 0)
            commentsRecycler.setVisibility(View.VISIBLE);
        //load post picture
        Picasso.with(getActivity()).load(Constants.PHOTO_PATH + response.getPictureName())
                .into(postPicture, new Callback() {
                    @Override
                    public void onSuccess() {
                        loadPhotoProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {

                    }
                });
        List<Comments> data = loadedPost.getComments();
        LinearLayoutManager m = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        commentsRecycler.setHasFixedSize(true);
        commentsRecycler.setNestedScrollingEnabled(false);
        CommentsAdapter adapter = new CommentsAdapter(inflater, getActivity(), data, this, getArguments().getInt("callerId"));
        commentsRecycler.setAdapter(adapter);
        commentsRecycler.setLayoutManager(m);

    }

    public void setLoadedPost(Posts loadedPost) {
        this.loadedPost = loadedPost;
    }
}
