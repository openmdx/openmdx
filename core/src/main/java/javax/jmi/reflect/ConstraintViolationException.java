package javax.jmi.reflect;

/** Exception thrown when a constraint is violated.
 */
@SuppressWarnings("serial")
public class ConstraintViolationException extends JmiException {
    
    /**
     * Constructs new {@code ConstraintViolationException} without detail message.
     * @param objectInError object violating the constraint.
     * @param elementInError violated constraint object.
     */
    public ConstraintViolationException(Object objectInError, RefObject elementInError) {
        super(objectInError, elementInError);
    }

    /**
     * Constructs an {@code ConstraintViolationException} with the specified detail message.
     * @param objectInError object violating the constraint.
     * @param elementInError violated constraint object.
     * @param msg the detail message.
     */
    public ConstraintViolationException(Object objectInError, RefObject elementInError, String msg) {
        super(objectInError, elementInError, msg);
    }
}
