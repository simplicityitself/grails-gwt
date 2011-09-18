package org.codehaus.groovy.grails.plugins.gwt

import java.util.concurrent.Executors
import java.util.concurrent.Callable

/**
 * Manage GWT Compilation.
 *
 * Remove from scripting environment to give more opportunity to use threading when this is appropriate
 */
class GWTCompiler {
  List modules

  def numThreads
  boolean draft = false
  def baseDir
  def logDir

  //TODO, support the property passthrough
  def gwtOutputStyle ="PRETTY"
  def usingGwt16
  def gwtOutputPath
  def compileReport
  def gwtModuleList
  def grailsSettings
  def gwtRun
  def compilerClass
  def failed

  public int compileAll() {
    logDir = new File(baseDir, "target/gwt/logs")
    logDir.mkdirs()

    long then = System.currentTimeMillis()
    modules = gwtModuleList ?: findModules("${baseDir}/src/gwt")

    if (numThreads) {
      println "Configured to use ${numThreads} (detected ${Runtime.runtime.availableProcessors()} available hardware threads)"
    } else {
      println "Auto configuring to use all of the ${Runtime.runtime.availableProcessors()} available hardware threads"
      numThreads = Runtime.runtime.availableProcessors()
    }

    if (compileReport) {
      println "Will generate a compilation report"
    }
    if (gwtOutputStyle) {
      println "Using GWT JS Syle ${gwtOutputStyle}"
    }
    if (draft) {
      println "Draft compilation (not for production)"
    }
    println "Will compile ${modules.size()} modules"

    failed = []

    //TODO, select a compilation strategy.  if 2 or less modules, select GWT worker, otherwise full parallel.

    if (shouldUseFullParallel()) {
      fullParallelCompile()
    } else {
      gwtWorkerCompile()
    }

    long now = System.currentTimeMillis()

    println "Compilation run completed in ${(now - then) / 1000} seconds"

    if (failed) {
      println "The following modules have FAILED COMPILATION :-"
      failed.each {
        println "     * ${it}"
      }
      println "\nGWT Compilation has FAILED, logs are available at ${logDir.absolutePath}, dumping to console."

      failed.each {
        println "************************************************************************************************"
        println "    ${it}"
        println "************************************************************************************************"
        new File(logDir, "FAILED-${it}.log").eachLine {
          println "  | ${it}"
        }
      }
      return 1
    }
    return 0
  }

  boolean shouldUseFullParallel() {
    //TODO, respect config option
    //TODO, if more than 2 modules then full parallel by default, otherwise GWT worker
    return true
  }

  def gwtWorkerCompile() {
    println "Selected GWT Worker parallel compilation"
  }

  def fullParallelCompile() {
    println "Selected Full Parallel compilation :-"
    def executor = Executors.newFixedThreadPool(numThreads)

    int remaining = 0

    modules.each { moduleName ->
      remaining++
      executor.submit({
        try {
          compile(moduleName)
        } catch (Exception ex) {
          failed << moduleName
          if (!(ex instanceof GwtCompilationException)) {
            ex.printStackTrace()
          }
        } finally {
          synchronized (executor) {
            remaining --
            executor.notifyAll()
          }
        }
      } as Callable)
    }

    while(remaining > 0) {
      synchronized (executor) {
        executor.wait(500)
      }
    }

    executor.shutdownNow()
  }

  def compile(String moduleName) {
    def b = new AntBuilder()
    println "  Compiling ${moduleName}"

    def logFile = new File(logDir, "${moduleName}.log")

    logFile.delete()

    logFile << "================================================================\n"
    logFile << "   Compilation started at ${new Date()}\n"
    logFile << "================================================================\n\n"

    logFile << "Base Dir = ${baseDir}\n"

    try {

      def result = gwtRun(compilerClass, [fork:true, output:"${logFile.absoluteFile}", error:"${logFile.absoluteFile}", append:true]) {
          jvmarg(value: '-Djava.awt.headless=true')
          arg(value: '-style')
          arg(value: gwtOutputStyle)

        //TODO, this should move to the GWT WOrkers strategy
//          // Multi-threaded compilation.
//          if (usingGwt16 && numCompileWorkers > 0) {
//              arg(value: "-localWorkers")
//              arg(value: numCompileWorkers)
//          }

          // Draft compile - GWT 2.0+ only
//          if (gwtDraftCompile) {
//              arg(value: "-draftCompile")
//          }

          // The argument specifying the output directory depends on
          // the version of GWT in use.
          if (usingGwt16) {
              // GWT 1.6 uses a different directory structure, and
              // hence arguments to previous versions.
              arg(value: "-war")
          }
          else {
              arg(value: "-out")
          }

          arg(value: gwtOutputPath)
          arg(value: moduleName)
      }
      logFile << "================================================================\n"
      logFile << "   Compilation finished at ${new Date()}\n"
      logFile << "================================================================\n\n"

      if (result != "0") {
        def newLogFile = new File(logDir, "FAILED-${moduleName}.log")
        newLogFile.delete()
        logFile.renameTo(newLogFile)
        println "   module ${moduleName} FAILED, output is available in ${newLogFile.absolutePath}"
        throw new GwtCompilationException()
      } else {
        println "   module ${moduleName} SUCCEEDED"
      }

    } catch (Exception ex) {
      logFile << ex.getMessage()
      throw ex
    }
  }
  //TODO, remove the copy paste here by passing a reference through.
  /**
 * Searches a given directory for any GWT module files, and
 * returns a list of their fully-qualified names.
 * @param searchDir A string path specifying the directory
 * to search in.
 * @param entryPointOnly Whether to find modules that contains entry-points (ie. GWT clients)
 * @return a list of fully-qualified module names.
 */
def findModules(String searchDir, boolean entryPointOnly = true) {
    def modules = []
    def baseLength = searchDir.size()

    def searchDirFile = new File(searchDir)
    if (searchDirFile.exists()) {
        searchDirFile.eachFileRecurse { File file ->
            // Replace Windows separators with Unix ones.
            def filePath = file.path.replace('\\' as char, '/' as char)

            // Chop off the search directory.
            filePath = filePath.substring(baseLength + 1)

            // Now check whether this path matches a module file.
            def m = filePath =~ /([\w\/]+)\.gwt\.xml$/
            if (m.count > 0) {
                // now check if this module has an entry point
                // if there's no entry point, then it's not necessary to compile the module
                if (!entryPointOnly || file.text =~ /entry-point/) {
                    // Extract the fully-qualified module name.
                    modules << m[0][1].replace('/' as char, '.' as char)
                }
            }
        }
    }

    return modules
}

}

class GwtCompilationException extends Exception {}
