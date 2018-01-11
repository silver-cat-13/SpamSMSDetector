package ca.uvic.frbk1992.spamsmsdetector.spamsms

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import ca.uvic.frbk1992.spamsmsdetector.*
import ca.uvic.frbk1992.spamsmsdetector.classifier.PhishingClassifier
import ca.uvic.frbk1992.spamsmsdetector.classifier.SMSSpamClassifier
import ca.uvic.frbk1992.spamsmsdetector.phisingDetector.FindValuesURL
import ca.uvic.frbk1992.spamsmsdetector.phisingDetector.URLCheck
import ca.uvic.frbk1992.spamsmsdetector.sms.SMSActivity

import kotlinx.android.synthetic.main.activity_spam_sms.*
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * This activity show the list of spam sms, it looks for all sms in the device (checks if
 * the app has permission first) later prepares the model sms_model to take all the
 * features (the words) and detects all the sms message and the final list
 */
class SpamSMSActivity : AppCompatActivity(),
        SpamSMSActivityFragment.OnSMSSpamListFragmentInteractionListener {

    private val TAG = this.javaClass.simpleName

    private var smsClassifier: SMSSpamClassifier? = null
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spam_sms)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initTensorFlowAndLoadModel()

        //call the SpamSMSActivityFragment and check for SMS permission and ask for it in case
        // it's need it
        if (savedInstanceState == null && requestPermissionReceiveSMS()) {
            startFragment(SpamSMSActivityFragment.newInstance(), SMS_SPAM_LIST_FRAGMENT_TAG);
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.execute({ smsClassifier!!.close() })
    }


    /**
     * Funtion to check if the
     */
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
                    startFragment(SpamSMSActivityFragment.newInstance(), SMS_LIST_FRAGMENT_TAG)
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //          OnSMSSpamListFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Funtion that retrieve all spam sms in the phone
     * @return List of all spam SMS
     */
    override fun getSpamSMS(): ArrayList<SMSClass> {
        val sms = ArrayList<SMSClass>()
        val uriSMSURI = Uri.parse("content://sms/inbox")
        val cur = contentResolver.query(uriSMSURI, null, null, null, null)

        while (cur != null && cur.moveToNext()) {
            val id = cur.getString(cur.getColumnIndex("_id"))
            val address = cur.getString(cur.getColumnIndex("address"))
            val body = cur.getString(cur.getColumnIndexOrThrow("body"))
            sms.add(SMSClass(id.toInt(), address, body, spam = true))
        }

        cur?.close()

        return retrieveSpamSMS(sms)
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
     * This function is called when the user wants to delete all his spam sms
     */
    override fun deleteAllSpamSMS(smsList: ArrayList<SMSClass>) {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //          OnSMSSpamListFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                   SpamSMSActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////


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
        executor.execute {
            try {
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
     * This function check all the sms in the list and retrieve all the spam sms
     */
    private fun retrieveSpamSMS(smsList: ArrayList<SMSClass>) : ArrayList<SMSClass>{
        //  val iterator : MutableCollection<SMSClass> = smsList.iterator()
        val smsSpamList  = ArrayList<SMSClass>()
        //test each sms
        for (sms in smsList){
            //find the features of the sms
            val featuresSMS = getArraySMS(sms.content)

            //clasiffy the sms
            if(smsClassifier != null && smsClassifier!!.isSpam(featuresSMS)) {
                //sms is spam
                Log.e(TAG, "SMS ${sms.content} is Spam")
                //add an url if the spam sms has an url
                smsSpamList.add(checkForUrl(sms))
            }
            else {
                Log.v(TAG, "SMS is ${sms.content} Not Spam")
            }
        }

        //return all the spam sms
        return smsSpamList
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

        Log.v(TAG, "featuresSMS ${Arrays.toString(featuresSMS)}")
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
