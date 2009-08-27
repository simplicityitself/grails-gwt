package org.grails.plugins.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async version of {@link GwtActionService}.
 */
public interface GwtActionServiceAsync {
    <T extends Response> void execute(Action<T> action, AsyncCallback<T> callback);
}
