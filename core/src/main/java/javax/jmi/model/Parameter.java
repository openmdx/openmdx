package javax.jmi.model;

import javax.jmi.reflect.*;

@SuppressWarnings("unused")
public interface Parameter extends TypedElement {
    public DirectionKind getDirection();
    public void setDirection(DirectionKind newValue);
    public MultiplicityType getMultiplicity();
    public void setMultiplicity(MultiplicityType newValue);
}
