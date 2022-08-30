package com.newshunt.appview.common.postcreation.view.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.JobIntentService
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.CreatePostModule
import com.newshunt.appview.common.postcreation.DaggerUploadJobServiceComponent
import com.newshunt.appview.common.postcreation.UploadJobServiceModule
import com.newshunt.appview.common.postcreation.analytics.entity.CreatePostPublishStatus
import com.newshunt.appview.common.postcreation.analytics.helper.CreatePostAnalyticsHelper
import com.newshunt.appview.common.postcreation.view.helper.CountingRequestBody
import com.newshunt.appview.common.postcreation.view.helper.PostConstants
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.IMAGE_UPLOAD
import com.newshunt.appview.common.postcreation.view.helper.PostNotificationHelper
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.view.UniqueIdHelper
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.JsPostActionParam
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.ImageUpload
import com.newshunt.dataentity.common.asset.PostCreateAsset
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.PostImage
import com.newshunt.dataentity.common.asset.isCommentOrRepost
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.CreatePost
import com.newshunt.dataentity.social.entity.DEFAULT_NOTIFICATION_REMOVAL_DELAY
import com.newshunt.dataentity.social.entity.ImageEntity
import com.newshunt.dataentity.social.entity.PostUploadStatus
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.disposeIfNeeded
import com.newshunt.dhutil.helper.APIUtils
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.iterate
import com.newshunt.news.model.apis.ImageUploadService
import com.newshunt.news.model.apis.PostCreationService
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.ImageDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.CpCreationUseCase
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

//TODO: @Rahul Move out from intent service to using a service for asynchronous callbacks

class UploadJobService : JobIntentService() {
    private var uniqueNotificationId = UniqueIdHelper.getInstance().generateUniqueId()
    @Inject
    lateinit var postService: PostCreationService
    @Inject
    lateinit var imgService: ImageUploadService
    private var cpId: Long = -1
    @Inject
    lateinit var imgDao: ImageDao
    @Inject
    lateinit var cpDao: CreatePostDao
    @Inject
    lateinit var fetchDao: FetchDao
    @Inject
    lateinit var followDao: FollowEntityDao
    @Inject
    lateinit var cpCreationUseCase: CpCreationUseCase

    private val mpindexval = AtomicReference<String>()
    private val mpindex = AtomicInteger(1)
    private var removeNotificationDelay: Long = DEFAULT_NOTIFICATION_REMOVAL_DELAY
    private var pageReferrer: PageReferrer? = null
    private var isExternalShare = false

    override fun onCreate() {
        super.onCreate()
        DaggerUploadJobServiceComponent.builder()
                .createPostModule(CreatePostModule(postId = "", socialDb = SocialDB.instance()))
                .uploadJobServiceModule(UploadJobServiceModule()).build()
                .inject(this)
        removeNotificationDelay = PreferenceManager.getPreference(
            AppStatePreference.POST_CREATE_NOTIFICATION_REMOVAL_DELAY,
            DEFAULT_NOTIFICATION_REMOVAL_DELAY
        )
    }

