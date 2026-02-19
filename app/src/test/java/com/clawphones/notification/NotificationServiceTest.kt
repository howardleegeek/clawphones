package com.clawphones.notification

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class NotificationServiceTest {
  @Test
  fun testChoosesPreferredTypeBasedOnScores() {
    val userPrefs = UserPreferences(
      allowedTypes = listOf(NotificationType.PUSH, NotificationType.EMAIL),
      preferredFrequencyPerHour = 3
    )
    val user = User(id = "u1", age = 30, preferences = userPrefs)
    val actions = listOf(
      Interaction(type = "PUSH", timestamp = System.currentTimeMillis(), engaged = true),
      Interaction(type = "EMAIL", timestamp = System.currentTimeMillis() - 1000, engaged = false)
    )
    val service = NotificationService()
    val config = service.decide(user, actions)
    // PUSH has higher base and engagement boost, expect PUSH
    assertEquals(NotificationType.PUSH, config.type)
    assertEquals(3, config.frequencyPerHour)
  }

  @Test
  fun testFrequencyRespectsPreference() {
    val userPrefs = UserPreferences(allowedTypes = listOf(NotificationType.SMS), preferredFrequencyPerHour = 1)
    val user = User(id = "u2", age = 25, preferences = userPrefs)
    val service = NotificationService()
    val config = service.decide(user, emptyList())
    assertEquals(NotificationType.SMS, config.type)
    assertEquals(1, config.frequencyPerHour)
  }
}
