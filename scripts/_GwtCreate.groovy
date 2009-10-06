includeTargets << grailsScript("_GrailsEvents")

grailsSrcPath = "src/java"
gwtSrcPath = "src/gwt"

/**
 * Takes a fully qualified class name and returns the package name and
 * simple class name as a pair (in that order). If the class name does
 * not include a package, then the package part is <tt>null</tt>.
 */
packageAndName = { String fullClassName ->
    def name = fullClassName
    def pkg = null
    def pos = fullClassName.lastIndexOf('.')
    if (pos != -1) {
        // Extract the name and the package.
        pkg = fullClassName.substring(0, pos)
        name = fullClassName.substring(pos + 1)
    }

    return [ pkg, name ]
}

/**
 * Converts a package name (with '.' separators) to a file path (with
 * '/' separators). If the package is <tt>null</tt>, this returns an
 * empty string.
 */
packageToPath = { String pkg ->
    return pkg != null ? '/' + pkg.replace('.' as char, '/' as char) : ''
}

/**
 * Installs a template file using the given arguments to populate the
 * template and determine where it goes.
 */
installFile = { File targetFile, File templateFile, Map tokens ->
    // Check whether the target file exists already.
    if (targetFile.exists()) {
        // It does, so find out whether the user wants to overwrite
        // the existing copy.
        ant.input(
            addProperty: "${targetFile.name}.overwrite",
            message: "GWT: ${targetFile.name} already exists. Overwrite? [y/n]")

        if (ant.antProject.properties."${targetFile.name}.overwrite" == "n") {
            // User doesn't want to overwrite, so stop the script.
            return
        }
    }

    // Now copy over the template file and replace the various tokens
    // with the appropriate values.
    ant.copy(file: templateFile, tofile: targetFile, overwrite: true)
    ant.replace(file: targetFile) {
        tokens.each { key, value ->
            ant.replacefilter(token: "@${key}@", value: value)
        }
    }

    // The file was created.
    event("CreatedFile", [ targetFile ])
}

/**
 *
 */
installGwtTemplate = { String pkg, String name, String templateName, String srcDir = gwtSrcPath ->
    // First, work out the name of the target file from the name of the
    // template.
    def targetFile = new File("${basedir}/${srcDir}${packageToPath(pkg)}", templateName.replace("Gwt", name))
    def templateFile = new File("${gwtPluginDir}/src/templates/artifacts", templateName)

    installFile(targetFile, templateFile, [ "artifact.package": pkg, "artifact.name": name ])
}
