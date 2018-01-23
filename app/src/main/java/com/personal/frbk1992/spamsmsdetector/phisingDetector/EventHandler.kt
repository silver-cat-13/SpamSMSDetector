package com.personal.frbk1992.spamsmsdetector.phisingDetector

/**
 * interface implemented by activity where it will receive the answer of a conection
 */
interface EventHandler{
    fun startedRequest()
    fun finished(data: Any)
    fun finishedWithException(ex: Exception)
    fun endedRequest()
}