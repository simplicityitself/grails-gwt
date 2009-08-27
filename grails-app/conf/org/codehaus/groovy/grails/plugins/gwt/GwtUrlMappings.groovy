package org.codehaus.groovy.grails.plugins.gwt

class GwtUrlMappings {
    static mappings = {
        "/gwt/$module/rpc"(controller: "gwt", action: "index")
    }
}
