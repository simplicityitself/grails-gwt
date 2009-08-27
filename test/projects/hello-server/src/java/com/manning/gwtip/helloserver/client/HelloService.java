package com.manning.gwtip.helloserver.client;

import com.google.gwt.user.client.rpc.RemoteService;


public interface HelloService extends RemoteService {
    public String sayHello(Person p);
}
