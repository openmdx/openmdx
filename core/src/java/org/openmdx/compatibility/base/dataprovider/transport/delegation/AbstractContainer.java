/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractContainer.java,v 1.7 2008/12/15 03:15:29 hburger Exp $
 * Description: Abstract Container
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:29 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.delegation;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;

import org.openmdx.base.accessor.generic.spi.ObjectFilter_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.query.AbstractFilter;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Selector;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * Abstract Container
 */
@SuppressWarnings("deprecation")
abstract class AbstractContainer<E>
    extends AbstractCollection<E>
    implements org.openmdx.compatibility.base.collection.Container<E>, Serializable, RefBaseObject
{

    /**
     * Constructor
     *  
     * @param attributeFilter
     *
     */
    protected AbstractContainer(
        Selector attributeFilter
    ) {
        this.attributeFilter = attributeFilter;
    }

    /**
     * The container's attribute filter
     */
    private Selector attributeFilter;
    
    /**
     * The current sequence state has to be requested from the subclass.
     */
    private static final long SEQUENCE_INITIALIZATION_PENDING = -2L;

    /**
     * The subclass has requested to use UUIDs instead of sequence values.
     */
    protected static final long SEQUENCE_NOT_SUPPORTED = -1L;

    /**
     * If sequences are supported by either the application or persistence 
     * layer.
     */
    protected static final long SEQUENCE_MIN_VALUE = 0L;

    /**
     * The next qualifier.
     */
    private long nextQualifier = SEQUENCE_INITIALIZATION_PENDING;

    /**
     * 
     */
    private static final String NO_JMI = "JMI objects have to be provided by a higher layer";
    
    /**
     * Retrieve the attribute filter
     * 
     * @return the attribute filter
     */
    protected final Selector getSelector(){
        return this.attributeFilter;
    }
    
    /**
     * Initial qualifier value callback method.
     * 
     * @return the initial qualifier value; or -1L for UUIDs
     * 
     * @exception ServiceException
     */
    protected abstract long initialQualifier(
    ) throws ServiceException;

    /**
     * Get the next qualifier
     * 
     * @return the next qualifier,
     * or null if a UID shoid be used
     */
    synchronized String nextQualifier(
    ){
        if(this.nextQualifier == SEQUENCE_INITIALIZATION_PENDING) try {
            this.nextQualifier = initialQualifier();
        } catch (ServiceException exception) {
            // this.nextQualifier remains < 0L
        } finally {
            if(this.nextQualifier < 0L) this.nextQualifier = SEQUENCE_NOT_SUPPORTED;
        }
        return this.nextQualifier == SEQUENCE_NOT_SUPPORTED ?
            null :
            String.valueOf(this.nextQualifier++);
    }

    /**
     * Evict the object
     */
    protected void evict(
    ){
        this.nextQualifier = SEQUENCE_INITIALIZATION_PENDING;
    }
    
    /**
     * Retrieve the model
     * 
     * @return the model, or <code>null</code> if no model provider is available
     */
    protected abstract Model_1_0 getModel();
    
    /**
     * Combine the current attribute filter with the requested one.
     * 
     * @param filter
     * 
     * @return the combined filter; or <code>null</code> if the
     * actual filter includes the requested one.
     */
    protected Selector combineWith(
        Object filter
    ){
        if(filter == null) {
            return null;
        } else {
            if(this.attributeFilter == null) {
                return new ObjectFilter_1(
                    getModel(),
                    (FilterProperty[])filter
                );
            } else {
                Set<FilterProperty> target = new LinkedHashSet<FilterProperty>();
                for(FilterProperty e : ((AbstractFilter)this.attributeFilter).getDelegate()) {
                    target.add(e);
                }
                boolean changed = false;
                for(FilterProperty e : (FilterProperty[])filter) {
                    changed |= target.add(e);
                }
                if(changed) {
                    return new ObjectFilter_1(
                        getModel(),
                        target.toArray(
                            new FilterProperty[target.size()]
                        )
                    );
                } else {
                    return null;
                }
            }
        }
        
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refImmediatePackage()
     */
    public RefPackage refImmediatePackage() {
        throw new UnsupportedOperationException(NO_JMI);
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMetaObject()
     */
    public RefObject refMetaObject() {
        throw new UnsupportedOperationException(NO_JMI);
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refOutermostPackage()
     */
    public RefPackage refOutermostPackage() {
        throw new UnsupportedOperationException(NO_JMI);
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refVerifyConstraints(boolean)
     */
    public Collection<?> refVerifyConstraints(boolean deepVerify) {
        return null; // TODO Nothing to be done yet
    }

}
