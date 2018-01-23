package com.personal.frbk1992.spamsmsdetector

import android.app.Activity
import android.content.Context
import android.widget.Toast


/**
 * Util methods
 */

/**
 * Show a neutral activity with a msg
 * @param activity the actyvity
 * @param title el titulo del dialog
 * @param content el mensaje
 * @param bottonMsg el boton neutral
 */
fun showNeutralDialog(activity: Activity, title: String, content: String, bottonMsg: String) {
    //Dialogo de alerta que aparece cuando se preciona acerca de
    val alert = android.app.AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(content)
            .setNeutralButton(bottonMsg) { _, _ ->
                // empty
            }
            .create()
    alert.show()

}


/**
 * Look for a Short value in the preference
 * @param c context
 * @param pref name of the preference
 * @param key name of the key
 * @param defaultValue default value
 * @return the short value
 */
fun searchPreference(c: Context, pref: String, key: String, defaultValue: Int): Int? {
    val settings = c.getSharedPreferences(pref, 0)
    return settings.getInt(key, defaultValue)
}

/**
 * Save a preference
 * @param c context
 * @param pref name of the preference
 * @param key name of the key
 * @param valor value of the preference
 */
fun savePrefence(c: Context, pref: String, key: String, valor: Int) {
    val settings = c.getSharedPreferences(pref, 0)
    val editor = settings.edit()
    editor.putInt(key, valor)
    editor.apply()
}

/**
 * Delete a preference
 * @param c context
 * @param pref name of the preference
 * @param key name of the key
 */
fun deletePreference(c: Context, pref: String, key: String) {
    val settings = c.getSharedPreferences(pref, 0)
    settings.edit().remove(key).apply()
}

/**
 * Funcion que muestra un Toast
 * @param c el Contexto
 * @param s el String a mostrar
 */
fun showToast(c: Context, s: CharSequence) {
    Toast.makeText(c, s, Toast.LENGTH_LONG).show()
}

/**
 * Delete a preference
 * @param c context
 * @param pref name of the preference
 */
fun deleteAllPreferences(c: Context, pref: String) {
    val settings = c.getSharedPreferences(pref, 0)
    settings.edit().clear().apply()
}



