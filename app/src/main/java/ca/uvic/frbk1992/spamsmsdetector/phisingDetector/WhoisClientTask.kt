package ca.uvic.frbk1992.spamsmsdetector.phisingDetector

import android.os.AsyncTask
import android.util.Log

import java.io.IOException
import org.apache.commons.net.whois.WhoisClient
import java.net.*
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * Made a http connection, the class receive a the interface EventHandler and the url as
 * parameters and the timeout, the timeout is 60 seconds by default
 */
class WhoisClientTask(val eventHandler: EventHandler, val urlString: String, val timeOut: Int = 60000)
    : AsyncTask<Int, Unit, ConnectionResponse>() {


    private val TAG = this.javaClass.simpleName

    private var pattern: Pattern? = null
    private var matcher: Matcher? = null

    // regex whois parser
    private val WHOIS_SERVER_PATTERN = "Whois Server:\\s(.*)"

    override fun onPreExecute() {
        super.onPreExecute()
        eventHandler.startedRequest()
    }

    override fun doInBackground(vararg code: Int?): ConnectionResponse {

        val result = StringBuilder("")

        val whois = WhoisClient()
        val connResponse = ConnectionResponse(url = urlString, code = code[0])

        try {

            whois.connect(WhoisClient.DEFAULT_HOST)
            Log.v(TAG, "query = ${URI(connResponse.url).host.removePrefix("www.")}")
            val whoisData1 = whois.query("= ${URI(connResponse.url).host.removePrefix("www.")}")

            // append first result
            result.append(whoisData1)
            whois.disconnect()

            val whoisServerUrl = getWhoisServer(whoisData1)
            if (whoisServerUrl != "") {

                val whoisData2 = queryWithWhoisServer(URI(connResponse.url).host, whoisServerUrl)

                // append 2nd result
                result.append(whoisData2)
            }

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


    override fun onPostExecute(result: ConnectionResponse?) {
        eventHandler.endedRequest()
        eventHandler.finished(result!!)
    }

    @Throws(SocketException::class, IOException::class)
    private fun queryWithWhoisServer(domainName: String, whoisServer: String): String {

        var result = ""
        val whois = WhoisClient()
        whois.connect(whoisServer)
        result = whois.query(domainName)
        whois.disconnect()

        return result

    }

    private fun getWhoisServer(whois: String): String {

        pattern = Pattern.compile(WHOIS_SERVER_PATTERN);

        var result = ""

        matcher = pattern!!.matcher(whois)

        // get last whois server
        while (matcher!!.find()) {
            result = matcher!!.group(1)
        }
        return result
    }




}