package com.clawphones.automation

import org.junit.Assert.*
import org.junit.Test

class AutomationModuleTest {
  // Fake scheduler to capture input without Android dependencies
  class FakeScheduler : Scheduler {
    var receivedTasks: List<AutomationTask> = emptyList()
    override fun scheduleTasks(context: SchedulingContext, tasks: List<AutomationTask>): Boolean {
      receivedTasks = tasks
      return true
    }
  }

  @Test
  fun schedulesOnlyEnabledTasks() {
    val t1 = AutomationTask("t1", 60, TaskType.DATA_SYNC, true)
    val t2 = AutomationTask("t2", 120, TaskType.BACKUP, false)
    val t3 = AutomationTask("t3", 30, TaskType.DATA_SYNC, true)
    val scheduler = FakeScheduler()
    val module = AutomationModule(scheduler)
    val ctx = SchedulingContext("user1", "unit-test")

    val result = module.setupAutomation(ctx, listOf(t1, t2, t3))
    assertTrue(result)
    val scheduled = scheduler.receivedTasks
    assertEquals(2, scheduled.size)
    assertTrue(scheduled.any { it.id == "t1" && it.enabled })
    assertTrue(scheduled.any { it.id == "t3" && it.enabled })
  }

  @Test
  fun logsContainContextAndTaskIds() {
    val t1 = AutomationTask("t1", 60, TaskType.DATA_SYNC, true)
    val scheduler = FakeScheduler()
    val module = AutomationModule(scheduler)
    val ctx = SchedulingContext("user42", "unit-test")
    module.setupAutomation(ctx, listOf(t1))
    val logs = Logger.getLogs()
    assertFalse(logs.isEmpty())
    val last = logs.last()
    assertTrue(last.contains("t1"))
    assertTrue(last.contains("user42"))
  }

  @Test
  fun noEnabledTasksSchedulesEmpty() {
    val t1 = AutomationTask("t1", 60, TaskType.DATA_SYNC, false)
    val scheduler = FakeScheduler()
    val module = AutomationModule(scheduler)
    val ctx = SchedulingContext("u", "unit-test")
    module.setupAutomation(ctx, listOf(t1))
    assertTrue(scheduler.receivedTasks.isEmpty())
  }
}
