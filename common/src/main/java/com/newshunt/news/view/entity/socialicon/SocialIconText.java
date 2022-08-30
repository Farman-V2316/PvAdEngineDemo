package com.newshunt.news.view.entity.socialicon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.IntegerRes;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.newshunt.common.util.R;
import com.newshunt.common.view.customview.fontview.NHTextView;

/**
 * @Author Rahul Ravindra
 */

public class SocialIconText extends LinearLayout {
  private ImageView socialIcon;
  private NHTextView socialText;


  public SocialIconText(final Context context) {
    this(context, null);
  }

  public SocialIconText(final Context context,
                        final @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SocialIconText(final Context context,
                        final @Nullable AttributeSet attrs,
                        final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    AttributeContainer attributeContainer = new AttributeContainer(context, attrs);
    if (!isInEditMode()) {
      setup(attributeContainer);
    }
  }

  private void setup(final AttributeContainer container) {
    inflateIcon(container);
    if (container.textEnabled) {
      inflateTextView(container);
    }

    //default horizontal
    setOrientation(HORIZONTAL);
    setGravity(Gravity.CENTER);
  }

  private void inflateIcon(final AttributeContainer container) {
    socialIcon = new ImageView(getContext());
    if (container.icon != -1) {
      socialIcon.setImageResource(container.icon);
    }

    //layoutparams
    LayoutParams params =
        new LayoutParams((int) container.socialIconWidth, (int) container.socialIconHeight);
    socialIcon.setLayoutParams(params);

    addView(socialIcon);
  }

  private void inflateTextView(final AttributeContainer container) {
    socialText = new NHTextView(getContext());
    socialText.setText(container.defaultText);

    socialText.setTextColor(container.textColor);
    socialText.setTextSize(getResources().getDimension(
        R.dimen.viral_social_icon_text_size));

    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, 0);
    socialText.setLayoutParams(params);

    addView(socialText);
  }

  public void setSocialText(final String status) {
    if (socialText != null) {
      socialText.setText(status);
    }
  }

  @SuppressLint("ResourceType")
  public void setSocialIcon(final @IntegerRes int socialIcon) {
    if (this.socialIcon != null) {
      this.socialIcon.setImageResource(socialIcon);
    }
  }

  protected class AttributeContainer {
    private float socialIconWidth;
    private float socialIconHeight;
    private boolean textEnabled;
    private int icon;
    private String defaultText;
    private int textColor;

    public AttributeContainer(final Context context, final AttributeSet attributeSet) {
      if (attributeSet == null) {
        return;
      }
      TypedArray typedArray =
          context.obtainStyledAttributes(attributeSet,
              R.styleable.SocialIconText);
      try {
        if (typedArray == null) {
          return;
        }

        socialIconWidth =
            typedArray.getDimension(R.styleable.SocialIconText_si_icon_width,
                LayoutParams.WRAP_CONTENT);

        socialIconHeight =
            typedArray.getDimension(R.styleable.SocialIconText_si_icon_height,
                LayoutParams.WRAP_CONTENT);

        textEnabled =
            typedArray.getBoolean(R.styleable.SocialIconText_si_text_enable,
                false);

        icon = typedArray.getResourceId(R.styleable.SocialIconText_si_icon, -1);
        defaultText =
            typedArray.getString(R.styleable.SocialIconText_si_default_text);

        textColor =
            typedArray.getColor(R.styleable.SocialIconText_si_text_color,
                0);
      } finally {
        typedArray.recycle();
      }
    }
  }
}
