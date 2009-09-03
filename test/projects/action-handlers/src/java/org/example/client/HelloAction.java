package org.example.client;

import org.grails.plugins.gwt.client.Action;

public class HelloAction implements Action<HelloResponse> {
    private static final long serialVersionUID = 1L;

    private String name;

    private HelloAction() {
    }

    public HelloAction(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
