includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("_GrailsCreateArtifacts")

gwtSrcPath = "src/gwt"

target (default: "Creates a new GWT action, response, and action handler.") {
    depends(parseArguments)
    promptForName(type: "")

    // The only argument should be the fully qualified name of the GWT
    // action. First, split it into package and name parts.
    def actionName = argsMap["params"][0]
    def actionPackage = null
    def pos = actionName.lastIndexOf('.')
    if (pos != -1) {
        // Extract the name and the package.
        actionPackage = actionName[0..<pos]
        actionName = actionName[(pos + 1)..-1]
    }

    def packagePath = (actionPackage != null ? '/' + actionPackage.replace('.' as char, '/' as char) : '')

    // Now create the action file.
    def targetFile = new File("${basedir}/${grailsSrcPath}${packagePath}/client", "${actionName}Action.java")
    def templateFile = new File("${gwtPluginDir}/src/templates/artifacts", "GwtAction.java")

    installFile(targetFile, templateFile, [ "action.package": (actionPackage ? "${actionPackage}." : ""), "action.name": actionName ])

    // Now for the response file.
    targetFile = new File("${basedir}/${grailsSrcPath}${packagePath}/client", "${actionName}Response.java")
    templateFile = new File("${gwtPluginDir}/src/templates/artifacts", "GwtResponse.java")

    installFile(targetFile, templateFile, [ "action.package": (actionPackage ? "${actionPackage}." : ""), "action.name": actionName ])

    // Finally, the action handler.
    targetFile = new File("${basedir}/grails-app/actionHandlers${packagePath}", "${actionName}ActionHandler.java")
    templateFile = new File("${gwtPluginDir}/src/templates/artifacts", "GwtActionHandler.java")

    installFile(targetFile, templateFile, [ "action.package": (actionPackage ? "package ${actionPackage}\n\n" : ""), "action.name": actionName ])
}
