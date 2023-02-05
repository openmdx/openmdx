package javax.jmi.xmi;

@SuppressWarnings("serial")
public class MalformedXMIException extends Exception {
    
    /**
     * Creates a new instance of {@code MalformedXMIException} without detail message.
     */
    public MalformedXMIException() {
    }
    
    
    /**
     * Constructs an instance of {@code MalformedXMIException} with the specified detail message.
     * @param msg the detail message.
     */
    public MalformedXMIException(String msg) {
        super(msg);
    }
}
