import XCTest

class LoginRegisterTests: XCTestCase {
    
    var app: XCUIApplication!
    var clawPhonesApp: ClawPhonesApp!
    
    override func setUp() {
        super.setUp()
        continueAfterFailure = false
        
        app = XCUIApplication()
        app.launchArguments = ["--uitesting", "true"]
        app.launch()
        clawPhonesApp = ClawPhonesApp(app: app)
    }
    
    override func tearDown() {
        app.terminate()
        super.tearDown()
    }
    
    func testLoginScreen_DisplaysCorrectly() {
        XCTAssertTrue(clawPhonesApp.loginScreen.isVisible)
        XCTAssertTrue(clawPhonesApp.loginScreen.emailTextField.exists)
        XCTAssertTrue(clawPhonesApp.loginScreen.passwordTextField.exists)
        XCTAssertTrue(clawPhonesApp.loginScreen.loginButton.exists)
        XCTAssertTrue(clawPhonesApp.loginScreen.registerButton.exists)
    }
    
    func testLogin_WithValidCredentials_Succeeds() {
        clawPhonesApp.loginScreen.login(email: "test@example.com", password: "password123")
        
        XCTAssertTrue(clawPhonesApp.conversationListScreen.isVisible)
    }
    
    func testLogin_WithInvalidEmail_ShowsError() {
        clawPhonesApp.loginScreen.login(email: "invalid", password: "password123")
        
        XCTAssertTrue(clawPhonesApp.loginScreen.errorLabel.exists)
    }
    
    func testLogin_WithEmptyFields_ShowsError() {
        clawPhonesApp.loginScreen.login(email: "", password: "")
        
        XCTAssertTrue(clawPhonesApp.loginScreen.errorLabel.exists)
    }
    
    func testNavigateToRegisterScreen() {
        clawPhonesApp.loginScreen.registerButton.tap()
        
        XCTAssertTrue(clawPhonesApp.registerScreen.isVisible)
    }
    
    func testRegisterScreen_DisplaysCorrectly() {
        clawPhonesApp.loginScreen.registerButton.tap()
        
        XCTAssertTrue(clawPhonesApp.registerScreen.isVisible)
        XCTAssertTrue(clawPhonesApp.registerScreen.emailTextField.exists)
        XCTAssertTrue(clawPhonesApp.registerScreen.passwordTextField.exists)
        XCTAssertTrue(clawPhonesApp.registerScreen.confirmPasswordTextField.exists)
        XCTAssertTrue(clawPhonesApp.registerScreen.registerButton.exists)
        XCTAssertTrue(clawPhonesApp.registerScreen.loginButton.exists)
    }
    
    func testRegister_WithValidData_Succeeds() {
        clawPhonesApp.loginScreen.registerButton.tap()
        
        clawPhonesApp.registerScreen.register(
            email: "newuser@example.com",
            password: "password123",
            confirmPassword: "password123"
        )
        
        XCTAssertTrue(clawPhonesApp.conversationListScreen.isVisible)
    }
    
    func testRegister_WithMismatchedPasswords_ShowsError() {
        clawPhonesApp.loginScreen.registerButton.tap()
        
        clawPhonesApp.registerScreen.register(
            email: "newuser@example.com",
            password: "password123",
            confirmPassword: "differentpassword"
        )
        
        XCTAssertTrue(clawPhonesApp.registerScreen.errorLabel.exists)
    }
    
    func testRegister_WithInvalidEmail_ShowsError() {
        clawPhonesApp.loginScreen.registerButton.tap()
        
        clawPhonesApp.registerScreen.register(
            email: "invalid",
            password: "password123",
            confirmPassword: "password123"
        )
        
        XCTAssertTrue(clawPhonesApp.registerScreen.errorLabel.exists)
    }
    
    func testRegister_WithEmptyFields_ShowsError() {
        clawPhonesApp.loginScreen.registerButton.tap()
        
        clawPhonesApp.registerScreen.register(
            email: "",
            password: "",
            confirmPassword: ""
        )
        
        XCTAssertTrue(clawPhonesApp.registerScreen.errorLabel.exists)
    }
    
    func testNavigateBackToLogin() {
        clawPhonesApp.loginScreen.registerButton.tap()
        XCTAssertTrue(clawPhonesApp.registerScreen.isVisible)
        
        clawPhonesApp.registerScreen.loginButton.tap()
        XCTAssertTrue(clawPhonesApp.loginScreen.isVisible)
    }
}
