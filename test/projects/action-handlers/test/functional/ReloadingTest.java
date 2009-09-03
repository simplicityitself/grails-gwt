package com.example.tests;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;

public class ReloadingTest extends SeleneseTestCase {
    public void setUp() throws Exception {
        setUp("http://localhost:8080/action-handlers/", "*chrome");
    }

    public void testReloading() throws Exception {
        selenium.open("/action-handlers/");
        selenium.type("recipient", "Peter");
        selenium.click("//button[@type='button']");
        assertTrue(selenium.isTextPresent(""));
        selenium.click("link=Change greeting");
        selenium.waitForPageToLoad("30000");
        selenium.type("recipient", "Peter");
        selenium.click("//button[@type='button']");
        assertTrue(selenium.isTextPresent(""));
        selenium.click("link=Change greeting");
        selenium.waitForPageToLoad("30000");
    }
}
