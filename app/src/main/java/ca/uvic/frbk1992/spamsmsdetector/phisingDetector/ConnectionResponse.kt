package ca.uvic.frbk1992.spamsmsdetector.phisingDetector

import java.security.cert.Certificate

/**
 * Created by frbk on 01-Nov-17. response from connecting to a url
 * The url is the URL of the website
 * answer is the response of the website, the .html code (can also be a .js)
 * code is an id setting to see what was the connection
 * isHttps gives us if the connection was https or not
 * certificates are the certificates the website is associated with
 */
class ConnectionResponse(var url : String? = "",
                         var answer : String? = "",
                         var code : Int? = -1,
                         var isHttps : Boolean? = false,
                         var ip: String = "",
                         var certificates : ArrayList<Certificate> = ArrayList<Certificate>()){

    val CONNECTION_NULL = -1


    object constants {
        val CODE_REGULAR_CONECTION = 0 //regular conection, get http

        val CODE_REDIRECT_CONECTION = 1 //conection to get the redirected website

        val CODE_WHO_IS_CONECTION = 2 //conection use for the who is conection

        val CODE_ALEXA_CONECTION = 3 //conection use for the alexa connection

        val CODE_GOOGLE_INDEX_CONECTION = 4 //conection use for the google page index
    }
}

