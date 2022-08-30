/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.apis;

import androidx.annotation.NonNull;

import com.newshunt.dataentity.common.asset.DiscussionResponse;
import com.newshunt.dataentity.common.asset.PostEntity;
import com.newshunt.dataentity.common.asset.PostSuggestedFollow;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.common.model.entity.model.LikesResponse;
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse;
import com.newshunt.dataentity.social.entity.PhotoChild;
import com.newshunt.news.util.NewsConstants;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;


/**
 * Retrofit interface to news detail API.
 * This gives full description of news detail content.
 *
 * @author abhijeet.jadhav
 */
public interface NewsDetailAPI {

  /**
   * Api to get story from child story id
   *
   * @return Call - callback on response
   */
  @Headers("variableResolution: y")
  @GET()
  Observable<Response<ApiResponse<DiscussionResponse>>> getDiscussions(@Url String url);

  /**
   * Api to get story from child story id
   *
   * @return Call - callback on response
   */
  @Headers("variableResolution: y")
  @GET()
  Observable<ApiResponse<DiscussionResponse>> getDiscussions(@Url String url,
                                                             @Query("filterType") String filterType);


  /**
   * MultiValueResponse  contains data items of type BaseContentAsset or its subclasses
   */
  @Headers("variableResolution: y")
  @POST()
  Observable<Response<ApiResponse<MultiValueResponse<PostEntity>>>> postRelatedStories(@Url String path,
                                                                                      @Body Object payload,
                                                                                      @Query("langCode") String langCodes,
                                                                                      @Query("appLanguage") String appLanguage,
                                                                                      @Query("edition") String edition);

  /**
   * MultiValueResponse  contains data items of type BaseContentAsset or its subclasses
   */
  @Headers("variableResolution: y")
  @GET()
  Observable<Response<ApiResponse<MultiValueResponse<PostEntity>>>> getRelatedStories(@Url String path,
                                                                            @Query("langCode") String langCodes,
                                                                            @Query("appLanguage") String appLanguage,
                                                                            @Query("edition") String edition);

  @GET("api/v2/posts/article/content/{postId}")
  Observable<Response<ApiResponse<PostEntity>>> getFullContent(@Path("postId") String postId,
                                                               @Query(NewsConstants.KEY_REFERRER_FLOW) String referrerFlow,
                                                               @Query(NewsConstants.KEY_REFERRER_FLOW_ID) String referrerFlowId,
                                                               @Query("useWidgetPosition") Boolean useWidgetPosition,
                                                               @Query("sendBothChunk") Boolean sendBothChunk);

  @GET("api/v2/posts/article/content/{postId}")
  Observable<Response<ApiResponse<PostEntity>>> getFullPost(@Path("postId") String postId,
                                                            @Query("useWidgetPosition") Boolean useWidgetPosition,
                                                            @Query("sendBothChunk") Boolean sendBothChunk);

  @GET()
  Observable<Response<ApiResponse<PostEntity>>> contentOfPost(@NonNull @Url String url);

  /**
   * Api to get story from child story id
   *
   * @return Call - callback on response
   */
  @GET("http://newshunt.net.in:8091/Test/dhandroiddev/sd/suggested-disabled.json")
  Observable<ApiResponse<List<PostSuggestedFollow>>> getSuggestedFollowForPostFromId(
      @Query("postId") String postId);

  @Headers("variableResolution: y")
  @GET()
  Observable<ApiResponse<MultiValueResponse<PostEntity>>> getMoreStories2(
      @Url String moreStoriesUrl,
      @Query("langCode") String langCodes,
      @Query("appLanguage") String appLanguage,
      @Query("edition") String edition);


  @GET("api/v2/posts/counts/{postId}")
  Observable<Response<ApiResponse<LikesResponse>>> getCountsForPost(@Path("postId") String postId);

  @GET("api/v2/posts/discussions/{postId}")
  Observable<Response<ApiResponse<DiscussionResponse>>> getDiscussionsForPost(
      @Path("postId") String postId,
      @Query("filterType") String filterType);

  @GET()
  Observable<Response<ApiResponse<MultiValueResponse<PhotoChild>>>> getChildPhotos(@Url String path);

  @GET("api/v2/posts/parent/content/{postId}")
  Observable<Response<ApiResponse<PostEntity>>> getParentForComment(@Path("postId") String postId);

}
