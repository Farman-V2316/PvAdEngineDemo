/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.service;



import com.newshunt.dataentity.common.model.entity.CommunicationEventsResponse;

import io.reactivex.Observable;

/**
 * @author shrikant.agrawal
 */
public interface CommunicationEventsService {

  Observable<CommunicationEventsResponse> getCommunicationEvents();

  Observable<CommunicationEventsResponse> getStoredCommunicationResponse();
}
