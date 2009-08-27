import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("_GrailsCreateArtifacts")

target (default: "Creates a new GSP page for hosting a GWT UI.") {
    depends(parseArguments)

    // This script takes multiple arguments (in fact, at least two),
    // so split the given string into separate parameters, using
    // whitespace as the delimiter.
    def argArray = argsMap["params"]

    if (argArray.size() < 2) {
        println "At least two arguments must be given to this script."
        println()
        println "USAGE: create-gwt-page GSPFILE MODULE"
        return 1
    }

    // Location of the template page.
    def templatePath = "${gwtPluginDir}/src/templates/artifacts"
    def templateFile = "${templatePath}/GwtHostPage.tmpl"

    // Now look at the first argument, which should be the location
    // of the page to create.
    def targetFile = null
    def m = argArray[0] =~ /(\w+)[\/\\]\w+\.gsp/
    if (m.matches()) {
        // The first group is the controller name, the second is the
        // view file. Does the controller already exist?
        def controllerName = GCU.getClassNameRepresentation(m[0][1]) + 'Controller'
        if (!new File("${basedir}/grails-app/controllers/${controllerName}.groovy").exists()) {
            // Controller doesn't exist - does the user want to create
            // it?
            ant.input(
                addProperty:"${controllerName}.auto.create",
                message:"${controllerName} does not exist - do you want to create it now? [y/n]")

            if (ant.antProject.properties."${controllerName}.auto.create" == "y") {
                // User wants to create the controller, so do so.
                createArtifact(name: m[0][1], suffix: "Controller", type: "Controller", path: "grails-app/controllers")
                createUnitTest(name: m[0][1], suffix: "Controller", superClass: "ControllerUnitTestCase")
                ant.mkdir(dir: "${basedir}/grails-app/views/${propertyName}")
            }
        }

        // The target file is written into the 'views' directory.
        targetFile = "grails-app/views/${argArray[0]}"
    }
    else {
        // Create the file in 'web-app'.
        targetFile = "web-app/${argArray[0]}"
    }

    // Copy the template file to the target location.
    ant.copy(file: templateFile, tofile: targetFile, overwrite: true)

    // Replace the tokens in the template.
    ant.replace(file: targetFile) {
        replacefilter(token: '@module.name@', value: argArray[1])
    }

    event("CreatedFile", [ targetFile ])
}
