package javax.jmi.model;

import javax.jmi.reflect.*;

@SuppressWarnings("unused")
public interface StructuralFeature extends Feature, TypedElement {
    public MultiplicityType getMultiplicity();
    public void setMultiplicity(MultiplicityType newValue);
    public boolean isChangeable();
    public void setChangeable(boolean newValue);
}
