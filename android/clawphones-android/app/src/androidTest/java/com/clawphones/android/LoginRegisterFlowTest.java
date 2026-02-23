package com.clawphones.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class LoginRegisterFlowTest {
  // This test assumes existence of LoginActivity and navigation to a Registration screen.
  @Test
  public void testNavigateToRegisterAndShowRegistrationScreen() {
    // Launch Login screen
    try (ActivityScenario<LoginActivity> ignored = ActivityScenario.launch(LoginActivity.class)) {
      // Try to click a "Register" control if present
      onView(withText("Register")).perform(click());
      // Expect registration screen to show something like "Create Account"
      onView(withText("Create Account")).check(matches(ViewMatchers.isDisplayed()));
    } catch (Exception e) {
      // If navigation elements are not present, fail fast with a readable message.
      throw new AssertionError("Login->Register flow not available in this build: " + e.getMessage(), e);
    }
  }
}
