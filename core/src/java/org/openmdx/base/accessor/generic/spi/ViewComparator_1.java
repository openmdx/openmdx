/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ViewComparator_1.java,v 1.2 2007/11/13 11:54:21 hburger Exp $
 * Description: ViewComparator_1 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/13 11:54:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

import java.util.Collections;
import java.util.Iterator;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.AbstractComparator;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.state1.view.DateStateContexts;

/**
 * ViewComparator_1
 */
public class ViewComparator_1
    extends AbstractComparator
{

    /**
     * Constructor 
     *
     * @param order
     */
    public ViewComparator_1(
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
    ) {
        Object value;
        try {
            Object_1_0 object = (Object_1_0) candidate;
            value = object.objGetValue(attribute);
            if(value == null) {
                if(SystemAttributes.OBJECT_IDENTITY.equals(attribute)) {
                    Path path = object.objGetPath();
                    if(path != null) {
                        value = path.toUri();
                    }
                }
            } else {
                value = marshal(value);
            }
        } catch (Exception ignore) {
            value = null;
        }
        return (
            value == null ? Collections.EMPTY_SET : Collections.singleton(value)
        ).iterator();
    }

    private Object marshal(
        Object source
    ){
        return 
            source == null ? null :
            source instanceof XMLGregorianCalendar ? DateStateContexts.toBasicFormat((XMLGregorianCalendar)source) :
            source;
    }

}
