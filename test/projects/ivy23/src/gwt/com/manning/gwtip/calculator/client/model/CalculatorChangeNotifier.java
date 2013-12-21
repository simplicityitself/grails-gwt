package com.manning.gwtip.calculator.client.model;


/**
 * Interface for change notifier.
 *
 * @author ccollins
 *
 */
public interface CalculatorChangeNotifier {
    public void addChangeListener(final CalculatorChangeListener listener);
}
