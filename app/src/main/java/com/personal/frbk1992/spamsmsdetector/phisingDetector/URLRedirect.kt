package com.personal.frbk1992.spamsmsdetector.phisingDetector

import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.net.*

/**
 * Made a http connection, the class receive a the interface EventHandler and the url as
 * parameters and the timeout, the timeout is 60 seconds by default
 */
class URLRedirect(val eventHandler: EventHandler, val urlString: String, val timeOut: Int = 30000)
    : AsyncTask<Int, Unit, ConnectionResponse>() {


    private val TAG = this.javaClass.simpleName

    override fun onPreExecute() {
        super.onPreExecute()
        eventHandler.startedRequest()
    }

    override fun doInBackground(vararg code: Int?): ConnectionResponse {

        var urlConnection: HttpURLConnection? = null
        val expandedURL : String?
        val connResponse = ConnectionResponse(url = urlString, code = code[0])

        try {

            val url = URL(urlString)


            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = url.openConnection() as HttpURLConnection
            // stop following browser redirect
            urlConnection.instanceFollowRedirects = false

            // extract location header containing the actual destination URL
            connResponse.answer = urlConnection.getHeaderField("Location")
            urlConnection.disconnect()

            Log.v(TAG, "expandedURL $connResponse.answer")

        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Error ", e)
            eventHandler.finishedWithException(e)
            return connResponse
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


    override fun onPostExecute(result: ConnectionResponse?) {
        eventHandler.endedRequest()
        eventHandler.finished(result!!)
    }
}