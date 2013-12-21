package com.manning.gwtip.calculator.client.model.operator;

import com.manning.gwtip.calculator.client.CalculatorConstants;
import com.manning.gwtip.calculator.client.model.CalculatorData;


public class OperatorInvert extends UnaryOperator {
    public OperatorInvert() {
        super(CalculatorConstants.INVERT);
    }

    public void operate(final CalculatorData data) {
        double displayDouble = Double.parseDouble(data.getDisplay());

        if (displayDouble < 0) {
            data.setDisplay(String.valueOf(Math.abs(displayDouble)));
        } else {
            data.setDisplay(String.valueOf(-displayDouble));
        }
    }
}
