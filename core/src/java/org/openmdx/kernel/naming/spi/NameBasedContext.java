/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: NameBasedContext.java,v 1.7 2008/01/09 15:55:07 hburger Exp $
 * Description: Name Based Context
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/09 15:55:07 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
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
package org.openmdx.kernel.naming.spi;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkException;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.Reference;
import javax.naming.spi.NamingManager;

/**
 * Name Based Context
 */
public abstract class NameBasedContext extends AbstractContext {
        
    /**
     * Constructor
     */
    protected NameBasedContext(
    ) {
        super();
    }


    //------------------------------------------------------------------------
    // Abstract Query Methods
    //------------------------------------------------------------------------

    /**
     * Resolve a binding
     * 
     * @param nameComponent
     * @return the object bound to the given name or null if not found
     * 
     * @throws NamingException
     */
    protected abstract Object resolveLink(
        String nameComponent
    ) throws NamingException;

    /**
     * List the bindings
     * 
     * @return this context's bindings
     * 
     * @throws NamingException
     */
    protected abstract NamingEnumeration<Binding> listBindings(
    ) throws NamingException;
    
    /**
     * List the bindings
     * 
     * @return this context's name class pairs
     * 
     * @throws NamingException
     */
    protected NamingEnumeration<NameClassPair> list(
    ) throws NamingException {
        return new NameClassPairs(listBindings());
    }

    
    //------------------------------------------------------------------------
    // Abstract Update Methods
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
     */
    public void bind(Name name, Object obj) throws NamingException {
        switch(name.size()){
            case 0: throw new NamingException(NAME_EMPTY);
            case 1: throw new OperationNotSupportedException();
            default: lookupPrefix1(name).bind(name.getSuffix(1), obj);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#unbind(java.lang.String)
     */
    public void unbind(
        Name name
    ) throws NamingException {
        switch(name.size()){
            case 0: throw new NamingException(NAME_EMPTY);
            case 1: throw new OperationNotSupportedException();
            default: lookupPrefix1(name).unbind(name.getSuffix(1));
        }        
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#createSubcontext(java.lang.String)
     */
    public Context createSubcontext(
         Name name
    ) throws NamingException {
        switch(name.size()){
            case 0: throw new NamingException(NAME_EMPTY);
            case 1: throw new OperationNotSupportedException();
            default: return lookupPrefix1(name).createSubcontext(name.getSuffix(1)); 
        }        
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#destroySubcontext(java.lang.String)
     */
    public void destroySubcontext(
        Name name
    ) throws NamingException {
        switch(name.size()){
            case 0: throw new NamingException(NAME_EMPTY);
            case 1: throw new OperationNotSupportedException();
            default: lookupPrefix1(name).destroySubcontext(name.getSuffix(1)); 
        }        
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
     */
    public void rebind(
        Name name, 
        Object obj
    ) throws NamingException {
        switch(name.size()){
            case 0: throw new NamingException(NAME_EMPTY);
            case 1: throw new OperationNotSupportedException();
            default: lookupPrefix1(name).rebind(name.getSuffix(1),obj); 
        }        
    }
    
    
    //------------------------------------------------------------------------
    // Name Based Methods
    //------------------------------------------------------------------------

    
    /* (non-Javadoc)
     * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
     */
    public Name composeName(
        Name name, 
        Name prefix
    ) throws NamingException {
        return ((Name)prefix.clone()).addAll(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#getNameParser(java.lang.String)
     */
    public NameParser getNameParser(
        Name name
    ) throws NamingException {
        return name.isEmpty() ?
            NameBasedContext.nameParser :
            lookupPrefix1(name).getNameParser(name.getSuffix(1));
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    public Object lookup(
        Name name
    ) throws NamingException {
        switch(name.size()){
            case 0: return new DelegatingContext(this);
            case 1: return resolve(name.get(0));
            default: return lookupPrefix1(name).lookup(name.getSuffix(1)); 
        }        
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#lookupLink(java.lang.String)
     */
    public Object lookupLink(
        Name name
    ) throws NamingException {
        switch(name.size()){
            case 0: return this;
            case 1: return resolveLink(name.get(0));
            default: return lookupPrefix1(name).lookupLink(name.getSuffix(1)); 
        }        
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#listBindings(java.lang.String)
     */
    public NamingEnumeration<Binding> listBindings(
        Name name
    ) throws NamingException {
        return name.isEmpty() ? 
            listBindings() : 
            lookupPrefix1(name).listBindings(name.getSuffix(1));
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#list(java.lang.String)
     */
    public NamingEnumeration<NameClassPair> list(
        Name name
    ) throws NamingException {
        return name.isEmpty() ?
            list() :
            lookupPrefix1(name).list(name.getSuffix(1));
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
     */
    public void rename(
         Name oldName, 
         Name newName
    ) throws NamingException {
        if(oldName.isEmpty() | newName.isEmpty()) throw new NamingException(NAME_EMPTY);
        Object object = lookupLink(oldName);
        bind(newName, object);
        unbind(oldName);
    }

    /**
     * Resolve a binding or throw an exception
     * 
     * @param nameComponent
     * @return the object bound to the given name but never null
     * 
     * @throws NamingException
     */
    protected Object resolve(
        String nameComponent
    ) throws NamingException {
        Object object = null;
        try {
        	object = resolveLink(nameComponent);
            if(object instanceof LinkRef){
                String link = ((LinkRef)object).getLinkName();
                boolean relative = link.startsWith(".");
                try {
                    if(relative) {
                        return lookup(link.substring(1));
                    } else {
                        Context initialContext = new InitialContext(this.environment);
                        try {
                            return initialContext.lookup(link);
                        } finally {
                            initialContext.close();
                        }
                    }
                } catch (NamingException namingException) {
                    LinkException linkException = new LinkException(
                        "Could not resolve " + (relative ? "relative" : "absolute") + " link"
                    );
                    linkException.setRootCause(namingException);
                    linkException.setLinkRemainingName(nameParser.parse(link));
                    throw linkException;
                }
            } else if (object instanceof Reference){
                try {
                    return NamingManager.getObjectInstance(
                    	object, 
						NameBasedContext.nameParser.parse(nameComponent), 
						this, 
						this.environment
					);
                } catch (Exception e) {
                    NamingException namingException = new NamingException(
                        "Could not resolve reference"
                    );
                    namingException.setRootCause(e);
                    throw namingException;
                }
            } else if (object instanceof Context){
                return new DelegatingContext((Context)object);
            } else {
                return object;
            }
        } catch (NamingException namingException){
            namingException.setRemainingName(NameBasedContext.nameParser.parse(nameComponent));
            namingException.setResolvedObj(object);
            throw namingException;
        }
    }
    
    /**
     * Resolve a names first cmponent
     * 
     * @param name
     * @return the requested object by delegation
     * @throws NamingException
     */
    private Context lookupPrefix1(
        Name name
    ) throws NamingException {
        if(name.isEmpty()) throw new NamingException(NAME_EMPTY);
        try {
            return (Context)resolve(name.get(0));
        } catch (ClassCastException exception) {
            NamingException namingException = new NotContextException();
            namingException.setRemainingName(name);
            namingException.setRootCause(exception);
            throw namingException;
        }
    }

    /**
     * 
     */
    private static final String NAME_EMPTY = "Empty name not allowed for this operation";

    
    //------------------------------------------------------------------------
    // String Based Methods
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
     */
    public void bind(
        String name, 
        Object obj
    ) throws NamingException {
        bind(NameBasedContext.nameParser.parse(name), obj);
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#unbind(java.lang.String)
     */
    public void unbind(
        String name
    ) throws NamingException {
        unbind(NameBasedContext.nameParser.parse(name));
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
     */
    public String composeName(
        String name, 
        String prefix
    ) throws NamingException {
    	return composeName(
    		NameBasedContext.nameParser.parse(name),
			NameBasedContext.nameParser.parse(prefix)
		).toString();
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#createSubcontext(java.lang.String)
     */
    public Context createSubcontext(
         String name
    ) throws NamingException {
        return createSubcontext(NameBasedContext.nameParser.parse(name));
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#destroySubcontext(java.lang.String)
     */
    public void destroySubcontext(
        String name
    ) throws NamingException {
        destroySubcontext(NameBasedContext.nameParser.parse(name));
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#getNameParser(java.lang.String)
     */
    public NameParser getNameParser(
        String name
    ) throws NamingException {
        return "".equals(name) ?
            NameBasedContext.nameParser :
            getNameParser(NameBasedContext.nameParser.parse(name));
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#listBindings(java.lang.String)
     */
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException
    {
        return listBindings(NameBasedContext.nameParser.parse(name));
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#list(java.lang.String)
     */
    public NamingEnumeration<NameClassPair> list(
        String name
    ) throws NamingException {
        return list(NameBasedContext.nameParser.parse(name));
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    public Object lookup(
        String name
    ) throws NamingException {
        return lookup(NameBasedContext.nameParser.parse(name));
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#lookupLink(java.lang.String)
     */
    public Object lookupLink(
        String name
    ) throws NamingException {
        return lookupLink(NameBasedContext.nameParser.parse(name));
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
     */
    public void rebind(
        String name, 
        Object obj
    ) throws NamingException {
        rebind(NameBasedContext.nameParser.parse(name), obj);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
     */
    public void rename(
         String oldName, 
         String newName
    ) throws NamingException {
        rename(NameBasedContext.nameParser.parse(oldName), NameBasedContext.nameParser.parse(newName));
    }

}