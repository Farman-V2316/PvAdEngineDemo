/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.interceptor;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.newshunt.common.domain.WriteToCacheUsecase;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dhutil.ExtnsKt;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Interceptor that can be used for caching raw responses. Behavior is controlled by request
 * headers
 * @author satosh.dhanymaraju
 */
@SuppressLint("DefaultLocale")
public class CachingInterceptor implements Interceptor {
  private static final String LOG_TAG = "CachingInterceptor";

  private final WriteToCacheUsecase<String> cacheWriter;

  @Retention(RetentionPolicy.SOURCE)
  @StringDef({
      DevDhHeaders.CACHE_URL_PLAIN, DevDhHeaders.CACHE_URL_USE_REQ_URL,
      DevDhHeaders.CACHE_URL_EXTRACT_FROM_RESPONSE
  })
  public @interface DevDhHeaders {
    // header value is the url
    String CACHE_URL_PLAIN = "dev-dh-cache-url-plain";
    /* header value, if true, use request url */
    String CACHE_URL_USE_REQ_URL = "dev-dh-cache-url-use-req-url";
    /* header value is a pattern to walk the json tree. It's names of json objects down until the
    string field whose value is cache url; ! is the separator. eg: "!data!pageUrl" */
    String CACHE_URL_EXTRACT_FROM_RESPONSE = "dev-dh-cache-url-extract-from-response";
  }

  public CachingInterceptor(
      WriteToCacheUsecase<String> cacheWriter) {
    this.cacheWriter = cacheWriter;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    final ArrayList<String> cacheUrls = new ArrayList<>();

    //plain
    List<String> encodedPainUrls = chain.request().headers(DevDhHeaders.CACHE_URL_PLAIN);
    for (String plainUrl : encodedPainUrls) {
      cacheUrls.add(ExtnsKt.urlDecode(plainUrl));
    }

    // requrl
    if (chain.request().header(DevDhHeaders.CACHE_URL_USE_REQ_URL) != null) {
      cacheUrls.add(chain.request().url().toString());
    }

    // extract from resp headers. actual urls will not be available now
    final List<String> extractHeaders = chain.request().headers(DevDhHeaders.CACHE_URL_EXTRACT_FROM_RESPONSE);

    //remove headers and request
    final Response response = chain.proceed(chain.request().newBuilder()
        .removeHeader(DevDhHeaders.CACHE_URL_PLAIN)
        .removeHeader(DevDhHeaders.CACHE_URL_USE_REQ_URL)
        .removeHeader(DevDhHeaders.CACHE_URL_EXTRACT_FROM_RESPONSE)
        .build());

    if (!response.isSuccessful()) { // TODO (satosh.dhanyamraju): store 204 response also?
      Logger.d(LOG_TAG, "intercept: nothing cached. response failed: "+response.code());
      return response;
    }

    final ResponseBody responseBody = response.body();
    if (responseBody == null || responseBody.contentLength() == 0) {
      Logger.e(LOG_TAG, "intercept: not-cached. empty response");
      return response;
    }


    // read response as string
    final long contentLength = responseBody.contentLength();
    final BufferedSource source = responseBody.source();
    source.request(Long.MAX_VALUE); // Buffer the entire body.
    final Buffer buffer = source.buffer();
    final String rawResponse = contentLength != 0 ?
        buffer.clone().readString(Charset.forName(Constants.TEXT_ENCODING_UTF_8)) :
        Constants.EMPTY_STRING;

    if (rawResponse.isEmpty()) {
      Logger.e(LOG_TAG, "intercept: not-cached. empty response");
      return response;
    }

    // extract urls from response
    if (!CommonUtils.isEmpty(extractHeaders)) {
      try {
        final JSONObject responseJSON = new JSONObject(rawResponse);
        for (String urlPtrn : extractHeaders) {
          if (CommonUtils.isEmpty(urlPtrn)) continue;
          try {
            String url = extractFrom(responseJSON,
                AndroidUtils.arrayListOf(str -> !CommonUtils.isEmpty(str), urlPtrn.split("!")));
            cacheUrls.add(url);
          } catch (JSONException e) {
            Logger.e(LOG_TAG, "intercept-pattern: "+e.getMessage()+","+urlPtrn );
            // try next pattern
          }
        }
      } catch (JSONException e) {
        Logger.e(LOG_TAG, "intercept: "+e.getMessage() );
      }
    }

    Logger.d(LOG_TAG, "intercept: cacheurls= "+cacheUrls.size()+"> "+cacheUrls);

    // write to cache
    Observable.fromIterable(
        new LinkedHashSet<>(AndroidUtils.arrayListOf(o -> !CommonUtils.isEmpty(o), cacheUrls)))// nonEmpty, then dedup
        .flatMap(s -> cacheWriter.writeToCache(s, rawResponse))
        .subscribe(o -> {
              Logger.d(LOG_TAG, String.format("Written %d bytes,", rawResponse.length()));
            }, Logger::caughtException);
    return response;
  }

  /**
   * recursively extract string from nested json objects.
   */
  @NonNull
  private static String extractFrom(@NonNull JSONObject jsonObject, @NonNull List<String> pathSgmnts)
      throws JSONException {
    final String first = pathSgmnts.get(0);
    final List<String> rest = pathSgmnts.subList(1, pathSgmnts.size());
    if (pathSgmnts.size() == 1) {
        return jsonObject.getString(first);
    } else {
      return extractFrom(jsonObject.getJSONObject(first), rest);
    }
  }
}
