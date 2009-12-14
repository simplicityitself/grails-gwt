includeTargets << new File("${gwtPluginDir}/scripts/_GwtInternal.groovy")

// Check that gwtHome points to a valid GWT installation.
eventClasspathStart = {
    if (!gwtHome || !new File(gwtHome, "gwt-user.jar").exists()) {
        event("StatusFinal", [ "ERROR: ${gwtHome} is not a valid GWT installation." ])
        exit(1)
    }
}

// Called when the compilation phase completes.
eventCompileEnd = {
    // Compile the GWT modules. This target is provided by '_GwtInternal'.
    checkGwtHome()
    if (!usingGwt16) {
        compileGwtModules()
    }
}

// Clean up the GWT-generated files on "clean".
eventCleanEnd = {
    gwtClean()
}

eventConfigureWarNameEnd = {
    // If any of the GWT modules haven't been compiled, force a compilation
    // now. This ensures that WAR files are always created with the latest
    // compiled JS files.
    if (!gwtModulesCompiled) {
        gwtForceCompile = true
        compileGwtModules()
    }
}

//
// The GWT libs must be copied to the WAR file. In addition, although
// we don't do dynamic compilation in production mode, the plugin
// groovy class gets compiled with the UnableToCompleteException in
// the class file. Thus, we also have to include this particular file
// in the system.
//
eventCreateWarStart = { warName, stagingDir ->
    // Extract the UnableToCompleteException file from gwt-dev-*.jar
    ant.unjar(dest: "${stagingDir}/WEB-INF/classes") {
        patternset(includes: "com/google/gwt/core/ext/UnableToCompleteException.class")
        fileset(dir: "${gwtHome}", includes: "gwt-dev-*.jar")
    }
}

//
// Adds the GWT servlet library to the root loader.
//
eventPackageAppEnd = {
    rootLoader.addURL(new File(gwtHome, "gwt-servlet.jar").toURI().toURL())
}
