/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Name:        $Id: ReferenceGrid.java,v 1.7 2008/04/04 11:53:28 hburger Exp $
 * Description: ReferenceGridControl
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/04 11:53:28 $
 * ====================================================================
 *
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
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.portal.servlet.Filter;
import org.openmdx.portal.servlet.control.GridControl;

public class ReferenceGrid 
    extends Grid
    implements Serializable {

    //-------------------------------------------------------------------------
    public ReferenceGrid(
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
    protected Collection getAllObjects(
    ) {
        Collection allObjects = (Collection)this.view.getObjectReference().getObject().refGetValue(
            this.getGridControl().getObjectContainer().getReferenceName()
        );
        if(allObjects == null) {
            allObjects = new ArrayList();
        }            
        return allObjects;    
    }
    
    //-------------------------------------------------------------------------
    protected List getFilteredObjects(
        Filter filter
    ) {
        Collection<?> allObjects = this.getAllObjects();
        List<?> filteredObjects = null;
        if(allObjects == null) {
            filteredObjects = new ArrayList<Object>();
        }
        else {
            filteredObjects = new ArrayList<Object>(allObjects);
        }
        for(Iterator i = filteredObjects.iterator(); i.hasNext(); ) {
            Object obj = i.next();
            if(obj instanceof RefObject_1_0) {
                try {
                    ((RefObject_1_0)obj).refRefresh();
                } catch(Exception e) {}
            }
        }
        return filteredObjects;
    }
    
    
    //-------------------------------------------------------------------------
    /**
     * Always show all rows in case of non-composite grids
     */
//  @Override
    public int getPageSize(
    ) {
        return Integer.MAX_VALUE;
    }

    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3978143227574497585L;

}

//--- End of File -----------------------------------------------------------
