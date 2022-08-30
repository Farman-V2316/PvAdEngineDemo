/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.common.view.customview.fontview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.transition.Transition;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.ViewUtils;
import com.newshunt.dhutil.R;
import com.newshunt.sdk.network.image.Image;

/**
 * Created by anshul on 16/11/17.
 * <p>
 * A custom view class for holding various tags.
 */

public class TagTextView extends RelativeLayout {

  private int tagViewStrokeSize;
  private float radius;
  private String text, imageUrl;
  private ImageView tagViewImage, tagViewRightImage, tagToggleImage;
  private TextView tagViewText;
  private GradientDrawable gradientDrawable;
  private int typeface = Typeface.NORMAL;
  private boolean showRightImage, showToggleImage;
  private int tagTextColor, backgroundColor, tagViewStrokeColor;

  public TagTextView(Context context) {
    super(context);
    init(context, null);
  }

  public TagTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public TagTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {

    tagViewStrokeSize = CommonUtils.getDimension(R.dimen.tag_view_default_border_size);

    int[] ATTRS = new int[]{android.R.attr.textStyle};
    TypedArray typedArray = context.obtainStyledAttributes(attrs, ATTRS);
    typeface = typedArray.getInt(0, Typeface.NORMAL);

    ATTRS = new int[]{android.R.attr.text};
    typedArray = context.obtainStyledAttributes(attrs, ATTRS);
    text = typedArray.getString(0);

    ATTRS = new int[]{android.R.attr.background};
    typedArray = context.obtainStyledAttributes(attrs, ATTRS);
    String backgroundColorStr = typedArray.getString(0);
    backgroundColor = ViewUtils.getColor(backgroundColorStr, Color.WHITE);

    typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagTextView);
    String tagTextColorStr = typedArray.getString(R.styleable.TagTextView_textColor);
    tagTextColor = ViewUtils.getColor(tagTextColorStr, Color.BLACK);

    typedArray.recycle();
    prepare();
  }

  public void prepare() {
    initView();
    updateView();
  }

  private void initView() {
    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.layout_tag_view, this, true);

    tagViewImage = view.findViewById(R.id.tag_view_image);
    tagViewText = view.findViewById(R.id.tag_view_text);
    tagViewRightImage = view.findViewById(R.id.tag_view_right_image);
    tagViewRightImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    tagToggleImage = view.findViewById(R.id.tag_view_toggle_image);
    tagToggleImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    if (typeface == Typeface.BOLD) {
      tagViewText.setTypeface(null, Typeface.BOLD);
    }

//    ViewCompat.setElevation(this, 10);
  }

  /**
   * Render the view with updated properties.
   */
  public void updateView() {
    tagViewText.setText(text);
    tagViewText.setTextColor(tagTextColor);

    if (showRightImage) {
      tagViewRightImage.setVisibility(VISIBLE);
    } else {
      tagViewRightImage.setVisibility(GONE);
    }

    initBackgroundColor();

    if (CommonUtils.isEmpty(imageUrl)) {
      tagViewImage.setVisibility(GONE);
      return;
    } else {
      tagViewImage.setVisibility(VISIBLE);
    }

    ViewCompat.setElevation(this, 10);
  }

  private void initBackgroundColor() {
    if (gradientDrawable == null) {
      gradientDrawable = new GradientDrawable();
    }
    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    if (radius == 0.0f) {
      radius = CommonUtils.getDimension(R.dimen.tag_view_default_radius);
    }
    gradientDrawable.setCornerRadii(new float[]{
        radius, radius, radius, radius,
        radius, radius, radius, radius
    });

    gradientDrawable.setColor(backgroundColor);
    gradientDrawable.setStroke(tagViewStrokeSize, tagViewStrokeColor);
    setBackground(gradientDrawable);
  }

  public void setRadius(float radius) {
    this.radius = radius;
  }

  public void setText(String text) {
    this.text = text;
  }

  //text should be bold only in viral header
  public void setBold(){
    tagViewText.setTypeface(null, Typeface.BOLD);
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
    if (DataUtil.isEmpty(imageUrl)) {
      return;
    }

    Image.load(imageUrl, true).into(new Image.ImageTarget() {

      @Override
      public void onResourceReady(@NonNull Object resource, @Nullable Transition transition) {
        if (!(resource instanceof Bitmap)) {
          return;
        }

        LayoutParams layoutParams = (LayoutParams) tagViewImage.getLayoutParams();
        int leftMargin = CommonUtils.getDimension(R.dimen.tag_image_margin_left);
        layoutParams.setMargins(leftMargin, 0, 0, 0);
        tagViewImage.setLayoutParams(layoutParams);
        tagViewImage.setImageBitmap((Bitmap) resource);
      }
    });
  }

  public void setBackgroundColor(int backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public void setTagTextColor(int tagTextColor) {
    this.tagTextColor = tagTextColor;
  }

  public void showRightImage(boolean showRightImage) {
    this.showRightImage = showRightImage;
  }

  public void setMaxTextWidth(int maxTextWidth) {
    tagViewText.setEllipsize(TextUtils.TruncateAt.END);
    tagViewText.setMaxWidth(maxTextWidth);
    tagViewText.setLines(1);
  }

  public void setTagViewStrokeColor(int tagViewStrokeColor) {
    this.tagViewStrokeColor = tagViewStrokeColor;
  }

  public void setToggleImageResource(int resId) {
    tagToggleImage.setImageResource(resId);
  }

  public void showToggleImage(boolean showToggleImage) {
    if (this.showToggleImage == showToggleImage) {
      return;
    }
    tagToggleImage.setVisibility(showToggleImage ? VISIBLE : GONE);
    this.showToggleImage = showToggleImage;
  }

  public void setTagViewStrokeSize(int tagViewStrokeSize) {
    this.tagViewStrokeSize = tagViewStrokeSize;
  }
}