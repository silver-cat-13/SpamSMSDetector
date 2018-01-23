package com.personal.frbk1992.spamsmsdetector

import android.os.Parcel
import android.os.Parcelable


/**
 * SMS Class
 */
class SMSClass(val id : Int = 0, val title : String = "", val content : String = ""
               , var spam : Boolean = false, var phishing : Boolean = false
               , var url : String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt() != 0,
            parcel.readInt() != 0,
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeInt(if(spam) 1 else 0)
        parcel.writeInt(if(phishing) 1 else 0)
        parcel.writeString(url)
    }

    override fun describeContents(): Int  = 0

    companion object CREATOR : Parcelable.Creator<SMSClass> {
        override fun createFromParcel(parcel: Parcel): SMSClass = SMSClass(parcel)
        override fun newArray(size: Int): Array<SMSClass?> = arrayOfNulls(size)
    }

}