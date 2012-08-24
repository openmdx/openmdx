/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: CompositeGrid
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Filter;
import org.openmdx.portal.servlet.control.GridControl;

public class CompositeGrid 
extends Grid
implements Serializable {

    //-------------------------------------------------------------------------
    public CompositeGrid(
        GridControl gridControl,
        ObjectView view,
        String lookupType
    ) {
        super(
            gridControl,
            view,
            lookupType
        );
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public Collection<RefObject_1_0> getAllObjects(
    	PersistenceManager pm
    ) {
    	Collection<RefObject_1_0> allObjects = null;
    	RefObject_1_0 parent = (RefObject_1_0)pm.getObjectById(
    		this.view.getObjectReference().getObject().refGetPath()
    	);
        allObjects = (Collection<RefObject_1_0>)this.dataBinding.getValue(
            parent, 
            this.getGridControl().getObjectContainer().getReferenceName(),
            this.view.getApplicationContext()
        );    		
    	if(allObjects == null) {
    		allObjects = Collections.emptyList();
    	}
    	return allObjects;
    }

    //-------------------------------------------------------------------------
    public void refresh(
        boolean refreshData
    ) {
        super.refresh(
            refreshData
        );
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public List<RefObject_1_0> getFilteredObjects(
    	PersistenceManager pm,
        Filter filter
    ) {
        Collection allObjects = this.getAllObjects(pm);
        List<RefObject_1_0> filteredObjects = null;
        if(filter == null) {
            filteredObjects = allObjects instanceof RefContainer ?
            	((RefContainer)allObjects).refGetAll(null) :
            	allObjects instanceof List ?
            		(List)allObjects :
            		Collections.EMPTY_LIST;
        }
        else {
            try {
                try {
                    filteredObjects = allObjects instanceof RefContainer ?
                    	((RefContainer)allObjects).refGetAll(filter) :
                    	allObjects instanceof List ?
                    		(List)allObjects :
                    		Collections.EMPTY_LIST;
                }
                catch(UnsupportedOperationException e) {}
                if(filteredObjects == null) {
                    filteredObjects = new ArrayList(
                    	((RefContainer)allObjects).refGetAll(filter)
                    );
                }
            }
            catch(Exception e) {
                filteredObjects = Collections.EMPTY_LIST;
                ServiceException e0 = new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.PROCESSING_FAILURE,
                    "error getting filtered objects",
                    new BasicException.Parameter("object", this.view.getObject()),
                    new BasicException.Parameter("reference", this.getGridControl().getQualifiedReferenceName()),
                    new BasicException.Parameter("filter", filter),
                    new BasicException.Parameter("principal", this.view.getApplicationContext().getLoginPrincipal())
                );
                SysLog.warning(e0.getMessage(), e0.getCause());
            }
        }
        return filteredObjects;
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258408426441815605L;

}

//--- End of File -----------------------------------------------------------
