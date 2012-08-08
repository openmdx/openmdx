
package javax.jmi.reflect;

/**
 * Superclass for exceptions modeled in MOF.
 */
@SuppressWarnings("serial")
public class RefException extends Exception {
    public RefException() {
    }
    
    public RefException(String msg) {
        super(msg);
    }
}
