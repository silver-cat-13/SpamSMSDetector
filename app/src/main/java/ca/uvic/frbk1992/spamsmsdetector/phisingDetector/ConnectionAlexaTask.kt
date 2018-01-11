package ca.uvic.frbk1992.spamsmsdetector.phisingDetector

import android.os.AsyncTask
import android.util.Log
import ca.uvic.frbk1992.spamsmsdetector.DEBUG
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.*
import java.security.cert.CertificateExpiredException

/**
 * Made a http connection, the class receive a the interface EventHandler and the url as
 * parameters and the timeout, the timeout is 60 seconds by default
 */
class ConnectionAlexaTask(val eventHandler: EventHandler, val urlString: String, val timeOut: Int = 60000)
    : AsyncTask<Int, Unit, ConnectionResponse>() {


    private val TAG = this.javaClass.simpleName
    private val URL_ALEXA = "http://data.alexa.com/data?cli=10&url="

    override fun onPreExecute() {
        super.onPreExecute()
        eventHandler.startedRequest()
    }

    override fun doInBackground(vararg code: Int?): ConnectionResponse {

        var urlConnection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        val connResponse = ConnectionResponse(url = urlString, code = code[0])

        try {

            val url = URL("$URL_ALEXA${URI(urlString).host.removePrefix("www.")}")

            //Log.v(TAG, "-------urlString " + connResponse.url)

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = timeOut
            urlConnection.connect()




            // Read the input stream into a String
            val inputStream = urlConnection.inputStream ?: return connResponse
            val buffer = StringBuilder()

            reader = BufferedReader(InputStreamReader(inputStream))

            var line : String? = null
            while (true) {
                line = reader?.readLine()
                if (line == null) break //end of the text
                buffer.append(line).append("\n")
                if(DEBUG) Log.v(TAG, line)
            }

            if (buffer.isEmpty()) {
                // Stream was empty.  No point in parsing.
                eventHandler.finishedWithException(NullPointerException("Respuesta es null"))
                return connResponse
            }

            //answer is saved
            connResponse.answer = buffer.toString()

        } catch (e: CertificateExpiredException) {
            Log.e(TAG, "Error CertificateExpiredException", e)
            eventHandler.finishedWithException(e)
            return connResponse
        }catch (e: SocketTimeoutException) {
            Log.e(TAG, "Error SocketTimeoutException", e)
            eventHandler.finishedWithException(e)
            return connResponse
        } catch (e: IOException) {
            Log.e(TAG, "Error IOException", e)
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


    override fun onPostExecute(result: ConnectionResponse?) {
        eventHandler.endedRequest()
        eventHandler.finished(result!!)
    }
}