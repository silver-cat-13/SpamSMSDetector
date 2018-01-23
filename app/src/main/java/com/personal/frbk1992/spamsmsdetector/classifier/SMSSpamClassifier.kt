package com.personal.frbk1992.spamsmsdetector.classifier


import android.content.ContentValues.TAG
import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import android.support.v4.os.TraceCompat
import com.personal.frbk1992.spamsmsdetector.BAG_OF_WORDS
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.regex.Pattern


/**
 * Created by frbk on 12-Nov-17.
 *
 * class that detects a sms spam classifier
 */
class SMSSpamClassifier : Classifier {


    private val TAG = this.javaClass.simpleName

    // Config values.
    private var inputName: String? = null
    private var outputName: String? = null

    // Pre-allocated buffers.
    private var outputs: FloatArray? = null
    private var outputNames: Array<String>? = null

    private var inferenceInterface: TensorFlowInferenceInterface? = null

    private var runStats = false

    private val SPAM = "P"
    private val NO_SPAM = "N"





    fun create(assetManager: AssetManager,
               modelFilename : String,
               inputName : String,
               outputName : String) : SMSSpamClassifier {

        Log.i(TAG, "Opening Spam Classifier")

        val c = SMSSpamClassifier()
        c.inputName = inputName
        c.outputName = outputName



        c.inferenceInterface = TensorFlowInferenceInterface(assetManager, modelFilename)

        // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
        val numClasses = c.inferenceInterface!!.graph().operation(outputName).output<Any>(0).shape().size(1).toInt()
        Log.i(TAG, "Output layer size is $numClasses")

        // Pre-allocate buffers.
        c.outputNames = arrayOf(outputName)
        c.outputs = FloatArray(numClasses)
        Log.i(TAG, "SMS Classifier complete")
        return c


    }

    /**
     * Classify the input
     */
    override fun classify(input: FloatArray): String {

        if(input.size % ATTRIBUTE_AMOUNT != 0){
            throw RuntimeException("Atribute lenght is wrong, ${input.size} " +
                    "must be divisible by $ATTRIBUTE_AMOUNT")
        }

        // Log this method so that it can be analyzed with systrace.
        TraceCompat.beginSection("recognizeImage")

        // Copy the input data into TensorFlow.
        TraceCompat.beginSection("feed")
        inferenceInterface!!.feed(inputName, input, input.size.toLong())
        TraceCompat.endSection()

        // Run the inference call.
        TraceCompat.beginSection("run")
        inferenceInterface!!.run(outputNames, runStats)
        TraceCompat.endSection()

        // Copy the output Tensor back into the output array
        TraceCompat.beginSection("fetch")
        inferenceInterface!!.fetch(outputName, outputs)
        TraceCompat.endSection()

        Log.i(TAG, "Result of the classifier ${Arrays.toString(outputs)}")

        var spamResult = NO_SPAM

        if(outputs!![0] < outputs!![1]){
            spamResult = SPAM
        }

        TraceCompat.endSection() // recognize page

        return spamResult
    }


    /**
     * Classify several inputs, the size of the input must be the amount of features * the amount
     * of samples
     *
     * i.e. val input = FloatArrayOf(N * M)
     * this is an input with N attributes and M samples
     *
     * the output will be an array of size M * 2, for each sample will generate
     * the output
     */
    fun classifySeveralSMS(input: FloatArray): FloatArray {


        //var fb = FloatBuffer.allocate(2)

        if(input.size % ATTRIBUTE_AMOUNT != 0){
            throw RuntimeException("Atribute lenght is wrong, ${input.size} " +
                    "must be divisible by $ATTRIBUTE_AMOUNT")
        }

        val fa = FloatArray((input.size / ATTRIBUTE_AMOUNT) * 2)

        // Log this method so that it can be analyzed with systrace.
        TraceCompat.beginSection("recognizeImage")

        // Copy the input data into TensorFlow.
        TraceCompat.beginSection("feed")
        inferenceInterface!!.feed(inputName, input, input.size.toLong())
        TraceCompat.endSection()

        // Run the inference call.
        TraceCompat.beginSection("run")
        inferenceInterface!!.run(outputNames, runStats)
        TraceCompat.endSection()

        // Copy the output Tensor back into the output array
        TraceCompat.beginSection("fetch")
        inferenceInterface!!.fetch(outputName, fa)
        TraceCompat.endSection()

        Log.i(TAG, "Result of the classifier ${Arrays.toString(fa)}")

        TraceCompat.endSection() // recognize page

        return fa
    }

    /**
     * Static methods
     */
    companion object {

        const val ATTRIBUTE_AMOUNT = 141

        /**
         * This function get the features for the Spam Classifier, the function uses the Bag of Word
         * to get the features.
         * @param content: is the content of the sms
         * @return return a FloatArray of the value of each attribute
         */
        fun getFeaturesForSpamClassifier(content : String) : FloatArray{
            //get the features for the spam classifier
            val featuresSMS = FloatArray(ATTRIBUTE_AMOUNT)
            for (i in BAG_OF_WORDS.indices){
                //count the amount of words in the sms
                val amountWords = StringUtils.countMatches(content, BAG_OF_WORDS[i])
                featuresSMS[i] = amountWords.toFloat()
            }
            return featuresSMS
        }


        /**
         * This function get an URL from the SMS using Regex, if the SMS contains more than one
         * URL, the function will only return the first one
         * @param content: the content of the SMS
         * @return the URL of the SMS, if there are no URL returns an empty string
         */
        fun getUrlFromSMS(content: String) : String{
            /*
            Detects a
             */
            val urlPattern = Pattern.compile(
                    "((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))",
                    Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)

            val matcher = urlPattern.matcher(content)
            while (matcher.find()) {
                //url found
                val match = matcher.group(1)
                Log.v(TAG, "URL in SMS $match")
                //if url does not start with http:// nor https:// add http:// by default
                if(!match.startsWith("http://") && !match.startsWith("https://"))
                    return "http://" + match
                return match
            }
            return ""
        }
    }



    /**
     * get the statString from the inference
     */
    override fun getStatString(): String = inferenceInterface!!.statString

    /**
     * Activate or deactivate the inference in debug mode
     */
    override fun enableStatLogging(debug: Boolean){runStats = debug}

    /**
     * close the TensorFlowInferenceInterface
     */
    override fun close() = inferenceInterface!!.close()

    /**
     * check if the answer from classifier is spam given the input
     */
    fun isSpam(input: FloatArray) : Boolean = classify(input) == SPAM

}