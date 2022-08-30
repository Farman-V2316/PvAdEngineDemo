/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view.viewholder;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.dataentity.news.model.entity.Counts;
import com.newshunt.dataentity.news.model.entity.EntityConfig;
import com.newshunt.dataentity.social.entity.LikeType;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.view.listener.LikeEmojiListener;
import com.newshunt.dhutil.view.listener.LikeEmojiPopupDismissListener;

/**
 * View holder to display each item of an Emoji list
 *
 * @author umesh.isran
 */
/*
public class LikeEmojiViewHolder extends RecyclerView.ViewHolder {
  private AppCompatImageView imgView;
  private NHTextView emoticon_caption;
  private LikeType[] emojisEnum;
  private Counts counts;

  public LikeEmojiViewHolder(final View itemView,
                             final LikeEmojiListener likeEmojiListener,
                             final LikeEmojiPopupDismissListener likePopupDismissListener,
                             Counts counts) {
    super(itemView);
    this.emojisEnum = LikeType.values();
    this.counts = counts;

    imgView = (AppCompatImageView) itemView.findViewById(R.id.emoticon);
    emoticon_caption = (NHTextView) itemView.findViewById(R.id.emoticon_caption);

    View parentView = itemView.findViewById(R.id.ll_emoji);
    parentView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (likeEmojiListener != null) {
          likeEmojiListener.onLikeEmojiSelected(emojisEnum[getAdapterPosition()]);
        }
        if (likePopupDismissListener != null) {
          likePopupDismissListener.dismissPopup();
        }
      }
    });
  }

  public void bindViewHolder(final LikeEmojiViewHolder tvEmojiViewHolder, final int position) {
    if (tvEmojiViewHolder == null) {
      return;
    }
    switch (emojisEnum[position]) {
      case LIKE:
        imgView.setImageResource(R.drawable.ic_emoji_like);
        if (counts != null) {
          setValue(counts.getLIKE());
        } else {
          emoticon_caption.setText("0");
        }
        break;
      case LOVE:
        imgView.setImageResource(R.drawable.ic_emoji_love);
        if (counts != null) {
          setValue(counts.getLOVE());
        } else {
          emoticon_caption.setText("0");
        }
        break;
      case HAPPY:
        imgView.setImageResource(R.drawable.ic_emoji_haha);
        if (counts != null) {
          setValue(counts.getSMILE());
        } else {
          emoticon_caption.setText("0");
        }
        break;
      case SAD:
        imgView.setImageResource(R.drawable.ic_emoji_sad);
        if (counts != null) {
          setValue(counts.getSAD());
        } else {
          emoticon_caption.setText("0");
        }
        break;
      case WOW:
        imgView.setImageResource(R.drawable.ic_emoji_wow);
        if (counts != null) {
          setValue(counts.getWOW());
        } else {
          emoticon_caption.setText("0");
        }
        break;
      case ANGRY:
        imgView.setImageResource(R.drawable.ic_emoji_angry);
        if (counts != null) {
          setValue(counts.getANGRY());
        } else {
          emoticon_caption.setText("0");
        }
        break;
      default:
        imgView.setImageResource(R.drawable.ic_emoji_like);
        if (counts != null) {
          setValue(counts.getLIKE());
        } else {
          emoticon_caption.setText("0");
        }
    }
  }

  private void setValue(EntityConfig config) {
    String value = "0";
    if (config != null && !CommonUtils.isEmpty(config.getValue())) {
      value = config.getValue();
    }
    emoticon_caption.setText(value);
  }


}
*/
