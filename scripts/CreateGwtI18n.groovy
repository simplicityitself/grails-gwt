includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("_GrailsCreateArtifacts")

gwtSrcPath = "src/gwt"

/**
 * grails create-gwt-i18n MODULE
 */
target (default: "Creates a new i18n properties file for a GWT module.") {
    depends(parseArguments)
    promptForName(type: "")

    def i18nSuffixes = [ "Constants", "Messages" ]
    if (argsMap["constants-only"] && argsMap["messages-only"]) {
        println "You can only specify one of 'constants-only' and 'messages-only' - they are mutually exclusive."
        return 1
    }
    else if (argsMap["constants-only"]) {
        i18nSuffixes.remove("Messages")
    }
    else if (argsMap["messages-only"]) {
        i18nSuffixes.remove("Constants")
    }

    // The only argument should be the fully qualified name of the GWT
    // module. First, split it into package and name parts.
    def moduleName = argsMap["params"][0]
    def modulePackage = null
    def pos = moduleName.lastIndexOf('.')
    if (pos != -1) {
        // Extract the name and the package.
        modulePackage = moduleName.substring(0, pos)
        moduleName = moduleName.substring(pos + 1)
    }

    def packagePath = (modulePackage != null ? '/' + modulePackage.replace('.' as char, '/' as char) : '')

    // Now create the properties file(s).
    i18nSuffixes.each { suffix ->
        def targetPath = "${basedir}/${gwtSrcPath}${packagePath}/client"
        def i18nFile = "${targetPath}/${moduleName}${suffix}.properties"
        def templatePath = "${gwtPluginDir}/src/templates/artifacts"
        def templateFile = "${templatePath}/i18n.properties"

        // Check whether the target module exists already.
        if (new File(i18nFile).exists()) {
            // It does, so find out whether the user wants to overwrite
            // the existing copy.
            ant.input(
                addProperty:"${moduleName}${suffix}.overwrite",
                message:"GwtI18n: ${moduleName}${suffix}.properties already exists. Overwrite? [y/n]")

            if (ant.antProject.properties."${moduleName}${suffix}.overwrite" == "n") {
                // User doesn't want to overwrite, so stop the script.
                return
            }
        }

        // Copy the template module file over, replacing any tokens in the
        // process.
        ant.copy(file: templateFile, tofile: i18nFile, overwrite: true)

        event("CreatedFile", [ i18nFile ])
    }
}

