package ca.uvic.frbk1992.spamsmsdetector.classifier


import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import android.support.v4.os.TraceCompat
import java.util.*


/**
 * Created by frbk on 12-Nov-17.
 *
 * class that detects a sms spam classifier
 */
class SMSSpamClassifier : Classifier {

    companion object {
        const val ATTRIBUTE_AMOUNT = 141
    }

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