/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Contexts
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.naming;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NotContextException;

/**
 * Contexts
 */
public class Contexts {
   
    /**
     * Constructor
     */
    protected Contexts(
    ){
        // Avoid instantiations
    }

    /**
     * Binds an object to a name creating intermediate contexts if necessary 
     * 
     * @param context
     * @param name the name relative to the context
     * @param object the object to be bound to the name
     * 
     * @return the existing or newly created context
     */
    public static void bind(
        Context context,
        Name name,
        Object object
    ) throws NamingException {
        int i = name.size() - 1;
        getSubcontext(context, name.getPrefix(i)).bind(name.get(i), object);
    }

    /**
     * Binds an object to a name creating intermediate contexts if necessary 
     * 
     * @param context
     * @param name the name relative to the context
     * @param object the object to be bound to the name
     * 
     * @return the existing or newly created context
     */
    public static void bind(
        Context context,
        String name,
        Object object
    ) throws NamingException {
        bind(context, context.getNameParser("").parse(name), object);
    }

    /**
     * Lookup or create a subcontext 
     * 
     * @param context
     * @param name the name relative to the context
     * 
     * @return the existing or newly created context
     */
    public static Context getSubcontext(
        Context context,
        Name name
    ) throws NamingException {
        Context cursor = context;
        for(int i=0; i< name.size(); i++) try {
            cursor = (Context) cursor.lookup(name.get(i));
        } catch (NameNotFoundException exception){
            cursor = cursor.createSubcontext(name.get(i));
        } catch (ClassCastException exception) {
            NamingException namingException = new NotContextException(
                "Intermediate name not bound to a context"
            );
            namingException.setRootCause(exception);
            namingException.setResolvedName(name.getPrefix(i));
            namingException.setRemainingName(name.getSuffix(i));
            throw namingException;
        }
        return cursor;
    }

    /**
     * Lookup or create a subcontext 
     * 
     * @param context
     * @param name the name relative to the context
     * 
     * @return the existing or newly created context
     * @throws NamingException
     */
    public static Context getSubcontext(
        Context context,
        String name
    ) throws NamingException{
        return getSubcontext(
            context,
            context.getNameParser("").parse(name)
        );
    }

	/**
	 * The name under which the lightweight container is bound to the registry  
	 */
	public static String getNamingService (
	){
		return System.getProperty(
			NAMING_SERVICE,
			"org.openmdx.rmi.naming"
		);
	}

	/**
	 * The lightweight container's RMI registry's port.  
	 */
	public static int getRegistryPort (
	){
		return Integer.getInteger(
			REGISTRY_PORT, 
			19523 // 0x4c43 stands for "LC"
		).intValue();		
	}

    /**
     * This system property contains the id under which the lightweight container's  naming service is bound to RMI registry.  
     */
    public static final String NAMING_SERVICE = "org.openmdx.rmi.naming.service";
    
    /**
     * This system property contains the port to be used by the lightweight container's  RMI registry.  
     */
    public static final String REGISTRY_PORT = "org.openmdx.rmi.registry.port";

}
