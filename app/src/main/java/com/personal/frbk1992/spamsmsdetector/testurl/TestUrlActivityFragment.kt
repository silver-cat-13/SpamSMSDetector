package com.personal.frbk1992.spamsmsdetector.testurl

import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.personal.frbk1992.spamsmsdetector.R
import com.personal.frbk1992.spamsmsdetector.phisingDetector.FindValuesURL
import com.personal.frbk1992.spamsmsdetector.phisingDetector.URLCheck
import com.personal.frbk1992.spamsmsdetector.showToast
import kotlinx.android.synthetic.main.fragment_test_url.*

/**
 * A placeholder fragment containing a simple view.
 */
class TestUrlActivityFragment : Fragment(), View.OnClickListener,
        FindValuesURL.OnFinishFeaturesPhishingWebsite{

    private val TAG =this.javaClass.simpleName

    private var mListener: OnTestUrlFragmentInteractionListener? = null //listener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_test_url, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //set the title and content of the sms
        button_fragment_test_url_test_id.setOnClickListener(this)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnTestUrlFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    //the button is pressed
    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.button_fragment_test_url_test_id ->{
                //the button to test url was pressed
                testUrl(edit_text_test_url_id.text.toString())
            }
        }
    }

    /**
     * Test the URL given by the user
     */
    private fun testUrl(url : String){
        if(url.isEmpty()){
            //url is empty
            showToast(context!!, "URL is empty")
            return
        }

        if(URLCheck.isURLValid(url)) {
            val test = FindValuesURL(context!!, fragment = this,  url = url)
            test.checkShortUrl()
        }
        else showToast(context!!, "URL is not valid")
    }

    //this funciotn is called when the app get all the features of the app
    override fun siteFeatures(ctx: Context, url: String, features: FloatArray, _id: Int) {
        //Log.v(TAG, "features ${getStringAtributes(features)}")
        mListener?.testUrl(features)
    }

    /**
     *
     */
    interface OnTestUrlFragmentInteractionListener{
        fun testUrl(features : FloatArray)
    }

    companion object {
        fun newInstance(): TestUrlActivityFragment = TestUrlActivityFragment()
    }
}
