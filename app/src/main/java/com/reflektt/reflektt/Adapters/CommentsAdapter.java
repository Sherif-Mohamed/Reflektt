package com.reflektt.reflektt.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.backendless.BackendlessUser;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.reflektt.reflektt.Constants;
import com.reflektt.reflektt.HomeFragments.ProfileFragment;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.Tables.Comments;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sherif on March,7th
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsVH> {
    private int callerId;
    private LayoutInflater inflater;
    private Context c;
    private Fragment callerFragment;
    private List<Comments> comments;

    public CommentsAdapter(LayoutInflater inflater, Context context, List<Comments> comments, Fragment f, int id) {
        c = context;
        this.inflater = inflater;
        callerId = id;
        callerFragment = f;
        this.comments = comments;
    }
    @Override
    public CommentsVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.comment, parent, false);
        return new CommentsVH(view);
    }

    @Override
    public void onBindViewHolder(CommentsVH holder, int position) {
        Comments property = comments.get(position);
        holder.username.setText((CharSequence) property.getCommentedUser().getProperty("name"));
        holder.comment.setText(property.getComment());
        Picasso.with(c).load(Constants.PROFILE_PATH + property.getCommentedUser().getProperty("username")+".jpg")
                .into(holder.profilePic);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class CommentsVH extends RecyclerView.ViewHolder {
        @BindView(R.id.comment_profile)
        CircularImageView profilePic;
        @BindView(R.id.comment_name)
        TextView username;
        @BindView(R.id.comment_text)
        TextView comment;

        CommentsVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BackendlessUser property = comments.get(getAdapterPosition()).getCommentedUser();
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
