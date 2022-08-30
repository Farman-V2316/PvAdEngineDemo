/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;


/**
 * When view generates some event, whose action affects the app's model, but result of the action
 * need not be communicated back to the view(only one-way communication), then it can be modeled
 * as a CardEvent and posted on the bus. This class will consume that event and update the model
 *
 * @author satosh.dhanyamraju
 */
@Deprecated
public class DislikeStoryHelper {


  private final Map<MapKey, MapValue> dislikeState;

  public DislikeStoryHelper(Map<MapKey, MapValue> map) {
    this.dislikeState = map;
  }


  public static class MapKey {
    private final String storyId;
    private final String groupId;

    MapKey(@NonNull String storyId, String groupId) {
      this.storyId = storyId;
      this.groupId = groupId;
    }

    public String getGroupId() {
      return groupId;
    }

    public String getStoryId() {
      return storyId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      MapKey mapKey = (MapKey) o;

      if (!storyId.equals(mapKey.storyId)) {
        return false;
      }
      return groupId != null ? groupId.equals(mapKey.groupId) : mapKey.groupId == null;
    }

    @Override
    public int hashCode() {
      int result = storyId.hashCode();
      result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
      return result;
    }
  }

  public static class MapValue {
    private final boolean syncedWithServer;
    private final boolean isPerSession;// if true, it will not be saved to preferences
    @Nullable
    private final DislikeStoryCardEvent dislikeStoryCardEvent;

    MapValue(boolean syncedWithServer, boolean isPerSession, @Nullable DislikeStoryCardEvent dislikeStoryCardEvent) {
      this.syncedWithServer = syncedWithServer;
      this.isPerSession = isPerSession;
      this.dislikeStoryCardEvent = dislikeStoryCardEvent;
    }

    public boolean isSyncedWithServer() {
      return syncedWithServer;
    }

    public boolean isPerSession() {
      return isPerSession;
    }

    @Nullable
    public DislikeStoryCardEvent getDislikeStoryCardEvent() {
      return dislikeStoryCardEvent;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      MapValue mapValue = (MapValue) o;

      if (syncedWithServer != mapValue.syncedWithServer) {
        return false;
      }
      return isPerSession == mapValue.isPerSession;
    }

    @Override
    public int hashCode() {
      int result = (syncedWithServer ? 1 : 0);
      result = 31 * result + (isPerSession ? 1 : 0);
      return result;
    }
  }
}
