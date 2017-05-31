package com.reflektt.reflektt.HomeFragments.AddPost;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.reflektt.reflektt.BackgroundService;
import com.reflektt.reflektt.Constants;
import com.reflektt.reflektt.HomeActivity;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.Tables.Posts;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AddPost_Phase2 extends Fragment {
    @BindView(R.id.post_btn)
    Button postButton;
    @BindView(R.id.post)
    EditText postText;
    @BindView(R.id.post_pic)
    ImageView postPicture;

    private String selectedPath;

    public AddPost_Phase2() {
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
        View v = inflater.inflate(R.layout.fragment_add_post2, container, false);
        ButterKnife.bind(this, v);
        //Load chosen picture
        Uri uri = (Uri) getArguments().get("pictureUri");
        try {
            postPicture.setImageBitmap(MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri));
            selectedPath = uri.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog pDialog = new ProgressDialog(getContext());
                pDialog.setMessage("Posting..");
                pDialog.show();

                Posts post = new Posts();
                post.setPostedUser(BackgroundService.getService().getUser());
                post.setPost(postText.getText().toString());
                post.setPictureName(selectedPath.substring(selectedPath.lastIndexOf('/')+1));

                Intent intent = new Intent(getContext(),BackgroundService.class);
                intent.setAction(Constants.ACTION_UPLOAD);
                intent.putExtra("path",selectedPath);

                getContext().startService(intent);
                Backendless.Persistence.of(Posts.class).save(post, new AsyncCallback<Posts>() {
                    @Override
                    public void handleResponse(Posts response) {
                        BackgroundService.getService().add_post(response);
                        Backendless.Persistence.of(BackendlessUser.class).save(BackgroundService.getService().getUser(),
                                new AsyncCallback<BackendlessUser>() {
                                    @Override
                                    public void handleResponse(BackendlessUser response) {
                                        pDialog.dismiss();
                                        ((HomeActivity) getActivity()).loadedPost();
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
        return v;
    }


}
