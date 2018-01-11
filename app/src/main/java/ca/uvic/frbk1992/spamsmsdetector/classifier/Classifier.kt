package ca.uvic.frbk1992.spamsmsdetector.classifier

/**
 * Generic interface for interacting with different recognition engines.
 */
interface Classifier {

    /**
     * An immutable result returned by a Classifier describing what was recognized.
     *
     * id: A unique identifier for what has been recognized. Specific to the class,
     * not the instance of the object.
     *
     * title: Display name for the recognition.
     *
     * confidence: A sortable score for how good the recognition is relative to others. Higher should be better.

    data class Recognition(val id: String?, val title: String?, val confidence: Float?) */

    fun enableStatLogging(debug: Boolean)

    fun getStatString(): String

    fun close()

    fun classify(input: FloatArray): String
}
