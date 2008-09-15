package org.codehaus.groovy.grails.plugins.gwt

import com.google.gwt.user.client.rpc.SerializationException
import com.google.gwt.user.server.rpc.RemoteServiceServlet
import com.google.gwt.user.server.rpc.RPC
import com.google.gwt.user.server.rpc.RPCRequest

import java.util.Locale
import org.springframework.web.context.support.WebApplicationContextUtils as CtxUtils
import java.lang.reflect.UndeclaredThrowableException
import org.codehaus.groovy.grails.commons.GrailsClassUtils

/**
 * Custom GWT RPC servlet that dispatches client requests to Grails
 * services.
 */
class GrailsRemoteServiceServlet extends RemoteServiceServlet {
    /**
     * Overrides the standard GWT servlet method, dispatching RPC
     * requests to services.
     */
    String processCall(String payload) throws SerializationException {
        // First decode the request.
        RPCRequest rpcRequest = RPC.decodeRequest(payload, null, this)

        // The request contains the method to invoke and the arguments
        // to pass.
        def serviceMethod = rpcRequest.method

        // Get the name of the interface that declares this method.
        def ifaceName = serviceMethod.declaringClass.name
        def pos = ifaceName.lastIndexOf('.')
        if (pos != -1) {
            ifaceName = ifaceName.substring(pos + 1)
        }

        // Work out the name of the Grails service to dispatch this
        // request to.
        def serviceName = GrailsClassUtils.getPropertyName(ifaceName)

        // Get the Spring application context and retrieve the required
        // service from it.
        def ctx = CtxUtils.getWebApplicationContext(this.servletContext)
        def service = null;
        try {
            service = ctx.getBean(serviceName)
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

        try {
            // Invoke the method on the service and encode the response.
            def retval = service.invokeMethod(serviceMethod.name, rpcRequest.parameters)
            return RPC.encodeResponseForSuccess(serviceMethod, retval, rpcRequest.serializationPolicy)

        }
        catch (Throwable ex) {
            if (ex instanceof UndeclaredThrowableException) {
                ex = ex.cause
            }

            if (log.warnEnabled) {
                log.warn "Call to service method '${serviceName}.${serviceMethod.name}()' failed", ex
            }

            return RPC.encodeResponseForFailure(serviceMethod, ex, rpcRequest.serializationPolicy)
        }
    }
}
