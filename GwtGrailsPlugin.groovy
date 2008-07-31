/*
 * Copyright 2007 Peter Ledbrook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
import java.lang.reflect.Modifier

import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.plugins.support.GrailsPluginUtils
import org.codehaus.groovy.grails.web.plugins.support.WebMetaUtils
import java.lang.reflect.Method
import java.beans.Introspector
import java.beans.PropertyDescriptor

class GwtGrailsPlugin {
    private static final GROOVY_METHODS = [
            'getMetaClass',
            'getProperty',
            'invokeMethod',
            'setMetaClass',
            'setProperty' ] as Set

    private static final Class COLLECTION_TYPE_ARG_CLASS
    private static final Class MAP_TYPE_ARG_CLASS

    static {
        // Check whether the JVM supports annotations.
        try {
            // Try to load the Annotation class dynamically.
            Class.forName('java.lang.annotation.Annotation')

            // The Annotation class was loaded fine, so we can check
            // for the TypeArg annotation. We load the annotation
            // class dynamically so that the plugin can be used with
            // the 1.4 JDK.
            COLLECTION_TYPE_ARG_CLASS =
                Class.forName('org.codehaus.groovy.grails.plugins.gwt.annotation.CollectionTypeArg')
            MAP_TYPE_ARG_CLASS =
                Class.forName('org.codehaus.groovy.grails.plugins.gwt.annotation.MapTypeArg')
        }
        catch (ClassNotFoundException ex) {
            COLLECTION_TYPE_ARG_CLASS = null
            MAP_TYPE_ARG_CLASS = null
        }
    }

    def version = '0.3-SNAPSHOT'
    def author = 'Peter Ledbrook'
    def authorEmail = 'peter@cacoethes.co.uk'
    def title = 'The Google Web Toolkit for Grails.'
    def description = '''\
Incorporates GWT into Grails. In particular, GWT host pages can be
GSPs and standard Grails services can be used to handle client RPC
requests.
'''
    def documentation = 'http://grails.codehaus.org/GWT+Plugin'

    def grailsVersion = GrailsPluginUtils.grailsVersion
    def observe = [ 'services' ]

    def srcDir = 'src/java'

    def doWithSpring = {
        // Generating the java interfaces tends to cause Grails to
        // reload Jetty in development mode, which then triggers this
        // closure. To avoid entering an infinite loop of generate ->
        // restart -> generate, we check whether a system property has
        // been set.
        //
        // On first startup, this property will not be set, but on all
        // subsequent restarts, it will.
        if (System.getProperty('gwt.plugin.started')) {
            return
        }

        // Iterate through each of the declared services and
        // configure them for GWT.
        application.serviceClasses.each { serviceWrapper ->
            def packageName = getPackage(serviceWrapper)
            if (packageName != null) {
                // Generate GWT client interfaces for this service.
                // The matching group is the package in which to
                // place the generated interfaces.
                log.info "Exposing ${serviceWrapper.shortName} as a GWT service."
                generateClientInterfaces(serviceWrapper, packageName, log)
            }
        }

        // Generated the interfaces, so now set the required system
        // property to prevent any further calls.
        System.setProperty('gwt.plugin.started', 'true')
    }   

    def doWithApplicationContext = { applicationContext ->
    }

    def doWithWebDescriptor = { xml ->
        xml.servlet[0] + {
            servlet {
                'servlet-name'('GwtRpcServlet')
                'servlet-class'('org.codehaus.groovy.grails.plugins.gwt.GrailsRemoteServiceServlet')
            }
        }

        // Create a servlet mapping for each module defined in the
        // project.
        def modules = findModules(srcDir)
        modules.each { module ->
            xml.'servlet-mapping'[0] + {
                'servlet-mapping' {
                    'servlet-name'('GwtRpcServlet')
                    'url-pattern'("/gwt/$module/rpc")
                }
            }
        }
    }                                      

    /**
     * Registers the common web-related dynamic properties on services
     * that are exposed via GWT.
     */
    def doWithDynamicMethods = { ctx ->
	    application.serviceClasses.each { serviceWrapper ->
            def packageName = getPackage(serviceWrapper)
            if (packageName != null) {
                WebMetaUtils.registerCommonWebProperties(serviceWrapper.clazz.metaClass, application)
            }
        }

    }

    def onChange = { event ->
        if (application.isServiceClass(event.source)) {
            // A service has been modified (or created).
            def serviceWrapper = application.getServiceClass(event.source?.name)
            def packageName = getPackage(serviceWrapper)
            println "Change on ${event.source.name}"
            println "Service: ${serviceWrapper}"

            // Find any generated interfaces that match the modified
            // service.
            def matchingInterfaces = []
            new File(srcDir).eachFileRecurse { file ->
                if (file.name ==~ "${serviceWrapper.shortName}Async.java") {
                    def mainInterfaceFile = new File(file.getParentFile(), "${serviceWrapper.shortName}.java")
                    if (mainInterfaceFile.exists()) {
                        matchingInterfaces << mainInterfaceFile
                    }
                }
            }

            // Does the service expose itself via GWT RPC? If not, then
            // delete any generated interface files.
            if (packageName == null) {
                matchingInterfaces.each { file ->
                    // Delete both the main interface file and the
                    // corresponding Async definition.
                    def asyncInterfaceFile = new File(file.getParentFile(), "${serviceWrapper.shortName}Async.java")
                    asyncInterfaceFile.delete()
                    file.delete()
                }
            }
            else {
                // This service is configured for GWT RPC. Get the
                // fully-qualified name of the interface.
                matchingInterfaces.each { file ->
                    // Get the file's path and replace Windows separators
                    // with Unix ones.
                    def path = file.path.replace('\\' as char, '/' as char)

                    // Remove the source directory from the path.
                    path = path.substring(srcDir.length() + 1)

                    // Extract the package name from the path.
                    def pkg = ''
                    def pos = path.lastIndexOf('/')
                    if (pos != -1) {
                        pkg = path.substring(0, pos).replace('/' as char, '.' as char)
                    }

                    // Does this match the package name specified by
                    // the service?
                    if (pkg != packageName) {
                        // Packages don't match, so remove the interface
                        // files.
                        def asyncInterfaceFile =
                            new File(file.getParentFile(), "${serviceWrapper.shortName}Async.java")
                        asyncInterfaceFile.delete()
                        file.delete()
                    }
                }

                // Now generate the interfaces for the service in the
                // specified package.
                log.info "Exposing ${serviceWrapper.shortName} as a GWT service."
                generateClientInterfaces(serviceWrapper, packageName, log)
            }
        }
    }                                                                                  

    def onApplicationChange = { event ->
    }

    /**
     * Determines whether the given service has been configured for
     * GWT RPC, and if so returns the package in which the corresponding
     * interfaces should be generated.
     * @param serviceWrapper (GrailsClass) The Grails service to query.
     * @return The fully-qualified name of the package in which to put
     * the generated interfaces.
     */
    def getPackage(serviceWrapper) {
        def exposeList = GrailsClassUtils.getStaticPropertyValue(serviceWrapper.clazz, 'expose')

        // Check whether 'gwt' is in the expose list.
        def gwtExposed = exposeList?.find { it.startsWith('gwt:') }
        if (gwtExposed) {
            def m = gwtExposed =~ 'gwt:(.*)'
            return m[0][1]
        }
        else {
            return null
        }
    }

    /**
     * Creates the required service and async interfaces for a given
     * Grails service. This checks to make sure that neither interface
     * file has been modified since they were last generated. If either
     * has, then the generation is skipped - we don't want to overwrite
     * user changes.
     * @param serviceWrapper (GrailsClass) The Grails service's class.
     * @param packageName (String) The GWT client package in which to
     * put the interfaces.
     * @param log The plugin logger.
     */
    def generateClientInterfaces(serviceWrapper, packageName, log) {
        // Find the directory in which to store the interface files.
        def outputDir = new File(srcDir, packageName.replace('.' as char, '/' as char))

        // Make sure that the output directory exists so that we can
        // create the interface files in it.
        if (!outputDir.exists()) {
            // The directory doesn't exist - it should really, since
            // a GWT module should reside here. Give the user the
            // benefit of the doubt and attempt to create the directory,
            // but log a warning in case the package is incorrect.
            log.warn "Directory '${outputDir}' does not exist - creating it now."
            if (!outputDir.mkdirs()) {
                log.error "Could not create required output directory."
                return
            }
        }

        // There should be a timestamp file indicating when the interface
        // files were last generated. Get hold of it.
        def className = serviceWrapper.shortName
        def timestampFile = new File(outputDir, "${className}.timestamp")

        // And the generated interface files.
        def mainFile = new File(outputDir, "${className}.java")
        def asyncFile = new File(outputDir, "${className}Async.java")

        // Now check that neither the main interface file, nor the
        // async one, has been modified since the last generation.
        if (timestampFile.exists()) {
            if (mainFile.exists() && asyncFile.exists() &&
                    (mainFile.lastModified() > timestampFile.lastModified() ||
                     asyncFile.lastModified() > timestampFile.lastModified())) {
                // Either the main interface file, or the async file (or
                // both), has been modified since last generation, so skip
                // the generation this time around.
                return
            }
        }
        else {
            // The timestamp doesn't exist, so the interfaces haven't
            // been generated yet. However, if the files exist already,
            // we should leave them alone.
            if (mainFile.exists() && asyncFile.exists()) {
                return
            }
        }

        // Start the content of the main interface definition.
        def output = new StringBuffer("""\
package ${packageName};

import com.google.gwt.user.client.rpc.RemoteService;

public interface ${className} extends RemoteService {""")

        // Start the content of the async interface definition.
        def outputAsync = new StringBuffer("""\
package ${packageName};

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ${className}Async {""")

        // Find out what properties the service class contains, because
        // we want to leave them out of the interface definition.
        def info = Introspector.getBeanInfo(serviceWrapper.clazz)
        def propMethods = [] as Set
        info.propertyDescriptors.each { PropertyDescriptor desc ->
            propMethods << desc.readMethod
            propMethods << desc.writeMethod

            // Groovy adds a "get*()" method for booleans as well as
            // the usual "is*()", so we have to remove it too.
            if (desc.readMethod?.name?.startsWith("is")) {
                def name = "get${desc.readMethod.name[2..-1]}".toString()
                propMethods << info.methodDescriptors.find { it.name == name }.method
            }
        }

        // Iterate through the methods declared by the Grails service,
        // adding the appropriate ones to the interface definitions.
        serviceWrapper.clazz.declaredMethods.each { Method method ->
            // Skip non-public, static, Groovy, and property methods.
            if (!Modifier.isPublic(method.modifiers) ||
                    Modifier.isStatic(method.modifiers) ||
                    GROOVY_METHODS.contains(method.name) ||
                    propMethods.contains(method)) {
                return
            }

            // Handle any TypeArg annotations on this method and its
            // parameters.
            if (COLLECTION_TYPE_ARG_CLASS != null) {
                handleTypeArg(output, method)
            }

            // Output this method definition.
            output << "\n    ${getType(method.returnType)} ${method.name}("
            outputAsync << "\n    void ${method.name}("

            // Handle the method's parameters.
            def paramTypes = method.parameterTypes
            for (int i in 0..<paramTypes.size()) {
                if (i > 0) {
                    output << ', '
                    outputAsync << ', '
                }

                def paramString = "${getType(paramTypes[i])} arg${i}"
                output << paramString
                outputAsync << paramString
            }

            // Close off the method args.
            output << ')'

            // Handle any exceptions and close off the method.
            def exceptionTypes = method.exceptionTypes
            if (exceptionTypes) {
                output << ' throws '
                output << exceptionTypes*.name.join(',')
            }

            // And don't forget the statement terminator! This needs
            // to compile under Java as well as Groovy ;)
            output << ';'

            // The async interface definition requires an extra parameter
            // on the method.
            if (paramTypes.size() > 0) {
                outputAsync << ', '
            }
            outputAsync << 'AsyncCallback callback);'
        }

        // Close the interface definitions off.
        output << '\n}\n'
        outputAsync << '\n}\n'

        // Write the definitions to the appropriate files.
        mainFile.write(output.toString())
        asyncFile.write(outputAsync.toString())

        // Now update/create the timestamp file so that we know when
        // these interface files were generated.
        FileUtils.touch(timestampFile)
    }

    /**
     * Returns the string representation of the given type. For example,
     * 'java.lang.String', 'int', 'java.lang.String[]', 'boolean[][][]'.
     */
    def getType(clazz) {
        if (!clazz.array) {
            // If the type is not an array, we can simply return its
            // name.
            return clazz.name
        }
        else {
            // The class name contains some number of '[' characters
            // indicating the dimensions of the array.
            def dimensions = clazz.name.count('[')

            // To get the base type of the array, we have to recurse
            // through the component types.
            def type = clazz.componentType
            for (int i in 1..<dimensions) {
                type = type.componentType
            }

            return type.name + '[]' * dimensions
        }
    }

    /**
     * Checks for any TypeArg annotations on a given method and writes
     * the appropriate javadoc with '@gwt.typeArgs' tags if necessary.
     * @param output (output stream, string buffer) The stream or buffer
     * to write the javadoc to. The only requirement is that is supports
     * the '<<' operator.
     * @param method (Method) The method definition to process.
     */
    def handleTypeArg(output, method) {
        // Determines whether we have started writing the javadoc
        // comment or not.
        def commentStarted = false

        // Handle the method's parameters.
        def paramAnns = method.parameterAnnotations
        def paramTypes = method.parameterTypes
        for (int i in 0..<paramAnns.size()) {
            if (paramAnns[i].size() > 0) {
                // Look for a TypeArg annotation on this parameter.
                paramAnns[i].each { ann ->
                    if (COLLECTION_TYPE_ARG_CLASS.isInstance(ann) || MAP_TYPE_ARG_CLASS.isInstance(ann)) {
                        checkTypeArg(paramTypes[i], ann)

                        // Found it. So add a GWT typeArgs javadoc for
                        // it.
                        writeTypeArgEntry(output, ann, "arg$i")
                    }
                }
            }
        }

        // Check for a TypeArg annotation on the method itself. This
        // will apply to the return type of the method.
        def ann = method.getAnnotation(COLLECTION_TYPE_ARG_CLASS)
        if (!ann) ann = method.getAnnotation(MAP_TYPE_ARG_CLASS)
        if (ann) {
            checkTypeArg(method.returnType, ann)
            if (!commentStarted) {
                startTypeArgComment(output)
                commentStarted = true
            }
            writeTypeArgEntry(output, ann, null)
        }

        // Close the javadoc comment if we started one.
        if (commentStarted) {
            endTypeArgComment(output)
        }
    }

    /**
     * Checks whether a TypeArg annotation matches the given type. If
     * it doesn't, an exception is thrown.
     * @param type (Class) The type to check the annotation against.
     * Should either implement Collection or Map.
     * @param ann (Annotation) The TypeArg annotation to check.
     */
    def checkTypeArg(type, ann) {
        if (Collection.isAssignableFrom(type)) {
            if (!COLLECTION_TYPE_ARG_CLASS.isInstance(ann)) {
                throw new RuntimeException(
                        "TypeArg error: annotation is not of type CollectionTypeArg, " +
                        "but the corresponding type is a Collection.")
            }
        }
        else if (Map.isAssignableFrom(type)) {
            if (!MAP_TYPE_ARG_CLASS.isInstance(ann)) {
                throw new RuntimeException(
                        "TypeArg error: annotation is not of type MapTypeArg, " +
                        "but the corresponding type is a Map.")
            }
        }
        else {
            throw new RuntimeException(
                    "TypeArg error: annotation present, but the corresponding type " +
                    "is neither a Collection nor a Map.")
        }
    }

    /**
     * Writes the start of a javadoc comment to the given output.
     */
    def startTypeArgComment(output) {
        output << '\n    /**'
    }

    /**
     * Writes the end of a javadoc comment to the given output.
     */
    def endTypeArgComment(output) {
        output << '\n     */'
    }

    /**
     * Writes a '@gwt.typeArgs' javadoc entry for the given annotation
     * and parameter name.
     * @param output (stream, buffer) The output to write the entry to.
     * The only requirement is that it implements the '<<' operator.
     * @param ann (Annotation) The TypeArg annotation to generate the
     * GWT javadoc tag for.
     * @param paramName (String) The parameter name as specified in the
     * corresponding method signature. Use <code>null</code> if the tag
     * corresponds to a return type.
     */
    def writeTypeArgEntry(output, ann, paramName) {
        output << '\n     * @gwt.typeArgs'
        if (paramName) {
            output << ' ' << paramName
        }
        output << ' <'
        if (MAP_TYPE_ARG_CLASS.isInstance(ann)) {
            output << ann.key().name << ', '
        }
        output << ann.value().name << '>'
    }

    /**
     * Searches a given directory for any GWT module files, and
     * returns a list of their fully-qualified names.
     * @param searchDir A string path specifying the directory
     * to search in.
     * @return a list of fully-qualified module names.
     */
    def findModules(searchDir) {
        def modules = []
        def baseLength = searchDir.size()

        new File(searchDir).eachFileRecurse { file ->
            // Replace Windows separators with Unix ones.
            file = file.path.replace('\\' as char, '/' as char)

            // Chop off the search directory.
            file = file.substring(baseLength + 1)

            // Now check whether this path matches a module file.
            def m = file =~ /([\w\/]+)\.gwt\.xml$/
            if (m.count > 0) {
                // Extract the fully-qualified module name.
                modules << m[0][1].replace('/' as char, '.' as char)
            }
        }

        return modules
    }
}
