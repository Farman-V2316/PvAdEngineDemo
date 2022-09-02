package com.newsdistill.pvadenginedemo.dummydata.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.newsdistill.pvadenginedemo.R;
import com.newsdistill.pvadenginedemo.dummydata.FetchDummyData;
import com.newsdistill.pvadenginedemo.dummydata.adapter.FeedAdapter;
import com.newsdistill.pvadenginedemo.model.CommunityPost;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_layout, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        initRV();
        return view;
    }

    private void initRV() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        new PagerSnapHelper().attachToRecyclerView(recyclerView);
        FeedAdapter feedAdapter = new FeedAdapter(getActivity(), getData(), "home");
        recyclerView.setAdapter(feedAdapter);
    }

    private List<Object> getData() {
        FetchDummyData dummyData = new FetchDummyData(getActivity(), "dummy_feed.json");
        List<CommunityPost> communityPosts = dummyData.getDummyData();
        List<Object> posts = new ArrayList<>();
        posts.addAll(communityPosts);
        return posts;
    }
}
