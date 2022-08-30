package com.newshunt.dataentity.dhutil.model.entity;


/**
 * used wiwth bus to notify when a tab is selected
 *
 * @author bedprakash.rout on 3/22/2016.
 */
public class NHTabClicked {
  private String currentSectionId;


  public NHTabClicked(String currentSectionId) {
    this.currentSectionId = currentSectionId;
  }

  public String getTabType() {
    return currentSectionId;
  }
}
