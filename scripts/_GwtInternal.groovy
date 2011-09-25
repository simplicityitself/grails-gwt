
// This script may be run more than once, because the _Events script
// includes targets from it.
if (getBinding().variables.containsKey("_gwt_internal_called")) return
_gwt_internal_called = true

includeTargets << grailsScript("_GrailsInit")

// The targets in this script assume that Init has already been loaded.
// By not explicitly including Init here, we can use this script from
// the Events script.

// This construct makes a 'gwtForceCompile' option available to scripts
// that use these targets. We only define the property if it hasn't
// already been defined. We cannot simply initialise it here because
// all targets appear to trigger the Events script, which might then
// include this script, which would then result in the property value
// being overwritten.
//
// The events mechanism is a source of great frustration!
if (!(getBinding().variables.containsKey("gwtForceCompile"))) {
    gwtForceCompile = false
}

// We do the same for 'gwtModuleList'.
if (!(getBinding().variables.containsKey("gwtModuleList"))) {
    gwtModuleList = null
}

// Common properties and closures (used as re-usable functions).
ant.property(environment: "env")
gwtHome = System.getProperty("gwt.home") ?: ant.project.properties."env.GWT_HOME"
gwtOutputPath = getPropertyValue("gwt.output.path", "${basedir}/web-app/gwt")
gwtOutputStyle = getPropertyValue("gwt.output.style", "OBF")
gwtDisableCompile = getPropertyValue("gwt.compile.disable", "false").toBoolean()
gwtHostedModeOutput = getPropertyValue("gwt.hosted.output.path", "tomcat/classes") // Default is where gwt shell runs its embedded tomcat
gwtModulesCompiled = false
gwtLibPath = "$basedir/lib/gwt"
gwtLibFile = new File(gwtLibPath)
gwtSrcPath = "src/gwt"
grailsSrcPath = "src/java"
gwtClassesCompiled = false
gwtDependencies = [] as Set
gwtProdTestDirPath = ".gwt-test-temp"
gwtTestTypeName = "gwt"
gwtTestTypesRegistered = false
gwtProdTestTypeName = "gwtprod"
gwtRelativeTestSrcPath = "gwt"
gwtClassesDir = new File(grailsSettings.projectWorkDir, "gwtclasses")
gwtJavacCmd = getPropertyValue("gwt.javac.cmd", null)
gwtJavaCmd = getPropertyValue("gwt.java.cmd", null)

//
// A target to check for existence of the GWT Home
//
target (checkGwtHome: "Stops if GWT_HOME does not exist") {
    if (!gwtHome) {
        event("StatusFinal", ["GWT must be installed and GWT_HOME environment must be set."])
        exit(1)
    }

    // Check whether we are using GWT 1.6.
    ant.available(classname: "com.google.gwt.dev.Compiler", property: "isGwt16") {
        ant.classpath {
            fileset(dir: "${gwtHome}") {
                include(name: "gwt-dev*.jar")
            }
        }
    }

    if (ant.project.properties.isGwt16) {
        usingGwt16 = true
        compilerClass = "com.google.gwt.dev.Compiler"
    }
    else {
        usingGwt16 = false
        compilerClass = "com.google.gwt.dev.GWTCompiler"
    }

    // Is this project using Google Gin?
    usingGoogleGin = false
    if (gwtLibFile.exists() || buildConfig.gwt.use.provided.deps == true) {
        ant.available(classname: "com.google.gwt.inject.client.Ginjector", property: "usingGin") {
            ant.classpath {
                if (gwtLibFile.exists()) {
                    fileset(dir: gwtLibPath) {
                        include(name: "*.jar")
                    }
                }
				
                if (buildConfig.gwt.use.provided.deps == true) {
                    if (grailsSettings.metaClass.hasProperty(grailsSettings, "providedDependencies")) {
                        grailsSettings.providedDependencies.each { dep ->
                            pathElement(location: dep.absolutePath)
                        }
                    }
                    else {
                        ant.echo message: "WARN: You have set gwt.use.provided.deps, " +
                                          "but are using a pre-1.2 version of Grails. The setting " +
                                          "will be ignored."
                    }
                }
            }
        }

        usingGoogleGin = ant.project.properties.usingGin != null
    }
}

