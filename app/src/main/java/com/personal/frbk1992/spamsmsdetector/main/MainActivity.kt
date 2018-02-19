package com.personal.frbk1992.spamsmsdetector.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.personal.frbk1992.spamsmsdetector.*
import com.personal.frbk1992.spamsmsdetector.appInfo.AppInfoActivity
import com.personal.frbk1992.spamsmsdetector.sms.SMSActivity
import com.personal.frbk1992.spamsmsdetector.spamsms.SpamSMSActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.app_bar_main.*


/**
 * This is the main activity, this activity call the SMSListFragment fragment where the sms list will
 * show
 * @see SMSListFragment
 */
class MainActivity : AppCompatActivity(), SMSListFragment.OnSMSListFragmentInteractionListener{


    private val TAG = this.javaClass.simpleName

    //create the RxPermission
    private val rxPermission : RxPermissions by lazy{ RxPermissions(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // closes the activity in case another activity calls it using the EXIT intent value
        // this is use when an activity is closing all the activities
        if (intent.getBooleanExtra(EXIT, false)) {
            finish()
        }
        setSupportActionBar(toolbar)



        //call the SMSListFragment and check for SMS permission and ask for it in case it's need it
        if (savedInstanceState == null) {
            rxPermission.request(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                    .subscribe({ granted ->
                if(granted){
                    //permission was granted
                    startFragment(SMSListFragment.newInstance(), SMS_LIST_FRAGMENT_TAG)
                }else{
                    //permission was not granted
                    showNeutralDialogFinishActivity(getString(R.string.alert_dialog_not_permission_title_string),
                            getString(R.string.alert_dialog_not_permission_body_string),
                            getString(R.string.alert_dialog_not_permission_botton_string))
                }
            })

        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //              OnSMSListFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Funtion that retrieve all sms in the phone
     * @return List of all SMS
     */
    override fun getSMS(): ArrayList<SMSClass> {
        val sms = ArrayList<SMSClass>()

        val uriSMSURI = Uri.parse("content://sms/inbox")
        val cur = contentResolver.query(uriSMSURI,
                null, null, null, null)

        while (cur != null && cur.moveToNext()) {
            val id = cur.getString(cur.getColumnIndex("_id"))
            val address = cur.getString(cur.getColumnIndex("address"))
            val body = cur.getString(cur.getColumnIndexOrThrow("body"))
            sms.add(SMSClass(id.toInt(), address, body))
        }
        cur?.close()


        return sms
    }

    /**
     * Function that is called when the user click an sms, this function show the detail of the sms
     * @param sms selected sms to show
     */
    override fun showSMS(sms: SMSClass) {
        val intent = Intent(baseContext, SMSActivity::class.java)
        intent.putExtra(SMS_DETAIL_ACTIVITY, sms)
        startActivity(intent)
    }

    /**
     * function that called the TestUrlActivity via intent
     */
    override fun goInfoApp() {
        val intent = Intent(baseContext, AppInfoActivity::class.java)
        startActivity(intent)
    }




    //list of spam sms that are spam sms
  //  private var smsSpamList : SparseArray<SMSClass>? = null


    /**
     * Function that check all the sms to look for all spam sms
     */
    override fun testAllSMSForSpam(smsList : ArrayList<SMSClass>) {
        val intent = Intent(baseContext, SpamSMSActivity::class.java)
        //intent.putExtra(SMS_DETAIL_ACTIVITY, sms)
        startActivity(intent)

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //              OnSMSListFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      MainActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Funcion que inicializa un fragment
     * @param fragment el fragment a inicializar
     * @param tag el tag que va a tener el Fragment
     */
    private fun startFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.main_container, fragment, tag)
                .commit()
    }






    /**
     * Show a dialog that ends the activity when it dismissed
     * @param title el titulo del dialog
     * @param content el mensaje
     * @param bottonMsg el boton neutral
     */
    private fun showNeutralDialogFinishActivity(title: String, content: String, bottonMsg: String) {
        //Dialogo de alerta que aparece cuando se preciona acerca de
        val alert = android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setNeutralButton(bottonMsg) { _, _ ->
                    //empty
                }
                .create()
        alert.setOnDismissListener {
            //finish the activity
            finish()
        }
        alert.show()

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      MainActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////
}