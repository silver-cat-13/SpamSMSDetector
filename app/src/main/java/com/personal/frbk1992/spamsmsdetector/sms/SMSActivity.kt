package com.personal.frbk1992.spamsmsdetector.sms

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.personal.frbk1992.spamsmsdetector.R
import com.personal.frbk1992.spamsmsdetector.*
import com.personal.frbk1992.spamsmsdetector.classifier.PhishingClassifier
import com.personal.frbk1992.spamsmsdetector.classifier.SMSSpamClassifier
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_sms.*
import java.util.concurrent.Callable

/**
 * This activity is used to show the detail of the activity, if the SMS is Spam it will show a warning
 * at the top of the fragment and if it contains a URL and it is spam, it will show a floating button
 * to checl if the URL is linked to a phishing site
 */
class SMSActivity : AppCompatActivity(), SMSDetailFragment.OnSMSDetailFragmentInteractionListener {

    //SMSClass instance
    var sms : SMSClass? = SMSClass()

    //TAG for logs
    private val TAG = this.javaClass.simpleName

    //String with ID of the SMS to save the SMS with savedInstanceState
    private val SMS = "sms"

    //variables used to indicate if the classifier must be created or destroyed
    private val INIT_CLASSIFIER = 1
    private val DESTROY_CLASSIFIER = 2

    //SMS and Phishing classifiers
    private var phishingClassifier: PhishingClassifier? = null
    private var smsSpamClassifier : SMSSpamClassifier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //set the view
        setContentView(R.layout.activity_sms)
        //set a toolbar
        setSupportActionBar(toolbar)

        //show the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //get the sms from the intent
        val bundle = intent.extras
        if(bundle != null){
            sms = bundle.getParcelable(SMS_DETAIL_ACTIVITY)
        }

        //get the sms form savedInstanceState
        if(savedInstanceState != null){
            sms = savedInstanceState.getParcelable(SMS)
        }

        //get the URL, in case there is one
        sms!!.url = SMSSpamClassifier.getUrlFromSMS(sms!!.content)

