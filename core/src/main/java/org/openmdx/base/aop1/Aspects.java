package org.openmdx.base.aop1;

import java.util.HashMap;
import java.util.Map;

public class Aspects {

	private Aspects() {
		// Avoid instantiation
	}

	private static final Map<String, String> ASPECT_TYPES = new HashMap<String, String>(3);
	
	static {
		ASPECT_TYPES.put("org:openmdx:state2:DateState", "org:openmdx:state2:BasicState");
		ASPECT_TYPES.put("org:openmdx:state2:DateTimeState", "org:openmdx:state2:BasicState");
		ASPECT_TYPES.put("org:openmdx:role2:Role", "org:openmdx:role2:Role");
	}

    /**
     * Tells whether the class denotes an aspect base class.
     *  
     * @param qualifiedClassName
     * 
     * @return {@code true} if the class denotes an aspect base class
     */
    public static boolean isAspectBaseClass(
        String qualifiedClassName
    ){
        return ASPECT_TYPES.containsKey(qualifiedClassName);
    }

    /**
     * Determine the aspect type
     * 
     * @param aspectBaseClassCandidate 
     * 
     * @return the aspect type in case of an aspect base class, {@code null} otherwise
     */
    public static String getAspectType(String aspectBaseClassCandidate) {
    	return ASPECT_TYPES.get(aspectBaseClassCandidate);
    }
    
}
