
grails.project.dependency.resolver = "maven" // or ivy

grails.project.dependency.resolution = {
    inherits("global")
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        mavenCentral()
    }
    dependencies {
      test ("org.spockframework:spock-grails-support:0.7-groovy-2.0") { export = false }
      build 'com.google.gwt:gwt-user:2.4.0', {
        export=false
      }
    }
    plugins {
        test(":spock:0.7") { export = false }
        build (":release:3.0.0", ':rest-client-builder:1.0.3') { export = false }
        build ":extended-dependency-manager:0.5.2"
        runtime ":resources:1.2"
    }
}

grails.release.scm.enabled = false

// Needed to run commands locally that trip over the GWT_HOME check.
gwt { version = "2.4.0" }