    override fun onHandleWork(intent: Intent) {
        Logger.d(TAG, "##### Job started #####")

        cpId = if (intent.getLongExtra(POST_CP_ID, -1) == cpId) -1
        else intent.getLongExtra(POST_CP_ID, -1)

        Logger.d(TAG, "Job with post_id : $cpId")
        pageReferrer = intent.extras?.get(BUNDLE_PAGE_REFERRER) as? PageReferrer?
        if(pageReferrer == null){
            pageReferrer = PageReferrer(NhGenericReferrer.CREATE_POST_HOME)
        }
        if (cpId == -1L) {
            Logger.e(TAG, "did not get a post_id to publish. Killing service")
            return
        }

        isExternalShare = intent.getBooleanExtra(IS_EXTERNAL_SHARE, false)

        //Generate unique notification id for each request
        uniqueNotificationId = UniqueIdHelper.getInstance().generateUniqueId()

        //Clear existing notification - e.g in case of retry from local card
        val createPost = cpDao.cpbypostID(cpId)
        val oldNotificationId = createPost?.cpEntity?.notificationId ?: -1
        PostNotificationHelper.clearNotification(oldNotificationId)
        createPost?.let {
            cpDao.update(createPost.cpEntity.copy(notificationId = uniqueNotificationId))
        }

        val images = imgDao.imgbypostID(cpId.toInt())
        if (images.isEmpty()) {
            cpApi(cpId)
            return
        }

        Flowable.create({ em: FlowableEmitter<Pair<String, Double>> ->
            //check if img asset were previously uploaded and the post failed. Repost again
            if (images.all { it.serverGenId.isNotEmpty() && it.imgUploaded }) {
                Logger.d(TAG, "img asset uploaded previously retrying CP api call")
                cpApi(cpId)
            } else {
                Logger.d(TAG, "uploading image assets")
                imgService.uploadImages(prepareRotationDetailsPart(images) ,
                    images.map { entity -> prepareFilePart(entity.imgPath, em) })
                        .repeatWhen({ it })
                        .takeUntil { it.code == 200 }
                        .filter { it.code == 200 && it.paths.isNullOrEmpty().not() }
                        .blockingSubscribe({ imgSuccessPojo: ImageUpload ->

                            // update db with asset_id from BE and save state in DB
                            imgSuccessPojo.paths?.let {
                                (images to it).iterate().forEach { pair ->
                                    val (ie, id) = pair
                                    imgDao.updateServerState(ie?.imgPath!!, id!!)
                                }
                            }

                            val cp = cpDao.cpbypostID(cpId) ?: return@blockingSubscribe
                            val attachmentAsset = PostCreateAsset(
                                    images = images.mapIndexed { i, imageEntity ->
                                        PostImage(
                                                id = imgSuccessPojo.paths?.get(i),
                                                height = imageEntity.imgHeight,
                                                width = imageEntity.imgWidth,
                                                orientation = imageEntity.imgOrientation,
                                                resolution = imageEntity.imgRes,
                                                format = imageEntity.imgFormat)
                                    }
                            )
                            uploadPost(attachmentAsset, cp, true)

                        }, {
                            Logger.caughtException(it)
                            cpDao.updatePostState(
                                    cpId = cpId.toInt(),
                                    state = PostUploadStatus.FAILED,
                                    progress = 0,
                                    isLocalCardShown = true,
                                    fetchDao = fetchDao,
                                    snackbarMessage = null,
                                    groupJoined = false)
                            createNotificationWithProgress(0.toDouble(), PostNotificationHelper
                                .CreatePostNotificationStatus.FAILURE, true)
                        })
            }
        }, BackpressureStrategy.LATEST)
                .doOnSubscribe {
                    cpDao.updatePostState(
                            cpId = cpId.toInt(),
                            state = PostUploadStatus.UPLOADING,
                            progress = 50,
                            isLocalCardShown = true, fetchDao = fetchDao,
                            snackbarMessage = null,
                            groupJoined = false)
                }
                .subscribe {
                    val (img_path, _) = it
                    val old_img = mpindexval.get()
                    if (old_img != img_path) {
                        mpindexval.set(img_path)
                        val index =
                            if (old_img == img_path) mpindex.get() else mpindex.incrementAndGet()
                        val imgSize = images.size
                        val prog = (50 / imgSize).times(index)
                        createNotificationWithProgress(
                            prog.toDouble(), PostNotificationHelper
                                .CreatePostNotificationStatus.PROGRESS, true
                        )
                    }
                }.disposeIfNeeded()
    }


    private fun cpApi(cp_id: Long) {
        val cp = cpDao.cpbypostID(cp_id) ?: return
        cpDao.update(cp.cpEntity.copy(
                progress = 50,
                isLocalcardShown = true))
        uploadPost(if(cp.images.isEmpty()) null else PostCreateAsset(
                images = cp.images.map{ imageEntity ->
                            PostImage(
                                    id = imageEntity.serverGenId,
                                    height = imageEntity.imgHeight,
                                    width = imageEntity.imgWidth,
                                    orientation = imageEntity.imgOrientation,
                                    resolution = imageEntity.imgRes,
                                    format = imageEntity.imgFormat)
                        }
                ), cp)
    }

