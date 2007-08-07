
//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'Ant' to access a global instance of AntBuilder
//
// For example you can create directory under project tree:
// Ant.mkdir(dir:"/home/pal20/dev/projects/grails-gwt/grails-app/jobs")
//

Ant.property(environment:"env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

// Create the directory for storing GWT files.
Ant.mkdir(dir:"${basedir}/web-app/gwt")

// Now copy the gwt.js file into the project's javascript directory.
targetJsFile = new File("${basedir}/web-app/js/gwt.js")
if (targetJsFile.exists()) {
    Ant.input(
        addProperty: "gwt.js.overwrite",
        message: "You already have a 'gwt.js' file installed. Overwrite? [y/n]")

    if (Ant.antProject.properties."gwt.js.overwrite" == "n") {
        return
    }
}

sourceJsFile = new File("${basedir}/plugins/gwt-0.1.1/web-app/js/gwt.js")
Ant.copy(file: sourceJsFile, tofile: targetJsFile, overwrite: true)
 
event("CreatedFile", [ targetJsFile ])
