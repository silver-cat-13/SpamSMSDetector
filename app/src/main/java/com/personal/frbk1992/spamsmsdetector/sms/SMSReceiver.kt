package com.personal.frbk1992.spamsmsdetector.sms

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context

import android.telephony.SmsMessage
import android.util.Log
import android.content.Intent

import android.support.v4.app.NotificationCompat
import com.personal.frbk1992.spamsmsdetector.R
import com.personal.frbk1992.spamsmsdetector.*
import com.personal.frbk1992.spamsmsdetector.classifier.PhishingClassifier
import com.personal.frbk1992.spamsmsdetector.classifier.SMSSpamClassifier
import com.personal.frbk1992.spamsmsdetector.main.MainActivity
import com.personal.frbk1992.spamsmsdetector.phisingDetector.FindValuesURL
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.apache.commons.lang3.StringUtils
import android.os.Build


/**
 * The broadcast receiver is called when there is an incoming SMS, it checks if the SMS is spam or not
 * and if it is spam check if it contains an URL and proceeds to check if it is phishing the site or not.
 * It wanrs the user in case the SMS is spam or spam and phishing with different messages via a
 * notification
 */
class SMSReceiver : BroadcastReceiver(), FindValuesURL.OnFinishFeaturesPhishingWebsite{


    //Tag for the logs
    private val TAG = this.javaClass.simpleName

    //classifiers
    private var phishingClassifier: PhishingClassifier? = null
    private var smsSpamClassifier: SMSSpamClassifier? = null

    //values that indicate if the classifier must be start or closed
    private val INIT_CLASSIFIER = 1
    private val DESTROY_CLASSIFIER = 2

    // possible results for the notification, this values are used to change the icon in the
    //notification
    private val SPAM_SMS = 1
    private val SPAM_PHISHING_SMS = 2

    //receiving new SMS
    override fun onReceive(context: Context, intent: Intent) {

        val bundle = intent.extras
        if (bundle != null) {
            //getting the SMS
            Log.i(TAG, "SMSReceiver : Reading Bundle")

            val pdus = bundle.get("pdus") as Array<*>
            val sms = SmsMessage.createFromPdu(pdus[0] as ByteArray)

            //classify if the SMS is Spam or not
            classifySMS(context, sms.messageBody)

        }
    }

    /**
     * Function that shows the notification of the sms in case is spam or spam and phishing
     */
    private fun showNotification(ctx : Context, sms : String, title: String, result: Int){
        val mNotificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // The id of the channel.
        val channelId = "my_channel_01"

        // The user-visible name of the channel.
        val name = ctx.getString(R.string.channel_name)

        // The user-visible description of the channel.
        val description = ctx.getString(R.string.channel_description)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW)
            } else {
                null
            }
            // Configure the notification channel.
            mChannel?.description = description

            mChannel?.enableLights(true)
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel?.lightColor = ctx.getColor(R.color.colorPrimary)

            mChannel?.enableVibration(true)

            mNotificationManager.createNotificationChannel(mChannel)
        }

        //set the notification icon
        val notificationIcon = if(result == SPAM_SMS){
            //notification for spam sms
            R.drawable.ic_spam_sms_notification
        }else{
            //notfication for spam and phishing sms
            R.drawable.ic_spam_phighing_notification
        }

        //build the notification
        val mBuilder = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(notificationIcon)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(sms)
        //mBuilder.notification.flags = mBuilder.notification.flags or Notification.FLAG_AUTO_CANCEL
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

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(1, mBuilder.build())
    }

    /**
     * classifies the incoming sms
     */
    private fun classifySMS(ctx: Context, sms: String){
        val featuresSMS = FloatArray(SMSSpamClassifier.ATTRIBUTE_AMOUNT)

        for (i in BAG_OF_WORDS.indices){
            //count the amount of words in the sms and set the features in the featuresSMS array
            val amountWords = StringUtils.countMatches(sms, BAG_OF_WORDS[i])
            featuresSMS[i] = amountWords.toFloat()
        }

        //create observable that check if the features correspond to a spam sms
        val featuresObservable = Single.fromCallable{
            smsSpamClassifier = SMSSpamClassifier().create(
                    assetManager = ctx.assets,
                    modelFilename = SMS_MODEL_FILE,
                    inputName = INPUT,
                    outputName = OUTPUT)
            //output for the callable, which is a boolean
            smsSpamClassifier!!.isSpam(featuresSMS)
        }
        //set the callable in a new thread, using the main thread in the result
        featuresObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Boolean>{
                    override fun onSuccess(t: Boolean) {
                        //closing classifier
                        //the function always get a single SMS, there is no need to keep the classifier
                        //open any longer
                        Single.just(DESTROY_CLASSIFIER)
                                .subscribeOn(Schedulers.newThread())
                                .subscribe(singleSMSSPamClassifier(ctx))
                        if(t) {
                            Log.v(TAG, "User just received SPAM sms")
                            // SMS is spam, check if it has an URL
                            checkForUrl(ctx, sms)
                        }
                    }

                    override fun onError(e: Throwable) {
                        //closing classifier
                        Single.just(DESTROY_CLASSIFIER)
                                .subscribeOn(Schedulers.newThread())
                                .subscribe(singleSMSSPamClassifier(ctx))
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
                    ctx.getString(R.string.notification_message_warning_title), SPAM_SMS)
        }
    }





    /**
     * Function is called when the class FindValuesURL find all the features in the url
     */
    override fun siteFeatures(ctx : Context, url : String ,features: FloatArray, _id : Int) {
        //create observable that check if the features correspond to a phishing site
        val featuresObservable = Single.fromCallable{
            phishingClassifier = PhishingClassifier().create(
                    assetManager = ctx.assets,
                    modelFilename = PHISHING_MODEL_FILE,
                    inputName = INPUT,
                    outputName = OUTPUT)
            //callable that will return a boolean if the site is phishing or note
            phishingClassifier!!.isPhishing(features)
        }
        //check if the URL is phishing
        featuresObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Boolean>{
                    override fun onSuccess(t: Boolean) {
                        Single.just(DESTROY_CLASSIFIER)
                                .subscribeOn(Schedulers.newThread())
                                .subscribe(singlePhishingClassifier(ctx))
                        if(t) {
                            // SMS contains phishing site
                            showNotification(ctx, ctx.getString(R.string.notification_message_warning_spam_phishing_message),
                                    ctx.getString(R.string.notification_message_warning_title), SPAM_PHISHING_SMS)
                        }
                        else //SMS does not contain phishing site but it is still spam
                            showNotification(ctx, ctx.getString(R.string.notification_message_warning_spam_message),
                                    ctx.getString(R.string.notification_message_warning_title), SPAM_SMS)
                    }

                    override fun onError(e: Throwable) {
                        //closing classifier
                        Single.just(DESTROY_CLASSIFIER)
                                .subscribeOn(Schedulers.newThread())
                                .subscribe(singlePhishingClassifier(ctx))
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
