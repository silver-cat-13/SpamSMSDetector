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
 *
 * Class adapter for the list
 */
class MyListAdapter(val mContext: Context?,
                    val mValues: ArrayList<SMSClass>?,
                    val mListener : SMSListFragment.OnSMSListFragmentInteractionListener?)
    : ArrayAdapter<SMSClass>(mContext, R.layout.content_note, R.id.text_view_list_title, mValues) {

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
    //                         Metodos de la clase MyNotesListViewAdapter                         //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun remove(sms: SMSClass) {
        mValues!!.remove(sms)
        notifyDataSetChanged()
    }

    fun getAllSMS(): ArrayList<SMSClass>? = mValues


    /*
    Funcion que se llama cuando se actualiza la lista de sms
    */
    fun update(smsList: List<SMSClass>?): Boolean {
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
    }

}