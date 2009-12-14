includeTargets << new File("${gwtPluginDir}/scripts/_GwtInternal.groovy")
includeTargets << grailsScript("_GrailsCompile")

USAGE = """
    compile-gwt-modules [--draft]

where
    --draft  = Compiler uses draft mode, resulting in less optimised
               Javscript (GWT 2.0+ only)
"""

target (default: "Compiles the GWT modules to Javscript.") {
    depends(parseArguments, checkGwtHome)

    // Force compilation of the GWT modules.
    gwtForceCompile = true

    // If arguments are provided, treat them as a list of modules to
    // compile.
    gwtModuleList = argsMap["params"]

    // Handle draft compilation mode. We assign a default value of
    // 'null' so that we know whether we can override with the
    // gwt.draft.compile setting.
    gwtDraftCompile = argsMap["draft"] ?: null

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
