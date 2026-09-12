package com.rnlkav.storyapp.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.rnlkav.storyapp.R
import com.rnlkav.storyapp.ui.login.LoginActivity
import com.rnlkav.storyapp.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthenticationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun login_logout_success() {
        onView(withId(R.id.ed_login_email)).perform(typeText("bean@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText("12345678"), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).perform(click())

        // Give some time for transition if IdlingResource is slow
        Thread.sleep(2000)

        // Check if MainActivity is opened (Maps menu item is visible)
        onView(withId(R.id.action_maps)).check(matches(isDisplayed()))

        // Logout
        onView(withId(R.id.action_logout)).perform(click())
        
        // Wait for dialog
        Thread.sleep(1000)
        onView(withText(R.string.yes)).perform(click())

        // Give some time for transition back to Log in
        Thread.sleep(2000)

        // Check if LoginActivity is opened again
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
    }
}
