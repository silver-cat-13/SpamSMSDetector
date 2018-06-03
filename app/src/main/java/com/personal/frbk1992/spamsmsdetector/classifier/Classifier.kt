package com.personal.frbk1992.spamsmsdetector.classifier

/**
 * Generic interface for interacting with different recognition engines.
 */
interface Classifier {

    //function to be called when the classifier get closed
    fun close()

    //function to be called to classify an input
    fun classify(input: FloatArray): String
}
