grails.project.dependency.resolver = 'maven'

grails.project.dependency.resolution = {
    inherits('global')
    log 'warn'
    repositories {
        grailsCentral()
        mavenCentral()
    }
    dependencies {
        test('org.spockframework:spock-grails-support:0.7-groovy-2.0') {
            export = false
        }
        build 'com.google.gwt:gwt-user:2.4.0', {
            export = false
        }
    }
    plugins {
        test(':spock:0.7') {
            export = false
        }
        build(':release:3.0.1', ':rest-client-builder:2.0.1') {
            export = false
        }
        build ':extended-dependency-manager:0.5.5'
        runtime ':resources:1.2.8'
    }
}

grails.release.scm.enabled = false

// Needed to run commands locally that trip over the GWT_HOME check.
gwt { version = '2.4.0' }
