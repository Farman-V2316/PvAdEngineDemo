/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.sqlite

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.ads.FcCounter
import com.newshunt.dataentity.common.asset.BackgroundType2
import com.newshunt.dataentity.common.asset.ColdStartEntityType
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.DetailAttachLocation
import com.newshunt.dataentity.common.asset.DistancingSpec
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.HastTagAsset
import com.newshunt.dataentity.common.asset.LocationEntityLevel
import com.newshunt.dataentity.common.asset.NodeType2
import com.newshunt.dataentity.common.asset.PollOptions
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.PostPollOption
import com.newshunt.dataentity.common.asset.PostPrivacy
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.asset.RepostAsset
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.Ticker2
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.follow.entity.FollowBlockConfigWrapper
import com.newshunt.dataentity.common.follow.entity.FollowBlockLangConfig
import com.newshunt.dataentity.common.follow.entity.FollowEntityType
import com.newshunt.dataentity.common.follow.entity.FollowUnFollowReason
import com.newshunt.dataentity.common.model.entity.SyncStatus
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.GradientItem
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.dhutil.model.entity.asset.ImageDetail
import com.newshunt.dataentity.model.entity.AccountPermission
import com.newshunt.dataentity.model.entity.BookMarkAction
import com.newshunt.dataentity.model.entity.MemberRole
import com.newshunt.dataentity.model.entity.MembershipStatus
import com.newshunt.dataentity.model.entity.ProfilePersona
import com.newshunt.dataentity.model.entity.SettingState
import com.newshunt.dataentity.model.entity.SocialPrivacy
import com.newshunt.dataentity.news.model.entity.DisplayLocation
import com.newshunt.dataentity.news.model.entity.server.asset.AnimationType
import com.newshunt.dataentity.news.model.entity.server.asset.CardLabelBGType
import com.newshunt.dataentity.news.model.entity.server.asset.CardLabelType
import com.newshunt.dataentity.news.model.entity.server.asset.CardLandingType
import com.newshunt.dataentity.news.model.entity.server.asset.PostState
import com.newshunt.dataentity.news.model.entity.server.navigation.SectionType
import com.newshunt.dataentity.social.entity.AdSpec
import com.newshunt.dataentity.social.entity.PostUploadStatus
import com.newshunt.news.model.utils.SerializationUtils
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SocialTypeConv {
    private val LOG_TAG = "SocialTypeConv"
    private val INVALID_POSTENTITY = PostEntity(id = Constants.INVALID_POSTENTITY_ID)
    //[ enums

    @TypeConverter
    fun reason(reason: FollowUnFollowReason): String = reason.reason

    @TypeConverter
    fun reasonBack(string: String): FollowUnFollowReason = FollowUnFollowReason.getValue(string)


    @TypeConverter
    fun toFollowEntity(entityType: String?): FollowEntityType? {
        return FollowEntityType.from(entityType ?: Constants.EMPTY_STRING)
    }

    @TypeConverter
    fun toString(entityType: FollowEntityType?): String? {
        return entityType?.name
    }

    @TypeConverter
    fun toStr(uiType: UiType2?) = uiType?.name

    @TypeConverter
    fun toUIType(uiType: String?) = uiType?.let { UiType2.valueOf(it) }

    @TypeConverter
    fun toStr(t: CardLandingType?) = t?.name

    @TypeConverter
    fun toCardLandingType(t: String?) = t?.let { CardLandingType.valueOf(it) }

    @TypeConverter
    fun toStr(t: CardLabelType?) = t?.name

    @TypeConverter
    fun toCardLabelType(t: String?) = t?.let { CardLabelType.valueOf(it) }

    @TypeConverter
    fun toStr(t: CardLabelBGType?) = t?.name

    @TypeConverter
    fun toCardLabelBGType(t: String?) = t?.let { CardLabelBGType.valueOf(it) }

    @TypeConverter
    fun toStr(t: BackgroundType2?) = t?.name

    @TypeConverter
    fun toBackgroundType2(t: String?) = t?.let { BackgroundType2.valueOf(it) }

    @TypeConverter
    fun toStr(t: DisplayLocation?) = t?.name

    @TypeConverter
    fun toDisplayLocation(t: String?) = t?.let { DisplayLocation.valueOf(it) }

    @TypeConverter
    fun toStr(t: PostPrivacy?) = t?.name

    @TypeConverter
    fun toPostPrivacy(t: String?) = t?.let { PostPrivacy.valueOf(it) }

    @TypeConverter
    fun toStr(t: PostState?) = t?.name

    @TypeConverter
    fun toPostState(t: String?) = t?.let { PostState.valueOf(it) }

    @TypeConverter
    fun toStr(t: PostEntityLevel?) = t?.name

    @TypeConverter
    fun toPostEntityLevel(t: String?) = t?.let { PostEntityLevel.valueOf(t) }

    @TypeConverter
    fun toStr(t: LocationEntityLevel?) = t?.name

    @TypeConverter
    fun toLocationEntityLevel(t: String?) = t?.let { LocationEntityLevel.valueOf(t) }

    @TypeConverter
    fun toStr(t: NodeType2?) = t?.name

    @TypeConverter
    fun toNodeType2(t: String?) = t?.let { NodeType2.valueOf(it) }


    // enums ]

    // [collections

    // TODO(satosh.dhanyamraju): test this
    @TypeConverter
    fun toStr(t: Map<String, String>?) = JsonUtils.toJson(t)

    @TypeConverter
    fun toMap(t: String?) = t.let {
        JsonUtils.fromJson<Map<String, String>?>(t, object : TypeToken<Map<String, String>?>() {}.type)
    }

    @TypeConverter
    fun toStr(t: List<String>?) = JsonUtils.toJson(t)

    @TypeConverter
    fun toList(t: String?) = t.let {
        JsonUtils.fromJson<List<String>?>(t, object : TypeToken<List<String>?>() {}.type)
    }

    // TODO: satosh.dhanyamraju: store in separate table
    @TypeConverter
    fun toStrPoll(t: List<PollOptions>?) = JsonUtils.toJson(t)

    @TypeConverter
    fun toListPoll(t: String?) = t.let {
        JsonUtils.fromJson<List<PollOptions>?>(t, object : TypeToken<List<PollOptions>?>() {}.type)
    }


    @TypeConverter
    fun toListHashTagAsset(t: String?) = t.let {
        JsonUtils.fromJson<List<HastTagAsset>?>(t, object : TypeToken<List<HastTagAsset>?>() {}.type)
    }


    @TypeConverter
    fun toStrHashTagAsset(t: List<HastTagAsset>?) = JsonUtils.toJson(t)


    @TypeConverter
    fun toListTicker(t: String?) = t.let {
        JsonUtils.fromJson<List<Ticker2>?>(t, object : TypeToken<List<Ticker2>?>() {}.type)
    }


    @TypeConverter
    fun toStrTicker(t: List<Ticker2>?) = JsonUtils.toJson(t)

    @TypeConverter
    fun toListPostEntity(t: String?) = t.let {
        JsonUtils.fromJson<List<PostEntity>?>(t, object : TypeToken<List<PostEntity>?>() {}.type)
    }


    @TypeConverter
    fun toStrPostEntity(t: List<PostEntity>?) = JsonUtils.toJson(t)


    // collections]

    @TypeConverter
    fun pageEntityToByteArray(pageEntity: PageEntity): ByteArray {
        return SerializationUtils.serialize(pageEntity)
    }

    @TypeConverter
    fun byteArrayToPageEntity(bytes: ByteArray): PageEntity {
        return try {
            SerializationUtils.deserialize(bytes)
        } catch (e: Exception) {
            Logger.e(LOG_TAG, "byteArrayToPageEntity: ", e)
            throw e
        }
    }

    @TypeConverter
    fun postSourceAssetToByteArray(psa: PostSourceAsset?): ByteArray? {
        return try {
            if (psa == null) {
                null
            }
            else {
                SerializationUtils.serialize(psa)
            }
        } catch (e: Exception) {
            Logger.e(LOG_TAG, "psaToByteArray", e)
            throw e
        }
    }

    @TypeConverter
    fun byteArraytoPostSourceAsset(bytes: ByteArray?): PostSourceAsset? {
        if (bytes == null) {
            return null
        }

        return runCatching<PostSourceAsset?>{SerializationUtils.deserialize(bytes)}.getOrNull()
    }

    @TypeConverter
    fun peToByteArray(pageEntity: PostEntity): ByteArray {
        return try {
            SerializationUtils.serialize(pageEntity)
        } catch (e: Exception) {
            Logger.e(LOG_TAG, "peToByteArray", e)
            throw e
        }
    }

    @TypeConverter
    fun byteArrayToPE(bytes: ByteArray): PostEntity {
        return runCatching<PostEntity>{SerializationUtils.deserialize(bytes)}
                .getOrDefault(INVALID_POSTENTITY)
    }

    @TypeConverter
    fun toSectionType(t: String?) = t?.let { SectionType.fromName(t) }

    @TypeConverter
    fun toStr(sectionType: SectionType?) = sectionType?.let { it.name }

    @TypeConverter
    fun toStr(value: Format?) = value?.name

    @TypeConverter
    fun toFormat(value: String?) = value?.let { Format.valueOf(it) }

    @TypeConverter
    fun toStr(value: SubFormat?) = value?.name

    @TypeConverter
    fun toSubFormat(value: String?) = value?.let { SubFormat.valueOf(it) }

    @TypeConverter
    fun toStr(value: DetailAttachLocation?) = value?.name

    @TypeConverter
    fun toDetailUitype(value: String?) = value?.let {
        DetailAttachLocation
                .valueOf(it)
    }


    @TypeConverter
    fun toFollowType(t: String?) = t?.let { FollowActionType.valueOf(it) }

    @TypeConverter
    fun toStr(value: FollowActionType?) = value?.name

    @TypeConverter
    fun fromSettingState(settingState: SettingState?): String? {
        return settingState?.name
    }

    @TypeConverter
    fun toSettingState(stateStr: String?): SettingState {
        return SettingState.fromName(stateStr)
    }


    @TypeConverter
    fun fromPrivacy(privacy: SocialPrivacy?): String {
        return privacy?.name ?: SocialPrivacy.PUBLIC.name
    }

    @TypeConverter
    fun toPrivacy(privacy: String?): SocialPrivacy {
        return SocialPrivacy.fromName(privacy)
    }

    @TypeConverter
    fun fromAccountPermission(tagging : AccountPermission?): String {
        return tagging?.name ?: AccountPermission.ALLOWED.name
    }

    @TypeConverter
    fun toAccountPermission(tagging: String?): AccountPermission {
        return AccountPermission.fromName(tagging)
    }

    @TypeConverter
    fun toMemberRole(role: String?): MemberRole {
        return MemberRole.fromName(role)
    }


    @TypeConverter
    fun fromMemberRole(role: MemberRole?): String {
        return role?.name ?: MemberRole.NONE.name
    }

    @TypeConverter
    fun toMembershipStatus(status: String?): MembershipStatus {
        return MembershipStatus.fromName(status)
    }

    @TypeConverter
    fun fromMembershipStatus(status: MembershipStatus?): String {
        return status?.name ?: MembershipStatus.NONE.name
    }

    @TypeConverter
    fun toString(coldStartEntityType: ColdStartEntityType): String {
        return coldStartEntityType.name
    }

    @TypeConverter
    fun toColdStartEntityType(value: String): ColdStartEntityType {
        return ColdStartEntityType.valueOf(value)
    }

    //TODO: @rahul PostPollOption another entity with a foreign key
    @TypeConverter
    fun toOptions(p: String): List<PostPollOption> {
        return JsonUtils.fromJson<List<PostPollOption>>(
            p, object : TypeToken<List<PostPollOption>>() {}.type
        ) ?: emptyList()
    }

    @TypeConverter
    fun toStrOptions(list: List<PostPollOption>): String {
        return JsonUtils.toJson(list)
    }

    @TypeConverter
    fun toStrComParams(map: HashMap<String, Any>): String {
        return JsonUtils.toJson(map)
    }

    @TypeConverter
    fun toComParams(data: String): HashMap<String, Any> {
        return JsonUtils.fromJson<HashMap<String, Any>>(data, object : TypeToken<HashMap<String, Any>>() {}.type)
                ?: HashMap()
    }

    @TypeConverter
    fun touiModeString(mode: CreatePostUiMode): String = mode.name

    @TypeConverter
    fun toUIMode(mode: String): CreatePostUiMode = CreatePostUiMode.valueOf(mode)

    @TypeConverter
    fun toCreatePostStateString(state: PostUploadStatus): String = state.name

    @TypeConverter
    fun toCreatePostState(mode: String): PostUploadStatus = PostUploadStatus.valueOf(mode)

    //TODO(raunak): Move to a different table
    @TypeConverter
    fun toPersona(persona: String): ProfilePersona {
        return JsonUtils.fromJson(persona, ProfilePersona::class.java)
    }

    @TypeConverter
    fun fromPersona(persona: ProfilePersona): String {
        return JsonUtils.toJson(persona)
    }

    @TypeConverter
    fun toString(animationType: AnimationType): String = animationType.name

    @TypeConverter
    fun toAnimationType(name: String): AnimationType = AnimationType.valueOf(name)

    @TypeConverter
    fun toSyncStatus(statusStr: String?): SyncStatus {
        return SyncStatus.from(statusStr)
    }

    @TypeConverter
    fun toSyncStatusStr(status: SyncStatus?): String {
        return status?.name ?: SyncStatus.UN_SYNCED.name
    }

    @TypeConverter
    fun toBookmarkAction(action: String?): BookMarkAction {
        return BookMarkAction.from(action)
    }

    @TypeConverter
    fun toBookMarkActionStr(action: BookMarkAction?): String {
        return action?.name ?: BookMarkAction.ADD.name
    }

    //TODO(raunak): Need to move this to different tables
    @TypeConverter
    fun toStrAdSpec(adSpec: AdSpec?): String {
        return JsonUtils.toJson(adSpec) ?: Constants.EMPTY_STRING
    }

    @TypeConverter
    fun toAdSpec(p: String?): AdSpec {
        return JsonUtils.fromJson(p, AdSpec::class.java) ?: AdSpec()
    }

    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return value.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toCpRepostAsset(p: String?): RepostAsset? {
        return p?.let {
            JsonUtils.fromJson<RepostAsset>(p, object : TypeToken<RepostAsset>() {}.type)
        }
    }

    @TypeConverter
    fun toStrCpRepostAsset(t: RepostAsset?): String? {
        return JsonUtils.toJson(t)
    }


    @TypeConverter
    fun toImageDetail(p: String?): ImageDetail? {
        return p?.let {
            JsonUtils.fromJson<ImageDetail>(p, object : TypeToken<ImageDetail>() {}.type)
        }
    }

    @TypeConverter
    fun fromImageDetail(t: ImageDetail?): String? {
        return JsonUtils.toJson(t)
    }

    @TypeConverter
    fun toDistancingSpec(p: String?): DistancingSpec? {
        return p?.let {
            JsonUtils.fromJson<DistancingSpec>(p, object : TypeToken<DistancingSpec>() {}.type)
        }
    }

    @TypeConverter
    fun fromDistancingSpec(t: DistancingSpec?): String? {
        return JsonUtils.toJson(t)
    }



    @TypeConverter
    fun toImageDetailList(p: String?): List<ImageDetail>? {
        return p?.let {
            JsonUtils.fromJson<List<ImageDetail>>(p, object : TypeToken<List<ImageDetail>>() {}.type)
        }
    }

    @TypeConverter
    fun fromImageDetailList(t: List<ImageDetail>?): String? {
        return JsonUtils.toJson(t)
    }

    @TypeConverter
    fun fromPageSection(t: PageSection): String {
        return t.section
    }

    @TypeConverter
    fun toPageSection(t:String): PageSection? {
        return PageSection.values().find { it.section == t }
    }

    @TypeConverter
    fun fromFCImpressionCounter(counter: FcCounter): Int {
        return counter.actual
    }

    @TypeConverter
    fun toFCImpressionCounter(counter: Int): FcCounter? {
        return FcCounter(counter)
    }

    @TypeConverter
    fun fromFollowBlockConfig(followBlockConfig: FollowBlockConfigWrapper?):ByteArray? {
        followBlockConfig ?: return null
        return SerializationUtils.serialize(followBlockConfig)
    }

    @TypeConverter
    fun byteArrayToFollowBlockConfig(byteArray: ByteArray?):FollowBlockConfigWrapper? {
        byteArray ?: return null
        return runCatching<FollowBlockConfigWrapper?>{SerializationUtils.deserialize(byteArray)}.getOrNull()
    }

    @TypeConverter
    fun fromGradientItem(gradientItemList: List<GradientItem>?): String? {
        if (gradientItemList == null) return null
        val type: Type = object : TypeToken<List<GradientItem>?>() {}.type
        val json: String = Gson().toJson(gradientItemList, type)
        return if (gradientItemList.isEmpty()) null else json
    }

    @TypeConverter
    fun toGradientItem(gradientItemList: String?): List<GradientItem>? {
        val gson = Gson()
        val type: Type = object :
            TypeToken<List<GradientItem>?>() {}.type
        return gson.fromJson<List<GradientItem>>(gradientItemList, type)
    }
}