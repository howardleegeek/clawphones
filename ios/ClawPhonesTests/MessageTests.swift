import XCTest

class MessageTests: XCTestCase {
    
    var app: XCUIApplication!
    var clawPhonesApp: ClawPhonesApp!
    
    override func setUp() {
        super.setUp()
        continueAfterFailure = false
        
        app = XCUIApplication()
        app.launchArguments = ["--uitesting", "--loggedin", "true", "--withconversation"]
        app.launch()
        clawPhonesApp = ClawPhonesApp(app: app)
    }
    
    override func tearDown() {
        app.terminate()
        super.tearDown()
    }
    
    func testChatScreen_DisplaysCorrectly() {
        XCTAssertTrue(clawPhonesApp.chatScreen.isVisible)
        XCTAssertTrue(clawPhonesApp.chatScreen.messageTextField.exists)
        XCTAssertTrue(clawPhonesApp.chatScreen.sendButton.exists)
        XCTAssertTrue(clawPhonesApp.chatScreen.messagesTableView.exists)
        XCTAssertTrue(clawPhonesApp.chatScreen.backButton.exists)
    }
    
    func testSendMessage_TextMessage_Succeeds() {
        let initialMessageCount = clawPhonesApp.chatScreen.messageCells.count
        
        clawPhonesApp.chatScreen.sendMessage("Hello, World!")
        
        let newMessageCount = clawPhonesApp.chatScreen.messageCells.count
        XCTAssertEqual(newMessageCount, initialMessageCount + 1)
    }
    
    func testSendMessage_EmptyMessage_NotSent() {
        let initialMessageCount = clawPhonesApp.chatScreen.messageCells.count
        
        clawPhonesApp.chatScreen.sendMessage("")
        
        let newMessageCount = clawPhonesApp.chatScreen.messageCells.count
        XCTAssertEqual(newMessageCount, initialMessageCount)
    }
    
    func testSendMessage_LongMessage_Succeeds() {
        let longMessage = String(repeating: "A", count: 1000)
        let initialMessageCount = clawPhonesApp.chatScreen.messageCells.count
        
        clawPhonesApp.chatScreen.sendMessage(longMessage)
        
        let newMessageCount = clawPhonesApp.chatScreen.messageCells.count
        XCTAssertEqual(newMessageCount, initialMessageCount + 1)
    }
    
    func testSendMessage_SpecialCharacters_Succeeds() {
        let specialMessage = "Hello! @#$%^&*()"
        let initialMessageCount = clawPhonesApp.chatScreen.messageCells.count
        
        clawPhonesApp.chatScreen.sendMessage(specialMessage)
        
        let newMessageCount = clawPhonesApp.chatScreen.messageCells.count
        XCTAssertEqual(newMessageCount, initialMessageCount + 1)
    }
    
    func testSendMessage_Emoji_Succeeds() {
        let emojiMessage = "Hello 👋😊🎉"
        let initialMessageCount = clawPhonesApp.chatScreen.messageCells.count
        
        clawPhonesApp.chatScreen.sendMessage(emojiMessage)
        
        let newMessageCount = clawPhonesApp.chatScreen.messageCells.count
        XCTAssertEqual(newMessageCount, initialMessageCount + 1)
    }
    
    func testSendMessage_MultilineMessage_Succeeds() {
        let multilineMessage = "Line 1\nLine 2\nLine 3"
        let initialMessageCount = clawPhonesApp.chatScreen.messageCells.count
        
        clawPhonesApp.chatScreen.sendMessage(multilineMessage)
        
        let newMessageCount = clawPhonesApp.chatScreen.messageCells.count
        XCTAssertEqual(newMessageCount, initialMessageCount + 1)
    }
    
    func testChatScreen_BackButton_NavigatesBack() {
        clawPhonesApp.chatScreen.backButton.tap()
        
        XCTAssertTrue(clawPhonesApp.conversationListScreen.isVisible)
    }
    
    func testChatScreen_ReceiveMessage_DisplaysCorrectly() {
        let initialMessageCount = clawPhonesApp.chatScreen.messageCells.count
        
        app.buttons["simulateReceiveMessage"].tap()
        
        let newMessageCount = clawPhonesApp.chatScreen.messageCells.count
        XCTAssertEqual(newMessageCount, initialMessageCount + 1)
    }
    
    func testChatScreen_ScrollToOldMessages() {
        let messageCount = clawPhonesApp.chatScreen.messageCells.count
        guard messageCount > 5 else { return }
        
        clawPhonesApp.chatScreen.messagesTableView.swipeUp()
        
        XCTAssertTrue(clawPhonesApp.chatScreen.messagesTableView.exists)
    }
}
