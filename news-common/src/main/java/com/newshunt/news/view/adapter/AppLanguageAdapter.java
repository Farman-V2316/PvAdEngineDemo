/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.adapter;

import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener;
import com.newshunt.dataentity.common.model.entity.language.Language;
import com.newshunt.news.common.R;

import java.util.List;

/**
 * Adapter to set languages to fragment
 *
 * @author datta.vitore
 */
public class AppLanguageAdapter
    extends RecyclerView.Adapter<AppLanguageAdapter.Holder> {

  private List<Language> languages;
  private RecyclerViewOnItemClickListener viewOnItemClickListener;

  public AppLanguageAdapter(List<Language> languages,
                            RecyclerViewOnItemClickListener viewOnItemClickListener) {

    this.languages = languages;
    this.viewOnItemClickListener = viewOnItemClickListener;
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new Holder(createNewsView(parent));
  }

  private View createNewsView(ViewGroup parent) {
    return LayoutInflater.from(parent.getContext())
        .inflate(R.layout.onboarding_choose_language_list_item, parent, false);
  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    holder.onboardingLanguageText.setText(
        (languages.get(position).getLangUni()));
  }

  @Override
  public int getItemCount() {
    return languages.size();
  }

  public List<Language> getLanguages() {
    return languages;
  }

  /**
   * Hold views for a Language.
   */
  public class Holder extends RecyclerView.ViewHolder {
    private TextView onboardingLanguageText;

    public Holder(View view) {
      super(view);

      onboardingLanguageText = (TextView) view.findViewById(R.id.onboarding_language_text);

      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          viewOnItemClickListener.onItemClick(new Intent(), getPosition());
        }
      });
    }
  }


}
