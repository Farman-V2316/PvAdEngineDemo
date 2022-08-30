/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.listener;

import com.newshunt.dataentity.common.model.entity.EventsInfo;

import java.util.List;

/**
 * @author shrikant.agrawal
 */
public interface CommunicationEventInterface {
  EventsInfo getEvent(String resource, String event);

  // TODO (satosh.dhanyamraju): needs refactoring
  List<EventsInfo> getEvents(String resource, String event);

  boolean getIsCommunicationDialogVisible();
}
