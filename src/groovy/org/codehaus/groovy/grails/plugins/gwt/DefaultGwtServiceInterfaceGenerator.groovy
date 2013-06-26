package org.codehaus.groovy.grails.plugins.gwt

import java.beans.Introspector
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.plugins.gwt.GwtServiceInterfaceGenerator
import org.codehaus.groovy.grails.plugins.gwt.annotation.CollectionTypeArg
import org.codehaus.groovy.grails.plugins.gwt.annotation.MapTypeArg
import java.beans.PropertyDescriptor
import java.beans.MethodDescriptor
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.GrailsClass

/**
 * Created by IntelliJ IDEA.
 * User: pal20
 * Date: 02-Apr-2009
 * Time: 09:00:14
 * To change this template use File | Settings | File Templates.
 */
class DefaultGwtServiceInterfaceGenerator implements GwtServiceInterfaceGenerator {
    String srcPath = "src/java";

    boolean isGwtExposed(Class serviceClass) {
        return getPackage(serviceClass) != null
    }

    boolean getInterfacesExist(Class serviceClass) {
        return getInterfacesExist(serviceClass, getPackage(serviceClass))
    }

    boolean getInterfacesExist(Class serviceClass, String packageName) {
        File outputDir = getOutputDir(packageName)
        return new File(outputDir, getMainFilename(serviceClass)).exists() &&
                new File(outputDir, getAsyncFilename(serviceClass)).exists()
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
    void generateInterfaces(Class serviceClass) {
        String pkg = getPackage(serviceClass)

        // Ignore this service class if the package is null.
        if (pkg != null) {
            generateInterfaces(serviceClass, pkg)
        }
    }

    void generateInterfaces(Class serviceClass, String packageName) {
        // Find the directory in which to store the interface files.
        def outputDir = new File(srcPath, packageName.replace('.' as char, '/' as char))

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

        // The generated interface files.
        def className = serviceClass.simpleName
        def mainFile = new File(outputDir, "${className}.java")
        def asyncFile = new File(outputDir, "${className}Async.java")

        // Start the content of the main interface definition.
        def output = new StringBuilder(1024)
        def outputAsync = new StringBuilder(1024)
        writeInterfaces(serviceClass, packageName, output, outputAsync)

        // Write the definitions to the appropriate files.
        mainFile.write(output.toString())
        asyncFile.write(outputAsync.toString())
    }

    /**
     * Determines whether the given service has been configured for
     * GWT RPC, and if so returns the package in which the corresponding
     * interfaces should be generated. If the package is specified in
     * the expose list using the "gwt:<package>" notation, then that
     * package is returned. Otherwise, this returns the package of the
     * given service class.
     * @param serviceClass The Grails service to query.
     * @return The fully-qualified name of the package in which to put
     * the generated interfaces, or <code>null</code> if the expose list
     * does not contain an entry for GWT.
     */
    String getPackage(Class serviceClass) {
        def exposeList = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'expose')

        // Check whether 'gwt' is in the expose list.
        def gwtExposed = exposeList?.find { it instanceof String && it.startsWith('gwt') }
        if (gwtExposed) {
            def m = gwtExposed =~ 'gwt:(.*)'
            if (m) {
                return m[0][1]
            }
            else {
                return getDefaultPackage(serviceClass)
            }
        }
        else {
            return null
        }
    }

