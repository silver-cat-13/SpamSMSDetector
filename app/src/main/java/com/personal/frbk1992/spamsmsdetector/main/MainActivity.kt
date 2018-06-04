package com.personal.frbk1992.spamsmsdetector.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.personal.frbk1992.spamsmsdetector.*
import com.personal.frbk1992.spamsmsdetector.appInfo.AppInfoActivity
import com.personal.frbk1992.spamsmsdetector.main.SMSListFragment.OnSMSListFragmentInteractionListener
import com.personal.frbk1992.spamsmsdetector.sms.SMSActivity
import com.personal.frbk1992.spamsmsdetector.spamsms.SpamSMSActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.app_bar_main.*


/**
 * This is the main activity, this activity call the SMSListFragment fragment where the sms list will
 * show, the class implements [OnSMSListFragmentInteractionListener], which is a
 * custom interface used by the SMSListFragment fragment to called function from the activity
 * @see SMSListFragment
 */
class MainActivity : AppCompatActivity(), SMSListFragment.OnSMSListFragmentInteractionListener{

    //tag used for Log
    private val TAG = this.javaClass.simpleName

    //create the RxPermission
    private val rxPermission : RxPermissions by lazy{ RxPermissions(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // closes the activity in case another activity calls it using the EXIT intent value
        // this is use when an activity is closing all the activities
        // This may happened if the user decides to remove the permission to read SMS to the app
        // while another activity, other than MainActivity, is open. the Activity that is currently open
        // finish and the MainActivity with it
        if (intent.getBooleanExtra(EXIT, false)) {
            finish()
        }


        //set the title of the activity
        this.title = resources.getString(R.string.title_activity_sms)

        //set the layout
        setContentView(R.layout.activity_main)

        //set the toolbar
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
     * Function that retrieve all sms in the phone and creates an ArrayList<SMSClass> with each SMS
     * @return List of all SMS
     */
    override fun getSMS(): ArrayList<SMSClass> {
        val sms = ArrayList<SMSClass>()
        /*
        val sms1 = SMSClass(1, "+10000000", "This is no spam", spam = false)
        val sms2 = SMSClass(1, "+10000000", "On your laptop")
        val sms3 = SMSClass(1, "+10000000", "This is spam", spam = true)
        val sms4 = SMSClass(1
                , "+10000000"
                , "Your R0YALBANK services has been disabled for safety! Please visit the link below in order to reactivate your account rbc.com.verifybanssl.com/?12506615001"
        )
        val sms5 = SMSClass(1
                , "+10000000"
                , "Hi there! Check my message in new social network. Waiting for your reply... My link: http://u.to/3_fEEQ"
        )
        sms.add(sms1)
        sms.add(sms2)
        sms.add(sms3)
        sms.add(sms4)
        sms.add(sms5)*/

        val uriSMSURI = Uri.parse("content://sms/inbox")
        val cur = contentResolver.query(uriSMSURI,
                null, null, null, null)

        while (cur != null && cur.moveToNext()) {
            //check if the SMS is correct
            try {
                val id = cur.getString(cur.getColumnIndex("_id"))
                val address = cur.getString(cur.getColumnIndex("address"))
                val body = cur.getString(cur.getColumnIndexOrThrow("body"))
                sms.add(SMSClass(id.toInt(), address, body))
            }catch (e : java.lang.IllegalStateException){
                //an IllegalStateException by one SMS, it will be not taken into account
                Log.e(TAG, "Error with one SMS ${e.message}")
            }
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


    /**
     * Function that will call the SpamSMSActivity, this activity shows all spam SMS
     */
    override fun goSpamSMSActivity(smsList : ArrayList<SMSClass>) {
        val intent = Intent(baseContext, SpamSMSActivity::class.java)
        intent.putParcelableArrayListExtra(SMS_LIST_INTENT, smsList)
        startActivity(intent)

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //              OnSMSListFragmentInteractionListener  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      MainActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Start a fragment given an instance of the fragment and a TAG
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
     * Show a dialog that ends the activity when it dismissed, this dialog is used if the user
     * do not accept the permission for reading SMS.
     * @param title title of the dialog
     * @param content content of the dialog
     * @param bottonMsg text in the button
     */
    private fun showNeutralDialogFinishActivity(title: String, content: String, bottonMsg: String) {
        val alert = android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setNeutralButton(bottonMsg) { _, _ ->
                    //empty
                }
                .create()
        //when the dialog dismiss call this function
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