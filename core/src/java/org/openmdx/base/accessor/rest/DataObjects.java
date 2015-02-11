/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Dirty Features 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.rest;

import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.spi.DelegatingRefObject_1_0;
import org.openmdx.base.exception.ServiceException;


/**
 * Data Objects
 */
public class DataObjects {

    /**
     * Constructor 
     */
    private DataObjects() {
        // Avoid instantiation
    }

    /**
     * Retrieve the data object associated with a given JMI object
     * 
     * @return the data object associated with the given JMI object
     * 
     * @throws ServiceException
     */
    public static DataObject_1_0 getDataObject(
        RefObject refObject
    ){
        return (
            (RefObject_1_0) (refObject instanceof DelegatingRefObject_1_0 ? ((DelegatingRefObject_1_0)refObject).openmdxjdoGetDataObject() : refObject)
        ).refDelegate().objGetDelegate();
    }
 
}