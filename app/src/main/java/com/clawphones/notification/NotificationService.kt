package com.clawphones.notification

enum class NotificationType {
    PUSH, EMAIL, SMS
}

data class UserPreferences(
  val allowedTypes: List<NotificationType>,
  val preferredFrequencyPerHour: Int? = null
)

data class User(
  val id: String,
  val age: Int,
  val preferences: UserPreferences
)

data class Interaction(
  val type: String,
  val timestamp: Long,
  val engaged: Boolean
)

data class NotificationConfig(
  val type: NotificationType,
  val frequencyPerHour: Int
)

/** Lightweight ML model to score how likely a user is to engage with a given notification type. */
class MLModel {
  fun predict(user: User, type: NotificationType, recentActions: List<Interaction>): Double {
    val base = when (type) {
      NotificationType.PUSH -> 1.0
      NotificationType.EMAIL -> 0.8
      NotificationType.SMS -> 0.6
    }
    val engagementBoost = if (recentActions.any { it.engaged && it.type.equals(type.name, ignoreCase = true) }) 0.5 else 0.0
    val preferenceBoost = if (user.preferences.allowedTypes.contains(type)) 0.2 else 0.0
    return base + engagementBoost + preferenceBoost
  }
}

class NotificationService(private val model: MLModel = MLModel()) {
  /** Decide the best notification config for a user given their recent actions. */
  fun decide(user: User, recentActions: List<Interaction>): NotificationConfig {
    val candidates = if (user.preferences.allowedTypes.isEmpty()) NotificationType.values().toList() else user.preferences.allowedTypes
    val scores = candidates.associateWith { t -> model.predict(user, t, recentActions) }
    val bestType = scores.maxByOrNull { it.value }?.key ?: NotificationType.PUSH
    val freq = user.preferences.preferredFrequencyPerHour ?: 2
    val frequencyPerHour = freq.coerceIn(1, 4)
    return NotificationConfig(bestType, frequencyPerHour)
  }
}
