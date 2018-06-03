package com.personal.frbk1992.spamsmsdetector.phisingDetector


import com.personal.frbk1992.spamsmsdetector.phisingDetector.ConnectionResponse.Constants.CODE_REGULAR_CONNECTION
import java.io.IOException

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URI

import java.util.concurrent.TimeUnit


// URL for the Alexa connection
private val URL_ALEXA = "http://data.alexa.com/data?cli=10&url="

/**
 * Return an URL to query an alexa search, Alexa is used to know the rank of a website globally
 * @param url: url with the domain to do the query
 * @return the Alexa URL with the query
 */
fun createURLAlexa(url : String) : String{
    return "$URL_ALEXA${URI(url).host.removePrefix("www.")}"
}

// Google index URL
private val URL_GOOGLE_INDEX = "https://www.google.com/search?q=site:"

/**
 * Return an URL to query an google search looking for an specific domain, if the domain does not
 * appear at google it means is a domain does not have register, maybe is too new or maybe Google
 * remove it for security concerns.
 * @param url: url with the domain to do the query
 * @return the Google URL with the query
 */
fun createURLGoogleIndex(url : String) : String{
    return "$URL_GOOGLE_INDEX${URI(url).host.removePrefix("www.")}"
}

/**
 * Make an connection using OkHttp library
 * @param eventHandler: EventHandler instance used to return the result or error
 * @param url: URL to make the conection
 * @param code: The code is an ID that set the type of connection, a regular connection of a site,
 * a Alexa Query or an Google search query
 */
fun getValuesFromServer(eventHandler: EventHandler, url : String, code : Int) {

    // set the OkHttpClient instance
    val client  = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()

    val request = Request.Builder()
            .url(url)
            .build()

    // make the connection
    client.newCall(request).enqueue(object : Callback {
        // error, call finishedWithException with the error
        override fun onFailure(call: Call, e: IOException) {
            call.cancel()
            eventHandler.finishedWithException(e, code)
        }

        // success, close the connection and return the result
        override fun onResponse(call: Call, response: Response) {

            //val myResponse = response.body()!!.string()
            val connResponse = ConnectionResponse(url = url,
                    code = code,
                    answer = response.body()!!.string())

            // Get the certificates, if they exist
            if(code == CODE_REGULAR_CONNECTION) {
                try {
                    connResponse.isHttps = true
                    connResponse.certificates.addAll(response.handshake().peerCertificates().toList())
                } catch (e: Exception) {
                    //there are no certificates, it was an http website
                    connResponse.isHttps = false
                    connResponse.certificates.clear()
                }
            }
            response.close()
            eventHandler.finished(connResponse)

        }
    })
}
