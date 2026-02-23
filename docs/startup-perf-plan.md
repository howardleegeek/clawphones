# Startup Performance Plan (Cold Start < 2s)

- Objective: Achieve cold-start time under 2 seconds, with lazy loading of non-critical modules and optimized launch screen.
- Scope: Android app under clawphones project. Modify only existing Android project files.
- Approach:
  1) Audit initializations in MainApplication.kt; move heavy work to on-demand paths.
  2) Introduce a minimal, lazy heavy module that is not initialized during onCreate.
  3) Use lightweight App Startup patterns where feasible (defer non-critical work).
  4) Remove any unnecessary initializations already present.
  5) Implement a simple performance test scaffold to measure cold start time.
- Acceptance Criteria:
  - Cold startup time < 2s
  - Non-critical modules are loaded lazily
  - Unnecessary initializations removed
  - Launch screen optimized (via best-practice splash handling)
- Tests:
  - Add a small instrumentation test (Android) to measure app startup time and verify it is under threshold.
  - If instrumentation tests are unavailable, use a console-based perf stub to validate lazy initializers.
- Notes:
  - Ensure no TODO/FIXME placeholders remain in the code.
  - Document any changes for reviewers.
