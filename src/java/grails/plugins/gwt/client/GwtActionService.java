package grails.plugins.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import grails.plugins.gwt.shared.Action;
import grails.plugins.gwt.shared.Response;

/**
 * GWT RPC interface for the plugin's action service. Any client requests
 * that need to go to action handlers on the server-side should go through
 * this interface.
 */
public interface GwtActionService extends RemoteService {
    <T extends Response> T execute(Action<T> action);
}
