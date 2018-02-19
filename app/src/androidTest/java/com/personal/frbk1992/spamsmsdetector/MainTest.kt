package com.personal.frbk1992.spamsmsdetector

import android.Manifest
import android.os.Build
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.personal.frbk1992.spamsmsdetector.main.MainActivity
import com.personal.frbk1992.spamsmsdetector.main.SMSListFragment
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainTest {

    /*
    val sms = ArrayList<SMSClass>()
    val dummySMSNoSpam = SMSClass(1, "+10000000", "This is no spam", spam = false)
    val dummySMSSpamNoURL = SMSClass(1, "+10000000", "On your laptop")
    val dummySMSDefaultSpamNoURL = SMSClass(1, "+10000000", "This is spam", spam = true)
    val dummySMSSpamURL = SMSClass(1
                , "+10000000"
                , "Your R0YALBANK services has been disabled for safety! Please visit the " +
                "link below in order to reactivate your account rbc.com.verifybanssl.com/?12506615001"
                )
    sms.add(dummySMSNoSpam)
    sms.add(dummySMSSpamNoURL)
    sms.add(dummySMSDefaultSpamNoURL)
    sms.add(dummySMSSpamURL)
     */

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    lateinit var smsListFragment : SMSListFragment

    @Before
    fun grantSMSPermissions() {
        // In M+, we have to set permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                    "pm grant  ${InstrumentationRegistry.getTargetContext().packageName} " +
                            " ${Manifest.permission.RECEIVE_SMS}")
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                    "pm grant  ${InstrumentationRegistry.getTargetContext().packageName} " +
                            " ${Manifest.permission.READ_SMS}")
        }
    }

    /**
     * Init the fragment
     */
    @Before
    fun initFragment() {

        val transaction = mActivityRule.activity.supportFragmentManager.beginTransaction()
        runOnUiThread{
            smsListFragment = SMSListFragment()
        }
        transaction.add(smsListFragment, SMS_LIST_FRAGMENT_TAG)
        transaction.commit()
    }


    /**
     * Check the list with is showing
     */
    @Test
    fun isListShowing() {
        onView(withId(android.R.id.list)).check(matches(isDisplayed()))
    }

    /**
     * Check that the first dummy sms is a no spam sms
     *
     * In this test the spam sms model is tested
     */
    @Test
    fun clickSMSGoToDetailForSMSNoSpamMsg() {
        //dummy sms
        val sms = SMSClass(1, "+10000000", "This is no spam", spam = false)
        //pressed the first dummy sms, a not spam sms
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0).perform(click())

        //check the content of the sms is displayed with the actual content
        onView(withId(R.id.fragment_sms_content)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_sms_content)).check(matches(withText(containsString(sms.content))))

        //check the title of the sms is showing correctly
        onView(withId(R.id.fragment_sms_title)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_sms_title)).check(matches(withText(containsString(sms.title))))

        //check the spam indicator text is not showing
        onView(withId(R.id.fragment_sms_indicator_spam_phishing)).check(matches(not(isDisplayed())))

        //check phishing floating button is not showing
        onView(withId(R.id.fragment_sms_floating_button_test_phishing_url)).check(matches(not(isDisplayed())))
    }

    /**
     * Check that the first dummy sms is aspam sms
     *
     * In this test the spam sms model is tested
     */
    @Test
    fun clickSMSGoToDetailForSMSSpamMsgWithNoURL() {
        //dummy sms
        val sms = SMSClass(1, "+10000000", "On your laptop")
        //pressed the first dummy sms, a not spam sms
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(1).perform(click())

        //check the content of the sms is displayed with the actual content
        onView(withId(R.id.fragment_sms_content)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_sms_content)).check(matches(withText(containsString(sms.content))))

        //check the title of the sms is showing correctly
        onView(withId(R.id.fragment_sms_title)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_sms_title)).check(matches(withText(containsString(sms.title))))

        //check the spam indicator text is not showing
        onView(withId(R.id.fragment_sms_indicator_spam_phishing)).check(matches((isDisplayed())))
        onView(withId(R.id.fragment_sms_indicator_spam_phishing))
                .check(matches(withText(containsString("SMS is SPAM"))))

        //check phishing floating button is not showing
        onView(withId(R.id.fragment_sms_floating_button_test_phishing_url)).check(matches(not(isDisplayed())))
    }

    /**
     * Check that the first dummy sms is a spam sms by default
     *
     * In this test the spam sms model is not tested
     */
    @Test
    fun clickSMSGoToDetailForSMSDefaultSpamMsgWithNoURL() {
        //dummy sms
        val sms = SMSClass(1, "+10000000", "This is spam", spam = true)
        //pressed the first dummy sms, a not spam sms
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(2).perform(click())

        //check the content of the sms is displayed with the actual content
        onView(withId(R.id.fragment_sms_content)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_sms_content)).check(matches(withText(containsString(sms.content))))

        //check the title of the sms is showing correctly
        onView(withId(R.id.fragment_sms_title)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_sms_title)).check(matches(withText(containsString(sms.title))))

        //check the spam indicator text is not showing
        onView(withId(R.id.fragment_sms_indicator_spam_phishing)).check(matches((isDisplayed())))
        onView(withId(R.id.fragment_sms_indicator_spam_phishing))
                .check(matches(withText(containsString("SMS is SPAM"))))

        //check phishing floating button is not showing
        onView(withId(R.id.fragment_sms_floating_button_test_phishing_url)).check(matches(not(isDisplayed())))
    }

    /**
     * Check that the second dummy sms is a spam sms with URL
     *
     * In this test the spam sms model is tested
     */
    @Test
    fun clickSMSGoToDetailForSMSDefaultSpamMsgWithURL() {
        //dummy sms
        val sms = SMSClass(1
                , "+10000000"
                , "Your R0YALBANK services has been disabled for safety! Please visit the link below in order to reactivate your account rbc.com.verifybanssl.com/?12506615001"
        )
        //pressed the first dummy sms, a not spam sms
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(3).perform(click())

        //check the content of the sms is displayed with the actual content
        onView(withId(R.id.fragment_sms_content)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_sms_content)).check(matches(withText(containsString(sms.content))))

        //check the title of the sms is showing correctly
        onView(withId(R.id.fragment_sms_title)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_sms_title)).check(matches(withText(containsString(sms.title))))

        //check the spam indicator text is not showing
        onView(withId(R.id.fragment_sms_indicator_spam_phishing)).check(matches((isDisplayed())))
        onView(withId(R.id.fragment_sms_indicator_spam_phishing))
                .check(matches(withText(containsString("SMS is SPAM"))))

        //check phishing floating button is not showing
        onView(withId(R.id.fragment_sms_floating_button_test_phishing_url)).check(matches(isDisplayed()))
    }
}
