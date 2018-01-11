package ca.uvic.frbk1992.spamsmsdetector.sms

import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.*
import ca.uvic.frbk1992.spamsmsdetector.R
import ca.uvic.frbk1992.spamsmsdetector.SMSClass
import ca.uvic.frbk1992.spamsmsdetector.main.MyListAdapter
import ca.uvic.frbk1992.spamsmsdetector.main.SMSListFragment
import ca.uvic.frbk1992.spamsmsdetector.phisingDetector.FindValuesURL
import ca.uvic.frbk1992.spamsmsdetector.showToast
import kotlinx.android.synthetic.main.fragment_sms.*
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
class SMSDetailFragment : Fragment(), FindValuesURL.OnFinishFeaturesPhishingWebsite {

    private val TAG =this.javaClass.simpleName

    val ARG_SMS = "sms"
    var sms : SMSClass? = null

    private var mListener: OnSMSDetailFragmentInteractionListener? = null //listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sms = arguments?.getParcelable(ARG_SMS)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if(sms?.url != "") setHasOptionsMenu(true) // add the menu
        return inflater.inflate(R.layout.fragment_sms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //set the title and content of the sms
        fragment_sms_title.text = sms?.title
        fragment_sms_content.text = sms?.content
        if(sms?.spam!!) fragment_sms_indicatpr_spam_phishing.text = "SMS is Spam!!!"
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnSMSDetailFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * Tell the fragment which menu it will load
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(sms?.url != "") {
            inflater.inflate(R.menu.fragment_spam_sms_detail_menu, menu)

        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * This function is called when the user selected an option in the menu
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.fragment_spam_sms_detail_test_url_id ->{
                //the test the url if it is an phishing site
                sms!!.url = "http://www.maciel.med.br/"
                val test = FindValuesURL(context!!, fragment = this, url = sms!!.url, _id= sms!!.id)
                test.checkShortUrl()
            }
        }
        return false
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                   OnFinishFeaturesPhishingWebsite  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * this function is called when all the features has been detected
     */
    override fun siteFeatures(ctx: Context, url: String, features: FloatArray, _id: Int) {
        Log.v(TAG, "url $url")
        Log.v(TAG, "features ${Arrays.toString(features)}")

        if(mListener?.detectPhishingSite(features)!!) showToast(context!!, "Warning: URL is phishing")
        else showToast(context!!, "URL is not phishing")
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                   OnFinishFeaturesPhishingWebsite  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This interface is implemented by SMSActivity to connect between the Fragment and the Activity
     */
    interface OnSMSDetailFragmentInteractionListener{
        fun detectPhishingSite(features : FloatArray) : Boolean
    }

    companion object {

        val ARG_SMS = "sms"

        fun newInstance(sms : SMSClass): SMSDetailFragment{
            val fragment = SMSDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_SMS, sms)
            fragment.arguments = args
            return fragment
        }
    }
}
