Ant.property(environment:"env")

grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

includeTargets << new File ("${grailsHome}/scripts/Init.groovy")
includeTargets << new File("${gwtPluginDir}/scripts/_GwtInternal.groovy")

target ('default': 'Calls \'compileGetModules\'.') {
    // Force compilation of the GWT modules.
    gwtForceCompile = true

    // If arguments are provided, treat them as a list of modules to
    // compile.
    gwtModuleList = args?.split('\\n')

    // Compile the GWT modules. This target is provided by '_GwtInternal'.
    compileGwtModules()
}
