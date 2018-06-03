package com.personal.frbk1992.spamsmsdetector

import android.Manifest
import android.os.Build
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.*
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
import java.util.concurrent.TimeUnit
import android.support.test.espresso.util.HumanReadables
import android.support.test.espresso.PerformException
import android.support.test.espresso.util.TreeIterables
import android.support.test.espresso.UiController
import android.support.test.espresso.matcher.ViewMatchers.isRoot
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.ViewActions
import android.view.View
import org.hamcrest.Matcher
import java.util.concurrent.TimeoutException
import org.hamcrest.BaseMatcher
import org.hamcrest.Description


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainTest {



    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    lateinit var smsListFragment : SMSListFragment


    // if there are more than one Matcher this function only return the decide by position
    private fun getElementFromMatchAtPosition(matcher: Matcher<View>, position: Int): Matcher<View> {
        return object : BaseMatcher<View>() {
            internal var counter = 0
            override fun matches(item: Any): Boolean {
                if (matcher.matches(item)) {
                    if (counter == position) {
                        counter++
                        return true
                    }
                    counter++
                }
                return false
            }

            override fun describeTo(description: Description) {
                description.appendText("Element at hierarchy position $position")
            }
        }
    }


    /**
     * Perform action of waiting for a specific view id.
     * @param viewId The id of the view to wait for.
     * @param millis The timeout of until when to wait for.
     */
    private fun waitId(viewId: Int, millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isRoot()
            }

            override fun getDescription(): String {
                return "wait for a specific view with id <$viewId> during $millis millis."
            }

            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadUntilIdle()
                val startTime = System.currentTimeMillis()
                val endTime = startTime + millis
                val viewMatcher = withId(viewId)

                do {
                    for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                        // found view with required ID
                        if (viewMatcher.matches(child)) {
                            return
                        }
                    }

                    uiController.loopMainThreadForAtLeast(50)
                } while (System.currentTimeMillis() < endTime)

                // timeout happens
                throw PerformException.Builder()
                        .withActionDescription(this.description)
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(TimeoutException())
                        .build()
            }
        }
    }


    /**
     * Set the permission for reading SMS
     */
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

       /* val sms = SMSClass(1, "+10000000", "This is no spam", spam = false)
        val sms2 = SMSClass(1, "+10000000", "On your laptop")
        val sms3 = SMSClass(1, "+10000000", "This is spam", spam = true)
        val sms4 = SMSClass(1
                , "+10000000"
                , "Your R0YALBANK services has been disabled for safety! Please visit the link below in order to reactivate your account rbc.com.verifybanssl.com/?12506615001"
        )
        val smsList = ArrayList<SMSClass>()
        smsList.add(sms)
        smsList.add(sms2)
        smsList.add(sms3)
        smsList.add(sms4)
        runOnUiThread{
            smsListFragment.myListAdapter.update(smsList)
        }*/
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
     * Check that the second dummy sms is a spam sms
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
     * Check that the third dummy sms is a spam sms by default
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
     * Check that the fourth dummy sms is a spam sms with URL
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


    /**
     * Check that the fifth dummy sms is another spam sms with URL
     *
     * In this test the spam sms model is tested
     */
    @Test
    fun clickSMSGoToDetailForSMSDefaultSpamMsgWithURL2() {
        //dummy sms
        val sms = SMSClass(1
                , "+10000000"
                , "Hi there! Check my message in new social network. Waiting for your reply... My link: http://u.to/3_fEEQ"
        )
        //pressed the first dummy sms, a not spam sms
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(4).perform(click())

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


    /**
     * go to check all spam sms
     */
    @Test
    fun clickGetSpamSMS() {

        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

        // Click the item.
        onView(getElementFromMatchAtPosition(withText("Get spam SMS"), 0)).perform(click())

        onView(withId(android.R.id.list)).check(matches(isDisplayed()))

        // wait during 15 seconds for a view
        onView(isRoot()).perform(waitId(android.R.id.list, TimeUnit.SECONDS.toMillis(15)))


        //dummy sms
        var sms = SMSClass(1, "+10000000", "On your laptop")
        //pressed the first dummy sms, a not spam sms
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0).perform(click())

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

        onView(isRoot()).perform(ViewActions.pressBack())






        onView(isRoot()).perform(ViewActions.pressBack())


        //dummy sms
        sms = SMSClass(1
                , "+10000000"
                , "Your R0YALBANK services has been disabled for safety! Please visit the link below in order to reactivate your account rbc.com.verifybanssl.com/?12506615001"
        )
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
        onView(withId(R.id.fragment_sms_floating_button_test_phishing_url)).check(matches(isDisplayed()))



        onView(isRoot()).perform(ViewActions.pressBack())





        //dummy sms
        sms = SMSClass(1
                , "+10000000"
                , "Hi there! Check my message in new social network. Waiting for your reply... My link: http://u.to/3_fEEQ"
        )
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
        onView(withId(R.id.fragment_sms_floating_button_test_phishing_url)).check(matches(isDisplayed()))


        onView(isRoot()).perform(ViewActions.pressBack())
    }


}
