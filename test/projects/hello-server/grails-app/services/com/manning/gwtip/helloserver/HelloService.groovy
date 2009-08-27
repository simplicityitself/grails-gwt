package com.manning.gwtip.helloserver

import com.manning.gwtip.helloserver.client.Person

class HelloService implements com.manning.gwtip.helloserver.client.HelloService {
    def transactional = false

    static expose = [ "gwt:com.manning.gwtip.helloserver.client" ]

    String sayHello(Person p) {
        return "Hello " + p.name + ". How is the weather at " + p.address + "?"
    }
}
