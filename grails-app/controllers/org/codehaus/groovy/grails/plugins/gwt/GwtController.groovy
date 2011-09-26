package org.codehaus.groovy.grails.plugins.gwt

import com.google.gwt.user.client.rpc.SerializationException
import com.google.gwt.user.server.rpc.RPC
import com.google.gwt.user.server.rpc.RPCRequest
import com.google.gwt.user.server.rpc.RemoteServiceServlet

import java.lang.reflect.UndeclaredThrowableException
import javax.servlet.ServletContext
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU
import org.codehaus.groovy.grails.web.context.ServletContextHolder

/**
 * Custom controller that dispatches GWT client requests to the appropriate
 * Grails service.
 */
class GwtController extends RemoteServiceServlet {
    /**
     * The Grails application instance is injected here. We use it to
     * get hold of the Spring application context.
     */
    GrailsApplication grailsApplication

    /**
     * The default action simply delegates to the GWT RemoteServiceServlet.
     */
    def index = {
        doPost(request, response)
        response.outputStream.flush()
    }

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
        def serviceName = GCU.getPropertyName(ifaceName)

        // Get the Spring application context and retrieve the required
        // service from it.
        def service = null
        try {
            service = grailsApplication.mainContext.getBean(serviceName)
        }
        catch (Exception ex) {
            ex.printStackTrace()
            throw ex
        }

        try {
            // Check that the service is exposed via GWT.
            def expose = GCU.getPropertyOrStaticPropertyOrFieldValue(service, "expose")
            if (!expose || !expose.find { it.startsWith("gwt") }) {
                // This is not a GWT exposed service. We can't allow
                // access to it.
                throw new RuntimeException("Cannot access the bean '${serviceName}' via GWT.")
            }

            // Invoke the method on the service and encode the response.
			def serviceMethodName = serviceMethod.name
			def retval = service."$serviceMethodName"(* rpcRequest.parameters)
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

    /**
     * Since this class is not instantiated as a proper servlet, we
     * override this method to provide the servlet context. This allows
     * various bits of the GWT server-side code to work properly.
     */
    ServletContext getServletContext() { return ServletContextHolder.servletContext }

    /**
     * Dummy versions of getter methods because the real ones throw an
     * exception. This is a workaround for the fact that Grails likes
     * to call all the getter methods on controllers. No idea why.
     */
    Enumeration getInitParameterNames() {}
    String getServletInfo() {}
    String getServletName() {}
}
