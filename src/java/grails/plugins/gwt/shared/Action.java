package grails.plugins.gwt.shared;

import java.io.Serializable;

/**
 * Embodies a client-side action, for example a search, or selecting a
 * contact. It's an incarnation of the standard Command pattern, geared
 * towards client-server communication (hence the {@link Response}).
 * @param <T> The type of the response associated with this action.
 */
public interface Action<T extends Response> extends Serializable {
}
