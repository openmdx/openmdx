package javax.jmi.model;

import javax.jmi.reflect.*;

@SuppressWarnings("unused")
public interface Constant extends TypedElement {
    public String getValue();
    public void setValue(String newValue);
}
