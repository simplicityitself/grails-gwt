package org.example.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import grails.plugins.gwt.client.GwtActionService;
import grails.plugins.gwt.client.GwtActionServiceAsync;

import org.example.shared.HelloAction;
import org.example.shared.HelloResponse;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Main implements EntryPoint {
    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        final VerticalPanel hostPanel = new VerticalPanel();
        
        final FlowPanel mainPanel = new FlowPanel();
        final TextBox nameField = new TextBox();
        nameField.setName("recipient");
        mainPanel.add(nameField);
        
        final FlowPanel responsePanel = new FlowPanel();
        final Button helloButton = new Button("Say hello");
        helloButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GwtActionServiceAsync service = GWT.create(GwtActionService.class);
                ((ServiceDefTarget) service).setServiceEntryPoint(GWT.getModuleBaseURL() + "rpc");
                service.execute(new HelloAction(nameField.getText()), new AsyncCallback<HelloResponse>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Failed to send hello", caught);
                    }

                    @Override
                    public void onSuccess(HelloResponse result) {
                        responsePanel.clear();
                        responsePanel.add(new HTML(result.getResponse()));
                    }
                });
            }
        });
        mainPanel.add(helloButton);
        
        hostPanel.add(mainPanel);
        hostPanel.add(responsePanel);
        RootPanel.get().add(hostPanel);
    }
}
