package com.personal.frbk1992.spamsmsdetector.classifier


import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import android.support.v4.os.TraceCompat
import java.util.*


/**
 * Class that creates a classifier that can be used to detect if an URL is linked to a phishing site
 * or note
 */
class PhishingClassifier : Classifier {

    //TAG for the logs
    private val TAG = this.javaClass.simpleName

    // Config values.
    private var inputName: String? = null
    private var outputName: String? = null

    // Pre-allocated buffers.
    private var outputs: FloatArray? = null
    private var outputNames: Array<String>? = null

    //Tensorflow interface
    private var inferenceInterface: TensorFlowInferenceInterface? = null

    //variable used to set the debug state the tensorflow interface
    private var runStats = false

    //results if it is pshihing or not
    private val PHISHING = "P"
    private val NO_PHISHING = "N"

    //number of features
    private val ATTRIBUTE_AMOUNT = 17


    /**
     * Create the classifier to classify if a set of features correspond to a phishing site or not
     * @param assetManager: the asset manager given by the context, it is used to get the model file
     * @param modelFilename: the name of the model used to clasify the features
     * @param inputName: name of the input parameters
     * @param outputName: name of the output parameters
     * @return the classifier
     */
    fun create(assetManager: AssetManager,
               modelFilename : String,
               inputName : String,
               outputName : String) : PhishingClassifier {

        // init the classifier
        val c = PhishingClassifier()
        // set the input and output values
        c.inputName = inputName
        c.outputName = outputName

        // set the TensorFlow interface
        c.inferenceInterface = TensorFlowInferenceInterface(assetManager, modelFilename)

        // the shape of the output is [N, NUM_CLASSES], where N is the batch size.
        // for the class of this classifier is an array of 2 possible values
        val numClasses = c.inferenceInterface!!.graph()
                .operation(outputName)
                .output<Any>(0).shape()
                .size(1).toInt()

        // pre-allocate buffers.
        c.outputNames = arrayOf(outputName)
        c.outputs = FloatArray(numClasses)

        return c


    }

    /**
     * Classify the input
     * @param input: an FloatArray with the result of each feature
     * @return a String "P" or "N" indicating if it is phishing or not
     */
    override fun classify(input: FloatArray): String {

        if(input.size % ATTRIBUTE_AMOUNT != 0){
            throw RuntimeException("Atribute lenght is wrong, ${input.size} " +
                    "must be divisible by $ATTRIBUTE_AMOUNT")
        }

        // log this method so that it can be analyzed with systrace.
        TraceCompat.beginSection("getFeaturesInput")

        // copy the input data into TensorFlow.
        TraceCompat.beginSection("feed")
        inferenceInterface!!.feed(inputName, input, input.size.toLong())
        TraceCompat.endSection()

        // run the inference call.
        TraceCompat.beginSection("run")
        inferenceInterface!!.run(outputNames, runStats)
        TraceCompat.endSection()

        // copy the output Tensor back into the output array
        TraceCompat.beginSection("fetch")
        inferenceInterface!!.fetch(outputName, outputs)
        TraceCompat.endSection()

      //  Log.i(TAG, "Result of the classifier ${Arrays.toString(outputs)}")

        var phishingResult = NO_PHISHING

        if(outputs!![0] > outputs!![1]){
            phishingResult = PHISHING
        }

        TraceCompat.endSection()

        return phishingResult
    }


    /*
     * Classify several inputs, the size of the input must be the amount of features * the amount
     * of samples
     *
     * i.e. val input = FloatArrayOf(N * M)
     * this is an input with N attributes and M samples
     *
     * the output will be an array of size M * 2, for each sample will generate
     * the output
     */
    /*fun classifySeveralSites(input: FloatArray): FloatArray {


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
    }*/



    /**
     * close the TensorFlowInferenceInterface
     */
    override fun close() = inferenceInterface!!.close()

    /**
     * check if the answer from classifier is phishing given the input
     */
    fun isPhishing(input: FloatArray) : Boolean = classify(input) == PHISHING

}