package com.newsdistill.pvadenginedemo.dummydata.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.newsdistill.pvadenginedemo.R;
import com.newsdistill.pvadenginedemo.dummydata.FetchDummyData;
import com.newsdistill.pvadenginedemo.dummydata.adapter.FeedAdapter;
import com.newsdistill.pvadenginedemo.dummydata.util.LinearManager;
import com.newsdistill.pvadenginedemo.dummydata.util.OnViewPagerListener;
import com.newsdistill.pvadenginedemo.model.CommunityPost;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_layout, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        FetchDummyData dummyData = new FetchDummyData(getActivity(), "dummy_feed.json");
        List<CommunityPost> communityPosts = dummyData.getDummyData();
        List<Object> posts = new ArrayList<>();
        posts.addAll(communityPosts);

        LinearManager manager = new LinearManager(getActivity(), LinearLayout.VERTICAL, false);
        manager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onPageRelease(boolean isNext, int position) {
            }
            @Override
            public void onPageSelected(int position, boolean bottom) {
            }
        });
        recyclerView.setLayoutManager(manager);
        FeedAdapter feedAdapter = new FeedAdapter(getActivity(), posts, "home");
        recyclerView.setAdapter(feedAdapter);

        return view;
    }
}
