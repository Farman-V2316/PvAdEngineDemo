/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.domain

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.newshunt.common.helper.common.CommonBaseUrlsContainer
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.sqlite.CHANNEL_DB
import com.newshunt.common.model.sqlite.entity.ChannelConfigEntry
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.notification.helper.NotificationEnableHelper
import com.newshunt.notification.helper.TemporaryChannelManager
import com.newshunt.notification.helper.getChannelImportantceEnumValue
import com.newshunt.notification.helper.getChannelImportantceIntValue
import com.newshunt.notification.helper.logNotificationChannelCreated
import com.newshunt.notification.helper.logNotificationChannelDeleted
import com.newshunt.notification.helper.logNotificationChannelStateChange
import com.newshunt.notification.helper.logNotificationGroupCreated
import com.newshunt.notification.helper.logNotificationGroupDeleted
import com.newshunt.notification.helper.logNotificationGroupStateChange
import com.newshunt.notification.model.entity.ChannelImportantance
import com.newshunt.notification.model.entity.NotificationChannelGroupInfo
import com.newshunt.notification.model.entity.NotificationChannelInfo
import com.newshunt.notification.model.entity.NotificationChannelResponse
import com.newshunt.notification.model.internal.rest.server.NotificationChannelPriorityDelta
import com.newshunt.notification.model.service.NotificationChannelService
import com.newshunt.notification.model.service.NotificationChannelServiceImpl
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlin.collections.ArrayList

/**
 * @author Amitkumar
 */
class NotificationChannelUsecaseController : NotificationChannelUsecase {
    private val notificationChannelService:
            NotificationChannelService = NotificationChannelServiceImpl()
    private val compositeDisposable = CompositeDisposable()
    private var specialChannel: Set<String>? = null
    private val temporaryChannelManager: TemporaryChannelManager = TemporaryChannelManager()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun requestNotificationChannelData() : Disposable {
        val context = CommonUtils.getApplication();
        val baseUrl = CommonBaseUrlsContainer.getInstance().notificationChannelUrl
        val disposable = notificationChannelService.requestChannelInfo(baseUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateChannels(it, context)
                }, {
                    Logger.d(LOG_TAG, "config pull request failed. Syncing user config change")
                    syncChangedConfig()
                    Logger.caughtException(it)
                })
        compositeDisposable.add(disposable)
        return disposable
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun syncChangedConfig(): Disposable {
        val context = CommonUtils.getApplication();

        val notificationManager = getNotificationManager(context)

        val currentChannels = notificationManager.notificationChannels.filter {
            !isSpecialChannel(it.id)
        }.map {
            NotificationChannelInfo(it.id,
                    getChannelImportantceEnumValue(it.importance),
                    it.group,
                    0)
        }

        val currentGroups = notificationManager.notificationChannelGroups.map {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                NotificationChannelGroupInfo(it.id, !it.isBlocked, 0)
            } else {
                NotificationChannelGroupInfo(it.id, true, 0)
            }
        }

