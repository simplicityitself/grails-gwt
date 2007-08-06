import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

Ant.property(environment: 'env')
grailsHome = Ant.antProject.properties.'env.GRAILS_HOME'
gwtHome = Ant.antProject.properties.'env.GWT_HOME'

includeTargets << new File ("${grailsHome}/scripts/Init.groovy")

task ('default': 'Calls \'compileGetModules\'.') {
    compileGwtModules()
}

task (compileGwtModules: 'Compiles any GWT modules in \'src/java\'.') {
    depends(checkVersion)

    if (!gwtHome) {
        println 'GWT_HOME is not set - cannot compile GWT modules.'
        return
    }

    event('GwtCompileStart', [ 'Starting to compile the GWT modules.' ])

    // Compile any GWT modules. This requires the GWT 'dev' JAR file,
    // so the user must have defined the GWT_HOME environment variable
    // so that we can locate that JAR.
    def modules = findModules("${basedir}/src/java")
    Ant.sequential {
        def outputPath = "${basedir}/web-app/gwt"

        echo(message: 'Compiling GWT modules')

        modules.each { moduleName ->
            echo(message: "Module: ${moduleName}")
            java(classname: 'com.google.gwt.dev.GWTCompiler', fork: 'true') {
                // Have to prefix this with 'Ant' because the Init
                // script includes a 'classpath' target.
                Ant.classpath {
                    fileset(dir: "${gwtHome}") {
                        include(name: 'gwt-dev-*.jar')
                        include(name: 'gwt-user.jar')
                    }
                    pathElement(location: "${basedir}/src/java")
                }
                arg(value: '-out')
                arg(value: outputPath)
                arg(value: moduleName)
            }

            // The compile generates the files in a sub-directory of
            // 'web-app/gwt' named after the module, but we want them
            // directly in the 'gwt' dir.
            def path = "${basedir}/web-app/gwt/${moduleName}"
            delete(file: "${path}/gwt.js")
            copy(todir: outputPath) {
                fileset(dir: path, includes: '**/*')
            }
            delete(dir: path)
        }
    }

    event('GwtCompileEnd', [ 'Finished compiling the GWT modules.' ])
}

// Searches a given directory for any GWT module files, and
// returns a list of their fully-qualified names.
def findModules(searchDir) {
    def modules = []
    def baseLength = searchDir.size()

    new File(searchDir).eachFileRecurse { file ->
        // Chop off the search directory.
        file = file.path.substring(baseLength + 1)

        // Now check whether this path matches a module file.
        def m = file =~ /([\w\/]+)\.gwt\.xml/
        if (m.matches()) {
            // Extract the fully-qualified module name.
            modules << m[0][1].replaceAll('/', '.')
        }
    }

    return modules
}
