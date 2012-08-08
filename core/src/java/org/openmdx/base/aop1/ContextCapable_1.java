/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ContextCapable_1.java,v 1.3 2009/01/17 02:37:22 hburger Exp $
 * Description: Object_1
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/17 02:37:22 $
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
package org.openmdx.base.aop1;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.view.EmbeddedContainer_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.accessor.view.PlugIn_1;
import org.openmdx.base.exception.ServiceException;

/**
 * Registers the the delegates with their manager
 */
public class ContextCapable_1 extends PlugIn_1 {

    /**
     * Constructor 
     *
     * @param self the plug-in holder
     * @param next the next plug-in
     * 
     * @throws ServiceException
     */
    public ContextCapable_1(
        ObjectView_1_0 self,
        PlugIn_1 next
    ) throws ServiceException{
        super(self, next);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -2271195156332300696L;

    /**
     * Match a context's class feature name
     */
    private static final String OBJECT_CLASS_SUFFIX = ':' + SystemAttributes.OBJECT_CLASS;

    /**
     * The context map unless contextCapable is <code>FALSE</code>.
     */
    private transient Container_1_0 context = null;
    
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
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException {
        if(SystemAttributes.CONTEXT_CAPABLE_CONTEXT.equals(feature)) {
            if(this.context == null) {
                Map<String, DataObject_1_0> context = new HashMap<String, DataObject_1_0>();
                for(String name : super.objDefaultFetchGroup()) {
                    if(
                        name.startsWith(SystemAttributes.CONTEXT_PREFIX) &&
                        name.endsWith(OBJECT_CLASS_SUFFIX)
                    ){
                        int lastColon =  name.lastIndexOf(':');
                        String qualifier = name.substring(SystemAttributes.CONTEXT_PREFIX.length(), lastColon);
                        String prefix = name.substring(0, lastColon + 1);
                        String objectClass = (String) super.objGetValue(
                            prefix + SystemAttributes.OBJECT_CLASS
                        );
                        context.put(
                            qualifier,
                            new Context_1(
                                getDelegate(),
                                objectClass, 
                                prefix, 
                                qualifier
                            )
                        );
                    }
                }
                this.context = new EmbeddedContainer_1(context);
            }
            return this.context;
        } else {
            return super.objGetContainer(feature);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objDefaultFetchGroup()
     */
    public Set<String> objDefaultFetchGroup() throws ServiceException {
        Set<String> features = super.objDefaultFetchGroup();
        features.add(SystemAttributes.CONTEXT_CAPABLE_CONTEXT);
        return features;
    }

}