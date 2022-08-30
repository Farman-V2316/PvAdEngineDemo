/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.sqlite

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.ads.AdFCType
import com.newshunt.dataentity.ads.AdFrequencyCapEntity
import com.newshunt.dataentity.ads.PersistedAdEntity
import com.newshunt.dataentity.common.asset.ArticleTimeSpentTrackEntity
import com.newshunt.dataentity.common.asset.Associations
import com.newshunt.dataentity.common.asset.AssociationsChildren
import com.newshunt.dataentity.common.asset.Card
import com.newshunt.dataentity.common.asset.CardNudge
import com.newshunt.dataentity.common.asset.CookieEntity
import com.newshunt.dataentity.common.asset.Discussions
import com.newshunt.dataentity.common.asset.DiscussionsChildren
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.asset.NCCImpression
import com.newshunt.dataentity.common.asset.NLFCItem
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.AddPageEntity
import com.newshunt.dataentity.common.pages.EntityInfoView
import com.newshunt.dataentity.common.pages.EntityPojo
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.common.pages.PageSyncEntity
import com.newshunt.dataentity.common.pages.ReportEntity
import com.newshunt.dataentity.common.pages.S_PageEntity
import com.newshunt.dataentity.common.pages.SourceFollowBlockEntity
import com.newshunt.dataentity.common.pages.TopicsEntity
import com.newshunt.dataentity.common.pages.UserFollowEntity
import com.newshunt.dataentity.dhutil.analytics.SessionInfo
import com.newshunt.dataentity.model.entity.BookmarkEntity
import com.newshunt.dataentity.model.entity.ContactEntity
import com.newshunt.dataentity.model.entity.DeletedInteractionsEntity
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.HistoryEntity
import com.newshunt.dataentity.model.entity.InAppUpdatesEntity
import com.newshunt.dataentity.model.entity.Member
import com.newshunt.dataentity.model.entity.PendingApprovalsEntity
import com.newshunt.dataentity.social.entity.AdSpecEntity
import com.newshunt.dataentity.social.entity.AdditionalContents
import com.newshunt.dataentity.social.entity.AllLevelCards
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.DetailCard
import com.newshunt.dataentity.social.entity.DislikeEntity
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.dataentity.social.entity.FetchDataEntity
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dataentity.social.entity.ImageEntity
import com.newshunt.dataentity.social.entity.ImmersiveAdRuleEntity
import com.newshunt.dataentity.social.entity.Interaction
import com.newshunt.dataentity.social.entity.LOCATION_VIEW_SQL_10
import com.newshunt.dataentity.social.entity.LOCATION_VIEW_SQL_4
import com.newshunt.dataentity.social.entity.LOCATION_VIEW_SQL_9
import com.newshunt.dataentity.social.entity.LocalDelete
import com.newshunt.dataentity.social.entity.LocalPostView
import com.newshunt.dataentity.social.entity.LocationsView
import com.newshunt.dataentity.social.entity.MenuDictionaryEntity1
import com.newshunt.dataentity.social.entity.MenuL1
import com.newshunt.dataentity.social.entity.MenuL2
import com.newshunt.dataentity.social.entity.MenuOptionData
import com.newshunt.dataentity.social.entity.MenuOptionDataView
import com.newshunt.dataentity.social.entity.MenuOptionKey
import com.newshunt.dataentity.social.entity.PhotoChild
import com.newshunt.dataentity.social.entity.PullInfoEntity
import com.newshunt.dataentity.social.entity.Q_All_Level_cards
import com.newshunt.dataentity.social.entity.Q_All_Level_cards_6
import com.newshunt.dataentity.social.entity.Q_All_Level_cards_7
import com.newshunt.dataentity.social.entity.Q_FeedPage
import com.newshunt.dataentity.social.entity.Q_MenuOpDataView
import com.newshunt.dataentity.social.entity.RecentTabEntity
import com.newshunt.dataentity.social.entity.RelatedList
import com.newshunt.dataentity.social.entity.SearchPage
import com.newshunt.dataentity.social.entity.TopLevelCard
import com.newshunt.dataentity.social.entity.Vote
import com.newshunt.dataentity.viral.model.entity.UiEventEntity
import com.newshunt.news.model.daos.AdFrequencyCapDao
import com.newshunt.news.model.daos.AddPageDao
import com.newshunt.news.model.daos.AdditionalContentsDao
import com.newshunt.news.model.daos.AdsDao
import com.newshunt.news.model.daos.AssociationsDao
import com.newshunt.news.model.daos.BookmarksDao
import com.newshunt.news.model.daos.CSSDao
import com.newshunt.news.model.daos.CSSEntity
import com.newshunt.news.model.daos.CardDao
import com.newshunt.news.model.daos.CommonDao
import com.newshunt.news.model.daos.ContactsDao
import com.newshunt.news.model.daos.CookieDao
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.daos.DeletedInteractionsDao
import com.newshunt.news.model.daos.DiscussionsDao
import com.newshunt.news.model.daos.DislikeDao
import com.newshunt.news.model.daos.EntityInfoDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowBlockRecoDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.GeneralFeedDao
import com.newshunt.news.model.daos.GroupInfoDao
import com.newshunt.news.model.daos.HistoryDao
import com.newshunt.news.model.daos.ImageDao
import com.newshunt.news.model.daos.ImmersiveRuleDao
import com.newshunt.news.model.daos.InAppUpdatesDao
import com.newshunt.news.model.daos.InteractionDao
import com.newshunt.news.model.daos.LocalDeleteDao
import com.newshunt.news.model.daos.LocationEntityDao
import com.newshunt.news.model.daos.MemberDao
import com.newshunt.news.model.daos.MenuDao
import com.newshunt.news.model.daos.NCCImpressionDao
import com.newshunt.news.model.daos.NonLinearPostDao
import com.newshunt.news.model.daos.NudgeDao
import com.newshunt.news.model.daos.PageEntityDao
import com.newshunt.news.model.daos.PageSyncEntityDao
import com.newshunt.news.model.daos.PageableTopicsDao
import com.newshunt.news.model.daos.PendingApprovalsDao
import com.newshunt.news.model.daos.PhotoChildDao
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.model.daos.PullDao
import com.newshunt.news.model.daos.RecentArticleTrackerDao
import com.newshunt.news.model.daos.RelatedListDao
import com.newshunt.news.model.daos.ReportDao
import com.newshunt.news.model.daos.SearchFeedDao
import com.newshunt.news.model.daos.SessionDao
import com.newshunt.news.model.daos.UiEventDao
import com.newshunt.news.model.daos.UserFollowDao
import com.newshunt.news.model.daos.VoteDao

/**
 * Database for whole application.
 *
 * @author satosh.dhanyamraju
 *
 */
