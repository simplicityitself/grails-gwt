package com.manning.gwtip.calculator.client.view.button;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.manning.gwtip.calculator.client.CalculatorConstants;
import com.manning.gwtip.calculator.client.controller.CalculatorController;
import com.manning.gwtip.calculator.client.model.operator.AbstractOperator;


public class ButtonOperator extends Button {
    public ButtonOperator(final CalculatorController controller,
        final AbstractOperator op) {
        super(op.label);
        this.addClickListener(new ClickListener() {
                public void onClick(Widget sender) {
                    controller.processOperator(op);
                }
            });
        this.setStyleName(CalculatorConstants.STYLE_BUTTON);
    }
}
