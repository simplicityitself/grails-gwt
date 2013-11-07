grails.project.work.dir = "target"
grails.plugin.location.gwt = "../../.."

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
}

// Needed to run commands locally that trip over the GWT_HOME check.
gwt { version = "2.5.0" }
