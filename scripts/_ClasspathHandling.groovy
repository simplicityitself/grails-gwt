
if (getBinding().variables.containsKey("updateClasspath")) return

updateClasspath = { classLoader = null ->
  // Add GWT libraries to compiler classpath.
  if (getBinding().variables.containsKey("gwtHome") || getBinding().variables.containsKey("gwtResolvedDependencies")) {
    maybeUseGwtHomeLibs(classLoader)

    grailsSettings.testDependencies << gwtClassesDir

    maybeUseGwtLibDir()

    maybeUseProvidedDependencies()

    addGwtResolvedDeps()
  }
}

def addGwtResolvedDeps() {

  gwtResolvedDependencies.each { File f ->
    if (!f.name.contains("gwt-dev")) {
      println "Adding ${f.name} to classpath"
      rootLoader.addURL(f.toURL())

      if (classLoader) {
        classLoader.addURL(f.toURL())
      }
    }
    if (f.name.startsWith("gwt-dev") || f.name.startsWith("gwt-user")) {
      haveGwtOnClasspath=true
    } else {
      //grailsSettings.compileDependencies << f
      //grailsSettings.runtimeDependencies << f
    }
    //grailsSettings.testDependencies << f
    gwtDependencies << f
  }
}


def maybeUseProvidedDependencies() {
  if (buildConfig.gwt.use.provided.deps == true) {
    println "Adding Provided Dependencies to the GWT Classpath (Deprecated Feature, to be removed in a future release)"
    grailsSettings.providedDependencies.each { dep ->
      grailsSettings.testDependencies << dep
      gwtDependencies << dep
    }
  }
}

def maybeUseGwtLibDir() {
  if (gwtLibFile.exists()) {
    println "Adding lib/gwt/* to the GWT classpath"
    gwtLibFile.eachFileMatch(~/.+\.jar$/) { f ->
      grailsSettings.testDependencies << f
      gwtDependencies << f
    }
  }
}

def maybeUseGwtHomeLibs(classLoader) {
  if(getBinding().variables.containsKey("gwtHome") && gwtHome) {
    def gwtHomeFile = new File(gwtHome)
    if (gwtHomeFile.exists()) {
      println "Using the GWT installation at ${gwtHome.absolutePath}"
      // Update the dependency lists.
      new File(gwtHome).eachFileMatch(~/^gwt-(dev.*|user.*)\.jar$/) { File f ->
        grailsSettings.providedDependencies << f
        grailsSettings.testDependencies << f
        gwtDependencies << f
        rootLoader.addURL(f.toURL())

        if (classLoader) {
          classLoader.addURL(f.toURL())
        }
      }
      def gwtServlet = new File(gwtHomeFile, "gwt-servlet.jar")
      if (gwtServlet.exists()) {
        grailsSettings.runtimeDependencies << gwtServlet
      }
    }
  }
}