/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ContextCapable_1.java,v 1.3 2008/09/18 12:46:37 hburger Exp $
 * Description: Object_1
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/18 12:46:37 $
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;

/**
 * Registers the the delegates with their manager
 */
abstract class ContextCapable_1 extends AbstractObject_1 {

    /**
     * Constructor 
     *
     * @param object
     * @param marshaller
     * @throws ServiceException
     */
    protected ContextCapable_1(
        Object_1_0 object,
        Manager_1 marshaller
    ) throws ServiceException{
        super(object, marshaller);
    }

    /**
     * Remembers whether the object is an instance of CntextCapable
     */
    private transient Boolean contextCapable = null;

    /**
     * The context map unless contextCapable is <code>FALSE</code>.
     */
    private transient FilterableMap<String, Object_1_0> context = null;

    /**
     * org::openmdx::base::ContextCapable's MOF id
     */
    private final static String CONTEXT_CAPABLE = "org:openmdx:base:ContextCapable";

    /**
     * Match a context's class feature name
     */
    private static final String OBJECT_CLASS_SUFFIX = ':' + SystemAttributes.OBJECT_CLASS;

    
    //--------------------------------------------------------------------------
    // Supports ContextCapable
    //--------------------------------------------------------------------------

    /**
     * Tells whether the object is an instance of CntextCapable
     * 
     * @return <code>true</code> if the object is an instance of CntextCapable
     * 
     * @throws ServiceException 
     */
    private boolean isContextCapable(
    ) throws ServiceException{
        if(this.contextCapable == null) {
            Object_1_0 object = getDelegate();
            boolean contextCapable = getMarshaller().getModel().isInstanceof(
                object, 
                CONTEXT_CAPABLE
            );
            if(contextCapable) {
                Map<String, Object_1_0> context = new HashMap<String, Object_1_0>();
                for(String name : object.objDefaultFetchGroup()) {
                    if(
                        name.startsWith(SystemAttributes.CONTEXT_PREFIX) &&
                        name.endsWith(OBJECT_CLASS_SUFFIX)
                    ){
                        int lastColon =  name.lastIndexOf(':');
                        String qualifier = name.substring(SystemAttributes.CONTEXT_PREFIX.length(), lastColon);
                        String prefix = name.substring(0, lastColon + 1);
                        String objectClass = (String) object.objGetValue(
                            prefix + SystemAttributes.OBJECT_CLASS
                        );
                        context.put(
                            qualifier,
                            new Context_1(
                                this,
                                objectClass, 
                                prefix, 
                                qualifier
                            )
                        );
                    }
                }
                this.context = new EmbeddedContainer_1(context);
            }
            this.contextCapable = Boolean.valueOf(contextCapable);
            return contextCapable;
        } else {
            return this.contextCapable.booleanValue();
        }
    }
    
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
    public FilterableMap<String, Object_1_0> objGetContainer(
        String feature
    ) throws ServiceException {
        return SystemAttributes.CONTEXT_CAPABLE_CONTEXT.equals(feature) && isContextCapable() ?
            this.context :
            super.objGetContainer(feature);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objDefaultFetchGroup()
     */
    public Set<String> objDefaultFetchGroup() throws ServiceException {
        Set<String> features = super.objDefaultFetchGroup();
        if(isContextCapable()) {
            features.add(SystemAttributes.CONTEXT_CAPABLE_CONTEXT);
        }
        return features;
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
        try {
            getMarshaller().cache(getDelegate(), this);
        } catch (ServiceException exception) {
            throw new ExtendedIOException(exception);
        }
    }

}