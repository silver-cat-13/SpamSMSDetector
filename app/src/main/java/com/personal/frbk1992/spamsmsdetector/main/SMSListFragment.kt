package com.personal.frbk1992.spamsmsdetector.main

import android.content.Context
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.*
import android.widget.ListView

import com.personal.frbk1992.spamsmsdetector.R
import com.personal.frbk1992.spamsmsdetector.SMSClass
import com.personal.frbk1992.spamsmsdetector.showToast


/**
 * A fragment representing a list of SMS the user has.
 *
 * Activities containing this fragment MUST implement the [OnSMSListFragmentInteractionListener]
 * interface.
 */
class SMSListFragment : ListFragment(){


    private var mListener: OnSMSListFragmentInteractionListener? = null //listener
    private var mListView : ListView? = null
    lateinit var myListAdapter : MyListAdapter
    private  lateinit var smsList : ArrayList<SMSClass>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true) // add the menu
        return inflater.inflate(R.layout.fragment_sms_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // ubica la vista en el fragment_notes_list.xml
        mListView = view.findViewById(android.R.id.list)

        //get all the sms
        smsList =  mListener!!.getSMS()

        //inicia el adaptador
        myListAdapter = MyListAdapter(this.context,
                smsList,
                mListener)

        // le pasa el el adaptador al listview
        listView.adapter = myListAdapter;
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnSMSListFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        //sms was selected, go the detail
        //get the sms selected and show ir
        mListener?.showSMS(myListAdapter.getAllSMS()!![position])
    }



    /**
     * Tell the fragment which menu it will load
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_sms_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * This function is called when the user selected an option in the menu
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.fragment_list_menu_test_spam_sms_id ->{
                //the test option was selected
                mListener?.testAllSMSForSpam(smsList)
            }
            R.id.fragment_list_menu_information_app -> {
                //go to the information activity
                mListener?.goInfoApp()
            }
        }
        return false
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                   SMSListFragment  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * this function is called by the activity to update the list of sms
     */
    fun updateSMSList(smsList : ArrayList<SMSClass>){
        //update the list
        myListAdapter.update(smsList)

        showToast(this.context!!, "Showing ${smsList.size} SPAM SMS")
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                   SMSListFragment  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * This interface is implemented by MainActivity to connect between the Fragment and the Activity
     */
    interface OnSMSListFragmentInteractionListener{
        //get all sms
        fun getSMS() : ArrayList<SMSClass>

        //show the detail of a sms
        fun showSMS(sms : SMSClass)

        //test all the sms to look for spam sms
        fun testAllSMSForSpam(smsList : ArrayList<SMSClass>)

        //go to the information app activity
        fun goInfoApp()
    }

    companion object {
        fun newInstance(): SMSListFragment = SMSListFragment()
    }

}
