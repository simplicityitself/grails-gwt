import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

Ant.property(environment: 'env')
grailsHome = Ant.antProject.properties.'env.GRAILS_HOME'
srcDir = 'src/java'

includeTargets << new File ("${grailsHome}/scripts/Init.groovy")

task ('default': 'Creates a new GWT module.') {
    depends(promptForName)

    // The only argument should be the fully qualified name of the GWT
    // module. First, split it into package and name parts.
    def moduleName
    def modulePackage = null
    def pos = args.lastIndexOf('.')
    if (pos != -1) {
        // Extract the name and the package.
        moduleName = args.substring(pos + 1)
        modulePackage = args.substring(0, pos)
    }
    else {
        moduleName = args
    }

    def packagePath = (modulePackage != null ? '/' + modulePackage.replace('.' as char, '/' as char) : '')

    // Now create the module file.
    def targetPath = "${basedir}/${srcDir}${packagePath}"
    def moduleFile = "${targetPath}/${moduleName}.gwt.xml"
    def templatePath = "@plugin.basedir@/src/templates/artifacts"
    def templateFile = "${templatePath}/GwtModule.gwt.xml"

    // Check whether the target module exists already.
    if (new File(moduleFile).exists()) {
        // It does, so find out whether the user wants to overwrite
        // the existing copy.
        Ant.input(
            addProperty:"${moduleName}.overwrite",
            message:"GwtModule: ${moduleName} already exists. Overwrite? [y/n]")

        if (Ant.antProject.properties."${moduleName}.overwrite" == "n") {
            // User doesn't want to overwrite, so stop the script.
            return
        }
    }

    // Copy the template module file over, replacing any tokens in the
    // process.
    Ant.copy(file: templateFile, tofile: moduleFile, overwrite: true)
    Ant.replace(file: moduleFile) {
        Ant.replacefilter(token: '@module.package@', value: (modulePackage != null ? modulePackage + '.' : ''))
        Ant.replacefilter(token: '@module.name@', value: moduleName)
    }
//    Ant.replace(file: moduleFile, token: '@module.name@', value: args)
//    Ant.sequential {
//        copy(file: templateFile, tofile: moduleFile, overwrite: true) {
//            filterset {
//                filter(token: '@module.name@', value: 'test')
//                filter(token: '@TEST@', value: 'test2')
//            }
//        }
//    }

    // Now copy the template client entry point over.
    templateFile = "${templatePath}/GwtClientEntryPoint.java"
    def entryPointFile = "${targetPath}/client/${moduleName}.java"

    Ant.copy(file: templateFile, tofile: entryPointFile, overwrite: true)
    Ant.replace(file: entryPointFile) {
        Ant.replacefilter(token: '@module.package@', value: (modulePackage != null ? modulePackage + '.' : ''))
        Ant.replacefilter(token: '@module.name@', value: moduleName)
    }

    event("CreatedFile", [ moduleFile ])
}
