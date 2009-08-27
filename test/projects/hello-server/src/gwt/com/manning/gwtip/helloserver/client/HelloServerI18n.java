package com.manning.gwtip.helloserver.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.manning.gwtip.helloserver.client.i18n.Constants;


public class HelloServerI18n implements com.google.gwt.core.client.EntryPoint {
    private static final String HISTORY_DELIM = ":";
    private static boolean ignoreEvent = false;
    
    // replace the HelloServiceAsync with HelloServiceImpl
    private HelloServiceImpl service;
    private TextBox name = new TextBox();
    private TextBox address = new TextBox();
    private Label response = new Label();
    private Constants constants;

    // remove the AsyncCallback stuff
    private HistoryListener historyListener = new HistoryListener() {
            public void onHistoryChanged(String historyToken) {
                if (!ignoreEvent) {
                    String[] nameAndAddress = historyToken.split(HISTORY_DELIM);

                    if (nameAndAddress.length == 2) {
                        // non async service.sayHello and response.setText
                        String result = service.sayHello(new Person(
                                    nameAndAddress[0], nameAndAddress[1]));
                        response.setText(result);
                    }
                }
            }
        };

    public HelloServerI18n() {
        super();
    }

    public void onModuleLoad() {
        // remove async service endpoint stuff
        service = new HelloServiceImpl();

        constants = (Constants) GWT.create(Constants.class);

        RootPanel root = RootPanel.get();
        root.add(new Label(constants.nameLabel()));
        root.add(name);
        root.add(new Label(constants.addressLabel()));
        root.add(address);

        Button button = new Button("Hello!",
                new ClickListener() {
                    public void onClick(Widget sender) {
                        // non async service.sayHello and response.setText
                        String result = service.sayHello(new Person(
                                    name.getText(), address.getText()));
                        response.setText(result);
                        ignoreEvent = true;
                        History.newItem(name.getText() + HISTORY_DELIM +
                            address.getText());
                        ignoreEvent = false;
                    }
                });

        root.add(button);
        root.add(response);
        root.add(new Hyperlink("John", "John Doe:Anytown, NA, 55555"));
        root.add(new Hyperlink("Jane", "Jane Doe:Nowhere, XX, 44444"));
        History.addHistoryListener(historyListener);
    }
}
