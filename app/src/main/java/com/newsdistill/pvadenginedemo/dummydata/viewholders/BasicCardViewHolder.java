package com.newsdistill.pvadenginedemo.dummydata.viewholders;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.newsdistill.pvadenginedemo.R;
import com.newsdistill.pvadenginedemo.ads.HomeFeedAdHandler;
import com.newsdistill.pvadenginedemo.dummydata.util.DisplayUtils;
import com.newsdistill.pvadenginedemo.model.CommunityPost;
import com.newshunt.adengine.model.entity.NativeAdContainer;
import com.newshunt.adengine.model.entity.version.AdPosition;
import com.squareup.otto.Subscribe;

public class BasicCardViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = BasicCardViewHolder.class.getSimpleName();
    private Activity context;
    private String pageName;

    private ImageView imageView;
    private TextView titleView;
    private RelativeLayout adContainer;
    private HomeFeedAdHandler homeFeedAdHandler;
    private LifecycleOwner lifecycleOwner;

    public BasicCardViewHolder(Activity context, @NonNull View view, String pageName, LifecycleOwner viewLifecycleOwner) {
        super(view);
        this.context = context;
        this.pageName = pageName;
        this.lifecycleOwner = viewLifecycleOwner;
        imageView = view.findViewById(R.id.imageView);
        titleView = view.findViewById(R.id.titleView);
        adContainer = view.findViewById(R.id.home_ad_layout);
        homeFeedAdHandler = new HomeFeedAdHandler(lifecycleOwner);

    }

    public void bind(CommunityPost post, int position) {
        appendImageView(post);
        titleView.setText(post.getTitle());
        Log.d("panda", "binding a feed ad for post id : " + post.getPostId());
        String adZoneType = getZoneAdType(position);
        addFeedAd(post, adZoneType);
    }

    private String getZoneAdType(int position) {
        switch (position) {
            case 1:
            case 5:
            case 9:

            case 3:
            case 7 : return "PGI_IMAGE";

            default: return "";
        }
    }

    private void addFeedAd(CommunityPost post, String adZoneType) {
        homeFeedAdHandler.loadHomeFeedAd(AdPosition.PGI, adZoneType);
    }

    @Subscribe
    public void setAdResponse(NativeAdContainer nativeAdContainer) {
        Log.d( "panda:", " setAdResponse-------------------> $nativeAdContainer");
        if (nativeAdContainer.getUniqueRequestId() != homeFeedAdHandler.getAdRequestID())
            return;

        adContainer  = itemView.findViewById(R.id.home_ad_layout);
        homeFeedAdHandler.insertAd(context, nativeAdContainer, adContainer);
    }

    private void appendImageView(CommunityPost post) {
        int height = (int) getImageHeight(post.getImageUrl(), pageName.equals("shorts"));
        DisplayUtils displayUtils = DisplayUtils.getInstance();
        double cardDefHeight = (int) displayUtils.getDefVideoCardImageHeight();

        int imageHeight = Math.max((int) Math.ceil(cardDefHeight), height);
        imageView.getLayoutParams().height = imageHeight;
        try {
            Glide.with(imageView).load(post.getImageUrl())
                    .into(imageView);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static float getImageHeight(String imageUrl, boolean isFullScreen) {
        float defaultHeight = (int) (DisplayUtils.getInstance().getWidthPx() * 0.5625);
        float aspectRatio = 0;
        if (TextUtils.isEmpty(imageUrl)) {
            return defaultHeight;
        }
        int indexPos = imageUrl.lastIndexOf("_");
        if (indexPos <= 0) {
            return defaultHeight;
        }

        float cardHeight = 0;
        String dimensions = imageUrl.substring(indexPos + 1);
        if (TextUtils.isEmpty(dimensions)) {
            return defaultHeight;
        }
        boolean hasDimensions = dimensions.matches("[\\d]{2,4}x[\\d]{2,4}(.){3,6}");

        if (hasDimensions) {
            String heightWidth[] = dimensions.split("x");
            if (heightWidth.length == 2) {
                try {
                    final float height = Float.parseFloat(heightWidth[0]);
                    final float width = Float.parseFloat(heightWidth[1].replaceAll(".jpg", ""));
                    aspectRatio = height / width;
                    cardHeight = getRequiredCardHeight(aspectRatio);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (cardHeight == 0) {
            return defaultHeight;
        }

        if (isFullScreen) {
            return cardHeight;
        } else if (aspectRatio <= 1) {
            return Math.min(cardHeight, (int) (DisplayUtils.getInstance().getWidthPx() * 0.5625));
        } else if (aspectRatio > 1) {
            return Math.min(cardHeight, (int) (DisplayUtils.getInstance().getWidthPx() * 0.75));
        } else {
            return Math.min(cardHeight, defaultHeight);
        }
    }

    private static float getRequiredCardHeight(float aspectRatio) {
        DisplayUtils displayUtils = DisplayUtils.getInstance();

        if (displayUtils.getHeightPx() == 0) {
            return 0;
        }

        float screenHeight = (float) displayUtils.getHeightPx() / displayUtils.getScaleDensity();
        float margin = screenHeight - 55 - 20; // bottom navigation, status, additional space
        float maxHeightFactor = margin / screenHeight;
        float maxHeight = (float) (displayUtils.getHeightPx() * maxHeightFactor);

        float height = displayUtils.getWidthPx() * aspectRatio;
        if (height > maxHeight) {
            height = maxHeight;
        }
        return height;
    }
}