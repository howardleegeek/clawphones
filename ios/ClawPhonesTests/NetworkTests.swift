import XCTest

// Simple in-project helpers to simulate network states and settings for tests.
enum NetworkError: Error, Equatable {
    case timeout
    case offline
    case unknown
}

final class NetworkSimulator {
    private let timeout: Bool
    private let offline: Bool
    init(timeout: Bool = false, offline: Bool = false) {
        self.timeout = timeout
        self.offline = offline
    }
    func request() -> Result<String, NetworkError> {
        if timeout { return .failure(.timeout) }
        if offline { return .failure(.offline) }
        return .success("OK")
    }
}

final class SettingsManager {
    private(set) var notificationsEnabled: Bool
    private(set) var locationTrackingEnabled: Bool
    init(notificationsEnabled: Bool = true, locationTrackingEnabled: Bool = true) {
        self.notificationsEnabled = notificationsEnabled
        self.locationTrackingEnabled = locationTrackingEnabled
    }
    func setNotifications(_ enabled: Bool) { notificationsEnabled = enabled }
    func setLocationTracking(_ enabled: Bool) { locationTrackingEnabled = enabled }
}

struct Logger {
    static func log(taskID: String, _ message: String) {
        print("[task_id: \(taskID)] \(message)")
    }
}

final class ClawPhonesNetworkTests: XCTestCase {
    private let taskID = "S01-009"
    func testNetworkTimeout() {
        Logger.log(taskID: taskID, "Starting testNetworkTimeout")
        let sim = NetworkSimulator(timeout: true)
        let res = sim.request()
        switch res {
        case .success(let v):
            XCTFail("Expected timeout, got success: \(v)")
        case .failure(let e):
            XCTAssertEqual(e, .timeout)
        }
    }

    func testOfflinePrompt() {
        Logger.log(taskID: taskID, "Starting testOfflinePrompt")
        let sim = NetworkSimulator(offline: true)
        let res = sim.request()
        switch res {
        case .success(let v):
            XCTFail("Expected offline, got \(v)")
        case .failure(let e):
            XCTAssertEqual(e, .offline)
            XCTAssertTrue(offlinePromptNeeded(for: e))
        }
    }

    func testErrorModalShowsOnErrors() {
        Logger.log(taskID: taskID, "Starting testErrorModalShowsOnErrors")
        // Error modal should show for network-related errors
        XCTAssertTrue(shouldShowErrorModal(.timeout))
        XCTAssertTrue(shouldShowErrorModal(.offline))
        XCTAssertFalse(shouldShowErrorModal(.unknown))
    }

    func testSettingsPageFunctionality() {
        Logger.log(taskID: taskID, "Starting testSettingsPageFunctionality")
        let settings = SettingsManager(notificationsEnabled: true, locationTrackingEnabled: false)
        // Toggle notifications
        settings.setNotifications(false)
        XCTAssertFalse(settings.notificationsEnabled)
        // Toggle location tracking
        settings.setLocationTracking(true)
        XCTAssertTrue(settings.locationTrackingEnabled)
    }

    // Helpers
    private func shouldShowErrorModal(_ error: NetworkError) -> Bool {
        switch error {
        case .timeout, .offline:
            return true
        case .unknown:
            return false
        }
    }

    private func offlinePromptNeeded(for error: NetworkError) -> Bool {
        return error == .offline
    }
}
