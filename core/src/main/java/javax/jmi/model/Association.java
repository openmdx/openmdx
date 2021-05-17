package javax.jmi.model;

import javax.jmi.reflect.*;

@SuppressWarnings("unused")
public interface Association extends Classifier {
    public boolean isDerived();
    public void setDerived(boolean newValue);
}