@Database(
        entities = [
            FetchInfoEntity::class,
            S_PageEntity::class,
            Location::class,
            CookieEntity::class,
            FetchDataEntity::class,
            PageSyncEntity::class,
            Vote::class,
            TopicsEntity::class,
            Interaction::class,
            FollowSyncEntity::class,
            UserFollowEntity::class,
            EntityPojo::class,
            GeneralFeed::class,
            AdditionalContents::class,
            ContactEntity::class,
            RelatedList::class,
            GroupInfo::class,
            Member::class,
            Discussions::class,
            Associations::class,
            ImageEntity::class,
            CreatePostEntity::class,
            PhotoChild::class,
            AddPageEntity::class,
            MenuL1::class,
            MenuL2::class,
            MenuOptionKey::class,
            MenuOptionData::class,
            DislikeEntity::class,
            MenuDictionaryEntity1::class,
            SearchPage::class,
            NLFCItem::class,
            DeletedInteractionsEntity::class,
            BookmarkEntity::class,
            PullInfoEntity::class,
            RecentTabEntity::class,
            AdSpecEntity::class,
            HistoryEntity::class,
            PendingApprovalsEntity::class,
            ArticleTimeSpentTrackEntity::class,
            SessionInfo::class,
            Card::class,
            LocalDelete::class,
            UiEventEntity::class,
            CardNudge::class,
            ImmersiveAdRuleEntity::class,
            PersistedAdEntity::class,
            AdFrequencyCapEntity::class,
            InAppUpdatesEntity::class,
            ReportEntity::class,
            SourceFollowBlockEntity::class,
            CSSEntity::class,
            NCCImpression::class],
        views = [
            FeedPage::class,
            EntityInfoView::class,
            DiscussionsChildren::class,
            AssociationsChildren::class,
            TopLevelCard::class,
            DetailCard::class,
            AllLevelCards::class,
            MenuOptionDataView::class,
            LocalPostView::class,
            LocationsView::class],
        version = 10
)
@TypeConverters(SocialTypeConv::class)
abstract class SocialDB : RoomDatabase() {

    abstract fun postDao(): PostDao

    abstract fun fetchDao(): FetchDao

    abstract fun recentArticleTrackerDao(): RecentArticleTrackerDao

    abstract fun pageEntityDao(): PageEntityDao

    abstract fun pageSyncEntityDao(): PageSyncEntityDao

    abstract fun dislikeDao(): DislikeDao

    abstract fun voteDao(): VoteDao

    abstract fun pageableTopicsDao(): PageableTopicsDao

    abstract fun locationsDao(): LocationEntityDao

    abstract fun interactionsDao(): InteractionDao

    abstract fun entityInfoDao(): EntityInfoDao

    abstract fun followEntityDao(): FollowEntityDao

    abstract fun followBlockRecoDao(): FollowBlockRecoDao

    abstract fun cookieDao(): CookieDao

    abstract fun cardDao(): CardDao

    abstract fun userFollowDao(): UserFollowDao

    abstract fun groupDao(): GeneralFeedDao

    abstract fun additionalContentsDao(): AdditionalContentsDao

    abstract fun relatedListDao(): RelatedListDao

    abstract fun discussionsDao(): DiscussionsDao

    abstract fun associationsDao(): AssociationsDao

    abstract fun groupInfoDao(): GroupInfoDao

    abstract fun contactsDao(): ContactsDao

    abstract fun memberDao(): MemberDao

    abstract fun cpDao(): CreatePostDao

    abstract fun imgDao(): ImageDao

    abstract fun photoChildDao(): PhotoChildDao

    abstract fun addPageDao(): AddPageDao

    abstract fun menuDao(): MenuDao

    abstract fun searchFeedDao(): SearchFeedDao

    abstract fun nonLinearPostDao(): NonLinearPostDao

    abstract fun deletedInteractionsDao(): DeletedInteractionsDao

    abstract fun bookmarkDao(): BookmarksDao

    abstract fun pullDao(): PullDao

    abstract fun historyDao(): HistoryDao

    abstract fun pendingApprovalsDao(): PendingApprovalsDao

    abstract fun sessionDao(): SessionDao

    abstract fun commonDao(): CommonDao

    abstract fun localDeleteDao(): LocalDeleteDao

    abstract fun uiEventDao(): UiEventDao

    abstract fun nudgeDao(): NudgeDao

    abstract fun immersiveRuleDao(): ImmersiveRuleDao

    abstract fun adsDao(): AdsDao

    abstract fun adFrequencyCapDao(): AdFrequencyCapDao

    abstract fun inAppUpdatesDao(): InAppUpdatesDao

    abstract fun reportDao(): ReportDao

    abstract fun cssDao() : CSSDao

    abstract fun nccImpressionDao() : NCCImpressionDao

