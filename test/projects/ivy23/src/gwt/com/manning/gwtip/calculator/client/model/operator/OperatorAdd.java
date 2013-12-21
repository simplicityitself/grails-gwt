package com.manning.gwtip.calculator.client.model.operator;

import com.manning.gwtip.calculator.client.CalculatorConstants;
import com.manning.gwtip.calculator.client.model.CalculatorData;


public class OperatorAdd extends BinaryOperator {
    public OperatorAdd() {
        super(CalculatorConstants.ADD);
    }

    public void operate(final CalculatorData data) {
        data.setDisplay(String.valueOf(data.getBuffer() +
                Double.parseDouble(data.getDisplay())));
        data.setInitDisplay(true);
    }
}
