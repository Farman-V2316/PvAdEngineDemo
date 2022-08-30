/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.accounts.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.databinding.ChooseAccountRvItemBinding
import com.newshunt.dataentity.sso.model.entity.DHAccount

/**
 * Recycler view Adapter implementation for Accounts Link Recycler view
 *
 * @author srikanth on 06/11/2020
 */
class AccountsLinkAdapter(private val dhAccounts: List<DHAccount>,
                          private val isMobileAccounts: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //Maintain the currently selected item.
    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewBinding = DataBindingUtil.inflate<ChooseAccountRvItemBinding>(LayoutInflater.from(parent.context),
                R.layout.choose_account_rv_item, parent, false)
        return DHAccountViewHolder(viewBinding, this, viewBinding.root, isMobileAccounts)
    }

    override fun getItemCount(): Int {
        return dhAccounts.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DHAccountViewHolder).bind(dhAccounts[position], position)
    }

    /**
     * Returns the account selected by user. Null if no account is selected.
     */
    fun getSelectedAccount(): DHAccount? {
        return if (selectedPosition >= 0) {
            dhAccounts[selectedPosition]
        } else {
            null
        }
    }

    /**
     * Returns a single account which was unselected
     */
    fun getUnSelectedAccount(): DHAccount? {
        return getSelectedAccount()?.let { selectedAccount ->
            dhAccounts.filterNot {
                it == selectedAccount
            }.first()
        }
    }
}

/**
 * A simple view holder allowing selection of an account
 */
class DHAccountViewHolder(private val viewBinding: ChooseAccountRvItemBinding,
                          private val adapter: AccountsLinkAdapter,
                          val itemView: View,
                          private val isMobileNumberView: Boolean) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    init {
        super.itemView.setOnClickListener(this)
        viewBinding.rbChooseAccount.setOnClickListener(this)
    }

    fun bind(dhAccount: DHAccount, position: Int) {
        viewBinding.dhAccount = dhAccount
        viewBinding.adapter = adapter
        viewBinding.position = position
        viewBinding.showNumberAsName = isMobileNumberView
    }

    override fun onClick(view: View?) {
        view ?: return
        adapter.selectedPosition = adapterPosition
        adapter.notifyDataSetChanged()
    }
}