        //call the SMSListFragment
        if (savedInstanceState == null) {
            startFragment(SMSDetailFragment.newInstance(sms!!), SMS_DETAIL_FRAGMENT_TAG)
        }
        //init the models
        initTensorFlowAndLoadModel()
    }

    override fun onDestroy() {
        //destroy the classifier
        Single.just(DESTROY_CLASSIFIER)
                .subscribeOn(Schedulers.newThread())
                .subscribe(singlePhishingClassifier(this))
        Single.just(DESTROY_CLASSIFIER)
                .subscribeOn(Schedulers.newThread())
                .subscribe(singleSMSSPamClassifier(this))
        super.onDestroy()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        //save the service state
        savedInstanceState?.putParcelable(SMS, sms)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        //restore the service bound state
        sms = savedInstanceState?.getParcelable(SMS) ?: SMSClass()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //            OnSMSDetailFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This function is called to detect if the features of a site is a phishing site, it uses the
     * phishing classifier for it
     * @param features: float array with all values of the features [-1.0, 0.0 or 1.0]
     */
    override fun detectPhishingSite(features: FloatArray): Boolean {

        //create observable that check if the features correspond to a phishing site
        val featuresObservable = Single.fromCallable{ phishingClassifier!!.isPhishing(features) }
        featuresObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Boolean>{
                    override fun onSuccess(t: Boolean) {
                        //tell the fragment the answer about the phishing site
                        val fragment = supportFragmentManager.findFragmentById(R.id.sms_container)
                                as? SMSDetailFragment
                        fragment?.sitePhishingResult(t)
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "${e.message}")
                        throw RuntimeException("Error with TensorFlow! ${e.message}", e)
                    }

                    override fun onSubscribe(d: Disposable) {
                        //onSubscribe
                    }

                })

        return false

    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //            OnSMSDetailFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                       SMSActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Start the fragment
     * @param fragment el fragment a inicializar
     * @param tag el tag que va a tener el Fragment
     */
    private fun startFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.sms_container, fragment, tag)
                .commit()
    }


    /**
     * This function inizialize the classifiers but only if there is need to. If the activity
     * was called on an Spam SMS the SMS Spam classifier won't be called, if it is unknown if the
     * SMS is Spam, the Spam classifier will be created and the sms will be check if it is spam or
     * not. if the SMS don't have an URL, (or the sms is not Spam) the phishing classifier
     * won't be called either
     */
    private fun initTensorFlowAndLoadModel() {
        if(!sms!!.spam) {
            //SMS is not Spam,
            Single.fromCallable(callableSpamClassifier(this))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getSimpleSpamClassifier())
        }else if(sms!!.url != ""){

            if(sms!!.url != "") {
                //init phishing model
                Single.just(INIT_CLASSIFIER)
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(singlePhishingClassifier(this))
            }

            //SMS does contains URL
            //tell the fragment about it to show the floating button
            val fragment = supportFragmentManager.findFragmentById(R.id.sms_container) as? SMSDetailFragment
            fragment?.smsContainsURL()
        }
    }


    /**
     * This function return a single of RxJava where create/destroy the PhishingClassifier
     * depending on the integer observable
     */
    private fun singlePhishingClassifier(context: Context): SingleObserver<Int> {
        return object : SingleObserver<Int>{

            override fun onSuccess(t: Int) {
                when(t){
                    INIT_CLASSIFIER ->{
                        Log.i(TAG, "Creating model for phishing classifier")
                        //create the model
                        phishingClassifier = PhishingClassifier().create(
                                assetManager = context.assets,
                                modelFilename = PHISHING_MODEL_FILE,
                                inputName = INPUT,
                                outputName = OUTPUT)

                    }DESTROY_CLASSIFIER ->{
                        Log.i(TAG, "Closing phishing classifier")
                        //close the model
                        phishingClassifier?.close()
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
     * This function return a single of RxJava where create/destroy the SMSSpamClassifier
     * depending on the integer observable
     */
    private fun singleSMSSPamClassifier(context: Context): SingleObserver<Int> {
        return object : SingleObserver<Int>{

            override fun onSuccess(t: Int) {
                Log.e(TAG, "Thread Name 3 ${Thread.currentThread().name}")
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
     * This function return a callable of RxJava where create the SMSSpamClassifier.
     * Also, check if the sms is spam or not, in the case is spam return true, false otherwise
     */
    private fun callableSpamClassifier(context: Context): Callable<Boolean> {
        return Callable {
            Log.v(TAG, "Creating model for Spam classifier")
            smsSpamClassifier = SMSSpamClassifier().create(
                    assetManager = context.assets,
                    modelFilename = SMS_MODEL_FILE,
                    inputName = INPUT,
                    outputName = OUTPUT)
            //check if the sms is spam, get all features first
            smsSpamClassifier!!.isSpam(SMSSpamClassifier.getFeaturesForSpamClassifier(sms!!.content))
        }
    }


    /**
     * Get a simple observer from the spam classifier, if the observable is true it means the sms
     * is spam, false otherwise. In the case the sms is true the activity call a function in the
     * fragment to warn the user about it.
     */
    private fun getSimpleSpamClassifier(): SingleObserver<Boolean> {
        return object : SingleObserver<Boolean>{

            override fun onSuccess(t: Boolean) {
                if (t) {
                    //SMS is Spam
                    val fragment = supportFragmentManager.findFragmentById(R.id.sms_container)
                            as? SMSDetailFragment
                    fragment?.smsIsSpam()

                    if(sms!!.url != ""){
                        //SMS does contains URL
                        //tell the fragment about it
                        fragment?.smsContainsURL()
                        if(phishingClassifier == null) {
                            //sms has an URL but the phishingClassifier is null
                            //init phishing model
                            Single.just(INIT_CLASSIFIER)
                                    .subscribeOn(Schedulers.newThread())
                                    .subscribe(singlePhishingClassifier(applicationContext))
                        }
                    }

                }
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "Error creating the spam classifier ${e.message}")
                throw RuntimeException("Error creating the spam classifier ${e.message}", e)
            }

            override fun onSubscribe(d: Disposable) {
                //onSubscribe
            }


        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                       SMSActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
