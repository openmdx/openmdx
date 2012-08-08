/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Object_1.java,v 1.26 2007/10/23 10:27:23 hburger Exp $
 * Description: Embedded Object Provider
 * Revision:    $Revision: 1.26 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/23 10:27:23 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.accessor.generic.view;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_2;
import org.openmdx.base.accessor.generic.spi.AbstractObject_1;
import org.openmdx.base.accessor.generic.spi.MarshallingObject_1;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.MarshalException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;

/**
 * Maps embedded objects to members of their corresponding container:<ul>
 * <li>context
 * <li>role
 * <li>view
 * </ul>
 */
class Object_1 
    extends MarshallingObject_1 
    implements Object_1_2
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3689345537765552435L;

    /**
     * 
     */
    private transient ServiceException inaccessibilityReason;

    /**
     * Constructor
     * @throws ServiceException 
     */
    Object_1(
        Object_1_0 object,
        Manager_1 marshaller
    ) throws ServiceException{
        super(object, marshaller);
        this.objAddEventListener(null, marshaller);
    }
    
    /**
     * Assert that the object is accessible.
     * 
     * @throws ServiceException if it is unaccessible
     */
    protected void assertAcessibility() throws ServiceException{
        if(objIsInaccessable()) {
            throw getInaccessabilityReason();
        }
    }
    
    /**
     * Set inaccessabilityReason.
     * 
     * @param inaccessabilityReason The inaccessabilityReason to set.
     */
    void setInaccessabilityReason(
        ServiceException inaccessabilityReason
    ) {        
        this.inaccessibilityReason = inaccessabilityReason;
        this.context = null;
        this.role = null;
        this.view = null;
        super.marshaller = null;
    }    

    /* (non-Javadoc)
     */
    Manager_1 getFactory(
    ){
        return (Manager_1)super.marshaller;
    }


    //--------------------------------------------------------------------------
    // Extends DelegatingObject_1
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.DelegatingObject_1#getDelegate()
     */
    protected Object_1_0 getDelegate() {
        return this.objIsInaccessable() ? null : super.getDelegate();
    }
    

    //--------------------------------------------------------------------------
    // Extends Object
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    public String toString(
    ){        
        try {
            return AbstractObject_1.toString(this, this.objGetClass(), null);
        } catch (ServiceException e) {
            return AbstractObject_1.toString(this, "n/a", null);
        }
    }


    //--------------------------------------------------------------------------
    // Extends MarshallingObject_1
    //--------------------------------------------------------------------------

    /**
     * Get a reference feature.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature is not a reference
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    public FilterableMap objGetContainer(
        String _feature
    ) throws ServiceException {
        String feature = _feature;
        assertAcessibility();
        boolean contextContainer = SystemAttributes.CONTEXT_CAPABLE_CONTEXT.equals(feature);
        boolean roleContainer = SystemAttributes.ROLE_CAPABLE_ROLE.equals(feature);
        boolean viewContainer = SystemAttributes.VIEW_CAPABLE_VIEW.equals(feature);
        boolean useObjectIdentity = feature.endsWith(SystemAttributes.USE_OBJECT_IDENTITY_HINT);
        if(useObjectIdentity) {
            feature = feature.substring(
                0, 
                feature.length() - SystemAttributes.USE_OBJECT_IDENTITY_HINT.length()
            );
            if(
                !objIsInaccessable() &&
                this.marshaller instanceof ObjectFactory_1_0
            ) {
                Object path = objGetValue(SystemAttributes.OBJECT_IDENTITY);
                if(path != null) try {
                    Path identity = new Path(path.toString());
                    if(
                        !objGetPath().equals(identity)
                    ) return ((ObjectFactory_1_0)this.marshaller).getObject(identity).objGetContainer(feature);
                } catch (Exception exception) {
                    // fall back to the object's path
                }
            }
        } else if(viewContainer|contextContainer|roleContainer)for(
            Iterator i = super.objDefaultFetchGroup().iterator();
            i.hasNext();
        ){
            String attribute = (String)i.next();
            if(contextContainer && attribute.startsWith(SystemAttributes.CONTEXT_PREFIX)) return getContext();
            if(roleContainer && attribute.startsWith(SystemAttributes.ROLE_PREFIX)) return getRole();
            if(viewContainer && attribute.startsWith(SystemAttributes.VIEW_PREFIX)) return getView();
        }
        return super.objGetContainer(feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objDefaultFetchGroup()
     */
    public Set objDefaultFetchGroup() throws ServiceException {
        assertAcessibility();
        Set features = super.objDefaultFetchGroup();
        boolean includeView = false;
        boolean includeContext = false;
        boolean includeRole = false;
        for(
            Iterator i = features.iterator();
            i.hasNext();
        ){
            String feature = (String)i.next();
            if(feature.startsWith(SystemAttributes.CONTEXT_PREFIX)){
                includeContext = true;
                i.remove();
            } else if(feature.startsWith(SystemAttributes.ROLE_PREFIX)){
                includeRole = true;
                i.remove();
            } else if(feature.startsWith(SystemAttributes.VIEW_PREFIX)){
                includeView = true;
                i.remove();
            }
        }
        if(includeContext) features.add(SystemAttributes.CONTEXT_CAPABLE_CONTEXT);
        if(includeRole) features.add(SystemAttributes.ROLE_CAPABLE_ROLE);
        if(includeView) features.add(SystemAttributes.VIEW_CAPABLE_VIEW);
        return features;
    }

    //--------------------------------------------------------------------------
    // Implements Object_1_2
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_2#getInaccessabilityReason()
     */
    public ServiceException getInaccessabilityReason() {
        return this.inaccessibilityReason;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_2#objIsInaccessable()
     */
    public boolean objIsInaccessable() {
        return this.inaccessibilityReason != null;
    }


    //--------------------------------------------------------------------------
    // Implements Serializable
    //--------------------------------------------------------------------------

    /**
     * Save the data of the <tt>Object_1_0</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The objects data
     */
    private synchronized void writeObject(
        java.io.ObjectOutputStream stream
    ) throws java.io.IOException {
        // stream.defaultWriteObject(); has nothing to do
    }

    /**
     * Reconstitute the <tt>Object_1_0</tt> instance from a stream (that is,
     * deserialize it).
     */
    private synchronized void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        // stream.defaultReadObject(); has nothing to do
        getFactory().cache(getDelegate(), this);
    }


    //--------------------------------------------------------------------------
    // Class Container
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    private abstract class Container 
        extends AbstractMap 
        implements FilterableMap, Serializable {

        /**
         * Constructor
         * 
         * @param   prefix
         * @param lazy tells, whether the embedded object may be fetched lazily
         */
        Container(
            String prefix, 
            boolean lazy
        ) {
            this.containerPrefix = prefix;
            this.lazy = lazy;
            if(!lazy) {
                this.getDelegate();
            }
        }

        
        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        public Set entrySet() {
            return getDelegate().entrySet();
        }

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        public int size() {
            return getDelegate().size();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
         */
        public FilterableMap subMap(Object filter) {
            throw new UnsupportedOperationException("Filtering of embedded objects not yet implemented");
        }

       /*
        * Select an object matching the filter.
        * <p>
        * The acceptable filter object classes must be specified by the 
        * Container implementation.
        *
        * @param     filter
        *            The filter to be applied to objects of this container
        *
        * @return    the object matching the filter;
        *            or <code>null</code> if no object matches the filter.
        * 
        * @exception ClassCastException
        *            if the class of the specified filter prevents it from
        *            being applied to this container.
        * @exception IllegalArgumentException
        *            if some aspect of this filter prevents it from being
        *            applied to this container. 
        * @exception InvalidCardinalityException
        *            if more than one object matches the filter. 
        * @exception MarshalException
        *            if object retrieval fails
        */
        public synchronized Object get(
            Object filter
        ) {
            return get((String)filter, this.lazy);
        }

        /**
         * Retrieves or creates an embedded object.
         * 
         * @param objectQualifier
         * @param mayCreateEmbeddedObject
         * 
         * @return an mbedded object; or <code>null</code>
         */
        private synchronized Object get(
            String objectQualifier,
            boolean mayCreateEmbeddedObject
        ) {
            Object embeddedObject = this.map.get(objectQualifier);
            if(embeddedObject == null && mayCreateEmbeddedObject) try {
                String objectPrefix = containerPrefix + objectQualifier + ':';
                String objectClass = (String) Object_1.this.getDelegate().objGetValue(
                    objectPrefix + SystemAttributes.OBJECT_CLASS
                );
                if(objectClass == null) return null;
                this.map.put(
                    objectQualifier,
                    embeddedObject = getEmbeddedObject(objectClass, objectPrefix, objectQualifier)
                );
            } catch (ServiceException exception) {
                throw new MarshalException(exception);
            }
            return embeddedObject;
        }
        
        /* (non-Javadoc)
         */
        protected abstract Object_1_0 getEmbeddedObject(
            String objectClass, 
            String prefix, 
            String qualifier
        ) throws ServiceException;
        
        /* (non-Javadoc)
         * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
         */
        public List values(Object criteria) {
            if(criteria != null) throw new UnsupportedOperationException("Re-Ordering not supported");
            return new ArrayList(getDelegate().values());
        }

        /* (non-Javadoc)
         */
        protected SortedMap getDelegate(
        ) throws MarshalException {
            try {
                for(
                    Iterator i=Object_1.this.getDelegate().objDefaultFetchGroup().iterator();
                    i.hasNext();
                ){
                    String feature = (String) i.next();
                    if(feature.startsWith(containerPrefix)) get(
                        feature.substring(containerPrefix.length(), feature.lastIndexOf(':')),
                        true // Creates embedded object if necessary
                    ); 
                }
                return this.map;
            } catch (ServiceException exception) {
                throw new MarshalException(exception);
            }
        }

        private final SortedMap map = new TreeMap();

        protected final String containerPrefix;

        protected final boolean lazy;
        
    }


    //--------------------------------------------------------------------------
    // Class Context
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    private FilterableMap context = null;

    /* (non-Javadoc)
     */
    private synchronized FilterableMap getContext(
    ){
        if(this.context==null)this.context = new Context();
        return this.context;
    }

    /* (non-Javadoc)
     */
    class Context extends Container{
        
        /**
         * 
         */
        private static final long serialVersionUID = 3760567468472873785L;

        
        /**
         * Constructor
         */
        Context() {
            super(SystemAttributes.CONTEXT_PREFIX, false);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.view.Object_1.Container#createView(java.lang.String)
         */
        protected Object_1_0 getEmbeddedObject(String objectClass, String prefix, String qualifier) throws ServiceException {
            return new ContextObject_1(
                Object_1.this,
                objectClass,
                prefix, 
                qualifier
            );
        }

    }

    
    //--------------------------------------------------------------------------
    // Class Role
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    private FilterableMap role = null;

    /* (non-Javadoc)
     */
    private synchronized FilterableMap getRole(
    ){
        if(this.role==null)this.role = new Role();
        return this.role;
    }

    /* (non-Javadoc)
     */
    class Role extends Container {

        /**
         * 
         */
        private static final long serialVersionUID = 3833182532186485552L;

        /**
         * Constructor
         */
        Role() {
            super(SystemAttributes.ROLE_PREFIX, true);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.view.Object_1.Container#createView(java.lang.String)
         */
        protected Object_1_0 getEmbeddedObject(String objectClass, String prefix, String qualifier) throws ServiceException {
            return new RoleObject_1(
                Object_1.this,
                objectClass,
                prefix, 
                qualifier
            );
        }
        
    }
    
    /**
     * Add a role to a core object
     * 
     * @param roleId
     * @param objectClass
     * @return
     * @throws ServiceException
     */
    public Object_1_0 addRole(
        String roleId,
        String objectClass
    ) throws ServiceException {
        assertAcessibility();
        getDelegate().objSetValue(
            SystemAttributes.ROLE_PREFIX + roleId + ':' + SystemAttributes.OBJECT_CLASS,
            objectClass
        );
        return (Object_1_0) getRole().get(roleId);           
    }
    

    //--------------------------------------------------------------------------
    // Class View
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    private FilterableMap view = null;

    /* (non-Javadoc)
     */
    private synchronized FilterableMap getView(
    ){
        if(this.view==null)this.view = new View();
        return this.view;
    }
    
    /* (non-Javadoc)
     */
    private class View extends Container {

       /**
         * 
         */
        private static final long serialVersionUID = 3256438127291020598L;

    /**
         * Constructor
         */
        View() {
            super(SystemAttributes.VIEW_PREFIX, false);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.view.Object_1.Container#createView(java.lang.String)
         */
        protected Object_1_0 getEmbeddedObject(String objectClass, String prefix, String qualifier) throws ServiceException {
            return new ViewObject_1(
                Object_1.this,
                objectClass,
                prefix, 
                qualifier
            );
        }

    }

}