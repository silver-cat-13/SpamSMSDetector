package ca.uvic.frbk1992.spamsmsdetector.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import ca.uvic.frbk1992.spamsmsdetector.*
import ca.uvic.frbk1992.spamsmsdetector.classifier.PhishingClassifier
import ca.uvic.frbk1992.spamsmsdetector.classifier.SMSSpamClassifier
import ca.uvic.frbk1992.spamsmsdetector.phisingDetector.FindValuesURL
import ca.uvic.frbk1992.spamsmsdetector.sms.SMSActivity
import ca.uvic.frbk1992.spamsmsdetector.spamsms.SpamSMSActivity
import ca.uvic.frbk1992.spamsmsdetector.testurl.TestUrlActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


/**
 * This is the main activity, this activity call the SMSListFragment fragment where the sms list will
 * show
 * @see SMSListFragment
 */
class MainActivity : RxAppCompatActivity(), SMSListFragment.OnSMSListFragmentInteractionListener,
        FindValuesURL.OnFinishFeaturesPhishingWebsite, NavigationView.OnNavigationItemSelectedListener {


    private val TAG = this.javaClass.simpleName

    private var phishingClassifier: PhishingClassifier? = null
    private var smsClassifier: SMSSpamClassifier? = null
    private val executor = Executors.newSingleThreadExecutor()

    var test : FindValuesURL? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        //create the RxPermission
        val rxPermission = RxPermissions(this)

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


    /**
     * this function is called when all the features has been detected
     */
    override fun siteFeatures(ctx: Context, url : String,features : FloatArray, _id : Int){

        Log.v(TAG, "features ${Arrays.toString(features)}")

        //Log.v(TAG, "features ${Arrays.toString(f)}")
       //Log.v(TAG, "features ${getStringAtributes(f)}")
        if(phishingClassifier!!.isPhishing(features)) {
            Log.e(TAG, "The Site $url is phishing")
        }
        else
            Log.v(TAG, "Site is $url NOT phishing")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.test_url -> {
                goTestUrl()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when(requestCode){
            REQUEST_PERMISSION_RECEIVE_SMS ->{
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showNeutralDialog(this, "Please", ":(", "close")
                }else{
                    //permission acepted, show fragment
                    startFragment(SMSListFragment.newInstance(), SMS_LIST_FRAGMENT_TAG)
                }
            }
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
     * function that called the TestUrlActivity via intent
     */
    fun goTestUrl() {
        val intent = Intent(baseContext, TestUrlActivity::class.java)
        startActivity(intent)
    }

    /**
     * Show a dialog that ends the activity when it dismissed
     * @param title el titulo del dialog
     * @param content el mensaje
     * @param bottonMsg el boton neutral
     */
    fun showNeutralDialogFinishActivity(title: String, content: String, bottonMsg: String) {
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



