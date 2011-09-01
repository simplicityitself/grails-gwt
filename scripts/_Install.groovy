//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'Ant' to access a global instance of AntBuilder
//

// Create the directory for storing GWT files.
ant.mkdir(dir: "${basedir}/web-app/gwt")

// add gwt-user.jar to compile dependencies 
// otherwise it is not possible to compile plugin classes
// if installing plugin is just a part of some other grails workflow (i.e. testing)
 
ant.property(environment: "env")
gwtHome = ant.project.properties."env.GWT_HOME"
if (gwtHome) {
   new File(gwtHome).eachFileMatch(~/^gwt-user\.jar$/) { File f ->
        grailsSettings.compileDependencies << f
   }
}
