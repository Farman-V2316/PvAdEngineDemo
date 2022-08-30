/*
 * Created by Rahul Ravindran at 8/1/20 5:05 PM
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.asset.PostMeta
import com.newshunt.dataentity.common.asset.PostPollPojo
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.asset.PostType
import com.newshunt.dataentity.common.asset.RepostAsset
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.CreatePost
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.CreatePostID
import com.newshunt.dataentity.social.entity.ImageEntity
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.daos.ImageDao
import io.reactivex.Observable
import java.io.Serializable
import java.util.*
import javax.inject.Inject


/*
*  Updates user location in cp_entity table
*  Looks up by cp_id and updates location object
* */
class CpLocationInsertUseCase @Inject constructor(private val cpdao: CreatePostDao) :
        BundleUsecase<Boolean> {
    companion object {
        const val LOC_POJO = "loc_pojo"
    }

    override fun invoke(p1: Bundle): Observable<Boolean> {
        return Observable.fromCallable {
            val loc = p1.getSerializable(LOC_POJO) as? PostCurrentPlace
            //check if lat,lng not available from api. Pick up from the address
            if (loc?.latitude == 0.0 && loc?.longitude == 0.0 && Geocoder.isPresent()) {
                val geoCoder = Geocoder(CommonUtils.getApplication(), Locale.getDefault())
                try {
                    val address = geoCoder.getFromLocationName(loc?.name, 1)
                    val adr: Address = address[0]
                    loc?.latitude = adr.latitude
                    loc?.longitude = adr.longitude
                } catch (e: Exception) {
                    Logger.caughtException(e)
                }
            }
            val cpId = p1.getLong(CpImageInsertUseCase.POST_ID)
            check(cpId > 0) { "cpID is null" }

            val entity = cpdao.cpentityByID(cpId.toInt())
            entity?.let {
                it.userLocation = loc
                cpdao.update(it)
                return@fromCallable true
            } ?: return@fromCallable false
        }
    }
}

/*
*  Inserts to img_entity table
*  Actions
*   1. Single entry
*   2. Delete all entries[$IS_REMOVE]
* */
class CpImageInsertUseCase @Inject constructor(private val imgdao: ImageDao) :
        BundleUsecase<Boolean> {
    companion object {
        const val IMG_ENTITY = "img_entity"
        const val IS_REMOVE = "is_remove"
        const val POST_ID = "post_id"
    }

    override fun invoke(p1: Bundle): Observable<Boolean> {
        return Observable.fromCallable {
            val entity = p1.getSerializable(IMG_ENTITY) as ImageEntity
            val isRemove = p1.getBoolean(IS_REMOVE)
            val postId = p1.getLong(POST_ID, -1)
            if (isRemove && postId > -1) {
                imgdao.removeImg(postId.toInt(), entity.imgPath)
            } else {
                imgdao.insReplace(entity)
            }
            true
        }
    }
}

/*
*  Fetch images  by cp_id from img_enity table
*
*/
class CpImageReadUseCase @Inject constructor(private val imgdao: ImageDao) :
        BundleUsecase<List<ImageEntity>> {
    override fun invoke(p1: Bundle): Observable<List<ImageEntity>> {
        return Observable.fromCallable {
            val cpId = p1.getLong(CpImageInsertUseCase.POST_ID, -1)
            if (cpId != -1L) {
                imgdao.imgbypostID(cpId.toInt())
            } else emptyList()
        }
    }
}


/*
*  Creation of CP entity in cp_entity table
*  Actions
*   1. Inserts new CP entry
*   2. Updates text as well as resets to default TEXT type
*   3. Delete create entry if no post action is triggered [*includes deletion img_entity]
* */
class CpCreationUseCase @Inject constructor(private val cpdao: CreatePostDao) :
        BundleUsecase<CreatePostID> {
    companion object {
        const val CP_ENTITY = "cp_entity"
        const val CP_BODY = "cp_text"
        const val CP_USER_DATA = "cp_user_data"
        const val CP_ACTION_TYPE = "cp_action_type"

        enum class ENTITY_ACTION_TYPE: Serializable {
            REMOVE, UPDATE, NEW
        }
    }


    override fun invoke(p1: Bundle): Observable<CreatePostID> {
        return Observable.fromCallable {
            val entity = p1.getSerializable(CP_ENTITY) as? CreatePostEntity
            val text = p1.getString(CP_BODY) ?: ""
            val cpId = p1.getLong(CpImageInsertUseCase.POST_ID)
            val userdata = p1.getSerializable(CP_USER_DATA) as? UserLoginResponse
            val actionType = p1.getSerializable(CP_ACTION_TYPE) as? ENTITY_ACTION_TYPE

            actionType ?: return@fromCallable CreatePostID.CP_ID_NOT_FOUND

            entity ?: kotlin.run {
                return@fromCallable when (actionType) {
                    ENTITY_ACTION_TYPE.REMOVE -> {
                        if (cpId <= 0L) CreatePostID.CP_ID_NOT_FOUND else {
                            cpdao.delete(cpId.toInt())
                            CreatePostID.CP_ID_NOT_FOUND
                        }
                    }
                    ENTITY_ACTION_TYPE.UPDATE -> {
                        if (cpId <= 0) return@fromCallable CreatePostID.CP_ID_NOT_FOUND
                        val cpEntity = cpdao.cpentityByID(cpId.toInt())
                        cpEntity ?: return@fromCallable CreatePostID.CP_ID_NOT_FOUND
                        cpdao.update(
                                cpEntity.copy(
                                        postId = AppUserPreferenceUtils.generateRandomUserIdForPost(),
                                        text = text,
                                        type = PostType.TEXT.postType,
                                        poll = null,
                                        userData = userdata?.let {
                                            PostSourceAsset(
                                                    id = it.userId,
                                                    handle = it.handle,
                                                    displayName = it.name,
                                                    imageUrl = it.profileImage
                                            )
                                        },
                                        uiType = UiType2.NORMAL.name))
                        CreatePostID(cpId, CreatePostID.CP_OP.UPDATE)
                    }
                    else -> CreatePostID.CP_ID_NOT_FOUND
                }
            }
            return@fromCallable if (actionType == ENTITY_ACTION_TYPE.NEW) CreatePostID(cpdao.replaceCP(entity)[0], CreatePostID.CP_OP.ADD)
            else CreatePostID.CP_ID_NOT_FOUND
        }
    }
}


