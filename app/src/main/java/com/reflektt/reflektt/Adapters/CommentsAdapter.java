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
 * Created by Shiko on 07/03/2017.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsVH> {
    private int callerId;
    private LayoutInflater inflater;
    private Context c;
    private Fragment callerFragment;
    private List<Comments> comments;

    public CommentsAdapter(Context context, List<Comments> comments, Fragment f, int id){
        c = context;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        callerId = id;
        callerFragment = f;
        this.comments = comments;
    }
    @Override
    public CommentsVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.comment, parent, false);
        CommentsVH viewHolder = new CommentsVH(view);
        return viewHolder;
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

    public class CommentsVH extends RecyclerView.ViewHolder {
        @BindView(R.id.comment_profile)
        CircularImageView profilePic;
        @BindView(R.id.comment_name)
        TextView username;
        @BindView(R.id.comment_text)
        TextView comment;

        public CommentsVH(View itemView) {
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
