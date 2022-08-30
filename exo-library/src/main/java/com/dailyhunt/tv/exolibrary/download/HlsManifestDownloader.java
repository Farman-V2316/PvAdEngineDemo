/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.download;

import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.offline.SegmentDownloader;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParser;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.ParsingLoadable;
import com.newshunt.common.helper.common.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vinod.bc
 */
public class HlsManifestDownloader extends SegmentDownloader<HlsPlaylist> {

  private static final String TAG = HlsManifestDownloader.class.getName();

  /**
   * @param playlistUri       The {@link Uri} of the playlist to be downloaded.
   * @param streamKeys        Keys defining which renditions in the playlist should be selected for
   *                          download. If empty, all renditions are downloaded.
   * @param constructorHelper A {@link DownloaderConstructorHelper} instance.
   */
  public HlsManifestDownloader(Uri playlistUri,
                               List<StreamKey> streamKeys,
                               DownloaderConstructorHelper constructorHelper) {
    super(playlistUri, streamKeys, constructorHelper);
  }

  @Override
  protected HlsPlaylist getManifest(DataSource dataSource, DataSpec dataSpec) throws IOException {
    Logger.d(TAG, "getManifest dataSource : " + dataSource.getUri());
    Logger.d(TAG, "getManifest dataSpec : " + dataSpec.uri);
    HlsPlaylist playlist = loadManifest(dataSource, dataSpec);
    Logger.d(TAG, "getManifest playlist : " + playlist.tags.size());
    Logger.d(TAG, "getManifest playlist List : " + playlist.tags);
    if (listener != null) {
      listener.onHlsManifestLoaded(playlist);
    }
    return null;
  }

  @Override
  protected List<Segment> getSegments(DataSource dataSource, HlsPlaylist playlist,
                                      boolean allowIncompleteList)
          throws InterruptedException, IOException {
    ArrayList<Segment> segments = new ArrayList<>();
    return segments;
  }

  private static HlsPlaylist loadManifest(DataSource dataSource, DataSpec dataSpec)
          throws IOException {
    Logger.d(TAG, "loadManifest dataSource : " + dataSource.getUri());
    Logger.d(TAG, "loadManifest dataSpec : " + dataSpec.uri);
    return ParsingLoadable.load(
            dataSource, new HlsPlaylistParser(), dataSpec, C.DATA_TYPE_MANIFEST);
  }

  private HlsManifestListener listener = null;

  public void setListener(HlsManifestListener listener) {
    this.listener = listener;
  }

  public interface HlsManifestListener {
    public void onHlsManifestLoaded(HlsPlaylist playlist);
  }

}
