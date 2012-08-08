/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Name:        $Id: CompositeGrid.java,v 1.16 2008/04/25 23:39:22 wfro Exp $
 * Description: CompositeGrid
 * Revision:    $Revision: 1.16 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2008/04/25 23:39:22 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
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
    private Collection getAllObjects(
    ) {
        Collection allObjects = (Collection)this.dataBinding.getValue(
            this.view.getObjectReference().getObject(), 
            this.getGridControl().getObjectContainer().getReferenceName()
        );
        return allObjects == null
            ? Collections.EMPTY_LIST
            : allObjects;
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
    protected List<?> getFilteredObjects(
        Filter filter
    ) {
        Collection allObjects = this.getAllObjects();
        List filteredObjects = null;
        if(filter == null) {
             filteredObjects = ((RefContainer)allObjects).refGetAll(null);
        }
        else {
            try {
                try {
                    filteredObjects = ((RefContainer)allObjects).refGetAll(filter);
                }
                catch(UnsupportedOperationException e) {}
                if(filteredObjects == null) {
                    filteredObjects = new ArrayList<Object>(
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
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("object", this.view.getObject()),
                        new BasicException.Parameter("reference", this.getGridControl().getQualifiedReferenceName()),
                        new BasicException.Parameter("filter", filter),
                        new BasicException.Parameter("principal", this.view.getApplicationContext().getLoginPrincipalId()),                        
                    },
                    "error getting filtered objects"
                );
                AppLog.warning(e0.getMessage(), e0.getCause());
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