        val disposable = getDeltaChannelConfig(currentChannels, currentGroups)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { delta ->
                    if (!isDeltaEmpty(delta)) {
                        Logger.d(LOG_TAG, "Delta is not empty requesting update api")
                        uploadUserConfigChanges(context, delta)
                    } else {
                        Observable.just(delta)
                    }
                }.doOnNext { delta ->
                    logDelta(delta)
                }.subscribe({
                    syncDeltaInDatabase(it)
                }, {
                    Logger.caughtException(it)
                })
        compositeDisposable.add(disposable)
        return disposable
    }

    private fun logDelta(delta: NotificationChannelPriorityDelta) {
        delta.addedChannels.filter {
            it.changeType == STATE_CHANGE
        }.forEach {
            logNotificationChannelStateChange(it.id, it.priority ?: ChannelImportantance.DEFAULT)
        }

        delta.removedChannels.filter {
            it.changeType == STATE_CHANGE
        }.forEach {
            logNotificationChannelStateChange(it.id,
                    ChannelImportantance.NONE)
        }

        delta.removedGroupInfo.filter {
            it.changeType == STATE_CHANGE
        }.forEach {
            logNotificationGroupStateChange(it.id, false)
        }

        delta.addedGroupInfo.filter {
            it.changeType == STATE_CHANGE
        }.forEach {
            logNotificationGroupStateChange(it.id, true)
        }
    }

    private fun syncDeltaInDatabase(delta: NotificationChannelPriorityDelta?,
                                    deletedEntities: List<String>? = null) {
        if (delta == null || isDeltaEmpty(delta)) {
            return
        }
        try {
            CHANNEL_DB.beginTransaction()
            val updatedEntries = (delta.addedChannels.plus(delta.removedChannels)).map { entity ->
                ChannelConfigEntry(entity.id,
                        getChannelImportantceIntValue(entity.priority ?: ChannelImportantance.NONE),
                        false)
            }.plus(delta.addedGroupInfo.map { group ->
                ChannelConfigEntry(group.id, getChannelImportantceIntValue(true),
                        true)
            }).plus(delta.removedGroupInfo.map { group ->
                ChannelConfigEntry(group.id, getChannelImportantceIntValue(false),
                        true)
            })
            val result = CHANNEL_DB.dao().updateChannel(updatedEntries)
            Logger.d(LOG_TAG, "entry updated in db $result")

            deletedEntities?.let {
                val deleteResult = CHANNEL_DB.dao().deleteChannel(it.map { id ->
                    ChannelConfigEntry(id, 0, false)
                })
                Logger.d(LOG_TAG, "deleted entry in db $deleteResult")
            }
            CHANNEL_DB.setTransactionSuccessful()
        } catch (e: Exception) {
            Logger.caughtException(e)
        } finally {
            CHANNEL_DB.endTransaction()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateChannels(response: NotificationChannelResponse?, context: Context) {
        val notificationManager = getNotificationManager(context)
        temporaryChannelManager.load()
        /*
        * existingChannels is list of pair<id,name> where name is used for analytics events
        * */
        // Save the Channelname for grouped notifications on Tray.
        if(response?.otherChannelGroups?.get(0)?.channelItems?.get(0) != null) {
            Logger.d(LOG_TAG,"Default notification is coming")
            PreferenceManager.savePreference(AppStatePreference.DEFAULT_CHANNEL_GROUPED_TRAY_NOTIFICATION,
                    response?.otherChannelGroups.get(0).channelItems?.get(0)?.id)
        }
        val existingChannels = notificationManager.notificationChannels.map {
            NotificationChannelInfo(it.name.toString(), it.id, it.description, it.canShowBadge(),
                    false, false,
                    false, null, null, null, it.group,
                    getChannelImportantceEnumValue(it.importance))
        }

        /*
        * existingGroups is list of pair<id,name> where name is used for analytics events
        * */
        val existingGroups = notificationManager.notificationChannelGroups.map {
            val groupEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                !it.isBlocked
            } else {
                true
            }
            NotificationChannelGroupInfo(it.name.toString(), it.id, null, null,
                    groupEnabled, 0)
        }
        val currentConfigOfChannel = mutableMapOf<String, NotificationChannelInfo>()
        val currentConfigOfGroup = mutableMapOf<String, NotificationChannelGroupInfo>()
        for (channel in existingChannels) {
            if (!CommonUtils.isEmpty(channel.id)) {
                currentConfigOfChannel[channel.id] = channel
            }
        }

        for (group in existingGroups) {
            if (!CommonUtils.isEmpty(group.id)) {
                currentConfigOfGroup[group.id] = group
            }
        }

        val responseGroupsIds = (response?.channelGroups?.map { it.id } ?: emptyList()).toMutableList()
        response?.otherChannelGroups?.map { it?.id }?.let { responseGroupsIds.addAll(it) } // Adding the groupId of Grouped Notification.
        response?.impChannelGroups?.map { it?.id }?.let{ responseGroupsIds.addAll(it) }
        response?.stickyChannelGroups?.map { it?.id }?.let{ responseGroupsIds.addAll(it) }

        val deleteGroupsFromSetting = existingGroups.filter { oldGroup ->
            responseGroupsIds.find {
                oldGroup.id == it
            } == null
        }
        deleteChannelsGroup(deleteGroupsFromSetting, notificationManager)

        createGroup(response?.channelGroups
                ?: emptyList(), notificationManager, currentConfigOfGroup)
        createGroup(response?.otherChannelGroups?: emptyList(),
                notificationManager,currentConfigOfGroup)
        createGroup(response?.impChannelGroups ?: emptyList(),
                notificationManager, currentConfigOfGroup)
        createGroup(response?.stickyChannelGroups?: emptyList(),
                notificationManager, currentConfigOfGroup)
        val allNewChannels: MutableList<NotificationChannelInfo> = mutableListOf()
        val allNewgroups: MutableList<NotificationChannelGroupInfo> = mutableListOf()
        response?.channelGroups?.forEach { responseGroup ->
            createChannel((responseGroup.channelItems
                    ?: emptyList()), notificationManager, responseGroup.id, currentConfigOfChannel, false)
            allNewChannels.addAll(responseGroup.channelItems?.map { responseChannel ->
                NotificationChannelInfo(responseChannel.id,
                        currentConfigOfChannel[responseChannel.id]?.priority
                                ?: responseChannel.priority, responseChannel.groupId, 0)
            } ?: emptyList()
            )
            allNewgroups.add(responseGroup.copy(enabled =
            currentConfigOfGroup[responseGroup.id]?.enabled != false))
        }
        response?.otherChannelGroups?.forEach { responseGroup ->
            createChannel((responseGroup.channelItems
                    ?: emptyList()), notificationManager, responseGroup.id, currentConfigOfChannel, false)
            allNewChannels.addAll(responseGroup.channelItems?.map { responseChannel ->
                NotificationChannelInfo(responseChannel.id,
                        currentConfigOfChannel[responseChannel.id]?.priority
                                ?: responseChannel.priority, responseChannel.groupId, 0)
            } ?: emptyList()
            )
            allNewgroups.add(responseGroup.copy(enabled =
            currentConfigOfGroup[responseGroup.id]?.enabled != false)) }

        response?.impChannelGroups?.forEach { responseGroup ->
            createChannel((responseGroup.channelItems
                    ?: emptyList()), notificationManager, responseGroup.id, currentConfigOfChannel, false)
            allNewChannels.addAll(responseGroup.channelItems?.map { responseChannel ->
                NotificationChannelInfo(responseChannel.id,
                        currentConfigOfChannel[responseChannel.id]?.priority
                                ?: responseChannel.priority, responseChannel.groupId, 0)
            } ?: emptyList()
            )
            allNewgroups.add(responseGroup.copy(enabled =
            currentConfigOfGroup[responseGroup.id]?.enabled != false)) }

        val stickyExistingChannel  = ArrayList<NotificationChannelInfo>()
        response?.stickyChannelGroups?.forEach { responseGroup ->
            if(responseGroup.channelItems != null){
                for(channel in responseGroup.channelItems){
                    if(channel.name != null && isSpecialChannel(channel.name)){
                        stickyExistingChannel.addAll(existingChannels.filter { oldChannels -> (isSpecialChannel(channel.name) && channel.name==oldChannels.id && !channel.id.equals(channel.name)) })
                    }
                }
            }

            createChannel((responseGroup.channelItems
                    ?: emptyList()), notificationManager, responseGroup.id, currentConfigOfChannel, true)
            allNewChannels.addAll(responseGroup.channelItems?.map { responseChannel ->
                NotificationChannelInfo(responseChannel.id,
                        currentConfigOfChannel[responseChannel.id]?.priority
                                ?: responseChannel.priority, responseChannel.groupId, 0)
            } ?: emptyList()
            )
            allNewgroups.add(responseGroup.copy(enabled =
            currentConfigOfGroup[responseGroup.id]?.enabled != false)) }

        val deleteChannelsFromSetting = existingChannels.filter { oldChannels ->
            !isSpecialChannel(oldChannels.id) &&
                    allNewChannels.find { it.id == oldChannels.id } == null
        } + stickyExistingChannel
        deleteChannels(deleteChannelsFromSetting, notificationManager)
        temporaryChannelManager.save()

        compositeDisposable.add(getDeltaChannelConfig(allNewChannels, allNewgroups)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { delta ->
                    if (!isDeltaEmpty(delta)) {
                        logDelta(delta)
                        uploadUserConfigChanges(context, delta)
                    } else {
                        Observable.just(delta)
                    }
                }.subscribe({ delta: NotificationChannelPriorityDelta? ->
                    syncDeltaInDatabase(delta, deleteGroupsFromSetting.map { it.id }
                            .plus(deleteChannelsFromSetting.map { it.id }))
                }, {
                    Logger.caughtException(it)
                }))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    /*
    * @channelsInfo : list of pair<channelId,channelName> where channelName is used for analytics
     * events
    * */
    private fun deleteChannels(channelsInfo: List<NotificationChannelInfo>, notificationManager:
    NotificationManager) {
        channelsInfo.forEach {
            if (!temporaryChannelManager.isTemporaryChannel(it.id)) {
                notificationManager.deleteNotificationChannel(it.id)
                logNotificationChannelDeleted(it.id, it.name)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    /*
    * @groupInfos : list of pair<groupId,channelName> where channelName is used for analytics
     * events
    * */
    private fun deleteChannelsGroup(groupInfos: List<NotificationChannelGroupInfo>,
                                    notificationManager:
                                    NotificationManager) {
        groupInfos.forEach {
            if (!temporaryChannelManager.isTemporaryGroup(it.id)) {
                notificationManager.deleteNotificationChannelGroup(it.id)
                logNotificationGroupDeleted(it.id, it.name)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(channelsInfo: List<NotificationChannelInfo>, notificationManager:
    NotificationManager, group: String, currentConfigOfChannel: Map<String, NotificationChannelInfo>, enableVibration: Boolean) {
        for (channel in channelsInfo) {
            val diffType = getDiffType(currentConfigOfChannel[channel.id], channel.copy(groupId = group))
            if (diffType > 0) {
                notificationManager.createNotificationChannel(NotificationChannel(channel.id,
                        channel.name, getChannelImportantceIntValue(channel.priority)).apply {
                    this.group = group
                    this.description = channel.description
                    this.setShowBadge(channel.showBadge ?: false)
                    if (channel.makeSound == true) {
                        this.setSound(Settings.System.DEFAULT_NOTIFICATION_URI,
                                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build())
                    } else {
                        this.setSound(null, null)
                    }
                    if(enableVibration){
                        this.vibrationPattern = longArrayOf(100)
                        this.enableVibration(true)
                    }
                })
            }
            if (diffType == CREATE) {
                logNotificationChannelCreated(channel.id, channel.name, channel.priority
                        ?: ChannelImportantance.DEFAULT)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createGroup(groupsInfo: List<NotificationChannelGroupInfo>, notificationManager:
    NotificationManager, currentConfigOfGroup: Map<String, NotificationChannelGroupInfo>) {
        for (group in groupsInfo) {
            val diffType = getDiffType(currentConfigOfGroup[group.id], group)
            if (diffType != 0) {
                notificationManager.createNotificationChannelGroup(NotificationChannelGroup(group.id,
                        group.name).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        this.description = group.description
                    }
                })
            }
            if (diffType == CREATE) {
                logNotificationGroupCreated(group.id, group.name)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDeltaChannelConfig(currentChannels: List<NotificationChannelInfo>,
                                      currentGroups: List<NotificationChannelGroupInfo>):
            Observable<NotificationChannelPriorityDelta?> {

        return Observable.fromCallable {
            var oldConfig = emptyList<ChannelConfigEntry>()
            try {
                CHANNEL_DB.beginTransaction()
                oldConfig = CHANNEL_DB.dao().fetchAllChannels()
                CHANNEL_DB.setTransactionSuccessful()
            } catch (e: Exception) {
                Logger.caughtException(e)
            } finally {
                CHANNEL_DB.endTransaction()
            }

            val oldGroupsMap = mutableMapOf<String, NotificationChannelGroupInfo>()
            val oldChannelMap = mutableMapOf<String, NotificationChannelInfo>()

            val currentGroupMap = mutableMapOf<String, NotificationChannelGroupInfo>()
            val currentChannelMap = mutableMapOf<String, NotificationChannelInfo>()

            oldConfig.filter { it.isGroup }.map {
                NotificationChannelGroupInfo(it.id,
                        getChannelImportantceEnumValue(it.imp) != ChannelImportantance.NONE,
                        0)
            }.forEach {
                oldGroupsMap[it.id] = it
            }

            currentChannels.forEach {
                currentChannelMap[it.id] = it
            }

            oldConfig.filter { !it.isGroup }.map {
                NotificationChannelInfo(it.id, getChannelImportantceEnumValue(it.imp), null, 0)
                        .copy(groupId = currentChannelMap[it.id]?.groupId)
            }.forEach {
                oldChannelMap[it.id] = it
            }

            currentGroups.forEach {
                currentGroupMap[it.id] = it
            }

            /*
            * List of channel id whose priority is changed and it is not equal to NONE or channel
            * which are added and not present in old channel list
            */
            val addedChannels = mutableListOf<NotificationChannelInfo>()
            val deletedChannels = mutableListOf<NotificationChannelInfo>()
            val addedGroups = mutableListOf<NotificationChannelGroupInfo>()
            val deletedGroups = mutableListOf<NotificationChannelGroupInfo>()

            for (channel in currentChannels) {
                val oldChannelConfig = oldChannelMap[channel.id]
                if (oldChannelConfig == null) {
                    addedChannels.add(channel.copy(changeType = CREATE))
                } else if (oldChannelConfig.priority != channel.priority) {
                    if (channel.priority != ChannelImportantance.NONE) {
                        addedChannels.add(channel.copy(changeType = STATE_CHANGE))
                    } else if (channel.priority == ChannelImportantance.NONE) {
                        deletedChannels.add(channel.copy(changeType = STATE_CHANGE))
                    }
                }
            }

            for (channel in oldChannelMap.values) {
                val currentChannel = currentChannelMap[channel.id]
                if (currentChannel == null && channel.priority != ChannelImportantance.NONE) {
                    deletedChannels.add(channel.copy(changeType = DELETE))
                }
            }

            for (group in currentGroups) {
                val oldGroup = oldGroupsMap[group.id]
                if (oldGroup == null) {
                    addedGroups.add(group.copy(changeType = CREATE))
                } else {
                    if (!oldGroup.enabled && group.enabled) {
                        addedGroups.add(group.copy(changeType = STATE_CHANGE))
                    } else if (oldGroup.enabled && !group.enabled) {
                        deletedGroups.add(group.copy(changeType = STATE_CHANGE))
                    }
                }
            }

            for (group in oldGroupsMap.values) {
                val current = currentGroupMap[group.id]
                if (current == null && group.enabled) {
                    deletedGroups.add(group.copy(changeType = DELETE))
                }
            }

            NotificationChannelPriorityDelta(addedGroups,
                    deletedGroups.map {
                        NotificationChannelGroupInfo(it.id, it.enabled, it.changeType)
                    },
                    addedChannels,
                    deletedChannels)

        }
    }

    private fun isDeltaEmpty(config: NotificationChannelPriorityDelta): Boolean {
        return config.addedChannels.isNullOrEmpty() && config.removedChannels.isNullOrEmpty() &&
                config.addedGroupInfo.isNullOrEmpty() && config.removedGroupInfo.isNullOrEmpty()
    }

    private fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
    }

    fun stop() {
        compositeDisposable.dispose()
    }

    companion object {
        private const val LOG_TAG = "NChannelController"
        private const val UPDATE = 1
        private const val CREATE = 2
        private const val DELETE = 3
        private const val STATE_CHANGE = 4
    }

    private fun isSpecialChannel(channelId: String): Boolean {
        if (specialChannel == null) {
            specialChannel = PreferenceManager.getPreference(AppStatePreference.EXEMPTED_NOTIFICATION_CHANNELS,
                    emptySet())
        }
        return specialChannel!!.contains(channelId)
    }

    private fun getDiffType(oldChannel: NotificationChannelInfo?,
                            newChannel: NotificationChannelInfo): Int {
        if (oldChannel == null || oldChannel.groupId != newChannel.groupId) {
            return CREATE
        }
        if (oldChannel.description != newChannel.description ||
                oldChannel.name != newChannel.name) {
            return UPDATE
        }
        return 0
    }

    private fun getDiffType(oldGroup: NotificationChannelGroupInfo?,
                            newGroup: NotificationChannelGroupInfo): Int {
        if (oldGroup == null) {
            return CREATE
        }
        if (oldGroup.description != newGroup.description || oldGroup.name != newGroup.name) {
            return UPDATE
        }
        return 0
    }

    private fun uploadUserConfigChanges(context: Context, delta:
    NotificationChannelPriorityDelta): Observable<NotificationChannelPriorityDelta> {
        val managerCompat = NotificationManagerCompat.from(context)
        return notificationChannelService.updateChannelPriorityConfig(delta,
                NotificationEnableHelper.getsInstance().isNotificationEnabled,
                managerCompat.areNotificationsEnabled())
    }
}