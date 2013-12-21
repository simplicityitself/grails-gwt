package com.manning.gwtip.calculator.client.controller;

import com.manning.gwtip.calculator.client.CalculatorConstants;
import com.manning.gwtip.calculator.client.model.CalculatorData;
import com.manning.gwtip.calculator.client.model.operator.AbstractOperator;
import com.manning.gwtip.calculator.client.model.operator.BinaryOperator;
import com.manning.gwtip.calculator.client.model.operator.UnaryOperator;


/**
 * Example Controller for GWT Calculator demo.
 * 
 */
public class CalculatorController {
    CalculatorData data;
    double prevBuffer;

    /**
     * Ctor.
     */
    public CalculatorController(final CalculatorData data) {
        this.data = data;
    }

    /**
     * Handle the clear task.
     */
    public void processClear() {
        data.clear();
        data.setLastOperator(null);
    }

    /**
     * Handle the equals task.
     */
    public void processEquals() {
        if (data.getLastOperator() != null) {
            if (!data.isLastOpEquals()) {
                prevBuffer = Double.parseDouble(data.getDisplay());
            }

            data.getLastOperator().operate(data);
            data.setBuffer(prevBuffer);
            data.setLastOpEquals(true);

        }
    }

    /**
     * Process operator - based on status of lastOperator, invoke operators.
     *
     * @param op
     */
    public void processOperator(final AbstractOperator op) {
        if (op instanceof BinaryOperator) {
            if ((data.getLastOperator() == null) || (data.isLastOpEquals())) {
                data.setBuffer(Double.parseDouble(data.getDisplay()));
                data.setInitDisplay(true);
            } else {
                data.getLastOperator().operate(data);
            }
            data.setLastOperator(op);
        } else if (op instanceof UnaryOperator) {
            op.operate(data);
        }

        data.setLastOpEquals(false);
    }

    /**
     * Process digit, start the display, append to the display, etc.
     *
     * @param s
     */
    public void processDigit(final String s) {
        if (data.isLastOpEquals()) {
            data.setLastOperator(null);
        }

        if (data.isInitDisplay()) {
            if (data.isLastOpEquals()) {
                data.setBuffer(0.0);
            } else {
                data.setBuffer(Double.parseDouble(data.getDisplay()));
            }

            data.setDisplay(s);
            data.setInitDisplay(false);
        } else {
            if (data.getDisplay().indexOf(CalculatorConstants.POINT) == -1) {
                data.setDisplay(data.getDisplay() + s);
            } else if (!s.equals(CalculatorConstants.POINT)) {
                data.setDisplay(data.getDisplay() + s);
            }
        }
        data.setLastOpEquals(false);
    }
}
