/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.group.DaggerPhoneBookComponent
import com.newshunt.appview.common.group.ui.adapter.ContactListAdapter
import com.newshunt.appview.common.group.viewmodel.PhoneBookVMF
import com.newshunt.appview.common.group.viewmodel.PhoneBookViewModel
import com.newshunt.appview.databinding.ActivityPhonebookBinding
import com.newshunt.common.view.customview.NHBaseActivity
import com.newshunt.dataentity.model.entity.Contact
import com.newshunt.dataentity.model.entity.PHONEBOOK_SEARCH_QUERY
import com.newshunt.dataentity.model.entity.SMS_BODY
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.theme.ThemeUtils
import kotlinx.android.synthetic.main.actionbar_phonebook.view.close_btn
import kotlinx.android.synthetic.main.actionbar_phonebook.view.close_btn_white
import javax.inject.Inject


/**
 * Activity is responsible for listing the local contact and sending invitation
 *
 * @author mukesh.yadav
 */
private const val SMS_TO = "smsto:"

class PhoneBookActivity : NHBaseActivity() {

    private lateinit var mPhoneBookViewModel: PhoneBookViewModel
    private lateinit var mViewBinding: ActivityPhonebookBinding
    @Inject
    lateinit var mPhoneBookVMF: PhoneBookVMF
    private lateinit var adapter: ContactListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ThemeUtils.preferredTheme.themeId)
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_phonebook)
        /**
         * Initializing dagger
         * */
        DaggerPhoneBookComponent
                .builder()
                .build()
                .inject(this)
        mPhoneBookViewModel = ViewModelProviders.of(this, mPhoneBookVMF)
                .get(PhoneBookViewModel::class.java)
        setupViews()
        initRecyclerView()
        initSearchView()
        initSendInvitationButtonClick()
        observeContactListLiveData()
        observeFilterContacts()
        observeRVItemClick()
    }

    private fun setupViews() {
        /*setting initial state to disable for send invite button*/
        mViewBinding.buttonInvite.isEnabled = false
        for (i in 0 until mViewBinding.buttonInvite.childCount) {
            val child = mViewBinding.buttonInvite.getChildAt(i)
            child.isEnabled = false
        }
    }

    /**
     * Handling the invite button click event as well as observing the state of selected contacts
     * */
    private fun initSendInvitationButtonClick() {
        mViewBinding.buttonInvite.setOnClickListener {
            sendInvitation()
        }

    }

    /**
     * Setup the contact list recyclerView
     * */
    private fun initRecyclerView() {
        adapter = ContactListAdapter(this, this, mPhoneBookViewModel)
        mViewBinding.rvPhonebook.adapter = adapter
        mViewBinding.rvPhonebook.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mViewBinding.executePendingBindings()
        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.divider_phonebook)?.let { itemDecorator.setDrawable(it) }
        mViewBinding.rvPhonebook.addItemDecoration(itemDecorator)
        if(ThemeUtils.isNightMode()) {
            mViewBinding.actionBar.close_btn.visibility = View.GONE
            mViewBinding.actionBar.close_btn_white.visibility = View.VISIBLE
        }
        mViewBinding.actionBar.close_btn.setOnClickListener{
            onBackPressed()
        }
        mViewBinding.actionBar.close_btn_white.setOnClickListener{
            onBackPressed()
        }
    }

    /**
     *send the invitation to selected contacts
     * */
    private fun sendInvitation() {
        val uri = Uri.parse("$SMS_TO ${mPhoneBookViewModel.selectedContact}")
        val it = Intent(Intent.ACTION_SENDTO, uri)
        it.putExtra(SMS_BODY, intent.extras?.getString(SMS_BODY))
        val numbers = uri.toString().split("[")[1].split("]")[0].split(",").map { it.replace(" ", "") }
        val invitesSent = numbers.size
        AnalyticsHelper2.logSentInvitationClick(numbers, invitesSent)
        startActivity(it)
    }

    /**
     * Handling the search Edit text
     * */
    private fun initSearchView() {
        mViewBinding.etSearchContact.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                filterContacts(editable.toString())
            }
        })
        intent?.getStringExtra(PHONEBOOK_SEARCH_QUERY)?.let { queryString ->
            mViewBinding.etSearchContact.append(queryString)
        }
    }

    private fun filterContacts(searchString: String) {
        mPhoneBookViewModel.filterContacts(searchString)
    }

    /**
     *observing the filtered contact list and updating the recyclerView
     * */
    private fun observeFilterContacts() {
        mPhoneBookViewModel.filteredContactLiveData.observe(this, Observer {
            if (it.isEmpty()) {
                setEmptyViewVisibility(true)
            } else {
                setEmptyViewVisibility(false)
                adapter.updateList(it)
                updateRVAdapter(it)
            }
        })
    }

    private fun updateRVAdapter(list: List<Contact>) {
        if (list.isEmpty()) {
            setEmptyViewVisibility(true)
        } else {
            setEmptyViewVisibility(false)
            adapter.updateList(list)
        }
    }

    private fun observeContactListLiveData() {
        mPhoneBookViewModel.contactListLDStatus.observe(this, Observer {
            if (it) {
                showShimmer()
            } else {
                hideShimmer()
            }
        })
    }

    private fun showShimmer() {
        mViewBinding.contactShimmer.profileShimmerContainer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        mViewBinding.contactShimmer.profileShimmerContainer.visibility = View.GONE
    }

    private fun setEmptyViewVisibility(visibility: Boolean) {
        if (visibility) {
            mViewBinding.emptyView.visibility = View.VISIBLE
            mViewBinding.rvPhonebook.visibility = View.GONE
        } else {
            mViewBinding.emptyView.visibility = View.GONE
            mViewBinding.rvPhonebook.visibility = View.VISIBLE
        }
    }

    /**
     * Observing recyclerview click and based on selection enabling and disabling invite button and updating
     * the recycler view Adapter to check/uncheck checkbox
     * */
    private fun observeRVItemClick() {
        mPhoneBookViewModel.isContactSelected.observe(this, Observer {
            setInviteButtonState(it)
        })
    }

    private fun setInviteButtonState(isEnabled: Boolean) {
        mViewBinding.buttonInvite.isEnabled = isEnabled
        for (i in 0 until mViewBinding.buttonInvite.childCount) {
            val child = mViewBinding.buttonInvite.getChildAt(i)
            child.isEnabled = isEnabled
            //need to notify adapter to change checkbox
            adapter.notifyDataSetChanged()
        }
    }

}
