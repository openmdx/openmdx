package javax.jmi.reflect;

import java.util.Collection;

@SuppressWarnings("rawtypes")
public interface RefBaseObject {
    RefObject refMetaObject();
    RefPackage refImmediatePackage();
    RefPackage refOutermostPackage();
    String refMofId();
    Collection refVerifyConstraints(boolean deepVerify);
    boolean equals(Object other);
    int hashCode();
}
