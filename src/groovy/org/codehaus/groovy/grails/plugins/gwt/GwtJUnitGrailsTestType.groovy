/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.plugins.gwt


import org.codehaus.groovy.grails.test.GrailsTestTypeResult
import org.codehaus.groovy.grails.test.support.GrailsTestTypeSupport
import org.codehaus.groovy.grails.test.event.GrailsTestEventPublisher


import java.lang.reflect.Modifier

import junit.framework.TestSuite

import org.codehaus.groovy.grails.plugins.gwt.XMLJUnitResultFormatter



/**
 * An {@code GrailsTestType} for GWT unit tests.
 * @author Predrag Knezevic (pedjak@gmail.com)
 */
class GwtJUnitGrailsTestType extends GrailsTestTypeSupport {

    static final SUFFIXES = ["Test", "Tests"].asImmutable()
    static final SUITE_NAME = "GWTTests"
    
    private def classLoader 
        
    private File testsFile
    private File gwtTmpTestsDir
    private File allTestsSrcFile
    
    def classPath
    
    def testClassesDir
    
    private boolean gwtProdTestType
    
    GwtJUnitGrailsTestType(String name, String sourceDirectory) {
        super(name, sourceDirectory)
        
    }

    
    protected List<String> getTestSuffixes() { SUFFIXES }

    protected List<String> getTestExtensions() {
        ["java"]
    }

    protected int doPrepare() {
        gwtProdTestType = name == buildBinding.gwtProdTestTypeName
        def testClasses = getTestClasses()
        if (testClasses) {
            def suite = new TestSuite("GWT TestSuite")
            gwtTmpTestsDir = File.createTempFile("gwt", "tests")
            gwtTmpTestsDir.delete()
            gwtTmpTestsDir.mkdirs()
            
            testsFile = new File(gwtTmpTestsDir, "tests.txt")
            testsFile << "${SUITE_NAME},${buildBinding.testReportsDir.absolutePath},TEST-unit-${name}-${SUITE_NAME}\n"
            testsFile.deleteOnExit()
            
            // generate test suite src            
            allTestsSrcFile = new File(gwtTmpTestsDir, "${SUITE_NAME}.java")
            
            allTestsSrcFile << """
            
public class ${SUITE_NAME} extends junit.framework.TestCase {

    public static junit.framework.Test suite() {
        com.google.gwt.junit.tools.GWTTestSuite suite = new com.google.gwt.junit.tools.GWTTestSuite("GWT Tests");
                
            """
            testClasses.each { tc ->
                suite.addTestSuite(tc)
                buildBinding.ant.echo (message: "Found tests in ${tc.name}")
                allTestsSrcFile << "suite.addTestSuite(${tc.name}.class);\n"
            }
            
            allTestsSrcFile << """
return suite;
    }
}                
            """
            // compile the generated class
            buildBinding.ant.javac(destdir: testClassesDir.absolutePath,
                                    srcdir: gwtTmpTestsDir.absolutePath,
                                    encoding:"UTF-8", debug: "yes") {
            
                classpath() {
                    path(refid:"grails.test.classpath")
                    buildBinding.gwtDependencies.each { dep ->
                        pathElement(location: dep.absolutePath)
                    }
                }                        
            }
                
            allTestsSrcFile.deleteOnExit()
            
            suite.testCount()
        }
        else {
            0
        }
    }

    void cleanup() {
        gwtTmpTestsDir.deleteDir()
    }
    
    protected getTestClasses() {
        def classes = []
        eachSourceFile { testTargetPattern, sourceFile ->
            def testClass = sourceFileToClass(sourceFile)
            if (!Modifier.isAbstract(testClass.modifiers)) {
                classes << testClass
            }
        }
        classes
    }

    protected ClassLoader getTestClassLoader() {
        if (!classLoader) {
            testClassesDir = new File(buildBinding.grailsSettings.testClassesDir, relativeSourcePath) 
            classPath = [   testClassesDir,
                            buildBinding.grailsSettings.classesDir,
                            new File(buildBinding.testSourceDir, relativeSourcePath),
                            new File(buildBinding.gwtSrcPath),
                            new File(buildBinding.grailsSrcPath),
                            new File(buildBinding.gwtPluginDir, buildBinding.gwtSrcPath),                           
                            new File(buildBinding.gwtPluginDir, buildBinding.grailsSrcPath)
                            ] as Set
            // GWT
            new File(buildBinding.gwtHome).eachFileMatch(~/^gwt.+\.jar$/) { f ->
                classPath << f
            }
            if (compiledClassesDir) {
                classPath << compiledClassesDir
            }
            // JUNIT
            classPath.addAll(buildBinding.grailsSettings.testDependencies.findAll { it.name =~ /^junit/ })
            
            if (buildBinding.grailsSettings.metaClass.hasProperty(buildBinding.grailsSettings, "pluginClassesDir")) {
                classPath << buildBinding.grailsSettings.pluginClassesDir
            }
            
            // and all other GWT dependencies
            classPath.addAll(buildBinding.gwtDependencies)
            classLoader = new URLClassLoader(classPath*.toURI()*.toURL() as URL[])            
        }
        classLoader
    }

    
    protected GrailsTestTypeResult doRun(GrailsTestEventPublisher eventPublisher) {
        // do nothing here
    }

    @Override
    GrailsTestTypeResult run(GrailsTestEventPublisher eventPublisher) {

        // run tests using ant's junit task        
        buildBinding.ant.junit(fork: true) {
            if (buildBinding.buildConfig.gwt.test.args) {
                def c = buildBinding.buildConfig.gwt.run.args.clone()
                c.delegate = delegate
                c()
            }
            else {
                jvmarg(value: "-Xmx256m")
            }
            sysproperty(key:"gwt.args", value:"${gwtProdTestType ? '-prod -out '+buildBinding.gwtProdTestDirPath : ''} -standardsMode -logLevel INFO")
            sysproperty(key:"java.awt.headless", value:"true")
            classpath() {
                pathElement(location: gwtTmpTestsDir.absolutePath)
                pathElement(location: buildBinding.grailsSettings.classesDir.absolutePath)
                pathElement(location: testClassesDir.absolutePath)
                classPath.each { dep ->
                    pathElement(location: dep.absolutePath)
                }
            }
            formatter (type: "plain", usefile: false)
            test(name: SUITE_NAME, outfile: "TEST-unit-${name}-${SUITE_NAME}", todir: buildBinding.testReportsDir.absolutePath) {
                formatter(classname: XMLJUnitResultFormatter.name, extension: ".xml")
            }
        }

        // aggregate test results
        def parser = new XmlSlurper()
        int testsTotal = 0
        int testsFailed = 0
        buildBinding.testReportsDir.eachFileMatch(~/^TEST-unit-${name}-.+\.xml$/) { f ->
            def testResult = parser.parse(f)
            testsFailed += testResult['@errors'].toInteger()
            testsFailed += testResult['@failures'].toInteger()
            testsTotal += testResult['@tests'].toInteger()
        }
        return [getPassCount: { -> testsTotal - testsFailed }, 
                getFailCount: { -> testsFailed }] as GrailsTestTypeResult  
    }
}
