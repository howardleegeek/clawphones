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

@RunWith(AndroidJUnit4.class)
public class SettingsPageTest {
  private UiDevice device;
  private Context context;

  @Before
  public void setUp() {
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    context = InstrumentationRegistry.getInstrumentation().getTargetContext();
  }

  private boolean openSettings() {
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

  @Test
  public void testMainSettingsScreenVisible() {
    assumeSettingsOpened();
    // Check for a generic Settings header presence, try common locales
    UiObject2 header = device.findObject(By.text("Settings"));
    if (header == null || !header.exists()) {
      header = device.findObject(By.text("设置")); // Chinese locale
    }
    assertTrue("Settings main header not visible", header != null && header.exists());
  }

  private void assumeSettingsOpened() {
    org.junit.Assume.assumeTrue(openSettings());
  }
}
