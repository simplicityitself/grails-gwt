
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
      build 'xom:xom:1.2.5', {
        excludes 'xml-apis', 'xerces'
      }
    }
    plugins {
      build ':new-doc:0.3.1', {
        excludes 'xom'
      }
    }
}
//lets us use the grails commands on this project without external setup.
gwt {
  version="2.4.0"
  parallel=false
}

