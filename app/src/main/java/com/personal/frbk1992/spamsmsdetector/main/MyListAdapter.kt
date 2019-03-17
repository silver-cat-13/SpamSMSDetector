package com.personal.frbk1992.spamsmsdetector.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.personal.frbk1992.spamsmsdetector.R
import android.util.Log
import android.util.SparseArray
import com.personal.frbk1992.spamsmsdetector.Conversation
import com.personal.frbk1992.spamsmsdetector.SMSClass
import com.personal.frbk1992.spamsmsdetector.SparseArrayAdapter


/**
 * Class adapter for the list
 * Class constructor
 * @param mContext: The Activity context
 * @param mValues: The list of SMS given by the SMSClass
 */
class MyListAdapter(val mContext: Context?,
                    val mValues: SparseArray<Conversation>?)
    : SparseArrayAdapter<Conversation>(mContext, R.layout.content_note, R.id.text_view_list_title, mValues!!) {

    //TAG for the logs
    private val TAG = this.javaClass.simpleName


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                           Functions of the  MyListAdapter                                  //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Function that removes an SMS
     * @param sms: SMS to be removed
     */
    fun remove(c: Conversation) {
        mValues!!.delete(c.threadId) // the threadID is also the key used in the SparseArray
        notifyDataSetChanged()
    }

    /**
     * Get all the SMS in an ArrayList
     */
    fun getAllSMS(): SparseArray<Conversation>? = mValues


    /**
     * Function to be called when the SMS List is updated with a new list.
     * @param smsList: new list of SMS to be shown
     */
    fun update(conversations: SparseArray<Conversation>?): Boolean {
        //if the list is null return false
        if (conversations == null) {
            Log.e(TAG, "error, list input is null")
            return false
        }
        //clear the current list
        mValues?.clear()

        //add the new values in mValues
        for (i in 0..conversations.size()) {
            val c = conversations.valueAt(i)
            mValues?.put(c.threadId, c)
        }

        notifyDataSetChanged()
        return true
    }

}