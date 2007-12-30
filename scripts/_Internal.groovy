import grails.util.GrailsUtil as GU
import org.codehaus.groovy.grails.commons.GrailsApplication as GA

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
try {
    def test = !gwtForceCompile

    // If we get here, 'gwtForceCompile' already exists, so we don't
    // want to override the value.
}
catch (MissingPropertyException ex) {
    gwtForceCompile = false
}

// We do the same for 'gwtModuleList'.
try {
    def test = !gwtModuleList
}
catch (MissingPropertyException ex) {
    gwtModuleList = null
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
target (compileGwtModules: "Compiles any GWT modules in 'src/java'.") {
    depends(checkVersion)

    // Set some internal properties.
    def gwtHome = Ant.antProject.properties.'env.GWT_HOME'
    def srcDir = 'src/java'

    // We can't continue unless GWT_HOME is set.
    if (!gwtHome) {
        println 'GWT_HOME is not set - cannot compile GWT modules.'
        return
    }

    // This triggers the Events scripts in the application and plugins.
    event('GwtCompileStart', [ 'Starting to compile the GWT modules.' ])

    // Compile any GWT modules. This requires the GWT 'dev' JAR file,
    // so the user must have defined the GWT_HOME environment variable
    // so that we can locate that JAR.
    def modules = gwtModuleList ?: findModules("${basedir}/${srcDir}")
    Ant.sequential {
        def outputPath = "${basedir}/web-app/gwt"

        echo(message: 'Compiling GWT modules')

        modules.each { moduleName ->
            // Only run the compiler if this is production mode or
            // the 'nocache' file is missing.
            if (!gwtForceCompile &&
                    GU.environment != GA.ENV_PRODUCTION &&
                    new File("${outputPath}/${moduleName}/${moduleName}.nocache.js").exists()) {
                // We can skip this module.
                return
            }

            echo(message: "Module: ${moduleName}")
            java(classname: 'com.google.gwt.dev.GWTCompiler', fork: 'true') {
                // Have to prefix this with 'Ant' because the Init
                // script includes a 'classpath' target.
                Ant.classpath {
                    fileset(dir: "${gwtHome}") {
                        include(name: 'gwt-dev*.jar')
                        include(name: 'gwt-user.jar')
                    }
                    pathElement(location: "${basedir}/${srcDir}")
                }
                arg(value: '-out')
                arg(value: outputPath)
                arg(value: moduleName)
            }
        }
    }

    event('GwtCompileEnd', [ 'Finished compiling the GWT modules.' ])
}

/**
 * Searches a given directory for any GWT module files, and
 * returns a list of their fully-qualified names.
 * @param searchDir A string path specifying the directory
 * to search in.
 * @return a list of fully-qualified module names.
 */
def findModules(searchDir) {
    def modules = []
    def baseLength = searchDir.size()

    new File(searchDir).eachFileRecurse { file ->
        // Replace Windows separators with Unix ones.
        file = file.path.replace('\\' as char, '/' as char)

        // Chop off the search directory.
        file = file.substring(baseLength + 1)

        // Now check whether this path matches a module file.
        def m = file =~ /([\w\/]+)\.gwt\.xml$/
        if (m.count > 0) {
            // Extract the fully-qualified module name.
            modules << m[0][1].replace('/' as char, '.' as char)
        }
    }

    return modules
}
