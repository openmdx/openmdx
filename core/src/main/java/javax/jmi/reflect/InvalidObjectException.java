package javax.jmi.reflect;

/** Exception thrown when an object operation detects
 * a non-existing (i.e. deleted) object.
 */
@SuppressWarnings("serial")
public class InvalidObjectException extends JmiException {
    
    /**
     * Constructs an {@code InvalidObjectException} without detail message.
     * @elementInError element in error.
     */
    public InvalidObjectException(RefObject elementInError) {
        super(elementInError);
    }

    /**
     * Constructs an {@code InvalidObjectException} with the specified detail message.
     * @elementInError element in error.
     * @param msg the detail message.
     */
    public InvalidObjectException(RefObject elementInError, String msg) {
        super(elementInError, msg);
    }
}


