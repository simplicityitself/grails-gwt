package org.codehaus.groovy.grails.plugins.gwt

import com.google.gwt.user.server.rpc.RPCServletUtils
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.support.RequestContextUtils

import javax.servlet.http.HttpServletResponse

class GwtRequestController {

    private static final String JSON_CHARSET = 'UTF-8'
    private static final String JSON_CONTENT_TYPE = 'application/json'

    RequestFactoryService requestFactoryService

    public static final String GWT_LANGUAGE = 'X-GWT-Language'

    def index = {
        String gwtLanguage = request.getHeader(GWT_LANGUAGE)
        if (gwtLanguage) {
            Locale gwtLocale = new Locale(gwtLanguage)
            RequestContextUtils.getLocaleResolver(request).setLocale(request, response, gwtLocale)
            RequestContextHolder.requestAttributes.setAttribute(GWT_LANGUAGE, gwtLocale, RequestAttributes.SCOPE_REQUEST)
        }
        def jsonRequestString = RPCServletUtils.readContent(request, JSON_CONTENT_TYPE, JSON_CHARSET)
        if (log.isDebugEnabled()) {
            log.debug(">>>$jsonRequestString")
        }
        try {
            def payload = requestFactoryService.process(jsonRequestString)
            if (log.isDebugEnabled()) {
                log.debug("<<<$payload")
            }
            render(text: payload, contentType: JSON_CONTENT_TYPE, encoding: JSON_CHARSET)
        } catch (RuntimeException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            log.error('Unexpected error', e)
        }
    }

}