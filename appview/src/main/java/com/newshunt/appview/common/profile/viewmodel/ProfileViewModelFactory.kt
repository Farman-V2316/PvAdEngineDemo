package com.newshunt.appview.common.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

/**
 * Factory to help create ProfileViewModel
 * <p>
 * Created by srikanth.ramaswamy on 06/28/2019.
 */
class ProfileViewModelFactory @Inject constructor() : ViewModelProvider.Factory {

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return profileViewModel as T
    }
}

class EditProfileViewModelFactory @Inject constructor(): ViewModelProvider.Factory {
    @Inject
    lateinit var editProfileViewModel: EditProfileViewModel

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return editProfileViewModel as T
    }
}

class HistoryViewModelFactory @Inject constructor(): ViewModelProvider.Factory {
    @Inject
    lateinit var historyViewModel: HistoryViewModel

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return historyViewModel as T
    }
}