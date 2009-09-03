package org.example

class ChangeController {
    def index = {
        // Get the contents of the current action handler.
        def handlerFile = new File("grails-app/actionHandlers/org/example/HelloActionHandler.groovy")
        def code = handlerFile.text

        // Are we currently "Hi there..." or "Welcome..."?
        if (code.contains("Hi there")) {
            code = code.replace("Hi there", "Welcome")
        }
        else {
            code = code.replace("Welcome", "Hi there")
        }

        // The greeting has now been changed, so write the new version
        // to the file.
        handlerFile.text = code

        // Wait for the change to trigger.
        Thread.sleep(5000)

        // Go back to the home page.
        redirect(uri: "/")
    }
}
