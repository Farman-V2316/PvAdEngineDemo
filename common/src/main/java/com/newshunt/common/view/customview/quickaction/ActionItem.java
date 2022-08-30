package com.newshunt.common.view.customview.quickaction;

/**
 * Action item, displayed as menu with icon and text.
 *
 * https://github.com/Vinayrraj/Jhilwara-Quick-Action-Library
 *
 * @author Lorensius. W. L. T <lorenz@londatiga.net>
 *         <p>
 *         Contributors:
 *         - Kevin Peck <kevinwpeck@gmail.com>
 */
public class ActionItem {
  private String title;
  private int actionId = -1;
  private boolean selected;
  private boolean sticky;

  /**
   * Constructor
   *
   * @param actionId Action id for case statements
   * @param title    Title
   */
  public ActionItem(int actionId, String title) {
    this.title = title;
    this.actionId = actionId;
  }

  /**
   * Set action title
   *
   * @param title action title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get action title
   *
   * @return action title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Set action id
   *
   * @param actionId Action id for this action
   */
  public void setActionId(int actionId) {
    this.actionId = actionId;
  }

  /**
   * @return Our action id
   */
  public int getActionId() {
    return actionId;
  }

  /**
   * Set sticky status of button
   *
   * @param sticky true for sticky, pop up sends event but does not disappear
   */
  public void setSticky(boolean sticky) {
    this.sticky = sticky;
  }

  /**
   * @return true if button is sticky, menu stays visible after press
   */
  public boolean isSticky() {
    return sticky;
  }

  /**
   * Set selected flag;
   *
   * @param selected Flag to indicate the item is selected
   */
  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  /**
   * Check if item is selected
   *
   * @return true or false
   */
  public boolean isSelected() {
    return this.selected;
  }

}