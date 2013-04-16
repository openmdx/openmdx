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
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Filter;
import org.openmdx.portal.servlet.control.GridControl;

/**
 * CompositeGrid
 *
 */
public class CompositeGrid extends Grid implements Serializable {

    /**
     * Constructor 
     *
     * @param gridControl
     * @param view
     * @param lookupType
     */
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

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.Grid#refresh(boolean)
     */
    @Override
    public void refresh(
        boolean refreshData
    ) {
        super.refresh(
            refreshData
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.Grid#getFilteredObjects(javax.jdo.PersistenceManager, org.openmdx.portal.servlet.Filter)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<RefObject_1_0> getFilteredObjects(
    	PersistenceManager pm,
        Filter filter
    ) {
        List<RefObject_1_0> filteredObjects = null;
        try {
	    	RefObject_1_0 parent = (RefObject_1_0)pm.getObjectById(
	    		this.view.getObjectReference().getObject().refGetPath()
	    	);
	        Object allObjects = this.dataBinding.getValue(
	            parent, 
	            this.getGridControl().getObjectContainer().getReferenceName(),
	            this.view.getApplicationContext()
	        );
	    	if(allObjects instanceof RefContainer) {
    			filteredObjects = ((RefContainer)allObjects).refGetAll(filter);
	    	} else if(
	    		allObjects instanceof Object[] && 
	    		((Object[])allObjects).length == 2 && 
	    		((Object[])allObjects)[0] instanceof RefContainer    		
	    	) {
				RefContainer container = (RefContainer)((Object[])allObjects)[0];
				org.openmdx.base.query.Filter query = ((RefQuery_1_0)((Object[])allObjects)[1]).refGetFilter();
				if(filter != null) {
					// Order specifiers not allowed in data binding
					query.getOrderSpecifier().clear();
					query.getCondition().addAll(filter.getCondition());
					query.getOrderSpecifier().addAll(filter.getOrderSpecifier());
					query.getExtension().addAll(filter.getExtension());
				}
				filteredObjects = container.refGetAll(query);
	    	} else if(allObjects instanceof List) {
	    		filteredObjects = (List<RefObject_1_0>)allObjects;
	    	} else if(allObjects instanceof Collection) {
	    		filteredObjects = new ArrayList<RefObject_1_0>((Collection)allObjects);
	    	} else {
	    		filteredObjects = Collections.emptyList();
	    	}
        } catch(Exception e) {
            filteredObjects = Collections.emptyList();
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
    	return filteredObjects;
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258408426441815605L;

}

//--- End of File -----------------------------------------------------------
