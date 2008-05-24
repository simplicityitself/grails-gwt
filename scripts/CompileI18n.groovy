Ant.property(environment:"env")

grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

includeTargets << new File ("${grailsHome}/scripts/Init.groovy")
includeTargets << new File("${gwtPluginDir}/scripts/_GwtInternal.groovy")

target ('default': 'Compiles GWT i18n properties files.') {
    depends(checkVersion)

    // Compile the i18n properties files. This target is provided by
    // '_GwtInternal'.
    compileI18n()
}
