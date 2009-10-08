package org.example.shared;

import grails.plugins.gwt.shared.Action;

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
