package com.newshunt.appview.common.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dhutil.helper.preference.FollowBlockPrefUtil
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.model.usecase.ExplicitWrapperObject
import com.newshunt.news.viewmodel.FollowUpdateViewModel

import java.util.*

/*
 * @author Rekha Rani
 * Utils class to trigger cold start and explicit signal usecases related to follow block
 * recommendation
 */
class FollowBlockSignalUtils(
    var vmFollowUpdate: FollowUpdateViewModel,
    var viewLifecycleOwner: LifecycleOwner
) {

    var coldSignalLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    var explictSignalLiveData: MutableLiveData<ExplicitWrapperObject?> = MutableLiveData(ExplicitWrapperObject())
    fun initColdSignal() {

        var lang = UserPreferenceUtil.getUserPrimaryLanguage() ?: Constants.DEFAULT_LANGUAGE
        vmFollowUpdate.coldSignalLiveData.observe(viewLifecycleOwner, { it ->
            if (it == true) {
                FollowBlockPrefUtil.updateColdSignalFollowCarouselShow()
                coldSignalLiveData.postValue(true)
            }

        })
        vmFollowUpdate.triggerColdStartSignal(lang)

    }

    fun initExplicitSignalListner() {

        vmFollowUpdate.explicitFollowBlockLiveData.observe(viewLifecycleOwner, { it ->
            explictSignalLiveData.postValue(it)
        })
    }

    fun triggerExplicitSignal(action: String?, commonAsset: CommonAsset?) {
        commonAsset?.i_langCode()?.let {
            vmFollowUpdate.triggerExplicitBlockFollowSignal(
                commonAsset.i_source()?.id,
                it,commonAsset
            )
        }
    }
}