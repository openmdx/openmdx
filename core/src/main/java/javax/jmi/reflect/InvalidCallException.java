package javax.jmi.reflect;

/** Thrown by reflection methods when client tries to call them incorrectly.
 * (e.g. when {@code refSetValue} is called for attribute which is not
 * changeable)
 */
@SuppressWarnings("serial")
public class InvalidCallException extends JmiException {
    
    /**
     * Constructs an {@code InvalidCallException} without detail message.
     * @param objectInError object in error
     * @param elementInError element in error.
     */
    public InvalidCallException(Object objectInError, RefObject elementInError) {
        super(objectInError, elementInError);
    }

    /**
     * Constructs an {@code InvalidCallException} with the specified detail message.
     * @param objectInError object in error
     * @param elementInError element in error.
     * @param msg the detail message.
     */
    public InvalidCallException(Object objectInError, RefObject elementInError, String msg) {
        super(objectInError, elementInError, msg);
    }
}