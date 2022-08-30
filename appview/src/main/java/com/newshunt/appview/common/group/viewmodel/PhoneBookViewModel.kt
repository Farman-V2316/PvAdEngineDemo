/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.viewmodel

import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.viewmodel.ClickDelegate
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.model.entity.Contact
import com.newshunt.news.model.usecase.PhoneBookUsecase
import com.newshunt.news.model.usecase.toMediator2
import javax.inject.Inject

/**
 * This is corresponding VM for PhoneBookActivity
 *
 * @author Mukesh Yadav
 * */
private const val SMS_LIMIT = 50

class PhoneBookViewModel @Inject constructor(phoneBookUsecase: PhoneBookUsecase) : ViewModel(), ClickDelegate {
    val filteredContactLiveData = MediatorLiveData<List<Contact>>()
    val isContactSelected: MutableLiveData<Boolean> = MutableLiveData()
    private val phoneBookModeratorUseCase = phoneBookUsecase.toMediator2()
    var selectedContact: HashSet<String> = HashSet()
    private var filterString: String = Constants.EMPTY_STRING
    private var contactList: List<Contact> = emptyList()

    init {
        phoneBookModeratorUseCase.execute(Unit)
        filteredContactLiveData.addSource(phoneBookModeratorUseCase.data()) {
            if(it.isSuccess) {
                contactList = it.getOrNull() ?: emptyList()
                filterContacts(filterString)
            }
        }
    }

    val contactListLDStatus by lazy {
        phoneBookModeratorUseCase.status()
    }

    /**
     * Handling the Contact selection click
     *
     * @param view
     * @param item
     */
    override fun onViewClick(view: View, item: Any) {
        super.onViewClick(view, item)
        val contact = item as Contact
        contact.isSelected = !contact.isSelected

        if (contact.isSelected) {
            selectedContact.add(contact.phoneNumber)
        } else {
            selectedContact.remove(contact.phoneNumber)
        }
        isContactSelected.value = selectedContact.isNotEmpty() || selectedContact.size > SMS_LIMIT
    }

    /**
     * Filter the contact list based on
     * @param searchString
     */
    fun filterContacts(searchString: String) {
        filterString = searchString
        filteredContactLiveData.value = if (contactList.isNotEmpty()) {
            contactList.filter { contact ->
                contact.name.contains(filterString, true)
            }
        } else {
            emptyList()
        }
    }

    override fun onCleared() {
        phoneBookModeratorUseCase.dispose()
        super.onCleared()
    }
}

class PhoneBookVMF @Inject constructor() : ViewModelProvider.Factory {
    @Inject
    lateinit var phoneBookViewModel: PhoneBookViewModel

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return phoneBookViewModel as T
    }
}