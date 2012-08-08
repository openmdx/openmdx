/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StatedObjectContainer_1.java,v 1.2 2009/01/17 02:37:21 hburger Exp $
 * Description: StatedObjectContainer_1 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/17 02:37:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

package org.openmdx.compatibility.state1.aop1;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * StatedObjectContainer_1
 *
 */
public class StatedObjectContainer_1
    extends org.openmdx.state2.aop1.StatedObjectContainer_1
{

    /**
     * Constructor 
     *
     * @param parent
     * @param container
     * @throws ServiceException
     */
    public StatedObjectContainer_1(
        ObjectView_1_0 parent,
        Container_1_0 container
    ) throws ServiceException {
        super(
            parent, 
            container,
            "org:openmdx:compatibility:state1:DateState"
        );
    }
    
    /**
     * Constructor 
     *
     * @param parent
     * @param container
     * @param criteria
     */
    protected StatedObjectContainer_1(
        ObjectView_1_0 parent,
        Container_1_0 container,
        Object criteria) {
        super(parent, container, criteria);
    }

    /**
     * Constructor 
     *
     * @param parent
     * @param container
     * @param instanceOf
     * @throws ServiceException
     */
    public StatedObjectContainer_1(
        ObjectView_1_0 parent,
        Container_1_0 container,
        String instanceOf
    ) throws ServiceException {
        super(parent, container, instanceOf);
    }


    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -1740755703888250396L;

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop1.StatedObjectContainer_1#subMap(java.lang.Object)
     */
    @Override
    public FilterableMap<String, DataObject_1_0> subMap(Object filter) {
        return filter == null ? this : new StatedObjectContainer_1(
            this.parent,
            this.selection,
            filter
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop1.StatedObjectContainer_1#get(java.lang.Object)
     */
    @Override
    public DataObject_1_0 get(Object key) {
        DataObject_1_0 dataObject = super.get(key);
        if(dataObject != null) {
            try {
                dataObject.objGetClass();
                ObjectView_1_0 objectView = (ObjectView_1_0) parent.getMarshaller().marshal(dataObject);
                if(objectView.jdoIsDeleted()) {
                    return null;
                }
            } catch (ServiceException exception) {
                if(BasicException.Code.NOT_FOUND == exception.getExceptionCode()) {
                    return null;
                }
                throw new RuntimeServiceException(exception);
            }
        }
        return dataObject;
    }

}
