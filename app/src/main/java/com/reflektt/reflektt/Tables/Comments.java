package com.reflektt.reflektt.Tables;

import com.backendless.BackendlessUser;

/**
 * Created by Shiko on 08/03/2017.
 */

public class Comments{
    private BackendlessUser commentedUser;
    private String comment;

    public Comments(){

    }
    public BackendlessUser getCommentedUser() {
        return commentedUser;
    }

    public void setCommentedUser(BackendlessUser commentedUser) {
        this.commentedUser = commentedUser;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
