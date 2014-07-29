/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Map Target
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2014, OMEX AG, Switzerland
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
package org.openmdx.application.xml.spi;

import java.util.Map;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * Map Target
 */
public class MapTarget implements ImportTarget {

    /**
     * Constructor
     * 
     * @param target
     */
    public MapTarget(Map<Path, MappedRecord> target) {
        this.target = target;
    }

    /**
     * The delegate
     */
    private final Map<Path, MappedRecord> target;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.application.xml.ImportTarget#importObject(org.openmdx
     * .application.xml.ImportTarget.Mode, javax.resource.cci.MappedRecord)
     */
    @SuppressWarnings("unchecked")
    public void importObject(
        ImportMode mode, 
        MappedRecord objectHolder
    ) throws ServiceException {
        Path objectId = Object_2Facade.getPath(objectHolder);
        boolean exists = this.target.containsKey(objectId);
        switch (mode) {
            case UPDATE:
                if(!exists) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        "There is no object with the given id in the map",
                        new BasicException.Parameter("xri", objectId),
                        new BasicException.Parameter("mode", mode)
                    );
                }
                Object_2Facade.getValue(this.target.get(objectId)).putAll(
                    Object_2Facade.getValue(objectHolder)
                );
                break;
            case SET:
                if(exists) {
                    Object_2Facade.getValue(this.target.get(objectId)).putAll(
                        Object_2Facade.getValue(objectHolder)
                    );
                } else {
                    this.target.put(objectId, objectHolder);
                }
                break;
            case CREATE:
                if(exists) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.DUPLICATE,
                        "There is already an object with the given id in the map",
                        new BasicException.Parameter("xri", objectId),
                        new BasicException.Parameter("mode", mode)
                    );
                }
                this.target.put(objectId, objectHolder);
                break;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ImportTarget#importEpilog()
     */
    public void importEpilog(
        boolean successful
    ) {
        // nothing to be done
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ImportTarget#importProlog()
     */
    public void importProlog() {
        // nothing to be done
    }
    
}