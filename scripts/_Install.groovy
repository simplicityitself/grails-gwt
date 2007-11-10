//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'Ant' to access a global instance of AntBuilder
//

// Create the directory for storing GWT files.
Ant.mkdir(dir: "${basedir}/web-app/gwt")

// Update scripts with the location of the plugin relative to the project.
def pluginDir = pluginBasedir.replace(basedir.replace('\\' as char, '/' as char), '')[1..-1]
def scripts = [ 'CreateGwtPage', 'CreateGwtModule' ]
scripts.each { script ->
    Ant.replace(
        file: "${pluginBasedir}/scripts/${script}.groovy",
        token: '@plugin.basedir@',
        value: "${pluginDir}")
}