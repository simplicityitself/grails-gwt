import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

Ant.property(environment: 'env')
grailsHome = Ant.antProject.properties.'env.GRAILS_HOME'

includeTargets << new File ("${grailsHome}/scripts/Init.groovy")
includeTargets << new File ("${grailsHome}/scripts/CreateController.groovy")

target ('default': 'Creates a new GWT module.') {
    depends(promptForName)

    // This script takes multiple arguments (in fact, at least two),
    // so split the given string into separate parameters, using
    // whitespace as the delimiter.
    def argArray = args.split('\\s+')

    if (argArray.size() < 2) {
        println "At least two arguments must be given to this script."
        return
    }

    // Location of the template page.
    def templatePath = "@plugin.basedir@/src/templates/artifacts"
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
            Ant.input(
                addProperty:"${controllerName}.auto.create",
                message:"${controllerName} does not exist - do you want to create it now? [y/n]")

            if (Ant.antProject.properties."${controllerName}.auto.create" == "y") {
                // User wants to create the controller, so do so.
                args = m[0][1]
                typeName = "Controller" 
                artifactName = "Controller" 	
                artifactPath = "grails-app/controllers"
                createArtifact()
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
    Ant.copy(file: templateFile, tofile: targetFile, overwrite: true)

    // Replace the tokens in the template.
    Ant.replace(file: targetFile) {
        Ant.replacefilter(token: '@module.name@', value: argArray[1])
    }

    event("CreatedFile", [ targetFile ])
}
