package com.newsdistill.pvadenginedemo.dummydata.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.newsdistill.pvadenginedemo.R;
import com.newsdistill.pvadenginedemo.dummydata.viewholders.AdViewHolder;
import com.newsdistill.pvadenginedemo.dummydata.viewholders.BasicCardViewHolder;
import com.newsdistill.pvadenginedemo.model.Ad;
import com.newsdistill.pvadenginedemo.model.CommunityPost;

import java.util.ArrayList;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private List<Object> posts = new ArrayList<>();
    private Activity context;
    private String pageName;
    private static final int TYPE_FEED = 1;
    private static final int TYPE_AD = 2;

    public FeedAdapter(Activity context, List<Object> posts, String pageName) {
        this.context = context;
        this.posts = posts;
        this.pageName = pageName;
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = posts.get(position);
        if (obj != null) {
            if (obj instanceof CommunityPost) {
                return TYPE_FEED;
            }

            if (obj instanceof Ad) {
                return TYPE_AD;
            }
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FEED) {
            View view = LayoutInflater.from(context).inflate(R.layout.feed_item_layout, parent, false);
            return new BasicCardViewHolder(context, view, pageName);
        }

        if (viewType == TYPE_AD) {
            View view = LayoutInflater.from(context).inflate(R.layout.ad_layout, parent, false);
            return new AdViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object obj = posts.get(position);
        if (holder != null) {
            if (holder instanceof BasicCardViewHolder) {
                CommunityPost post = (CommunityPost) obj;
                ((BasicCardViewHolder)holder).bind(post);
            }

            if (holder instanceof AdViewHolder) {
                int adRequestID = (position > 2) ? 111 : 100;
                ((AdViewHolder)holder).bind(context, position, adRequestID);
            }
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public void setPosts(List<Object> posts) {
        this.posts = posts;
    }

    public List<Object> getPosts(){
        return posts;
    }

}
