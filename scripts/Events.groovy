// This script is loaded at package time as well as once it has been
// installed. To determine whether installation has occurred or not,
// we check whether 'plugins/gwt-0.2.2' has been expanded or not.
if ('@plugin.basedir@'[1..-2] == 'plugin.basedir') {
    // The plugin is not installed, so assume we are in the root of
    // the plugin project.
    includeTargets << new File('scripts/_Internal.groovy')
}
else {
    includeTargets << new File('@plugin.basedir@/scripts/_Internal.groovy')
}

// Called when the compilation phase completes.
eventCompileStart = { type ->
    if (type != 'source') {
        return
    }

    // Compile the GWT modules. This target is provided by '_Internal'.
    compileGwtModules()
}
