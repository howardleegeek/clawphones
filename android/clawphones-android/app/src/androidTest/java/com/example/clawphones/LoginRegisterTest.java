package com.example.clawphones;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.content.Context;
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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class LoginRegisterTest {

  // A tiny in-test activity to simulate login/register flows with Espresso
  public static class DummyLoginActivity extends AppCompatActivity {
    public static final int USERNAME_ID = 2002;
    public static final int PASSWORD_ID = 2003;
    public static final int LOGIN_BUTTON_ID = 2004;
    public static final int REGISTER_BUTTON_ID = 2005;
    public static final int STATUS_ID = 2006;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout root = new LinearLayout(this);
      root.setOrientation(LinearLayout.VERTICAL);
      root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

      EditText username = new EditText(this);
      username.setId(USERNAME_ID);
      username.setHint("Username");
      root.addView(username);

      EditText password = new EditText(this);
      password.setId(PASSWORD_ID);
      password.setHint("Password");
      root.addView(password);

      Button loginBtn = new Button(this);
      loginBtn.setId(LOGIN_BUTTON_ID);
      loginBtn.setText("Login");
      root.addView(loginBtn);

      Button registerBtn = new Button(this);
      registerBtn.setId(REGISTER_BUTTON_ID);
      registerBtn.setText("Register");
      root.addView(registerBtn);

      TextView status = new TextView(this);
      status.setId(STATUS_ID);
      status.setText("Not logged in");
      root.addView(status);

      loginBtn.setOnClickListener(v -> {
        String user = username.getText() != null ? username.getText().toString() : "user";
        status.setText("Welcome, " + user);
      });

      registerBtn.setOnClickListener(v -> {
        String user = username.getText() != null ? username.getText().toString() : "user";
        status.setText("Registered: " + user);
      });

      setContentView(root);
    }
  }

  @Test
  public void testLoginAndRegisterFlows() {
    // Launch the dummy login activity
    ActivityScenario<DummyLoginActivity> scenario = ActivityScenario.launch(DummyLoginActivity.class);

    // Verify initial UI exists
    onView(withId(DummyLoginActivity.LOGIN_BUTTON_ID)).check(matches(withText("Login")));

    // Enter credentials
    onView(withId(DummyLoginActivity.USERNAME_ID)).perform(typeText("alice"), closeSoftKeyboard());
    onView(withId(DummyLoginActivity.PASSWORD_ID)).perform(typeText("secret"), closeSoftKeyboard());

    // Perform login
    onView(withId(DummyLoginActivity.LOGIN_BUTTON_ID)).perform(click());
    onView(withId(DummyLoginActivity.STATUS_ID)).check(matches(withText("Welcome, alice")));

    // Perform register with another username
    onView(withId(DummyLoginActivity.USERNAME_ID)).perform(typeText("alice"), closeSoftKeyboard());
    onView(withId(DummyLoginActivity.REGISTER_BUTTON_ID)).perform(click());
    onView(withId(DummyLoginActivity.STATUS_ID)).check(matches(withText("Registered: alice")));
  }
}
