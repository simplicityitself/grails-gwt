package com.manning.gwtip.calculator.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.manning.gwtip.calculator.client.view.CalculatorWidget;


/**
 * GWT EntryPoint class which defines onModuleLoad
 * for simple GWT Calculator example.
 */
public class Calculator implements EntryPoint {
    public void onModuleLoad() {
        // add custom "CalculatorWidget" to the implicit RootPanel
        RootPanel.get().add(new CalculatorWidget("calculator"));
    }
}
