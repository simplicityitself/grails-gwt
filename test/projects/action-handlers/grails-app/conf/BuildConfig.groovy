grails.project.work.dir = "work"
grails.plugin.location.gwt = "../../.."

grails.project.dependency.resolver = "maven" // or ivy

grails.project.dependency.resolution = {
  inherits("global")
  log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
  repositories {
    mavenCentral()
    grailsCentral()
  }
  dependencies {
    //for the release plugin..
//        build "org.apache.httpcomponents:httpclient:4.0.3"
    test ("org.spockframework:spock-grails-support:0.7-groovy-2.0") { export = false }
    build 'com.google.gwt:gwt-user:2.4.0', {
      export=false
    }
  }
  plugins {
    runtime ":resources:1.2"
  }
}

grails.release.scm.enabled = false

// Needed to run commands locally that trip over the GWT_HOME check.
gwt { version = "2.4.0" }
