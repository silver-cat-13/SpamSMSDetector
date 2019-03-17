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