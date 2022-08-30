/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.newshunt.appview.BR
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.newshunt.appview.R
import com.newshunt.appview.common.group.viewmodel.PhoneBookViewModel
import com.newshunt.dataentity.model.entity.Contact
import com.newshunt.appview.databinding.ItemPhonebookViewBinding

/**
 * This Adapter is resposible for inflating the contact list on Ui
 * @author Mukesh Yadav
 * */
class ContactListAdapter(private val context: Context,
                         private val lifecycleOwner: LifecycleOwner?,
                         private val phoneBookViewModel: PhoneBookViewModel) :
        RecyclerView.Adapter<ContactInfoViewHolder>() {

    private var mContactList: List<Contact>? = null
    private val mInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactInfoViewHolder {
        val viewBinding = DataBindingUtil.inflate<ItemPhonebookViewBinding>(mInflater, R.layout.item_phonebook_view,
                parent, false)
        viewBinding.setVariable(BR.vm, phoneBookViewModel)
        return ContactInfoViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return mContactList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ContactInfoViewHolder, position: Int) {
        holder.bind(mContactList?.get(position), lifecycleOwner)
    }

    fun updateList(contactList: List<Contact>) {
        this.mContactList = contactList
        notifyDataSetChanged()
    }
}

class ContactInfoViewHolder(private val viewBinding: ItemPhonebookViewBinding)
    : RecyclerView.ViewHolder(viewBinding.root), UpdateContactList {

    override fun bind(item: Contact?, lifecycleOwner: LifecycleOwner?) {
        viewBinding.setVariable(BR.contact, item)

        Glide.with(lifecycleOwner as Context)
                .load(item?.photoUri)
                .apply(RequestOptions().placeholder(R.drawable.vector_user_avatar))
                .apply(RequestOptions.circleCropTransform())
                .into(viewBinding.imgProfilePhoto)

        lifecycleOwner?.let {
            viewBinding.lifecycleOwner = it
        }
        viewBinding.executePendingBindings()
    }
}

interface UpdateContactList {
    fun bind(item: Contact?, lifecycleOwner: LifecycleOwner?)
}