    private fun uploadPost(cpPostAsset: PostCreateAsset?, cp: CreatePost,
                           isImageAttached: Boolean = false) {
        Logger.d(TAG, "##### CP API ######")
        val postBody = cp.cpEntity.buildPojo(cpPostAsset)
        if (postBody.uiMode != CreatePostUiMode.COMMENT &&
                postBody.uiMode != CreatePostUiMode.REPLY) {
            val post = cp.toPostWithLocalInfo()
            fetchDao.insertLocalPost(listOf(post), followDao, donotmarkReadInForyou = isExternalShare)
            Logger.d(TAG, "localpost cpid = ${post.localInfo?.cpId}")
        }
        val type = if (postBody.uiMode == CreatePostUiMode.REPLY) {
            CreatePostUiMode.COMMENT.name.toLowerCase(Locale.ENGLISH)
        } else {
            postBody.uiMode.name.toLowerCase(Locale.ENGLISH)
        }
        postService.createPost(postBody, type, postBody.id ?: "")
                .blockingSubscribe({
                    Logger.d(TAG, "****** post creation success ***** ")
                    createNotificationWithProgress(100.toDouble(), PostNotificationHelper
                        .CreatePostNotificationStatus.SUCCESS, isImageAttached, postBody.uiMode)
                    val postEntity: PostEntity? = it.body().data
                    cpDao.updatePostState(
                            cpId = cpId.toInt(),
                            state = PostUploadStatus.SUCCESS,
                            progress = 100,
                            isLocalCardShown = true,
                            fetchDao = fetchDao,
                            snackbarMessage = postEntity?.message,
                            groupJoined = postEntity?.groupJoined)

                    if (postBody.uiMode != CreatePostUiMode.COMMENT &&
                        postBody.uiMode != CreatePostUiMode.REPLY) {
                        postEntity?.let { entity ->
                            fetchDao.insertLocalPost(listOf(entity),followDao, true,
                                    donotmarkReadInForyou = isExternalShare)
                        }
                    } else {
                        postEntity?.let { entity ->
                            entity.level = PostEntityLevel.LOCAL_COMMENT
                            SocialDB.instance().postDao().insReplace(entity)
                        }
                    }

                    AndroidUtils.getMainThreadHandler().post {
                        if(!CommonUtils.isEmpty(postBody.parentId) && postBody.uiMode.isCommentOrRepost()){
                            BusProvider.getUIBusInstance().post(JsPostActionParam(
                                    postBody.parentId?:"", postBody.uiMode.name))
                        }
                        if (postBody.uiMode == CreatePostUiMode.COMMENT ||
                            postBody.uiMode == CreatePostUiMode.REPLY
                        ) {
                            val message = CommonUtils.getString(R.string.post_comment_success)
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                        }
                        CreatePostAnalyticsHelper.logCreatePostPublishStatusEvent(pageReferrer!!,
                                CreatePostPublishStatus.SUCCESS, null)
                    }
                }, {
                    cpDao.updatePostState(
                            cpId = cpId.toInt(),
                            isLocalCardShown = true,
                            progress = 0,
                            state = PostUploadStatus.FAILED,
                            fetchDao = fetchDao,
                            snackbarMessage = null,
                            groupJoined = false)
                    createNotificationWithProgress(0.toDouble(), PostNotificationHelper
                        .CreatePostNotificationStatus.FAILURE, isImageAttached, postBody.uiMode)
                    Logger.e(TAG, "****** post creation failure ***** ")
                    Logger.caughtException(it)
                    AndroidUtils.getMainThreadHandler().post{
                        if (postBody.uiMode == CreatePostUiMode.COMMENT ||
                            postBody.uiMode == CreatePostUiMode.REPLY
                        ) {
                            val message = CommonUtils.getString(R.string.post_comment_failure)
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                        }
                        val baseError = APIUtils.getError(it)
                        if(baseError != null){
                            CreatePostAnalyticsHelper.logCreatePostPublishStatusEvent(pageReferrer!!,
                                    CreatePostPublishStatus.FAILURE, baseError)
                        }
                    }
                })
    }

