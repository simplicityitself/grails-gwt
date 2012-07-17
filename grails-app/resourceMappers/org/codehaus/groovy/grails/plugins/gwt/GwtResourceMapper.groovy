package org.codehaus.groovy.grails.plugins.gwt

import org.grails.plugin.resource.mapper.MapperPhase

class GwtResourceMapper {

    def phase = MapperPhase.MUTATION

    static defaultIncludes = ['/gwt/**']

    def map(resource, config) {
        
    }
}