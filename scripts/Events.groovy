includeTargets << new File("${gwtPluginDir}/scripts/_GwtInternal.groovy")

// Called when the compilation phase completes.
eventCompileStart = { type ->
    if (type != 'source') {
        return
    }

    // Compile the GWT modules. This target is provided by '_GwtInternal'.
    compileGwtModules()
}