    private fun deleteCopiedImages() {
        if (cpId == -1L)
            return
        try {
            val imageList = imgDao.imgbypostID(cpId.toInt())
            imageList.forEach {
                val imagePath = it.imgPath
                if (imagePath.isNotEmpty()) {
                    val file = File(imagePath)
                    if (file.exists() && file.name.contains(
                            PostConstants.POST_CREATE_IMAGE_FILE_NAME, ignoreCase = true
                        )
                    ) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun prepareRotationDetailsPart(imageList: List<ImageEntity>): RequestBody {
        val rotateDesc =
            imageList.joinToString(separator = "|") {
                val file = File(it.imgPath)
                "${file.name}=${it.imgOrientation}"
            }
        return RequestBody.create(MediaType.parse("multipart/form-data"), rotateDesc)
    }

    private fun prepareFilePart(imagePath: String, emitter: FlowableEmitter<Pair<String, Double>>):
            MultipartBody.Part {
        // MultipartBody.Part is used to send also the actual file name
        val file = File(imagePath)
        return MultipartBody.Part.createFormData(IMAGE_UPLOAD, file.name,
                createCountingRequestBody(imagePath, file, emitter))
    }

    private fun createCountingRequestBody(
            imagePath: String,
            file: File,
            emitter: FlowableEmitter<Pair<String, Double>>
    ): RequestBody {
        val requestBody = createRequestBodyFromFile(file)
        return CountingRequestBody(requestBody, imagePath) { f, b, cl ->
            emitter.onNext(Pair(f, 1.0 * b / cl))
        }
    }

    private fun createRequestBodyFromFile(file: File): RequestBody {
        return RequestBody.create(MediaType.parse("image/*"), file)
    }

    private fun createNotificationWithProgress(
        progress: Double, state: PostNotificationHelper.CreatePostNotificationStatus,
        isImageAttached: Boolean = false, uiMode: CreatePostUiMode = CreatePostUiMode.POST) {
        //Do not show any notification for comment and reply mode
        if(uiMode == CreatePostUiMode.COMMENT || uiMode == CreatePostUiMode.REPLY) return
        PostNotificationHelper.buildNotification(uniqueNotificationId, progress.toInt(), state,
            cpId, isImageAttached)
        //clear notification once success
        if (state == PostNotificationHelper.CreatePostNotificationStatus.SUCCESS) {
            clearNotificationWithDelay()
        }
    }

    private fun clearNotificationWithDelay() {
        val id = uniqueNotificationId
        AndroidUtils.getMainThreadHandler().postDelayed({
            Executors.newSingleThreadExecutor().execute {
                PostNotificationHelper.clearNotification(id)
            }
        }, removeNotificationDelay)
    }

    companion object {
        val TAG = UploadJobService::class.java.simpleName
        const val POST_CP_ID = "post_cp_id"
        const val IS_RETRY = "retry"
        const val BUNDLE_PAGE_REFERRER = "pagereferrer"
        const val IS_EXTERNAL_SHARE = "is_external_share"

        fun enqueueWork(context: Context = CommonUtils.getApplication(), bundle: Bundle) {
            val intent = Intent(context, UploadJobService::class.java)
            intent.putExtras(bundle)
            enqueueWork(context, UploadJobService::class.java, PostConstants.POST_JOB_ID, intent)
        }

        @JvmStatic
        fun retry(cp_id: Long) {
            enqueueWork(bundle = bundleOf(POST_CP_ID to cp_id, IS_RETRY to true))
        }
    }
}


