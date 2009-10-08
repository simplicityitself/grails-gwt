package grails.plugins.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import grails.plugins.gwt.shared.Action;
import grails.plugins.gwt.shared.Response;

/**
 * Async version of {@link GwtActionService}.
 */
public interface GwtActionServiceAsync {
    <T extends Response> void execute(Action<T> action, AsyncCallback<T> callback);
}
