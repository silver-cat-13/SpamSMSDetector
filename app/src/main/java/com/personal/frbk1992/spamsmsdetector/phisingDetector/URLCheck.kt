package com.personal.frbk1992.spamsmsdetector.phisingDetector

import java.util.regex.Pattern
import com.personal.frbk1992.spamsmsdetector.LEGITIMATE
import com.personal.frbk1992.spamsmsdetector.PHISHING
import com.personal.frbk1992.spamsmsdetector.SUSPICIOUS
import java.net.URI
import java.net.MalformedURLException
import java.net.URL


/**
 * class that given an URL check
 */
class URLCheck(val url : String){

    private val TAG = this.javaClass.simpleName

    //check if the url is an ip address v4 or v6 or not
    //If The Domain Part has an IP Address → Phishing -1
    // Otherwise→ Legitimate 1
    fun havingIpAddress() : Int{
        //check if the url is an ip address ipv4 using regex
        val isIPv4Decimal = Pattern.compile("(http[s]?:\\/\\/)?(:/d+)?((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9]).*")
        if(isIPv4Decimal.matcher(url).find()) return PHISHING
        //check if url is ip address ipv4 in hex
        val isIPv4Hex = Pattern.compile("(http[s]?:\\/\\/)?(:/d+)?(0[xX]([0-9][0-9]|[a-fA-F][0-9]|[0-9][a-fA-F]|[a-fA-F][a-fA-F])\\.){3}(0[xX]([0-9][0-9]|[a-fA-F][0-9]|[0-9][a-fA-F]|[a-fA-F][a-fA-F])).*")
        if(isIPv4Hex.matcher(url).find()) return PHISHING
        //check if url is ip address ipv4 in hex
        val isIPv6 = Pattern.compile("(http[s]?:\\/\\/)?(:/d+)?(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])).*")
        if(isIPv6.matcher(url).find()) return PHISHING
        return 1
    }


    //Long URL to Hide the Suspicious Part
    //URL length<54 → feature=Legitimate -1
    // else if URL length≥54 and ≤75 → feature=Suspicious 0
    // otherwise→ feature=Phishing) 1
    fun urlLenght() : Int{
        return when {
            url.length < 44 -> LEGITIMATE
            url.length in 44..75 -> SUSPICIOUS
            else -> PHISHING
        }
    }

    //check if the url is a shortened url by comparing with a list of
    fun shortiningService() : Int{
        val shortUrls = arrayOf("goo.gl/", "bit.ly/", "tinyurl.com/", "lc.chat/", "is.gd/"
                , "soo.gd/", "s2r.co/", "bc.vc/", "x.co/")

        return if (shortUrls.any { url.contains(it, false) }) PHISHING else LEGITIMATE
    }

    // having “@” Symbol
    // Url Having @ Symbol→ Phishing
    // Otherwise→ Legitimate
    fun havingAtSymbol() : Int{
        return when {
            url.contains("@") -> PHISHING
            else -> LEGITIMATE
        }
    }

    // ThePosition of the Last Occurrence of "//" " in the URL > 7→ Phishin
    //Otherwise→ Legitimate
    fun doubleSlashRedirecting() : Int{
        return when {
            url.indexOf("//",  7) >= 0 -> PHISHING
            else -> LEGITIMATE
        }
    }

    // ThePosition of the Last Occurrence of "//" " in the URL > 7→ Phishin
    //Otherwise→ Legitimate
    fun prefixSuffix() : Int{
        val domain = getDomain()
        return when {
            domain.contains("-") -> PHISHING
            else -> LEGITIMATE
        }
    }

    // Dots In Domain Part=1 → Legitimate
    // Dots In Domain Part=2 → Suspicious
    // Otherwise→ Phishing)
    fun havingSubDomain() : Int{
        val subDomainAmount = getDomain().split(".").size
        return when {
            subDomainAmount > 2 -> PHISHING
            subDomainAmount == 2 -> SUSPICIOUS
            else -> LEGITIMATE
        }
    }

    //check the port number
    //if the port number is 80 or 443 is legitimate, if the port number is -1 is legitimate too
    fun port() :Int{
        val port = getPort()
        return if(port == -1 || port == 80 || port == 443) LEGITIMATE
        else PHISHING
    }

    //check if the website contains https
    fun checkDomainContainsHTTPS() : Int {
        return when {
            url.startsWith("https", false) -> LEGITIMATE
            else -> PHISHING
        }
    }

    //check if the website contains https
    fun httpsToken() : Int {
        return when {
            getDomain().contains("https", false) -> PHISHING
            else -> LEGITIMATE
        }
    }


    //get domain of the url
    fun getDomain() : String{
        if(havingIpAddress() == PHISHING)
            return url.removePrefix("www.").removePrefix("http.//")
        return URI(url).host.removePrefix("www.")
    }

    //get port
    fun getPort() : Int = URI(url).port


    //check url
    fun checkURL(urlTest : String = url) : Boolean{
        return try{
            URL(urlTest)
            true
        }catch (e : MalformedURLException){
            false
        }
    }

    companion object {
        //check url
        fun isURLValid(urlTest : String) : Boolean{
            return try{
                URL(urlTest)
                true
            }catch (e : MalformedURLException){
                false
            }
        }
    }
}