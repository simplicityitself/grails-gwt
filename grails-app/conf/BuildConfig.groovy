
grails.project.dependency.resolution = {
    inherits("global")
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
    }
    dependencies {
      //for the release plugin..
      build 'org.apache.httpcomponents:httpclient:4.0.3'
    }
}

grails.release.scm.enabled = false
//needed to run commands locally that trip over the GWT_HOME check.
gwt {
  version="2.4.0"
}