//
// A target for compiling any GWT modules defined in the project.
//
// Options:
//
//   gwtForceCompile - Set to true to force module compilation. Otherwise
//                     the modules are only compiled if the environment is
//                     production or the 'nocache.js' file is missing.
//
//   gwtModuleList - A collection or array of modules that should be compiled.
//                   If this is null or empty, all the modules in the
//                   application will be compiled.
//
target (compileGwtModules: "Compiles any GWT modules in '$gwtSrcPath'.") {
    depends(checkGwtHome)
    if (usingGwt16) depends(compile)

    if (gwtDisableCompile) return

    // Make sure that the I18n properties files are compiled before
    // the modules are, unless we're not keeping the properties files
    // in sync with the Java interfaces.
    if (buildConfig.gwt.sync.i18n instanceof Map || buildConfig.gwt.sync.i18n == true) {
        compileI18n()
    }

    // Multi-core compilation.
    def numCompileWorkers = getPropertyValue("gwt.local.workers", 0).toInteger()
    
    // Draft compilation.
    if (!(getBinding().variables.containsKey("gwtDraftCompile"))) {
        gwtDraftCompile = null
    }

    if (gwtDraftCompile == null) {
        gwtDraftCompile = getPropertyValue("gwt.draft.compile", false).toBoolean()
    }
    
    // This triggers the Events scripts in the application and plugins.
    event("GwtCompileStart", [ "Starting to compile the GWT modules." ])

    // Compile any GWT modules. This requires the GWT 'dev' JAR file,
    // so the user must have defined the GWT_HOME environment variable
    // so that we can locate that JAR.
    def modules = gwtModuleList ?: findModules("${basedir}/${gwtSrcPath}", true)
    event("StatusUpdate", [ "Compiling GWT modules" ])
    gwtModulesCompiled = true

    modules.each { moduleName ->
        // Only run the compiler if this is production mode or
        // the 'nocache' file is older than any files in the
        // module directory.
        if (!gwtForceCompile && new File("${gwtOutputPath}/${moduleName}/${moduleName}.nocache.js").exists()) {
            // We can skip this module.
            gwtModulesCompiled = false
            return
        }

        event("StatusUpdate", [ "Module: ${moduleName}" ])

        gwtRun(compilerClass) {
            jvmarg(value: '-Djava.awt.headless=true')
            arg(value: '-style')
            arg(value: gwtOutputStyle)

            // Multi-threaded compilation.
            if (usingGwt16 && numCompileWorkers > 0) {
                arg(value: "-localWorkers")
                arg(value: numCompileWorkers)
            }

            // Draft compile - GWT 2.0+ only
            if (gwtDraftCompile) {
                arg(value: "-draftCompile")
            }

            // The argument specifying the output directory depends on
            // the version of GWT in use.
            if (usingGwt16) {
                // GWT 1.6 uses a different directory structure, and
                // hence arguments to previous versions.
                arg(value: "-war")
            }
            else {
                arg(value: "-out")
            }

            arg(value: gwtOutputPath)
            arg(value: moduleName)
        }
    }
    
    event("StatusUpdate", [ "Finished compiling GWT modules" ])
    event("GwtCompileEnd", [ "Finished compiling the GWT modules." ])
}

