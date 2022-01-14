/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Hashtable Based Context
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.lightweight.naming.spi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.Reference;
import javax.naming.Referenceable;


/**
 * Hash Map Based Context
 */
public class HashMapContext extends NameBasedContext {

	/**
	 * Constructor
	 * 
	 * @param environment
	 * @param namePrefix
	 * @param nameSuffix
	 */
    public HashMapContext(
        Map<?, ?> environment,
        Context namePrefix, 
        String nameSuffix
    ){
        super(environment);
        this.namePrefix = namePrefix;
        this.nameSuffix = nameSuffix;
    }

    /**
     * Constructor
     */
    public HashMapContext(
        Map<?, ?> environment,
        NameParser nameParser
    ){
        this(environment, null, null);
    }
    
    /**
     * getNameInNamespace is supported if namePrefix is not null.
     */
    private Context namePrefix;

    /**
     * 
     */
    private String nameSuffix;

    /**
     * Using a HashMap instead of a HashTable allows null values to be stored.
     */
    private Map<String, Object> bindings = new HashMap<String, Object>();
    

    //------------------------------------------------------------------------
    // Extends AbstractContext
    //------------------------------------------------------------------------

    /**
     * Resolve a binding
     * 
     * @param nameComponent
     * @return the object bound to the given name or null if not found
     * 
     * @throws NamingException
     */
    @Override
    protected Object resolveLink(
        String nameComponent
    ) throws NamingException {
    	synchronized (this.bindings){
    		Object object = this.bindings.get(nameComponent);
    		if(
    			object == null && 
				!this.bindings.containsKey(nameComponent)
			) throw new NameNotFoundException(
                "No object bound to \"" + nameComponent + '"'
            );
    		return object;
    	} 
    }

    /**
     * List the bindings
     * 
     * @return this context's bindings
     * 
     * @throws NamingException
     */
    @Override
    protected NamingEnumeration<Binding> listBindings(
    ) throws NamingException {
    	synchronized (this.bindings){
            return new Bindings(this.bindings.entrySet());
    	} 
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
     */
    @Override
    public void bind(Name name, Object obj) throws NamingException {
        if(name.size() == 1) synchronized (this.bindings){
            String nameComponent = name.get(0);
            if(this.bindings.containsKey(nameComponent)) throw new NameAlreadyBoundException(
                "There is already an object bound to \"" + nameComponent + '"'
            );
            if(obj instanceof Referenceable) {
                Reference reference = ((Referenceable)obj).getReference();
                if(reference == null) {
                    this.bindings.put(
                        nameComponent, 
                        obj
                    );
                } else {
                    this.bindings.put(
                        nameComponent, 
                        reference
                    );
                }
            } else {
                this.bindings.put(
                    nameComponent, 
                    obj
                );
            }
            if(obj instanceof HashMapContext){
                HashMapContext subContext = (HashMapContext) obj;
                subContext.namePrefix = this;
            }
        } else {
            super.bind(name, obj);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#unbind(java.lang.String)
     */
    @Override
    public void unbind(
        Name name
    ) throws NamingException {
        if(name.size() == 1) synchronized(this.bindings){
            String nameComponent = name.get(0);
            this.bindings.remove(nameComponent);
        } else {
            super.unbind(name);
        }
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#createSubcontext(java.lang.String)
     */
    @Override
    public Context createSubcontext(
         Name name
    ) throws NamingException {
        if(name.size() == 1) synchronized(this.bindings){ 
            String nameComponent = name.get(0);
            Context subContext = new HashMapContext(getEnvironment(), this, nameComponent);
            bind(name, subContext);
            return subContext;
        } else {
            return super.createSubcontext(name);
        }
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#destroySubcontext(java.lang.String)
     */
    @Override
    public void destroySubcontext(
        Name name
    ) throws NamingException {
        if(name.size() == 1) synchronized(this.bindings){
            String nameComponent = name.get(0);
            if(!this.bindings.containsKey(nameComponent))return;
            Object candidate = resolveLink(nameComponent);
            if(
                candidate instanceof HashMapContext &&
                ((HashMapContext)candidate).namePrefix == this
            ){
                unbind(name);
            } else throw new NotContextException(
                "The context has not been created with this.createSubcontext()"            
            );        
        } else {
            super.destroySubcontext(name);
        }
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
     */
    @Override
    public void rebind(
        Name name, 
        Object obj
    ) throws NamingException {
        if(name.size() == 1) synchronized(this.bindings){
            String nameComponent = name.get(0);
            this.bindings.put(nameComponent, obj);
        } else {
            super.rebind(name, obj);
        }
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#getNameInNamespace()
     */
    public String getNameInNamespace() throws NamingException {
        if(this.namePrefix == null) {
            return this.nameSuffix;
        } else {
            String namePrefix = this.namePrefix.getNameInNamespace();
            return "".equals(namePrefix) ? this.nameSuffix : namePrefix + '/' + this.nameSuffix;
        }
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#close()
     */
    @Override
    public void close() throws NamingException {
        //
    }
    

    //------------------------------------------------------------------------
    // Class Bindings
    //------------------------------------------------------------------------
    
    /**
     * Bindings
     * <p>
     * Convert each Map.Entry to a Binding
     */
    private final class Bindings implements NamingEnumeration<Binding> {

        Iterator<Map.Entry<String,Object>> iterator;

        Bindings(
            Set<Map.Entry<String,Object>> entries             
        ){
            this.iterator = entries.iterator();
        }
        
        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#next()
         */
        public Binding next() throws NamingException {
            Map.Entry<String,Object> entry = this.iterator.next();
            String name = entry.getKey();
			Binding binding = new Binding(name, entry.getValue());
			binding.setNameInNamespace(
	            getNameInNamespace() + "/" + name
	        );
			return binding;
        }

        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#hasMore()
         */
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#close()
         */
        public void close() throws NamingException {
            this.iterator = null;
        }

        /* (non-Javadoc)
         * @see java.util.Enumeration#hasMoreElements()
         */
        public boolean hasMoreElements() {
            return this.iterator.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Enumeration#nextElement()
         */
        public Binding nextElement() {
        	try {
				return next();
			} catch (NamingException e) {
				throw new RuntimeException(e);
			}
        }
        
    }

}
