package org.codehaus.groovy.grails.plugins.gwt;

/**
 * Created by IntelliJ IDEA.
 * User: pal20
 * Date: 02-Apr-2009
 * Time: 08:56:05
 * To change this template use File | Settings | File Templates.
 */
public interface GwtServiceInterfaceGenerator {
    boolean isGwtExposed(Class serviceClass);
    boolean getInterfacesExist(Class serviceClass);
    boolean getInterfacesExist(Class serviceClass, String packageName);
    void generateInterfaces(Class serviceClass);
    void generateInterfaces(Class serviceClass, String packageName);
}