// This is only used when running under hosted mode and you have server
// code (ie. test service classes) used by your client code during testing.
target (compileServerCode: "Compiles gwt server code into tomcat/classes directory.") {
    depends(checkGwtHome)

    ant.mkdir(dir: gwtHostedModeOutput)
    gwtJavac(destdir: gwtHostedModeOutput, debug:"yes") {
        // Have to prefix this with 'ant' because the Init script
        // includes a 'classpath' target.
        ant.classpath {
            fileset(dir:"${gwtHome}") {
                include(name: "gwt-dev*.jar")
                include(name: "gwt-user.jar")
            }

            // Include a GWT-specific lib directory if it exists.
            if (gwtLibFile.exists()) {
                fileset(dir: gwtLibPath) {
                    include(name: "*.jar")
                }
            }
        }

        if (new File("${basedir}/${gwtSrcPath}").exists()) {
            src(path: "${basedir}/${gwtSrcPath}")
        }

        src(path: "${basedir}/${grailsSrcPath}")
        include(name: "**/server/**")
    }
}

target (compileI18n: "Compiles any i18n properties files for any GWT modules in '$gwtSrcPath'.") {
    depends(checkGwtHome)

    // This triggers the Events scripts in the application and plugins.
    event("GwtCompileI18nStart", [ "Starting to compile the i18n properties files." ])

    // Compile any i18n properties files that match the filename
    // "<Module>Constants.properties".
    def modules = gwtModuleList ?: findModules("${basedir}/${gwtSrcPath}", false)
    modules += gwtModuleList ?: findModules("${basedir}/${grailsSrcPath}", false)

    event("StatusUpdate", [ "Compiling GWT i18n properties files" ])

    def suffixes = [ "Constants", "Messages" ]
    modules.each { moduleName ->
        event("StatusUpdate", [ "Module: ${moduleName}" ])

        // Split the module name into package and name parts. The
        // package part includes the trailing '.'.
        def pkg = ""
        def pos = moduleName.lastIndexOf('.')
        if (pos > -1) {
            pkg = moduleName[0..pos]
            moduleName = moduleName[(pos + 1)..-1]
        }

        // Check whether the corresponding properties file exists.
        suffixes.each { suffix ->
            def i18nName = "${pkg}client.${moduleName}${suffix}"
            def i18nFile = new File("${basedir}/${gwtSrcPath}", i18nName.replace('.' as char, '/' as char) + ".properties")
            def generatedFile = new File(i18nFile.parentFile, i18nFile.name - ".properties" + ".java")

            if (!i18nFile.exists()) {
                event("StatusUpdate", [ "No i18n ${suffix} file found" ])
            }
            else if (i18nFile.lastModified() < generatedFile.lastModified()) {
                // The generated file is newer than the associated
                // properties file, so skip this one.
                println "Skipping ${i18nFile.name} - the Java file is newer than the properties file."
                return
            }
            else {
                gwtRun("com.google.gwt.i18n.tools.I18NSync") {
                    jvmarg(value: '-Djava.awt.headless=true')
                    arg(value: "-out")
                    arg(value: gwtSrcPath)
                    if (suffix == "Messages") {
                        arg(value: "-createMessages")
                    }
                    arg(value: i18nName)
                }

                event("StatusUpdate", [ "Created class ${i18nName}" ])
            }
        }
    }

    event("StatusUpdate", [ "Finished compiling the i18n properties files." ])
    event("GwtCompileI18nEnd", [ "Finished compiling the i18n properties files." ])
}

target (gwtClean: "Cleans the files generated by GWT.") {
    // Start by removing the directory containing all the javascript
    // files.    
    ant.delete(dir: gwtOutputPath)
    ant.delete(dir: gwtProdTestDirPath)
    ant.delete(dir: gwtClassesDir.path)

    // Now remove any generated i18n files, unless we're not keeping
    // the properties files in sync with the Java interfaces.
    if (buildConfig.gwt.sync.i18n == false) return 0

    def modules = gwtModuleList ?: findModules("${basedir}/${gwtSrcPath}", false)
    modules += gwtModuleList ?: findModules("${basedir}/${grailsSrcPath}", false)

    modules.each { moduleName ->
        // Split the module name into package and name parts. The
        // package part includes the trailing '.'.
        def pkg = ""
        def pos = moduleName.lastIndexOf('.')
        if (pos > -1) {
            pkg = moduleName[0..pos]
            moduleName = moduleName[(pos + 1)..-1]
        }

        // If there is a properties file, delete the corresponding
        // constants file. If it doesn't exist, that doesn't matter:
        // nothing will happen.
        def pkgPath = pkg.replace('.' as char, '/' as char)
        def i18nRoot = new File("${basedir}/${gwtSrcPath}", "${pkgPath}/client")

        def suffixes = [ "Constants", "Messages" ]
        suffixes.each { suffix ->
            def i18nPropFile = new File(i18nRoot, "${moduleName}${suffix}.properties")
            if (i18nPropFile.exists()) {
                ant.delete(file: new File(i18nRoot, "${moduleName}${suffix}.java").path)
            }
        }
    }
}

