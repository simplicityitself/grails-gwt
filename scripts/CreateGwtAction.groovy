includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("_GrailsCreateArtifacts")
includeTargets << new File("${gwtPluginDir}/scripts/_GwtCreate.groovy")

USAGE = """
    create-gwt-action [--shared-pkg=SHAREDPKG] MODULEPKG.ACTIONNAME
    create-gwt-action [--shared-pkg=SHAREDPKG] MODULEPKG [SUBPKG] ACTIONNAME

where
    SHAREDPKG = The action and response need to be in a package that is
                on the source path of the module. By default, this is the
                "${buildConfig.gwt.shared.package ?: "client"}" sub-package, but you can override that with this
                option or with the "gwt.shared.package" build setting.
    MODULEPKG = The package name of the action's GWT module.
    SUBPKG    = The name of an optional sub-package in which the action
                class will go, which will be a sub-package of "shared".
    EVENTNAME = The name of the action, without the "Action" suffix.
"""

target (default: "Creates a new GWT action, response, and action handler.") {
    depends(parseArguments)
    promptForName(type: "")
    
    // We support either one argument or three.
    def params = argsMap["params"]
    if (!params || params.size() > 3) {
        println "Unexpected number of command arguments."
        println()
        println "USAGE:${USAGE}"
        exit(1)
    }
    else if (!params[0]) {
        println "An action name must be given."
        exit(1)
    }
    
    // If we only have one argument, we must split it into package and
    // name parts. Otherwise, we just use the provided arguments as is.
    def modulePackage, actionName
    def subPackage = ""
    if (params.size() == 1) {
        (modulePackage, actionName) = packageAndName(params[0])
    }
    else {
        modulePackage = params[0]
        if (params.size() == 2) {
            actionName = params[1]
        }
        else {
            subPackage = '.' + params[1]
            actionName = params[2]
        }
    }

    // Now create the action file.
    def sharedPkg = argsMap["shared-pkg"] ?: buildConfig.gwt.shared.package ?: "client"
    def actionPackage = "${modulePackage}.${sharedPkg}${subPackage}"
    installGwtTemplate(actionPackage, actionName, "GwtAction.java", grailsSrcPath)

    // Now for the response file.
    installGwtTemplate(actionPackage, actionName, "GwtResponse.java", grailsSrcPath)

    // Finally, the action handler.
    def handlerPackage = modulePackage + subPackage
    targetFile = new File(
            "${basedir}/grails-app/actionHandlers${handlerPackage ? '/' : ''}${packageToPath(handlerPackage)}",
            "${actionName}ActionHandler.groovy")
    templateFile = new File("${gwtPluginDir}/src/templates/artifacts", "GwtActionHandler.groovy")

    installFile(targetFile, templateFile, [
        "package.line": (handlerPackage ? "package ${handlerPackage}\n\n" : ""),
        "action.package": actionPackage,
        "action.name": actionName ])
}
