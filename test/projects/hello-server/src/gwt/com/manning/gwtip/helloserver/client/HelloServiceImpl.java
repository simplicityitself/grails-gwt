package com.manning.gwtip.helloserver.client;

import com.google.gwt.core.client.GWT;

import com.manning.gwtip.helloserver.client.i18n.Messages;


public class HelloServiceImpl implements HelloService {
    Messages messages;

    public HelloServiceImpl() {
        super();
        messages = (Messages) GWT.create(Messages.class);
    }

    public String sayHello(Person p) {
        return messages.helloMessage(p.name, p.address);
    }
}
