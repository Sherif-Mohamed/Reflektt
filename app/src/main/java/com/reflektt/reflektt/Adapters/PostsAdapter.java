package com.reflektt.reflektt.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import com.reflektt.reflektt.BackgroundService;
import com.reflektt.reflektt.Constants;
import com.reflektt.reflektt.Fragments.CommentsFragment;
import com.reflektt.reflektt.Fragments.UsersFragment;
import com.reflektt.reflektt.HomeFragments.ProfileFragment;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.Tables.Comments;
import com.reflektt.reflektt.Tables.Posts;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.reflektt.reflektt.Adapters.ProductAdapter.VIEW_ITEM;
import static com.reflektt.reflektt.Adapters.ProductAdapter.VIEW_PROG;
import static com.reflektt.reflektt.R.id.comments;

/**
 * Created by Sherif on March,5th
 */

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater inflater;
    private List<Posts> data;
    private Context context;
    private Fragment callerFragment;
    private int callerId;

    public PostsAdapter(LayoutInflater inflater, List<Posts> collection, Fragment caller, int callerId, Context c) {
        this.inflater = inflater;
        data =new LinkedList<> (collection);
        context = c;
        callerFragment = caller;
        this.callerId = callerId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            View view = inflater.inflate(R.layout.post_item, parent, false);
            return new PostsVH(view);
        } else {
            View view = inflater.inflate(R.layout.progress_bar, parent, false);
            return new ProductAdapter.ProgressVH(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder newHolder, int position) {
        if (newHolder instanceof PostsVH) {
            final PostsVH holder = (PostsVH) newHolder;
            Posts property = data.get(position);
            BackendlessUser user = property.getPostedUser();
            //set Profile picture
            Picasso.with(context).load(Constants.PROFILE_PATH + user.getProperty("username") + ".jpg").into(holder.profilePicture);
            //set name
            holder.name.setText((CharSequence) user.getProperty("name"));
            //set username
            holder.username.setText(String.format("@%s", user.getProperty("username")));
            //TODO: add time change
            //set post text
            holder.post.setText(property.getPost());
            //set number of likers
            BackendlessUser[] likers = property.getLikers();
            if (likers.length != 0) {
                holder.likers.setText(String.format(new Locale("en"),"%d", likers.length));
                // if LoggedInUser liked the post, fill the heart of like
                for (BackendlessUser x : likers) {
                    if (x.getObjectId().equals(BackgroundService.getService().getUser().getObjectId())) {
                        holder.favorite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.fav_filled));
                        holder.isLiked = true;
                        break;
                    }
                }
            } else
                holder.likers.setText("0");
            //if comments existed, show view all comments
            List<Comments> comments = property.getComments();
            if (comments.size() > 0)
                holder.viewComments.setVisibility(View.VISIBLE);
            //load post picture
            Picasso.with(context).load(Constants.PHOTO_PATH + property.getPictureName())
                    .into(holder.postPicture, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.loadPhotoProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {

                        }
                    });
            final LinkedList<String> relationProps = new LinkedList<>();
            relationProps.add( "comments" );
            relationProps.add( "comments.commentedUser" );
            Backendless.Data.of(Posts.class).loadRelations(data.get(position), relationProps, new AsyncCallback<Posts>() {
                @Override
                public void handleResponse(Posts response) {
                    data.set(newHolder.getAdapterPosition(), response);
                }

                @Override
                public void handleFault(BackendlessFault fault) {

                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class PostsVH extends RecyclerView.ViewHolder {
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
        @BindView(comments)
        TextView viewComments;
        private boolean isLiked = false;

        PostsVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Posts post =data.get(getAdapterPosition());
                    if (isLiked) favorite.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.fav));
                    else favorite.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.fav_filled));
                    Backendless.Persistence.of(Posts.class).findById(post, new AsyncCallback<Posts>() {
                        @Override
                        public void handleResponse(Posts response) {
                            if (!isLiked) {
                                data.set(getAdapterPosition(),response);
                                BackendlessUser[] oldLikers = response.getLikers();
                                BackendlessUser[] newLikers;
                                if (oldLikers != null) {
                                    newLikers = new BackendlessUser[oldLikers.length + 1];
                                    System.arraycopy(oldLikers, 0, newLikers, 0, oldLikers.length);
                                } else
                                    newLikers = new BackendlessUser[1];
                                newLikers[newLikers.length - 1] = BackgroundService.getService().getUser();
                                data.get(getAdapterPosition()).setLikers(newLikers);
                                likers.setText(String.format(new Locale("en"),"%d",newLikers.length));
                                isLiked = true;
                            } else {
                                BackendlessUser[] oldLikers = data.get(getAdapterPosition()).getLikers();
                                isLiked = false;
                                BackendlessUser[] newLikers = new BackendlessUser[oldLikers.length - 1];
                                int i = 0;
                                for (BackendlessUser x : oldLikers)
                                    if (!x.getObjectId().equals(BackgroundService.getService().getUser().getObjectId()))
                                        newLikers[i++] = x;
                                data.get(getAdapterPosition()).setLikers(newLikers);
                                likers.setText(String.format(new Locale("en"),"%d",newLikers.length));
                            }
                            Backendless.Data.of(Posts.class).save(data.get(getAdapterPosition()), new AsyncCallback<Posts>() {
                                        @Override
                                        public void handleResponse(Posts response) {
                                            data.set(getAdapterPosition(),response);
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
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<Comments> property = data.get(getAdapterPosition()).getComments();
                    CommentsFragment fragment = new CommentsFragment();
                    fragment.setData(property);
                    fragment.setPost(data.get(getAdapterPosition()));
                    Bundle b = new Bundle();
                    b.putInt("callerId", callerId);
                    fragment.setArguments(b);
                    FragmentTransaction ft = callerFragment.getFragmentManager().beginTransaction();
                    ft.replace(callerId, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
            likers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BackendlessUser[] property = data.get(getAdapterPosition()).getLikers();
                    if (property.length != 0) {
                        UsersFragment fragment = new UsersFragment();
                        fragment.users = property;
                        Bundle b = new Bundle();
                        b.putInt("callerId", callerId);
                        fragment.setArguments(b);
                        FragmentTransaction ft = callerFragment.getFragmentManager().beginTransaction();
                        ft.replace(callerId, fragment);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                }
            });
            viewComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<Comments> property = data.get(getAdapterPosition()).getComments();
                    CommentsFragment fragment = new CommentsFragment();
                    fragment.setData(property);
                    Bundle b = new Bundle();
                    b.putInt("callerId", callerId);
                    fragment.setArguments(b);
                    FragmentTransaction ft = callerFragment.getFragmentManager().beginTransaction();
                    ft.replace(callerId, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BackendlessUser property = data.get(getAdapterPosition()).getPostedUser();
                    String userId = property.getObjectId();
                    Fragment newFragment = new ProfileFragment();
                    Bundle b = new Bundle();
                    b.putString("id", userId);
                    newFragment.setArguments(b);
                    FragmentTransaction ft = callerFragment.getFragmentManager().beginTransaction();
                    ft.replace(callerId, newFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BackendlessUser property = data.get(getAdapterPosition()).getPostedUser();
                    String userId = property.getObjectId();
                    Fragment newFragment = new ProfileFragment();
                    Bundle b = new Bundle();
                    b.putString("id", userId);
                    newFragment.setArguments(b);
                    FragmentTransaction ft = callerFragment.getFragmentManager().beginTransaction();
                    ft.replace(callerId, newFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
        }

    }
}
