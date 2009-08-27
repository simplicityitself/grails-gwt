package com.manning.gwtip.helloserver.client;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface HelloServiceAsync {
    public void sayHello(Person p, AsyncCallback callback);
}
