/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: java URL Context Factory
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.lightweight.naming.java;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.ObjectFactory;

import org.openmdx.kernel.lightweight.naming.spi.HashMapContext;

/**
 * java URL Context Factory
 */
public class javaURLContextFactory implements ObjectFactory {
    
   /**
    * Creates an object using the location or reference information
    * specified.  
    * 
    * @param obj The possibly null object containing location or reference 
    *      information that can be used in creating an object.
    * @param name The name of this object relative to <code>nameCtx</code>,
    *      or null if no name is specified.
    * @param nameCtx The context relative to which the <code>name</code>
    *      parameter is specified, or null if <code>name</code> is
    *      relative to the default initial context.
    * @param environment The possibly null environment that is used in
    *      creating the object.
    * @return The object created; null if an object cannot be created.
    * @exception Exception if this object factory encountered an exception
    * while attempting to create an object, and no other object factories are
    * to be tried.
    */
    public Object getObjectInstance(
       Object obj, 
       Name name, 
       Context nameCtx,
       Hashtable<?,?> environment
    ) throws NamingException {
        Object object = obj;
        if(object instanceof Object[]){
            Object[] urls = (Object[]) object;
            if(urls.length == 0) {
                throw new NoInitialContextException("URL array is empty");
            } else {
                object = urls[0]; // Just take the first of the equivalent URLs
            }
        }
        if(object == null){
            return javaContext;
        } else if(object instanceof String){
            String url = (String) object;
            if(!url.startsWith("java:")) throw new NoInitialContextException(
                "'java' URL scheme expected: " + url
            );
            return javaContext.lookup(url);
        } else { 
            throw new NoInitialContextException(
                "'java' URL supports String object only: " + object.getClass().getName()
            );
        }
    }

    /**
     * Retrieve the <code>java:comp</code> context
     * 
     * @return the component context
     * 
     * @throws NamingException 
     */
    private static Context getComponentContext(
    ) throws NamingException {
        try {
            return (Context) javaContext.lookup("java:comp");
        } catch (NameNotFoundException exception) {
            return javaContext.createSubcontext("java:comp");
        }
    }
    
    /**
     * Process <code>org.openmdx.comp&hellip;</code> properties
     * 
     * @param properties
     * 
     * @throws NoInitialContextException
     */
    public static void populate(
        Map<?,?> properties
    ) throws NoInitialContextException {
        if(properties != null && !properties.isEmpty()) try {
            Context compContext = getComponentContext();
            for(Map.Entry<?,?> e : properties.entrySet()) {
                Object key = e.getKey();
                if(key instanceof String) {
                    String name = (String) key;
                    if(name.startsWith("org.openmdx.comp.")) {
                        String[] path = name.split("\\.");
                        Context context = compContext;
                        int t = path.length - 1;
                        for(int i = 3; i < t; i++){
                            try {
                                context = (Context) context.lookup(path[i]);
                            } catch (NameNotFoundException exception) {
                                context = context.createSubcontext(path[i]);
                            }
                        }
                        Object value = e.getValue();
                        if(value instanceof String){
                            String s = (String) value;
                            int c = s.indexOf(':');
                            int q = s.indexOf('?');
                            if(c > 0 && (q < 0 || c < q)) {
                                value = new LinkRef(s); 
                            }
                        }
                        context.rebind(
                            path[t], 
                            value
                        );
                    }
                }
            }
        } catch (Exception exception) {
            throw (NoInitialContextException) new NoInitialContextException(
                "Could not populate the non-managed environment's env context"
            ).initCause(
                exception
            );
        }
    }

    /**
     * The initial java: context
     */
    private final static Context javaContext = new HashMapContext(null, null, "");

}
