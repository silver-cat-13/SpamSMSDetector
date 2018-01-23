package ca.uvic.frbk1992.spamsmsdetector.app_info


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.uvic.frbk1992.spamsmsdetector.APACHE_LICENSE

import ca.uvic.frbk1992.spamsmsdetector.R
import kotlinx.android.synthetic.main.fragment_app_info.*
import java.io.IOException
import java.io.InputStream


/**
 * A fragment with the information of the app, the licence and giving credits to other people.
 */
class AppInfoFragment : Fragment() {

    private val TAG = this.javaClass.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //set the information from the TXT
        fragment_app_info_text_view_apache_license.text = getLicenseAsset()
    }

    private fun getLicenseAsset() : String{

        var tContents = ""

        try {
            val stream = context!!.assets.open(APACHE_LICENSE)
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            tContents = String(buffer)
        } catch (e : IOException) {
            // Handle exceptions here
            Log.e(TAG, "error reading the apache license")
            return "..."
        }

        return tContents;
    }

    companion object {
        /**
         * Use this factory method to create a new instance ofthe fragment
         *
         * @return A new instance of fragment AppInfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): AppInfoFragment {
            return AppInfoFragment()
        }
    }


}
