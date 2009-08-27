package org.codehaus.groovy.grails.plugins.gwt

import grails.test.GrailsUnitTestCase
import org.codehaus.groovy.grails.commons.DefaultGrailsServiceClass

/**
 * Created by IntelliJ IDEA.
 * User: pal20
 * Date: 02-Apr-2009
 * Time: 18:11:44
 * To change this template use File | Settings | File Templates.
 */
class DefaultGwtServiceInterfaceGeneratorTests extends GrailsUnitTestCase {
    void testIsGwtExposed() {
        def baseDir = new File("src/gwt")
        def testGenerator = new DefaultGwtServiceInterfaceGenerator(srcPath: baseDir.path)

        assertFalse testGenerator.isGwtExposed(TestServiceNoGwt)
        assertTrue testGenerator.isGwtExposed(TestServiceWithPackage)
    }

    void testWriteInterfaces() {
        def baseDir = new File("src/gwt")
        def testGenerator = new DefaultGwtServiceInterfaceGenerator(srcPath: baseDir.path)

        def output = new StringBuilder()
        def outputAsync = new StringBuilder()
        testGenerator.writeInterfaces(TestServiceWithPackage, "org.example.client", output, outputAsync)

        assertEquals """\
package org.example.client;

import com.google.gwt.user.client.rpc.RemoteService;

public interface TestServiceWithPackage extends RemoteService {
    void someMethod();
    java.util.List evaluateItems(java.lang.String arg0);
}""", output.toString().trim()

        assertEquals """\
package org.example.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TestServiceWithPackageAsync {
    void someMethod(AsyncCallback callback);
    void evaluateItems(java.lang.String arg0, AsyncCallback callback);
}""", outputAsync.toString().trim()
    }

    void testGetOutputDir() {
        def baseDir = new File("src/gwt")
        def testGenerator = new DefaultGwtServiceInterfaceGenerator(srcPath: baseDir.path)

        assertEquals baseDir, testGenerator.getOutputDir("")
        assertEquals new File(baseDir, "org/example/gwt"), testGenerator.getOutputDir("org.example.gwt")

        shouldFail(NullPointerException) {
            testGenerator.getOutputDir(null)
        }
    }

    void testGetMainFilename() {
        def baseDir = new File("src/gwt")
        def testGenerator = new DefaultGwtServiceInterfaceGenerator(srcPath: baseDir.path)

        assertEquals "String.java", testGenerator.getMainFilename(String)
        assertEquals "TestServiceWithPackage.java", testGenerator.getMainFilename(TestServiceWithPackage)

        shouldFail(NullPointerException) {
            testGenerator.getMainFilename(null)
        }
    }

    void testGetAsyncFilename() {
        def baseDir = new File("src/gwt")
        def testGenerator = new DefaultGwtServiceInterfaceGenerator(srcPath: baseDir.path)

        assertEquals "StringAsync.java", testGenerator.getAsyncFilename(String)
        assertEquals "TestServiceWithPackageAsync.java", testGenerator.getAsyncFilename(TestServiceWithPackage)

        shouldFail(NullPointerException) {
            testGenerator.getAsyncFilename(null)
        }
    }

    void testGetPackage() {
        def baseDir = new File("src/gwt")
        def testGenerator = new DefaultGwtServiceInterfaceGenerator(srcPath: baseDir.path)

        assertEquals "org.example.client", testGenerator.getPackage(TestServiceWithPackage)
        assertEquals "org.codehaus.groovy.grails.plugins.gwt.client", testGenerator.getPackage(TestServiceWithoutPackage)
        assertNull testGenerator.getPackage(TestServiceNoGwt)

        shouldFail(NullPointerException) {
            testGenerator.getPackage(null)
        }
    }

    void testGetDefaultPackage() {
        def baseDir = new File("src/gwt")
        def testGenerator = new DefaultGwtServiceInterfaceGenerator(srcPath: baseDir.path)

        assertEquals "java.lang.client", testGenerator.getDefaultPackage(String)
        assertEquals "org.codehaus.groovy.grails.plugins.gwt.client", testGenerator.getDefaultPackage(TestServiceWithoutPackage)

        shouldFail(NullPointerException) {
            testGenerator.getDefaultPackage(null)
        }
    }

    void testGetType() {
        def testGenerator = new DefaultGwtServiceInterfaceGenerator()

        assertEquals "java.lang.String", testGenerator.getType(String)
        assertEquals "byte[]", testGenerator.getType(byte[])
        assertEquals "java.lang.Integer[]", testGenerator.getType(Integer[])
    }
}

class TestServiceNoGwt {
    def anotherService
    String title = "Hello"
    boolean enabled

    void someMethod() {}
    List<String> evaluateItems(String name) { return [] }
}

class TestServiceWithoutPackage {
    static expose = [ "gwt" ]

    def anotherService
    String title = "Hello"
    boolean enabled

    void someMethod() {}
    List<String> evaluateItems(String name) { return [] }
}

class TestServiceWithPackage {
    static expose = [ "gwt:org.example.client" ]

    def anotherService
    String title = "Hello"
    boolean enabled

    void someMethod() {}
    List<String> evaluateItems(String name) { return [] }
}
