package com.manning.gwtip.calculator.client.view;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.manning.gwtip.calculator.client.CalculatorConstants;
import com.manning.gwtip.calculator.client.controller.CalculatorController;
import com.manning.gwtip.calculator.client.model.CalculatorChangeListener;
import com.manning.gwtip.calculator.client.model.CalculatorData;
import com.manning.gwtip.calculator.client.model.operator.OperatorAdd;
import com.manning.gwtip.calculator.client.model.operator.OperatorDivide;
import com.manning.gwtip.calculator.client.model.operator.OperatorInvert;
import com.manning.gwtip.calculator.client.model.operator.OperatorMultiply;
import com.manning.gwtip.calculator.client.model.operator.OperatorPower;
import com.manning.gwtip.calculator.client.model.operator.OperatorSqrt;
import com.manning.gwtip.calculator.client.model.operator.OperatorSubtract;
import com.manning.gwtip.calculator.client.view.button.ButtonDigit;
import com.manning.gwtip.calculator.client.view.button.ButtonOperator;


/**
 * Example GWT CalculatorWidget - the View for the calculator.
 * 
 * @author ccollins
 */
public class CalculatorWidget extends VerticalPanel {
    private TextBox display;

    /**
     * GWT CalculatorWidget ctor.
     *
     * @param title
     */
    public CalculatorWidget(final String title) {
        super();

        // instantiate the model
        final CalculatorData data = new CalculatorData();

        // instantiate the controller
        final CalculatorController controller = new CalculatorController(data);

        // Panel for components
        VerticalPanel p = new VerticalPanel();
        p.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        p.setStyleName(CalculatorConstants.STYLE_PANEL);
        
        // Grid for calculator layout
        Grid g = new Grid(4, 5);
        g.setStyleName(CalculatorConstants.STYLE_GRID);

        // put the digits in the grid
        final Button zero = new ButtonDigit(controller, "0");
        g.setWidget(3, 0, zero);

        final Button one = new ButtonDigit(controller, "1");
        g.setWidget(2, 0, one);

        final Button two = new ButtonDigit(controller, "2");
        g.setWidget(2, 1, two);

        final Button three = new ButtonDigit(controller, "3");
        g.setWidget(2, 2, three);

        final Button four = new ButtonDigit(controller, "4");
        g.setWidget(1, 0, four);

        final Button five = new ButtonDigit(controller, "5");
        g.setWidget(1, 1, five);

        final Button six = new ButtonDigit(controller, "6");
        g.setWidget(1, 2, six);

        final Button seven = new ButtonDigit(controller, "7");
        g.setWidget(0, 0, seven);

        final Button eight = new ButtonDigit(controller, "8");
        g.setWidget(0, 1, eight);

        final Button nine = new ButtonDigit(controller, "9");
        g.setWidget(0, 2, nine);

        final Button point = new ButtonDigit(controller, ".");
        g.setWidget(3, 2, point);

        // put the operators in the grid        
        final Button divide = new ButtonOperator(controller,
                new OperatorDivide());
        g.setWidget(0, 3, divide);

        final Button multiply = new ButtonOperator(controller,
                new OperatorMultiply());
        g.setWidget(1, 3, multiply);

        final Button subtract = new ButtonOperator(controller,
                new OperatorSubtract());
        g.setWidget(2, 3, subtract);

        final Button add = new ButtonOperator(controller, new OperatorAdd());
        g.setWidget(3, 3, add);

        final Button sqrt = new ButtonOperator(controller, new OperatorSqrt());
        g.setWidget(0, 4, sqrt);

        final Button power = new ButtonOperator(controller, new OperatorPower());
        g.setWidget(1, 4, power);

        final Button invert = new ButtonOperator(controller,
                new OperatorInvert());
        g.setWidget(3, 1, invert);

        // add special button for clear (handled directly by controller)
        final Button clear = new Button(CalculatorConstants.CLEAR);
        clear.addClickListener(new ClickListener() {
                public void onClick(Widget sender) {
                    controller.processClear();
                }
            });
        clear.setStyleName(CalculatorConstants.STYLE_BUTTON);
        g.setWidget(2, 4, clear);

        // add special button for equals (handled directly by controller)
        final Button equals = new Button(CalculatorConstants.EQUALS);
        equals.addClickListener(new ClickListener() {
                public void onClick(Widget sender) {
                    controller.processEquals();
                }
            });
        equals.setStyleName(CalculatorConstants.STYLE_BUTTON);
        g.setWidget(3, 4, equals);

        // initialize the display textBox for results
        display = new TextBox();

        // add a view change listener to the "data" model object 
        // and if the model changes, update the view
        data.addChangeListener(new CalculatorChangeListener() {
                public void onChange(CalculatorData data) {
                    display.setText(String.valueOf(data.getDisplay()));
                }
            });
        display.setText("0");
        display.setTextAlignment(TextBox.ALIGN_RIGHT);

        // add the textBox and the grid to the panel, and the panel to the widget
        p.add(display);
        p.add(g);
        this.add(p);       
    }
}
