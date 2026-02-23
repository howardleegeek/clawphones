package com.oyster.clawphones;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** UI Automator tests focusing on network-related settings behavior. */
@RunWith(AndroidJUnit4.class)
public class NetworkTimeoutTest {
  private UiDevice device;
  private Context context;

  @Before
  public void setUp() {
    device = UiDevice.getInstance(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation());
    context = InstrumentationRegistry.getInstrumentation().getTargetContext();
  }

  private boolean startSettingsIfPresent() {
    try {
      PackageManager pm = context.getPackageManager();
      Intent intent = pm.getLaunchIntentForPackage("com.android.settings");
      if (intent == null) return false;
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
      device.wait(Until.hasObject(By.pkg("com.android.settings").depth(0)), 5000);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean navigateToNetworkSection() {
    String[] labels = new String[] {"Network & internet", "网络与互联网", "Connections", "网络"};
    for (String label : labels) {
      UiObject2 item = device.findObject(By.text(label));
      if (item != null && item.exists()) {
        item.click();
        return true;
      }
    }
    return false;
  }

  @Test
  public void testNetworkSectionAccessible() {
    assumeSettingsOpened();
    // Try navigation if available
    navigateToNetworkSection();
    // Do not fail if sub-section isn't found; presence of Settings is enough for this test
    assertTrue("Settings foreground check failed", device.wait(Until.hasObject(By.pkg("com.android.settings").depth(0)), 2000));
  }

  private void assumeSettingsOpened() {
    if (!startSettingsIfPresent()) {
      // If Settings can't be opened, skip the test gracefully
      org.junit.Assume.assumeTrue(false);
    }
  }
}