    protected void writeInterfaces(Class serviceClass, String packageName, mainOutput, asyncOutput) {
        def className = serviceClass.simpleName

        // Start the content of the main interface definition.
        mainOutput << """\
package ${packageName};

import com.google.gwt.user.client.rpc.RemoteService;

public interface ${className} extends RemoteService {"""

        // Start the content of the async interface definition.
        asyncOutput << """\
package ${packageName};

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ${className}Async {"""

        // Find out what properties the service class contains, because
        // we want to leave them out of the interface definition.
        def info = Introspector.getBeanInfo(serviceClass)
        def propMethods = [] as Set
        info.propertyDescriptors.each {PropertyDescriptor desc ->
            propMethods << desc.readMethod
            propMethods << desc.writeMethod

            // Groovy adds a "get*()" method for booleans as well as
            // the usual "is*()", so we have to remove it too.
            if (desc.readMethod?.name?.startsWith("is")) {
                def name = "get${desc.readMethod.name[2..-1]}".toString()
                def getMethod = info.methodDescriptors.find { it.name == name }
                if (getMethod) {
                    propMethods << getMethod.method
                }
            }
        }

        // Iterate through the methods declared by the Grails service,
        // adding the appropriate ones to the interface definitions.
        serviceClass.declaredMethods.each {Method method ->
            // Skip non-public, static, Groovy, and property methods.
            if (method.synthetic ||
                    !Modifier.isPublic(method.modifiers) ||
                    Modifier.isStatic(method.modifiers) ||
                    propMethods.contains(method)) {
                return
            }

            // Handle any TypeArg annotations on this method and its
            // parameters.
            if (CollectionTypeArg != null) {
                handleTypeArg(mainOutput, method)
            }

            // Output this method definition.
            mainOutput << "\n    ${getType(method.returnType)} ${method.name}("
            asyncOutput << "\n    void ${method.name}("

            // Handle the method's parameters.
            def paramTypes = method.parameterTypes
            for (int i in 0..<paramTypes.size()) {
                if (i > 0) {
                    mainOutput << ', '
                    asyncOutput << ', '
                }

                def paramString = "${getType(paramTypes[i])} arg${i}"
                mainOutput << paramString
                asyncOutput << paramString
            }

            // Close off the method args.
            mainOutput << ')'

            // Handle any exceptions and close off the method.
            def exceptionTypes = method.exceptionTypes
            if (exceptionTypes) {
                mainOutput << ' throws '
                mainOutput << exceptionTypes*.name.join(',')
            }

            // And don't forget the statement terminator! This needs
            // to compile under Java as well as Groovy ;)
            mainOutput << ';'

            // The async interface definition requires an extra parameter
            // on the method.
            if (paramTypes.size() > 0) {
                asyncOutput << ', '
            }
            asyncOutput << 'AsyncCallback callback);'
        }

        // Close the interface definitions off.
        mainOutput << '\n}\n'
        asyncOutput << '\n}\n'
    }

    protected File getOutputDir(String packageName) {
        // Find the directory in which to store the interface files.
        return new File(srcPath, packageName.replace('.' as char, '/' as char))
    }

    protected String getMainFilename(Class serviceClass) {
        return "${serviceClass.simpleName}.java"
    }

    protected String getAsyncFilename(Class serviceClass) {
        return "${serviceClass.simpleName}Async.java"
    }

    protected String getDefaultPackage(Class serviceClass) {
        Package pkg = serviceClass.getPackage()
        return pkg ? pkg.name + ".client" : "client"
    }

    /**
     * Returns the string representation of the given type. For example,
     * 'java.lang.String', 'int', 'java.lang.String[]', 'boolean[][][]'.
     */
    protected String getType(Class clazz) {
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
     * @param method The method definition to process.
     */
    private void handleTypeArg(output, Method method) {
        // Determines whether we have started writing the javadoc
        // comment or not.
        def commentStarted = false

        // Handle the method's parameters.
        def paramAnns = method.parameterAnnotations
        def paramTypes = method.parameterTypes
        for (int i in 0..<paramAnns.size()) {
            if (paramAnns[i].size() > 0) {
                // Look for a TypeArg annotation on this parameter.
                paramAnns[i].each {Annotation ann ->
                    if (CollectionTypeArg.isInstance(ann) || MapTypeArg.isInstance(ann)) {
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
        def ann = method.getAnnotation(CollectionTypeArg)
        if (!ann) ann = method.getAnnotation(MapTypeArg)
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
     * @param type The type to check the annotation against.
     * Should either implement Collection or Map.
     * @param ann The TypeArg annotation to check.
     */
    private void checkTypeArg(Class type, Annotation ann) {
        if (Collection.isAssignableFrom(type)) {
            if (!CollectionTypeArg.isInstance(ann)) {
                throw new RuntimeException(
                        "TypeArg error: annotation is not of type CollectionTypeArg, " +
                                "but the corresponding type is a Collection.")
            }
        }
        else if (Map.isAssignableFrom(type)) {
            if (!MapTypeArg.isInstance(ann)) {
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
    private void startTypeArgComment(output) {
        output << '\n    /**'
    }

    /**
     * Writes the end of a javadoc comment to the given output.
     */
    private void endTypeArgComment(output) {
        output << '\n     */'
    }

    /**
     * Writes a '@gwt.typeArgs' javadoc entry for the given annotation
     * and parameter name.
     * @param output (stream, buffer) The output to write the entry to.
     * The only requirement is that it implements the '<<' operator.
     * @param ann The TypeArg annotation to generate the
     * GWT javadoc tag for.
     * @param paramName The parameter name as specified in the
     * corresponding method signature. Use <code>null</code> if the tag
     * corresponds to a return type.
     */
    private void writeTypeArgEntry(output, Annotation ann, String paramName) {
        output << '\n     * @gwt.typeArgs'
        if (paramName) {
            output << ' ' << paramName
        }
        output << ' <'
        if (MapTypeArg.isInstance(ann)) {
            output << ann.key().name << ', '
        }
        output << ann.value().name << '>'
    }

    private Log getLog() {
        return LogFactory.getLog(this.getClass())
    }
}
