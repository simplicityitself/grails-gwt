package com.manning.gwtip.calculator.client.model;

import com.manning.gwtip.calculator.client.model.operator.AbstractOperator;


/**
 * Calculator data - the data half of the model (the other half being logic and operations, the operators).
 * 
 */
public class CalculatorData implements CalculatorChangeNotifier {
    private String display; 
    private double buffer; 
    private boolean initDisplay = true; 
    private boolean lastOpEquals; 
    private AbstractOperator lastOperator; 
    private CalculatorChangeListener listener;

    public CalculatorData() {
        this.display = "0";
    }

    /**
     * Add change listener so that view components can register and update
     * themselves when the model changes.
     */
    public void addChangeListener(final CalculatorChangeListener listener) {
        this.listener = listener;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("***CalculatorData***\n");
        sb.append("display = " + this.display + "\n");
        sb.append("buffer = " + this.buffer + "\n");

        return sb.toString();
    }

    public void clear() {
        this.display = "0";
        this.buffer = 0.0;
        this.initDisplay = true;
        this.lastOpEquals = false;
        listener.onChange(this);
    }

    public double getBuffer() {
        return buffer;
    }

    public void setBuffer(double buffer) {
        this.buffer = buffer;
        listener.onChange(this);
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
        listener.onChange(this);
    }

    public boolean isInitDisplay() {
        return initDisplay;
    }

    public void setInitDisplay(boolean initDisplay) {
        this.initDisplay = initDisplay;
        listener.onChange(this);
    }

    public AbstractOperator getLastOperator()
    {
        return lastOperator;
    }
    
    public void setLastOperator(AbstractOperator op)
    {
        this.lastOperator = op;
    }
    
    public boolean isLastOpEquals() {
        return lastOpEquals;
    }

    public void setLastOpEquals(boolean lastOpEquals) {
        this.lastOpEquals = lastOpEquals;
        listener.onChange(this);
    }
    
    
}
