package org.openmdx.base.aop1;

import java.util.Arrays;
import java.util.Collection;

public class Aspects {

	private Aspects() {
		// Avoid instantiation
	}

    /**
     * The list of aspect base classes may be extended by overriding isAspectBaseClass()
     */
    private static final Collection<String> ASPECT_BASE_CLASSES = Arrays.asList(
        "org:openmdx:state2:DateState",
        "org:openmdx:state2:DateTimeState",
        "org:openmdx:role2:Role"
    );

    /**
     * Tells whether the class denotes an aspect base class.
     *  
     * @param qualifiedClassName
     * 
     * @return <code>true</code> if the class denotes an aspect base class
     */
    public static boolean isAspectBaseClass(
        String qualifiedClassName
    ){
        return ASPECT_BASE_CLASSES.contains(qualifiedClassName);
    }
    	
}
