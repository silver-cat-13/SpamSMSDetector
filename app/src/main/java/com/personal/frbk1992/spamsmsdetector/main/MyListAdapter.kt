package com.personal.frbk1992.spamsmsdetector.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.personal.frbk1992.spamsmsdetector.R
import android.util.Log
import com.personal.frbk1992.spamsmsdetector.SMSClass


/**
 * Class adapter for the list
 * Class constructor
 * @param mContext: The Activity context
 * @param mValues: The list of SMS given by the SMSClass
 */
class MyListAdapter(val mContext: Context?,
                    val mValues: ArrayList<SMSClass>?)
    : ArrayAdapter<SMSClass>(mContext, R.layout.content_note, R.id.text_view_list_title, mValues) {

    //TAG for the logs
    private val TAG = this.javaClass.simpleName

    /*
    Holder class
     */
    private class ViewHolder {
        var mView : View? = null
        var mTitleView : TextView? = null
        var mContentView : TextView? = null
        var smsContent : SMSClass? = null

    }


    /**
     * GetView function add the information to each list from every MyNote instance in the list
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view : View = LayoutInflater.from(mContext)
                .inflate(R.layout.content_note, parent, false)

        //get all the views ids and save them in the ViewHolder class
        val holder = ViewHolder()
        holder.mView = view
        holder.mTitleView = view.findViewById(R.id.text_view_list_title)
        holder.mContentView = view.findViewById(R.id.text_view_list_detail)
        holder.smsContent = mValues!![position]

        holder.mTitleView!!.text = holder.smsContent!!.title
        holder.mContentView!!.text  = holder.smsContent!!.content

        return view

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                           Functions of the  MyNotesListViewAdapter                         //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Function that removes an SMS
     * @param sms: SMS to be removed
     */
    override fun remove(sms: SMSClass) {
        mValues!!.remove(sms)
        notifyDataSetChanged()
    }

    /**
     * Get all the SMS in an ArrayList
     */
    fun getAllSMS(): ArrayList<SMSClass>? = mValues


    /*
     * Function to be called when the SMS List is updated with a new list.
     * @param smsList: new list of SMS to be shown
     */
    /*fun update(smsList: List<SMSClass>?): Boolean {
        if (smsList == null) {
            Log.e(TAG, "Error con lista")
            return false
        }
        mValues?.clear()

        val addList = mValues!!.addAll(smsList)

        if (addList) {
            notifyDataSetChanged()
            return true
        }
        Log.e(TAG, "Error actualizando lista")
        return false
    }*/

}