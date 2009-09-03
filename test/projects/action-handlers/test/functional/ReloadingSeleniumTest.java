import junit.framework.Test;
import junit.framework.TestSuite;

public class ReloadingSeleniumTest {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ReloadingTests.class);
        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
