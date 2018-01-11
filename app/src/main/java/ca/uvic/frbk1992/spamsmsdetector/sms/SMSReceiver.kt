package ca.uvic.frbk1992.spamsmsdetector.sms

import android.content.BroadcastReceiver
import android.content.Context

import android.telephony.SmsMessage
import android.util.Log
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent

import android.support.v4.app.NotificationCompat
import ca.uvic.frbk1992.spamsmsdetector.*
import ca.uvic.frbk1992.spamsmsdetector.classifier.PhishingClassifier
import ca.uvic.frbk1992.spamsmsdetector.classifier.SMSSpamClassifier
import ca.uvic.frbk1992.spamsmsdetector.main.MainActivity
import ca.uvic.frbk1992.spamsmsdetector.phisingDetector.FindValuesURL
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Pattern




/*
This is  areceover class, this class is called when an incoming sms is in the phone
 */
class SMSReceiver : BroadcastReceiver(), FindValuesURL.OnFinishFeaturesPhishingWebsite{


    private val TAG = this.javaClass.simpleName

    private var phishingClassifier: PhishingClassifier? = null
    private var smsClassifier: SMSSpamClassifier? = null
    private val executor = Executors.newSingleThreadExecutor()

    override fun onReceive(context: Context, intent: Intent) {
        //init the classifiers
        initTensorFlowAndLoadModel(context)

        val bundle = intent.extras
        if (bundle != null) {
            Log.i(TAG, "SMSReceiver : Reading Bundle")

            val pdus = bundle.get("pdus") as Array<*>
            val sms = SmsMessage.createFromPdu(pdus[0] as ByteArray)

            Log.i(TAG, sms.displayMessageBody)
            Log.i(TAG, sms.messageBody)
            Log.i(TAG, sms.displayOriginatingAddress)
            Log.i(TAG, sms.originatingAddress)

            classifySMS(context, sms.messageBody, sms.originatingAddress)
            //showNotification(context, sms.messageBody, sms.originatingAddress)

        }
    }

    /**
     * Function that shows a notification of the sms
     */
    private fun showNotification(ctx : Context, sms : String, address : String){
        // The id of the channel.
        val CHANNEL_ID = "my_channel_01"
        val mBuilder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sms_24dp)
                .setContentTitle("New SMS from $address")
                .setContentText(sms)
        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(ctx, MainActivity::class.java)

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        val stackBuilder = TaskStackBuilder.create(ctx)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(1, mBuilder.build())

        closeTensorFlowAndLoadModel()

    }

    /**
     * classifies the incoming sms
     */
    private fun classifySMS(ctx :Context,  sms : String, address : String){
        val featuresSMS = FloatArray(SMSSpamClassifier.ATTRIBUTE_AMOUNT)

        for (i in BAG_OF_WORDS.indices){
            //count the amount of words in the sms
            val amountWords = StringUtils.countMatches(sms, BAG_OF_WORDS[i])
            featuresSMS[i] = amountWords.toFloat()
        }

        Log.v(TAG, "featuresSMS ${Arrays.toString(featuresSMS)}")

        //clasiffy the sms
        if(smsClassifier == null){
            Log.e(TAG, "smsClassifier is null")
        }
        if(smsClassifier != null && smsClassifier!!.isSpam(featuresSMS)) {
            Log.w(TAG, "SMS is Spam")
            checkForUrl(ctx, sms)
        }
        else {
            Log.v(TAG, "SMS is not Spam")
            closeTensorFlowAndLoadModel()
            showNotification(ctx, "You just receive a SMS", address)
        }
    }




    private fun checkForUrl(ctx : Context, sms: String) {
        /*
        Detects a
         */
        val urlPattern = Pattern.compile(
                "((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))",
                Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)

        val matcher = urlPattern.matcher(sms)
        while (matcher.find()) {
            //url found
            val match = matcher.group(1)
            Log.v(TAG, "URL in SMS $match")

            val test = FindValuesURL(ctx, receiver = this,  url = match)
            test.checkShortUrl()

        }
    }


    /**
     * this function init the classifiers
     */
    private fun initTensorFlowAndLoadModel(ctx : Context) {
        try {
            Log.d(TAG, "Loading models")
            phishingClassifier = PhishingClassifier().create(
                    assetManager = ctx.assets,
                    modelFilename = PHISHING_MODEL_FILE,
                    inputName = INPUT,
                    outputName = OUTPUT)

            smsClassifier = SMSSpamClassifier().create(
                    assetManager = ctx.assets,
                    modelFilename = SMS_MODEL_FILE,
                    inputName = INPUT,
                    outputName = OUTPUT)

            Log.d(TAG, "Load Success")
        } catch (e: Exception) {
            throw RuntimeException("Error initializing TensorFlow!", e)
        }
    }

    /**
     * Function that clases the classifier
     */
    private fun closeTensorFlowAndLoadModel() {
        executor.execute({ phishingClassifier!!.close() })
        executor.execute({ smsClassifier!!.close() })
    }


    /**
     * Function is called when the class FindValuesURL find all the features in the url
     */
    override fun siteFeatures(ctx : Context, url : String ,features: FloatArray, _id : Int) {
        Log.v(TAG, "features ${Arrays.toString(features)}")

        if(phishingClassifier!!.isPhishing(features)) {
            showNotification(ctx, "You just receive a SPAM SMS with a Phishing Site",
                    "Warning")
        }
        else
            showNotification(ctx, "You just receive a SPAM SMS",
                    "Warning")
    }
}
