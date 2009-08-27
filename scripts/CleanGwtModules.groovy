includeTargets << new File("${gwtPluginDir}/scripts/_GwtInternal.groovy")

target (default: "Cleans the compiled GWT module files.") {
    depends(gwtClean)
}
