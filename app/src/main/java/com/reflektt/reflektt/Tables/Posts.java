package com.reflektt.reflektt.Tables;

import com.backendless.BackendlessUser;

import java.util.LinkedList;

/**
 * Created by Shiko on 08/03/2017.
 */

public class Posts {
    private BackendlessUser postedUser;
    private String post;
    private LinkedList<Comments> comments;
    private BackendlessUser[] likers;
    private String pictureName;
    private String objectId;

    public Posts() {

    }

    public BackendlessUser getPostedUser() {
        return postedUser;
    }

    public void setPostedUser(BackendlessUser postedUser) {
        this.postedUser = postedUser;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public LinkedList<Comments> getComments() {
        return comments;
    }

    public void setComments(LinkedList<Comments> comments) {
        this.comments = comments;
    }

    public BackendlessUser[] getLikers() {
        return likers;
    }

    public void setLikers(BackendlessUser[] likers) {
        this.likers = likers;
    }

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public boolean equals(Posts i) {
        if (post.equals(i.post)
                && postedUser.getObjectId().equals(i.postedUser.getObjectId())) return true;
        return false;
    }



}
