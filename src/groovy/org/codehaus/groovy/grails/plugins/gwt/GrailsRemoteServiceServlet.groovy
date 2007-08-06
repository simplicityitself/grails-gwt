package org.codehaus.groovy.grails.plugins.gwt

import com.google.gwt.user.client.rpc.SerializationException
import com.google.gwt.user.server.rpc.RemoteServiceServlet
import com.google.gwt.user.server.rpc.RPC
import com.google.gwt.user.server.rpc.RPCRequest

import org.springframework.web.context.support.WebApplicationContextUtils as CtxUtils

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
        RPCRequest rpcRequest = RPC.decodeRequest(payload, null)

        // The request contains the method to invoke and the arguments
        // to pass.
        def serviceMethod = rpcRequest.method

        // Get the name of the interface that declares this methods,
        // and strip off the package and any 'Service' suffix.
        def ifaceName = serviceMethod.declaringClass.name
        def pos = ifaceName.lastIndexOf('.')
        if (pos != -1) {
            ifaceName = ifaceName.substring(pos + 1)
        }
        if (ifaceName.endsWith('Service')) {
            ifaceName = ifaceName.substring(0, ifaceName.size() - 'Service'.size())
        }

        // Work out the name of the Grails service to dispatch this
        // request to.
        def serviceName = "gwt${ifaceName}Service"

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
            return RPC.encodeResponseForSuccess(serviceMethod, retval)
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return RPC.encodeResponseForFailure(serviceMethod, ex)
        }
    }
}
