package com.example.clawphones;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;
import androidx.appcompat.app.AppCompatActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.Espresso.onData;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.allOf;

import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;

import static androidx.test.espresso.Espresso.onData;

@RunWith(AndroidJUnit4.class)
public class SessionListTest {
  public static class DummySessionListActivity extends AppCompatActivity {
    public static final int SESSIONS_LIST_ID = 3001;
    public static final int STATUS_ID = 3002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout root = new LinearLayout(this);
      root.setOrientation(LinearLayout.VERTICAL);
      root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

      ListView listView = new ListView(this);
      listView.setId(SESSIONS_LIST_ID);
      final String[] sessions = new String[] {"Session 1", "Session 2"};
      ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sessions);
      listView.setAdapter(adapter);
      root.addView(listView);

      TextView status = new TextView(this);
      status.setId(STATUS_ID);
      status.setText("No selection");
      root.addView(status);

      listView.setOnItemClickListener((parent, view, position, id) -> {
        status.setText("Selected: " + sessions[position]);
      });

      setContentView(root);
    }
  }

  @Test
  public void testSelectSession() {
    ActivityScenario<DummySessionListActivity> scenario = ActivityScenario.launch(DummySessionListActivity.class);
    // Click the first session item
    onData(allOf(is(instanceOf(String.class)), is("Session 1")))
      .inAdapterView(withId(DummySessionListActivity.SESSIONS_LIST_ID))
      .atPosition(0)
      .perform(click());

    onView(withId(DummySessionListActivity.STATUS_ID)).check(matches(withText("Selected: Session 1")));
  }
}
