/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Model Builder 
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
package org.openmdx.application.mof.repository.accessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelBuilder_1_0;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;

/**
 * Model Builder
 */
public class ModelBuilder_1 implements ModelBuilder_1_0 {

    /**
     * Create a model repository re-builder
     * 
     * @param source the URL of the model repository dump
     */
    public ModelBuilder_1(
        Boolean xmlValidation,
        InputStream dump
    ){
        this(
            new ModelRepositoryLoader(dump)
        );
    }

    /**
     * Create a model repository re-builder
     * 
     * @param source the URL of the model repository dump
     * 
     * @throws IOException 
     */
    public ModelBuilder_1(
        Boolean xmlValidation,
        URL dump
    ) throws IOException{
        this(
            new ModelRepositoryLoader(dump.openStream())
        );
    }

    /**
     * Create a model repository builder
     * @param qualifiedPackageNames the single colon separated package names
     * 
     * @return a new model repository builder
     * @throws ServiceException  
     */
    public ModelBuilder_1(
        Boolean xmlValidation,
        Set<String> qualifiedPackageNames
    ) throws ServiceException {
        this(
            new ModelPackageLoader(
                null, 
                qualifiedPackageNames.toArray(new String[qualifiedPackageNames.size()]), 
                Boolean.TRUE.equals(xmlValidation)
            )
        );
    }

    /**
     * Create a model repository builderModelRepositoryLoader
     * @param modelProvider the model provider
     * 
     * @return a new model repository builder
     * @throws ServiceException 
     */
    public ModelBuilder_1(
        Boolean xmlValidation,
        Dataprovider_1_0 modelProvider
    ) throws ServiceException{
        this(
            new ModelPackageLoader(
                modelProvider, 
                null, 
                Boolean.TRUE.equals(xmlValidation)
             )
        );
    }

    /**
     * Constructor 
     * @param modelLoader
     */
    private ModelBuilder_1(
        ModelLoader modelLoader
    ){
        this.modelLoader = modelLoader;
    }

    /**
     * The model information source in case of <code>DUMP</code> 
     */
    private final ModelLoader modelLoader;
    
    /**
     * 
     */
    private Map<String,ModelElement_1_0> modelElements;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelBuilder_1_0#build()
     */
    @Override
    public Model_1_0 build(
    ) throws ServiceException {
        this.modelElements = new HashMap<String, ModelElement_1_0>();
        final Map<String, List<AssociationDef>> associationDefs = new HashMap<String, List<AssociationDef>>(); 
        final Model_1 model = new Model_1(this.modelElements, associationDefs);
        this.modelLoader.populateModelElements(model);
        populateAssociationDefs(associationDefs);
        return model;
    }

    /** 
     * Prepare AssociationDefs which allow fast lookup of referenced and exposed
     * ends given a path
     */
    private void populateAssociationDefs(
        Map<String,List<AssociationDef>> associationDefMap
    ) throws ServiceException {
        for(
            Iterator<ModelElement_1_0> i = this.modelElements.values().iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 elementDef = i.next();
            /**
             * Add only associations used in references to the list of AssociationDefs
             */
            if(elementDef.objGetClass().equals(ModelAttributes.REFERENCE)) {
                addAssociationDef(associationDefMap, elementDef);
            }
        }
    }

    /**
     * @param associationDefMap
     * @param elementDef
     * @throws ServiceException
     */
    private void addAssociationDef(
        Map<String, List<AssociationDef>> associationDefMap,
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        Path referencedEndPath = (Path)elementDef.objGetValue("referencedEnd");
        Path exposedEndPath = (Path)elementDef.objGetValue("exposedEnd");
        String referenceName = (String)elementDef.objGetValue("name");
        List<AssociationDef> associationDefs = associationDefMap.get(referenceName);
        if(associationDefs == null) {
            associationDefMap.put(
                referenceName,
                associationDefs = new ArrayList<AssociationDef>()
            );
        }
        associationDefs.add(
            new AssociationDef(
                ModelHelper.getDereferencedType(
                    ModelHelper.getElement(exposedEndPath, this.modelElements).objGetValue("type"),
                    this.modelElements
                ),
                ModelHelper.getDereferencedType(
                    ModelHelper.getElement(referencedEndPath, this.modelElements).objGetValue("type"),
                    this.modelElements
                ),
                elementDef,
                modelElements
            )
        );
    }
    
}
