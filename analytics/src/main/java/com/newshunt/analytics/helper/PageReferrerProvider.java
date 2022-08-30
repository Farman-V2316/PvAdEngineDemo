package com.newshunt.analytics.helper;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;

/**
 * @author: bedprakash.rout on 7/8/2016.
 */
public interface PageReferrerProvider {

  PageReferrer getProvidedPageReferrer();

  PageReferrer getYoungestPageReferrer();
}