    companion object {

        private var INST: SocialDB? = null

        /**
         * creates a new instance if current instance is already closed
         */
        @JvmStatic
        @JvmOverloads
        fun instance(context: Context = CommonUtils.getApplication(), inMemoryDB: Boolean = false):
                SocialDB {
            if (INST == null) {
                synchronized(this) {
                    if (INST == null) {
                        Logger.d(TAG, "[${Thread.currentThread().name}] creating new connection. $INST")
                        INST = if (inMemoryDB) {
                            Room.inMemoryDatabaseBuilder(context, SocialDB::class.java)
                                    .addMigrations(MIGRATION_1_2)
                                    .addMigrations(MIGRATION_2_3)
                                    .addMigrations(MIGRATION_3_4)
                                    .addCallback(object : Callback() {
                                        override fun onCreate(db: SupportSQLiteDatabase) {
                                            super.onCreate(db)
                                            addTriggers(db)
                                        }

                                        override fun onOpen(db: SupportSQLiteDatabase) {
                                            super.onOpen(db)
                                            addDbOpenTriggers(db)
                                        }
                                    })
                                    .allowMainThreadQueries()
                                    .build()
                        } else Room.databaseBuilder(context, SocialDB::class.java, "social.db")
                                .addMigrations(MIGRATION_1_2)
                                .addMigrations(MIGRATION_2_3)
                                .addMigrations(MIGRATION_3_4)
                                .addMigrations(MIGRATION_4_5)
                                .addMigrations(MIGRATION_5_6)
                                .addMigrations(MIGRATION_6_7)
                                .addMigrations(MIGRATION_7_8)
                                .addMigrations(MIGRATION_8_9)
                                .addMigrations(MIGRATION_9_10)
                                .addCallback(object : Callback() {
                                    override fun onCreate(db: SupportSQLiteDatabase) {
                                        super.onCreate(db)
                                        addTriggers(db)
                                    }

                                    override fun onOpen(db: SupportSQLiteDatabase) {
                                        super.onOpen(db)
                                        addDbOpenTriggers(db)
                                    }
                                })
                                .build()
                    }
                }
            }
            return INST!!
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adding a default value since receivedTs is a non-null field and migration will
                // crash otherwise. The value is inconsequential as the DB is anyways cleaned up after upgrade.
                database.execSQL("ALTER TABLE fetch_data ADD COLUMN receivedTs INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                database.execSQL("DROP VIEW `menu_option_data_view`")
                database.execSQL("CREATE VIEW `menu_option_data_view` AS $Q_MenuOpDataView")
                database.execSQL("ALTER TABLE card ADD COLUMN contentImageInfo TEXT")

                // Add section and update index to ad_spec table
                database.execSQL("ALTER TABLE ad_spec ADD COLUMN section TEXT NOT NULL DEFAULT '${AdSpecEntity.SECTION_ANY}'")
                database.execSQL("DROP INDEX index_ad_spec_entityId")
                database.execSQL("CREATE UNIQUE INDEX index_ad_spec_entityId_section ON ad_spec (entityId, section)")
            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE card ADD COLUMN langCode TEXT")
                database.execSQL("CREATE TABLE IF NOT EXISTS `card_nudge` (`id` INTEGER NOT NULL, `level` TEXT, `format` TEXT, `subFormat` TEXT, `uiType2` TEXT, `hasCommentsOrReposts` INTEGER, `type` TEXT NOT NULL, `terminationType` TEXT NOT NULL, `text` TEXT NOT NULL, `tooltipDurationSec` INTEGER NOT NULL, `maxAttempts` INTEGER NOT NULL, `sessionGroup` INTEGER NOT NULL, `st_curAttempts` INTEGER, `st_active` INTEGER, `st_terminated` INTEGER, PRIMARY KEY(`id`))")
                database.execSQL("ALTER TABLE card ADD COLUMN adId TEXT")
                database.execSQL("DROP VIEW `feed_page_view`")
                database.execSQL("CREATE VIEW `feed_page_view` AS $Q_FeedPage")
                database.execSQL("DROP VIEW `all_level_cards_view`")
                database.execSQL("CREATE VIEW `all_level_cards_view` AS $Q_All_Level_cards")
                database.execSQL("ALTER TABLE history ADD COLUMN hide_control INTEGER DEFAULT 0")
                database.execSQL("DROP TABLE IF EXISTS immersive_ad_rule_stats")
                database.execSQL("CREATE TABLE IF NOT EXISTS `immersive_ad_rule_stats` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `played_in_immersive` INTEGER NOT NULL, `ad_distance` INTEGER NOT NULL, `ad_id` TEXT, `ad_position` INTEGER NOT NULL, `entryTs` INTEGER NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX index_immersive_ad_rule_stats_ad_id ON immersive_ad_rule_stats (ad_id)")
                database.execSQL("DROP TABLE IF EXISTS pages")
                database.execSQL("CREATE TABLE IF NOT EXISTS `pages` (`section` TEXT NOT NULL, `id` TEXT NOT NULL, `name` TEXT, `displayName` TEXT, `entityType` TEXT NOT NULL, `subType` TEXT, `entityLayout` TEXT, `contentUrl` TEXT, `entityInfoUrl` TEXT, `handle` TEXT, `deeplinkUrl` TEXT, `moreContentLoadUrl` TEXT, `entityImageUrl` TEXT, `shareUrl` TEXT, `nameEnglish` TEXT, `appIndexDescription` TEXT, `isRemovable` INTEGER NOT NULL, `allowReorder` INTEGER NOT NULL, `isServerDetermined` INTEGER NOT NULL, `viewOrder` INTEGER NOT NULL, `contentRequestMethod` TEXT, `enableWebHistory` INTEGER NOT NULL, `badgeType` TEXT, `isFollowable` INTEGER NOT NULL, `legacyKey` TEXT, `createPostText` TEXT, `createPostType` TEXT, `showParentInTab` INTEGER NOT NULL, `share_shareTitle` TEXT, `share_shareDescription` TEXT, `share_sourceName` TEXT, `header_bannerImageUrl` TEXT, `header_headerType` TEXT, `header_logoUrl` TEXT, `header_hideLogo` INTEGER, `header_hideMastHead` INTEGER, `counts_story_value` TEXT, `counts_story_ts` INTEGER, `counts_sources_value` TEXT, `counts_sources_ts` INTEGER, `counts_follow_value` TEXT, `counts_follow_ts` INTEGER, `counts_like_value` TEXT, `counts_like_ts` INTEGER, `counts_comments_value` TEXT, `counts_comments_ts` INTEGER, `counts_views_value` TEXT, `counts_views_ts` INTEGER, `counts_share_value` TEXT, `counts_share_ts` INTEGER, `counts_sad_value` TEXT, `counts_sad_ts` INTEGER, `counts_happy_value` TEXT, `counts_happy_ts` INTEGER, `counts_love_value` TEXT, `counts_love_ts` INTEGER, `counts_angry_value` TEXT, `counts_angry_ts` INTEGER, `counts_wow_value` TEXT, `counts_wow_ts` INTEGER, `counts_total_like_value` TEXT, `counts_total_like_ts` INTEGER, `counts_watch_value` TEXT, `counts_watch_ts` INTEGER, `counts_download_value` TEXT, `counts_download_ts` INTEGER, `counts_repost_value` TEXT, `counts_repost_ts` INTEGER, `counts_total_pending_approvals_value` TEXT, `counts_total_pending_approvals_ts` INTEGER, `counts_invites_value` TEXT, `counts_invites_ts` INTEGER, `counts_post_approvals_value` TEXT, `counts_post_approvals_ts` INTEGER, `counts_member_approvals_value` TEXT, `counts_member_approvals_ts` INTEGER, PRIMARY KEY(`id`, `section`))")
                database.execSQL("DROP TABLE IF EXISTS pageabletopics")
                database.execSQL("CREATE TABLE IF NOT EXISTS `pageabletopics` (`section` TEXT NOT NULL, `id` TEXT NOT NULL, `name` TEXT, `displayName` TEXT, `entityType` TEXT NOT NULL, `subType` TEXT, `entityLayout` TEXT, `contentUrl` TEXT, `entityInfoUrl` TEXT, `handle` TEXT, `deeplinkUrl` TEXT, `moreContentLoadUrl` TEXT, `entityImageUrl` TEXT, `shareUrl` TEXT, `nameEnglish` TEXT, `appIndexDescription` TEXT, `isRemovable` INTEGER NOT NULL, `allowReorder` INTEGER NOT NULL, `isServerDetermined` INTEGER NOT NULL, `viewOrder` INTEGER NOT NULL, `contentRequestMethod` TEXT, `enableWebHistory` INTEGER NOT NULL, `badgeType` TEXT, `isFollowable` INTEGER NOT NULL, `legacyKey` TEXT, `createPostText` TEXT, `createPostType` TEXT, `showParentInTab` INTEGER NOT NULL, `share_shareTitle` TEXT, `share_shareDescription` TEXT, `share_sourceName` TEXT, `header_bannerImageUrl` TEXT, `header_headerType` TEXT, `header_logoUrl` TEXT, `header_hideLogo` INTEGER, `header_hideMastHead` INTEGER, `counts_story_value` TEXT, `counts_story_ts` INTEGER, `counts_sources_value` TEXT, `counts_sources_ts` INTEGER, `counts_follow_value` TEXT, `counts_follow_ts` INTEGER, `counts_like_value` TEXT, `counts_like_ts` INTEGER, `counts_comments_value` TEXT, `counts_comments_ts` INTEGER, `counts_views_value` TEXT, `counts_views_ts` INTEGER, `counts_share_value` TEXT, `counts_share_ts` INTEGER, `counts_sad_value` TEXT, `counts_sad_ts` INTEGER, `counts_happy_value` TEXT, `counts_happy_ts` INTEGER, `counts_love_value` TEXT, `counts_love_ts` INTEGER, `counts_angry_value` TEXT, `counts_angry_ts` INTEGER, `counts_wow_value` TEXT, `counts_wow_ts` INTEGER, `counts_total_like_value` TEXT, `counts_total_like_ts` INTEGER, `counts_watch_value` TEXT, `counts_watch_ts` INTEGER, `counts_download_value` TEXT, `counts_download_ts` INTEGER, `counts_repost_value` TEXT, `counts_repost_ts` INTEGER, `counts_total_pending_approvals_value` TEXT, `counts_total_pending_approvals_ts` INTEGER, `counts_invites_value` TEXT, `counts_invites_ts` INTEGER, `counts_post_approvals_value` TEXT, `counts_post_approvals_ts` INTEGER, `counts_member_approvals_value` TEXT, `counts_member_approvals_ts` INTEGER, PRIMARY KEY(`id`))")
                database.execSQL("DROP TABLE IF EXISTS follow")
                database.execSQL("CREATE TABLE IF NOT EXISTS `follow` (`action` TEXT NOT NULL, `actionTime` INTEGER NOT NULL, `isSynced` INTEGER NOT NULL, `entityId` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entitySubType` TEXT, `displayName` TEXT, `entityImageUrl` TEXT, `iconUrl` TEXT, `handle` TEXT, `deeplinkUrl` TEXT, `badgeType` TEXT, `memberRole` TEXT, `experiment` TEXT, `nameEnglish` TEXT, `counts_story_value` TEXT, `counts_story_ts` INTEGER, `counts_sources_value` TEXT, `counts_sources_ts` INTEGER, `counts_follow_value` TEXT, `counts_follow_ts` INTEGER, `counts_like_value` TEXT, `counts_like_ts` INTEGER, `counts_comments_value` TEXT, `counts_comments_ts` INTEGER, `counts_views_value` TEXT, `counts_views_ts` INTEGER, `counts_share_value` TEXT, `counts_share_ts` INTEGER, `counts_sad_value` TEXT, `counts_sad_ts` INTEGER, `counts_happy_value` TEXT, `counts_happy_ts` INTEGER, `counts_love_value` TEXT, `counts_love_ts` INTEGER, `counts_angry_value` TEXT, `counts_angry_ts` INTEGER, `counts_wow_value` TEXT, `counts_wow_ts` INTEGER, `counts_total_like_value` TEXT, `counts_total_like_ts` INTEGER, `counts_watch_value` TEXT, `counts_watch_ts` INTEGER, `counts_download_value` TEXT, `counts_download_ts` INTEGER, `counts_repost_value` TEXT, `counts_repost_ts` INTEGER, `counts_total_pending_approvals_value` TEXT, `counts_total_pending_approvals_ts` INTEGER, `counts_invites_value` TEXT, `counts_invites_ts` INTEGER, `counts_post_approvals_value` TEXT, `counts_post_approvals_ts` INTEGER, `counts_member_approvals_value` TEXT, `counts_member_approvals_ts` INTEGER, PRIMARY KEY(`entityId`))")
                database.execSQL("DROP TABLE IF EXISTS userFollow")
                database.execSQL("CREATE TABLE IF NOT EXISTS `userFollow` (`fetchEntity` TEXT NOT NULL, `entityId` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entitySubType` TEXT, `displayName` TEXT, `entityImageUrl` TEXT, `iconUrl` TEXT, `handle` TEXT, `deeplinkUrl` TEXT, `badgeType` TEXT, `memberRole` TEXT, `experiment` TEXT, `nameEnglish` TEXT, `counts_story_value` TEXT, `counts_story_ts` INTEGER, `counts_sources_value` TEXT, `counts_sources_ts` INTEGER, `counts_follow_value` TEXT, `counts_follow_ts` INTEGER, `counts_like_value` TEXT, `counts_like_ts` INTEGER, `counts_comments_value` TEXT, `counts_comments_ts` INTEGER, `counts_views_value` TEXT, `counts_views_ts` INTEGER, `counts_share_value` TEXT, `counts_share_ts` INTEGER, `counts_sad_value` TEXT, `counts_sad_ts` INTEGER, `counts_happy_value` TEXT, `counts_happy_ts` INTEGER, `counts_love_value` TEXT, `counts_love_ts` INTEGER, `counts_angry_value` TEXT, `counts_angry_ts` INTEGER, `counts_wow_value` TEXT, `counts_wow_ts` INTEGER, `counts_total_like_value` TEXT, `counts_total_like_ts` INTEGER, `counts_watch_value` TEXT, `counts_watch_ts` INTEGER, `counts_download_value` TEXT, `counts_download_ts` INTEGER, `counts_repost_value` TEXT, `counts_repost_ts` INTEGER, `counts_total_pending_approvals_value` TEXT, `counts_total_pending_approvals_ts` INTEGER, `counts_invites_value` TEXT, `counts_invites_ts` INTEGER, `counts_post_approvals_value` TEXT, `counts_post_approvals_ts` INTEGER, `counts_member_approvals_value` TEXT, `counts_member_approvals_ts` INTEGER, PRIMARY KEY(`entityId`, `fetchEntity`))")
                database.execSQL("DROP TABLE IF EXISTS entityInfo")
                database.execSQL("CREATE TABLE IF NOT EXISTS `entityInfo` (`parentId` TEXT NOT NULL, `section` TEXT NOT NULL, `id` TEXT NOT NULL, `name` TEXT, `displayName` TEXT, `entityType` TEXT NOT NULL, `subType` TEXT, `entityLayout` TEXT, `contentUrl` TEXT, `entityInfoUrl` TEXT, `handle` TEXT, `deeplinkUrl` TEXT, `moreContentLoadUrl` TEXT, `entityImageUrl` TEXT, `shareUrl` TEXT, `nameEnglish` TEXT, `appIndexDescription` TEXT, `isRemovable` INTEGER NOT NULL, `allowReorder` INTEGER NOT NULL, `isServerDetermined` INTEGER NOT NULL, `viewOrder` INTEGER NOT NULL, `contentRequestMethod` TEXT, `enableWebHistory` INTEGER NOT NULL, `badgeType` TEXT, `isFollowable` INTEGER NOT NULL, `legacyKey` TEXT, `createPostText` TEXT, `createPostType` TEXT, `showParentInTab` INTEGER NOT NULL, `share_shareTitle` TEXT, `share_shareDescription` TEXT, `share_sourceName` TEXT, `header_bannerImageUrl` TEXT, `header_headerType` TEXT, `header_logoUrl` TEXT, `header_hideLogo` INTEGER, `header_hideMastHead` INTEGER, `counts_story_value` TEXT, `counts_story_ts` INTEGER, `counts_sources_value` TEXT, `counts_sources_ts` INTEGER, `counts_follow_value` TEXT, `counts_follow_ts` INTEGER, `counts_like_value` TEXT, `counts_like_ts` INTEGER, `counts_comments_value` TEXT, `counts_comments_ts` INTEGER, `counts_views_value` TEXT, `counts_views_ts` INTEGER, `counts_share_value` TEXT, `counts_share_ts` INTEGER, `counts_sad_value` TEXT, `counts_sad_ts` INTEGER, `counts_happy_value` TEXT, `counts_happy_ts` INTEGER, `counts_love_value` TEXT, `counts_love_ts` INTEGER, `counts_angry_value` TEXT, `counts_angry_ts` INTEGER, `counts_wow_value` TEXT, `counts_wow_ts` INTEGER, `counts_total_like_value` TEXT, `counts_total_like_ts` INTEGER, `counts_watch_value` TEXT, `counts_watch_ts` INTEGER, `counts_download_value` TEXT, `counts_download_ts` INTEGER, `counts_repost_value` TEXT, `counts_repost_ts` INTEGER, `counts_total_pending_approvals_value` TEXT, `counts_total_pending_approvals_ts` INTEGER, `counts_invites_value` TEXT, `counts_invites_ts` INTEGER, `counts_post_approvals_value` TEXT, `counts_post_approvals_ts` INTEGER, `counts_member_approvals_value` TEXT, `counts_member_approvals_ts` INTEGER, PRIMARY KEY(`id`, `parentId`))")
                database.execSQL("DROP TABLE IF EXISTS cp_entity")
                database.execSQL("CREATE TABLE IF NOT EXISTS `cp_entity` (`cpId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `post_id` TEXT NOT NULL, `type` TEXT, `ui_mode` TEXT NOT NULL, `text` TEXT, `title` TEXT, `privacy_level` TEXT, `allow_comments` INTEGER NOT NULL, `language` TEXT, `progress` INTEGER NOT NULL, `selectedLikeType` TEXT, `format` TEXT NOT NULL, `sub_format` TEXT NOT NULL, `ui_type` TEXT NOT NULL, `repost_asset` TEXT, `parent_id` TEXT, `parent_post_id` TEXT, `comment_params` TEXT NOT NULL, `comment_deleted` INTEGER, `state` TEXT NOT NULL, `retry_count` INTEGER NOT NULL, `notification_id` INTEGER NOT NULL, `is_localcard_shown` INTEGER NOT NULL, `creation_date` INTEGER NOT NULL, `message` TEXT, `group_joined` INTEGER, `user_id` TEXT, `user_catId` TEXT, `user_handle` TEXT, `user_displayName` TEXT, `user_legacyKey` TEXT, `user_sourceName` TEXT, `user_playerKey` TEXT, `user_entityImageUrl` TEXT, `user_icon` TEXT, `user_imageUrl` TEXT, `user_type` TEXT, `user_entityType` TEXT, `user_feedType` TEXT, `user_deeplinkUrl` TEXT, `user_nameEnglish` TEXT, `user_c_story_value` TEXT, `user_c_story_ts` INTEGER, `user_c_sources_value` TEXT, `user_c_sources_ts` INTEGER, `user_c_follow_value` TEXT, `user_c_follow_ts` INTEGER, `user_c_like_value` TEXT, `user_c_like_ts` INTEGER, `user_c_comments_value` TEXT, `user_c_comments_ts` INTEGER, `user_c_views_value` TEXT, `user_c_views_ts` INTEGER, `user_c_share_value` TEXT, `user_c_share_ts` INTEGER, `user_c_sad_value` TEXT, `user_c_sad_ts` INTEGER, `user_c_happy_value` TEXT, `user_c_happy_ts` INTEGER, `user_c_love_value` TEXT, `user_c_love_ts` INTEGER, `user_c_angry_value` TEXT, `user_c_angry_ts` INTEGER, `user_c_wow_value` TEXT, `user_c_wow_ts` INTEGER, `user_c_total_like_value` TEXT, `user_c_total_like_ts` INTEGER, `user_c_watch_value` TEXT, `user_c_watch_ts` INTEGER, `user_c_download_value` TEXT, `user_c_download_ts` INTEGER, `user_c_repost_value` TEXT, `user_c_repost_ts` INTEGER, `user_c_total_pending_approvals_value` TEXT, `user_c_total_pending_approvals_ts` INTEGER, `user_c_invites_value` TEXT, `user_c_invites_ts` INTEGER, `user_c_post_approvals_value` TEXT, `user_c_post_approvals_ts` INTEGER, `user_c_member_approvals_value` TEXT, `user_c_member_approvals_ts` INTEGER, `loc_id` TEXT, `loc_name` TEXT, `loc_address` TEXT, `loc_latitude` REAL, `loc_longitude` REAL, `loc_distance` INTEGER, `loc_isUserSelected` INTEGER, `loc_isAutoLocation` INTEGER, `oemb_type` TEXT, `oemb_title` TEXT, `oemb_url` TEXT, `oemb_author` TEXT, `oemb_authorUrl` TEXT, `oemb_providerName` TEXT, `oemb_description` TEXT, `oemb_thumbnailUrl` TEXT, `oemb_thumbnailWidth` INTEGER, `oemb_thunmbnailHeight` INTEGER, `oemb_html` TEXT, `poll_pollTitle` TEXT, `poll_duration` INTEGER, `poll_options` TEXT, `localprogress` INTEGER, `localstatus` TEXT, `localpageId` TEXT, `locallocation` TEXT, `localsection` TEXT, `localshownInForyou` INTEGER, `localcreationDate` INTEGER, `localcpId` INTEGER, `localnextCardId` TEXT, `localfetchedFromServer` INTEGER, `localisCreatedFromMyPosts` INTEGER, `localisCreatedFromOpenGroup` INTEGER)")
            }
        }

        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS `persisted_ads` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `adId` TEXT NOT NULL, `adGroupId` TEXT NOT NULL, `campaignId` TEXT NOT NULL, 
                    `adPosition` TEXT NOT NULL, `adContentType` TEXT NOT NULL, `adJson` TEXT NOT NULL)""")
                database.execSQL("CREATE UNIQUE INDEX index_persisted_ads_adGroupId ON persisted_ads (adId)")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `ads_frequency_cap_data` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `campaignId` TEXT NOT NULL, `cap` INTEGER NOT NULL,`resetTime` INTEGER NOT NULL,
                    `firstImpressionTime` INTEGER NOT NULL,`impressionCounter` INTEGER NOT NULL)""")
                database.execSQL("CREATE UNIQUE INDEX index_ads_frequency_cap_data_campaignId ON ads_frequency_cap_data (campaignId)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `cookie_table` (`location` TEXT NOT NULL, `cookie` TEXT NOT NULL, PRIMARY KEY(`location`))")
                database.execSQL("CREATE TABLE IF NOT EXISTS `locations` (`parentid` TEXT, `level` TEXT NOT NULL, `isFollowed` INTEGER NOT NULL, `isAlsoParent` INTEGER NOT NULL, `id` TEXT NOT NULL, `name` TEXT, `displayName` TEXT, `entityType` TEXT NOT NULL, `subType` TEXT, `entityLayout` TEXT, `contentUrl` TEXT, `entityInfoUrl` TEXT, `handle` TEXT, `deeplinkUrl` TEXT, `moreContentLoadUrl` TEXT, `entityImageUrl` TEXT, `shareUrl` TEXT, `nameEnglish` TEXT, `appIndexDescription` TEXT, `isRemovable` INTEGER NOT NULL, `allowReorder` INTEGER NOT NULL, `isServerDetermined` INTEGER NOT NULL, `viewOrder` INTEGER NOT NULL, `contentRequestMethod` TEXT, `enableWebHistory` INTEGER NOT NULL, `badgeType` TEXT, `isFollowable` INTEGER NOT NULL, `legacyKey` TEXT, `createPostText` TEXT, `createPostType` TEXT, `showParentInTab` INTEGER NOT NULL, `share_shareTitle` TEXT, `share_shareDescription` TEXT, `share_sourceName` TEXT, `header_bannerImageUrl` TEXT, `header_headerType` TEXT, `header_logoUrl` TEXT, `header_hideLogo` INTEGER, `header_hideMastHead` INTEGER, `counts_story_value` TEXT, `counts_story_ts` INTEGER, `counts_sources_value` TEXT, `counts_sources_ts` INTEGER, `counts_follow_value` TEXT, `counts_follow_ts` INTEGER, `counts_like_value` TEXT, `counts_like_ts` INTEGER, `counts_comments_value` TEXT, `counts_comments_ts` INTEGER, `counts_views_value` TEXT, `counts_views_ts` INTEGER, `counts_share_value` TEXT, `counts_share_ts` INTEGER, `counts_sad_value` TEXT, `counts_sad_ts` INTEGER, `counts_happy_value` TEXT, `counts_happy_ts` INTEGER, `counts_love_value` TEXT, `counts_love_ts` INTEGER, `counts_angry_value` TEXT, `counts_angry_ts` INTEGER, `counts_wow_value` TEXT, `counts_wow_ts` INTEGER, `counts_total_like_value` TEXT, `counts_total_like_ts` INTEGER, `counts_watch_value` TEXT, `counts_watch_ts` INTEGER, `counts_download_value` TEXT, `counts_download_ts` INTEGER, `counts_repost_value` TEXT, `counts_repost_ts` INTEGER, `counts_total_pending_approvals_value` TEXT, `counts_total_pending_approvals_ts` INTEGER, `counts_invites_value` TEXT, `counts_invites_ts` INTEGER, `counts_post_approvals_value` TEXT, `counts_post_approvals_ts` INTEGER, `counts_member_approvals_value` TEXT, `counts_member_approvals_ts` INTEGER, PRIMARY KEY(`id`, `level`, `isAlsoParent`))")
                database.execSQL("CREATE VIEW `locations_view` AS $LOCATION_VIEW_SQL_4")
            }
        }

        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS `in_app_updates` (`available_version` INTEGER PRIMARY KEY NOT NULL DEFAULT 0, `prompt_shown_count` INTEGER NOT NULL DEFAULT 0, last_prompt_ts INTEGER NOT NULL DEFAULT 0 )""")
            }
        }

        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE card ADD COLUMN ignoreSourceBlock INTEGER NOT NULL DEFAULT 1")
                database.execSQL("DROP VIEW `all_level_cards_view`")
                database.execSQL("CREATE VIEW `all_level_cards_view` AS $Q_All_Level_cards_6")
            }
        }

        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `report` (`rowId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entityId` TEXT NOT NULL)")
                database.execSQL("DROP VIEW `all_level_cards_view`")
                database.execSQL("CREATE VIEW `all_level_cards_view` AS $Q_All_Level_cards_7")
            }
        }

        private val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ads_frequency_cap_data ADD COLUMN capId TEXT NOT NULL DEFAULT '123'")
                database.execSQL("ALTER TABLE ads_frequency_cap_data ADD COLUMN type TEXT NOT NULL DEFAULT '${AdFCType.CAMPAIGN.name}'")
                database.execSQL("UPDATE ads_frequency_cap_data SET capId=campaignId")
                database.execSQL("DROP INDEX index_ads_frequency_cap_data_campaignId")
                database.execSQL("CREATE UNIQUE INDEX index_ads_frequency_cap_data_capId_type ON ads_frequency_cap_data (capId, type)")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `source_follow_block` (`sourceId` TEXT PRIMARY KEY NOT NULL, `pageViewCount` INTEGER NOT NULL,`showLessCount` INTEGER NOT NULL,`reportCount` INTEGER NOT NULL,`shareCount` INTEGER NOT NULL,`postSourceEntity` BLOB,`configData` BLOB, `updateTimeStamp` INTEGER NOT NULL,`sourceLang` TEXT NOT NULL,`showImplicitFollowDialogCount` INTEGER NOT NULL,`showImplicitBlockDialogCount` INTEGER NOT NULL,`updateType` TEXT)""")
                database.execSQL("DROP TABLE pageableTopics")
                database.execSQL("CREATE TABLE IF NOT EXISTS `pageabletopics` (`section` TEXT NOT NULL, `id` TEXT NOT NULL, `name` TEXT, `displayName` TEXT, `entityType` TEXT NOT NULL, `subType` TEXT, `entityLayout` TEXT, `contentUrl` TEXT, `entityInfoUrl` TEXT, `handle` TEXT, `deeplinkUrl` TEXT, `moreContentLoadUrl` TEXT, `entityImageUrl` TEXT, `shareUrl` TEXT, `nameEnglish` TEXT, `appIndexDescription` TEXT, `isRemovable` INTEGER NOT NULL, `allowReorder` INTEGER NOT NULL, `isServerDetermined` INTEGER NOT NULL, `viewOrder` INTEGER NOT NULL, `contentRequestMethod` TEXT, `enableWebHistory` INTEGER NOT NULL, `badgeType` TEXT, `isFollowable` INTEGER NOT NULL, `legacyKey` TEXT, `createPostText` TEXT, `createPostType` TEXT, `showParentInTab` INTEGER NOT NULL, `share_shareTitle` TEXT, `share_shareDescription` TEXT, `share_sourceName` TEXT, `header_bannerImageUrl` TEXT, `header_headerType` TEXT, `header_logoUrl` TEXT, `header_hideLogo` INTEGER, `header_hideMastHead` INTEGER, `counts_story_value` TEXT, `counts_story_ts` INTEGER, `counts_sources_value` TEXT, `counts_sources_ts` INTEGER, `counts_follow_value` TEXT, `counts_follow_ts` INTEGER, `counts_like_value` TEXT, `counts_like_ts` INTEGER, `counts_comments_value` TEXT, `counts_comments_ts` INTEGER, `counts_views_value` TEXT, `counts_views_ts` INTEGER, `counts_share_value` TEXT, `counts_share_ts` INTEGER, `counts_sad_value` TEXT, `counts_sad_ts` INTEGER, `counts_happy_value` TEXT, `counts_happy_ts` INTEGER, `counts_love_value` TEXT, `counts_love_ts` INTEGER, `counts_angry_value` TEXT, `counts_angry_ts` INTEGER, `counts_wow_value` TEXT, `counts_wow_ts` INTEGER, `counts_total_like_value` TEXT, `counts_total_like_ts` INTEGER, `counts_watch_value` TEXT, `counts_watch_ts` INTEGER, `counts_download_value` TEXT, `counts_download_ts` INTEGER, `counts_repost_value` TEXT, `counts_repost_ts` INTEGER, `counts_total_pending_approvals_value` TEXT, `counts_total_pending_approvals_ts` INTEGER, `counts_invites_value` TEXT, `counts_invites_ts` INTEGER, `counts_post_approvals_value` TEXT, `counts_post_approvals_ts` INTEGER, `counts_member_approvals_value` TEXT, `counts_member_approvals_ts` INTEGER, PRIMARY KEY(`id`, `section`))")
            }
        }

        private val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS pages")
                database.execSQL("CREATE TABLE IF NOT EXISTS `pages` (`section` TEXT NOT NULL, `id` TEXT NOT NULL, `name` TEXT, `displayName` TEXT, `entityType` TEXT NOT NULL, `subType` TEXT, `entityLayout` TEXT, `contentUrl` TEXT, `entityInfoUrl` TEXT, `handle` TEXT, `deeplinkUrl` TEXT, `moreContentLoadUrl` TEXT, `entityImageUrl` TEXT, `shareUrl` TEXT, `nameEnglish` TEXT, `appIndexDescription` TEXT, `isRemovable` INTEGER NOT NULL, `allowReorder` INTEGER NOT NULL, `isServerDetermined` INTEGER NOT NULL, `viewOrder` INTEGER NOT NULL, `contentRequestMethod` TEXT, `enableWebHistory` INTEGER NOT NULL, `badgeType` TEXT, `isFollowable` INTEGER NOT NULL, `legacyKey` TEXT, `createPostText` TEXT, `createPostType` TEXT, `showParentInTab` INTEGER NOT NULL, `share_shareTitle` TEXT, `share_shareDescription` TEXT, `share_sourceName` TEXT, `header_bannerImageUrl` TEXT, `header_headerType` TEXT, `header_logoUrl` TEXT, `header_hideLogo` INTEGER, `header_hideMastHead` INTEGER, `counts_story_value` TEXT, `counts_story_ts` INTEGER, `counts_sources_value` TEXT, `counts_sources_ts` INTEGER, `counts_follow_value` TEXT, `counts_follow_ts` INTEGER, `counts_like_value` TEXT, `counts_like_ts` INTEGER, `counts_comments_value` TEXT, `counts_comments_ts` INTEGER, `counts_views_value` TEXT, `counts_views_ts` INTEGER, `counts_share_value` TEXT, `counts_share_ts` INTEGER, `counts_sad_value` TEXT, `counts_sad_ts` INTEGER, `counts_happy_value` TEXT, `counts_happy_ts` INTEGER, `counts_love_value` TEXT, `counts_love_ts` INTEGER, `counts_angry_value` TEXT, `counts_angry_ts` INTEGER, `counts_wow_value` TEXT, `counts_wow_ts` INTEGER, `counts_total_like_value` TEXT, `counts_total_like_ts` INTEGER, `counts_watch_value` TEXT, `counts_watch_ts` INTEGER, `counts_download_value` TEXT, `counts_download_ts` INTEGER, `counts_repost_value` TEXT, `counts_repost_ts` INTEGER, `counts_total_pending_approvals_value` TEXT, `counts_total_pending_approvals_ts` INTEGER, `counts_invites_value` TEXT, `counts_invites_ts` INTEGER, `counts_post_approvals_value` TEXT, `counts_post_approvals_ts` INTEGER, `counts_member_approvals_value` TEXT, `counts_member_approvals_ts` INTEGER, `promotion_iconUrl` TEXT, `promotion_colourGradient` TEXT, `promotion_indicatorColour` TEXT, `promotion_publishTime` INTEGER, `promotion_expiryTime` INTEGER, PRIMARY KEY(`id`, `section`))")
                database.execSQL("DROP TABLE IF EXISTS pageabletopics")
                database.execSQL("CREATE TABLE IF NOT EXISTS `pageabletopics` (`section` TEXT NOT NULL, `id` TEXT NOT NULL, `name` TEXT, `displayName` TEXT, `entityType` TEXT NOT NULL, `subType` TEXT, `entityLayout` TEXT, `contentUrl` TEXT, `entityInfoUrl` TEXT, `handle` TEXT, `deeplinkUrl` TEXT, `moreContentLoadUrl` TEXT, `entityImageUrl` TEXT, `shareUrl` TEXT, `nameEnglish` TEXT, `appIndexDescription` TEXT, `isRemovable` INTEGER NOT NULL, `allowReorder` INTEGER NOT NULL, `isServerDetermined` INTEGER NOT NULL, `viewOrder` INTEGER NOT NULL, `contentRequestMethod` TEXT, `enableWebHistory` INTEGER NOT NULL, `badgeType` TEXT, `isFollowable` INTEGER NOT NULL, `legacyKey` TEXT, `createPostText` TEXT, `createPostType` TEXT, `showParentInTab` INTEGER NOT NULL, `share_shareTitle` TEXT, `share_shareDescription` TEXT, `share_sourceName` TEXT, `header_bannerImageUrl` TEXT, `header_headerType` TEXT, `header_logoUrl` TEXT, `header_hideLogo` INTEGER, `header_hideMastHead` INTEGER, `counts_story_value` TEXT, `counts_story_ts` INTEGER, `counts_sources_value` TEXT, `counts_sources_ts` INTEGER, `counts_follow_value` TEXT, `counts_follow_ts` INTEGER, `counts_like_value` TEXT, `counts_like_ts` INTEGER, `counts_comments_value` TEXT, `counts_comments_ts` INTEGER, `counts_views_value` TEXT, `counts_views_ts` INTEGER, `counts_share_value` TEXT, `counts_share_ts` INTEGER, `counts_sad_value` TEXT, `counts_sad_ts` INTEGER, `counts_happy_value` TEXT, `counts_happy_ts` INTEGER, `counts_love_value` TEXT, `counts_love_ts` INTEGER, `counts_angry_value` TEXT, `counts_angry_ts` INTEGER, `counts_wow_value` TEXT, `counts_wow_ts` INTEGER, `counts_total_like_value` TEXT, `counts_total_like_ts` INTEGER, `counts_watch_value` TEXT, `counts_watch_ts` INTEGER, `counts_download_value` TEXT, `counts_download_ts` INTEGER, `counts_repost_value` TEXT, `counts_repost_ts` INTEGER, `counts_total_pending_approvals_value` TEXT, `counts_total_pending_approvals_ts` INTEGER, `counts_invites_value` TEXT, `counts_invites_ts` INTEGER, `counts_post_approvals_value` TEXT, `counts_post_approvals_ts` INTEGER, `counts_member_approvals_value` TEXT, `counts_member_approvals_ts` INTEGER, `promotion_iconUrl` TEXT, `promotion_colourGradient` TEXT, `promotion_indicatorColour` TEXT, `promotion_publishTime` INTEGER, `promotion_expiryTime` INTEGER, PRIMARY KEY(`id`,`section`))")
                database.execSQL("DROP TABLE IF EXISTS entityInfo")
                database.execSQL("CREATE TABLE IF NOT EXISTS `entityInfo` (`parentId` TEXT NOT NULL, `section` TEXT NOT NULL, `id` TEXT NOT NULL, `name` TEXT, `displayName` TEXT, `entityType` TEXT NOT NULL, `subType` TEXT, `entityLayout` TEXT, `contentUrl` TEXT, `entityInfoUrl` TEXT, `handle` TEXT, `deeplinkUrl` TEXT, `moreContentLoadUrl` TEXT, `entityImageUrl` TEXT, `shareUrl` TEXT, `nameEnglish` TEXT, `appIndexDescription` TEXT, `isRemovable` INTEGER NOT NULL, `allowReorder` INTEGER NOT NULL, `isServerDetermined` INTEGER NOT NULL, `viewOrder` INTEGER NOT NULL, `contentRequestMethod` TEXT, `enableWebHistory` INTEGER NOT NULL, `badgeType` TEXT, `isFollowable` INTEGER NOT NULL, `legacyKey` TEXT, `createPostText` TEXT, `createPostType` TEXT, `showParentInTab` INTEGER NOT NULL, `share_shareTitle` TEXT, `share_shareDescription` TEXT, `share_sourceName` TEXT, `header_bannerImageUrl` TEXT, `header_headerType` TEXT, `header_logoUrl` TEXT, `header_hideLogo` INTEGER, `header_hideMastHead` INTEGER, `counts_story_value` TEXT, `counts_story_ts` INTEGER, `counts_sources_value` TEXT, `counts_sources_ts` INTEGER, `counts_follow_value` TEXT, `counts_follow_ts` INTEGER, `counts_like_value` TEXT, `counts_like_ts` INTEGER, `counts_comments_value` TEXT, `counts_comments_ts` INTEGER, `counts_views_value` TEXT, `counts_views_ts` INTEGER, `counts_share_value` TEXT, `counts_share_ts` INTEGER, `counts_sad_value` TEXT, `counts_sad_ts` INTEGER, `counts_happy_value` TEXT, `counts_happy_ts` INTEGER, `counts_love_value` TEXT, `counts_love_ts` INTEGER, `counts_angry_value` TEXT, `counts_angry_ts` INTEGER, `counts_wow_value` TEXT, `counts_wow_ts` INTEGER, `counts_total_like_value` TEXT, `counts_total_like_ts` INTEGER, `counts_watch_value` TEXT, `counts_watch_ts` INTEGER, `counts_download_value` TEXT, `counts_download_ts` INTEGER, `counts_repost_value` TEXT, `counts_repost_ts` INTEGER, `counts_total_pending_approvals_value` TEXT, `counts_total_pending_approvals_ts` INTEGER, `counts_invites_value` TEXT, `counts_invites_ts` INTEGER, `counts_post_approvals_value` TEXT, `counts_post_approvals_ts` INTEGER, `counts_member_approvals_value` TEXT, `counts_member_approvals_ts` INTEGER, `promotion_iconUrl` TEXT, `promotion_colourGradient` TEXT, `promotion_indicatorColour` TEXT, `promotion_publishTime` INTEGER, `promotion_expiryTime` INTEGER, PRIMARY KEY(`id`, `parentId`))")
                database.execSQL("DROP TABLE IF EXISTS locations")
                database.execSQL("CREATE TABLE IF NOT EXISTS `locations` (`parentid` TEXT, `level` TEXT NOT NULL, `isFollowed` INTEGER NOT NULL, `isAlsoParent` INTEGER NOT NULL, `id` TEXT NOT NULL, `name` TEXT, `displayName` TEXT, `entityType` TEXT NOT NULL, `subType` TEXT, `entityLayout` TEXT, `contentUrl` TEXT, `entityInfoUrl` TEXT, `handle` TEXT, `deeplinkUrl` TEXT, `moreContentLoadUrl` TEXT, `entityImageUrl` TEXT, `shareUrl` TEXT, `nameEnglish` TEXT, `appIndexDescription` TEXT, `isRemovable` INTEGER NOT NULL, `allowReorder` INTEGER NOT NULL, `isServerDetermined` INTEGER NOT NULL, `viewOrder` INTEGER NOT NULL, `contentRequestMethod` TEXT, `enableWebHistory` INTEGER NOT NULL, `badgeType` TEXT, `isFollowable` INTEGER NOT NULL, `legacyKey` TEXT, `createPostText` TEXT, `createPostType` TEXT, `showParentInTab` INTEGER NOT NULL, `share_shareTitle` TEXT, `share_shareDescription` TEXT, `share_sourceName` TEXT, `header_bannerImageUrl` TEXT, `header_headerType` TEXT, `header_logoUrl` TEXT, `header_hideLogo` INTEGER, `header_hideMastHead` INTEGER, `counts_story_value` TEXT, `counts_story_ts` INTEGER, `counts_sources_value` TEXT, `counts_sources_ts` INTEGER, `counts_follow_value` TEXT, `counts_follow_ts` INTEGER, `counts_like_value` TEXT, `counts_like_ts` INTEGER, `counts_comments_value` TEXT, `counts_comments_ts` INTEGER, `counts_views_value` TEXT, `counts_views_ts` INTEGER, `counts_share_value` TEXT, `counts_share_ts` INTEGER, `counts_sad_value` TEXT, `counts_sad_ts` INTEGER, `counts_happy_value` TEXT, `counts_happy_ts` INTEGER, `counts_love_value` TEXT, `counts_love_ts` INTEGER, `counts_angry_value` TEXT, `counts_angry_ts` INTEGER, `counts_wow_value` TEXT, `counts_wow_ts` INTEGER, `counts_total_like_value` TEXT, `counts_total_like_ts` INTEGER, `counts_watch_value` TEXT, `counts_watch_ts` INTEGER, `counts_download_value` TEXT, `counts_download_ts` INTEGER, `counts_repost_value` TEXT, `counts_repost_ts` INTEGER, `counts_total_pending_approvals_value` TEXT, `counts_total_pending_approvals_ts` INTEGER, `counts_invites_value` TEXT, `counts_invites_ts` INTEGER, `counts_post_approvals_value` TEXT, `counts_post_approvals_ts` INTEGER, `counts_member_approvals_value` TEXT, `counts_member_approvals_ts` INTEGER, `promotion_iconUrl` TEXT, `promotion_colourGradient` TEXT, `promotion_indicatorColour` TEXT, `promotion_publishTime` INTEGER, `promotion_expiryTime` INTEGER, PRIMARY KEY(`id`, `level`, `isAlsoParent`))")

                database.execSQL("DROP VIEW `locations_view`")
                database.execSQL("CREATE VIEW `locations_view` AS $LOCATION_VIEW_SQL_9")
                database.execSQL("CREATE TABLE IF NOT EXISTS `css_entities` (`id` TEXT NOT NULL, `fetch_id` INTEGER NOT NULL, `state` INTEGER NOT NULL, `batch_id` TEXT NOT NULL, `ts` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                database.execSQL("ALTER TABLE `article_time_spent_track` ADD COLUMN referrer TEXT NOT NULL DEFAULT \" \"")
            }
        }

        private val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP VIEW `locations_view`")

                val tabNames = listOf("pages","locations","pageabletopics","entityInfo")
                val colNames = listOf("descriptionUrl","subTitle","defaultTabId","moreText","carouselUrl", "description")

                tabNames.forEach { tab ->
                    colNames.forEach { col ->
                        database.execSQL(alterTableQuery(tab,col,"TEXT","null"))
                    }
                }

                database.execSQL("ALTER TABLE `members` ADD COLUMN `subTitle` TEXT")
                database.execSQL("CREATE TABLE IF NOT EXISTS `ncc_impression` (`cardId` TEXT NOT NULL, `data` TEXT, `status` TEXT NOT NULL, PRIMARY KEY(`cardId`))")
                database.execSQL("CREATE VIEW `locations_view` AS $LOCATION_VIEW_SQL_10")
            }
        }

        private fun addTriggers(db: SupportSQLiteDatabase) {
            try {
                // TODO(satosh.dhanyamraju): fix all triggers
                db.execSQL(FetchDao.trigger_delete_dangling_posts)
                db.execSQL(FetchDao.trigger_delete_dangling_discussion)
                db.execSQL(FetchDao.trigger_delete_dangling_related)
                db.execSQL(FetchDao.trigger_del_post_not_in_related)
                db.execSQL(CreatePostDao.trigger_delete_local_from_posts)
                db.execSQL(FetchDao.trigger_delete_fetchdata_of_localcards)
            } catch (e: Exception) {
                Logger.e(TAG, "addTriggers: $e")
            } finally {
                Logger.d(TAG, "addTriggers: Exit")
            }
        }

        private fun alterTableQuery(tableName:String, colName:String, colType:String, default:String?):String {
            default?.let {
                return "ALTER TABLE $tableName ADD COLUMN $colName $colType DEFAULT $default"
            }
            return "ALTER TABLE $tableName ADD COLUMN $colName $colType"
        }

        private fun addDbOpenTriggers(db: SupportSQLiteDatabase) {
            try {
                db.execSQL(CreatePostDao.delete_dangling_cp_entries_new_session)
                db.execSQL(FetchDao.trigger_drop_table_immersive_ad_rule)
                db.execSQL(ReportDao.delete_old_reported_comments_new_session)
                val ts = System.currentTimeMillis() - 86400000 /*24hr*/
                db.execSQL(String.format(CSSDao.delete_older_items, ts))
                db.execSQL(CSSDao.mark_unknown_discarded)
                db.execSQL(CSSDao.mark_ncc_not_synced)
                db.execSQL(CSSDao.clear_batch_id)

            } catch (e: Exception) {
                Logger.e(TAG, "addTriggers: $e")
            } finally {
                Logger.d(TAG, "addTriggers: Exit")
            }
        }

        /**
         * should be closed on app exit
         */
        @JvmStatic
        fun closeConnection() {
            INST?.close()
        }

        const val TAG = "SocialDB"
    }

    /**
     * No need to call this function.
     * If and when called, we need to make sure companion's INST is set to null, so that a new
     * connection will be created next time.
     */
    override fun close() {
        super.close()
        INST = null
    }
}