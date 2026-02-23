import XCTest

class ClawPhonesApp {
    let app: XCUIApplication
    
    init(app: XCUIApplication) {
        self.app = app
    }
    
    var loginScreen: LoginScreen {
        LoginScreen(app: app)
    }
    
    var registerScreen: RegisterScreen {
        RegisterScreen(app: app)
    }
    
    var conversationListScreen: ConversationListScreen {
        ConversationListScreen(app: app)
    }
    
    var chatScreen: ChatScreen {
        ChatScreen(app: app)
    }
}

class LoginScreen {
    let app: XCUIApplication
    
    init(app: XCUIApplication) {
        self.app = app
    }
    
    var emailTextField: XCUIElement {
        app.textFields["emailTextField"]
    }
    
    var passwordTextField: XCUIElement {
        app.secureTextFields["passwordTextField"]
    }
    
    var loginButton: XCUIElement {
        app.buttons["loginButton"]
    }
    
    var registerButton: XCUIElement {
        app.buttons["registerButton"]
    }
    
    var errorLabel: XCUIElement {
        app.staticTexts["errorLabel"]
    }
    
    var isVisible: Bool {
        loginButton.exists
    }
    
    func login(email: String, password: String) {
        emailTextField.tap()
        emailTextField.typeText(email)
        
        passwordTextField.tap()
        passwordTextField.typeText(password)
        
        loginButton.tap()
    }
}

class RegisterScreen {
    let app: XCUIApplication
    
    init(app: XCUIApplication) {
        self.app = app
    }
    
    var emailTextField: XCUIElement {
        app.textFields["emailTextField"]
    }
    
    var passwordTextField: XCUIElement {
        app.secureTextFields["passwordTextField"]
    }
    
    var confirmPasswordTextField: XCUIElement {
        app.secureTextFields["confirmPasswordTextField"]
    }
    
    var registerButton: XCUIElement {
        app.buttons["registerButton"]
    }
    
    var loginButton: XCUIElement {
        app.buttons["loginButton"]
    }
    
    var errorLabel: XCUIElement {
        app.staticTexts["errorLabel"]
    }
    
    var isVisible: Bool {
        registerButton.exists
    }
    
    func register(email: String, password: String, confirmPassword: String) {
        emailTextField.tap()
        emailTextField.typeText(email)
        
        passwordTextField.tap()
        passwordTextField.typeText(password)
        
        confirmPasswordTextField.tap()
        confirmPasswordTextField.typeText(confirmPassword)
        
        registerButton.tap()
    }
}

class ConversationListScreen {
    let app: XCUIApplication
    
    init(app: XCUIApplication) {
        self.app = app
    }
    
    var conversationTableView: XCUIElement {
        app.tables["conversationTableView"]
    }
    
    var conversationCells: XCUIElementQuery {
        app.cells["conversationCell"]
    }
    
    var newChatButton: XCUIElement {
        app.buttons["newChatButton"]
    }
    
    var settingsButton: XCUIElement {
        app.buttons["settingsButton"]
    }
    
    var emptyStateLabel: XCUIElement {
        app.staticTexts["emptyStateLabel"]
    }
    
    var isVisible: Bool {
        conversationTableView.exists
    }
    
    func selectConversation(at index: Int) {
        guard index < conversationCells.count else { return }
        conversationCells.element(boundBy: index).tap()
    }
}

class ChatScreen {
    let app: XCUIApplication
    
    init(app: XCUIApplication) {
        self.app = app
    }
    
    var messageTextField: XCUIElement {
        app.textFields["messageTextField"]
    }
    
    var sendButton: XCUIElement {
        app.buttons["sendButton"]
    }
    
    var messagesTableView: XCUIElement {
        app.tables["messagesTableView"]
    }
    
    var backButton: XCUIElement {
        app.buttons["backButton"]
    }
    
    var messageCells: XCUIElementQuery {
        app.cells["messageCell"]
    }
    
    var isVisible: Bool {
        messageTextField.exists
    }
    
    func sendMessage(_ message: String) {
        messageTextField.tap()
        messageTextField.typeText(message)
        sendButton.tap()
    }
}
