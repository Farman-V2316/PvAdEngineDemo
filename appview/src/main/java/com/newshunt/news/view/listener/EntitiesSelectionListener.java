package com.newshunt.news.view.listener;


import com.newshunt.dataentity.common.pages.PageEntity;

import java.util.List;

/**
 * @author anshul.jain on 6/15/2016.
 */
public class EntitiesSelectionListener {

  public interface SingleEntitySelectListener<T> {
    void onEntitySelected(boolean isSelected, T entityNode);
  }

  public interface MultiEntitySelectListener<T> {
    void onEntitySelected(boolean isSelected, T entityNode, boolean
        enableDoneButton);

    void onAllEntitiesSelected(boolean isSelected, List<T> entityNodeList);
  }

  public interface EntitySelectAllListener {
    void onAllEntitiesSelected(boolean isSelected);
  }

  public interface SelectEntityListener {
    void onEntitySelected();

    void updateUIForAddedPages(List<PageEntity> pageEntities);
  }
}
