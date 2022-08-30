package com.newshunt.dataentity.notification;

/**
 * Represents different types of notification (including news and books)
 *
 * @author santosh.kulkarni
 */
public enum NavigationType {

  // Server decided Notification Ids
  TYPE_OPEN_APP(21),
  TYPE_OPEN_NEWSITEM(22),
  TYPE_OPEN_BOOKDETAILS(23),
  TYPE_OPEN_BOOKLIST_CATEGORY(42),
  TYPE_OPEN_BOOKLIST(25),
  TYPE_OPEN_BOOKHOME(26),
  TYPE_OPEN_MYLIBRARY(27),
  TYPE_OPEN_BOOK_PAYMENT(28),
  TYPE_OPEN_NEWS_LIST(29),
  TYPE_OPEN_NEWS_LIST_CATEGORY(30),
  TYPE_OPEN_CART(31),
  TYPE_OPEN_TOPIC(40),
  TYPE_OPEN_STICKY_CRICKET(41),
  TYPE_OPEN_VIRAL_TOPIC(43),
  TYPE_OPEN_VIRAL_ITEM(44),

  // Client handling notification Ids. Starts with 1001.
  TYPE_OPEN_BOOK_READER(1001),
  TYPE_OPEN_LOCATION(1002),
  TYPE_OPEN_NEWS_HOME(1003),
  TYPE_OPEN_DEFAULT(1004),
  TYPE_OPEN_WEBPAGE(1006),
  TYPE_OPEN_SIMILAR_STORIES(1008),
  TYPE_OPEN_SSO(1009),
  TYPE_OPEN_ALL_SOCIAL_COMMENTS(1010),
  TYPE_OPEN_COMMENT(1011),
  TYPE_OPEN_LOCATION_LIST(1012),
  TYPE_OPEN_TOPIC_LIST(1013),
  TYPE_OPEN_NP_GROUP_LIST(1015),
  TYPE_OPEN_FOLLOW_EXPLORE_TAB(1016),
  TYPE_OPEN_EXPLORE_ENTITY(1017),
  TYPE_OPEN_FOLLOW_HOME(1018),
  TYPE_OPEN_EXPLORE_VIEW_TAB(1019),//Open to Explore Activity with any one of
  // sources/locations/topics
  TYPE_OPEN_FOLLOWING(1020),
  TYPE_OPEN_FOLLOWING_FEED(1021),
  TYPE_OPEN_LOCO(1022),
  TYPE_OPEN_FOLLOWERS(1023),
  TYPE_OPEN_SOCIAL_GROUP(1024),
  TYPE_OPEN_SOCIAL_GROUP_APPROVAL(1025),
  TYPE_OPEN_SOCIAL_GROUP_CREATE(1026),
  TYPE_OPEN_SOCIAL_GROUP_INVITES(1027),
  TYPE_OPEN_CREATE_POST(1028),
  TYPE_OPEN_CONTACTS_RECO(1029),
  //Open search or presearch
  TYPE_OPEN_SEARCH_ITEM(1030),
  TYPE_OPEN_PERMISSION(1031),
  TYPE_OPEN_LANG_SELECTION(1032),
  TYPE_OPEN_LOCAL_SECTION(1033),
  TYPE_HANDLE_ADJUNCT_LANG(1034),
  TYPE_HANDLE_APP_SECTION(1035),
  TYPE_SETTINGS(1036),
  TYPE_SETTINGS_AUTOSCROLL(1037),
  TYPE_NOTIFICATION_INBOX(1038),
  TYPE_NOTIFICATION_SETTINGS(1039),


  WAKEUP_TO_TESTPREP(100),
  PRODUCT_DETAILS(101),
  OPEN_UNIT(102),
  //- Open the given book ID. If book is not in library and it is a free book then the book should
  // be automatically downloaded and opened (similar to onboarding test)
  BUY_PRODUCT(103), //- Directly show the payment page of the given book ID
  COLLECTION(104), //-Jump to the view all page of given collection�,
  GROUP(105), //- Show the given group. this can be used for exam / examgroup / topic,
  MY_UNITS_WITH_FILTER(106),
  MY_TESTS(107),
  MANAGE_INTEREST(108),//- Jump to add/edit exam page
  PRODUCTS_IN_CART(109), //- Show payment pages with items in cart id
  APP_REVIEW(110),
  APP_FEEDBACK(111),

  READ_STUDY_MATERIAL(112),//- To open a study material
  CONTENT_REVIEW(113),
  TEST_RESULT(114), //- Jump to test result page using the result url (for rank movement)
  NEWS(115),// - using newsitem url
  ADD_INTEREST(116),//- Add Interest view. Interest group and interest will be populated.
  START_TEST(117),//-Start a test
  OPEN_WEB_PAGE(118),
  SUBSCRIPTION_DETAILS(119),
  TYPE_INTERACTION(120),

  TYPE_TV_OPEN_TO_DETAIL(500),
  TYPE_TV_OPEN_TO_GROUP_TAB(501),
  TYPE_TV_OPEN_TO_CATEGORY(502),
  TYPE_TV_OPEN_TO_SECTION(503),
  TYPE_TV_OPEN_TO_CHANNEL(504),
  TYPE_TV_OPEN_TO_PLAYLIST(505),

  TYPE_OPEN_LIVETV_ITEM(601),
  TYPE_OPEN_LIVETV_SECTION(602),
  TYPE_OPEN_LIVETV_GROUP_TAB(603),

  TYPE_TV_OPEN_TO_SHOW(701),

  // Server decided Notification Ids
  TYPE_OPEN_VIDEO_ITEM(801),
  TYPE_OPEN_PROFILE(802),
  TYPE_OPEN_TAGS(803),
  TYPE_OPEN_FEED(804),
  TYPE_OPEN_CATEGORY(805),
  TYPE_OPEN_PROMOTION(807),

  TYPE_DH_TV_OPEN_TO_DETAIL(900),
  TYPE_DH_TV_OPEN_TO_SECTION(901),
  TYPE_DH_TV_OPEN_TO_CHANNEL(902),
  TYPE_DH_TV_OPEN_TO_SPL(903),
  TYPE_DH_TV_OPEN_TO_TAG(904),
  DELETE_NOTIFICATIONS(905),
  LANG_UPDATE_NOTIFICATION(906),
  TYPE_OPEN_NEWSITEM_ADJUNCT_STICKY(907),
  IN_APP(908),

  //Putting the index 100000 randomly as it is not coming from the server and is generated via
  // client only
  SELF_BOARDING(100000);
  // index : These are pre-defined values for notification types (followed in older
  // versions of an app.)
  private final int index;

  NavigationType(int index) {
    this.index = index;
  }

  public static NavigationType fromIndex(int index) {
    for (NavigationType type : NavigationType.values()) {
      if (type.index == index) {
        return type;
      }
    }
    return null;
  }

  public static NavigationType fromString(String name) {
    for (NavigationType type : NavigationType.values()) {
      if (type.name().equalsIgnoreCase(name)) {
        return type;
      }
    }
    return null;
  }

  public int getIndex() {
    return index;
  }
}
