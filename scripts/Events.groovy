import grails.util.GrailsUtil as GU
import org.codehaus.groovy.grails.commons.GrailsApplication as GA

srcDir = 'src/java'

// Called when the compilation phase completes.
eventCompileStart = { type ->
    def gwtHome = Ant.antProject.properties.'env.GWT_HOME'

    if (!gwtHome || type != 'source') {
        return
    }

    // Compile any GWT modules. This requires the GWT 'dev' JAR file,
    // so the user must have defined the GWT_HOME environment variable
    // so that we can locate that JAR.
    def modules = findModules("${basedir}/${srcDir}")
    Ant.sequential {
        def outputPath = "${basedir}/web-app/gwt"

        echo(message: 'Compiling GWT modules')

        modules.each { moduleName ->
            // Only run the compiler if this is production mode or
            // the 'nocache' file is missing.
            if (GU.environment != GA.ENV_PRODUCTION &&
                    new File("${outputPath}/${moduleName}/${moduleName}.nocache.js").exists()) {
                // We can skip this module.
                return
            }

            // Run the compiler.
            echo(message: "Module: ${moduleName}")
            java(classname: 'com.google.gwt.dev.GWTCompiler', fork: 'true') {
                classpath {
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
        def m = file =~ /([\w\/]+)\.gwt\.xml/
        if (m.count > 0) {
            // Extract the fully-qualified module name.
            modules << m[0][1].replace('/' as char, '.' as char)
        }
    }

    return modules
}
