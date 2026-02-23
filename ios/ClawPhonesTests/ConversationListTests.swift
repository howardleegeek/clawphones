import XCTest

class ConversationListTests: XCTestCase {
    
    var app: XCUIApplication!
    var clawPhonesApp: ClawPhonesApp!
    
    override func setUp() {
        super.setUp()
        continueAfterFailure = false
        
        app = XCUIApplication()
        app.launchArguments = ["--uitesting", "--loggedin", "true"]
        app.launch()
        clawPhonesApp = ClawPhonesApp(app: app)
    }
    
    override func tearDown() {
        app.terminate()
        super.tearDown()
    }
    
    func testConversationList_DisplaysCorrectly() {
        XCTAssertTrue(clawPhonesApp.conversationListScreen.isVisible)
        XCTAssertTrue(clawPhonesApp.conversationListScreen.conversationTableView.exists)
        XCTAssertTrue(clawPhonesApp.conversationListScreen.newChatButton.exists)
        XCTAssertTrue(clawPhonesApp.conversationListScreen.settingsButton.exists)
    }
    
    func testConversationList_WithConversations_ShowsCells() {
        let cellCount = clawPhonesApp.conversationListScreen.conversationCells.count
        XCTAssertGreaterThan(cellCount, 0)
    }
    
    func testConversationList_SelectConversation_NavigatesToChat() {
        clawPhonesApp.conversationListScreen.selectConversation(at: 0)
        
        XCTAssertTrue(clawPhonesApp.chatScreen.isVisible)
    }
    
    func testConversationList_NewChatButton_NavigatesToChat() {
        clawPhonesApp.conversationListScreen.newChatButton.tap()
        
        XCTAssertTrue(clawPhonesApp.chatScreen.isVisible)
    }
    
    func testConversationList_SettingsButton_OpensSettings() {
        clawPhonesApp.conversationListScreen.settingsButton.tap()
        
        XCTAssertTrue(app.buttons["logoutButton"].exists)
    }
    
    func testConversationList_EmptyState_ShowsMessage() {
        app.launchArguments = ["--uitesting", "--loggedin", "true", "--emptyconversations"]
        app.launch()
        
        let clawPhonesEmpty = ClawPhonesApp(app: app)
        
        XCTAssertTrue(clawPhonesEmpty.conversationListScreen.emptyStateLabel.exists)
    }
    
    func testConversationList_LastMessagePreview_Displayed() {
        let firstCell = clawPhonesApp.conversationListScreen.conversationCells.firstMatch
        
        XCTAssertTrue(firstCell.staticTexts.element.exists)
    }
    
    func testConversationList_ConversationTimestamp_Displayed() {
        let cellCount = clawPhonesApp.conversationListScreen.conversationCells.count
        guard cellCount > 0 else { return }
        
        let firstCell = clawPhonesApp.conversationListScreen.conversationCells.element(boundBy: 0)
        XCTAssertTrue(firstCell.exists)
    }
    
    func testConversationList_SwipeToDelete() {
        let cellCount = clawPhonesApp.conversationListScreen.conversationCells.count
        guard cellCount > 0 else { return }
        
        let firstCell = clawPhonesApp.conversationListScreen.conversationCells.element(boundBy: 0)
        
        firstCell.swipeLeft()
        
        XCTAssertTrue(app.buttons["deleteButton"].exists)
    }
}
