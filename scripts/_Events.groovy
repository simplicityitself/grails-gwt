includeTargets << new File("${gwtPluginDir}/scripts/_GwtInternal.groovy")

// Check that gwtHome points to a valid GWT installation.
eventClasspathStart = {
    if (!gwtHome || !new File(gwtHome, "gwt-user.jar").exists()) {
        event("StatusFinal", [ "ERROR: ${gwtHome} is not a valid GWT installation." ])
        exit(1)
    }
}

eventCompileStart = {
    // Add GWT libraries to compiler classpath.
    if (gwtHome) {
        def gwtHomeFile = new File(gwtHome)
        if (gwtHomeFile.exists()) {
            // This line is required if we want to modify the classpaths.
            classpathSet = false

            // Update the dependency lists.
            new File(gwtHome).eachFileMatch(~/^gwt-(dev-\w+|user)\.jar$/) { File f ->
                grailsSettings.compileDependencies << f
                grailsSettings.testDependencies << f
            }

            grailsSettings.runtimeDependencies << new File(gwtHomeFile, "gwt-servlet.jar")

            // Regenerate the classpaths based on the modified dependencies.
            classpath()
        }
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

        // Disable draft mode when we create a WAR.
        gwtDraftCompile = false
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

eventGwtRunHostedStart = {
    compileGwtClasses()
}

eventGwtCompileStart = {
    compileGwtClasses()
}

void compileGwtClasses() {
    if (googleGinUsed) {
        // Hack to work around an issue in Google Gin:
        //
        //    http://code.google.com/p/google-gin/issues/detail?id=36
        //
        ant.mkdir(dir: grailsSettings.classesDir.path)
        ant.javac(srcdir: "src/gwt", destDir: grailsSettings.classesDir.path, includes: "**/*.java") {
            ant.classpath {
                fileset(dir: gwtHome) {
                    include(name: "gwt-dev*.jar")
                    include(name: "gwt-user.jar")
                }

                fileset(dir: "lib/gwt", includes: "*.jar")
            }
        }
    }
}

boolean isGoogleGinUsed() {
    // Is this project using Google Gin?
    if (gwtLibFile.exists()) {
        ant.available(classname: "com.google.gwt.inject.client.Ginjector", property: "usingGin") {
            ant.classpath {
                fileset(dir: gwtLibPath) {
                    include(name: "*.jar")
                }
            }
        }

        return ant.project.properties.usingGin != null
    }
    else {
        return false
    }
}
