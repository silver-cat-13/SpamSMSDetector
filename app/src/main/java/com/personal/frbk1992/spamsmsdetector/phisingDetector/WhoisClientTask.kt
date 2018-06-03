package com.personal.frbk1992.spamsmsdetector.phisingDetector

import android.os.AsyncTask
import android.util.Log

import java.io.IOException
import org.apache.commons.net.whois.WhoisClient
import java.net.*



/**
 * Make an async connection to the whois.internic.net using the [org.apache.commons.net.whois.WhoisClient]
 * library.
 * @param eventHandler: EventHandler instance used to return the result
 * @param urlString: URL input
 */
class WhoisClientTask(val eventHandler: EventHandler, val urlString: String)
    : AsyncTask<Int, Unit, ConnectionResponse>() {

    // Tag for logs
    private val TAG = this.javaClass.simpleName

   // private var pattern: Pattern? = null
   // private var matcher: Matcher? = null

    // regex whois parser
   // private val WHOIS_SERVER_PATTERN = "Whois Server:\\s(.*)"


    /**
     * DO the connection
     */
    override fun doInBackground(vararg code: Int?): ConnectionResponse {

        val result = StringBuilder("")

        // init the WhoIsClient instance
        val whois = WhoisClient()
        val connResponse = ConnectionResponse(url = urlString, code = code[0])

        try {

            // set the default host whois.internic.net and default port 43
            whois.connect(WhoisClient.DEFAULT_HOST, WhoisClient.DEFAULT_PORT)
            Log.v(TAG, "query = ${URI(connResponse.url).host.removePrefix("www.")}")

            // set the query
            val whoisData1 = whois.query("=${URI(connResponse.url).host.removePrefix("www.")}")
            // append first result
            result.append(whoisData1)

            //close
            whois.disconnect()

            connResponse.answer = result.toString()


        } catch (e: IOException) {
            Log.e(TAG, "Error IOException")
            e.printStackTrace()
            eventHandler.finishedWithException(e)
            return connResponse
        }catch (e: SocketException) {
            Log.e(TAG, "Error IOException")
            e.printStackTrace()
            eventHandler.finishedWithException(e)
            return connResponse
        }

        return connResponse
    }

    /**
     * Function called after the doInBackground is done
     */
    override fun onPostExecute(result: ConnectionResponse?) {
        //return the result
        eventHandler.finished(result!!)
    }

// This was a Test
//
//    @Throws(SocketException::class, IOException::class)
//    private fun queryWithWhoisServer(domainName: String, whoisServer: String): String {
//
//        var result: String
//        val whois = WhoisClient()
//        whois.connect(whoisServer)
//        result = whois.query(domainName)
//        whois.disconnect()
//
//        return result
//
//    }
//
//    private fun getWhoisServer(whois: String): String {
//
//        pattern = Pattern.compile(WHOIS_SERVER_PATTERN)
//
//        var result = ""
//
//        matcher = pattern!!.matcher(whois)
//
//        // get last whois server
//        while (matcher!!.find()) {
//            result = matcher!!.group(1)
//        }
//        return result
//    }
//
//
}