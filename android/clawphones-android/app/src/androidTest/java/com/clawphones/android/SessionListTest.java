package com.clawphones.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.action.ViewActions.click;
import androidx.test.espresso.matcher.ViewMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

@RunWith(AndroidJUnit4.class)
public class SessionListTest {
  @Test
  public void testOpenFirstSessionShowsChat() {
    try (ActivityScenario<SessionListActivity> scenario = ActivityScenario.launch(SessionListActivity.class)) {
      onView(withId(R.id.recycler_sessions))
        .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
      onView(withText("Chat")).check(matches(ViewMatchers.isDisplayed()));
    } catch (Exception e) {
      throw new AssertionError("Failed to open first session: " + e.getMessage(), e);
    }
  }
}
