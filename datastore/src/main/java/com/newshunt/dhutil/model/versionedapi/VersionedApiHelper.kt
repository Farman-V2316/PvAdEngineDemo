/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.versionedapi

import androidx.room.Transaction
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dhutil.model.sqlite.versionDbInstance
import io.reactivex.Observable
import java.lang.reflect.Type

/**
 * @author shrikant.agrawal
 * Helper class for accessing the versioned database
 */
class VersionedApiHelper<T> {

    fun fromCacheKt(entityType: String, parentType: String = Constants.EMPTY_STRING, parentId: String = Constants.EMPTY_STRING, classOfT: Type): Observable<T> {
        return Observable.fromCallable {
            val versionDbEntity = versionDbInstance.versionServiceDao().getVersionEntity(entityType, parentType, parentId)
            if (versionDbEntity == null || versionDbEntity.data == null) {
                throw Exception(Constants.NOT_FOUND_IN_CACHE)
            }
            val bytes = versionDbEntity.data
            val json = String(bytes)
            JsonUtils.fromJson<T>(json, classOfT)
        }
    }

    fun getLocalEntity(entityType: String, parentType: String = Constants.EMPTY_STRING, parentId: String = Constants.EMPTY_STRING, classOfT: Type): T? {
            val versionDbEntity = versionDbInstance.versionServiceDao().getVersionEntity(entityType, parentType, parentId)
            if (versionDbEntity == null || versionDbEntity.data == null) {
                return null
            }
            val bytes = versionDbEntity.data
            val json = String(bytes)
            return JsonUtils.fromJson<T>(json, classOfT)
    }

    fun insertVersionDbEntity(versionDbEntity: VersionDbEntity) {
        versionDbInstance.versionServiceDao().insertVersionEntity(versionDbEntity)
    }

    @Transaction
    fun insertAfterVersionValidation(validator: ((String?) -> Boolean)?,
                                     versionDbEntity: VersionDbEntity): Long {
        return versionDbInstance.versionServiceDao().insertAfterVersionValidation(validator, versionDbEntity)
    }

    companion object {
      @JvmStatic
      fun cleanUpVersionedData(versionEntityList: List<String>) {
          versionDbInstance.versionServiceDao().removeVersionEntities(versionEntityList)
      }

      @JvmStatic
      fun resetVersion(entityType: String, parentType: String = Constants.EMPTY_STRING, parentId: String = Constants.EMPTY_STRING) {
          return versionDbInstance.versionServiceDao().resetVersionForEntity(entityType, parentType, parentId)
      }

      @JvmStatic fun resetAllApiVersion() {
          versionDbInstance.versionServiceDao().resetVersionForAll()
      }

      // for preloadtesting
      @JvmStatic fun getRawData(entityType: String) : VersionDbEntity {
          return versionDbInstance.versionServiceDao().getVersionEntity(entityType)
      }

      @JvmStatic fun insertVersionEntity(versionDbEntity: VersionDbEntity) {
          versionDbInstance.versionServiceDao().insertVersionEntity(versionDbEntity)
      }

      @JvmStatic fun getLocalVersion(entityType: String, parentType: String = Constants.EMPTY_STRING, parentId: String = Constants.EMPTY_STRING): String {
        return versionDbInstance.versionServiceDao().getStoredVersion(entityType, parentType, parentId)
      }

      @JvmStatic fun getNullableLocalVersion(entityType: String, parentType: String = Constants.EMPTY_STRING, parentId: String = Constants.EMPTY_STRING): String? {
        return versionDbInstance.versionServiceDao().getStoredVersion(entityType, parentType, parentId)
        }
    }
}