package com.reflektt.reflektt.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.reflektt.reflektt.Adapters.CommentsAdapter;
import com.reflektt.reflektt.BackgroundService;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.Tables.Comments;
import com.reflektt.reflektt.Tables.Posts;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentsFragment extends Fragment {
    @BindView(R.id.comments_view)RecyclerView commentsRecycler;
    @BindView(R.id.comment)EditText commentText;
    @BindView(R.id.sendComment)ImageView send;
    @BindView(R.id.refresh_comment)SwipeRefreshLayout mRefresh;
    private Posts post;
    private List<Comments> data;
    private CommentsAdapter adapter;
    private int callerId;
    public CommentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_comments, container, false);
        ButterKnife.bind(this,v);

        mRefresh.setColorSchemeResources(android.R.color.holo_red_light,android.R.color.holo_green_light,android.R.color.holo_blue_light,android.R.color.holo_orange_light);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setAllowOptimization(false);
                CommentsFragment t = new CommentsFragment();
                t.setData(data);
                t.setPost(post);
                ft.replace(callerId,t).commitAllowingStateLoss();
                mRefresh.setRefreshing(false);
            }
        });
        callerId = getArguments().getInt("callerId");
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Comments newComment = new Comments();
                send.setEnabled(false);
                if(commentText.getText() == null) return;
                newComment.setComment(commentText.getText().toString());
                newComment.setCommentedUser(BackgroundService.getService().getUser());
                commentText.setEnabled(false);
                data.add(newComment);
                adapter.notifyDataSetChanged();
                Backendless.Persistence.of(Posts.class).findById(post, new AsyncCallback<Posts>() {
                    @Override
                    public void handleResponse(Posts response) {
                        LinkedList<Comments> comments = response.getComments();
                        comments.add(newComment);
                        response.setComments(comments);
                        Backendless.Data.of(Posts.class).save(response, new AsyncCallback<Posts>() {
                            @Override
                            public void handleResponse(Posts response) {
                                commentText.setEnabled(true);
                                send.setEnabled(true);
                                commentText.setText("");
                                data = response.getComments();
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {

                            }
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getActivity(),getResources().getString(R.string.error),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        LinearLayoutManager m = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        commentsRecycler.setHasFixedSize(true);
        commentsRecycler.setNestedScrollingEnabled(true);
        adapter =new CommentsAdapter(getActivity(), data, this, getArguments().getInt("callerId"));
        commentsRecycler.setAdapter(adapter);
        commentsRecycler.setLayoutManager(m);
        return v;
    }

    public void setPost(Posts post) {
        this.post = post;
    }

    public void setData(List<Comments> data) {
        this.data = data;
    }
}
