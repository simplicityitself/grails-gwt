
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

gwt {
  version="2.4.0"
  parallel=false
}
