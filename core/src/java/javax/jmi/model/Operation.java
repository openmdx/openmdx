package javax.jmi.model;

import javax.jmi.reflect.*;

@SuppressWarnings({"unused","unchecked"})
public interface Operation extends BehavioralFeature {
    public boolean isQuery();
    public void setQuery(boolean newValue);
    public java.util.List getExceptions();
}
