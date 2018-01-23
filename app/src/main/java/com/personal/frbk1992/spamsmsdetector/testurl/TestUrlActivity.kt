package com.personal.frbk1992.spamsmsdetector.testurl

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.personal.frbk1992.spamsmsdetector.R
import com.personal.frbk1992.spamsmsdetector.*
import com.personal.frbk1992.spamsmsdetector.classifier.PhishingClassifier

import kotlinx.android.synthetic.main.activity_test_url.*
import java.util.*
import java.util.concurrent.Executors

class TestUrlActivity : AppCompatActivity(), TestUrlActivityFragment.OnTestUrlFragmentInteractionListener {

    private val TAG = this.javaClass.simpleName

    private var phishingClassifier: PhishingClassifier? = null
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_url)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initTensorFlowAndLoadModel()

        if (savedInstanceState == null) {
            startFragment(TestUrlActivityFragment.newInstance(), TEST_URL_FRAGMENT_TAG);
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.execute({ phishingClassifier!!.close() })
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //              OnTestUrlFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This funtion is called when the user want to try if an url is phishing
     */
    override fun testUrl(features: FloatArray){
        Log.v(TAG, "features ${Arrays.toString(features)}")

        if(phishingClassifier!!.isPhishing(features)) {
            showToast(this, "The URL is a phishing site")
        }
        else
            showToast(this, "The URL is not a phishing site")

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //              OnTestUrlFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                   TestUrlActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Funcion que inicializa un fragment
     * @param fragment el fragment a inicializar
     * @param tag el tag que va a tener el Fragment
     */
    private fun startFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.test_url_container, fragment, tag)
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
    //                                   TestUrlActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////



}
