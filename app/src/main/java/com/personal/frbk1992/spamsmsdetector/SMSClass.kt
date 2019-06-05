package com.personal.frbk1992.spamsmsdetector

import android.os.Parcel
import android.os.Parcelable


/**
 * SMS Class
 * This class is used to get the most important information from an SMS for the application.
 * The class implements the
 * {@see <a href="https://developer.android.com/reference/android/os/Parcelable">android.os.Parcelable</a>}
 * and an instance can be easily sent to another activity.
 */
/**
 * Class constructor
 * @param id: The ID of the SMS retrieve by the OS, this ID can be used to check if two SMS are different
 * even if they contain the same body and were sent by the same sender.
 * @param title: The Sender of the SMS default value is an empty string.
 * @param content: The SMS body, default value is an empty string.
 * @param spam: A boolean indicating if the SMS is spam or not, default value is false.
 * @param phishing: A boolean indicating if a URL in the SMS is phishing or not, default value is false.
 * @param url: A string with the first URL in the SMS, if the SMS contains more than one URL only the
 * first one will be taken into account
 *
 * TODO add more parameters used for the app
 */
class SMSClass(val id : Int = 0, val title : String = "", val content : String = ""
               , var spam : Boolean = false, var phishing : Boolean = false
               , var url : String = "") : Parcelable {

    /**
     * Class constructor
     * @param parcel: used by the
     * {@see <a href="https://developer.android.com/reference/android/os/Parcelable">android.os.Parcelable</a>}
     * class.
     */
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt() != 0,
            parcel.readInt() != 0,
            parcel.readString())

    /**
     * Function implemented by
     * {@see <a href="https://developer.android.com/reference/android/os/Parcelable">android.os.Parcelable</a>}
     */
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