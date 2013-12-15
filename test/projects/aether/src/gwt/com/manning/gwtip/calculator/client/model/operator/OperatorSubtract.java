package com.manning.gwtip.calculator.client.model.operator;

import com.manning.gwtip.calculator.client.CalculatorConstants;
import com.manning.gwtip.calculator.client.model.CalculatorData;


public class OperatorSubtract extends BinaryOperator {
    public OperatorSubtract() {
        super(CalculatorConstants.SUBTRACT);
    }

    public void operate(final CalculatorData data) {
        // subtraction and division are noncommutative, 
        // if equals is pressed multiple times back to back we have to swap the order
        if (data.isLastOpEquals()) {
            data.setDisplay(String.valueOf(Double.parseDouble(data.getDisplay()) -
                    data.getBuffer()));
        } else {
            data.setDisplay(String.valueOf(data.getBuffer() -
                    Double.parseDouble(data.getDisplay())));
        }

        data.setInitDisplay(true);
    }
}
