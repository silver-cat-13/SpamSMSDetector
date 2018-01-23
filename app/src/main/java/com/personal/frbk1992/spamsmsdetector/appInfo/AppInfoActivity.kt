package com.personal.frbk1992.spamsmsdetector.appInfo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import com.personal.frbk1992.spamsmsdetector.APP_INFO_FRAGMENT_TAG
import com.personal.frbk1992.spamsmsdetector.R
import kotlinx.android.synthetic.main.activity_sms.*

class AppInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_info)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //call the SMSListFragment
        if (savedInstanceState == null) {
            startFragment(AppInfoFragment.newInstance(), APP_INFO_FRAGMENT_TAG);
        }


    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                   AppInfoActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Funcion que inicializa un fragment
     * @param fragment el fragment a inicializar
     * @param tag el tag que va a tener el Fragment
     */
    private fun startFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.app_info_container, fragment, tag)
                .commit()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                   AppInfoActivity  Functions                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
