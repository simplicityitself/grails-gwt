package com.manning.gwtip.calculator.client.model.operator;

import com.manning.gwtip.calculator.client.CalculatorConstants;
import com.manning.gwtip.calculator.client.model.CalculatorData;


public class OperatorPower extends BinaryOperator {
    public OperatorPower() {
        super(CalculatorConstants.POWER);
    }

    public void operate(final CalculatorData data) {
        data.setDisplay(String.valueOf(Math.pow(data.getBuffer(),
                    Double.parseDouble(data.getDisplay()))));
        data.setInitDisplay(true);
    }
}
