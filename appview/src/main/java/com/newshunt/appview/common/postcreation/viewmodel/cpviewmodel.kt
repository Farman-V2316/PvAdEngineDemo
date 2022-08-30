/*
 * Created by Rahul Ravindran at 19/9/19 5:44 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.postcreation.viewmodel

import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.linkedin.android.spyglass.tokenization.QueryToken
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.analytics.entity.CreatePostActionType
import com.newshunt.appview.common.postcreation.analytics.entity.CreatePostImageAttachmentType
import com.newshunt.appview.common.postcreation.analytics.helper.CreatePostAnalyticsHelper
import com.newshunt.appview.common.postcreation.view.activity.CreatePostView
import com.newshunt.appview.common.postcreation.view.helper.ImageFilePath
import com.newshunt.appview.common.postcreation.view.helper.PostConstants
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.CAMERA_REQUEST
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.LOCATION_REQUEST
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.PICK_IMAGE_MULTIPLE
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.POST_SELECTED_LOCATION
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.Added
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.LocalInfo
import com.newshunt.dataentity.common.asset.OEmbedResponse
import com.newshunt.dataentity.common.asset.Phrase
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostMeta
import com.newshunt.dataentity.common.asset.PostPollOption
import com.newshunt.dataentity.common.asset.PostPollPojo
import com.newshunt.dataentity.common.asset.PostType
import com.newshunt.dataentity.common.asset.RepostAsset
import com.newshunt.dataentity.dhutil.model.entity.asset.ImageDetail
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.social.entity.CreatePost
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.CreatePostID
import com.newshunt.dataentity.social.entity.DEFAULT_IMAGE_COMPRESS_QUALITY
import com.newshunt.dataentity.social.entity.DetailCard
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.getFromStream
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.take1
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.CpCreationUseCase
import com.newshunt.news.model.usecase.CpImageInsertUseCase
import com.newshunt.news.model.usecase.CpLocationInsertUseCase
import com.newshunt.news.model.usecase.CpPollInsertUseCase
import com.newshunt.news.model.usecase.CpPrivacyUseCase
import com.newshunt.news.model.usecase.CpReadUseCase
import com.newshunt.news.model.usecase.CpRepostInsertUseCase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.OEmbedUsecase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.model.usecase.transform
import com.newshunt.news.view.activity.SearchActvityInterface
import com.newshunt.news.view.fragment.FetchParentUsecase
import com.newshunt.permissionhelper.utilities.Permission
import com.newshunt.search.viewmodel.SearchViewModel
import com.newshunt.sso.SSO
import java.io.File
import javax.inject.Inject


class CreatePostViewModel(context: Application,
                          val postId: String,
                          val embedUseCase: MediatorUsecase<Bundle, OEmbedResponse>,
                          val cpCreationUseCase: MediatorUsecase<Bundle, CreatePostID>,
                          val cpLocUseCase: MediatorUsecase<Bundle, Boolean>,
                          val cpPollUseCase: MediatorUsecase<Bundle, Boolean>,
                          val cpPrivacyUseCase: MediatorUsecase<Bundle, Boolean>,
                          val hashTagViewModel: HashTagViewModel,
                          val cpReadUseCase: MediatorUsecase<Bundle, CreatePost?>,
                          val cpRepostUseCase: MediatorUsecase<Bundle, Boolean>,
                          val detailsAPIUsecase: MediatorUsecase<Bundle, PostEntity?>) :
        AndroidViewModel(context), ClickHandlingViewModel {

    val imgData: MutableLiveData<Array<ImageDetail>> = MutableLiveData()
    val locationData: MutableLiveData<PostCurrentPlace?> = MutableLiveData()
    val privacyData: MutableLiveData<PostMeta> = MutableLiveData()
    var isBusRegistered = false
    var cpId: Long = -1
        private set
    private var parentId: String = ""
    private var parentPostId: String = ""
    private var viewType: PostType = PostType.TEXT
    private var uiMode = CreatePostUiMode.POST
    private var vi: CreatePostView? = null
    private var serviceRunning: Boolean = false
    private var currentPageReferrer: PageReferrer? = null
    private var compressImage = false
    private var compressQuality: Int = 0
    val cpIdData: MutableLiveData<Long> = MutableLiveData()


    fun attachView(view: CreatePostView) {
        this.vi = view
        hashTagViewModel.attachView(view)
    }

    fun setCurrentPageReferrer(referrer: PageReferrer) {
        this.currentPageReferrer = referrer
    }

    override fun onViewClick(view: View) {
        when (view.id) {
            R.id.create_post_action_camera -> {
                CreatePostAnalyticsHelper.logCreatePostUIActionEvent(CreatePostActionType
                        .IMAGE_ATTACH, CreatePostImageAttachmentType.CAMERA, currentPageReferrer!!)
                vi?.checkPermission(CAMERA_REQUEST, mutableListOf(Permission.ACCESS_CAMERA,
                        Permission.WRITE_EXTERNAL_STORAGE))
            }

            R.id.create_post_action_gallery -> {
                CreatePostAnalyticsHelper.logCreatePostUIActionEvent(CreatePostActionType
                        .IMAGE_ATTACH, CreatePostImageAttachmentType.GALLERY, currentPageReferrer!!)
                vi?.checkPermission(
                        PICK_IMAGE_MULTIPLE, mutableListOf(Permission.WRITE_EXTERNAL_STORAGE)
                )
            }

            R.id.create_post_action_location -> {
                CreatePostAnalyticsHelper.logCreatePostUIActionEvent(CreatePostActionType
                        .LOCATION_ATTACHED, CreatePostImageAttachmentType.NONE, currentPageReferrer!!)
                vi?.checkPermission(LOCATION_REQUEST, mutableListOf(Permission.ACCESS_FINE_LOCATION))
            }

            R.id.create_post_action_hash -> {
                CreatePostAnalyticsHelper.logCreatePostUIActionEvent(CreatePostActionType
                        .HASHTAG_COMPOSE, CreatePostImageAttachmentType.NONE, currentPageReferrer!!)
                vi?.onHashClicked()
            }

            R.id.create_post_action_at -> {
                CreatePostAnalyticsHelper.logCreatePostUIActionEvent(CreatePostActionType
                        .HANDLE_COMPOSE, CreatePostImageAttachmentType.NONE, currentPageReferrer!!)
                vi?.onHandleClicked()
            }
        }
    }


    fun intentFromActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_MULTIPLE -> {
                    val uris = if (data?.clipData?.itemCount ?: -1 > 0) {
                        Array<Uri>(
                                data?.clipData?.itemCount ?: -1
                        ) { i -> data?.clipData?.getItemAt(i)?.uri ?: Uri.EMPTY }
                    } else {
                        arrayOf(data?.data ?: Uri.EMPTY)
                    }
                    val imgList = uris.mapNotNull { transformImg(it) }
                    imgData.value = imgList.toTypedArray()
                }

                CAMERA_REQUEST -> {
                    if (data?.hasExtra(PostConstants.CAMERA_IMAGE_PATH) == true) {
                        val path: String? = data.extras?.getString(PostConstants.CAMERA_IMAGE_PATH)
                        path?.let {
                            val uris = arrayOf(Uri.fromFile(File(path)))
                            val imgList = uris.mapNotNull { transformImg(it) }
                            imgData.value = imgList.toTypedArray()
                        }
                    }
                }

                LOCATION_REQUEST -> {
                    val loc = data?.extras?.getSerializable(POST_SELECTED_LOCATION) as?
                            PostCurrentPlace?
                    locationData.value = loc
                    if (cpId > -1) {
                        cpLocUseCase.execute(
                                bundleOf(
                                        CpLocationInsertUseCase.LOC_POJO to loc,
                                        CpImageInsertUseCase.POST_ID to cpId
                                )
                        )
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun transformImg(uri: Uri): ImageDetail? {
        val imagePath = ImageFilePath.getPath(getApplication(), uri, compressImage, compressQuality)
        if (imagePath.isNullOrEmpty()) {
            return null
        }
        val exifI = ExifInterface(imagePath)

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)
        val width = options.outWidth.toFloat()
        val height = options.outHeight.toFloat()

        val orientation: Int = exifI.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        val rotate = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_270 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_90 -> 270
            else -> 0
        }
        return ImageDetail(
                imagePath, width, height, rotate,
                "${exifI.getAttribute(ExifInterface.TAG_X_RESOLUTION)}X${exifI.getAttribute(
                        ExifInterface.TAG_Y_RESOLUTION
                )}",
                ""
        )
    }

    fun createPost(body: String,
                   p_duration: Long,
                   vararg pollOptions: PostPollOption) {
        //please invoke service from here
        if (pollOptions.isEmpty()) {
            cpCreationUseCase.execute(bundleOf(CpCreationUseCase.CP_BODY to body,
                    CpImageInsertUseCase.POST_ID to cpId,
                    CpCreationUseCase.CP_ACTION_TYPE to CpCreationUseCase.Companion.ENTITY_ACTION_TYPE.UPDATE,
                    CpCreationUseCase.CP_USER_DATA to SSO.getLoginResponse()))
        } else {
            cpPollUseCase.execute(bundleOf(CpPollInsertUseCase.POLL_POJO to
                    PostPollPojo(
                            pollTitle = body,
                            duration = p_duration,
                            options = pollOptions.toList()
                    ),
                    CpCreationUseCase.CP_USER_DATA to SSO.getLoginResponse(),
                    CpImageInsertUseCase.POST_ID to cpId
            ))
        }
        serviceRunning = true
    }


    fun fetchRepostItemFromNetwork(): LiveData<PostEntity?> {
        return transform(detailsAPIUsecase.apply {
            execute(bundleOf(Constants.BUNDLE_POST_ID to postId))
        }.data())
    }

    fun fetchRepostItemFromDB(): LiveData<PostEntity?> {
        return Transformations.switchMap(SocialDB.instance().fetchDao().detailCardByPostId(parentPostId)) { dc: DetailCard? ->
            if (dc != null) MutableLiveData<PostEntity>().apply {
                value = dc.rootPostEntity()
            }
            else Transformations.switchMap(SocialDB.instance().fetchDao().detailCardByPostId
            (parentId)) {
                val ld = MutableLiveData<PostEntity>()
                ld.value = it?.i_collectionItems()?.find { it.i_id() == parentPostId } as? PostEntity
                ld
            }
        }
    }

    fun fetchOmbed(url: String) {
        if(cpId > -1) {
            embedUseCase.execute(
                bundleOf(
                    OEmbedUsecase.OEMBED_URL to url,
                    CpImageInsertUseCase.POST_ID to cpId,
                    OEmbedUsecase.ACTION_TYPE to OEmbedUsecase.Companion.OEMBED_ACTION_TYPE.UPDATE
                )
            )
        }
    }

    fun removeOmbed() {
        embedUseCase.execute(bundleOf(CpImageInsertUseCase.POST_ID to cpId,
                OEmbedUsecase.ACTION_TYPE to OEmbedUsecase.Companion.OEMBED_ACTION_TYPE.REMOVE))
    }

    private val LOG_TAG = "CreatePostViewModel"
    fun addRepostCard(repostEntity: PostEntity) {
        val postAsset = repostEntity.rootPostEntity() ?: run {
            Logger.e(LOG_TAG, "addRepostCard: rootPostEntity is NULL")
            return
        }
        val repostAsset = RepostAsset(
                postAsset.id, postAsset.type, postAsset.format, postAsset.thumbnailInfos,
                postAsset.subFormat, postAsset.uiType, postAsset.content, postAsset.source,
                postAsset.title, postAsset.maxImageViewportHeightPercentage, postAsset.titleEnglish, postAsset.imageCount, postAsset
                .linkAsset,
                postAsset.viral, postAsset.videoAsset)
        cpRepostUseCase.execute(
                bundleOf(
                        CpRepostInsertUseCase.REPOST_POJO to repostAsset,
                        CpImageInsertUseCase.POST_ID to cpId
                )
        )
    }

    fun stop() {
        //remove entry from table
        if (!serviceRunning)
            cpCreationUseCase.execute(bundleOf(
                    CpCreationUseCase.CP_ACTION_TYPE to CpCreationUseCase.Companion.ENTITY_ACTION_TYPE.REMOVE,
                    CpImageInsertUseCase.POST_ID to cpId
            ))

        if (isBusRegistered) {
            BusProvider.getRestBusInstance().unregister(this)
            isBusRegistered = false
        }
        hashTagViewModel.kill()
        //moved de-refrence to destroy of lifecycle of view
        vi = null
    }

    fun start(intent: Intent) {
        if (!isBusRegistered) {
            BusProvider.getRestBusInstance().register(this)
            isBusRegistered = true
        }
        //handle intent data
        parentPostId = intent.getStringExtra(Constants.BUNDLE_POST_ID) ?: ""
        parentId = intent.getStringExtra(Constants.BUNDLE_PARENT_ID) ?: ""
        viewType = PostType.valueOf(intent.getStringExtra(Constants.BUNDLE_VIEW_TYPE) ?: "TEXT")
        uiMode = intent.getSerializableExtra(Constants.BUNDLE_MODE) as? CreatePostUiMode
                ?: CreatePostUiMode.POST

        var location: PostCurrentPlace? = null
        val isLocationEnable = PreferenceManager.getPreference(AppStatePreference.POST_CREATE_LOCATION_ENABLE, true)
        if (isLocationEnable && uiMode != CreatePostUiMode.COMMENT
                && uiMode != CreatePostUiMode.REPLY) {
            location = JsonUtils.fromJson<PostCurrentPlace>(PreferenceManager.getPreference(AppStatePreference.POST_CREATE_DEFAULT_LOCATION, ""),
                    PostCurrentPlace::class.java)
        }

        location?.let {
            locationData.value = location
        }

        val localInf = intent.getSerializableExtra(Constants.BUNDLE_CREATE_POST_NEXT_CARD_ID_FOR_LOCAL_CARD) as? LocalInfo

        cpCreationUseCase.execute(
                bundleOf(
                        CpCreationUseCase.CP_ENTITY to CreatePostEntity(
                                text = Constants.EMPTY_STRING,
                                uiMode = uiMode,
                                type = PostType.TEXT.postType,
                                title = Constants.EMPTY_STRING,
                                parentId = parentId,
                                parentPostId = parentPostId,
                                language = UserPreferenceUtil.getUserPrimaryLanguage(),
                                commentParams = when (uiMode) {
                                    CreatePostUiMode.COMMENT, CreatePostUiMode.REPLY -> {
                                        hashMapOf("postId" to parentPostId, "type" to viewType)
                                    }
                                    else -> hashMapOf()
                                },
                                userLocation = location,
                                localInfo = localInf),
                        CpCreationUseCase.CP_ACTION_TYPE to CpCreationUseCase.Companion.ENTITY_ACTION_TYPE.NEW
                )
        )
        cpCreationUseCase.data().take1().observe(vi?.lifecyleOwner()!!, Observer {
            if (it.isSuccess) {
                cpId = it.getOrNull()?.id ?: -1
                //checking if images shared into DH app
                imgData.postValue(intent.getFromStream().mapNotNull { img: Uri -> transformImg(img) }
                        .toTypedArray())
                cpIdData.postValue(cpId)
            }
        })

        hashTagViewModel.start()

        compressImage = PreferenceManager.getPreference(
                AppStatePreference.POST_CREATE_COMPRESS_IMAGE, false
        )
        compressQuality = PreferenceManager.getPreference(
                AppStatePreference.POST_CREATE_COMPRESS_IMAGE_QUALITY, DEFAULT_IMAGE_COMPRESS_QUALITY
        )

    }

    override fun onCleared() {
        embedUseCase.dispose()
        cpLocUseCase.dispose()
        cpPrivacyUseCase.dispose()
        super.onCleared()
    }

    class Factory @Inject constructor(private val app: Application,
                                      private val postId: String,
                                      private val embedUseCase: MediatorUsecase<Bundle, OEmbedResponse>,
                                      private val cpcreationUseCase: CpCreationUseCase,
                                      private val cplocUseCase: CpLocationInsertUseCase,
                                      private val cppollUseCase: CpPollInsertUseCase,
                                      private val privacyUseCase: CpPrivacyUseCase,
                                      private val htVM: HashTagViewModel,
                                      private val cpReadUseCase: CpReadUseCase,
                                      private val cpRepostUseCase: CpRepostInsertUseCase,
                                      private val detailsAPIUsecase: FetchParentUsecase) :
            ViewModelProvider
    .AndroidViewModelFactory(app) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CreatePostViewModel(app, postId, embedUseCase,
                    cpcreationUseCase.toMediator2(true),
                    cplocUseCase.toMediator2(true),
                    cppollUseCase.toMediator2(true),
                    privacyUseCase.toMediator2(true),
                    htVM,
                    cpReadUseCase.toMediator2(true),
                    cpRepostUseCase.toMediator2(true),
                    detailsAPIUsecase.toMediator2(true)) as T
        }
    }

}

class HashTagViewModel @Inject constructor(private val searchVM: SearchViewModel) : ViewModel(), SearchActvityInterface {

    private val TAG = HashTagViewModel::class.java.simpleName
    private var view: CreatePostView? = null
    private val tagObserver = Observer<Phrase> { phrase ->
        var queryToken: QueryToken? = null
        var editableText: String = ""
        when (phrase) {
            is Added -> {
                queryToken = phrase.queryToken
                editableText = phrase.editable ?: ""
            }
        }
        queryToken?.let {
            searchVM.typing(Triple(it.tokenString, it.tokenString.hashCode().toString(), editableText))
        }
    }

    fun attachView(view: CreatePostView) {
        this.view = view
    }

    fun kill() {
        view = null
    }

    fun start() {
        view?.initSuggestions()
    }

    fun getTagObserver(): Observer<Phrase> = tagObserver

    override fun searchViewModel(): SearchViewModel = searchVM

    override fun submitQuery(query: SearchQuery, searchtype: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editQuery(query: SearchSuggestionItem?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun curReferrer(): PageReferrer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateReferrer(referrer: PageReferrer) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProvidedReferrer(): PageReferrer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getReferrerEventSection(): NhAnalyticsEventSection {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


