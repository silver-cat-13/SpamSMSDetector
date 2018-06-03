package com.personal.frbk1992.spamsmsdetector.phisingDetector

import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.net.*

/**
 * Make an connection and return a possible redirection, for example: the domain gooogle.com
 * redirect to the google.com domain, this can be used to get the URL long version of an shorten URL
 * or a malicious website can have several redirection to hide itself
 * @param eventHandler: EventHandler instance used to return the result or error
 * @param urlString: URL to try to find the redirection
 */
class URLRedirect(val eventHandler: EventHandler, val urlString: String)
    : AsyncTask<Int, Unit, ConnectionResponse>() {


    // TAG for the logs
    private val TAG = this.javaClass.simpleName


    // perform the async task
    override fun doInBackground(vararg code: Int?): ConnectionResponse {

        // start the HttpURLConnection instance
        var urlConnection: HttpURLConnection? = null
        val connResponse = ConnectionResponse(url = urlString, code = code[0])

        try {

            val url = URL(urlString)

            // Create the request to the URL, and open the connection
            urlConnection = url.openConnection() as HttpURLConnection
            // stop following browser redirect
            urlConnection.instanceFollowRedirects = false

            // extract location header containing the actual destination URL
            connResponse.answer = urlConnection.getHeaderField("Location")
            urlConnection.disconnect()

            Log.v(TAG, "expandedURL $connResponse.answer")

        } catch (e: IOException) {
            Log.e(TAG, "Error ", e)
            eventHandler.finishedWithException(e)
            return connResponse
        }catch (e: MalformedURLException) {
            Log.e(TAG, "Error ", e)
            eventHandler.finishedWithException(e)
            return connResponse
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect()
            }
        }

        return connResponse
    }

    // return the value
    override fun onPostExecute(result: ConnectionResponse?) {
        eventHandler.finished(result!!)
    }
}