package com.newshunt.common.util

import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.dataentity.dhutil.model.entity.upgrade.LangInfo
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
private const val USER_SELECTED:String = "user_selected"
private const val SYSTEM_SELECTED:String = "system_selected"
private const val BLACK_LISTED:String = "black_listed"
object LangInfoRepo {
    private var langInfo: LangInfo? = null
    private val LOG_TAG = "LangInfoRepo"
    private lateinit var langInfoMap:HashMap<String,String>

    fun getLangInfo(): LangInfo? {
        langInfo?.let {
            return it
        }
        val langInfoString = UserPreferenceUtil.getUserLangInfo()
        val type = object : TypeToken<LangInfo>() {}.type
        langInfo = JsonUtils.fromJson<LangInfo?>(langInfoString, type)
        return langInfo
    }

    fun getNonNullLangInfo():LangInfo {
        getLangInfo()?.let { return it }
        return initaliseAndReturnLangInfo()
    }

    fun getUserSelectedLangList():List<String> {
        initIfLangInfoWasNull()
        val langList = mutableListOf<String>()
        this.langInfo?.userSelectedLang?.let {
            if(it.isNotEmpty()) {
                langList.addAll(it.split(Constants.COMMA_CHARACTER))
            }
        }
        return langList
    }

    fun getSystemSelectedLangList():List<String> {
        initIfLangInfoWasNull()
        val langList = mutableListOf<String>()
        this.langInfo?.sysSelectedLang?.let {
            if(it.isNotEmpty()) {
                langList.addAll(it.split(Constants.COMMA_CHARACTER))
            }
        }
        return langList
    }

    fun getBlackListedLangList():List<String> {
        initIfLangInfoWasNull()
        val langList = mutableListOf<String>()
        this.langInfo?.blackListedLang?.let {
            if(it.isNotEmpty()) {
                langList.addAll(it.split(Constants.COMMA_CHARACTER))
            }
        }

        return langList
    }


    fun setLangInfo(langInfo: LangInfo) {
        this.langInfo = langInfo
    }

    @JvmStatic
    fun updateLangInfo(langInfo: LangInfo) {
        this.langInfo = langInfo
        UserPreferenceUtil.saveUserLangInfo(JsonUtils.toJson(LangInfoRepo.langInfo))
    }

    private fun initialiseLangInfo(isforced:Boolean = false) {
        if(langInfo == null || isforced) {
            langInfo = LangInfo(Constants.EMPTY_STRING, Constants.EMPTY_STRING, Constants.EMPTY_STRING)
        }
    }

    private fun initaliseAndReturnLangInfo():LangInfo {
        val langInfo = LangInfo(Constants.EMPTY_STRING, Constants.EMPTY_STRING, Constants.EMPTY_STRING)
        updateLangInfo(langInfo)
        return langInfo
    }
    /*
    * User Selection should override every other selection.
    * Remove particular lang from System Selected and blacklisted language selection
    */
    fun addUserSelectedLanguage(lang:String) {
        initialiseLangInfo()
        this.langInfo?.userSelectedLang = addLanguge(this.langInfo?.userSelectedLang,lang)
        removeLanguages(listOf(SYSTEM_SELECTED, BLACK_LISTED),lang)
        this.langInfo?.let { updateLangInfo(it) }
    }

    fun addSystemSelectedLanguage(lang:String) {
        initialiseLangInfo()
        this.langInfo?.sysSelectedLang = addLanguge(this.langInfo?.sysSelectedLang,lang)
        removeLanguages(listOf(USER_SELECTED,BLACK_LISTED),lang)
        this.langInfo?.let { updateLangInfo(it) }
    }

    fun addBlackListedSelectedLanguage(lang:String) {
        initialiseLangInfo()
        this.langInfo?.blackListedLang = addLanguge(this.langInfo?.blackListedLang,lang)
        removeLanguages(listOf(USER_SELECTED, SYSTEM_SELECTED),lang)
        UserPreferenceUtil.saveUserLangInfo(JsonUtils.toJson(langInfo))
    }

    private fun addLanguge(langType:String?,lang:String):String {
        if(langType.isNullOrBlank()) {
            return lang
        } else if(!langType.contains(lang)) {
            return langType + Constants.COMMA_CHARACTER + lang
        }
        return langType
    }

    public fun removeLanguages(types:List<String>, lang:String) {
        types.forEach { type ->
            when (type) {
                USER_SELECTED -> {
                    val userSelectedLang = this.langInfo?.userSelectedLang?.split(Constants.COMMA_CHARACTER)
                    this.langInfo?.userSelectedLang = userSelectedLang?.filterNot { it == lang }?.joinToString(Constants.COMMA_CHARACTER)
                }
                SYSTEM_SELECTED -> {
                    val sysSelectedLang = this.langInfo?.sysSelectedLang?.split(Constants.COMMA_CHARACTER)
                    this.langInfo?.sysSelectedLang = sysSelectedLang?.filterNot { it == lang }?.joinToString(Constants.COMMA_CHARACTER)
                }
                BLACK_LISTED -> {
                    val blackListedLang = this.langInfo?.blackListedLang?.split(Constants.COMMA_CHARACTER)
                    this.langInfo?.blackListedLang = blackListedLang?.filterNot { it == lang }?.joinToString(Constants.COMMA_CHARACTER)
                }
            }
        }
    }

    fun getSystemSelectedLanguage():List<String> {
        initialiseLangInfo()
        return this.langInfo?.sysSelectedLang?.split(Constants.COMMA_CHARACTER) ?: listOf()
    }

    private fun isUserSelectedLanguage(adjLang:String):Boolean {
        return getUserSelectedLangList().contains(adjLang)
    }

    private fun isSystemSelectedLanguage(adjLang:String):Boolean {
        return getSystemSelectedLangList().contains(adjLang)
    }

    private fun isBlackListedLanguage(adjLang:String):Boolean {
        return getBlackListedLangList().contains(adjLang)
    }

    fun isUserOrSystemOrBlackListedLanguage(adjLang: String):Boolean {
        return isUserSelectedLanguage(adjLang) ||
                isSystemSelectedLanguage(adjLang) ||
                isBlackListedLanguage(adjLang)
    }

    @JvmStatic
    fun isUserOrSystemSelectedLanguage(lang: String): Boolean {
        return isUserSelectedLanguage(lang) ||
                isSystemSelectedLanguage(lang)
    }

    fun isUserOrSystemOrBlackListedLanguageForWebCard(adjunctLangList: String): Boolean {
        val adjunctList = adjunctLangList.split(Constants.COMMA_CHARACTER).toList()
        adjunctList.forEach {
            if(isUserOrSystemOrBlackListedLanguage(it))  return true
        }
        return false
    }

    fun getAllLangs():List<String> {
        val langs = mutableListOf<String>()
        langs.addAll(getUserSelectedLangList())
        langs.addAll(getSystemSelectedLangList())
        langs.addAll(getBlackListedLangList())
        return langs
    }

    private fun initIfLangInfoWasNull(){
        if(langInfo == null){
            getLangInfo()
        }
    }
}