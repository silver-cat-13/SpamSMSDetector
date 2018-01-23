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
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
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
    private var smsSpamClassifier: SMSSpamClassifier? = null

    private val INIT_CLASSIFIER = 1
    private val DESTROY_CLASSIFIER = 2

    private val executor = Executors.newSingleThreadExecutor()

    override fun onReceive(context: Context, intent: Intent) {
        //init the classifiers
        initTensorFlowAndLoadModel(context)

        val bundle = intent.extras
        if (bundle != null) {
            Log.i(TAG, "SMSReceiver : Reading Bundle")

            val pdus = bundle.get("pdus") as Array<*>
            val sms = SmsMessage.createFromPdu(pdus[0] as ByteArray)

            //classify if the SMS is Spam or not
            classifySMS(context, sms.messageBody, sms.originatingAddress)

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

        closeTensorFlowAndLoadModel(ctx)

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



        //create observable that check if the features correspond to a phishing site
        val featuresObservable = Single.fromCallable{ smsSpamClassifier!!.isSpam(featuresSMS) }
        featuresObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Boolean>{
                    override fun onSuccess(t: Boolean) {
                        if(t) {
                            // SMS contains phishing site
                            checkForUrl(ctx, sms)
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "${e.message}")
                        throw RuntimeException("Error with TensorFlow! ${e.message}", e)
                    }

                    override fun onSubscribe(d: Disposable) {
                        //onSubscribe
                    }

                })
    }


    /**
     * this function checks if the SMS contains an URL, in case it does it check if the URL is linked
     * to a phishing site, the SMS called is already known to be Spam.
     */
    private fun checkForUrl(ctx : Context, sms: String) {
        val url = SMSSpamClassifier.getUrlFromSMS(sms)
        if(url != ""){
            //SMS has URL
            val test = FindValuesURL(ctx, receiver = this,  url = url)
            test.getFeatures()
        } else {
            //SMS has no URL
            showNotification(ctx, ctx.getString(R.string.notification_message_warning_spam_message),
                    ctx.getString(R.string.notification_message_warning_title))
        }
    }


    /**
     * this function init the classifiers
     */
    private fun initTensorFlowAndLoadModel(ctx : Context) {
        //init phishing model
        Single.just(INIT_CLASSIFIER)
                .subscribeOn(Schedulers.newThread())
                .subscribe(singlePhishingClassifier(ctx))

        //init phishing model
        Single.just(INIT_CLASSIFIER)
                .subscribeOn(Schedulers.newThread())
                .subscribe(singleSMSSPamClassifier(ctx))
    }

    /**
     * Function that clases the classifier
     */
    private fun closeTensorFlowAndLoadModel(ctx : Context) {
        Single.just(DESTROY_CLASSIFIER)
                .subscribeOn(Schedulers.newThread())
                .subscribe(singlePhishingClassifier(ctx))
        Single.just(DESTROY_CLASSIFIER)
                .subscribeOn(Schedulers.newThread())
                .subscribe(singlePhishingClassifier(ctx))
    }


    /**
     * Function is called when the class FindValuesURL find all the features in the url
     */
    override fun siteFeatures(ctx : Context, url : String ,features: FloatArray, _id : Int) {
        //create observable that check if the features correspond to a phishing site
        val featuresObservable = Single.fromCallable{ phishingClassifier!!.isPhishing(features) }
        featuresObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Boolean>{
                    override fun onSuccess(t: Boolean) {
                        if(t) {
                            // SMS contains phishing site
                            showNotification(ctx, ctx.getString(R.string.notification_message_warning_spam_phishing_message),
                                    ctx.getString(R.string.notification_message_warning_title))
                        }
                        else //SMS does not contain phishing site
                            showNotification(ctx, ctx.getString(R.string.notification_message_warning_spam_message),
                                    ctx.getString(R.string.notification_message_warning_title))
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "${e.message}")
                        throw RuntimeException("Error with TensorFlow! ${e.message}", e)
                    }

                    override fun onSubscribe(d: Disposable) {
                        //onSubscribe
                    }

                })
    }



    /**
     * This function return a single of RxJava where create/destroy the PhishingClassifier
     * depending on the integer observable
     */
    private fun singlePhishingClassifier(context: Context): SingleObserver<Int> {
        return object : SingleObserver<Int> {

            override fun onSuccess(t: Int) {
                when(t){
                    INIT_CLASSIFIER ->{
                        Log.i(TAG, "Creating model for phishing classifier")

                        phishingClassifier = PhishingClassifier().create(
                                assetManager = context.assets,
                                modelFilename = PHISHING_MODEL_FILE,
                                inputName = INPUT,
                                outputName = OUTPUT)

                    }DESTROY_CLASSIFIER ->{
                        Log.i(TAG, "Closing phishing classifier")
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
                throw RuntimeException("Error with TensorFlow!", e)
            }

            override fun onSubscribe(d: Disposable) {
                // onSubscribe
            }
        }
    }


}
