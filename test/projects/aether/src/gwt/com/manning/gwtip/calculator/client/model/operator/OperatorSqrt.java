package com.manning.gwtip.calculator.client.model.operator;

import com.manning.gwtip.calculator.client.CalculatorConstants;
import com.manning.gwtip.calculator.client.model.CalculatorData;


public class OperatorSqrt extends UnaryOperator {
    public OperatorSqrt() {
        super(CalculatorConstants.SQRT);
    }

    public void operate(final CalculatorData data) {
        data.setDisplay(String.valueOf(Math.sqrt(Double.parseDouble(
                        data.getDisplay()))));
    }
}