fun getStringAtributes(floatArray: FloatArray) : String{
    var attributes : String = ""

    if(floatArray[0] == -1.0F)
        attributes = "$attributes havingIPAddress = $PHISHING_S\n"
    else
        attributes = "$attributes havingIPAddress = $LEGITIMATE_S\n"

    if(floatArray[1] == -1.0F)
        attributes = "$attributes URL_Length = $PHISHING_S\n"
    else if(floatArray[1] == 0.0F)
        attributes = "$attributes URL_Length = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes URL_Length = $LEGITIMATE_S\n"

    if(floatArray[2] == -1.0F)
        attributes = "$attributes Shortining_Service = $PHISHING_S\n"
    else if(floatArray[2] == 0.0F)
        attributes = "$attributes Shortining_Service = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes Shortining_Service = $LEGITIMATE_S\n"

    if(floatArray[3] == -1.0F)
        attributes = "$attributes having_At_Symbol = $PHISHING_S\n"
    else if(floatArray[3] == 0.0F)
        attributes = "$attributes having_At_Symbol = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes having_At_Symbol = $LEGITIMATE_S\n"

    if(floatArray[4] == -1.0F)
        attributes = "$attributes double_slash_redirecting = $PHISHING_S\n"
    else if(floatArray[4] == 0.0F)
        attributes = "$attributes double_slash_redirecting = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes double_slash_redirecting = $LEGITIMATE_S\n"


    if(floatArray[5] == -1.0F)
        attributes = "$attributes Prefix_Suffix = $PHISHING_S\n"
    else if(floatArray[5] == 0.0F)
        attributes = "$attributes Prefix_Suffix = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes Prefix_Suffix = $LEGITIMATE_S\n"

    if(floatArray[6] == -1.0F)
        attributes = "$attributes having_Sub_Domain = $PHISHING_S\n"
    else if(floatArray[6] == 0.0F)
        attributes = "$attributes having_Sub_Domain = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes having_Sub_Domain = $LEGITIMATE_S\n"

    if(floatArray[7] == -1.0F)
        attributes = "$attributes SSLfinal_State = $PHISHING_S\n"
    else if(floatArray[7] == 0.0F)
        attributes = "$attributes SSLfinal_State = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes SSLfinal_State = $LEGITIMATE_S\n"

    if(floatArray[8] == -1.0F)
        attributes = "$attributes Domain_registeration_length = $PHISHING_S\n"
    else if(floatArray[8] == 0.0F)
        attributes = "$attributes Domain_registeration_length = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes Domain_registeration_length = $LEGITIMATE_S\n"

    if(floatArray[9] == -1.0F)
        attributes = "$attributes Favicon = $PHISHING_S\n"
    else if(floatArray[9] == 0.0F)
        attributes = "$attributes Favicon = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes Favicon = $LEGITIMATE_S\n"


    if(floatArray[10] == -1.0F)
        attributes = "$attributes port = $PHISHING_S\n"
    else if(floatArray[10] == 0.0F)
        attributes = "$attributes port = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes port = $LEGITIMATE_S\n"

    if(floatArray[11] == -1.0F)
        attributes = "$attributes HTTPS_token = $PHISHING_S\n"
    else if(floatArray[11] == 0.0F)
        attributes = "$attributes HTTPS_token = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes HTTPS_token = $LEGITIMATE_S\n"

    if(floatArray[12] == -1.0F)
        attributes = "$attributes Request_URL = $PHISHING_S\n"
    else if(floatArray[12] == 0.0F)
        attributes = "$attributes Request_URL = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes Request_URL = $LEGITIMATE_S\n"

    if(floatArray[13] == -1.0F)
        attributes = "$attributes URL_of_Anchor = $PHISHING_S\n"
    else if(floatArray[13] == 0.0F)
        attributes = "$attributes URL_of_Anchor = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes URL_of_Anchor = $LEGITIMATE_S\n"


    if(floatArray[14] == -1.0F)
        attributes = "$attributes Links_in_tags = $PHISHING_S\n"
    else if(floatArray[14] == 0.0F)
        attributes = "$attributes Links_in_tags = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes Links_in_tags = $LEGITIMATE_S\n"

    if(floatArray[15] == -1.0F)
        attributes = "$attributes Redirect = $PHISHING_S\n"
    else if(floatArray[15] == 0.0F)
        attributes = "$attributes Redirect = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes Redirect = $LEGITIMATE_S\n"

    if(floatArray[16] == -1.0F)
        attributes = "$attributes Iframe = $PHISHING_S\n"
    else if(floatArray[16] == 0.0F)
        attributes = "$attributes Iframe = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes Iframe = $LEGITIMATE_S\n"

    if(floatArray[17] == -1.0F)
        attributes = "$attributes age_of_domain = $PHISHING_S\n"
    else if(floatArray[17] == 0.0F)
        attributes = "$attributes age_of_domain = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes age_of_domain = $LEGITIMATE_S\n"
    if(floatArray[18] == -1.0F)
        attributes = "$attributes DNSRecord = $PHISHING_S\n"
    else if(floatArray[18] == 0.0F)
        attributes = "$attributes DNSRecord = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes DNSRecord = $LEGITIMATE_S\n"

    if(floatArray[19] == -1.0F)
        attributes = "$attributes web_traffic = $PHISHING_S\n"
    else if(floatArray[19] == 0.0F)
        attributes = "$attributes web_traffic = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes web_traffic = $LEGITIMATE_S\n"

    if(floatArray[20] == -1.0F)
        attributes = "$attributes Google_Index = $PHISHING_S\n"
    else if(floatArray[20] == 0.0F)
        attributes = "$attributes Google_Index = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes Google_Index = $LEGITIMATE_S\n"

    if(floatArray[21] == -1.0F)
        attributes = "$attributes Statistical_report = $PHISHING_S\n"
    else if(floatArray[21] == 0.0F)
        attributes = "$attributes Statistical_report = $SUSPICIOUS_S\n"
    else
        attributes = "$attributes Statistical_report = $LEGITIMATE_S\n"

    return attributes
}

