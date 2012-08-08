/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ObjectFilter_1.java,v 1.10 2008/11/05 12:53:41 hburger Exp $
 * Description: Object Filter
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/05 12:53:41 $
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
package org.openmdx.base.accessor.generic.spi;

import java.util.Iterator;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.exception.BadParameterException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.ModelAwareFilter;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * Object Filter
 */
public class ObjectFilter_1 extends ModelAwareFilter {

    /**
     * Constructor for a sub-class aware filter
     * 
     * @param model
     * @param filter
     * 
     * @exception   BadParameterException
     *              in case of an invalid filter property set
     * @exception   NullPointerException
     *              if the filter is <code>null</code>
     */
    public ObjectFilter_1(
        Model_1_0 model,
        FilterProperty[] filter
    ){
        super(model, filter);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 875014812028655977L;

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.query.AbstractFilter#getValues(java.lang.Object, java.lang.String)
     */
    protected Iterator<?> getValues(
        Object candidate, 
        String attribute
    ){
        try {
            Object_1_5 object = (Object_1_5)candidate;
            return SystemAttributes.OBJECT_INSTANCE_OF.equals(attribute) ?
                newInstanceOfIterator(object.objGetClass()) :
                object.objGetIterable(attribute).iterator();
        } catch (Exception exception){
            new RuntimeServiceException(exception).log();
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.query.AbstractFilter#accept(java.lang.Object)
     */
    public boolean accept(Object candidate) {
        Object_1_0 object = (Object_1_0)candidate;
        try {
            return 
                !object.objIsDeleted() &&
                super.accept(candidate);
        } catch (Exception exception){
            new RuntimeServiceException(exception).log();
            return false;
        }
    }
               
}
