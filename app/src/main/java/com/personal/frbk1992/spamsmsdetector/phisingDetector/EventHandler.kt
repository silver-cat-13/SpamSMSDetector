package com.personal.frbk1992.spamsmsdetector.phisingDetector

/**
 * interface implemented by activity where it will receive the answer of a conection
 */
interface EventHandler{
    //end the request
    fun finished(data: Any)
    //error during request
    //code variable indicate the type of request
    fun finishedWithException(ex: Exception, code: Int = -1)
}