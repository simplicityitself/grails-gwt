includeTargets << new File("${gwtPluginDir}/scripts/_Internal.groovy")

// Called when the compilation phase completes.
eventCompileStart = { type ->
    if (type != 'source') {
        return
    }

    // Compile the GWT modules. This target is provided by '_Internal'.
    compileGwtModules()
}
