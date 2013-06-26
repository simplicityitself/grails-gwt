
if (getBinding().variables.containsKey("updateClasspath")) return

updateClasspath = { classLoader = null ->
  // Add GWT libraries to compiler classpath.
  if (getBinding().variables.containsKey("gwtHome") || getBinding().variables.containsKey("gwtResolvedDependencies")) {
    if(getBinding().variables.containsKey("gwtHome")) {
      def gwtHomeFile = new File(gwtHome)
      if (gwtHomeFile.exists()) {
        // Update the dependency lists.
        new File(gwtHome).eachFileMatch(~/^gwt-(dev.*|user.*)\.jar$/) { File f ->
          grailsSettings.providedDependencies << f
          grailsSettings.testDependencies << f
          gwtDependencies << f
        }
        def gwtServlet = new File(gwtHomeFile, "gwt-servlet.jar")
        if (gwtServlet.exists()) {
          grailsSettings.runtimeDependencies << gwtServlet
        }
      }
    }
    grailsSettings.testDependencies << gwtClassesDir
    if (gwtLibFile.exists()) {
      gwtLibFile.eachFileMatch(~/.+\.jar$/) { f ->
        grailsSettings.testDependencies << f
        gwtDependencies << f
      }
    }
    if (buildConfig.gwt.use.provided.deps == true) {
      if (grailsSettings.metaClass.hasProperty(grailsSettings, "providedDependencies")) {
        grailsSettings.providedDependencies.each { dep ->
          grailsSettings.testDependencies << dep
          gwtDependencies << dep
        }
      }
      else {
        ant.echo message: "WARN: You have set gwt.use.provided.deps, " +
                "but are using a pre-1.2 version of Grails. The setting " +
                "will be ignored."
      }
    }

    gwtResolvedDependencies.each { File f ->
      if (!f.name.contains("gwt-dev")) {

        rootLoader.addURL(f.toURL())

        if (classLoader) {
          classLoader.addURL(f.toURL())
        }
      }
      if (f.name.startsWith("gwt-dev") || f.name.startsWith("gwt-user")) {
        grailsSettings.providedDependencies << f
      } else {
        grailsSettings.compileDependencies << f
        grailsSettings.runtimeDependencies << f
      }
      grailsSettings.testDependencies << f
      gwtDependencies << f
    }

  }
}