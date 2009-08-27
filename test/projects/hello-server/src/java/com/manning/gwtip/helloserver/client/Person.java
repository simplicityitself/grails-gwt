package com.manning.gwtip.helloserver.client;

import com.google.gwt.user.client.rpc.IsSerializable;


public class Person implements IsSerializable {
    public String name;
    public String address;

    public Person() {
        super();
    }

    public Person(String name, String address) {
        super();
        this.name = name;
        this.address = address;
    }
}
