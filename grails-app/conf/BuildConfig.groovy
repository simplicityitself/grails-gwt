grails.release.scm.enabled = false
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
        //build "org.apache.httpcomponents:httpclient:4.0.3"
        build "org.apache.ivy:ivy:2.2.0"


    }
    plugins {
        build (":release:2.0.0") {
          export=false
        }
        test(":spock:0.6") {
          export=false
        }
        compile (":new-doc:0.3.2") {
          export=false
          exclude "xom"
        }
    }
}

grails.release.scm.enabled = false

// Needed to run commands locally that trip over the GWT_HOME check.
gwt { version = "2.4.0" }
