grails.project.dependency.resolution = {
    inherits("global")
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
        grailsRepo "http://grails.org/plugins"

    }
    dependencies {
        //for the release plugin..
        build "org.apache.httpcomponents:httpclient:4.0.3"
        build "org.apache.ivy:ivy:2.2.0"
        build "org.grails.plugins:resources:1.2-RC1"
    }
    plugins {
        test(":spock:0.6")
    }
}

grails.release.scm.enabled = false

// Needed to run commands locally that trip over the GWT_HOME check.
gwt { version = "2.4.0" }
