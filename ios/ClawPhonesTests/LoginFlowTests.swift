import XCTest

// UI tests for ClawPhones iOS app
class LoginFlowTests: XCTestCase {
    let app = XCUIApplication()

    override func setUp() {
        super.setUp()
        // In UI tests it’s best to stop immediately when a failure occurs.
        continueAfterFailure = false
        // Launch the app for each test
        app.launch()
    }

    override func tearDown() {
        super.tearDown()
    }

    // Test login and (if needed) registration flow
    func testLoginRegistrationFlow() {
        // Try to find login fields
        let emailField = app.textFields["emailField"]
        let passwordField = app.secureTextFields["passwordField"]
        let loginButton = app.buttons["loginButton"]

        // If the login screen is present, perform a login; otherwise attempt registration
        if emailField.waitForExistence(timeout: 5) {
            emailField.tap()
            emailField.typeText("test@example.com")
            passwordField.tap()
            passwordField.typeText("Password123")
            loginButton.tap()
        } else {
            // Registration flow
            let signUpButton = app.buttons["signUpButton"]
            if signUpButton.waitForExistence(timeout: 5) {
                signUpButton.tap()
                let usernameField = app.textFields["usernameField"]
                let regEmailField = app.textFields["emailField"]
                let regPasswordField = app.secureTextFields["passwordField"]
                let confirmPasswordField = app.secureTextFields["confirmPasswordField"]
                let registerButton = app.buttons["registerButton"]

                if usernameField.exists {
                    usernameField.tap()
                    usernameField.typeText("TestUser")
                }
                if regEmailField.exists {
                    regEmailField.tap()
                    regEmailField.typeText("test@example.com")
                }
                if regPasswordField.exists {
                    regPasswordField.tap()
                    regPasswordField.typeText("Password123")
                }
                if confirmPasswordField.exists {
                    confirmPasswordField.tap()
                    confirmPasswordField.typeText("Password123")
                }
                if registerButton.exists {
                    registerButton.tap()
                }
            }
        }

        // After login/registration, user should see a conversations list
        let conversationsTable = app.tables["conversationsTable"]
        XCTAssertTrue(conversationsTable.waitForExistence(timeout: 10), "Conversations list should exist after login/registration")
    }

    // Additional helper to ensure login state for other tests
    func ensureLoggedIn() {
        // If not logged in, perform a minimal login with test account
        let emailExists = app.textFields["emailField"].exists
        if emailExists {
            app.textFields["emailField"].tap()
            app.textFields["emailField"].typeText("test@example.com")
            app.secureTextFields["passwordField"].tap()
            app.secureTextFields["passwordField"].typeText("Password123")
            app.buttons["loginButton"].tap()
        }
    }

}
