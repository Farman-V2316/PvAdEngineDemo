/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.viewholder

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.viewholder.SCVViewHolder
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.util.LangInfoRepo
import com.newshunt.common.view.customview.fontview.NHButton
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.language.Language
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.preference.AdjunctLangPreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.news.model.service.LanguageOfflineServiceImpl
import com.newshunt.news.view.adapter.LanguageItemDecoration
import com.newshunt.news.view.adapter.LanguageSelectionAdapter
import com.newshunt.onboarding.helper.AdjunctLanguageUtils
import com.newshunt.onboarding.helper.LanguageSelectionHelper
import com.newshunt.onboarding.helper.LaunchHelper
import com.newshunt.onboarding.model.internal.restadapter.PreferenceRestAdapter
import com.newshunt.onboarding.helper.HandshakeScheduler
import com.newshunt.onboarding.view.listener.LanguageSelectListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.Comparator


/**
 * @author anshul.jain
 */
/*
TODO : Change to use databinding for sub item and adapter also (Not Urgent)
 */
class LanguageSelectionViewHolder(viewDataBinding: ViewDataBinding,
                                  val vm: ClickHandlingViewModel,
                                  override val uniqueScreenId: Int,
                                  override val section: String,
                                  val pageReferrer: PageReferrer?) : SCVViewHolder(viewDataBinding.root, uniqueScreenId, section, pageReferrer),
        LanguageSelectListener,
        SeeAllClickListener {

    private val context = viewDataBinding.root.context

    private val moreLanguageList = viewDataBinding.root.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.more_languages_list)
    private val languageSelectedText = viewDataBinding.root.findViewById<NHTextView>(R.id.language_selected_text)
    private val selectedLanguageTextView = viewDataBinding.root.findViewById<NHTextView>(R.id.language_selected)
    private val moreLanguagesText = viewDataBinding.root.findViewById<NHTextView>(R.id.more_languages_text)
    private val changeLanguageButton = viewDataBinding.root.findViewById<NHTextView>(R.id.change_language_button)
    private val saveButton = viewDataBinding.root.findViewById<NHButton>(R.id.save_language_selection)
    private val numOfColumns = 3
    private val maxNumberOfLanguagesToShow = 5
    private val userSelectedLanguages = mutableListOf<String>()
    private var selectionLanguageList: List<Language>? = null
    private val languageCard = viewDataBinding.root.findViewById<ConstraintLayout>(R.id.language_card)
    private var eventFired = false
    private val visibiltyPercentageForCardView = 50 //visibility percentage for this card to be
    // considered as viewed.
    private val LOG_TAG = "LanguageSelection"
    private var asset: CommonAsset? = null

    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        if (item !is CommonAsset) {
            return
        }
        this.asset = item
        isSCVFired = false
        this.cardPosition = cardPosition
        if (item is CommonAsset) {
            analyticsItem = item
        }

        if (ThemeUtils.isNightMode()) {
            languageCard.setBackgroundColor(CommonUtils.getColor(R.color.theme_night_background))
        } else {
            languageCard.background = CommonUtils.getDrawable(R.drawable.language_card_background)
        }

        val obs = Observable.fromCallable {
            LanguageOfflineServiceImpl().getLanguages(UserPreferenceUtil.getUserEdition()).data?.rows?: mutableListOf()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
            subscribe {
                handleLanguageListResponse(it) }

    }

    private fun handleLanguageListResponse(allLanguages: MutableList<Language>) {
        allLanguages.sortWith(Comparator { lhs, rhs -> lhs.newsDisplayOrder - rhs.newsDisplayOrder })

        val selectedLangCode = UserPreferenceUtil.getUserPrimaryLanguage()
        selectedLanguageTextView.text = allLanguages.find { it.code == selectedLangCode }?.langUni

        val languageEn = allLanguages.find { it.code == Constants.ENGLISH_LANGUAGE_CODE }
        val languageHi = allLanguages.find { it.code == Constants.HINDI_LANGUAGE_CODE }

        allLanguages.remove(languageEn)
        allLanguages.remove(languageHi)

        if (languageHi != null) {
            allLanguages.add(0, languageHi)
        }
        if (languageEn != null) {
            allLanguages.add(0, languageEn)
        }

        selectionLanguageList = allLanguages.filter { it.code != selectedLangCode }.take(maxNumberOfLanguagesToShow)

        moreLanguageList.layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, numOfColumns, RecyclerView.VERTICAL, false)
        moreLanguageList.addItemDecoration(LanguageItemDecoration(CommonUtils.getDimension(R.dimen
                .language_view_holder_margin_right)))
        moreLanguageList.adapter = LanguageSelectionAdapter(selectionLanguageList
                ?: listOf(), context, this, this)

        val isEnglish = Constants.ENGLISH_LANGUAGE_CODE.equals(UserPreferenceUtil.getUserNavigationLanguage())
        languageSelectedText.text = if (isEnglish) {
            CommonUtils.getString(R.string.language_selected)
        } else {
            context.resources.getString(R.string.language_selected).plus(Constants.SPACE_STRING).plus("|").plus(Constants.SPACE_STRING).plus(CommonUtils.getString(R.string.language_selected_en))
        }

        moreLanguagesText.text = if (isEnglish) {
            CommonUtils.getString(R.string.onboarding_add_more_languages_lowercase)
        } else {
            context.resources.getString(R.string.onboarding_add_more_languages_lowercase).plus(Constants.SPACE_STRING).plus("|").plus(Constants.SPACE_STRING).plus(CommonUtils.getString(R.string
                    .add_more_languages_en))
        }

        changeLanguageButton.setOnClickListener {
            hideCardAndOpenOnboardingScreen(languageCard)
        }

        saveButton.setOnClickListener {
            if (CommonUtils.isEmpty(userSelectedLanguages)) {
                return@setOnClickListener
            }
            fireSaveButtonClickedEvent()
            saveChanges()
        }
        fireCardWidgetView()
    }

    private fun fireCardWidgetView() {
        if (eventFired) {
            return
        }
        eventFired = true
        val map = mutableMapOf<NhAnalyticsEventParam, Any>()
        map[NhAnalyticsNewsEventParam.WIDGET_TYPE] = Constants.LANG_MULTISELECT
        map[NhAnalyticsNewsEventParam.WIDGET_PLACEMENT] = Constants.IN_LIST
        map[AnalyticsParam.CARD_POSITION] = adapterPosition
        AnalyticsClient.log(NhAnalyticsNewsEvent.CARD_WIDGET_VIEW, NhAnalyticsEventSection.NEWS, map)
    }

    override fun onSeeAllClicked() {
        hideCardAndOpenOnboardingScreen(languageCard)
    }

    private fun hideCardAndOpenOnboardingScreen(view: View) {
        asset?.let {
            val args = Bundle()
            args.putSerializable(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            vm.onViewClick(view, it, args)
        }
    }

    private fun saveChanges() {

        // add all systemSelected to userSelected
        for (lang in userSelectedLanguages) {
            LangInfoRepo.addUserSelectedLanguage(lang)
        }
        PreferenceManager.savePreference(AdjunctLangPreference.PENDING_USER_WRITE_FLAG, true)
        PreferenceManager.savePreference(GenericAppStatePreference
                .FOLLOW_SYNC_LAST_SUCCESSFUL_TIME, 0L)
        CommonUtils.runInBackground {
            VersionedApiHelper.resetVersion(entityType = VersionEntity.FOLLOW_SYNC.name)
        }
        AdjunctLanguageUtils.checkLanguageChangeHandshake()
        AppSettingsProvider.onLanguagesChanged()
        PreferenceManager.savePreference(
                GenericAppStatePreference.ONBOARDING_VISITED_THROUGH_SETTINGS, true)
        hideCardAndOpenOnboardingScreen(saveButton)
        CommonUtils.setIsLanguageSelectedOnLanguageCard(true)
    }


    override fun onLanguageSelected(position: Int, selected: Boolean, autoSelected: Boolean) {
        selectionLanguageList?.let { list ->
            if (selected) {
                userSelectedLanguages.add(list[position].code)
            } else {
                userSelectedLanguages.remove(list[position].code)
            }
            if (userSelectedLanguages.isEmpty()) {
                saveButton.setBackgroundColor(CommonUtils.getColor(CommonUtils.getResourceIdFromAttribute(context, R
                        .attr.language_save_button_background)))
                saveButton.setTextColor(CommonUtils.getColor(CommonUtils.getResourceIdFromAttribute(context, R.attr
                        .language_save_button_textcolor)))
            } else {
                saveButton.setBackgroundColor(CommonUtils.getColor(R.color.save_language_button_selected))
                saveButton.setTextColor(CommonUtils.getColor(R.color.white_color))
            }
            fireLanguageSelectedEvent(selected, list, position)

        }
    }

    private fun fireLanguageSelectedEvent(selected: Boolean, list: List<Language>, position: Int) {
        val map = mutableMapOf<NhAnalyticsEventParam, Any>()
        map[NhAnalyticsAppEventParam.LANGUAGES] = UserPreferenceUtil.getUserPrimaryLanguage()
        if (selected) {
            map[NhAnalyticsAppEventParam.LANGUAGE_SELECTED] = list[position].code
        } else {
            map[NhAnalyticsAppEventParam.LANGUAGE_DESELECTED] = list[position].code
        }
        val experiments = asset?.i_experiments()
        if (experiments != null)
            AnalyticsClient.logDynamic(NhAnalyticsAppEvent.LANGUAGES_SELECTED, NhAnalyticsEventSection.NEWS,
                    map, experiments, pageReferrer, false)
        else
            AnalyticsClient.log(NhAnalyticsAppEvent.LANGUAGES_SELECTED, NhAnalyticsEventSection.NEWS,
                    map, pageReferrer)
    }

    private fun fireSaveButtonClickedEvent() {
        val map = mutableMapOf<NhAnalyticsEventParam, Any>()
        map[NhAnalyticsAppEventParam.LANGUAGES_OLD] = UserPreferenceUtil.getUserPrimaryLanguage()
        map[NhAnalyticsAppEventParam.LANGUAGES_NEW] = DataUtil.parseAsString(userSelectedLanguages).plus(Constants.COMMA_CHARACTER).plus(UserPreferenceUtil.getUserPrimaryLanguage())
        AnalyticsClient.log(NhAnalyticsAppEvent.CONTINUE_BUTTON_CLICKED, NhAnalyticsEventSection.NEWS,
                map)
    }

    override fun isLanguageSelected(position: Int, language: Language): Boolean {
        return false
    }

    //This interface use is mainly in onBoardingFragment when user start app first time than he
    // should not be able to select multiple language but here in viewholder we are allowing
    // multiselect of language so no condition is required.
    override fun canSelectLanguage(): Boolean {
        return true
    }

}

interface SeeAllClickListener {
    fun onSeeAllClicked()
}


