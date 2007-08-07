import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

Ant.property(environment: 'env')
grailsHome = Ant.antProject.properties.'env.GRAILS_HOME'
gwtHome = Ant.antProject.properties.'env.GWT_HOME'

includeTargets << new File ("${grailsHome}/scripts/Init.groovy")

task ('default': 'Runs the GWT hosted mode client.') {
    depends(checkVersion)

    if (!gwtHome) {
        println 'GWT_HOME is not set - cannot run GWT hosted mode.'
        return
    }

    // Check whether a host and port have been specified.
    def m = args =~ /([a-zA-Z]\w*)?:?(\d+)?/
    if (!args || args == '') {
        args = 'localhost:8080'
    }
    else if (m.matches()) {
        // The user can specify a host, a port, or both if separated
        // by a colon. If either or both are not given, the appropriate
        // defaults are used.
        args = (m[0][1] ? m[0][1] : 'localhost') + ':' + 
                (m[0][2] ? m[0][2] : 8080)
    }

    event('GwtRunHostedStart', [ 'Starting the GWT hosted mode client.' ])

    Ant.sequential {
        def outputPath = "${basedir}/web-app/gwt"

        java(classname: 'com.google.gwt.dev.GWTShell', fork: 'true') {
            // Have to prefix this with 'Ant' because the Init
            // script includes a 'classpath' target.
            Ant.classpath {
                fileset(dir: "${gwtHome}") {
                    include(name: 'gwt-dev-*.jar')
                    include(name: 'gwt-user.jar')
                }
                pathElement(location: "${basedir}/src/java")
            }

            // Hosted mode requires a special JVM argument on Mac OS X. 
            if (antProject.properties.'os.name' == 'Mac OS X') {
                jvmarg(value: '-XstartOnFirstThread')
            }

            arg(value: "http://${args}/${grailsAppName}")
        }
    }
}
