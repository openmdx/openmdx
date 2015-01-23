/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Model Repository Loader 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013-2014, OMEX AG, Switzerland
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
package org.openmdx.application.mof.repository.accessor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.application.xml.spi.ImportHelper;
import org.openmdx.application.xml.spi.ImportMode;
import org.openmdx.application.xml.spi.MapTarget;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.xml.sax.InputSource;

/**
 * Model Loader
 */
class ModelRepositoryLoader implements ModelLoader {

    /**
     * Constructor 
     */
    ModelRepositoryLoader(
        InputStream source
    ) {
        this.source = source;
    }

    /**
     * The input stream
     */
    private final InputStream source;
    
    /**
     * The model elements
     */
    private Map<String,ModelElement_1_0> target;

    /**
     * Restore the model content from its dump
     * 
     * @return the model content
     * 
     * @throws ServiceException
     */
    @Override
    public void populateModelElements(
        Model_1 model
    ) throws ServiceException {
        this.target = model.getModelElements();
        reCreateModelElements(model);
        completeInstanceOf();
    }

    /**
     * @param channel
     * @throws ServiceException
     */
    private void reCreateModelElements(
        Model_1 model
    ) throws ServiceException {
        for(Map.Entry<Path, ObjectRecord> entry : getContent().entrySet()) {
            if(!"org:omg:model1:Segment".equals(Object_2Facade.getObjectClass(entry.getValue()))){
                target.put(
                    entry.getKey().getLastSegment().toClassicRepresentation(),
                    new ModelElement_1(entry.getValue(), model)
                );
            }
        }
    }

    /**
     * Read the dumped model repository content
     * 
     * @return the data
     * 
     * @throws ServiceException
     */
    private Map<Path, ObjectRecord> getContent(
    ) throws ServiceException {
        Map<Path, ObjectRecord> content = new HashMap<Path, ObjectRecord>();
        new ImportHelper().importObjects(
            new MapTarget(content),
            ImportHelper.asSource(new InputSource(this.source)), 
            null, // errorHandler
            ImportMode.CREATE
        );
        return content;
    }

    /**
     * Calculate the "object_instanceof" values
     * 
     * @throws ServiceException
     */
    private void completeInstanceOf(
    ) throws ServiceException {
        for(Map.Entry<String,ModelElement_1_0> entry : target.entrySet()){
            ModelElement_1_0 element = entry.getValue();
            if(element.isClassType()) {
                List<Object> objectInstanceOf = element.objGetList(SystemAttributes.OBJECT_INSTANCE_OF);
                for(Object p : element.objGetList("allSupertype")) {
                    String superType = ((Path)p).getLastSegment().toClassicRepresentation();
                    if(!objectInstanceOf.contains(superType)) {
                        objectInstanceOf.add(superType);
                    }
                }
            }
        }
    }
    
}
