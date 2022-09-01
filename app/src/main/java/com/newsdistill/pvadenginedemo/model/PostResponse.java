package com.newsdistill.pvadenginedemo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PostResponse {

    @SerializedName("posts")
    @Expose
    private List<CommunityPost> posts = new ArrayList<>();

    public List<CommunityPost> getPosts() {
        return posts;
    }

    public void setPosts(List<CommunityPost> posts) {
        this.posts = posts;
    }

}
