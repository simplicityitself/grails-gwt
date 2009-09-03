class ReloadingFunctionalTests extends functionaltestplugin.FunctionalTestCase {
    void testSomeWebsiteFeature() {
        // Here call get(uri) or post(uri) to start the session
        // and then use the custom assertXXXX calls etc to check the response
        //
        // get('/something')
        // assertStatus 200
        // assertContentContains 'the expected text'
        get "/"
        Thread.sleep(5000)

        // Enter a name into the text box.
        def inputBox = page.getElementByName("recipient")
        inputBox.valueAttribute = "Peter"

        // Now click the button.
        page.body.getHtmlElementsByTagName("button")[0].click()

        // Sleep for a bit to allow the window to update.
        Thread.sleep(10000)

        // Now check the content.
        assertContentContains "Hi there Peter!"
    }
}
