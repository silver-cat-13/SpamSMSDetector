package ca.uvic.frbk1992.spamsmsdetector.sms

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import ca.uvic.frbk1992.spamsmsdetector.*
import ca.uvic.frbk1992.spamsmsdetector.classifier.PhishingClassifier
import ca.uvic.frbk1992.spamsmsdetector.classifier.SMSSpamClassifier

import kotlinx.android.synthetic.main.activity_sms.*
import java.util.concurrent.Executors

class SMSActivity : AppCompatActivity(), SMSDetailFragment.OnSMSDetailFragmentInteractionListener {

    var sms : SMSClass? = SMSClass()

    private val TAG = this.javaClass.simpleName

    private val SMS = "sms"

    private var phishingClassifier: PhishingClassifier? = null
    private val executor = Executors.newSingleThreadExecutor()

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
        super.onDestroy()
        executor.execute({ phishingClassifier!!.close() })
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
    override fun detectPhishingSite(features: FloatArray): Boolean
            = phishingClassifier!!.isPhishing(features)

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
     * This funcion initialize the model used for clarified the sms as a spam
     * and the ulr as a phishing site
     */
    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                phishingClassifier = PhishingClassifier().create(
                        assetManager = this.assets,
                        modelFilename = PHISHING_MODEL_FILE,
                        inputName = INPUT,
                        outputName = OUTPUT)

                Log.d(TAG, "Load Success")
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                       SMSActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////




}
