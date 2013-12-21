package com.manning.gwtip.calculator.client.view.button;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.manning.gwtip.calculator.client.CalculatorConstants;
import com.manning.gwtip.calculator.client.controller.CalculatorController;


public class ButtonDigit extends Button {
    public ButtonDigit(final CalculatorController controller, final String label) {
        super(label);
        this.addClickListener(new ClickListener() {
                public void onClick(Widget sender) {
                    controller.processDigit(label);
                }
            });
        this.setStyleName(CalculatorConstants.STYLE_BUTTON);
    }
}
