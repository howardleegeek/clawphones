package com.clawphones.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.action.ViewActions.click;
import androidx.test.espresso.action.ViewActions.typeText;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

@RunWith(AndroidJUnit4.class)
public class SendMessageTest {
  @Test
  public void testSendMessageAppearsInChat() {
    try (ActivityScenario<ChatActivity> scenario = ActivityScenario.launch(ChatActivity.class)) {
      onView(withId(R.id.edit_message)).perform(typeText("Hello Espresso"), ViewActions.closeSoftKeyboard());
      onView(withId(R.id.btn_send)).perform(click());
      onView(withText("Hello Espresso")).check(matches(ViewMatchers.isDisplayed()));
    } catch (Exception e) {
      throw new AssertionError("Sending message failed: " + e.getMessage(), e);
    }
  }
}
