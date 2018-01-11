package ca.uvic.frbk1992.spamsmsdetector.main

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.design.internal.NavigationMenuItemView
import android.support.v4.content.ContextCompat
import android.util.Log
import ca.uvic.frbk1992.spamsmsdetector.*
import ca.uvic.frbk1992.spamsmsdetector.phisingDetector.FindValuesURL
import ca.uvic.frbk1992.spamsmsdetector.classifier.PhishingClassifier
import ca.uvic.frbk1992.spamsmsdetector.classifier.SMSSpamClassifier
import ca.uvic.frbk1992.spamsmsdetector.phisingDetector.URLCheck
import ca.uvic.frbk1992.spamsmsdetector.sms.SMSActivity
import org.apache.commons.lang3.StringUtils
import java.util.concurrent.Executors
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import android.util.SparseArray
import android.widget.Toast
import ca.uvic.frbk1992.spamsmsdetector.R.id.main_container
import ca.uvic.frbk1992.spamsmsdetector.spamsms.SpamSMSActivity
import ca.uvic.frbk1992.spamsmsdetector.testurl.TestUrlActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.internal.util.HalfSerializer
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import io.reactivex.Observer
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;


/**
 * This is the main activity, this activity call the SMSListFragment fragment where the sms list will
 * show
 * @see SMSListFragment
 */
