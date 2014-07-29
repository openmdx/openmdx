/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: ReferenceGrid
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Filter;
import org.openmdx.portal.servlet.control.UiGridControl;

/**
 * ReferenceGrid
 *
 */
public class UiReferenceGrid extends UiGrid implements Serializable {

    /**
     * Constructor 
     *
     * @param gridControl
     * @param view
     * @param lookupType
     */
    public UiReferenceGrid(
        UiGridControl gridControl,
        ObjectView view,
        String lookupType
    ) {
        super(
            gridControl,
            view,
            lookupType
        );
    }

    /**
     * Get all referenced objects.
     * 
     * @param pm
     * @return
     */
    public Collection<RefObject_1_0> getAllObjects(
    	PersistenceManager pm
    ) {
    	UiGridControl control = (UiGridControl)this.control;
    	ObjectView view = this.getView();
    	RefObject_1_0 parent = (RefObject_1_0)pm.getObjectById(
    			view.getObjectReference().getObject().refGetPath()
    	);
        @SuppressWarnings("unchecked")
		Collection<RefObject_1_0> allObjects = (Collection<RefObject_1_0>)parent.refGetValue(
        	control.getObjectContainer().getReferenceName()
        );
        if(allObjects == null) {
            allObjects = Collections.emptyList();
        }
        return allObjects;    
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.Grid#getFilteredObjects(javax.jdo.PersistenceManager, boolean, org.openmdx.portal.servlet.Filter)
     */
    @Override
    public List<RefObject_1_0> getFilteredObjects(
    	PersistenceManager pm,
    	boolean preCalcListSize,
        Filter filter
    ) {
    	UiGridControl control = (UiGridControl)this.control;
    	ObjectView view = this.getView();
        Collection<RefObject_1_0> allObjects = this.getAllObjects(pm);
        List<RefObject_1_0> filteredObjects = new ArrayList<RefObject_1_0>();
        if(allObjects != null) {
            filteredObjects = new ArrayList<RefObject_1_0>();
            Iterator<RefObject_1_0> i = allObjects.iterator();
            while(i.hasNext()) {
                try {
                    RefObject_1_0 object = i.next();
                    filteredObjects.add(object);
                } catch(Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning(
                        "Unable to retrieve object (more info at detail level)", 
                        Arrays.asList(
                            view.getObjectReference().getXRI(), 
                            control.getObjectContainer().getReferenceName(), 
                            view.getApplicationContext().getLoginPrincipal(), 
                            e0.getMessage()
                        )
                    );
                    SysLog.detail(e0.getMessage(), e0.getCause());
                }
            }
        }
        return filteredObjects;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.Grid#getPageSize()
     */
    @Override
    public int getPageSize(
    ) {
        return Integer.MAX_VALUE;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Component#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		return Collections.emptyList();
	}

	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3978143227574497585L;

}

//--- End of File -----------------------------------------------------------
