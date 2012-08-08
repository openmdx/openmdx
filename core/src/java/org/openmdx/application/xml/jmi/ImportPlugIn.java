/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ImportPlugIn.java,v 1.4 2010/04/16 13:18:01 hburger Exp $
 * Description: Import Plug-In
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/16 13:18:01 $
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
package org.openmdx.application.xml.jmi;

import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.xml.spi.ImportMode;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * Import Plug-In
 */
public interface ImportPlugIn {

    /**
     * Retrieve an existing or newly created object instance
     * 
     * @param persistenceManager
     * @param mode 
     * @param objectHolder 
     * @param objectClass
     * 
     * @return the requested object, never <code>null</code>
     * 
     * @throws ServiceException if the object does not yet exist or can't be acquired
     */
    <T extends RefObject> T getInstance(
        PersistenceManager persistenceManager,
        ImportMode mode,
        MappedRecord objectHolder, 
        Class<T> objectClass
    ) throws ServiceException;

    /**
     * Prepare an instance
     *
     * @param persistenceManager 
     * @param target
     * @param objectHolder
     * @param cache
     */
    void prepareInstance(
        PersistenceManager persistenceManager,
        RefObject target,
        MappedRecord objectHolder, 
        Map<Path,RefObject> cache
    ) throws ServiceException;
    
}
