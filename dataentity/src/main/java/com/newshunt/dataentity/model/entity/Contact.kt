/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.model.entity

/**
 * Model class for contact
 * @author Mukesh Yadav
 * */
class Contact(var name: String,
              var phoneNumber: String,
              var photoUri: String?,
              var isSelected: Boolean = false) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contact

        if (name != other.name) return false
        if (phoneNumber != other.phoneNumber) return false
        if (photoUri != other.photoUri) return false
        if (isSelected != other.isSelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + (photoUri?.hashCode() ?: 0)
        result = 31 * result + isSelected.hashCode()
        return result
    }
}