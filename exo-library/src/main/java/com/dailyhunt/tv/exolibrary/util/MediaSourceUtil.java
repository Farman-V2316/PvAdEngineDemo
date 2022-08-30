package com.dailyhunt.tv.exolibrary.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.coolfie_exo.download.ExoCacheHelper;
import com.dailyhunt.tv.exolibrary.entities.MediaItem;
import com.dailyhunt.tv.exolibrary.interceptors.HttpNotFoundInterceptor;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.sdk.network.NetworkSDK;
import com.newshunt.sdk.network.Priority;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by rahul on 31/10/17.
 */

public final class MediaSourceUtil {
  public static final String TAG = "MediaSourceUtil";
  public static final String PREF_USE_OKHTTP_DS = "PREF_USE_OKHTTP_DS";

  private static Interceptor loggIngInterceptor = new Interceptor() {
    @Override
    public Response intercept(Chain chain) throws IOException {
      Response response = chain.proceed(chain.request());
      Logger.d(TAG, String.format("%s, url=%s", response.protocol(),
          response.request().url()));
      return response;
    }
  };

  private MediaSourceUtil() {
  }


  @Nullable
  public static String getExtension(final @NonNull Uri uri) {
    String path = uri.getLastPathSegment();
    if (path == null) {
      return null;
    }

    int periodIndex = path.lastIndexOf('.');
    if (periodIndex == -1 && uri.getPathSegments().size() > 1) {
      //Checks the second to last segment to handle manifest urls (e.g. "TearsOfSteelTeaser.ism/manifest")
      path = uri.getPathSegments().get(uri.getPathSegments().size() - 2);
      periodIndex = path.lastIndexOf('.');
    }

    //If there is no period, prepend one to the last segment in case it is the extension without a period
    if (periodIndex == -1) {
      periodIndex = 0;
      path = "." + uri.getLastPathSegment();
    }

    String rawExtension = path.substring(periodIndex);
    return rawExtension.toLowerCase();
  }


  public static DataSource.Factory defaultDataSourceFactory(final Context context,
                                                            final DefaultBandwidthMeter bandwidthMeter) {
    return new DefaultDataSourceFactory(context, bandwidthMeter,
        buildHttpDataSourceFactory(context));
  }

  private static HttpDataSource.Factory buildHttpDataSourceFactory(final Context context) {
    OkHttpClient.Builder client = NetworkSDK.clientBuilder(Priority.PRIORITY_HIGHEST, null);
    client.addInterceptor(new HttpNotFoundInterceptor());
    return new OkHttpDataSourceFactory(client.build(),
        Util.getUserAgent(context, getApplicationName(context)), null, null);
  }

  public static String getApplicationName(final Context context) {
    ApplicationInfo applicationInfo = context.getApplicationInfo();
    int stringId = applicationInfo.labelRes;
    return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() :
        context.getString(stringId);
  }

