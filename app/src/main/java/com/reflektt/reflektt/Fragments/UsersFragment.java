package com.reflektt.reflektt.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
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
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {
    @BindView(R.id.users_list)RecyclerView usersList;
    @BindView(R.id.refresh_users)SwipeRefreshLayout mRefresh;
    private int callerId;
    public BackendlessUser[] users;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_users, container, false);
        int id = getArguments().getInt("callerId");
        ButterKnife.bind(this,v);

        mRefresh.setColorSchemeResources(android.R.color.holo_red_light,android.R.color.holo_green_light,android.R.color.holo_blue_light,android.R.color.holo_orange_light);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(callerId,new UsersFragment()).commit();
                mRefresh.setRefreshing(false);
            }
        });

        usersList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        usersList.setHasFixedSize(true);
        if (users.length != 0)
            usersList.setAdapter(new UserAdapter(getActivity(),id));

        return v;
    }
    private class UserAdapter extends RecyclerView.Adapter<UserVH>{
        private LayoutInflater inflater;
        private Context context;

        UserAdapter(Context c, int id){
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            context = c;
            callerId = id;
        }
        @Override
        public UserVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.user_item, parent, false);
            return new UserVH(view);
        }

        @Override
        public void onBindViewHolder(UserVH holder, int position) {
            String name = (String) users[position].getProperty("username");
            holder.username.setText(name);
            Picasso.with(context).load(Constants.PROFILE_PATH + name + ".jpg").into(holder.profilePic);
        }

        @Override
        public int getItemCount() {
            return users.length;
        }
    }
    class UserVH extends RecyclerView.ViewHolder {
        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.profile_pic)
        CircularImageView profilePic;
        UserVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userId = users[getAdapterPosition()].getObjectId();
                    Fragment newFragment = new ProfileFragment();
                    Bundle b = new Bundle();
                    b.putString("id",userId);
                    newFragment.setArguments(b);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(callerId, newFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
        }

    }
}
