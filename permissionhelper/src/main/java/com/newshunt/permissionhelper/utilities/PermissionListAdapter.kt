/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.permissionhelper.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.newshunt.permissionhelper.Callbacks.PermissionRationaleProvider
import com.newshunt.permissionhelper.R

/**
 * List Used in Permission Rationale Dialog
 *
 * @author anshul.jain
 */
class PermissionListAdapter(context: Context, permissions: List<Permission>,
                            private val rationaleProvider: PermissionRationaleProvider) : BaseAdapter() {
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val permissionGroupList: List<PermissionGroup> = permissions.map {
        it.permissionGroup
    }.distinct()

    override fun getCount(): Int {
        return permissionGroupList.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val permissionListItemView = inflater?.inflate(R.layout.item_permission, null)
        val permissionDesc = permissionListItemView.findViewById<TextView>(R.id.permission_desc)
        val permissionTitle = permissionListItemView.findViewById<TextView>(R.id.permission_title)

        val data = rationaleProvider.getRationaleString(permissionGroupList[position])
        if (data != null) {
            permissionDesc.text = data.rationaleDescription
            permissionTitle.text = data.rationaleTitle
        }
        return permissionListItemView
    }
}
