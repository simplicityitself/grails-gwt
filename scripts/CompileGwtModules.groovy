includeTargets << new File("${gwtPluginDir}/scripts/_GwtInternal.groovy")
includeTargets << grailsScript("_GrailsCompile")

target (default: "Calls 'compileGetModules'.") {
    depends(parseArguments, checkGwtHome)

    // Force compilation of the GWT modules.
    gwtForceCompile = true

    // If arguments are provided, treat them as a list of modules to
    // compile.
    gwtModuleList = argsMap["params"]

    // Compile the GWT modules. We use the 'compile' target because
    // 'compileGwtModules' depends on it and the module compilation
    // is triggered by the end of the standard Grails compilation
    // (at the moment).
    if (usingGwt16) {
        compileGwtModules()
    }
    else {
        compile()
    }
}
