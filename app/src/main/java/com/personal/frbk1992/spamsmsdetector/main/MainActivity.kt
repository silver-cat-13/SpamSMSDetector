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
import android.content.ContentResolver
import android.util.SparseArray
import kotlin.concurrent.thread


/**
 * This is the main activity, this activity call the SMSListFragment fragment where the sms list will
 * show, the class implements [OnSMSListFragmentInteractionListener], which is a
 * custom interface used by the SMSListFragment fragment to called function from the activity
 * @see SMSListFragment
 */
class MainActivity : AppCompatActivity(), SMSListFragment.OnSMSListFragmentInteractionListener{

    //tag used for Log
    private val TAG = this.javaClass.simpleName

    private val SMS_URI_CONTENT = "content://sms/" // String used to get SMS
    private val SMS_CONVERSATIONS = "conversations"
    private val SMS_ID = "_id"
    private val SMS_ADDRESS = "address"
    private val SMS_BODY = "body"
    private val DATE_DEST_ORDER = "date DESC"

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
    override fun getSMS(): SparseArray<Conversation> {
        val conversations = SparseArray<Conversation>()

        // get the conversations in sms
        if(getConversarions(conversations) == 1){
            Log.e(TAG, "Error while getting the conversations, inform the use")

            // TODO handle better the error
            showToast(this, "There was an error while getting the SMS messages")
        }

        return conversations
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
     * This class get all the conversations and add them into a Conversation instance
     * TODO use the conversation to store eaach conversation instead class instead the SMSClass
     * TODO use RxJava to perform this action in an asynchronous way
     * @return the function returns 0 in sucess and 1 if there is an error
     */
    private fun getConversarions(conversations : SparseArray<Conversation>) : Int? {
        var isMe: Boolean // true if the user sent an email

        val uriSMSURI = Uri.parse(SMS_URI_CONTENT) // content://sms/
        val cur = contentResolver.query(uriSMSURI,
                null, null, null, DATE_DEST_ORDER)


        while (cur != null && cur.moveToNext()) {
            try {
                // check if the user is sender or receiver
                isMe = cur.getString(cur.getColumnIndexOrThrow("person")) == null

                // get all the parameters for the SMS
                val id = cur.getString(cur.getColumnIndex(SMS_ID))
                val address = cur.getString(cur.getColumnIndex(SMS_ADDRESS))
                val body = cur.getString(cur.getColumnIndexOrThrow(SMS_BODY))
                val date = cur.getString(cur.getColumnIndexOrThrow("date"))
                val tid = cur.getString(cur.getColumnIndexOrThrow("thread_id")).toInt()

                // create the SMSClass instance
                val sms = SMSClass(id.toInt(), address, body, isMe=isMe, date=date)

                // add the SMS in the conversations SparseArray
                Conversation.addSMSToConversation(conversations, sms, tid)

            }catch (e : java.lang.IllegalStateException){
                //an IllegalStateException by one SMS, it will be not taken into account
                Log.e(TAG, "Error with one SMS ${e.message}")
                cur?.close()
                return 1
            }
        }
        cur?.close()
        return 0 // success
    }


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