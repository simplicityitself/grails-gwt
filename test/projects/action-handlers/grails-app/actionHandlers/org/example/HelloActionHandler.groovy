package org.example

import org.example.shared.HelloAction
import org.example.shared.HelloResponse

class HelloActionHandler {
    HelloResponse execute(HelloAction action) {
        return new HelloResponse("Hi there ${action.name}!")
    }
}
