/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderObjectComparator.java,v 1.1 2009/01/05 13:44:50 wfro Exp $
 * Description: Dataprovider Object Filter
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:44:50 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 */
package org.openmdx.application.dataprovider.cci;

import java.util.Collections;
import java.util.Iterator;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.spi.AbstractComparator;
import org.openmdx.base.collection.SparseList;



/**
 * Dataprovider Object Filter
 * @author hburger
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
@SuppressWarnings("unchecked")
public class DataproviderObjectComparator extends AbstractComparator {

    /**
     * Constructor
     * 
     * @param filter
     */
    public DataproviderObjectComparator(
        AttributeSpecifier[] order
    ) {
        super(order);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.AbstractComparator#getValues(java.lang.Object, java.lang.String)
     */
    protected Iterator getValues(
        Object candidate, 
        String attribute
    ){
        DataproviderObject object = (DataproviderObject)candidate;
        SparseList values = object.getValues(attribute);
        return values != null ?
            values.iterator() :
            SystemAttributes.OBJECT_IDENTITY.equals(attribute) ?
            Collections.singleton(object.path().toUri()).iterator() :
            Collections.EMPTY_LIST.iterator();
    }
      
  }
