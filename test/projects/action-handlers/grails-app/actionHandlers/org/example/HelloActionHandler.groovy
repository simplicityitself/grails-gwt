package org.example

import org.example.client.HelloAction
import org.example.client.HelloResponse

class HelloActionHandler {
    HelloResponse execute(HelloAction action) {
        return new HelloResponse("Hi there ${action.name}!")
    }
}
