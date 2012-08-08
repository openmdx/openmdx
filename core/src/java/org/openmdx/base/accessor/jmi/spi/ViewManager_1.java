/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ViewManager_1.java,v 1.11 2008/02/29 14:43:44 hburger Exp $
 * Description: View Manager
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 14:43:44 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_3;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_4;
import org.openmdx.base.accessor.generic.spi.SinkConnection_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_2;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * View Manager
 */
class ViewManager_1 
    extends SinkConnection_1 
    implements Serializable
{

    /**
     * Constructor 
     */
    protected ViewManager_1(
    ){
        // Implements Serializable
    }
    
    /**
     * Constructor 
     *
     * @param delegate
     * @param model 
     * 
     * @throws ServiceException 
     */
    ViewManager_1(
        ObjectFactory_1_4 delegate, 
        Model_1_0 model        
    ){
        super(delegate, model);
        delegate.setModel(model);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -2094623324002928499L;

    /**
     * To register the views
     */
    private final Map<InteractionSpec,RefPackage_1_2> views = 
        new HashMap<InteractionSpec,RefPackage_1_2>();
    
    /**
     * Retrieve a registered view
     * 
     * @param viewContext
     * 
     * @return the view belonging to the given context
     */
    RefPackage_1_2 getView(
        Object viewContext
    ){
        return this.views.get(viewContext);
    }

    /**
     * Add a view for a given context
     * 
     * @param viewContext the view's context
     * @param view a <code>RefPackage</code> representing the view 
     */
    void addView(
        InteractionSpec viewContext,
        RefPackage_1_2 view
    ){
      this.views.put(viewContext, view);  
    }
 
    /**
     * The afterCompletion method notifies a a provider or plug-in that a
     * unit of work commit protocol has completed, and tells the instance
     * whether the unit of work has been committed or rolled back. 
     */
    public void afterCompletion(
        boolean committed
    ){
        super.afterCompletion(committed);
        for(RefPackage_1_2 view :this.views.values()) {
            ObjectFactory_1_0 cache = view.refObjectFactory();
            if(cache instanceof ObjectFactory_1_3) {
                ((ObjectFactory_1_3)cache).evict();
            }
        }
    }

}
