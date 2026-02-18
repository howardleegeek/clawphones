package com.example.clawphones;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import androidx.appcompat.app.AppCompatActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;

@RunWith(AndroidJUnit4.class)
public class SendMessageTest {
  public static class DummySendMessageActivity extends AppCompatActivity {
    public static final int MESSAGE_ID = 4001;
    public static final int SEND_BUTTON_ID = 4002;
    public static final int STATUS_ID = 4003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout root = new LinearLayout(this);
      root.setOrientation(LinearLayout.VERTICAL);
      root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

      EditText message = new EditText(this);
      message.setId(MESSAGE_ID);
      message.setHint("Message");
      root.addView(message);

      Button sendBtn = new Button(this);
      sendBtn.setId(SEND_BUTTON_ID);
      sendBtn.setText("Send");
      root.addView(sendBtn);

      TextView status = new TextView(this);
      status.setId(STATUS_ID);
      status.setText("No message sent");
      root.addView(status);

      sendBtn.setOnClickListener(v -> {
        String msg = message.getText() != null ? message.getText().toString() : "";
        status.setText("Sent: " + msg);
      });

      setContentView(root);
    }
  }

  @Test
  public void testSendMessage() {
    ActivityScenario<DummySendMessageActivity> scenario = ActivityScenario.launch(DummySendMessageActivity.class);

    onView(withId(DummySendMessageActivity.MESSAGE_ID)).perform(typeText("Hello"), closeSoftKeyboard());
    onView(withId(DummySendMessageActivity.SEND_BUTTON_ID)).perform(click());
    onView(withId(DummySendMessageActivity.STATUS_ID)).check(matches(withText("Sent: Hello")));
  }
}
