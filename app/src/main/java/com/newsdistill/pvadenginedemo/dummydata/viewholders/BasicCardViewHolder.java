package com.newsdistill.pvadenginedemo.dummydata.viewholders;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.newsdistill.pvadenginedemo.R;
import com.newsdistill.pvadenginedemo.dummydata.util.DisplayUtils;
import com.newsdistill.pvadenginedemo.model.CommunityPost;

public class BasicCardViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = BasicCardViewHolder.class.getSimpleName();
    private Context context;
    private String pageName;

    private ImageView imageView;
    private TextView titleView;

    public BasicCardViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public BasicCardViewHolder(Context context, View view, String pageName) {
        super(view);
        this.context = context;
        this.pageName = pageName;
        imageView = view.findViewById(R.id.imageView);
        titleView = view.findViewById(R.id.titleView);
    }

    public void bind(CommunityPost post) {
        appendImageView(post);
        titleView.setText(post.getTitle());
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