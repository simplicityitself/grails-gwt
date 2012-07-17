includeTargets << new File ("${gwtPluginDir}/scripts/_GwtInternal.groovy")

target (default: "Runs the GWT hosted mode client.") {
    depends(parseArguments)

    if (argsMap["params"]) {
        // Check whether a host and port have been specified.
        def m = argsMap["params"][0] =~ /([a-zA-Z][\w\.]*)?:?(\d+)?/
        if (m.matches()) {
            // The user can specify a host, a port, or both if separated
            // by a colon. If either or both are not given, the appropriate
            // defaults are used.
            gwtClientServer = (m[0][1] ? m[0][1] : "localhost") + ":" +
                    (m[0][2] ? m[0][2] : 8080)
        }
    }

    // Start the hosted mode client.
    runGwtClient()
}
