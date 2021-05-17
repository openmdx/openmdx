package javax.jmi.model;

import javax.jmi.reflect.*;

@SuppressWarnings("unused")
public interface CollectionType extends DataType, TypedElement {
    public MultiplicityType getMultiplicity();
    public void setMultiplicity(MultiplicityType newValue);
}
