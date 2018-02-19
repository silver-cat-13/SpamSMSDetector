package com.personal.frbk1992.spamsmsdetector.sms

import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.*
import com.personal.frbk1992.spamsmsdetector.R
import com.personal.frbk1992.spamsmsdetector.SMSClass
import com.personal.frbk1992.spamsmsdetector.phisingDetector.FindValuesURL
import com.personal.frbk1992.spamsmsdetector.showToast
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_sms.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A placeholder fragment containing a simple view.
 */
class SMSDetailFragment : Fragment(), FindValuesURL.OnFinishFeaturesPhishingWebsite {

    private val TAG =this.javaClass.simpleName

    val ARG_SMS = "sms"
    var sms : SMSClass? = null


    var phishingCheck = false //this variable indicate if the button to check if the url is phishing was pressed
    var phishingInProcess = false //this variable indicate if the app is checking if the URL is phishing is in process

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

        //warn the user about the spam sms en case it is spam
        if(sms?.spam!!) {
            //SMS is spam
            fragment_sms_indicator_spam_phishing.visibility = View.VISIBLE
            fragment_sms_indicator_spam_phishing.text = getString(R.string.fragment_sms_indicator_spam_phishing)

            //check if SMS has an URL
            if( sms!!.url != ""){
                //spam sms has URL
                fragment_sms_floating_button_test_phishing_url.visibility = View.VISIBLE
                //use RxJava to check if the URL is a phishing site on the click in the floating button
            }
        }

        // check if it is phishing the URL
        RxView.clicks(fragment_sms_floating_button_test_phishing_url)
                .throttleFirst(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ _ ->
                    //Perform some work here//
                    if(sms!!.phishing){
                        //site is phishing
                        showToast(activity!!.applicationContext,
                                getString(R.string.fragment_sms_check_url_for_phishing_url_phishing))
                        phishingInProcess = false
                        phishingCheck = true
                    }else if(phishingCheck && !sms!!.phishing){
                        //it was already checked and site is not phishing
                        showToast(activity!!.applicationContext,
                                getString(R.string.fragment_sms_check_url_for_phishing_url_not_phishing))
                    }else if(!phishingCheck && phishingInProcess){
                        //button was pressed, check is in process
                        showToast(activity!!.applicationContext,
                                getString(R.string.fragment_sms_check_url_for_phishing_is_in_process))
                    }else if(!phishingCheck && !phishingInProcess){
                        //Phishing is not in process and the button hasn't pressed, until now
                        showToast(activity!!.applicationContext,
                                getString(R.string.fragment_sms_check_url_for_phishing_start))

                        //check if URL is phishing here
                        FindValuesURL(context!!, fragment = this, url = sms!!.url, _id= sms!!.id).getFeatures()
                        phishingInProcess = true

                    }
                })


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


    /**
     * Function called when the Activity when the SMS given is spam
     */
    fun smsIsSpam(){
        //showToast(context!!, getString(R.string.fragment_sms_indicator_spam_phishing))
        fragment_sms_indicator_spam_phishing.visibility = View.VISIBLE
        fragment_sms_indicator_spam_phishing.text = getString(R.string.fragment_sms_indicator_spam_phishing)
    }

    /**
     * Function called by the Activity when the SMS is SPAM and contains an URL
     */
    fun smsContainsURL(){
        fragment_sms_floating_button_test_phishing_url.visibility = View.VISIBLE
    }

    fun sitePhishingResult(result : Boolean){
        sms!!.phishing = result

        //site was already checked, change to true
        phishingCheck = true

        //the phishing process is done, change to false
        phishingInProcess = false
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

        //check if site is phishing
        mListener?.detectPhishingSite(features)!!

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
