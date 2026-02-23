package com.clawphones.innovation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InnovationModuleTest {
    @Test
    fun testRecommendationTopContentIsMostRelevantForUser() {
        val module = InnovationModule("G13-05-CP")

        // Simulate user behavior
        module.collectEvent(UserEvent("u1", "contentA", Action.VIEW, 0))
        module.collectEvent(UserEvent("u1", "contentB", Action.VIEW, 0))
        module.collectEvent(UserEvent("u1", "contentA", Action.CLICK, 0))
        module.collectEvent(UserEvent("u2", "contentB", Action.VIEW, 0))

        module.trainModel()

        val recs = module.recommend("u1", 2)
        assertFalse("Recommendations should not be empty", recs.isEmpty())
        // contentA should be top because user u1 interacted with it more
        assertEquals("contentA", recs[0].contentId)
    }
}
