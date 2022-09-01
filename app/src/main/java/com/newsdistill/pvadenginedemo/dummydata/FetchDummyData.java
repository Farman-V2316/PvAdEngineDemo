package com.newsdistill.pvadenginedemo.dummydata;

import android.content.Context;

import com.google.gson.Gson;
import com.newsdistill.pvadenginedemo.model.CommunityPost;
import com.newsdistill.pvadenginedemo.model.PostResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FetchDummyData {

    private Context context;
    private String fileName;
    public FetchDummyData(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    private String fetchData() {
        String jsonData = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonData = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return "";
        }
        return jsonData;
    }

    public List<CommunityPost> getDummyData(){
        String postResponseJson = fetchData();
        PostResponse postResponse = new Gson().fromJson(postResponseJson, PostResponse.class);
        return postResponse.getPosts();
    }
}
