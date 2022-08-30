/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.dailyhunt.tv.exolibrary.util

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * For interacting with db. Handles entities [PlayerState], [BitrateEntry], [LoadEntry]
 *
 * Contains queries to analyze data, which would be used in analytics.
 * @author satosh.dhanyamraju
 */
@Dao
interface PADao {

  @Query("select * from player_states where uid = :uniqueId and st > 0")
  fun allStates(uniqueId: Long): List<PlayerState?>?

  @Query("select max(et)-min(et) from player_states where uid = :uniqueId")
  fun totalPlaybackTime(uniqueId: Long): Long?

  // todo : queries, select only columns required for computation
  @Query("""
    SELECT Sum(dur)
    FROM  (SELECT st,
                  next_t - et AS dur
           FROM   (SELECT t2.st,
                          t2.et,
                          (SELECT t1.et
                           FROM   player_states AS t1
                           WHERE  uid = :uniqueId
                                  AND t2.et < t1.et
                                  AND t2.st <> t1.st
                           ORDER  BY t1.et
                           LIMIT  1) AS next_t
                   FROM   player_states AS t2
                   WHERE  t2.uid = :uniqueId
                          AND next_t IS NOT NULL
                   GROUP  BY t2.st,
                             next_t
                   ORDER  BY t2.et))
    WHERE  st <> 3
  """)
  fun totalBufferTime(uniqueId: Long): Long?

  @Query("""
    SELECT COALESCE(bb, (SELECT bt
                         FROM   bitrate_entries
                         WHERE uid = :uniqueId
                         ORDER  BY et
                         LIMIT  1))
    FROM   (SELECT p.et,
                   p.st,
                   (SELECT b.bt
                    FROM   bitrate_entries AS b
                    WHERE  b.uid = :uniqueId
                           AND b.et <= p.et
                    ORDER  BY b.et DESC
                    LIMIT  1) AS bb
            FROM   player_states AS p
            WHERE  p.uid = :uniqueId
                   AND p.st <> -1)
  """)
  fun bandwidthEstimateAtStateChanges(uniqueId: Long): List<Long?>?

  @Query("""
    SELECT bt,
           t - et AS dur
    FROM   (SELECT min(et) et,
                   bt,
                   t
            FROM   (SELECT t1.uid,
                           t1.et,
                           t1.bt,
                           (SELECT t2.et
                            FROM   bitrate_entries AS t2
                            WHERE  t2.bt <> t1.bt
                                   AND t2.uid = :uniqueId
                                   AND t2.et > t1.et) AS t
                    FROM   bitrate_entries AS t1
                    WHERE  t1.uid = :uniqueId)
            WHERE  t IS NOT NULL
            GROUP  BY bt,
                      t)
    WHERE bt > 0
	  ORDER BY et;
  """)
  fun bandWidthChangeSummary(uniqueId: Long): List<BwDuration>?

  @Query("select * from load_entries")
  fun allLoadEntries(): List<LoadEntry?>?

  @Query("select * from load_entries where uid = :uniqueId")
  fun allLoadEntries(uniqueId: Long): List<LoadEntry?>?

  @Query("""
    UPDATE load_entries
    SET et_finish = :finishTime , fin_type = :finishType
    WHERE uri = :uri AND et_finish IS NULL;
  """)
  fun updateLoadEntry(uri: String, finishTime: Long, finishType: Int): Int

  @Query("""
    SELECT uri,
           Count(uri) AS attempts,
           Sum(CASE
                 WHEN fin_type = 1 THEN et_finish - et_start
                 ELSE 0
               END)   AS totalTime,
           Count(CASE
                   WHEN fin_type = 1 THEN 1
                   ELSE NULL
                 END) AS ends,
           Count(CASE
                   WHEN fin_type = 2 THEN 1
                   ELSE NULL
                 END) AS errors,
           Count(CASE
                   WHEN fin_type = 3 THEN 1
                   ELSE NULL
                 END) AS cancells,
           Count(CASE
                   WHEN fin_type IS NULL THEN 1
                   ELSE NULL
                 END) AS incompletes
    FROM   load_entries
    WHERE uid = :uniqueId
    GROUP  BY uri;
  """)
  fun loadEntrySummary(uniqueId: Long): List<LoadEntryAgg?>?

  @Query("""
   SELECT (
             SELECT min(et)
               FROM format_changes t2
              WHERE uid = :uniqueId AND
                    t2.et > t1.et
              ORDER BY et
              LIMIT 1
         ) - et
         AS t
   FROM format_changes t1
   WHERE uid = :uniqueId
   ORDER BY et
   LIMIT 1
  """)
  fun timeTakenForFirstFormatChange(uniqueId: Long): Long?

  @Query("""
  SELECT count( * ) - 1
  FROM format_changes
  WHERE uid = :uniqueId;
  """)
  fun formatChangeCount(uniqueId: Long): Int?

  // Inserts
  @Insert
  fun add(playerState: PlayerState)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun add(bitrateEntry: BitrateEntry)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun add(vararg loadEntry: LoadEntry)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun add(vararg formatChange: FormatChange)

  // Deletes
  @Query("delete from player_states where uid= :uniqueId")
  fun deletePlayerStates(uniqueId: Long): Int

  @Query("delete from bitrate_entries where uid= :uniqueId")
  fun deleteBitrateEntries(uniqueId: Long): Int

  @Query("delete from load_entries where uid= :uniqueId")
  fun deleteLoadEntries(uniqueId: Long): Int

  @Query("delete from format_changes where uid= :uniqueId")
  fun deleteFormatChanges(uniqueId: Long): Int

}