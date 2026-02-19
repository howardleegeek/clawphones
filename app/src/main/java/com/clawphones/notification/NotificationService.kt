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

  /**
   * Approximate probability (0.0 - 1.0) that the user will engage with this
   * notification type given their history. This is derived from the raw score
   * and then normalized to a [0, 1] range for easier thresholding.
   */
  fun probability(user: User, type: NotificationType, recentActions: List<Interaction>): Double {
    val raw = predict(user, type, recentActions)
    // Normalize to 0..1. We assume raw scores typically lie in [0, 2] in our model.
    return (raw / 2.0).coerceIn(0.0, 1.0)
  }
}

class NotificationService(private val model: MLModel = MLModel()) {
  /** Decide the best notification config for a user given their recent actions. */
  fun decide(user: User, recentActions: List<Interaction>): NotificationConfig {
    val candidates = if (user.preferences.allowedTypes.isEmpty()) NotificationType.values().toList() else user.preferences.allowedTypes
    val probabilities = candidates.associateWith { t -> model.probability(user, t, recentActions) }
    val bestType = probabilities.maxByOrNull { it.value }?.key ?: NotificationType.PUSH
    val freqBase = user.preferences.preferredFrequencyPerHour ?: 2
    var frequencyPerHour = freqBase.coerceIn(1, 4)
    val bestProb = probabilities[bestType] ?: 0.0
    // Adjust frequency based on predicted engagement probability
    frequencyPerHour = when {
      bestProb >= 0.75 -> (frequencyPerHour + 1).coerceAtMost(4)
      bestProb <= 0.25 -> (frequencyPerHour - 1).coerceAtLeast(1)
      else -> frequencyPerHour
    }
    return NotificationConfig(bestType, frequencyPerHour)
  }
}
