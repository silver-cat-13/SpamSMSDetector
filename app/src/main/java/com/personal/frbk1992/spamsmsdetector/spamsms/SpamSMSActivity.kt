package com.personal.frbk1992.spamsmsdetector.spamsms

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.personal.frbk1992.spamsmsdetector.R
import com.personal.frbk1992.spamsmsdetector.*
import com.personal.frbk1992.spamsmsdetector.appInfo.AppInfoActivity
import com.personal.frbk1992.spamsmsdetector.classifier.SMSSpamClassifier
import com.personal.frbk1992.spamsmsdetector.main.MainActivity
import com.personal.frbk1992.spamsmsdetector.phisingDetector.URLCheck
import com.personal.frbk1992.spamsmsdetector.sms.SMSActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_spam_sms.*
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.concurrent.Callable
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * This activity show the list of spam sms, it looks for all sms in the device (checks if
 * the app has permission first) later prepares the model sms_model to take all the
 * features (the words) and detects all the sms message and the final list
 */
class SpamSMSActivity : AppCompatActivity(),
        SpamSMSActivityFragment.OnSMSSpamListFragmentInteractionListener {

    //TAG used for Logs
    private val TAG = this.javaClass.simpleName

    //constants values used if the classifiers will be created or destroyed
    private val INIT_CLASSIFIER = 1
    private val DESTROY_CLASSIFIER = 2

    //classifier for spam SMS
    private var smsSpamClassifier: SMSSpamClassifier? = null

    //arrayList sms
    private lateinit var smsList : ArrayList<SMSClass>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //set the view
        setContentView(R.layout.activity_spam_sms)
        //set the action bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initTensorFlowAndLoadModel()

        //get the sms array list
        smsList = ArrayList()
        val bundle = intent.extras
        if(bundle != null){
            smsList.addAll(bundle.getParcelableArrayList(SMS_LIST_INTENT))
        }

        //call the SpamSMSActivityFragment and check for SMS permission and ask for it in case
        // it's need it
        if (savedInstanceState == null) {
            //permission was granted
            startFragment(SpamSMSActivityFragment.newInstance(), SMS_SPAM_LIST_FRAGMENT_TAG)

        }
    }

    override fun onDestroy() {
        Single.just(DESTROY_CLASSIFIER)
                .subscribeOn(Schedulers.newThread())
                .subscribe(singleSMSSPamClassifier(this))
        super.onDestroy()
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //          OnSMSSpamListFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Funtion that retrieve sms and retrieve only the ones that are spam. The function use another
     * thread to classify the sms and after they are clyssify the activity call the fragment
     * to update the list
     */
    override fun showAllSpamSMS(){
        //check sms list
        Single.fromCallable(callableSpamListClassifier(smsList))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getSimpleSpamListClassifier())
    }



    /**
     * Function that is called when the user click an sms, this function show the detail of the sms
     * @param sms selected sms to show
     */
    override fun showSpamSMS(sms: SMSClass) {
        val intent = Intent(baseContext, SMSActivity::class.java)
        intent.putExtra(SMS_DETAIL_ACTIVITY, sms)
        startActivity(intent)
    }

    /**
     * function that called the TestUrlActivity via intent
     */
    override fun goInfoApp() {
        val intent = Intent(baseContext, AppInfoActivity::class.java)
        startActivity(intent)
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //          OnSMSSpamListFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                   SpamSMSActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////



    /**
     * Funcion que inicializa un fragment
     * @param fragment el fragment a inicializar
     * @param tag el tag que va a tener el Fragment
     */
    private fun startFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.spam_sms_container, fragment, tag)
                .commit()
    }

    /**
     * This funcion initialize the model used for clarified the sms as a spam
     * and the ulr as a phishing site
     */
    private fun initTensorFlowAndLoadModel() {
        Single.just(INIT_CLASSIFIER)
                .subscribeOn(Schedulers.newThread())
                .subscribe(singleSMSSPamClassifier(this))
    }


    /**
     * This function return a single of RxJava where create/destroy the SMSSpamClassifier
     * depending on the integer observable
     */
    private fun singleSMSSPamClassifier(context: Context): SingleObserver<Int> {
        return object : SingleObserver<Int> {

            override fun onSuccess(t: Int) {
                when(t){
                    INIT_CLASSIFIER ->{
                        Log.i(TAG, "Creating model for Spam classifier")

                        smsSpamClassifier = SMSSpamClassifier().create(
                                assetManager = context.assets,
                                modelFilename = SMS_MODEL_FILE,
                                inputName = INPUT,
                                outputName = OUTPUT)

                    }DESTROY_CLASSIFIER ->{
                    Log.i(TAG, "Closing Spam classifier")
                    smsSpamClassifier?.close()
                }
                }
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "Error with TensorFlow ${e.message}")
                throw RuntimeException("Error with TensorFlow!", e)
            }

            override fun onSubscribe(d: Disposable) {
                // onSubscribe
            }


        }
    }

    /**
     * This function return a callable of RxJava where create the uses the
     * smsSpamClassifier to classify all the SMS ion a list and return only the spam sms
     * @param smsList SMS to check if is Spam or not.
     * @return a Callable object that return an boolean
     */
    private fun callableSpamListClassifier(smsList: ArrayList<SMSClass>) : Callable<ArrayList<SMSClass>> {
        return Callable {
            val smsSpamList = ArrayList<SMSClass>()
            //test each sms
            for (sms in smsList) {
                //find the features of the sms
                val featuresSMS = getArraySMS(sms.content)

                //classify the sms
                if (smsSpamClassifier != null && smsSpamClassifier!!.isSpam(featuresSMS)) {
                    //sms is spam
                    Log.v(TAG, "Classifier found a Spam SMS")
                    //add an url if the spam sms has an url
                    smsSpamList.add(checkForUrl(sms))
                }
            }

            //return all the spam sms
            smsSpamList
        }
    }


    /**
     * Get a simple observer from the spam classifier, on success the function receives a list with
     * all spam sms, the observer calls the fragment to update the list of spam sms
     */
    private fun getSimpleSpamListClassifier(): SingleObserver<ArrayList<SMSClass>> {
        return object : SingleObserver<ArrayList<SMSClass>>{

            override fun onSuccess(smsList: ArrayList<SMSClass>) {
                Log.v(TAG, "Showing list of Spam SMS")
                val fragment = supportFragmentManager.findFragmentById(R.id.spam_sms_container)
                        as? SpamSMSActivityFragment
                fragment?.showSpamSMS(smsList)
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "Error classifying sms ${e.message}")
                throw RuntimeException("Error classifying sms ${e.message}", e)
            }

            override fun onSubscribe(d: Disposable) {
                //onSubscribe
            }


        }
    }


    /**
     * get the features of an sms
     */
    private fun getArraySMS(sms : String) : FloatArray{
        val featuresSMS = FloatArray(SMSSpamClassifier.ATTRIBUTE_AMOUNT)

        for (i in BAG_OF_WORDS.indices){
            //count the amount of words in the sms
            val amountWords = StringUtils.countMatches(sms, BAG_OF_WORDS[i])
            featuresSMS[i] = amountWords.toFloat()
        }
        return featuresSMS
    }


    private fun checkForUrl(sms: SMSClass) : SMSClass{
        /*
        Detects an url in the SMS
         */
        val urlPattern = Pattern.compile(
                "((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))",
                Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)

        val matcher = urlPattern.matcher(sms.content)
        while (matcher.find()) {
            //url found
            val match = matcher.group(1)

            if(URLCheck.isURLValid(match)) {
                Log.v(TAG, "URL in SMS $match")
                sms.url = match
            }else{
                Log.v(TAG, "URL $match is not a valid URL")
            }
        }

        return sms
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                   SpamSMSActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////





}
