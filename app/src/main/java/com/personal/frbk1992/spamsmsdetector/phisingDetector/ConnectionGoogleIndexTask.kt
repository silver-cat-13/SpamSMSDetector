package com.personal.frbk1992.spamsmsdetector.phisingDetector

import android.os.AsyncTask
import android.util.Log
import com.personal.frbk1992.spamsmsdetector.DEBUG
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.*
import java.security.cert.CertificateExpiredException
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Made a http connection, the class receive a the interface EventHandler and the url as
 * parameters and the timeout, the timeout is 60 seconds by default
 */
class ConnectionGoogleIndexTask(val eventHandler: EventHandler, val urlString: String, val timeOut: Int = 60000)
    : AsyncTask<Int, Unit, ConnectionResponse>() {


    private val TAG = this.javaClass.simpleName

    private val urlGoogleIndex = "https://www.google.com/search?q=site:"

    override fun onPreExecute() {
        super.onPreExecute()
        eventHandler.startedRequest()
    }

    override fun doInBackground(vararg code: Int?): ConnectionResponse {

        var urlConnection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        val connResponse = ConnectionResponse(url = urlString, code = code[0], isHttps = true)

        try {
            val urlS = "$urlGoogleIndex${URI(urlString).host.removePrefix("www.")}"
            Log.v(TAG, "-------urlS " + urlS)
            val url = URL(urlS)

            Log.v(TAG, "-------urlString " + urlString)

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.connectTimeout = timeOut
            urlConnection.connect()


            // Read the input stream into a String
            val inputStream = urlConnection.inputStream ?: return connResponse
            val buffer = StringBuilder()

            reader = BufferedReader(InputStreamReader(inputStream))

            var line: String? = ""
            while (true) {
                line = reader.readLine()
                if (line == null) break //end of the text
                if(DEBUG) Log.v(TAG, "$line")
                buffer.append(line).append("\n")
            }

            if (buffer.isEmpty()) {
                // Stream was empty.  No point in parsing.
                eventHandler.finishedWithException(NullPointerException("Respuesta es null"))
                return connResponse
            }

            connResponse.answer = buffer.toString()

        } catch (e: CertificateExpiredException) {
            Log.e(TAG, "Error CertificateExpiredException", e)
            eventHandler.finishedWithException(e)
            return connResponse
        }catch (e: SSLPeerUnverifiedException) {
            Log.e(TAG, "Error SSLPeerUnverifiedException", e)
            eventHandler.finishedWithException(e)
            return connResponse
        }catch (e: SocketTimeoutException) {
            Log.e(TAG, "Error SocketTimeoutException", e)
            eventHandler.finishedWithException(e)
            return connResponse
        } catch (e: IOException) {
            Log.e(TAG, "Error IOException ${e.cause}")
            //check if the certofocate is old
            if(e.cause.toString().contains("CertificateException"))
                eventHandler.finishedWithException(CertificateExpiredException())
            eventHandler.finishedWithException(e)
            return connResponse
        }catch (e: MalformedURLException) {
            Log.e(TAG, "Error MalformedURLException", e)
            eventHandler.finishedWithException(e)
            return connResponse
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect()
            }
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error closing stream", e)
                    eventHandler.finishedWithException(e)
                    return connResponse
                }

            }
        }

        return connResponse
    }


    override fun onPostExecute(result: ConnectionResponse) {
        eventHandler.endedRequest()
        eventHandler.finished(result)
    }





}