package com.newshunt.dataentity.notification;

import com.newshunt.common.helper.common.Constants;

import java.io.Serializable;

/**
 * Created by santosh.kumar on 10/30/2015.
 */
public class BooksNavModel extends BaseModel implements Serializable {

  private String bookListId;
  private String bookLanguage = Constants.EMPTY_STRING;
  private String imageLink;
  private NotificationSectionType sectionType;
  private NotificationLayoutType layoutType;
  private String promoId;
  private String bookId;

  public String getBookLanguage() {
    return bookLanguage;
  }

  public void setBookLanguage(String bookLanguage) {
    this.bookLanguage = bookLanguage;
  }

  public String getImageLink() {
    return imageLink;
  }

  public void setImageLink(String imageLink) {
    this.imageLink = imageLink;
  }

  public NotificationSectionType getSectionType() {
    return sectionType;
  }

  public void setSectionType(NotificationSectionType sectionType) {
    this.sectionType = sectionType;
  }

  public NotificationLayoutType getLayoutType() {
    return layoutType;
  }

  public void setLayoutType(NotificationLayoutType layoutType) {
    this.layoutType = layoutType;
  }

  public String getPromoId() {
    return promoId;
  }

  public void setPromoId(String promoId) {
    this.promoId = promoId;
  }

  public String getBookId() {
    return bookId;
  }

  public void setBookId(String bookId) {
    this.bookId = bookId;
  }


  public String getBookListId() {
    return bookListId;
  }

  public void setBookListId(String bookListId) {
    this.bookListId = bookListId;
  }

  @Override
  public BaseModelType getBaseModelType() {
    return BaseModelType.BOOKS_MODEL;
  }

  @Override
  public String getItemId() {
    return bookId;
  }
}
