package org.codehaus.groovy.grails.plugins.gwt

import com.google.web.bindery.requestfactory.server.DefaultExceptionHandler
import com.google.web.bindery.requestfactory.server.ServiceLayer
import com.google.web.bindery.requestfactory.server.SimpleRequestProcessor

import javax.annotation.PostConstruct

class RequestFactoryService {

    RfValidationService rfValidationService

    private SimpleRequestProcessor processor

    static transactional = false

    public String process(String jsonRequestString) {
        return processor.process(jsonRequestString)
    }

    @PostConstruct
    public void initProcessor() {
        processor = new SimpleRequestProcessor(ServiceLayer.create(rfValidationService))
        processor.exceptionHandler = new DefaultExceptionHandler()
    }

}
