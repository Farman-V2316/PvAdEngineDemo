package com.newshunt.appview.common.postcreation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class PostCreationViewModelFactory @Inject constructor(
    private val currentPlaceVM: PostCurrentPlaceVM,
    private val autoCompleteLocationVM: PostAutoCompleteLocationVM) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(PostCurrentPlaceVM::class.java) ->
                currentPlaceVM as T

            modelClass.isAssignableFrom(PostAutoCompleteLocationVM::class.java) ->
                autoCompleteLocationVM as T

            else -> throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}