gwtClientServer = "${serverHost ?: 'localhost'}:${serverPort}"
target (runGwtClient: "Runs the GWT hosted mode client.") {
    depends(checkGwtHome)

    event("StatusUpdate", [ "Starting the GWT hosted mode client." ])
    event("GwtRunHostedStart", [ "Starting the GWT hosted mode client." ])

    // Check for GWT 2.0 hosted mode.
    ant.available(classname: "com.google.gwt.dev.DevMode", property: "isGwt20") {
        ant.classpath {
            fileset(dir: "${gwtHome}") {
                include(name: "gwt-dev*.jar")
            }
        }
    }

    def usingGwt20 = ant.project.properties.isGwt20 != null
    def runClass = usingGwt20 ? "com.google.gwt.dev.DevMode" :
            (usingGwt16 ? "com.google.gwt.dev.HostedMode" : "com.google.gwt.dev.GWTShell")
    def modules = usingGwt16 ? findModules("${basedir}/${gwtSrcPath}", false) : ""

    gwtRun(runClass) {
        // Hosted mode requires a special JVM argument on Mac OS X.
        if (antProject.properties.'os.name' == 'Mac OS X') {
            def osVersion = antProject.properties.'os.version'.split(/\./)
            if (osVersion[0].toInteger() == 10 && osVersion[1].toInteger() >= 6) {
                jvmarg(value: '-d32')
            }

            if (!usingGwt20) {
                jvmarg(value: '-XstartOnFirstThread')
            }
        }

        // Enable remote debugging if required.
        if (argsMap["debug"]) {
            def debugPort = !(argsMap["debug"] instanceof Boolean) ? argsMap["debug"].toInteger() : 5006
            jvmarg(value: "-Xdebug")
            jvmarg(value: "-Xnoagent")
            jvmarg(value: "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${debugPort}")
        }

        if (argsMap["bindAddress"]) {
            arg(value: "-bindAddress")
            arg(value: argsMap["bindAddress"])
        }
        
        arg(value: "-noserver")

        if (usingGwt16) {
            // GWT 1.6 uses a different directory structure, and
            // hence arguments to previous versions.
            arg(value: "-war")
        }
        else {
            arg(value: "-out")
        }
        arg(value: gwtOutputPath)

        if (usingGwt16) arg(value: "-startupUrl")
        arg(value: "http://${gwtClientServer}/${grailsAppName}")

        // GWT 1.6 requires a list of modules as arguments, so we just
        // pass it all the modules in the project.
        if (usingGwt16) {
            arg(line: modules.join(" "))
        }
    }
}

gwtJava = { Map options, Closure body ->
    if (gwtJavaCmd) {
        ant.echo message: "Using ${gwtJavaCmd} for invoking GWT tools"
        options["jvm"] = gwtJavaCmd
    }
    ant.java(options, body)
}

gwtJavac = { Map options, Closure body ->
    if (gwtJavacCmd) {
        ant.echo message: "Using ${gwtJavacCmd} for compiling GWT classes"
        options["fork"] = true
        options["executable"] = gwtJavacCmd
        // set target to java version of JDK used by Grails
        options["target"] = ant.project.properties['ant.java.version']
    }
    ant.javac(options, body)
}

