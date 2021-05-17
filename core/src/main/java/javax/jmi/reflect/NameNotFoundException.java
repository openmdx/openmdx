package javax.jmi.reflect;

/**
 * NameNotFoundException is thrown by the addBefore, modify, and remove 
 * methods to indicate that provided element does not exist.
 */
public class NameNotFoundException 
  extends RefException {
    
    /**
     * 
     */
    private static final long serialVersionUID = 3258410638232663606L;
    private final String name;
    public NameNotFoundException(String name) {
        super("name: " + name);
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
