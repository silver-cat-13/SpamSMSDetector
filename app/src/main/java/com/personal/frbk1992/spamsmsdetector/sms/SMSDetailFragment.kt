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
 * This fragment show the detail of the SMS
 * if the SMS is Spam it will show a warning and if the SMS is spam and contains an URL it will
 * show a floating button to check if the URL is linked to a phishing site
 */
class SMSDetailFragment : Fragment(), FindValuesURL.OnFinishFeaturesPhishingWebsite {

    //TAG for Logs
    private val TAG =this.javaClass.simpleName

    //name for the argument for the SMS
    private val ARG_SMS = "sms"
    private var sms : SMSClass? = null

    //these two variables are used to check if the app is checking if the URL is phishing or not
    //or if it already did.

    //this variable indicate if the button to check if the url is phishing was pressed
    private var phishingCheck = false
    //this variable indicate if the app is checking if the URL is phishing is in process
    private var phishingInProcess = false
    //this variable indicate if the there was an error checking the URL
    private var phishingError = false

    private var mListener: OnSMSDetailFragmentInteractionListener? = null //listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get the argument passed by the activity
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
            //set the view visible
            fragment_sms_indicator_spam_phishing.visibility = View.VISIBLE
            fragment_sms_indicator_spam_phishing.text = getString(R.string.fragment_sms_indicator_spam_phishing)

            //check if SMS has an URL
            if( sms!!.url != ""){
                //spam sms has URL
                fragment_sms_floating_button_test_phishing_url.visibility = View.VISIBLE
            }
        }

        // check if it is phishing the URL
        RxView.clicks(fragment_sms_floating_button_test_phishing_url)
                .throttleFirst(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ _ ->
                    //Perform some work here//
                    if(sms!!.phishing){
                        //site is phishing, there is no need to check
                        showToast(activity!!.applicationContext,
                                getString(R.string.fragment_sms_check_url_for_phishing_url_phishing))
                        //set phishingCheck to true to indicate it was already checked and
                        //phishingInProcess to false
                        phishingInProcess = false
                        phishingCheck = true
                    }else if(phishingError){
                        //Error
                        showToast(activity!!.applicationContext,
                                getString(R.string.fragment_sms_check_url_error))
                    }
                    else if(phishingCheck && !sms!!.phishing){
                        //it was already checked and site is not phishing
                        showToast(activity!!.applicationContext,
                                getString(R.string.fragment_sms_check_url_for_phishing_url_not_phishing))
                    }else if(!phishingCheck && phishingInProcess){
                        //button was pressed, check is in process
                        showToast(activity!!.applicationContext,
                                getString(R.string.fragment_sms_check_url_for_phishing_is_in_process))
                    }
                    else if(!phishingCheck && !phishingInProcess){
                        //Phishing is not in process and the button hasn't pressed, until now
                        showToast(activity!!.applicationContext,
                                getString(R.string.fragment_sms_check_url_for_phishing_start))

                        //check if URL is phishing here
                        FindValuesURL<Fragment>(context!!, listener = this, url = sms!!.url).getFeatures()
                        //phishingInProcess to true to indicate the app is checking if the URL is phishing
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
     * Function called when the Activity when the SMS given is spam
     * it shows the warn about the sms is spam
     */
    fun smsIsSpam(){
        //showToast(context!!, getString(R.string.fragment_sms_indicator_spam_phishing))
        fragment_sms_indicator_spam_phishing.visibility = View.VISIBLE
        fragment_sms_indicator_spam_phishing.text = getString(R.string.fragment_sms_indicator_spam_phishing)
    }

    /**
     * Function called by the Activity when the SMS is SPAM and contains an URL
     * it sets into visible the floating button
     */
    fun smsContainsURL(){
        fragment_sms_floating_button_test_phishing_url.visibility = View.VISIBLE
    }

    /**
     * Funtion called when the app have the result if the url is phishing or not, set the value of the
     * set the value ofthe sms instance and set the boolean values to the specific values
     */
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
    override fun siteFeatures(ctx: Context, url: String, features: FloatArray) {
        //Log.v(TAG, "url $url")
       // Log.v(TAG, "features ${Arrays.toString(features)}")

        //check if site is phishing
        mListener?.detectPhishingSite(features)!!

    }

    /**
     * Site could not be found, code != 200
     */
    override fun errorNoFoundUrl() {
        phishingError = true
        //Error
        showToast(activity!!.applicationContext,
                getString(R.string.fragment_sms_check_url_error))
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

        /**
         * Create an instance of the fragment with the value of the SMSClass instance
         */
        fun newInstance(sms : SMSClass): SMSDetailFragment{
            val fragment = SMSDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_SMS, sms)
            fragment.arguments = args
            return fragment
        }
    }
}