gwtRun = { String className, Closure body ->
    gwtJava(classname: className, fork: "true") {
        // Have to prefix this with 'ant' because the Init
        // script includes a 'classpath' target.
        ant.classpath {
            fileset(dir: "${gwtHome}") {
                include(name: "gwt-dev*.jar")
                include(name: "gwt-user.jar")
                // needed to include in case of GWT 2.3
                include(name: "validation-api*.jar")
            }

            // We allow users to specify GWT dependencies via the "provided"
            // configuration. Ideally, we would add a custom "gwt" conf,
            // but that's not possible with Grails at the moment.
            if (buildConfig.gwt.use.provided.deps == true) {
                if (grailsSettings.metaClass.hasProperty(grailsSettings, "providedDependencies")) {
                    grailsSettings.providedDependencies.each { dep ->
                        pathElement(location: dep.absolutePath)
                    }
                }
                else {
                    ant.echo message: "WARN: You have set gwt.use.provided.deps, " +
                            "but are using a pre-1.2 version of Grails. The setting " +
                            "will be ignored."
                }
            }

            // Include a GWT-specific lib directory if it exists.
            if (gwtLibFile.exists()) {
                fileset(dir: gwtLibPath) {
                    include(name: "*.jar")
                }
            }

            // Must include src/java and src/gwt in classpath so that
            // the source files can be translated.
            if (new File("${basedir}/${gwtSrcPath}").exists()) {
                pathElement(location: "${basedir}/${gwtSrcPath}")
            }
            pathElement(location: "${basedir}/${grailsSrcPath}")
            pathElement(location: grailsSettings.classesDir.path)
            pathElement(location: gwtClassesDir.path)
            
            // Fix to get this working with Grails 1.3+. We have to
            // add the directory where plugin classes are compiled
            // to. Pre-1.3, plugin classes were compiled to the same
            // directory as the application classes.
            if (grailsSettings.metaClass.hasProperty(grailsSettings, "pluginClassesDir")) {
                pathElement(location: grailsSettings.pluginClassesDir.path)
            }

            // Add the plugin's module paths.
            pathElement(location: "${gwtPluginDir}/${gwtSrcPath}")
            pathElement(location: "${gwtPluginDir}/${grailsSrcPath}")

            // Add the DTO source path if that plugin is installed in
            // the current project.
            if (getBinding().variables.containsKey("dtoPluginDir")) {
                pathElement(location: "${dtoPluginDir}/${grailsSrcPath}")
            }
        }

        if (buildConfig.gwt.run.args) {
            def c = buildConfig.gwt.run.args.clone()
            c.delegate = delegate
            c()
        }
        else {
            // Bump the max heap size up by default.
            jvmarg value: "-Xmx256m"
        }

        body.delegate = delegate
        body()
    }
}

/**
 * Searches a given directory for any GWT module files, and
 * returns a list of their fully-qualified names.
 * @param searchDir A string path specifying the directory
 * to search in.
 * @param entryPointOnly Whether to find modules that contains entry-points (ie. GWT clients)
 * @return a list of fully-qualified module names.
 */
def findModules(String searchDir, boolean entryPointOnly) {
    def modules = []
    def baseLength = searchDir.size()

    def searchDirFile = new File(searchDir)
    if (searchDirFile.exists()) {
        searchDirFile.eachFileRecurse { File file ->
            // Replace Windows separators with Unix ones.
            def filePath = file.path.replace('\\' as char, '/' as char)

            // Chop off the search directory.
            filePath = filePath.substring(baseLength + 1)

            // Now check whether this path matches a module file.
            def m = filePath =~ /([\w\/]+)\.gwt\.xml$/
            if (m.count > 0) {
                // now check if this module has an entry point
                // if there's no entry point, then it's not necessary to compile the module
                if (!entryPointOnly || file.text =~ /entry-point/) {
                    // Extract the fully-qualified module name.
                    modules << m[0][1].replace('/' as char, '.' as char)
                }
            }
        }
    }

    return modules
}
