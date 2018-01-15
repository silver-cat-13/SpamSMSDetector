package ca.uvic.frbk1992.spamsmsdetector.sms

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import ca.uvic.frbk1992.spamsmsdetector.*
import ca.uvic.frbk1992.spamsmsdetector.classifier.PhishingClassifier
import ca.uvic.frbk1992.spamsmsdetector.classifier.SMSSpamClassifier
import com.trello.rxlifecycle2.android.RxLifecycleAndroid
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_sms.*
import org.apache.commons.lang3.StringUtils
import java.util.concurrent.Executors

class SMSActivity : RxAppCompatActivity(), SMSDetailFragment.OnSMSDetailFragmentInteractionListener {

    var sms : SMSClass? = SMSClass()

    private val TAG = this.javaClass.simpleName

    private val SMS = "sms"

    private val INIT_CLASSIFIER = 1
    private val DESTROY_CLASSIFIER = 2

    private var phishingClassifier: PhishingClassifier? = null
    private var smsSpamClassifier : SMSSpamClassifier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //get the sms
        val bundle = intent.extras
        if(bundle != null){
            sms = bundle.getParcelable(SMS_DETAIL_ACTIVITY);
        }

        if(savedInstanceState != null){
            sms = savedInstanceState.getParcelable(SMS);
        }

        initTensorFlowAndLoadModel()

        //call the SMSListFragment
        if (savedInstanceState == null) {
            startFragment(SMSDetailFragment.newInstance(sms!!), SMS_DETAIL_FRAGMENT_TAG);
        }
    }

    override fun onDestroy() {
        Single.just(DESTROY_CLASSIFIER)
                .subscribeOn(Schedulers.newThread())
                .compose(RxLifecycleAndroid.bindActivity(lifecycle()))
                .subscribe(getSimpleObserverInitPhishingClassifier(this))
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
     * This function is called to detect if the features of a site is a phishing site
     */
    override fun detectPhishingSite(features: FloatArray): Boolean {

        //create observable that check if the features correspond to a phishing site
        val featuresObservable = Single.fromCallable{phishingClassifier!!.isPhishing(features)}
        featuresObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Boolean>{
                    override fun onSuccess(t: Boolean) {
                        if(t) showToast(application, "Warning: URL is phishing")
                        else showToast(application, "URL is not phishing")
                    }

                    override fun onError(e: Throwable) {
                        throw RuntimeException("Error with TensorFlow!", e)
                    }

                    override fun onSubscribe(d: Disposable) {
                        //onSubscribe
                    }

                })

        return false

    }

    /**
     * This function is called to detect if the sms is spam or not
     * the function is called after the view is done on the fragment and
     * the detection is done on a new thread using RxJava
     * In case the SMS is Spam the Activity called a function in the
     * fragment to warn the user about the Spam SMS.
     *
     * The SMS used is the one received by the activity
     */
    override fun detectSpamSMS(){

        //get the features for the spam classifier
        val featuresSMS = FloatArray(SMSSpamClassifier.ATTRIBUTE_AMOUNT)
        for (i in BAG_OF_WORDS.indices){
            //count the amount of words in the sms
            val amountWords = StringUtils.countMatches(sms!!.content, BAG_OF_WORDS[i])
            featuresSMS[i] = amountWords.toFloat()
        }

        //create observable that check if the features correspond to a phishing site
        val featuresObservable = Single.fromCallable{smsSpamClassifier!!.isSpam(featuresSMS)}
        featuresObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Boolean>{
                    override fun onSuccess(t: Boolean) {
                        if(t) {
                            //SMS is Spam
                            val fragment : SMSDetailFragment
                                    = fragmentManager.findFragmentById(R.id.sms_container) as SMSDetailFragment

                            fragment.smsIsSpam()

                        }
                    }

                    override fun onError(e: Throwable) {
                        throw RuntimeException("Error with TensorFlow!", e)
                    }

                    override fun onSubscribe(d: Disposable) {
                        //onSubscribe
                    }

                })
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //            OnSMSDetailFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                       SMSActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Funcion que inicializa un fragment
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
     * This funcion initialize the model used for clarifie the ulr as a phishing site
     */
    private fun initTensorFlowAndLoadModel() {
        //initialize the phishing model
        Single.just(INIT_CLASSIFIER)
                .subscribeOn(Schedulers.newThread())
                .compose(RxLifecycleAndroid.bindActivity(lifecycle()))
                .subscribe(getSimpleObserverInitPhishingClassifier(this))
    }


    /**
     * Get a simple observer from a class Classifier
     */
    private fun getSimpleObserverInitPhishingClassifier(context: Context): SingleObserver<Int> {
        return object : SingleObserver<Int>{

            override fun onSuccess(t: Int) {
                when(t){
                    INIT_CLASSIFIER ->{
                        Log.v(TAG, "Creating model for phishing classifier")
                        phishingClassifier = PhishingClassifier().create(
                                assetManager = context.assets,
                                modelFilename = PHISHING_MODEL_FILE,
                                inputName = INPUT,
                                outputName = OUTPUT)

                        smsSpamClassifier = SMSSpamClassifier().create(
                                assetManager = context.assets,
                                modelFilename = SMS_MODEL_FILE,
                                inputName = INPUT,
                                outputName = OUTPUT)
                    }
                    DESTROY_CLASSIFIER ->{
                        Log.v(TAG, "Closing phishing model")
                        phishingClassifier!!.close()
                        smsSpamClassifier!!.close()
                    }
                }

            }

            override fun onError(e: Throwable) {
                throw RuntimeException("Error with TensorFlow!", e)
            }

            override fun onSubscribe(d: Disposable) {
                // onSubscribe
            }


        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                       SMSActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
