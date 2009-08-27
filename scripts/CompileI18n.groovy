includeTargets << new File("${gwtPluginDir}/scripts/_GwtInternal.groovy")

target (default: "Generates the appropriate Java interfaces for all GWT I18n property files.") {
    // Compile the i18n properties files. This target is provided by
    // '_GwtInternal'.
    compileI18n()
}
