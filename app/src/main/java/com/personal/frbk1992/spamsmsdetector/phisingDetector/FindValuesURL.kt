package com.personal.frbk1992.spamsmsdetector.phisingDetector

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import com.personal.frbk1992.spamsmsdetector.*
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Created by frbk on 31-Oct-17. find the values of the url for testing it
 */
class FindValuesURL(val ctx : Context, val fragment : Fragment? = null,
                    val receiver : BroadcastReceiver? = null,
                    var url: String, val _id : Int = 0) : EventHandler{


    init {
        deleteAllPreferences(ctx, URL_PREFERENCES)
        initListener()
    }

    private val TAG = this.javaClass.simpleName


    private var featuresUrl : String = ""
    private var valuesRedirection = 0 //amount of time the website has been redirected

    //features to calculate, default value -2
    private var havingIPAddress  = -2
    private var urlLength   = -2
    private var shortiningService = -2
    private var havingAtSymbol   = -2
    private var doubleslashredirecting = -2
    private var prefixSuffix  = -2
    private var havingSubDomain  = -2
    private var sslFinalState  = -2
    private var domainRegisterationLength = -2
    private var favicon = -2
    private var port = -2
    private var httpsToken = -2
    private var requestURL  = -2
    private var urlOfAnchor = -2
    private var linksInTags = -2
    //private var sfh  = -2 //dont inderstand
    //private var submittingtoemail = -2 //dont understand
    //private var abnormalURL = -2 // dont understand
    private var redirect  = -2
   //private var rightClick  = -2 //if theres time
   //private var popUpWidnow  = -2 //if theres time
    private var iFrame = -2
    private var ageOfDomain = -2
    private var dnsRecord   = -2
    private var webtraffic  = -2
    private var googleIndex = -2
    //private var linkspointingtopage = -2 //if theres time
    private var statisticalReport = -2



    private var mListener : OnFinishFeaturesPhishingWebsite? = null


    //init method
    private fun initListener(){
        mListener = if(receiver == null && fragment == null) {
            if (ctx is OnFinishFeaturesPhishingWebsite) {
                ctx
            } else {
                throw RuntimeException(ctx.toString() + " must implement OnFinishFeaturesPhishingWebsite")
            }
        }else if(fragment != null){
            if (fragment is OnFinishFeaturesPhishingWebsite) {
                fragment
            } else {
                throw RuntimeException(receiver.toString() + " must implement OnFinishFeaturesPhishingWebsite")
            }
        }else{
            if (receiver is OnFinishFeaturesPhishingWebsite) {
                receiver
            } else {
                throw RuntimeException(receiver.toString() + " must implement OnFinishFeaturesPhishingWebsite")
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    //start the request
    override fun startedRequest() {
        Log.v(TAG, "startedRequest")
    }

    //request finish
    override fun endedRequest() {
        Log.v(TAG, "endedRequest")


    }

    //got some data
    override fun finished(data: Any) {
        val cr = data as ConnectionResponse
        if(DEBUG) Log.v(TAG, "answer ${cr.answer}")

        //check the code
        when(cr.code){
            ConnectionResponse.constants.CODE_REGULAR_CONECTION ->{
                //it was a regular conection, got the html file and the data

                //check if it was a HTTPS connection, if it was check certificates
                if(cr.isHttps!!)
                    checkCertificates(cr.certificates)

                //parser the html code
                parsetHTML(cr)

                //check phishing site
                checkBlackListPhishtank(cr)
            }
            ConnectionResponse.constants.CODE_WHO_IS_CONECTION ->{
                //connection was using who is
                parserWhoIsAnswer(cr)

            }
            ConnectionResponse.constants.CODE_ALEXA_CONECTION ->{
                //connection was using alexa
                parserAlexaAnswer(cr)

            }
            ConnectionResponse.constants.CODE_GOOGLE_INDEX_CONECTION ->{
                //connection was using alexa
                parserPageIndexAnswer(cr)

            }
            ConnectionResponse.constants.CODE_REDIRECT_CONECTION ->{

                //conection was a redirection, check if the answer is empty or null
                if(cr.answer == null || cr.answer!! == "" || !URLCheck(cr.answer!!).checkURL()){
                    //answer is empty or null, there was no redirection
                    //check the features in the url in the answer
                    Log.v(TAG, "no more redirections")
                    this.url = cr.url!!
                    checkURLFeatures()
                    return
                }

                Log.v(TAG, "answer ${URLCheck(cr.answer!!).getDomain()}")
                Log.v(TAG, "url ${URLCheck(cr.url!!).getDomain()}")


                val urlSplit = URLCheck(cr.url!!).getDomain().split(".")[0]
                val answerSplit = URLCheck(cr.answer!!).getDomain().split(".")[0]
                if(answerSplit == urlSplit){
                    Log.v(TAG, "no more redirections")
                    //there was no more redirection, make a regular conection and check
                    //check the features in the url in the answer
                    this.url = cr.url!!
                    checkURLFeatures()
                    return
                }

                //increase the amount of redirection by one
                valuesRedirection++

                Log.v(TAG, "redirection")
                //the conection was a redirection, try to find is the new site is a redirection too
                val urlRedirect = URLRedirect(this, cr.answer!!)
                urlRedirect.execute(ConnectionResponse.constants.CODE_REDIRECT_CONECTION)
            }
        }
    }


    //error with the request
    override fun finishedWithException(ex: Exception) {
        Log.v(TAG, "finishedWithException")
        Log.v(TAG, ex.toString())
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////


    fun connectToServer(){
        val ct = ConnectionHTTPTask(this, url)
        ct.execute(ConnectionResponse.constants.CODE_REGULAR_CONECTION)
    }


    //function that retrieve a list of all js files the lib contains
    //except for jquery, bootstrap and any min.js
    private fun getJSFiles(data : String) : ArrayList<String>{
        val jsFiles = ArrayList<String>()
        val domain = getDomain()
        var match : String
        val matcher = Pattern.compile("(src=\"([^\"]+.js)\")").matcher(data)
        Log.v(TAG, "domain $domain")
        while (matcher.find()) {
            match = matcher.group(2)
            if(!checkURL(match) && !match.contains("jquery", false)
                    && !match.contains("bootstrap", false)
                    && !match.contains(".min.", false))
                if(url.endsWith("/")) jsFiles.add(url)
                else{

                }
        }
        return jsFiles
    }


    //get domain of the url
    private fun getDomain() : String = URI(url).host.removePrefix("www.")

    /**
     * This function check if the String is a valid URL or not
     */
    private fun checkURL(urlTest : String) : Boolean{
        return try{
            URL(urlTest)
            true
        }catch (e : MalformedURLException){
            false
        }
    }


    /**
     * This function start the process of getting all the features to check if the URL is
     * phishing or not
     */
    fun getFeatures(){
        checkShortUrl()
    }


    /**
     * This function check if the function is a short url, in case it's an short url
     * the method will find the long version using a connection with URLRedirect
     * in case it's not an short url the method will call getFeaturesString() to finish
     * to check the URL for the rest of the features
     */
    fun checkShortUrl(){
        //check if the url is a shorten version
        val urlCheck = URLCheck(url)
        shortiningService = urlCheck.shortiningService()
        savePrefence(ctx, URL_PREFERENCES, SHORTENING_SERVICE, shortiningService)
        Log.v(TAG, "shortiningService $shortiningService")

        //make a conection using URLRedirect to find the final url
        val urlRedirect = URLRedirect(this, url)
        urlRedirect.execute(ConnectionResponse.constants.CODE_REDIRECT_CONECTION)
    }


    /**
     * This function check the basic information in the URL as a string, except for if it is an
     * tiny url
     * at the end the function calls a connection to retrieve the html file
     */
    private fun checkURLFeatures(){

        //check the amount of times a Redirection occur
        if(valuesRedirection >= 2 ){
            //redirection hight phisy
            Log.v(TAG, "redirection high phishing")
            redirect = PHISHING
            savePrefence(ctx, URL_PREFERENCES, REDIRECT, redirect)
        }else if(valuesRedirection == 1){
            //redirection not that high suspicious
            Log.v(TAG, "redirection suspicious")
            redirect = SUSPICIOUS
            savePrefence(ctx, URL_PREFERENCES, REDIRECT, redirect)
        }else{
            //redirection low legitimate
            Log.v(TAG, "redirection low, legitimate")
            redirect = LEGITIMATE
            savePrefence(ctx, URL_PREFERENCES, REDIRECT, redirect)
        }

        val urlCheck = URLCheck(url)

        //add the value if the url is an ip addrress
        havingIPAddress = urlCheck.havingIpAddress()
        savePrefence(ctx, URL_PREFERENCES, HAVING_IP_ADDRRESS, havingIPAddress)

        //add the value about the lenght of the url
        urlLength = urlCheck.urlLenght()
        savePrefence(ctx, URL_PREFERENCES, URL_LENGHT, urlLength)

        //check if it has @ symbol
        havingAtSymbol = urlCheck.havingAtSymbol()
        savePrefence(ctx, URL_PREFERENCES, HAVING_AT_SYMBOL, havingAtSymbol)

        //check if it has // redirection
        doubleslashredirecting = urlCheck.doubleSlashRedirecting()
        savePrefence(ctx, URL_PREFERENCES, DOUBLE_SLASH_REDIRECTING, doubleslashredirecting)

        //check if it has // redirection
        prefixSuffix = urlCheck.prefixSuffix()
        savePrefence(ctx, URL_PREFERENCES, PREFIX_SUFFIX, prefixSuffix)

        //check the subdomains
        havingSubDomain = urlCheck.havingSubDomain()
        savePrefence(ctx, URL_PREFERENCES, HAVING_SUB_DOMAIN, havingSubDomain)

        //check the port number
        port = urlCheck.port()
        savePrefence(ctx, URL_PREFERENCES, PORT, port)

        //check if the domain contains HTTPS
        httpsToken = urlCheck.httpsToken()
        savePrefence(ctx, URL_PREFERENCES, HTTPS_TOKEN, httpsToken)

        //check the ssl state
        when(urlCheck.checkDomainContainsHTTPS()){
            PHISHING -> {
                sslFinalState = PHISHING
                Log.v(TAG, "checkDomainContainsHTTPS phishing")
                savePrefence(ctx, URL_PREFERENCES, SSL_FINAL_STATE, sslFinalState)
                val ct = ConnectionHTTPTask(this, url)
                ct.execute(ConnectionResponse.constants.CODE_REGULAR_CONECTION)
            }
            else -> {
                Log.v(TAG, "checkDomainContainsHTTPS not phishing")
                val ct = ConnectionHTTPSTask(this, url)
                ct.execute(ConnectionResponse.constants.CODE_REGULAR_CONECTION)
            }
        }


        //make a who is connection
        val whoIs = WhoisClientTask(this, url)
        whoIs.execute(ConnectionResponse.constants.CODE_WHO_IS_CONECTION)

        //make a alexa connection
        val alexa = ConnectionAlexaTask(this, url)
        alexa.execute(ConnectionResponse.constants.CODE_ALEXA_CONECTION)

        val googleIndex = ConnectionGoogleIndexTask(this, url)
        googleIndex.execute(ConnectionResponse.constants.CODE_GOOGLE_INDEX_CONECTION)

        //check features until all of then are different than -2
        checkFeatures()
    }

    /**
     * function that checks if the certificate is from an trust CA and if the experiation date
     * is one year or more
     * this function modify the sslFinalState variable
     * sslFinalState it will be 1 if the CA is trust and if the expiration date is more than a year
     * it will be suspicious otherwise
     */
    private fun checkCertificates(certs : ArrayList<Certificate>){
        //most common CA
        val listCerts = arrayOf("Comodo", "IdenTrust", "Symantec", "GoDaddy", "GlobalSign",
                "DigiCert", "Certum", "Entrust", "Secom Trust", "Actalis",
                "Trustwave", "StartCom", "Let’s Encrypt", "Google Internet Authority G2")

        var trustCA = false
        var expiration = false

        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, 1)
        val date = cal.time

        for (cert in certs){
            if (cert is X509Certificate) {
                listCerts.asSequence().filter {
                            //the certificate is from a trust CA
                            cert.issuerDN.toString().contains(it, false)
                        }.forEach { trustCA = true }

                if (date <= cert.notAfter) {
                    //the experiation date is good
                    expiration = true
                }
            }
        }

        if(trustCA && expiration){
            Log.v(TAG, "Trust CA and trust date")
            sslFinalState = LEGITIMATE
            savePrefence(ctx, URL_PREFERENCES, SSL_FINAL_STATE, sslFinalState)
        }else{
            Log.v(TAG, "The CA is suspicious or the date")
            sslFinalState = SUSPICIOUS
            savePrefence(ctx, URL_PREFERENCES, SSL_FINAL_STATE, sslFinalState)
        }


    }

    /**
     * This function parser the HTML file in the answer
     * @param cr the ConectionResponse variable given by the connection
     */
    private fun parsetHTML(cr: ConnectionResponse) {
        //test if the page have a favicon on the same domain
        checkFavicon(cr)

        //check the amount of srs="" the html has outside the domain
        checkRequestUrl(cr)

        //check the amount of anchors the html has outside the domain
        checkAnchors(cr)

        //check the amount of <Link> <Script> the html has outside the domain
        checktLinkScript(cr)

        //check iFrame
        checkIFrame(cr)
    }



    /**
     * Function that get the favicon and check if the it comes from the same domain
     * if it does not have favicon will be consider phishing
     */
    private fun checkFavicon(cr: ConnectionResponse?) {

        if(cr?.answer == ""){
            Log.v(TAG, "Data is empty")
            favicon = PHISHING
            savePrefence(ctx, URL_PREFERENCES, FAVICON, favicon)
            return
        }

        //look for the favicon file in the html file
        val pattern = Pattern.compile("href=\"(http.+/favicon\\.ico)\"")
        val matcher = pattern.matcher(cr!!.answer)
        var favicoLink = ""
        while (matcher.find()) {
            favicoLink = matcher.group(1)
        }

        if(favicoLink == ""){
            Log.v(TAG, "no favicon found")
            favicon = PHISHING
            savePrefence(ctx, URL_PREFERENCES, FAVICON, favicon)
            return
        }

        val favIcoHost = URI(favicoLink).host.removePrefix("www.")
        val domain = URI(cr.url).host.removePrefix("www.")
        if(favIcoHost == domain){
            Log.v(TAG, "domain equal favicon")
            favicon = LEGITIMATE
            savePrefence(ctx, URL_PREFERENCES, FAVICON, favicon)
        }else{
            Log.v(TAG, "domain no equal favicon")
            favicon = PHISHING
            savePrefence(ctx, URL_PREFERENCES, FAVICON, favicon)
        }
    }


    /**
     * this function parser the answer for the who is data base
     */
    private fun parserWhoIsAnswer(cr: ConnectionResponse) {
        //check the expiration date
        checkExpirationDateDNS(cr)

        //check the expiration date
        checkCreationDateDNS(cr)
    }

    /**
     * this function parser the answer for the alexa response
     */
    private fun parserAlexaAnswer(cr: ConnectionResponse) {
        //check the rank of the site
        checkRankSite(cr)
    }

    /**
     * this function parser the answer for the page index
     */
    private fun parserPageIndexAnswer(cr: ConnectionResponse) {
        //check the rank of the site
        checkPageIndex(cr)
    }



    /**
     * This function parser the WhoIs answer to find the experation date of the dns
     */
    private fun checkExpirationDateDNS(cr : ConnectionResponse){
        val exprationDateString = "Registry Expiry Date:"

        if(cr.answer!!.contains(exprationDateString)){
            //find out if the experation date is 6 months or older
            //get the current date + 6 months
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, 18)
            val date = cal.time

            val pattern = Pattern.compile("Registry Expiry Date: (.*)T\\d")
            val matcher = pattern.matcher(cr.answer!!)
            var exprationDateString  = ""
            while (matcher.find()) {
                exprationDateString = matcher.group(1)
            }
            Log.v(TAG, "exprationDateString $exprationDateString")

            val formatter = SimpleDateFormat("yyyy-MM-dd")
            val expDate = formatter.parse(exprationDateString);

            if (date <= expDate) {
                //the experiation date is good
                Log.v(TAG, "the experiation DNS date is good")
                domainRegisterationLength = LEGITIMATE
                savePrefence(ctx, URL_PREFERENCES, DOMAIN_REGISTRATION_LENGHT, domainRegisterationLength)
            }else{
                Log.v(TAG, "the experiation DNS date is NOT good")
                domainRegisterationLength = PHISHING
                savePrefence(ctx, URL_PREFERENCES, DOMAIN_REGISTRATION_LENGHT, domainRegisterationLength)
            }


        }else{
            //does not have the string exprationDateString, the site was not found on the
            //who is data base, phising
            Log.v(TAG, "url was not found on WhoIs, consider it phishing")
            domainRegisterationLength = PHISHING
            savePrefence(ctx, URL_PREFERENCES, DOMAIN_REGISTRATION_LENGHT, domainRegisterationLength)
        }
    }

    /**
     * This function parser the WhoIs answer to find the creation date of the dns
     */
    private fun checkCreationDateDNS(cr : ConnectionResponse){
        val creationDateString = "Creation Date:"

        if(cr.answer!!.contains(creationDateString)){
            //find out if the experation date is 6 months or older
            //get the current date + 6 months
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -6)
            val date = cal.time

            val pattern = Pattern.compile("Creation Date: (.*)T\\d")
            val matcher = pattern.matcher(cr.answer!!)
            var creationDateString  = ""
            while (matcher.find()) {
                creationDateString = matcher.group(1)
            }
            Log.v(TAG, "creationDateString $creationDateString")

            val formatter = SimpleDateFormat("yyyy-MM-dd")
            val creationDate = formatter.parse(creationDateString);

            if (date >= creationDate) {
                //the creation date is good
                Log.v(TAG, "the creation DNS date is good")
                ageOfDomain = LEGITIMATE
                savePrefence(ctx, URL_PREFERENCES, AGE_OF_DOMAIN, ageOfDomain)
            }else{
                Log.v(TAG, "the creation DNS date is NOT good")
                ageOfDomain = PHISHING
                savePrefence(ctx, URL_PREFERENCES, AGE_OF_DOMAIN, ageOfDomain)
            }

            //set dnsrecord as phishing too
            Log.v(TAG, "url was found on WhoIs, consider it legitimate")
            dnsRecord = LEGITIMATE
            savePrefence(ctx, URL_PREFERENCES, DNS_RECORD, dnsRecord)


        }else{
            //does not have the string exprationDateString, the site was not found on the
            //who is data base, phising
            Log.v(TAG, "url was not found on WhoIs, consider it phishing")
            ageOfDomain = PHISHING
            savePrefence(ctx, URL_PREFERENCES, AGE_OF_DOMAIN, ageOfDomain)

            //set dnsrecord as phishing too
            dnsRecord = PHISHING
            savePrefence(ctx, URL_PREFERENCES, DNS_RECORD, dnsRecord)
        }
    }


    /**
     * This function calculate the amoun of src="..." the html page has and calculates
     * the porcentage of src outside the domain
     */
    fun checkRequestUrl(cr : ConnectionResponse){
        val data = cr.answer

        if(data == ""){
            Log.v(TAG, "Data is empty")
            requestURL = PHISHING
            savePrefence(ctx, URL_PREFERENCES, REQUEST_URL, requestURL)
            return
        }

        var amountSrcIn  = 0
        var amountSrcOut  = 0
        val domain = URI(cr.url).host.removePrefix("www.")
        val matcher = Pattern.compile("(src=['\"]([^{'\"}]*)['\"])").matcher(data)
        var match : String? = null

        while (matcher.find()) {
            match = matcher.group(2)
            if(match != "") {
                if (!match.contains(domain, false) && checkURL(match)) {
                    amountSrcOut++
                } else {
                    amountSrcIn++
                }
            }
        }
        val amountOfSrcOutside = (amountSrcOut.toFloat()/(amountSrcOut.toFloat() + amountSrcIn.toFloat())) * 100
        when {
            amountOfSrcOutside >= 61.0F -> {
                //61% of src are outside the domain, is phising
                Log.v(TAG, "61% or more of src are outside the domain, is phising")
                requestURL = PHISHING
                savePrefence(ctx, URL_PREFERENCES, REQUEST_URL, requestURL)
            }
            amountOfSrcOutside in 22.0F..60.0F -> {
                //between 22% and 61% of src are outside the domain, is phising
                Log.v(TAG, "between 22% and 61% of src are outside the domain, is suspicious")
                requestURL = SUSPICIOUS
                savePrefence(ctx, URL_PREFERENCES, REQUEST_URL, requestURL)
            }
            else -> {
                //less than 21% of src are outside the domain, is phising
                Log.v(TAG, "less than 21% of src are outside the domain, is legitimate")
                requestURL = LEGITIMATE
                savePrefence(ctx, URL_PREFERENCES, REQUEST_URL, requestURL)
            }
        }
    }

    /**
     * Calculate the amount of anchors <a> the html has outside the domain and check if
     * it is phishing or not
     */
    fun checkAnchors(cr : ConnectionResponse){
        val data = cr.answer


        if(data == ""){
            Log.v(TAG, "Data is empty")
            urlOfAnchor = PHISHING
            savePrefence(ctx, URL_PREFERENCES, URL_OF_ANCHOR, urlOfAnchor)
            return
        }


        var amountSrcIn  = 0.0
        var amountSrcOut  = 0.0
        val domain = URI(cr.url).host.removePrefix("www.")
        var match : String
        val matcher = Pattern.compile("(<a.+href=['\"]([^{'\"}]*)['\"])").matcher(data)
        while (matcher.find()) {
            match = matcher.group(2)
            if(match != "") {
                if (!match.contains(domain, false) && checkURL(match))
                    amountSrcOut++
                else
                    amountSrcIn++
            }
        }

        val amountOfSrcOutside = (amountSrcOut/(amountSrcOut + amountSrcIn)).toFloat() * 100
        when {
            amountOfSrcOutside >= 67 -> {
                //67% of <a> are outside the domain, is phising
                Log.v(TAG, "67% of <a> are outside the domain, is phishing")
                urlOfAnchor = PHISHING
                savePrefence(ctx, URL_PREFERENCES, URL_OF_ANCHOR, urlOfAnchor)
            }
            amountOfSrcOutside in 31..66 -> {
                //between 31% and 67% of <a> are outside the domain, is suspicious
                Log.v(TAG, "between 31% and 67% of <a> are outside the domain, is suspicious ")
                urlOfAnchor = SUSPICIOUS
                savePrefence(ctx, URL_PREFERENCES, URL_OF_ANCHOR, urlOfAnchor)
            }
            else -> {
                //less than 31% of <a> are outside the domain, is legitimate
                Log.v(TAG, "less than 31% of <a> are outside the domain, is legitimate")
                urlOfAnchor = LEGITIMATE
                savePrefence(ctx, URL_PREFERENCES, URL_OF_ANCHOR, urlOfAnchor)
            }
        }
    }

    /**
     * calculate the amount of Link and Script are outside the domain and calculate the %
     * cheching at the end the feature depending on the rage
     */
    fun checktLinkScript(cr : ConnectionResponse){
        val data = cr.answer

        if(data == ""){
            Log.v(TAG, "Data is empty")
            linksInTags = PHISHING
            savePrefence(ctx, URL_PREFERENCES, LINKS_IN_TAGS, linksInTags)
            return
        }

        var amountSrcIn  = 0.0
        var amountSrcOut  = 0.0
        val domain = URI(cr.url).host.removePrefix("www.")
        var match : String? = null
        val matcher = Pattern.compile("(<link.+href=['\"]([^{'\"}]*)['\"])").matcher(data)
        while (matcher.find()) {
            match = matcher.group(2)
            if(match != "") {
                if (!match.contains(domain, false) && checkURL(match))
                    amountSrcOut++
                else
                    amountSrcIn++
            }
        }

        val matcher2 = Pattern.compile("(<script.+src=['\"]([^{'\"}]*)['\"])").matcher(data)
        while (matcher2.find()) {
            match = matcher2.group(2)
            if(match != "") {
                if (!match.contains(domain, false) && checkURL(match))
                    amountSrcOut++
                else
                    amountSrcIn++
            }
        }

        val amountOfSrcOutside = (amountSrcOut/(amountSrcOut + amountSrcIn)).toFloat() * 100
        when {
            amountOfSrcOutside >= 81 -> {
                //81% of <Script> and <Link>  are outside the domain, is phising
                Log.v(TAG, "81% of <Script> and <Link>  are outside the domain, is phising")
                linksInTags = PHISHING
                savePrefence(ctx, URL_PREFERENCES, LINKS_IN_TAGS, linksInTags)
            }
            amountOfSrcOutside in 31..80 -> {
                //between 17% and 81% of <Script> and <Link>  are outside the domain, is suspicious
                Log.v(TAG, "between 17% and 81% of <Script> and <Link>  are outside the domain, is suspicious ")
                linksInTags = SUSPICIOUS
                savePrefence(ctx, URL_PREFERENCES, LINKS_IN_TAGS, linksInTags)
            }
            else -> {
                //less than 17% of <Script> and <Link>  are outside the domain, is legitimate
                Log.v(TAG, "less than 17% of <Script> and <Link>  are outside the domain, is legitimate")
                linksInTags = LEGITIMATE
                savePrefence(ctx, URL_PREFERENCES, LINKS_IN_TAGS, linksInTags)
            }
        }
    }

    /**
     * this function check for the IFrame tag and verify if it has the attribute frameBorder
     * and calculate the Iframe attribute
     */
    private fun checkIFrame(cr: ConnectionResponse) {
        val pattern = Pattern.compile("(<iframe.+frameborder=.+>)")
        val matcher = pattern.matcher(cr.answer)
        while (matcher.find()) {
            val m = matcher.group()
            //theres iframe with frameBorder attribute
            Log.v(TAG, "theres iframe with frameBorder attribute $m")
            iFrame = PHISHING
            savePrefence(ctx, URL_PREFERENCES, I_FFRAME, iFrame)
            return
        }
        //theres NO iframe with frameBorder attribute
        Log.v(TAG, "theres NO iframe with frameBorder attribute")
        iFrame = LEGITIMATE
        savePrefence(ctx, URL_PREFERENCES, I_FFRAME, iFrame)


       /* if (matcher.matches()) {
            //theres iframe with frameBorder attribute
            Log.v(TAG, "theres iframe with frameBorder attribute")
            iFrame = PHISHING
            savePrefence(ctx, URL_PREFERENCES, I_FFRAME, iFrame)
        }else{
            //theres NO iframe with frameBorder attribute
            Log.v(TAG, "theres NO iframe with frameBorder attribute")
            iFrame = LEGITIMATE
            savePrefence(ctx, URL_PREFERENCES, I_FFRAME, iFrame)
        }*/
    }

    /**
     * This function parser the Alexa response and find the rank of the webstie
     * modifies the web_traffic feature
     */
    private fun checkRankSite(cr : ConnectionResponse){
        val rankSite = "<REACH RANK="

        if(cr.answer!!.contains(rankSite)){
            //find the rank of the site
            val pattern = Pattern.compile("<REACH RANK=\"(\\d+)\"/>")
            val matcher = pattern.matcher(cr.answer!!)
            var rankSiteString  = ""
            while (matcher.find()) {
                rankSiteString = matcher.group(1)
            }
            Log.v(TAG, "rankSite $rankSiteString")

            if (rankSiteString.toInt() < 100000) {
                //the rank is good
                Log.v(TAG, "the rank is good")
                webtraffic = LEGITIMATE
                savePrefence(ctx, URL_PREFERENCES, WEB_TRAFFIC, webtraffic)
            }else{
                //the rank is NOT good
                Log.v(TAG, "the rank is NOT good")
                webtraffic = SUSPICIOUS
                savePrefence(ctx, URL_PREFERENCES, WEB_TRAFFIC, webtraffic)
            }


        }else{
            //was not found by alexa, phishing
            Log.v(TAG, "was not found by alexa, phishing")
            webtraffic = PHISHING
            savePrefence(ctx, URL_PREFERENCES, WEB_TRAFFIC, webtraffic)
        }
    }

    /**
     * This function parser the google response to check if it in index by google
     */
    private fun checkPageIndex(cr : ConnectionResponse){
        if(cr.answer!!.contains("did not match any documents")){
            //was not found by google, phishing
            Log.v(TAG, "definitely was not found by google, phishing")
            googleIndex = PHISHING
            savePrefence(ctx, URL_PREFERENCES, GOOGLE_INDEX, googleIndex)
        }else{
            //was found by google, legitimate
            Log.v(TAG, "was found by google, legitimate")
            googleIndex = LEGITIMATE
            savePrefence(ctx, URL_PREFERENCES, GOOGLE_INDEX, googleIndex)
        }
    }


    /**
     * this function check if the ip is in the top 10 in the last 6 months in phishtank
     * and modifies the Statistical_report feature
     */
    fun checkBlackListPhishtank(cr : ConnectionResponse){
        val blackListIp = arrayOf("146.112.61.108", "31.170.160.61", "67.199.248.11",
                "67.199.248.10", "69.50.209.78", "192.254.172.78", "216.58.193.65",
                "23.234.229.68", "173.212.223.160", "60.249.179.122", "200.219.245.41",
                "50.31.138.222", "200.219.245.53", "200.219.245.194", "213.174.157.151",
                "209.202.252.50", "95.110.230.232", "185.188.204.200", "220.158.164.119",
                "5.57.226.202", "137.74.218.116", "142.4.27.2", "166.78.238.48", "103.24.13.91",
                "216.58.193.97", "50.56.249.31", "209.99.40.222", "62.149.128.154",
                "107.180.21.19", "209.99.40.223", "193.109.246.27", "195.216.243.36",
                "103.229.125.187", "103.60.181.238", "220.158.164.119", "46.242.145.98",
                "82.223.208.140", "198.252.101.145", "185.165.123.4", "103.248.220.150",
                "77.222.40.60", "104.20.88.65", "111.92.189.87", "112.175.88.145", "70.39.184.115",
                "93.184.220.23", "104.20.87.65", "223.130.27.125", "192.254.190.96",
                "198.57.247.131", "27.121.64.188", "80.87.205.134", "54.72.9.51", "192.185.15.212",
                "103.200.116.11", "78.46.211.158", "160.153.162.141", "65.60.44.234",
                "107.180.28.153", "143.95.240.24", "83.125.22.208", "143.95.239.91",
                "121.50.168.88", "192.185.217.116", "181.174.165.13", "46.242.145.103",
                "121.50.168.40", "83.125.22.219", "103.204.179.81", "202.168.151.79",
                "192.254.232.236", "46.242.145.98", "198.252.65.61", "89.207.130.43",
                "74.125.28.132")

        if (blackListIp.any { cr.ip== it}){
            //ip is in the black list
            Log.v(TAG, "ip is in the black list")
            statisticalReport = PHISHING
            savePrefence(ctx, URL_PREFERENCES, STATISTICAL_REPORT, statisticalReport)
        }else{
            //ip was not in the black list, it is necessecary to check the domains
            Log.v(TAG, "ip was not in the black list, it is necessecary to check the domains")
            val blackListDomain = arrayOf("esy.es","hol.es","000webhostapp.com","16mb.com",
                    "for-our.info","beget.tech","blogspot.com","weebly.com","raymannag.ch",
                    "96.lt","totalsolution.com.br","sellercancelordernotification.com",
                    "kloshpro.com","webcindario.com","manageaccount-disputepaymentebay-paymentresolve.com")

            if (blackListDomain.any { cr.url!!.contains(it)}){
                //domain is in the black list
                Log.v(TAG, "domain is in the black list")
                statisticalReport = PHISHING
                savePrefence(ctx, URL_PREFERENCES, STATISTICAL_REPORT, statisticalReport)
            }else{
                //domain is not in black list
                Log.v(TAG, "domain is not in the black list")
                statisticalReport = LEGITIMATE
                savePrefence(ctx, URL_PREFERENCES, STATISTICAL_REPORT, statisticalReport)
            }
        }
    }


    private fun checkFeatures(){
        val myHandler = Handler()
        val features = getFeaturesArray_opt4()
        Log.v(TAG, "features ${Arrays.toString(features)}")

        if(features.contains((-2).toFloat())) {
            Log.v(TAG, "featuresString contains -2, try again")
            myHandler.postDelayed({checkFeatures()}, 250)
        }else{
            Log.v(TAG, "featuresString does not contain -2")
            mListener?.siteFeatures(ctx, url, features, _id)
        }

    }

    private fun getFeaturesArray() : FloatArray {
        return floatArrayOf(havingIPAddress.toFloat(), urlLength.toFloat(),
                shortiningService.toFloat(), havingAtSymbol.toFloat(), doubleslashredirecting.toFloat(),
                prefixSuffix.toFloat() , havingSubDomain.toFloat(), sslFinalState.toFloat(),
                domainRegisterationLength.toFloat() , favicon.toFloat(), port.toFloat(),
                httpsToken.toFloat(), requestURL.toFloat(), urlOfAnchor.toFloat(), linksInTags.toFloat(),
                redirect.toFloat(), iFrame.toFloat(), ageOfDomain.toFloat(), dnsRecord.toFloat(),
                webtraffic.toFloat(), googleIndex.toFloat(), statisticalReport.toFloat())
    }

    private fun getFeaturesArray_opt2() : FloatArray {
        return floatArrayOf(havingIPAddress.toFloat(), urlLength.toFloat(),
                shortiningService.toFloat(),
                prefixSuffix.toFloat() , havingSubDomain.toFloat(), sslFinalState.toFloat(),
                domainRegisterationLength.toFloat() , favicon.toFloat(), port.toFloat(),
                httpsToken.toFloat(), requestURL.toFloat(), urlOfAnchor.toFloat(), linksInTags.toFloat(),
                redirect.toFloat(), iFrame.toFloat(), ageOfDomain.toFloat(), dnsRecord.toFloat(),
                webtraffic.toFloat(), googleIndex.toFloat())
    }

    private fun getFeaturesArray_opt3() : FloatArray {
        return floatArrayOf(havingIPAddress.toFloat(), urlLength.toFloat(),
                shortiningService.toFloat(),
                prefixSuffix.toFloat() , havingSubDomain.toFloat(), sslFinalState.toFloat(),
                domainRegisterationLength.toFloat() , favicon.toFloat(),
                httpsToken.toFloat(), requestURL.toFloat(), urlOfAnchor.toFloat(), linksInTags.toFloat(),
                redirect.toFloat(), iFrame.toFloat(), ageOfDomain.toFloat(), dnsRecord.toFloat(),
                webtraffic.toFloat(), googleIndex.toFloat())
    }

    private fun getFeaturesArray_opt4() : FloatArray {
        return floatArrayOf(havingIPAddress.toFloat(), urlLength.toFloat(),
                shortiningService.toFloat(),
                prefixSuffix.toFloat() , havingSubDomain.toFloat(), sslFinalState.toFloat(),
                domainRegisterationLength.toFloat() , favicon.toFloat(),
                 requestURL.toFloat(), urlOfAnchor.toFloat(), linksInTags.toFloat(),
                redirect.toFloat(), iFrame.toFloat(), ageOfDomain.toFloat(), dnsRecord.toFloat(),
                webtraffic.toFloat(), googleIndex.toFloat())
    }

    private fun getFeaturesArray_new() : FloatArray {
        return floatArrayOf(sslFinalState.toFloat(), requestURL.toFloat(), urlOfAnchor.toFloat(),
                webtraffic.toFloat(), urlLength.toFloat(), ageOfDomain.toFloat(),
                havingIPAddress.toFloat())
    }


    interface OnFinishFeaturesPhishingWebsite{

        //finish to calculate all features
        fun siteFeatures(ctx : Context, url: String, features : FloatArray, _id : Int)
    }
}