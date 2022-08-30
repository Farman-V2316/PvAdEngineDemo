package com.newshunt.news.view.listener;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;

/**
 * Interface for video_played event to get Referrer Info
 *
 * @author: umesh.isran on 12/08/2018.
 */
public interface VideoPlayedReferrerListener {

  PageReferrer getPageReferrer();

  PageReferrer getReferrerFlow();
}
