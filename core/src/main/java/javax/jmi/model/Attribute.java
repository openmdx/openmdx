package javax.jmi.model;

import javax.jmi.reflect.*;

@SuppressWarnings("unused")
public interface Attribute extends StructuralFeature {
    public boolean isDerived();
    public void setDerived(boolean newValue);
}
