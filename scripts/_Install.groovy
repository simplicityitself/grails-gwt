

//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'Ant' to access a global instance of AntBuilder
//

// Create the directory for storing GWT files.
ant.mkdir(dir: "${basedir}/web-app/gwt")

// add gwt-user.jar and the others to compile dependencies 
// otherwise it is not possible to compile plugin classes
// if installing plugin is just a part of some other grails workflow (i.e. testing)

// updating classpath might fail for an unknown reason at the moment
// therefore wrap it in try-catch block
// just to avoid stoping the install process.
// Updating classpath here is only important
// if the plugin installation is happening within some other task
// that requires code compilation
// this could be avoided by doing 'grails refresh-dependencies' first
try {
  includeTargets << new File("${gwtPluginDir}/scripts/_GwtInternal.groovy")
  updateClasspath()
} catch (Throwable e) {
  // show the error message and the stacktrace
  println 'error by installing gwt plugin: '+e.message
  e.printStackTrace()
}