  public static DrmSessionManager<FrameworkMediaCrypto> generateDrmSessionManager() {
    UUID uuid = C.WIDEVINE_UUID;
    try {
      return new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid),
          new DefaultDrmCallBack(), null);
    } catch (final Exception e) {
      Log.d(TAG, e.getLocalizedMessage());
    }
    return null;
  }


  public static class DefaultDrmCallBack implements MediaDrmCallback {
    @Override
    public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest provisionRequest)
        throws Exception {
      return new byte[0];
    }

    @Override
    public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest keyRequest) throws Exception {
      return new byte[0];
    }
  }

  public static MediaSource getMappedSource(final Context context, final Uri uri, final boolean isLive) {
    return getMappedSource(context, uri, PreferenceManager.getBoolean(PREF_USE_OKHTTP_DS, false), isLive);
  }

  public static MediaSource getMappedSource(final Context context, final Uri uri,
                                            boolean useOkHttpDataSource, boolean isLive) {
    int extension = Util.inferContentType(uri);
    if (extension == C.TYPE_OTHER) {
      extension = reCheckContentType(uri.toString());
    }
    switch (extension) {
      case C.TYPE_DASH:
        return new DashMediaSource.Factory(buildDataSourceFactory(context, uri))
                .setTag(uri)
                .createMediaSource(uri);

      case C.TYPE_HLS:
        HttpDataSource.BaseFactory httpDataSourceFactory ;

        if (!useOkHttpDataSource) {
          httpDataSourceFactory = new DefaultHttpDataSourceFactory(
                  Util.getUserAgent(context, getApplicationName(context)),
                  null,
                  DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                  DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                  true /* allowCrossProtocolRedirects */);
        } else {
          OkHttpClient.Builder client = new OkHttpClient.Builder();
          if (Logger.loggerEnabled()) {
            client.addInterceptor(loggIngInterceptor);
          }
          httpDataSourceFactory =    new OkHttpDataSourceFactory(client.build(),
                  Util.getUserAgent(context, getApplicationName(context)));
        }

        DefaultDataSourceFactory dataFactory = new DefaultDataSourceFactory(
                context, null, httpDataSourceFactory);

        if(isLive) {
          Logger.d(TAG, "getMappedSource isLive = true");
          return new HlsMediaSource.Factory(dataFactory)
                  .setAllowChunklessPreparation(true)
                  .createMediaSource(uri);
        }
        Logger.d(TAG, "getMappedSource isLive = false");
        return new HlsMediaSource.Factory(buildDataSourceFactory(context, uri))
                .setAllowChunklessPreparation(true)
                .createMediaSource(uri);

      case C.TYPE_OTHER:
        HttpDataSource.BaseFactory dataSourceFactory;
        if (!useOkHttpDataSource) {
          dataSourceFactory = new DefaultHttpDataSourceFactory(
                  Util.getUserAgent(context, getApplicationName(context)),
                  null,
                  DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                  DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                  true /* allowCrossProtocolRedirects */
          );
        } else {
          OkHttpClient.Builder builder = new OkHttpClient.Builder();
          if (Logger.loggerEnabled()) {
            builder.addInterceptor(loggIngInterceptor);
          }
          dataSourceFactory = new OkHttpDataSourceFactory(builder.build(),
                  Util.getUserAgent(context, getApplicationName(context)));
        }

        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(extractorsFactory).createMediaSource(uri);
        return mediaSource;

      default: {
        throw new IllegalStateException("Unsupported type: " + extension);
      }
    }
  }

  private static DataSource.Factory buildDataSourceFactory(Context context, Uri uri) {
    Logger.d(TAG, "buildDataSourceFactory creating >.");
    DefaultDataSourceFactory upstreamFactory = buildDefaultDataSourceFactory(context, buildHttpDataSourceFactory(context));
    return ExoCacheHelper.INSTANCE.buildCacheDataSource(upstreamFactory, context, uri);
  }

  private static DefaultDataSourceFactory buildDefaultDataSourceFactory(Context context,
                                            HttpDataSource.Factory httpDataSourceFactory)  {
      Logger.d(TAG, "buildDataSourceFactory > httpDataSourceFactory ");
      return new DefaultDataSourceFactory(context, null, httpDataSourceFactory);
  }

  public static int reCheckContentType(String fileName) {
    fileName = Util.toLowerInvariant(fileName);
    if (fileName.contains("mpd")) {
      return C.TYPE_DASH;
    } else if (fileName.contains("m3u8")) {
      return C.TYPE_HLS;
    } else {
      return C.TYPE_OTHER;
    }
  }

  private static Pattern ISM_URL_PATTERN = Pattern.compile(".*\\.isml?(?:/(manifest(.*))?)?");
  private static final String ISM_HLS_FORMAT_EXTENSION = "format=m3u8-aapl";
  private static final String ISM_DASH_FORMAT_EXTENSION = "format=mpd-time-csf";

  public static int getContentType(Uri uri) {
    int contentType = Util.inferContentType(uri);
    Logger.d(TAG, "ContentType from Exo Util : $contentType");
    if (contentType != C.TYPE_OTHER) {
      return contentType;
    }
    String extension = getExtension(uri);
    Logger.d(TAG, "ContentType from custom find : $extension");
    if (extension != null) {
      extension = Util.toLowerInvariant(extension);
      if (extension.contains(".mpd")) {
        Logger.d(TAG, "ContentType from custom find : TYPE_DASH");
        return C.TYPE_DASH;
      } else if (extension.contains(".m3u8")) {
        Logger.d(TAG, "ContentType from custom find : TYPE_HLS");
        return C.TYPE_HLS;
      }
      Matcher ismMatcher = ISM_URL_PATTERN.matcher(extension);
      if (ismMatcher.matches()) {
        String extensions = ismMatcher.group(2);
        if (extensions != null) {
          if (extensions.contains(ISM_DASH_FORMAT_EXTENSION)) {
            Logger.d(TAG, "ContentType from custom find : TYPE_DASH");
            return C.TYPE_DASH;
          } else if (extensions.contains(ISM_HLS_FORMAT_EXTENSION)) {
            Logger.d(TAG, "ContentType from custom find : TYPE_HLS");
            return C.TYPE_HLS;
          }
        }
      }
    }
    Logger.d(TAG, "ContentType from custom find : TYPE_OTHER");
    return C.TYPE_OTHER;
  }

  public static DataSource.Factory buildForPrefetch(Context context) {
    DataSource.Factory upstreamFactory = buildDefaultDataSourceFactory(context, buildHttpDataSourceFactory(context));
      return new DataSource.Factory() {
        @Override
        public DataSource createDataSource() {
          return upstreamFactory.createDataSource();
        }
      };
  }
}

