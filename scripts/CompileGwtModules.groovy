Ant.property(environment:"env")   
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

includeTargets << new File ("${grailsHome}/scripts/Init.groovy")
includeTargets << new File('plugins/gwt-0.2.2/scripts/_Internal.groovy')

target ('default': 'Calls \'compileGetModules\'.') {
    // Compile the GWT modules. This target is provided by '_Internal'.
    gwtForceCompile = true
    compileGwtModules()
}