class MainActivity : RxAppCompatActivity(), SMSListFragment.OnSMSListFragmentInteractionListener,
        FindValuesURL.OnFinishFeaturesPhishingWebsite, NavigationView.OnNavigationItemSelectedListener {


    private val TAG = this.javaClass.simpleName

    private var phishingClassifier: PhishingClassifier? = null
    private var smsClassifier: SMSSpamClassifier? = null
    private val executor = Executors.newSingleThreadExecutor()

    var test : FindValuesURL? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        //create the RxPermission
        val rxPermission = RxPermissions(this)

        //call the SMSListFragment and check for SMS permission and ask for it in case it's need it
        if (savedInstanceState == null) {
            rxPermission.request(Manifest.permission.RECEIVE_SMS)
                    .subscribe({ granted ->
                if(granted){
                    //permission was granted
                    startFragment(SMSListFragment.newInstance(), SMS_LIST_FRAGMENT_TAG)
                }else{
                    //permission was not granted
                }
            })

        }


        testRxJava()

        //initTensorFlowAndLoadModel()

        //testClass()
    }

    override fun onDestroy() {
        super.onDestroy()
       // executor.execute({ phishingClassifier!!.close() })
       // executor.execute({ smsClassifier!!.close() })
    }

    fun testRxJava(){
        Log.e(TAG, " Test RX")

        val observable = Observable.create(ObservableOnSubscribe<Int> { e ->
            e.onNext(6)
            e.onNext(7)
            e.onNext(8)
            e.onNext(9)

            //Once the Observable has emitted all items in the sequence, call onComplete//
            e.onComplete()
        })

        observable.subscribe(getObserver(this))

        Log.e(TAG, " done")


        val list = listOf(1,2,3,4,5)
        list.toObservable()
                .subscribeOn(Schedulers.newThread())
                .subscribe(getObserver(this))

    }

    private fun getObserver(c : Context): Observer<Int> {
        return object : Observer<Int> {

            override fun onSubscribe(d: Disposable) {
                Log.e(TAG, " onSubscribe : " + d.isDisposed)
                println(" onSubscribe : " + d.isDisposed)
            }

            override fun onNext(value: Int) {
                Log.e(TAG, " value : " + value)
                println(" value : " + value)
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, " onError : " + e.message)
                println(" onError : " + e.message)
            }

            override fun onComplete() {
                //showToast(c, "onComplete")
                Log.e(TAG, " onComplete")
                println(" onComplete")
            }
        }
    }

    fun testClass(){
        val features = ArrayList<Float>(62033)
        val assetString = StringBuilder()
        var fIn :InputStream? = null
        var isr : InputStreamReader? = null
        var input : BufferedReader? = null
        try {
            fIn = this.getResources().getAssets()
                    .open("input.txt")
            isr = InputStreamReader(fIn)
            input =  BufferedReader(isr)
            var line: String
            while (true) {
                line = input.readLine()
                if (line == null) break //end of the text
                features.add(line.toFloat())
                //Log.v(TAG, line)
            }
        } catch (e : Exception) {
            Log.v(TAG, e.message)
        } finally {
            try {
                isr?.close()
                fIn?.close()
                input?.close()
            } catch (e2 : Exception) {
                Log.v(TAG, e2.message)
            }
        }

        val featuresArray = features.toFloatArray()
        val time = System.currentTimeMillis()
        testClassifier(featuresArray)
        Log.v(TAG, "time to run the function testClassifier ${time-System.currentTimeMillis()}")

    }

    private fun testClassifier(featuresArray: FloatArray) {
        phishingClassifier?.classifySeveralSites(featuresArray)
    }


    /**
     * this function is called when all the features has been detected
     */
    override fun siteFeatures(ctx: Context, url : String,features : FloatArray, _id : Int){

        Log.v(TAG, "features ${Arrays.toString(features)}")

        //Log.v(TAG, "features ${Arrays.toString(f)}")
       //Log.v(TAG, "features ${getStringAtributes(f)}")
        if(phishingClassifier!!.isPhishing(features)) {
            Log.e(TAG, "The Site $url is phishing")
        }
        else
            Log.v(TAG, "Site is $url NOT phishing")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.test_url -> {
                goTestUrl()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when(requestCode){
            REQUEST_PERMISSION_RECEIVE_SMS ->{
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showNeutralDialog(this, "Please", ":(", "close")
                }else{
                    //permission acepted, show fragment
                    startFragment(SMSListFragment.newInstance(), SMS_LIST_FRAGMENT_TAG)
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //              OnSMSListFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Funtion that retrieve all sms in the phone
     * @return List of all SMS
     */
    override fun getSMS(): ArrayList<SMSClass> {
        val sms = ArrayList<SMSClass>()
        val uriSMSURI = Uri.parse("content://sms/inbox")
        val cur = contentResolver.query(uriSMSURI, null, null, null, null)

        while (cur != null && cur.moveToNext()) {
            val id = cur.getString(cur.getColumnIndex("_id"))
            val address = cur.getString(cur.getColumnIndex("address"))
            val body = cur.getString(cur.getColumnIndexOrThrow("body"))
            sms.add(SMSClass(id.toInt(), address, body))
        }

        cur?.close()
        return sms
    }

    /**
     * Function that is called when the user click an sms, this function show the detail of the sms
     * @param sms selected sms to show
     */
    override fun showSMS(sms: SMSClass) {
        val intent = Intent(baseContext, SMSActivity::class.java)
        intent.putExtra(SMS_DETAIL_ACTIVITY, sms)
        startActivity(intent)
    }




    //list of spam sms that are spam sms
  //  private var smsSpamList : SparseArray<SMSClass>? = null


    /**
     * Function that check all the sms to look for all spam sms
     */
    override fun testAllSMSForSpam(smsList : ArrayList<SMSClass>) {
        val intent = Intent(baseContext, SpamSMSActivity::class.java)
        //intent.putExtra(SMS_DETAIL_ACTIVITY, sms)
        startActivity(intent)

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //              OnSMSListFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      MainActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Funcion que inicializa un fragment
     * @param fragment el fragment a inicializar
     * @param tag el tag que va a tener el Fragment
     */
    private fun startFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.main_container, fragment, tag)
                .commit()
    }


    /**
     * Function that check the permission of Manifest.permission.RECEIVE_SMS and ask for it
     * in case the user hasn't accpeted it
     * @return return false if the permission was already accpeted
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissionReceiveSMS() : Boolean{
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS)
                    , REQUEST_PERMISSION_RECEIVE_SMS);
            return false
        }
        return true
    }


    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                phishingClassifier = PhishingClassifier().create(
                        assetManager = this.assets,
                        modelFilename = PHISHING_MODEL_FILE,
                        inputName = INPUT,
                        outputName = OUTPUT)

                smsClassifier = SMSSpamClassifier().create(
                        assetManager = this.assets,
                        modelFilename = SMS_MODEL_FILE,
                        inputName = INPUT,
                        outputName = OUTPUT)

                Log.d(TAG, "Load Success")
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        }
    }



    /**
     * cget the features of an sms
     */
//    private fun getArraySMS(sms : String) : ArrayList<Float>{
//        val featuresSMS = ArrayList<Float>(SMSSpamClassifier.ATTRIBUTE_AMOUNT)
//
//        for (i in BAG_OF_WORDS.indices){
//            //count the amount of words in the sms
//            val amountWords = StringUtils.countMatches(sms, BAG_OF_WORDS[i])
//            featuresSMS[i] = amountWords.toFloat()
//        }
//
//        Log.v(TAG, "featuresSMS ${Arrays.toString(featuresSMS.toArray())}")
//        return featuresSMS
//    }

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

        Log.v(TAG, "featuresSMS ${Arrays.toString(featuresSMS)}")
        return featuresSMS
    }


    /**
     * function that called the TestUrlActivity via intent
     */
    fun goTestUrl() {
        val intent = Intent(baseContext, TestUrlActivity::class.java)
        startActivity(intent)
    }






    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      MainActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////




    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}



