import grails.test.GrailsUnitTestCase
import org.codehaus.groovy.grails.plugins.gwt.DefaultGwtServiceInterfaceGenerator
import org.codehaus.groovy.grails.commons.DefaultGrailsServiceClass

/**
 * Test case for {@link DefaultGwtServiceInterfaceGenerator} that sits
 * in the default package. It means we can test the getPackage() method
 * with services that are in the default package.
 */
class DefaultGwtServiceInterfaceGeneratorNoPackageTests extends GrailsUnitTestCase {
    void testGetPackage() {
        def baseDir = new File("src/gwt")
        def testGenerator = new DefaultGwtServiceInterfaceGenerator(srcPath: baseDir.path)

        assertEquals "org.example.client", testGenerator.getPackage(new DefaultGrailsServiceClass(TestServiceWithPackage))
        assertEquals "client", testGenerator.getPackage(new DefaultGrailsServiceClass(TestServiceWithoutPackage))
    }

    void testGetDefaultPackage() {
        def baseDir = new File("src/gwt")
        def testGenerator = new DefaultGwtServiceInterfaceGenerator(srcPath: baseDir.path)

        assertEquals "client", testGenerator.getDefaultPackage(TestServiceWithoutPackage)
    }
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
