package @artifact.package@;

import com.google.gwt.event.shared.GwtEvent;

public class @artifact.name@Event extends GwtEvent<@artifact.name@Handler> {
    public static final Type<@artifact.name@Handler> TYPE = new Type<@artifact.name@Handler>();
    
    public @artifact.name@Event() {
    }
    
    public Type<@artifact.name@Handler> getAssociatedType() {
        return TYPE;
    }
    
    protected void dispatch(@artifact.name@Handler handler) {
        handler.onEvent(this);
    }
}
