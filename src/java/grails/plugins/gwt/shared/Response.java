package grails.plugins.gwt.shared;

import java.io.Serializable;

/**
 * Represents the response to a particular action. Each implementation
 * of {@link Action} should have its own corresponding Response. For
 * example, a search action would have a response that contained the
 * (serializable) search results.
 */
public interface Response extends Serializable {
}
