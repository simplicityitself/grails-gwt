includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("_GrailsCreateArtifacts")
includeTargets << new File("${gwtPluginDir}/scripts/_GwtCreate.groovy")

USAGE = """
    create-gwt-module PKG.NAME

where
    PKG  = The package name of the module.
    NAME = The name of the module.
"""

target (default: "Creates a new GWT module.") {
    depends(parseArguments)
    promptForName(type: "")

    // We support just the one argument.
    def params = argsMap["params"]
    if (!params || params.size() > 1) {
        println "Unexpected number of command arguments."
        println()
        println "USAGE:${USAGE}"
        exit(1)
    }
    else if (!params[0]) {
        println "A module name must be given."
        exit(1)
    }
    
    // We must split the argument into package and name parts.
    def (modulePackage, moduleName) = packageAndName(params[0])
    
    // We require a package for the module.
    if (!modulePackage) {
        println "Please provide a package for the module."
        exit(1)
    }

    // Now create the module file.
    installGwtTemplate(modulePackage, moduleName, "Gwt.gwt.xml")

    // Now copy the template client entry point over.
    installGwtTemplate(modulePackage + ".client", moduleName, "Gwt.java")
}