/*
* Read CP entity from cp_entity table. Converts to @see com.newshunt.dataentity.social.entity.CreatePost
* */
class CpReadUseCase @Inject constructor(private val cpdao: CreatePostDao) :
        BundleUsecase<CreatePost?> {
    override fun invoke(p1: Bundle): Observable<CreatePost?> {
        return Observable.fromCallable {
            val cpId = p1.getLong(CpImageInsertUseCase.POST_ID, -1)
            if (cpId > -1) cpdao.cpbypostID(cpId) else null
        }
    }
}


/*
* Updates privacy state like PUBLIC,PRIVATE or allowComments for a post in cp_entity
* */
class CpPrivacyUseCase @Inject constructor(private val cpdao: CreatePostDao) :
        BundleUsecase<Boolean> {

    override fun invoke(p1: Bundle): Observable<Boolean> {
        return Observable.fromCallable {
            val privacyMeta = p1.getSerializable("POST_META_RESULT") as? PostMeta
            require(privacyMeta != null) { "privacy meta is null" }
            val cp_id = p1.getInt(CpImageInsertUseCase.POST_ID, -1)
            require(cp_id > 0) { "cpID id null" }
            cpdao.updatePostMeta(
                    postId = cp_id,
                    level = privacyMeta.privacyLevel.name,
                    allowComments = privacyMeta.allowComments)
            true
        }
    }
}


/*
* Updates poll data in cp_entity table
* */
class CpPollInsertUseCase @Inject constructor(private val cpdao: CreatePostDao) :
        BundleUsecase<Boolean> {
    companion object {
        const val POLL_POJO = "poll_pojo"
    }

    override fun invoke(p1: Bundle): Observable<Boolean> {
        return Observable.fromCallable {
            val poll = p1.getSerializable(POLL_POJO) as? PostPollPojo
            poll ?: return@fromCallable false

            val cpId = p1.getLong(CpImageInsertUseCase.POST_ID)
            if (cpId <= 0) return@fromCallable false

            val entity = cpdao.cpentityByID(cpId.toInt())
            entity ?: return@fromCallable false

            val userData = p1.getSerializable(CpCreationUseCase.CP_USER_DATA) as? UserLoginResponse

            cpdao.update(entity.copy(
                    postId = AppUserPreferenceUtils.generateRandomUserIdForPost(),
                    type = PostType.POLL.postType,
                    text = poll.pollTitle,
                    poll = poll,
                    userData = userData?.let {
                        PostSourceAsset(
                                id = it.userId,
                                handle = it.handle,
                                displayName = it.name,
                                imageUrl = it.profileImage)
                    },
                    uiType = UiType2.HORIZONTAL_BAR.name))
            true

        }
    }
}


/*
* Updates repost data in cp_entity table
* */
class CpRepostInsertUseCase @Inject constructor(private val cpdao: CreatePostDao) :
        BundleUsecase<Boolean> {
    companion object {
        const val REPOST_POJO = "repost_pojo"
    }

    override fun invoke(p1: Bundle): Observable<Boolean> {
        return Observable.fromCallable {
            val repostAsset = p1.getSerializable(REPOST_POJO) as? RepostAsset
            val cpId = p1.getLong(CpImageInsertUseCase.POST_ID)

            check(repostAsset != null) { "No repost asset" }
            check(cpId > 0) { "invalid cpID" }
            val entity = cpdao.cpentityByID(cpId.toInt())
            check(entity != null) { " null entity" }
            cpdao.update(entity.copy(
                    repostAsset = repostAsset
            ))
            true
        }
    }
}