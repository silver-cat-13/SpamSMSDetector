package ca.uvic.frbk1992.spamsmsdetector.spamsms

import android.content.Context
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.*
import android.widget.ListView
import ca.uvic.frbk1992.spamsmsdetector.R
import ca.uvic.frbk1992.spamsmsdetector.SMSClass
import kotlinx.android.synthetic.main.fragment_spam_sms.*

/**
 * This fragment shows a list of all spam sms, it calls the activity for all the spam sms
 * with {@link OnSMSSpamListFragmentInteractionListener.getSpamSMS}
 */
class SpamSMSActivityFragment : ListFragment() {

    private var mListener: OnSMSSpamListFragmentInteractionListener? = null //listener
    private var mListView : ListView? = null
    lateinit var myListAdapter : SpamSMSListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true) // add the menu
        return inflater.inflate(R.layout.fragment_spam_sms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // ubica la vista en el fragment_notes_list.xml
        mListView = view.findViewById(android.R.id.list)

        //getAll the spam SMS
        mListener?.showAllSpamSMS()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnSMSSpamListFragmentInteractionListener) {
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
        mListener?.showSpamSMS(myListAdapter.getAllSMS()!!.get(position))
    }

    /**
     * Tell the fragment which menu it will load
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_spam_sms_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * This function is called when the user selected an option in the menu
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.fragment_spam_list_menu_information_app ->{
                //the test option was selected
                mListener?.goInfoApp()
            }
        }
        return false
    }


    fun showSpamSMS(smsList: ArrayList<SMSClass>){
        fragment_spam_sms_progress_bar_loading.visibility = View.GONE
        if(smsList.isEmpty()){
            text_view_spam_sms_list_indicator.text =
                    context!!.getString(R.string.fragment_spam_sms_text_view_spam_sms_indicator_no_spam_string)
        }else {
            //get the number of the
            val value = context!!.getString(R.string.fragment_spam_sms_text_view_spam_sms_indicator_string)
                    .replace("#", smsList.size.toString())
            text_view_spam_sms_list_indicator.text = value
        }

        //inicia el adaptador
        myListAdapter = SpamSMSListAdapter(this.context,
                smsList,
                mListener)

        // le pasa el el adaptador al listview
        listView.adapter = myListAdapter;
    }



    /**
     * This interface is implemented by MainActivity to connect between the Fragment and the Activity
     */
    interface OnSMSSpamListFragmentInteractionListener{
        //get all sms
        fun showAllSpamSMS()

        //show the detail of a sms
        fun showSpamSMS(sms : SMSClass)

        //go to the information app activity
        fun goInfoApp()
    }

    companion object {
        fun newInstance(): SpamSMSActivityFragment = SpamSMSActivityFragment()
    }
}
