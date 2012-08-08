/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: HashtableContext.java,v 1.8 2008/03/21 18:48:01 hburger Exp $
 * Description: HashTable Context Class
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:48:01 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
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
package org.openmdx.compatibility.base.naming;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.RefAddr;
import javax.naming.Reference;

import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.naming.Contexts;

/**
 * Implementation of Context interface for deployment instances.
 *
 * @author Philippe Durieux
 * @author Philippe Coq monolog
 * @author Florent Benoit 2003.06.13 : handle Reference object for the lookup.
 * 
 * @deprecated in favour if org.openmdx.kernel.naming.spi.HashtableContext
 */
@SuppressWarnings("unchecked")
class HashtableContext
	extends Contexts  
	implements NameParser, ExtendedContext 
{
    
    /**
     * Environment
     */
    protected final Hashtable environment;

    /**
     * Bindings
     */
    protected final Hashtable bindings = new Hashtable();

	/**
	 * Bindings
	 */
	protected final String nameInNamespace;

	/**
	 * 
	 */ 
	protected final HashtableContext initialContext;
	   
    /**
     * Constructor
     * @param id id of the context.
     * @throws NamingException if naming failed.
     */
    protected HashtableContext(
    	Hashtable environment,
    	HashtableContext initialContext,    	
    	String nameInNamespace
    ) throws NamingException {
		this.environment = environment == null ? 
			new Hashtable() :
			// clone env to be able to change it.
			(Hashtable) (environment.clone());
		this.initialContext = initialContext == null ? 
			this :
			initialContext;
		this.nameInNamespace = nameInNamespace;
    }

	/**
	 * Constructor
	 * @param env initial environment.
	 * @throws NamingException if naming failed.
	 */
	protected HashtableContext(Hashtable environment) throws NamingException {
		this(environment, null, "");
	}


    // ------------------------------------------------------------------
    // Context implementation
    // ------------------------------------------------------------------

    /**
     * Retrieves the named object.
     * Delegate to the String version.
     *
     * @param name the name of the object to look up
     * @return the object bound to name
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(Name name) throws NamingException {
    	SysLog.trace("lookup",name);
		switch (name.size()) {
		  case 0:
			// Empty name means this context
			SysLog.trace("empty name");
			return this;
		  case 1:
			// leaf in the env tree
			Object ret = this.bindings.get(name.get(0));
			if (ret == null) {
				SysLog.trace(" " + name + " not found.");
				throw new NameNotFoundException(name.toString());
			}
			if (ret instanceof LinkRef) {
				RefAddr ra = ((Reference) ret).get(0);
				ret = this.initialContext.lookup((String)ra.getContent());
			} else if (ret instanceof Reference) {
				//Use NamingManager to build an object
				try {
					Object o = javax.naming.spi.NamingManager.getObjectInstance(ret, name, this, environment);
					ret = o;
				} catch (NamingException e) {
					throw e;
				} catch (Exception e) {
					throw new NamingException(e.getMessage());
				}
				if (ret == null) {
					throw new NamingException("Can not build an object with the reference '" + name  + "'");
				}
			}
			return ret;
		  default:	
		  	return firstContext(name).lookup(name.getSuffix(1));
		}
    }

    /**
     * Retrieves the named object.
     *
     * @param name the name of the object to look up
     * @return the object bound to name
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(String name) throws NamingException {
    	return lookup(parse(name));
    }
    
    /**
     * Binds a name to an object.
     * Delegate to the String version.
     *
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NameAlreadyBoundException if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException
     *   if object did not supply all mandatory attributes
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(Name name, Object obj) throws NamingException {
		SysLog.trace("bind",name);
		switch (name.size()) {
		  case 0:
			SysLog.warning("ComponentContext bind empty name ?");
			throw new InvalidNameException("ComponentContext cannot bind empty name");
		  case 1:
			// leaf in the env tree
			if (this.bindings.get(name.get(0)) != null) {
				SysLog.warning("ComponentContext: trying to overbind");
				throw new NameAlreadyBoundException("ComponentContext: Use rebind to bind over a name");
			}
			this.bindings.put(name.get(0), obj);
		    return;
		  default:
			firstContext(name).bind(name.getSuffix(1), obj);
			return;
		}
    }

    /**
     * Binds a name to an object.
     *
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NameAlreadyBoundException if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException
     *   if object did not supply all mandatory attributes
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(String name, Object obj) throws NamingException {
    	bind(parse(name),obj);
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     *
     * @param name
     *  the name to bind; may not be empty
     * @param obj
     *  the object to bind; possibly null
     * @throws javax.naming.directory.InvalidAttributesException
     *   if object did not supply all mandatory attributes
     * @throws NamingException if a naming exception is encountered
     *
     */
    public void rebind(Name name, Object obj) throws NamingException {
		SysLog.trace("rebind",name);
		switch (name.size()) {
		  case 0:
			SysLog.warning("ComponentContext rebind empty name ?");
			throw new InvalidNameException("ComponentContext cannot rebind empty name");
		  case 1:
			// leaf in the env tree
			this.bindings.put(name.get(0), obj);
			return;
		  default:
		    firstContext(name).rebind(name.getSuffix(1), obj);
			return;
		}
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     *
     * @param name
     *  the name to bind; may not be empty
     * @param obj
     *  the object to bind; possibly null
     * @throws javax.naming.directory.InvalidAttributesException
     *   if object did not supply all mandatory attributes
     * @throws NamingException if a naming exception is encountered
     */
    public void rebind(String name, Object obj) throws NamingException {
		rebind(parse(name),obj);
    }

    /**
     * Unbinds the named object.
     * @param name
     *  the name to unbind; may not be empty
     * @throws NameNotFoundException if an intermediate context does not exist
     * @throws NamingException if a naming exception is encountered
     */
    public void unbind(Name name) throws NamingException {
		SysLog.trace("unbind",name);
		switch (name.size()) {
		  case 0:
			SysLog.warning("ComponentContext unbind empty name ?");
			throw new InvalidNameException("ComponentContext cannot unbind empty name");
		  case 1:
			bindings.remove(name.get(0));
			return;
		  default:
		    firstContext(name).unbind(name.getSuffix(1));
		    return;
		}
    }

    /**
     * Unbinds the named object.
     * @param name
     *  the name to unbind; may not be empty
     * @throws NameNotFoundException if an intermediate context does not exist
     * @throws NamingException if a naming exception is encountered
     */
    public void unbind(String name) throws NamingException {
		unbind(parse(name));
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds
     * the old name.
     *
     * @param oldName
     *  the name of the existing binding; may not be empty
     * @param newName
     *  the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(Name oldName, Name newName) throws NamingException {
		SysLog.warning("ComponentContext rename " + oldName + " in " + newName);
		Object obj = lookup(oldName);
		bind(newName, obj);
		unbind(oldName);
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds
     * the old name.
     *
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(String oldName, String newName) throws NamingException {
		rename(parse(oldName),parse(newName));
    }

    /**
     * Enumerates the names bound in the named context, along with the
     * class names of objects bound to them.
     * The contents of any subcontexts are not included.
     *
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the
     *  bindings in this context.  Each element of the
     *  enumeration is of type NameClassPair.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration list(Name name) throws NamingException {
		SysLog.trace("list",name);
		return name.isEmpty() ?
			new ListOfNames(bindings) :
			firstContext(name).list(name.getSuffix(1));
    }

    /**
     * Enumerates the names bound in the named context, along with the
     * class names of objects bound to them.
     *
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the
     *  bindings in this context.  Each element of the
     *  enumeration is of type NameClassPair.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration list(String name) throws NamingException {
		return list(parse(name));
    }

    /**
     * Enumerates the names bound in the named context, along with the
     * objects bound to them.
     * The contents of any subcontexts are not included.
     *
     * If a binding is added to or removed from this context,
     * its effect on an enumeration previously returned is undefined.
     *
     * @param name
     *  the name of the context to list
     * @return an enumeration of the bindings in this context.
     *  Each element of the enumeration is of type
     *  Binding.
     * @throws NamingException if a naming exception is encountered
     *
     */
    public NamingEnumeration listBindings(Name name) throws NamingException {
		SysLog.trace("listBindings",name);
		return name.isEmpty() ?
			// List this context
			new ListOfBindings(bindings) :
			firstContext(name).listBindings(name.getSuffix(1));
    }

    /**
     * Enumerates the names bound in the named context, along with the
     * objects bound to them.
     *
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context.
     *  Each element of the enumeration is of type
     *  Binding.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration listBindings(String name) throws NamingException {
		return listBindings(parse(name));
		
    }

    /**
     * Destroys the named context and removes it from the namespace.
     * Not supported yet.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(Name name) throws NamingException {
		SysLog.warning("destroy", name);
		throw new OperationNotSupportedException("ComponentContext: destroySubcontext");
    }

    /**
     * Destroys the named context and removes it from the namespace.
     * Not supported yet.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(String name) throws NamingException {
    	destroySubcontext(parse(name));
    }

    /**
     * Creates and binds a new context.
     * Creates a new context with the given name and binds it in
     * the target context.
     *
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     *
     * @throws NameAlreadyBoundException if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException
     *  if creation of the subcontext requires specification of
     *  mandatory attributes
     * @throws NamingException if a naming exception is encountered
     */
    public Context createSubcontext(Name name) throws NamingException {
		SysLog.trace("create",name);
		switch (name.size()){
		  case 0:
			SysLog.warning("ComponentContext createSubcontext with empty name ?");
			throw new InvalidNameException("ComponentContext cannot create empty Subcontext");
		  case 1:
			// leaf in the env tree: create ctx and bind it in parent.
			Name subcontextNameInNamespace = parse(this.nameInNamespace);
			subcontextNameInNamespace.add(name.get(0));			
			Context context = new HashtableContext(
				this.environment, 
				this.initialContext,
				subcontextNameInNamespace.toString()
			);
			bindings.put(name.get(0), context);
			return context;
		  default:
		    return firstContext(name).createSubcontext(name.getSuffix(1));
		}
    }

    /**
     * Creates and binds a new context.
     *
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     *
     * @throws NameAlreadyBoundException if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException
     *  if creation of the subcontext requires specification of
     *  mandatory attributes
     * @throws NamingException if a naming exception is encountered
     */
    public Context createSubcontext(String name) throws NamingException {
		return createSubcontext(parse(name));
    }

    /**
     * Retrieves the named object, following links except
     * for the terminal atomic component of the name.
     * If the object bound to name is not a link,
     * returns the object itself.
     *
     * @param name the name of the object to look up
     * @return the object bound to name, not following the
     *  terminal link (if any).
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookupLink(Name name) throws NamingException {
		SysLog.trace("lookupLink", name);
		// To be done. For now: just return the object
		SysLog.warning("ComponentContext lookupLink not implemented yet!");
		return lookup(name);
    }

    /**
     * Retrieves the named object, following links except
     * for the terminal atomic component of the name.
     * If the object bound to name is not a link,
     * returns the object itself.
     *
     * @param name
     *  the name of the object to look up
     * @return the object bound to name, not following the
     *  terminal link (if any)
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookupLink(String name) throws NamingException {
		return lookupLink(parse(name));
    }

    /**
     * Retrieves the parser associated with the named context.
     *
     * @param name
     *  the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *  components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(Name name) throws NamingException {
		return name.isEmpty() ? 
			this.initialContext : 
			firstContext(name).getNameParser(name.getSuffix(1));
    }

    /**
     * Retrieves the parser associated with the named context.
     *
     * @param name
     *  the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *  components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(String name) throws NamingException {
    	return getNameParser(parse(name));
    }

    /**
     * Composes the name of this context with a name relative to
     * this context.
     *
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of prefix and name
     * @throws NamingException if a naming exception is encountered
     */
    public Name composeName(Name name, Name prefix) throws NamingException {
		return ((Name) prefix.clone()).addAll(name);
    }

    /**
     * Composes the name of this context with a name relative to
     * this context.
     *
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of prefix and name
     * @throws NamingException if a naming exception is encountered
     */
    public String composeName(String name, String prefix) throws NamingException {
		return composeName(parse(name),parse(prefix)).toString();
    }

    /**
     * Adds a new environment property to the environment of this
     * context.  If the property already exists, its value is overwritten.
     *
     * @param propName
     *  the name of the environment property to add; may not be null
     * @param propVal
     *  the value of the property to add; may not be null
     * @return the previous value of the property, or null if the property was
     *  not in the environment before
     * @throws NamingException if a naming exception is encountered
     */
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        SysLog.trace(propName,propVal);
        return environment.put(propName, propVal);
    }

    /**
     * Removes an environment property from the environment of this
     * context.
     *
     * @param propName the name of the environment property to remove; may not be null
     * @return the previous value of the property, or null if the property was
     *  not in the environment
     * @throws NamingException if a naming exception is encountered
     */
    public Object removeFromEnvironment(String propName) throws NamingException {
        SysLog.trace(propName);
        return environment.remove(propName);
    }

    /**
     * Retrieves the environment in effect for this context.
     *
     * @return the environment of this context; never null
     * @throws NamingException if a naming exception is encountered
     */
    public Hashtable getEnvironment() throws NamingException {
        return environment;
    }

    /**
     * Closes this context.
     *
     * @throws NamingException if a naming exception is encountered
     */
    public void close() throws NamingException {
        environment.clear();
    }

    /**
     * Retrieves the full name of this context within its own namespace.
     *
     * @return this context's name in its own namespace; never null
     * @throws OperationNotSupportedException if the naming system does
     *  not have the notion of a full name
     * @throws NamingException if a naming exception is encountered
     */
    public String getNameInNamespace() throws NamingException {
        return this.nameInNamespace;
    }

    // ------------------------------------------------------------------
    // protected Methods
    // ------------------------------------------------------------------

    /**
     * Find if this name is a sub context
     */
    private Context firstContext(Name name) throws NamingException {
    	String key = name.get(0);
		Object obj = bindings.get(key);
        if (obj == null) {
        	if(isLenient()) return createSubcontext(key);
			NamingException exception = new NameNotFoundException();
			exception.setRemainingName(name);
			throw exception;
        } else if (obj instanceof HashtableContext) {
            return (Context) obj;
        } else {
			NamingException exception = new NotContextException();
			exception.setRemainingName(name);
			exception.setResolvedObj(obj);
			throw exception;
        }
    }
	
	private boolean isLenient(
	){
		return "true".equals(this.environment.get(ExtendedContext.LENIENT));	
	}
	
    // ------------------------------------------------------------------
    // Inner classes for enumerating lists of bindings
    // ------------------------------------------------------------------

    /**
     * Implementation of the NamingEnumeration for list operations
     * Each element is of type NameClassPair.
     */
    class ListOfNames implements NamingEnumeration {
        protected Enumeration names;
        protected Hashtable bindings;
    
        // Constructor. Called by list()
        // copy bindings locally in this object and build an
        // enumeration of the keys.

        ListOfNames (Hashtable bindings) {
            this.bindings = bindings;
            this.names = bindings.keys();
        }
    
        // Methods implementing NamingEnumeration interface:
        // - hasMore
        // - next
        // - close
        public boolean hasMore() throws NamingException {
            return names.hasMoreElements();
        }
    
        public Object next() throws NamingException {
            String name = (String) names.nextElement();
            String className = bindings.get(name).getClass().getName();
            return new NameClassPair(name, className);
        }
    
        public void close() {
            //
        }

        // Methods inherited from Enumeration:
        // - nextElement
        // - hasMoreElements

        public Object nextElement() {
            try {
                return next();
            } catch (NamingException e) {
                throw new NoSuchElementException(e.toString());
            }
        }
    
        public boolean hasMoreElements() {
            try {
                return hasMore();
            } catch (NamingException e) {
                return false;
            }
        }
    
    }

    /**
     * Implementation of the NamingEnumeration for listBindings operations
     */
    class ListOfBindings extends ListOfNames {

        ListOfBindings (Hashtable bindings) {
            super(bindings);
        }

        // next() is the only different method.
        // It returns a Binding instead of a NameClassPair
        public Object next() throws NamingException {
            String name = (String) names.nextElement();
            return new Binding(name, this.bindings.get(name));
        }
    }


	//------------------------------------------------------------------------
	// Implements NameParser
	//------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see javax.naming.NameParser#parse(java.lang.String)
	 */
	public Name parse(String name) throws NamingException {
		return this.initialContext.parse(name);
	}

}
