package com.reflektt.reflektt.HomeFragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.reflektt.reflektt.Adapters.PostsAdapter;
import com.reflektt.reflektt.Adapters.ProductAdapter;
import com.reflektt.reflektt.BackgroundService;
import com.reflektt.reflektt.Constants;
import com.reflektt.reflektt.Fragments.ProductsFragment;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.Tables.Posts;
import com.reflektt.reflektt.Tables.Products;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomeFragment extends Fragment {

    @BindView(R.id.products_load)ProgressBar loadProducts;
    @BindView(R.id.posts_load)ProgressBar loadPosts;
    @BindView(R.id.products_view)RecyclerView productsRecycler;
    @BindView(R.id.posts_view)RecyclerView postsRecycler;
    @BindView(R.id.products_error)TextView productsErrorText;
    @BindView(R.id.posts_error)TextView postsErrorText;
    @BindView(R.id.more)TextView more;
    @BindView(R.id.refresh_home)SwipeRefreshLayout mRefresh;
    @BindView(R.id.suggestUsers)View suggestUsers;
    @BindView(R.id.suggestUsersLoad)ProgressBar loadUsers;
    @BindView(R.id.users_view)RecyclerView usersRecycler;
    QueryOptions queryOptions = new QueryOptions();
    List<String> sortBy = new LinkedList<>();
    BackendlessCollection<Posts> postsData;
    BackendlessCollection<Products> productsData;
    List<Products> data;
    List<Posts> posts=new LinkedList<>();
    LinearLayoutManager productsManager;
    LinearLayoutManager postsManager;
    ProductAdapter productAdapter;
    PostsAdapter postsAdapter;

    //for infinite loading
    boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        productsManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        postsManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        ButterKnife.bind(this, v);

        mRefresh.setColorSchemeResources(android.R.color.holo_red_light,android.R.color.holo_green_light,android.R.color.holo_blue_light,android.R.color.holo_orange_light);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.home_fragment,new HomeFragment()).commit();
                mRefresh.setRefreshing(false);
            }
        });

        productsRecycler.setHasFixedSize(true);
        productsRecycler.setNestedScrollingEnabled(true);
        postsRecycler.setHasFixedSize(true);
        postsRecycler.setNestedScrollingEnabled(false);

        loadProducts();
        loadPosts();

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductsFragment newFragment = new ProductsFragment();
                Bundle b = new Bundle();
                b.putInt("callerId", R.id.home_fragment);
                newFragment.setArguments(b);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.home_fragment, newFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        postsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = productsManager.getChildCount();
                    totalItemCount = productsManager.getItemCount();
                    pastVisiblesItems = productsManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && loading) {
                        loading = false;
                        posts.add(null);
                        postsAdapter.notifyItemChanged(posts.size());
                        //Do pagination.. i.e. fetch new data
                        postsData.nextPage(new AsyncCallback<BackendlessCollection<Posts>>() {

                            final Object[] followedUsers = (Object[]) BackgroundService.getService().getUser().getProperty("followings");

                            @Override
                            public void handleResponse(BackendlessCollection<Posts> response) {
                                List<Posts> followersData = new LinkedList<>();
                                posts.remove(posts.size() - 1);

                                for (Posts m : response.getData()) {

                                    for (Object followedUser : followedUsers) {
                                        BackendlessUser x = (BackendlessUser) followedUser;
                                        if (x.getObjectId().equals(m.getPostedUser().getObjectId())) {
                                            followersData.add(m);
                                            break;
                                        }
                                    }
                                    if(m.getPostedUser().getObjectId().equals(BackgroundService.getService().getUser().getObjectId())
                                            || m.getPostedUser().getProperty("username").equals("reflektt")){
                                        followersData.add(m);
                                    }
                                }
                                if (followersData.size() != 0)
                                    if (posts.get(posts.size() - 1).equals(followersData.get(followersData.size() - 1))) {
                                        loading = true;
                                        posts.addAll(followersData);
                                        postsAdapter.notifyDataSetChanged();
                                    }

                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {

                            }
                        });
                    }
                }
            }
        });
        Object[] x = (Object[]) BackgroundService.getService().getUser().getProperty("followings");
        if (x.length < 10){
            BackendlessUser[] xUsers = new BackendlessUser[0];
            if(x.length!=0)
                xUsers = (BackendlessUser[]) x;
            suggestUsers.setVisibility(View.VISIBLE);
            usersRecycler.setHasFixedSize(true);
            usersRecycler.setNestedScrollingEnabled(false);
            usersRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            BackendlessDataQuery query = new BackendlessDataQuery();
            query.setOffset((int) (Math.random() * 3));
            query.setPageSize(7);
            final BackendlessUser[] finalXUsers = xUsers;
            Backendless.Persistence.of(BackendlessUser.class).find(query, new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
                @Override
                public void handleResponse(BackendlessCollection<BackendlessUser> response) {
                    loadUsers.setVisibility(View.GONE);
                    List<BackendlessUser> list=new LinkedList<>();
                    for (BackendlessUser x:response.getData()) {
                        boolean isFollowed=false;
                        if (x.getObjectId().equals(BackgroundService.getService().getUser().getObjectId())) continue;
                        for (BackendlessUser xyz: finalXUsers) {
                            if(xyz.getObjectId().equals(x.getObjectId())){
                                isFollowed = true;
                                break;
                            }
                        }
                        if(!isFollowed && x.getProperty("username")!= null) list.add(x);
                    }
                    if(list.size() > 0)
                        usersRecycler.setAdapter(new UsersAdapter(list));
                    else
                        suggestUsers.setVisibility(View.GONE);
                }

                @Override
                public void handleFault(BackendlessFault fault) {

                }
            });
        }
        return v;
    }

    public void loadPosts() {
        //Sorting posts by their last created time
        loadPosts.setVisibility(View.VISIBLE);
        queryOptions = new QueryOptions();
        final Object[] followedUsers = (Object[]) BackgroundService.getService().getUser().getProperty("followings");
        sortBy = new LinkedList<>();
        sortBy.add("created DESC");
        queryOptions.setSortBy(sortBy);
        queryOptions.setPageSize(30);

        Backendless.Data.of(Posts.class).find(new BackendlessDataQuery(queryOptions), new AsyncCallback<BackendlessCollection<Posts>>() {
            @Override
            public void handleResponse(BackendlessCollection<Posts> response) {
                List<Posts> data = response.getCurrentPage();
                posts = new LinkedList<>();
                if (data.size() != 0) {
                    loadPosts.setVisibility(View.GONE);
                    // take only the posts of followers

                    for (Posts m : data) {
                        BackendlessUser user = m.getPostedUser();
                        if (followedUsers.length != 0) {
                            for (Object followedUser : followedUsers) {
                                BackendlessUser x = (BackendlessUser) followedUser;
                                if (x.getObjectId().equals(user.getObjectId())) {
                                    posts.add(m);
                                    break;
                                }
                            }
                        }
                        if(user.getObjectId().equals(BackgroundService.getService().getUser().getObjectId())
                                || user.getProperty("username").equals("reflektt")){
                            posts.add(m);
                        }
                    }
                    postsAdapter = new PostsAdapter(getActivity(), posts, HomeFragment.this, R.id.home_fragment);
                    postsData = response;
                    //send the followers data to adapter to load the posts

                    postsRecycler.setAdapter(postsAdapter);
                    postsRecycler.setLayoutManager(postsManager);
                } else {
                    postsRecycler.setVisibility(View.INVISIBLE);
                    postsErrorText.setVisibility(View.VISIBLE);
                    loadPosts.setVisibility(View.GONE);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                postsRecycler.setVisibility(View.INVISIBLE);
                postsErrorText.setVisibility(View.VISIBLE);
                loadPosts.setVisibility(View.GONE);
            }
        });
    }

    public void loadProducts() {
        //Sorting products by their last updated time
        loadProducts.setVisibility(View.VISIBLE);
        sortBy.add("updated DESC");
        queryOptions.setSortBy(sortBy);
        //paging the result to 10 products (make backendless return only the last 10 updated products)
        queryOptions.setPageSize(10);
        queryOptions.setOffset((int) (Math.random() * 10));
        Backendless.Data.of(Products.class).find(new BackendlessDataQuery(queryOptions), new AsyncCallback<BackendlessCollection<Products>>() {
            @Override
            public void handleResponse(BackendlessCollection<Products> response) {
                data = response.getData();
                productsData = response;
                productAdapter = new ProductAdapter(getActivity(), data, HomeFragment.this, R.id.home_fragment);
                productsRecycler.setAdapter(productAdapter);
                productsRecycler.setLayoutManager(productsManager);
                loadProducts.setVisibility(View.GONE);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                productsRecycler.setVisibility(View.INVISIBLE);
                productsErrorText.setVisibility(View.VISIBLE);
                loadProducts.setVisibility(View.GONE);
            }
        });
    }

    class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserVH>{
        List<BackendlessUser> data;
        LayoutInflater inflater;

        UsersAdapter(List<BackendlessUser> collection) {
            data=collection;
            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public UserVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.users_item, parent, false);
            return new UserVH(view);
        }

        @Override
        public void onBindViewHolder(UserVH holder, int position) {
            Picasso.with(getActivity())
                    .load(Constants.PROFILE_PATH + data.get(position).getProperty("username") + ".jpg")
                    .into(holder.profilePic);
            holder.userName.setText((CharSequence) data.get(position).getProperty("username"));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class UserVH extends RecyclerView.ViewHolder{
            @BindView(R.id.profile_pic)CircularImageView profilePic;
            @BindView(R.id.username)TextView userName;
            @BindView(R.id.follow)Button follow;
            UserVH(View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String userId = data.get(getAdapterPosition()).getObjectId();
                        Fragment newFragment = new ProfileFragment();
                        Bundle b = new Bundle();
                        b.putString("id",userId);
                        newFragment.setArguments(b);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.home_fragment, newFragment);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });
                follow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        follow.setEnabled(false);
                        follow.setText(R.string.processing);
                        follow.setBackgroundColor(Color.GRAY);
                        follow.setTextColor(Color.BLACK);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (BackgroundService.getService().follow(data.get(getAdapterPosition()).getObjectId()))
                                    data.remove(getAdapterPosition());
                                else
                                    Toast.makeText(getContext(),R.string.error,Toast.LENGTH_SHORT).show();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        usersRecycler.getAdapter().notifyDataSetChanged();
                                    }
                                });

                            }
                        }).start();

                    }
                });
            }
        }
    }
}
