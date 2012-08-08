/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Model_1.java,v 1.79 2008/11/25 17:47:50 hburger Exp $
 * Description: Model_1 basic accessor
 * Revision:    $Revision: 1.79 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/25 17:47:50 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.model1.accessor.basic.spi;

import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.CompactSparseList;
import org.openmdx.compatibility.base.collection.OffsetArrayList;
import org.openmdx.compatibility.base.collection.SparseArray;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.importer.xml.XmlImporter;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.url.protocol.XRI_2Protocols;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_1;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_6;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.ModelConstraints;
import org.openmdx.model1.code.ModelExceptions;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.code.Stereotypes;
import org.openmdx.model1.mapping.AbstractNames;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.xmi.XMINames;
import org.w3c.cci2.LargeObject;

/**
 * Helper class to access MOF repository. The class adds utility 
 * functions and provides a cache for fast model element access.
 */
//---------------------------------------------------------------------------
public class Model_1 implements Model_1_6 {

    //-------------------------------------------------------------------------
    public Model_1(
    ) throws ServiceException {
        if(Model_1.repository == null) {
            Model_1.repository = Model_1.createRepository(null);          
        }
        this.setupRepository();
    }

    //-------------------------------------------------------------------------
    public Model_1(
        String storageFolder
    ) throws ServiceException {
        if(Model_1.repository == null) {
            Model_1.repository = Model_1.createRepository(storageFolder);
        }
        Model_1.isSetup = false;
        this.setupRepository();
    }

    //-------------------------------------------------------------------------
    public Model_1(
        Dataprovider_1_0 repository
    ) throws ServiceException {
        if(Model_1.repository == null) {
            Model_1.repository = repository != null ? 
                repository : 
                Model_1.createRepository(null);
        }
        Model_1.isSetup = false;
        this.setupRepository();
    }

    //-------------------------------------------------------------------------
    public Model_1(
        Dataprovider_1_0 repository,
        boolean force
    ) throws ServiceException {
        Model_1.repository = repository != null ? 
            repository : 
                Model_1.createRepository(null);
        Model_1.isSetup = false;
        this.setupRepository();
    }

    //-------------------------------------------------------------------------
    private void setupRepository(
    ) throws ServiceException {
        synchronized(Model_1.repository) {
            if(!Model_1.isSetup) {
                Model_1.channel = new RequestCollection(
                    new ServiceHeader(),
                    Model_1.repository
                );
                this.refreshCache();
                this.refreshAssociationDefs();
                Model_1.structuralFeatureDefMap.clear();
                Model_1.isSetup = true;
            }
        }
    }

    //-------------------------------------------------------------------------
    static class Model_1Provider extends Layer_1 {

        //-----------------------------------------------------------------------    
        public Model_1Provider(
            List<Layer_1_0> delegates
        ) throws Exception {
            this.delegates = delegates;
            this.activate(
                (short)5, 
                new Configuration(), 
                delegates.get(0)
            );
        }

        //-----------------------------------------------------------------------
        public void deactivate(
        ) throws Exception {
            for(Iterator<Layer_1_0> i = this.delegates.iterator(); i.hasNext(); ) {
                (i.next()).deactivate();
            }          
        }

        //-----------------------------------------------------------------------
        // Variables
        //-----------------------------------------------------------------------
        private final List<Layer_1_0> delegates;
    }

    //-------------------------------------------------------------------------
    /**
     * Constructs an in-memory MOF repository consisting of the MOF Model_1 
     * application and the InMemory_1 persistence plugin.
     */
    public static Layer_1_0 createRepository(
        String storageFolder
    ) throws ServiceException {
        try {
            Configuration configuration = new Configuration();
            configuration.values("namespaceId").add("model1");
            Configuration persistenceConfiguration = new Configuration(configuration);
            if(storageFolder != null) {
                persistenceConfiguration.values("storageFolder").add(storageFolder);
            }
            Layer_1_0 persistencePlugin = (Layer_1_0)Classes.getApplicationClass(
                "org.openmdx.compatibility.base.dataprovider.layer.persistence.none.InMemory_1"
            ).newInstance();
            persistencePlugin.activate((short)0, persistenceConfiguration, null);
            Layer_1_0 modelPlugin = (Layer_1_0)Classes.getApplicationClass(
                "org.openmdx.model1.layer.model.Model_1"
            ).newInstance();
            modelPlugin.activate((short)1, configuration, persistencePlugin);
            Layer_1_0 applicationPlugin = (Layer_1_0)Classes.getApplicationClass(
                "org.openmdx.model1.layer.application.Model_1"
            ).newInstance();
            applicationPlugin.activate((short)2, configuration, modelPlugin);
            Layer_1_0 typePlugin = (Layer_1_0)Classes.getApplicationClass(
                "org.openmdx.model1.layer.type.Model_1"
            ).newInstance();
            typePlugin.activate((short)3, configuration, applicationPlugin);
            Layer_1_0 interceptionPlugin = (Layer_1_0)Classes.getApplicationClass(
                "org.openmdx.compatibility.base.dataprovider.layer.interception.Standard_1"
            ).newInstance();
            Configuration interceptionConfiguration = new Configuration(configuration);
            interceptionConfiguration.values("propagateSet").add(Boolean.TRUE);
            interceptionPlugin.activate((short)4, interceptionConfiguration, typePlugin);

            return new Model_1Provider(
                Arrays.asList(
                    new Layer_1_0[]{
                        interceptionPlugin,
                        typePlugin,
                        applicationPlugin,
                        modelPlugin,
                        persistencePlugin
                    }
                )
            );
        }
        catch(ServiceException e) {
            throw e;
        }
        catch(Exception e) {
            throw new RuntimeServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Helper class holding the types of the exposed and referenced ends.
     * In addition provides a method which returns the type of the referenced
     * end plus all its subtypes and their supertypes. An AssociationDef allows
     * efficient validation of object paths.
     */
    static class AssociationDef {

        //-----------------------------------------------------------------------
        /**
         * Creates and AssociationDef. exposedType and referencedType are the
         * types of the exposedEnd and referenceEnd of the supplied reference.
         */
        public AssociationDef(
            ModelElement_1_0 exposedType, 
            ModelElement_1_0 referencedType,
            ModelElement_1_0 reference,
            Model_1_0 model
        ) throws ServiceException {
            this.exposedType = exposedType;
            this.referencedType = referencedType;
            /**
             * Calculate the set of types referenced by referencedType
             * This is all subtypes of the referenced type and their supertypes
             */
            this.allReferencedTypes = new HashSet<String>();      
            for(
                Iterator<?> i = this.referencedType.values("allSubtype").iterator(); 
                i.hasNext(); 
            ) {
                this.allReferencedTypes.add(
                    ((Path)i.next()).getBase()
                );
            }
            // add all supertypes of referenced types
            Set<String> allSupertypes = new HashSet<String>();
            for(
                Iterator<?> i = this.allReferencedTypes.iterator();
                i.hasNext();
            ) {
                try {
                    ModelElement_1_0 type = model.getElement(i.next());
                    for(
                        Iterator<?> j = type.values("allSupertype").iterator(); 
                        j.hasNext(); 
                    ) {
                        allSupertypes.add(
                            ((Path)j.next()).getBase()
                        );
                    }
                }
                catch(Exception e) {
                    System.out.println("element not found");
                }
            }
            this.allReferencedTypes.addAll(
                allSupertypes
            );
            this.reference = reference;
        }

        //-----------------------------------------------------------------------
        public Set<String> getAllReferencedTypes(
        ) {
            return this.allReferencedTypes;
        }

        //-----------------------------------------------------------------------
        public ModelElement_1_0 getReferencedType(
        ) {
            return this.referencedType;
        }

        //-----------------------------------------------------------------------
        public ModelElement_1_0 getExposedType(
        ) {
            return this.exposedType;
        }

        //-----------------------------------------------------------------------
        public ModelElement_1_0 getReference(
        ) {
            return this.reference;
        }

        //-----------------------------------------------------------------------
        public String toString(
        ) {
            return
            "  reference=" + reference.path() + "\n" +
            "  referencedType=" + referencedType.path() + "\n" +
            "  exposedType=" + exposedType.path() + "\n" + 
            "  allReferencedTypes=" + allReferencedTypes + "\n";
        }

        //-----------------------------------------------------------------------
        // Variables
        //-----------------------------------------------------------------------
        private ModelElement_1_0 exposedType;
        private ModelElement_1_0 referencedType;
        private ModelElement_1_0 reference;
        private Set<String> allReferencedTypes;
    }

    //-------------------------------------------------------------------------
    private void loadModels(
        Collection<String> qualifiedPackageNames
    ) throws ServiceException {

        // get exclusive access to repository. Synchronize with
        // other concurrent loaders are readers
        synchronized(Model_1.repository) {
            boolean isDirty = false;
            boolean importing = false;
            for(
                Iterator<String> i = qualifiedPackageNames.iterator();
                i.hasNext();
            ) {
                String qualifiedPackageName = toCanonicalModelPackage(i.next());
                if(!Model_1.loadedModels.contains(qualifiedPackageName)) {
                    String modelName = qualifiedPackageName.substring(
                        qualifiedPackageName.lastIndexOf(':') + 1
                    );
                    // test whether package already loaded
                    String qualifiedModelName = qualifiedPackageName + ":" + modelName;
                    if(Model_1.cache.get(qualifiedModelName) == null) {
                        if(!importing) {
                            // beginImport
                            DataproviderObject params = new DataproviderObject(
                                PROVIDER_ROOT_PATH.getDescendant(
                                    "segment", "-", "beginImport"
                                )
                            );
                            params.values(SystemAttributes.OBJECT_CLASS).add(
                                "org:openmdx:base:Void"
                            );
                            Model_1.channel.addOperationRequest(params);
                            importing = true;
                        }        
                        // load model from XMI resource
                        String xmlResource = XRI_2Protocols.RESOURCE_PREFIX + this.toJavaPackageName(
                            qualifiedPackageName,
                            XMINames.XMI_PACKAGE_SUFFIX
                        ).replace('.', '/') + '/' + modelName + "_edit.xml";
                        SysLog.detail("loading model " + modelName + " from " + xmlResource);

                        // import elements
                        new XmlImporter(
                            new ServiceHeader(),
                            Model_1.repository
                        ).process(
                            xmlResource
                        );        
                        isDirty = true;
                    }  
                    // add to model to loaded models  
                    Model_1.loadedModels.add(qualifiedPackageName);
                }
            }
            if(isDirty) {
                if(importing) {
                    // endImport
                    DataproviderObject params = new DataproviderObject(
                        PROVIDER_ROOT_PATH.getDescendant(
                            "segment", "-", "endImport"
                        )
                    );
                    params.values(SystemAttributes.OBJECT_CLASS).add(
                        "org:openmdx:base:Void"
                    );
                    Model_1.channel.addOperationRequest(params);
                }    
                // retrieve imported and merged models and update cache    
                this.refreshCache();
                this.refreshAssociationDefs();
                Model_1.structuralFeatureDefMap.clear();
            }
        }
    }
    //---------------------------------------------------------------------------

    /**
     * Provides mappings for openMDX 1/2 model name changes.
     * 
     * @param qualifiedModelName
     * 
     * @return the <code>qualifiedModelName</code>'s canonical name
     */
    protected String toCanonicalModelPackage(
        String qualifiedModelName
    ){
        int index = Arrays.binarySearch(FROM_MODEL_PACKAGE_NAMES, qualifiedModelName);
        if(index < 0) {
            return qualifiedModelName;
        } else {
            SysLog.warning(
                "Deprecated model package name " + qualifiedModelName,
                "Replaced by canonical model package name " + TO_MODEL_PACKAGE_NAMES[index]
            );
            return TO_MODEL_PACKAGE_NAMES[index];
        }
    }

    //---------------------------------------------------------------------------
    /** 
     * Prepare AssociationDefs which allow fast lookup of referenced and exposed
     * ends given a path
     */
    private void refreshAssociationDefs(
    ) throws ServiceException {
        Map<String,List<AssociationDef>> associationDefMap = new HashMap<String,List<AssociationDef>>();
        for(
            Iterator<ModelElement_1_0> i = this.getContent().iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 elementDef = i.next();
            /**
             * Add only associations used in references to the list of AssociationDefs
             */
            if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.REFERENCE)) {
                Path referencedEndPath = (Path)elementDef.getValues("referencedEnd").get(0);
                Path exposedEndPath = (Path)elementDef.getValues("exposedEnd").get(0);
                String referenceName = (String)elementDef.getValues("name").get(0);
                List<AssociationDef> associationDefs = null;
                if((associationDefs = associationDefMap.get(referenceName)) == null) {
                    associationDefMap.put(
                        referenceName,
                        associationDefs = new ArrayList<AssociationDef>()
                    );
                }
                associationDefs.add(
                    new AssociationDef(
                        this.getDereferencedType(
                            getElement(exposedEndPath).getValues("type").get(0)
                        ),
                        this.getDereferencedType(
                            getElement(referencedEndPath).getValues("type").get(0)
                        ),
                        elementDef,
                        this
                    )
                );
            }
        }
        Model_1.associationDefMap = associationDefMap;
    }

    //---------------------------------------------------------------------------
    /**
     * Returns the AssociationDefs matching the object path. result[0] contains
     * the AssociationDefs corresponding to the last and second last reference 
     * elements of the path. result[1] is a list containing 1..n AssociationDefs
     * depending on whether the reference qualifies uniquely or not.
     */ 
    private AssociationDef[] getAssociationDefs(
        Path objectPath
    ) throws ServiceException {
        /**        
         * Iterate all reference names and follow the ReferenceLinks
         */     
        AssociationDef prev = null;
        // start from root association The association to the authority
        // is not modeled and is created is virtual association.
        AssociationDef current = new AssociationDef(
            null,
            this.getDereferencedType(AUTHORITY_TYPE_NAME),
            this.getDereferencedType(AUTHORITY_TYPE_NAME + ":provider"),
            this
        );
        for(
            int i = 1; 
            i < objectPath.size(); 
            i+=2
        ) {
            String referenceName = objectPath.get(i);
            // get candidate association definitions
            List<AssociationDef> candidates = Model_1.associationDefMap.get(referenceName);
            if(candidates == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "unknown reference in path.",
                    new BasicException.Parameter("path", objectPath),
                    new BasicException.Parameter("reference", referenceName)
                );
            }    
            // get next associations
            List<AssociationDef> next = new ArrayList<AssociationDef>();
            for(
                Iterator<AssociationDef> j = candidates.iterator();
                j.hasNext();
            ) {
                AssociationDef associationDef = j.next();        
                String exposedEndQualifiedName = (String)associationDef.getExposedType().getValues("qualifiedName").get(0);
                // Test whether one of the current referenced types matches the exposed type of 
                // the next candidate association
                if(current.getAllReferencedTypes().contains(exposedEndQualifiedName)) {
                    // Move prev forward only if not a root class is referenced. Referenced
                    // to root classes are interpreted as references to the concrete subclass, 
                    // e.g. ch:omex:generic:Role, ch:omex:generic:State
                    if(
                        !current.getReferencedType().values("stereotype").contains(Stereotypes.ROOT) || 
                        !((Boolean)current.getReferencedType().values("isAbstract").get(0)).booleanValue()
                    ) {
                        prev = current;
                    }
                    // prev is now the 'proper' current association. Add to the set of 
                    // next associations only if one of the referenced types matches the exposed
                    // type of the next candidate
                    if(prev.getAllReferencedTypes().contains(exposedEndQualifiedName)) {
                        next.add(associationDef);
                    }
                }
            }      
            // No matching assocation found
            if(next.isEmpty()) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "invalid reference. no matching association found",
                    new BasicException.Parameter("path", objectPath),
                    new BasicException.Parameter("reference/operation", referenceName),
                    new BasicException.Parameter("exposing class", current.getExposedType() == null ? null : current.getExposedType().values("qualifiedName").get(0))
                );
            }

            // the assocationDef with matching authority wins. This case occurs
            // e.g. when a path can matches to different models
            else if(next.size() > 1) {
                int matching = 0;
                int index = 0;
                int jj = 0;
                for(
                    Iterator<AssociationDef> j = next.iterator();
                    j.hasNext();
                    jj++
                ) {
                    AssociationDef assocationDef = j.next();
                    // model name (segment name) must match the path authority
                    if(assocationDef.getReference().path().get(4).equals(objectPath.get(0))) {
                        matching++;
                        index = jj; 
                    }
                }
                if(matching != 1) {        
                    List<String> matches = new ArrayList<String>();
                    for(
                        Iterator<AssociationDef> j = next.iterator();
                        j.hasNext();
                    ) {
                        Path referencePath = j.next().getReference().path(); 
                        if(referencePath.get(4).equals(objectPath.get(0))) {
                            matches.add(referencePath.get(6));
                        }
                    }
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "invalid reference. #matching referenced classifiers must be 1",
                        new BasicException.Parameter("path", objectPath),
                        new BasicException.Parameter("reference/operation", referenceName),
                        new BasicException.Parameter("exposing class", current.getExposedType() == null ? null : current.getExposedType().values("qualifiedName").get(0)),
                        new BasicException.Parameter("#matching referenced classifiers", matching),
                        new BasicException.Parameter("matching references", matches)
                    );
                }
                current = next.get(index);
            }

            // exact match
            else {
                current = next.get(0);
            }      
        }      
        return new AssociationDef[]{
            prev,
            current 
        };
    }

    //---------------------------------------------------------------------------
    private void verifyObjectCollection(
        Object values,
        Object type,
        String multiplicity,
        boolean enforceRequired,
        Stack<List<?>> validationContext, 
        boolean attributesOnly, 
        boolean verifyDerived
    ) throws ServiceException {
        int size;
        if(values instanceof Collection){
            size = ((Collection<?>)values).size();
        } 
        else if (values instanceof Map){
            size = ((Map<?,?>)values).size();
        } 
        else throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE, 
            "values expected to be in [Collectin|Map]",
            new BasicException.Parameter[]{
                new BasicException.Parameter("multiplicity", multiplicity),
                new BasicException.Parameter("values", values),
                new BasicException.Parameter("values class", (values == null) ? "null" : values.getClass().getName()),
                new BasicException.Parameter("context", validationContext)
            }
        );      
        // Verify multiplicity
        if(
            (Multiplicities.OPTIONAL_VALUE.equals(multiplicity) || Multiplicities.SINGLE_VALUE.equals(multiplicity)) &&
            (size > 1) 
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "number of values exceeds multiplicity",
                new BasicException.Parameter("multiplicity", multiplicity),
                new BasicException.Parameter("values", values),
                new BasicException.Parameter("values class", (values == null) ? "null" : values.getClass().getName()),
                new BasicException.Parameter("context", validationContext)
            );      
        }

        // verify stream muliplicity
        if(
            Multiplicities.STREAM.equals(multiplicity) &&
            size > 2
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "stream values restricted to stream and optional length",
                new BasicException.Parameter[]{
                    new BasicException.Parameter("multiplicity", multiplicity),
                    new BasicException.Parameter("values class", values == null ? null : values.getClass().getName()),
                    new BasicException.Parameter("context", validationContext)
                }
            );      
        }
        // Verify collection and multiplicity type.
        // SparseArray is valid for openMDX compatibility
        if(
            (Multiplicities.SET.equals(multiplicity) && !(values instanceof Set || values instanceof OffsetArrayList || values instanceof CompactSparseList)) ||
            (Multiplicities.LIST.equals(multiplicity) && !(values instanceof List)) ||
            (Multiplicities.SPARSEARRAY.equals(multiplicity) && !(values instanceof SortedMap || values instanceof SparseArray || values instanceof OffsetArrayList || values instanceof CompactSparseList)) ||
            (Multiplicities.MULTI_VALUE.equals(multiplicity) && !(values instanceof List))
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "multiplicity does not match value type",
                new BasicException.Parameter("multiplicity", multiplicity),
                new BasicException.Parameter("values", values),
                new BasicException.Parameter("values class", (values == null) ? "null" : values.getClass().getName()),
                new BasicException.Parameter("context", validationContext)
            );
        }
        // validate all values of collection
        validationContext.push(
            Arrays.asList("validated values", values)
        );
        int index = 0;
        for(
            Iterator<?> i = (values instanceof Collection ? (Collection<?>)values : ((Map<?,?>)values).values()).iterator();
            i.hasNext();
            index++
        ) {
            Object value = i.next();
            validationContext.push(
                Arrays.asList("index", new Integer(index), "value", value)
            );
            this.verifyObject(
                value,
                type,
                multiplicity,
                enforceRequired,
                validationContext, 
                attributesOnly, 
                verifyDerived
            );
            validationContext.pop();
        }
        validationContext.pop();      
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void verifyObject(
        Object value,
        Object type,
        String multiplicity,
        boolean enforceRequired,
        Stack<List<?>> validationContext, 
        boolean attributesOnly, 
        boolean verifyDerived
    ) throws ServiceException {
        // null --> no validation
        if((type == null) || (value == null)) {
            return;
        }
        ModelElement_1_0 typeDef = this.getDereferencedType(type);
        String typeName = (String)typeDef.values("qualifiedName").get(0); 
        // Collection
        if(value instanceof Collection || value instanceof SortedMap) {
            this.verifyObjectCollection(
                value,
                typeDef,
                multiplicity,
                enforceRequired,
                validationContext, 
                attributesOnly, 
                verifyDerived
            );
        }
        // PrimitiveType
        else if(this.isPrimitiveType(type)) {
            if(
                (value instanceof Boolean) && 
                "org:w3c:boolean".equals(typeName)
            ) {
                return;
            }
            else if(
                (value instanceof String) && 
                (PrimitiveTypes.STRING.equals(typeName) || PrimitiveTypes.ANYURI.equals(typeName))
            ) {
                return;
            }
            else if(
                (value instanceof Number) && 
                (PrimitiveTypes.DECIMAL.equals(typeName) || PrimitiveTypes.LONG.equals(typeName) || PrimitiveTypes.SHORT.equals(typeName) || PrimitiveTypes.INTEGER.equals(typeName))
            ) {
                return;
            }
            else if(
                (value instanceof String) && 
                PrimitiveTypes.DATETIME.equals(typeName)
            ) {
                try {
                    DateFormat.getInstance().parse((String)value);
                    return;
                } 
                catch(ParseException e) {
                    throw new ServiceException(e);
                }
            }
            else if(
                (value instanceof String) && 
                PrimitiveTypes.DATE.equals(typeName)
            ) {
                if(((String)value).length() > 8) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PARSE_FAILURE, 
                        "date must be of format YYYYMMDD",
                        new BasicException.Parameter("value", value),
                        new BasicException.Parameter("context", validationContext)
                    );
                }
                return;
            }
            else if(
                (value instanceof byte[] || value instanceof LargeObject) && 
                PrimitiveTypes.BINARY.equals(typeName)
            ) {
                return;
            }
            else if(
                (value instanceof Reader || value instanceof Long) && 
                PrimitiveTypes.STRING.equals(typeName) &&
                Multiplicities.STREAM.equals(multiplicity)
            ) {
                return;
            }
            else if(
                (value instanceof InputStream || value instanceof LargeObject || value instanceof Long) && 
                PrimitiveTypes.BINARY.equals(typeName) &&
                Multiplicities.STREAM.equals(multiplicity)
            ) {
                return;
            }
            else if(
                (value instanceof Path) && 
                (PrimitiveTypes.OBJECT_ID.equals(typeName) || this.isClassType(type))
            ) {
                if(((Path)value).size() % 2 != 1) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "not an object path. path.size() % 2 == 1",
                        new BasicException.Parameter("value", value),
                        new BasicException.Parameter("context", validationContext)
                    );
                }
                return;
            }
            else if(
                (value instanceof String) && 
                (PrimitiveTypes.OBJECT_ID.equals(typeName) || this.isClassType(type))
            ) {
                Path p = new Path((String)value);
                if(p.size() % 2 != 1) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "not an object path. path.size() % 2 == 1",
                        new BasicException.Parameter("value", value),
                        new BasicException.Parameter("context", validationContext)
                    );
                }
                return;
            }
            // Unknown primitive type. Assume it is a string
            else if(value instanceof String) {
                return;
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "type mismatch or type not supported",
                    new BasicException.Parameter("value", value),
                    new BasicException.Parameter("value class", (value == null) ? "null" : value.getClass().getName()),
                    new BasicException.Parameter("type", typeName),
                    new BasicException.Parameter("context", validationContext)
                );
            }
        }
        // StructureType
        else if(this.isStructureType(type)) {
            Set<String> fieldNames;
            if(value instanceof Structure_1_0) {
                fieldNames = new TreeSet<String>(((Structure_1_0)value).objFieldNames());
                // remove empty fields
                for(Iterator<String> i = fieldNames.iterator(); i.hasNext(); ) {
                    if(((Structure_1_0)value).objGetValue(i.next()) == null) {
                        i.remove();
                    } 
                }
            }
            else if(value instanceof DataproviderObject_1_0) {
                fieldNames = new TreeSet<String>(((DataproviderObject)value).attributeNames());
                // remove empty fields
                for(Iterator<String> i = fieldNames.iterator(); i.hasNext(); ) {
                    if(((DataproviderObject_1_0)value).getValues(i.next()) == null) {
                        i.remove();
                    } 
                }
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "value type not supported. Allowed are [Structure_1_0|DataproviderObject_1_0]",
                    new BasicException.Parameter("value", value),
                    new BasicException.Parameter("value class", (value == null) ? "null" : value.getClass().getName()),
                    new BasicException.Parameter("context", validationContext)
                );        
            }       
            Map<String,ModelElement_1_0> fieldDefs = (Map<String,ModelElement_1_0>)typeDef.values("field").get(0);
            // complete fieldNames with all required fields in case of includeRequired
            if((fieldDefs != null) && enforceRequired) {
                for(
                    Iterator<ModelElement_1_0> i = fieldDefs.values().iterator();
                    i.hasNext();
                ) {
                    ModelElement_1_0 fieldDef = i.next();
                    if(Multiplicities.SINGLE_VALUE.equals(fieldDef.values("multiplicity").get(0))) {
                        fieldNames.add(
                            (String)fieldDef.values("name").get(0)
                        );
                    }
                }
            }
            // validate all fields contained in value
            for(
                Iterator<String> i = fieldNames.iterator(); 
                i.hasNext(); 
            ) {
                String fieldName = i.next();
                // object_class for openMDX/2 compability
                if(
                    !fieldName.equals(SystemAttributes.OBJECT_CLASS) &&
                    !fieldName.equals(SystemAttributes.OBJECT_INSTANCE_OF)
                ) {
                    ModelElement_1_0 featureDef = fieldDefs.get(fieldName);        
                    if(featureDef == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE, 
                            "attribute not member of class",
                            new BasicException.Parameter("value", value),
                            new BasicException.Parameter("field", fieldName),
                            new BasicException.Parameter("structure type", typeDef.values("qualifiedName").get(0)),
                            new BasicException.Parameter("context", validationContext)
                        );
                    }
                    else {
                        Object fieldValue = value instanceof Structure_1_0 ? 
                            ((Structure_1_0)value).objGetValue(fieldName) : 
                            ((DataproviderObject_1_0)value).getValues(fieldName);
                        if(enforceRequired && (fieldValue == null)) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE, 
                                "field value is null. Either missing required field or value contains field with null value",
                                new BasicException.Parameter("value", value),
                                new BasicException.Parameter("field", fieldName),
                                new BasicException.Parameter("context", validationContext)
                            );                           
                        }
                        validationContext.push(
                            Arrays.asList("validated field", fieldName)
                        );
                        this.verifyObject(
                            fieldValue,
                            featureDef.values("type").get(0),
                            (String)featureDef.values("multiplicity").get(0),
                            enforceRequired,
                            validationContext, 
                            attributesOnly, 
                            verifyDerived
                        );
                        validationContext.pop();
                    }
                }       
            }
        }

        // Class
        else if(this.isClassType(type)) {
            Set<String> attributeNames = null;

            // validateObject does not support deep verify. Referenced objects
            // are not validated.
            if(value instanceof Path) {
                return;
            }
            else if(value instanceof Object_1_0) {
                attributeNames = new TreeSet<String>(((Object_1_0)value).objDefaultFetchGroup());
            }
            else if(value instanceof DataproviderObject_1_0) {
                attributeNames = new TreeSet<String>(((DataproviderObject_1_0)value).attributeNames());
            } 
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "value type not supported. Allowed are [Path|Object_1_0|DataproviderObject_1_0]",
                    new BasicException.Parameter("value", value),
                    new BasicException.Parameter("value class", value.getClass().getName()),
                    new BasicException.Parameter("context", validationContext)
                );        
            }       
            Map<String,ModelElement_1_0> structuralFeatureDefs = this.getStructuralFeatureDefs(
                typeDef, 
                false, // includeSubtypes
                true, // includeDerived, 
                attributesOnly
            );
            // Complete attributeNames with all required fields in case of enforceRequired
            if(enforceRequired) {
                for(
                    Iterator<ModelElement_1_0> i = structuralFeatureDefs.values().iterator();
                    i.hasNext();
                ) {
                    ModelElement_1_0 fieldDef = i.next();
                    if(
                        Multiplicities.SINGLE_VALUE.equals(fieldDef.values("multiplicity").get(0)) && 
                        (verifyDerived || !Boolean.TRUE.equals(fieldDef.values("isDerived").get(0)))
                    ) {
                        attributeNames.add(
                            (String)fieldDef.values("name").get(0)
                        );
                    }
                }
            }
            // Validate all attributes contained in value
            for(
                Iterator<String> i = attributeNames.iterator(); 
                i.hasNext(); 
            ) {
                String attributeName = i.next();

                // object_class and object_instanceof for openMDX/2 compatibility
                // at the current time ignore namespaces
                if(
                    !attributeName.equals(SystemAttributes.OBJECT_CLASS) &&
                    !attributeName.equals(SystemAttributes.OBJECT_INSTANCE_OF) &&
                    (attributeName.indexOf(':') < 0)
                ) {
                    ModelElement_1_0 featureDef = structuralFeatureDefs.get(attributeName); 
                    if(featureDef == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE, 
                            "attribute not member of class",
                            new BasicException.Parameter("value", value),
                            new BasicException.Parameter("attribute", attributeName),
                            new BasicException.Parameter("object class", typeDef.values("qualifiedName").get(0)),
                            new BasicException.Parameter("context", validationContext)
                        );
                    }
                    Object featureMultiplicity = featureDef.values("multiplicity").get(0);
                    Object attributeValue;
                    if (value instanceof Object_1_0) {
                        Object_1_0 object = (Object_1_0)value;
                        if(
                            Multiplicities.LIST.equals(featureMultiplicity) ||
                            Multiplicities.MULTI_VALUE.equals(featureMultiplicity) || (
                                this.isReferenceType(featureDef) &&
                                this.referenceIsStoredAsAttribute(featureDef) &&
                                Multiplicities.OPTIONAL_VALUE.equals(featureMultiplicity)
                            )                      
                        ) {
                            attributeValue = object.objGetList(attributeName);
                        } 
                        else if(Multiplicities.SET.equals(featureMultiplicity)) {
                            attributeValue = object.objGetSet(attributeName);
                        } 
                        else if(Multiplicities.SPARSEARRAY.equals(featureMultiplicity)) {
                            attributeValue = object.objGetSparseArray(attributeName);
                        } 
                        else if(Multiplicities.STREAM.equals(featureMultiplicity)) {
                            attributeValue = object.objGetLargeObject(attributeName);
                        } 
                        else {
                            attributeValue = object.objGetValue(attributeName);                      
                            if(
                                enforceRequired && 
                                Multiplicities.SINGLE_VALUE.equals(featureMultiplicity) &&
                                attributeValue == null
                            ) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.VALIDATION_FAILURE, 
                                    "Attribute value is null. Required attribute is either missing or contains the null value",
                                    new BasicException.Parameter("value", value),
                                    new BasicException.Parameter("attribute", attributeName),
                                    new BasicException.Parameter("multiplicity", featureMultiplicity),
                                    new BasicException.Parameter("context", validationContext)
                                );
                            }
                        }
                    } 
                    else if (value instanceof DataproviderObject_1_0) {
                        SparseList<?> genericValue = ((DataproviderObject_1_0)value).getValues(attributeName);
                        attributeValue = genericValue; 
                        if(
                            enforceRequired && 
                            Multiplicities.SINGLE_VALUE.equals(featureMultiplicity) &&
                            (attributeValue == null || genericValue.isEmpty())
                        ) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.VALIDATION_FAILURE, 
                                "Attribute value is null. Required attribute is either missing or contains the null value",
                                new BasicException.Parameter("value", value),
                                new BasicException.Parameter("attribute", attributeName),
                                new BasicException.Parameter("multiplicity", featureMultiplicity),
                                new BasicException.Parameter("context", validationContext)
                            );
                        }
                    } 
                    else {
                        attributeValue = null;
                    }
                    validationContext.push(
                        Arrays.asList("validated attribute", attributeName)
                    ); 
                    // in case the feature is a reference stored as attribute check for qualifiers
                    String attributeMultiplicity = (String)featureDef.values("multiplicity").get(0);
                    if(this.isReferenceType(featureDef)) {
                        ModelElement_1_0 referencedEnd = getElement(
                            featureDef.values("referencedEnd").get(0)
                        );
                        if(!referencedEnd.values("qualifierType").isEmpty()) {
                            attributeMultiplicity = Multiplicities.MULTI_VALUE;
                        }
                    }
                    this.verifyObject(
                        attributeValue,
                        featureDef.values("type").get(0),
                        attributeMultiplicity,
                        enforceRequired,
                        validationContext, 
                        attributesOnly, 
                        verifyDerived
                    );
                    validationContext.pop();
                }
            }
        }
        // Unsupported type
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "unsupported model element. Must be [PrimitiveType|Class|StructureType]",
                new BasicException.Parameter("type", type),
                new BasicException.Parameter("value", value),
                new BasicException.Parameter("context", validationContext)
            );            
        }
    }

    //-------------------------------------------------------------------------
    // Model_1_0
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    public Dataprovider_1_0 getRepository(
    ) throws ServiceException {
        return Model_1.repository;
    }

    //-------------------------------------------------------------------------
    /**
     * Refreshes the element cache. In addition performs the following operations: 
     * <ul>
     *   <li> for CLASS model elements the attributes 'attribute' and 'reference' are 
     *        added. 'attribute' contains the list of all class attributes
     *        and 'reference' a list of all references. </li>
     *   <li> references which are stored as attributes are added to the 'attribute' list. </li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private void refreshCache(
    ) throws ServiceException {

        SysLog.trace("refreshing cache...");   

        Map<String,ModelElement_1_0> cache = new HashMap<String,ModelElement_1_0>();

        // get exclusive access to repository. synchronize
        // with possibly concurrent loaders
        synchronized(Model_1.repository) {
            List<DataproviderObject_1_0> models = Model_1.channel.addFindRequest(
                PROVIDER_ROOT_PATH.getChild("segment"),
                null
            );
            for(
                Iterator<DataproviderObject_1_0> i = models.iterator();
                i.hasNext();
            ) {
                List<DataproviderObject_1_0> elementDefs = Model_1.channel.addFindRequest(
                    (i.next()).path().getChild("element"),
                    null,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null, 
                    0,
                    Integer.MAX_VALUE,
                    Directions.ASCENDING
                );
                for(
                    Iterator<DataproviderObject_1_0> j = elementDefs.iterator();
                    j.hasNext();
                ) {
                    DataproviderObject elementDef = (DataproviderObject)j.next();
                    cache.put(
                        elementDef.path().getBase(),
                        new ModelElement_1(elementDef, this)
                    ); 
                }
            }
        }
        SysLog.detail("#elements in cache", new Integer(cache.size()));
        /**
         * Complete attributes 'attribute' and 'reference' for class elements
         * and 'field' for structures. This improves performance when accessing
         * the features and fields of classes and structures.
         */
        for(
            Iterator<ModelElement_1_0> i = cache.values().iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 element = i.next();
            // feature = content + content of all supertypes. Attn: StructurType is a Classifier so 
            // if must be in this order!
            List<Object> content = null;
            if(element.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.STRUCTURE_TYPE)) {
                content = element.values("content");
            }
            else if(element.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                content = element.values("feature");
            }
            if(content != null) {        
                Map<String,ModelElement_1_0> attributes = new HashMap<String,ModelElement_1_0>();
                Map<String,ModelElement_1_0> references = new HashMap<String,ModelElement_1_0>();
                Map<String,ModelElement_1_0> fields = new HashMap<String,ModelElement_1_0>();
                Map<String,ModelElement_1_0> operations = new HashMap<String,ModelElement_1_0>();
                for(
                    Iterator<?> j = content.iterator();
                    j.hasNext();
                ) {
                    Path contentElementPath = (Path)j.next();
                    if(!cache.containsKey(contentElementPath.getBase())) {
                        throw new ServiceException (
                            BasicException.Code.DEFAULT_DOMAIN, 
                            BasicException.Code.NOT_FOUND, 
                            "element is member of container but was not found in model. Probably the model is inconsistent.",
                            new BasicException.Parameter [] {
                                new BasicException.Parameter("container", element.path()),
                                new BasicException.Parameter("element", contentElementPath.getBase())
                            }
                        );
                    }
                    ModelElement_1_0 contentElement = cache.get(
                        contentElementPath.getBase()
                    );
                    if(contentElement.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.ATTRIBUTE)) {
                        attributes.put(
                            (String)contentElement.values("name").get(0),
                            contentElement
                        );
                    }
                    else if(contentElement.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.STRUCTURE_FIELD)) {
                        fields.put(
                            (String)contentElement.values("name").get(0),
                            contentElement
                        );
                    }
                    else if(contentElement.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.OPERATION)) {
                        operations.put(
                            (String)contentElement.values("name").get(0),
                            contentElement
                        );
                    }
                    else if(contentElement.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.REFERENCE)) {
                        references.put(
                            (String)contentElement.values("name").get(0),
                            contentElement
                        );
                        // add references stored as attribute to the list of attributes
                        if(this.referenceIsStoredAsAttribute(contentElement.path(), cache)) {
                            ModelElement_1_0 attribute = new ModelElement_1(
                                new DataproviderObject(contentElement),
                                this
                            );
                            attribute.clearValues(SystemAttributes.OBJECT_CLASS).add(
                                ModelAttributes.ATTRIBUTE
                            );
                            attribute.clearValues("isDerived").add(
                                Boolean.valueOf(
                                    this.referenceIsDerived(contentElement, cache)
                                )
                            );
                            // Maximum length of path
                            attribute.clearValues("maxLength").add(
                                new Integer(1024)
                            );
                            // If reference has a qualifier --> multiplicity 0..n
                            if(this.getElement(contentElement.values("referencedEnd").get(0), cache).values("qualifierName").size() > 0) {
                                attribute.clearValues("multiplicity").add(Multiplicities.MULTI_VALUE);
                            }
                            SysLog.trace("referenceIsStoredAsAttribute", attribute.path());
                            attributes.put(
                                (String)attribute.values("name").get(0),
                                attribute
                            );
                        }
                    }
                }
                element.clearValues("attribute").add(attributes);
                element.clearValues("reference").add(references);
                element.clearValues("operation").add(operations);
                element.clearValues("field").add(fields);
            }
        }
        // Complete allFeature
        for(
            Iterator<ModelElement_1_0> i = cache.values().iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 classDef = i.next();      
            if(classDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                Map<String,ModelElement_1_0> allFeature = new HashMap<String,ModelElement_1_0>();
                for(
                    Iterator<?> j = classDef.values("allSupertype").iterator();
                    j.hasNext();
                ) {
                    ModelElement_1_0 supertype = this.getElement(
                        j.next(),
                        cache
                    );
                    allFeature.putAll((Map)supertype.values("attribute").get(0));
                    allFeature.putAll((Map)supertype.values("reference").get(0));
                    allFeature.putAll((Map)supertype.values("operation").get(0));
                }
                classDef.clearValues("allFeature").add(allFeature);
            }
        }
        // Complete allFeatureWithSubtype
        for(
            Iterator<ModelElement_1_0> i = cache.values().iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 classDef = i.next();      
            if(classDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                Map<String,ModelElement_1_0> allFeatureWithSubtype = new HashMap<String,ModelElement_1_0>((Map)classDef.values("allFeature").get(0));
                for(
                    Iterator<?> j = classDef.values("allSubtype").iterator();
                    j.hasNext();
                ) {
                    ModelElement_1_0 subtype = this.getElement(
                        j.next(), 
                        cache
                    );
                    allFeatureWithSubtype.putAll((Map)subtype.values("attribute").get(0));
                    allFeatureWithSubtype.putAll((Map)subtype.values("reference").get(0));        
                    allFeatureWithSubtype.putAll((Map)subtype.values("operation").get(0));        
                }
                classDef.clearValues("allFeatureWithSubtype").add(allFeatureWithSubtype);
            }
        }

        Model_1.cache = cache; // activate the new cache
        SysLog.trace("done refreshing cache");
    }

    //-------------------------------------------------------------------------
    public void addModels(
        Collection<String> qualifiedPackageNames
    ) throws ServiceException {
        this.loadModels(
            qualifiedPackageNames
        );
    }

    //-------------------------------------------------------------------------
    protected ModelElement_1_0 getElement(
        java.lang.Object element,
        Map<String,ModelElement_1_0> elements
    ) {
        ModelElement_1_0 e = null;
        if(element instanceof ModelElement_1_0) {
            e = (ModelElement_1_0)element;
        }
        else if(element instanceof Path) {
            e = elements.get(((Path)element).getBase());
        }
        else if(element instanceof DataproviderObject_1_0) {
            e = elements.get(((DataproviderObject_1_0)element).path().getBase());
        }
        else if(element instanceof List) {
            String qualifiedElementName = "";
            int ii = 0;
            for(Iterator<?> i = ((List<?>)element).iterator(); i.hasNext(); ii++) {
                qualifiedElementName += (ii == 0 ? "" : ":") + i.next();
            }
            e = elements.get(qualifiedElementName);
        }
        else {
            e = elements.get(element); 
        }
        return e;
    }  

    //-------------------------------------------------------------------------
    public ModelElement_1_0 getElement(
        java.lang.Object element
    ) throws ServiceException {
        ModelElement_1_0 e = this.getElement(
            element,
            Model_1.cache
        ); 
        if(e == null) {
            throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.NOT_FOUND, 
                "element not found in model package",
                new BasicException.Parameter [] {
                    new BasicException.Parameter("element", element)
                }
            );
        }
        return e;
    }

    //-------------------------------------------------------------------------
    public ModelElement_1_0 findElement(
        Object element
    ) {
        return this.getElement(
            element,
            Model_1.cache
        );
    }

    //-------------------------------------------------------------------------
    public Collection<ModelElement_1_0> getContent(
    ) throws ServiceException {
        return Model_1.cache.values();
    }  

    //-------------------------------------------------------------------------
    public boolean isLocal(
        Object type,
        Object modelPackage
    ) throws ServiceException {
        String packageNamePackage = this.toJavaPackageName(modelPackage, "-");
        String packageNameType = this.toJavaPackageName(type, "-");
        return packageNamePackage.equals(packageNameType);
    }

    //---------------------------------------------------------------------------
    public ModelElement_1_0 getFeatureDef(
        ModelElement_1_0 classifierDef,
        String feature,
        boolean includeSubtypes
    ) throws ServiceException {

        ModelElement_1_0 featureDef = null;

        // Structure
        if(this.isStructureType(classifierDef)) {
            if((featureDef = (ModelElement_1_0)((Map<?,?>)classifierDef.values("field").get(0)).get(feature)) != null) {
                return featureDef;
            }
        }
        // Class
        else {
            if(includeSubtypes) {
                // references stored as attributes are in maps allReference and allAttribute. 
                // give allReference priority in case feature is a reference
                if((featureDef = (ModelElement_1_0)((Map<?,?>)classifierDef.values("allFeatureWithSubtype").get(0)).get(feature)) != null) {
                    return featureDef;
                }
            }
            else {
                // references stored as attributes are in maps allReference and allAttribute. 
                // give allReference priority in case feature is a reference
                if((featureDef = (ModelElement_1_0)((Map<?,?>)classifierDef.values("allFeature").get(0)).get(feature)) != null) {
                    return featureDef;
                }
            }
        }
        return null;
    }

    //---------------------------------------------------------------------------
    /**
     * Return the set of attributes and references of the specified class, 
     * and if specified its subtypes.
     *  
     * @param classDef class to get feature of.  
     * @param includeSubtypes if true, in addition returns the features
     *         of the subtypes of class.
     * @param includeDerived if false, only non-derived attributes are returned.
     *         if true, derived and non-derived attributes are returned.
     * @return Map map of features of class, its supertypes and subtypes. The
     *          map contains an entry of the form (featureName, featureDef).
     */
    public Map<String,ModelElement_1_0> getAttributeDefs(
        ModelElement_1_0 classDef,
        boolean includeSubtypes,
        boolean includeDerived
    ) throws ServiceException {
        return getStructuralFeatureDefs(
            classDef, 
            includeSubtypes, 
            includeDerived, 
            true // attributesOnly
        );
    }

    /**
     * Return the set of attributes and references of the specified class, 
     * and if specified its subtypes.
     *  
     * @param classDef class to get feature of.  
     * @param includeSubtypes if true, in addition returns the features
     *         of the subtypes of class.
     * @param includeDerived if false, only non-derived attributes are returned.
     *         if true, derived and non-derived attributes are returned.
     * @param attributesOnly 
     *         if true return the same result as getAttributeDefs;
     *         if false include references not stored as attributes
     * @return Map map of features of class, its supertypes and subtypes. The
     *          map contains an entry of the form (featureName, featureDef).
     */
    @SuppressWarnings({
        "unchecked", "cast"
    })
    public Map<String,ModelElement_1_0> getStructuralFeatureDefs(
        ModelElement_1_0 classDef,
        boolean includeSubtypes,
        boolean includeDerived,
        boolean attributesOnly
    ) throws ServiceException {
        Map<Boolean,Map<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>>> allStructuralFeatureDefs = (Map)structuralFeatureDefMap.get(classDef.path());
        if(allStructuralFeatureDefs == null) {
            structuralFeatureDefMap.put(
                classDef.path(),
                allStructuralFeatureDefs = new HashMap<Boolean,Map<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>>>()
            );
        }
        Map<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>> structuralFeatureDefsIncludeSubtypes = (Map)allStructuralFeatureDefs.get(Boolean.valueOf(includeSubtypes));
        if(structuralFeatureDefsIncludeSubtypes == null) {
            allStructuralFeatureDefs.put(
                Boolean.valueOf(includeSubtypes),
                structuralFeatureDefsIncludeSubtypes = new HashMap<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>>()
            );
        }
        Map<Boolean,Map<String,ModelElement_1_0>> structuralFeatureDefsIncludeDerived = (Map<Boolean,Map<String,ModelElement_1_0>>)structuralFeatureDefsIncludeSubtypes.get(Boolean.valueOf(includeDerived));
        if(structuralFeatureDefsIncludeDerived == null) {
            structuralFeatureDefsIncludeSubtypes.put(
                Boolean.valueOf(includeDerived),
                structuralFeatureDefsIncludeDerived = new HashMap<Boolean,Map<String,ModelElement_1_0>>()
            );
        }
        Map<String,ModelElement_1_0> structuralFeatureDefs = (Map<String,ModelElement_1_0>)structuralFeatureDefsIncludeDerived.get(Boolean.valueOf(attributesOnly));
        if(structuralFeatureDefs == null) {
            Map<String,ModelElement_1_0> featureDefs = (Map<String,ModelElement_1_0>)classDef.values(
                includeSubtypes ? "allFeatureWithSubtype" : "allFeature"
            ).get(0);
            structuralFeatureDefs = new HashMap<String,ModelElement_1_0>();
            for(
                Iterator<ModelElement_1_0> i = featureDefs.values().iterator();
                i.hasNext();
            ) {
                ModelElement_1_0 featureDef = i.next();
                if(
                    this.isAttributeType(featureDef) || 
                    (this.isReferenceType(featureDef) && (!attributesOnly || this.referenceIsStoredAsAttribute(featureDef)))          
                ) {
                    Boolean isDerived = (Boolean)featureDef.values("isDerived").get(0);
                    if(includeDerived || (isDerived == null) || !isDerived.booleanValue()) {
                        structuralFeatureDefs.put(
                            (String)featureDef.values("name").get(0),
                            featureDef
                        );
                    }
                }
            }          
            structuralFeatureDefsIncludeDerived.put(
                Boolean.valueOf(attributesOnly),
                structuralFeatureDefs
            );
        }
        return structuralFeatureDefs;
    }

    //-------------------------------------------------------------------------
    private boolean referenceIsStoredAsAttribute(
        java.lang.Object referenceType,
        Map<String,ModelElement_1_0> elements
    ) throws ServiceException {
        ModelElement_1_0 reference = this.getElement(
            referenceType, 
            elements
        );    
        return reference.isReferenceStoredAsAttribute(elements);
    }

    //-------------------------------------------------------------------------
    public boolean referenceIsStoredAsAttribute(
        java.lang.Object referenceType
    ) throws ServiceException {
        return this.referenceIsStoredAsAttribute(
            referenceType,
            Model_1.cache
        );
    }

    //-------------------------------------------------------------------------
    private boolean referenceIsDerived(
        java.lang.Object referenceType,
        Map<String,ModelElement_1_0> elements
    ) throws ServiceException {
        ModelElement_1_0 reference = this.getElement(
            referenceType,
            elements
        );
        ModelElement_1_0 referencedEnd = this.getElement(
            reference.values("referencedEnd").get(0),
            elements
        );
        ModelElement_1_0 association = this.getElement(
            referencedEnd.values("container").get(0),
            elements
        );
        if(association.values("isDerived").size() < 1) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "missing feature isDerived",
                new BasicException.Parameter("association", association)
            );
        }
        return ((Boolean)association.values("isDerived").get(0)).booleanValue();
    }

    //-------------------------------------------------------------------------
    public boolean referenceIsDerived(
        java.lang.Object referenceType
    ) throws ServiceException {
        return this.referenceIsDerived(
            referenceType,
            Model_1.cache
        );
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    private ModelElement_1_0 getDereferencedType(
        java.lang.Object _element,
        Map<String,ModelElement_1_0> elements 
    ) throws ServiceException {
        java.lang.Object element = _element;
        Set<ModelElement_1_0> visitedElements = null;
        Object originalElement = element;
        while(true) {
            ModelElement_1_0 modelElement = this.getElement(element, elements);
            if(modelElement == null) {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.NOT_FOUND, 
                    "element not found in repository. Can not dereference type",
                    new BasicException.Parameter [] {
                        new BasicException.Parameter("element", element)
                    }
                );
            }
            if(modelElement.isAliasType()) {
                if(visitedElements == null) {
                    visitedElements = new HashSet<ModelElement_1_0>();
                }
                if(visitedElements.contains(modelElement)) {
                    throw new ServiceException (
                        ModelExceptions.MODEL_DOMAIN,
                        ModelExceptions.CIRCULAR_ALIAS_TYPE_DEFINITION, 
                        ModelConstraints.CIRCULAR_TYPE_DEPENCENCY_NOT_ALLOWED,
                        new BasicException.Parameter [] {
                            new BasicException.Parameter("element", originalElement)
                        }
                    );
                }
                visitedElements.add(modelElement);
                element = modelElement.values("type").get(0);
                Path elementType = (Path) element;
                if(
                    PrimitiveTypes.XRI_ALIAS.equals(elementType.getBase())
                ) {
                    element = elementType.getParent().add(PrimitiveTypes.XRI);
                }
            }
            else {
                return modelElement;
            }
        }   
    }

    //-------------------------------------------------------------------------
    public ModelElement_1_0 getDereferencedType(
        java.lang.Object element
    ) throws ServiceException {
        return this.getDereferencedType(
            element,
            Model_1.cache 
        );
    }

    //-------------------------------------------------------------------------
    public ModelElement_1_0 getElementType(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        // Cache dereferenced type as dereferencedType for performance reasons
        if(elementDef.attributeNames().contains("dereferencedType")) {
            return (ModelElement_1_0)elementDef.values("dereferencedType").get(0);
        }
        else {
            ModelElement_1_0 type = this.getDereferencedType(
                elementDef.values("type").get(0)
            );
            elementDef.values("dereferencedType").add(type);
            return type;
        }        
    }
    
    //-------------------------------------------------------------------------
    public ModelElement_1_0 getReferenceType(
        Path path
    ) throws ServiceException {
        AssociationDef[] assocDefs = this.getAssociationDefs(path);
        return assocDefs[1].getReference();
    }

    //-------------------------------------------------------------------------
    protected boolean isPrimitiveType(
        java.lang.Object type,
        Map<String,ModelElement_1_0> elements
    ) throws ServiceException {   
        return this.getDereferencedType(type, elements).isPrimitiveType();
    }

    //-------------------------------------------------------------------------
    public boolean isPrimitiveType(
        java.lang.Object type
    ) throws ServiceException {   
        return this.isPrimitiveType(
            type,
            Model_1.cache
        );
    }

    //-------------------------------------------------------------------------
    private boolean isNumericType(
        java.lang.Object type,
        Map<String,ModelElement_1_0> elements
    ) throws ServiceException {   
        ModelElement_1_0 typeDef = this.getDereferencedType(type, elements);
        if(typeDef.isPrimitiveType()) {
            String typeName = (String)typeDef.values("qualifiedName").get(0);
            return 
            PrimitiveTypes.DECIMAL.equals(typeName) ||
            PrimitiveTypes.INTEGER.equals(typeName) ||
            PrimitiveTypes.LONG.equals(typeName) ||
            PrimitiveTypes.SHORT.equals(typeName);
        }
        return false;
    }

    //-------------------------------------------------------------------------
    public boolean isNumericType(
        java.lang.Object type
    ) throws ServiceException {
        return this.isNumericType(
            type,
            Model_1.cache
        );
    }

    //-------------------------------------------------------------------------
    public boolean isStructureType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isStructureType();
    }

    //-------------------------------------------------------------------------
    public boolean isStructureFieldType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isStructureFieldType();
    }

    //---------------------------------------------------------------------------
    public void verifyObjectCollection(
        Object values,
        Object type,
        String multiplicity,
        boolean includeRequired
    ) throws ServiceException {
        Stack<List<?>> validationContext = new Stack<List<?>>();
        validationContext.push(
            Arrays.asList("values", values)
        );
        this.verifyObjectCollection(
            values,
            type,
            multiplicity,
            includeRequired,
            validationContext, 
            true, // attributesOnly
            true // verifyDerived
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Verifies an object
     * 
     * @param object to be verified
     * @param deepVerify When <code>deepVerify</code> is <code>true</code>, 
     * the refVerifyConstraints method carries out a shallowVerify on that
     * object and a deep verify through its containment hierarchy.
     * @param verifyDerived
     * tells whether derived features should be verified as well
     * 
     * @return the null value if no constraint is violated; 
     * otherwise, a list of <code>ServiceException</code> objects 
     * (each representing a constraint violation) is returned.
     */
    public Collection<?> verifyObject(
        Object_1_0 object,
        boolean deepVerify, 
        boolean verifyDerived
    ){
        try {
            this.verifyObject(
                object,
                object.objGetClass(),
                null, // multiplicity
                true, // enforceRequired
                !deepVerify, // attributesOnly, 
                verifyDerived
            );
            return null;
        } catch (ServiceException exception) {
            return Collections.singleton(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.VALIDATION_FAILURE,
                    "Object validation failed",
                    new BasicException.Parameter("elementInError", getElementInError(object)),
                    new BasicException.Parameter("objectInError")
                )
            );
        }
    }

    //-------------------------------------------------------------------------
    private Object getElementInError (
        Object_1_0 object
    ){
        try {
            return object.objGetPath();
        } catch (ServiceException exception) {
            return object.objGetResourceIdentifier();
        }
    }

    //-------------------------------------------------------------------------
    public void verifyObject(
        Object value,
        Object type,
        String multiplicity,
        boolean enforceRequired
    ) throws ServiceException {
        this.verifyObject(
            value,
            type,
            multiplicity,
            enforceRequired,
            true // attributesOnly
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Verifies a single the value to be of the specified type. 
     * The multiplicity is required to validate Stereotypes.STREAM types only.
     * The values must be of well-known spice types which are:
     * <ul>
     *   <li>Structure_1_0</li>
     *   <li>Object_1_0</li>
     *   <li>DataproviderObject_1_0</li>
     *   <li>Primitive types</li>
     * </ul>
     * The verification is done recursively. In case of a violation
     * an exception is thrown containing the violation.
     * 
     * @param type if type == null then no verification is performed.
     * 
     * @param enforceRequired if true, all required feature of value 
     *         are verified. Otherwise verifies only the available features.
     * @param attributesOnly 
     *         if true return the same result as 
     *         verifyObject(Object,Object,String,boolean);
     *         if false allow references not stored as attributes
     * 
     * @param removeDerived removes all derived features.
     */
    public void verifyObject(
        Object value,
        Object type,
        String multiplicity,
        boolean enforceRequired, 
        boolean attributesOnly
    ) throws ServiceException {
        this.verifyObject(
            value,
            type,
            multiplicity,
            enforceRequired,
            attributesOnly, 
            true // verifyDerived
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Verifies a single the value to be of the specified type. 
     * The multiplicity is required to validate Stereotypes.STREAM types only.
     * The values must be of well-known spice types which are:
     * <ul>
     *   <li>Structure_1_0</li>
     *   <li>Object_1_0</li>
     *   <li>DataproviderObject_1_0</li>
     *   <li>Primitive types</li>
     * </ul>
     * The verification is done recursively. In case of a violation
     * an exception is thrown containing the violation.
     * 
     * @param type if type == null then no verification is performed.
     * 
     * @param enforceRequired if true, all required feature of value 
     *         are verified. Otherwise verifies only the available features.
     * @param attributesOnly 
     *         if true return the same result as 
     *         verifyObject(Object,Object,String,boolean);
     *         if false allow references not stored as attributes
     * @param verifyDerived
     *        tells whether derived features should be verified as well
     * 
     * @param removeDerived removes all derived features.
     */
    public void verifyObject(
        Object value,
        Object type,
        String multiplicity,
        boolean enforceRequired, 
        boolean attributesOnly,
        boolean verifyDerived
    ) throws ServiceException {
        Stack<List<?>> validationContext = new Stack<List<?>>();
        validationContext.push(
            Arrays.asList("object", value)
        );
        this.verifyObject(
            value,
            type,
            multiplicity,
            enforceRequired,
            validationContext, 
            attributesOnly, 
            verifyDerived
        );
    }

    //-------------------------------------------------------------------------
    public boolean isClassType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isClassType();
    }

    //-------------------------------------------------------------------------
    public boolean isStructuralFeatureType(
        java.lang.Object type
    ) throws ServiceException {
        ModelElement_1_0 typeDef = this.getDereferencedType(type);
        return typeDef.isReferenceType() || typeDef.isAttributeType();
    }

    //-------------------------------------------------------------------------
    public boolean isReferenceType(
        java.lang.Object type,
        Map<String,ModelElement_1_0> elements
    ) throws ServiceException {
        return this.getDereferencedType(type, elements).isReferenceType();
    }

    //-------------------------------------------------------------------------
    public boolean isReferenceType(
        java.lang.Object type
    ) throws ServiceException {
        return this.isReferenceType(
            type,
            Model_1.cache
        );
    }

    //-------------------------------------------------------------------------
    public boolean isAttributeType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isAttributeType();
    }

    //-------------------------------------------------------------------------
    public boolean isOperationType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isOperationType();
    }

    //-------------------------------------------------------------------------
    public boolean isPackageType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isPackageType();
    }

    //-------------------------------------------------------------------------
    public boolean isRoled(
        ModelElement_1_0 classDef
    ) throws ServiceException {
        ModelElement_1_0 superClass = null;
        boolean isRole = false;
        for(
            Iterator<?> i = classDef.getValues("allSupertype").iterator();
            i.hasNext() && !isRole;
        ) {
            superClass = this.getDereferencedType(i.next());
            isRole = superClass.values("stereotype").contains(Stereotypes.ROLE);
        }
        return isRole;
    }  

    //-------------------------------------------------------------------------
    public boolean isSubtypeOf(
        DataproviderObject object,
        Object type
    ) throws ServiceException {
        String typeName = (String)this.getElement(type).values("qualifiedName").get(0);
        for(
            Iterator<?> i = this.getElement(object.values(SystemAttributes.OBJECT_CLASS).get(0)).values("allSupertype").iterator();
            i.hasNext();
        ) {
            if(typeName.equals(((Path)i.next()).getBase())) {
                return true;
            }
        }
        return false;
    }

    //-------------------------------------------------------------------------
    public boolean isInstanceof(
        Object_1_0 object,
        Object type
    ) throws ServiceException {
        return this.isSubtypeOf(
            object.objGetClass(),
            type
        );
    }

    //-------------------------------------------------------------------------
    public boolean isSubtypeOf(
        Object objectType,
        Object type
    ) throws ServiceException {
        String typeName = (String)this.getElement(type).values("qualifiedName").get(0);
        for(
            Iterator<?> i = this.getElement(objectType).values("allSupertype").iterator();
            i.hasNext();
        ) {
            if(typeName.equals(((Path)i.next()).getBase())) {
                return true;
            }
        }
        return false;
    }

    //---------------------------------------------------------------------------  
    public String toJavaPackageName(
        Object type,
        String packageSuffix
    ) throws ServiceException {
        return this.toJavaPackageName(
            type,
            packageSuffix,
            true
        );
    }

    //---------------------------------------------------------------------------  
    public String toJavaPackageName(
        Object type,
        String packageSuffix,
        boolean dereferenceType
    ) throws ServiceException {
        ModelElement_1_0 element = dereferenceType ? 
            this.getDereferencedType(type) : 
            this.getElement(type);
        return toJavaPackageName( 
            element.path().get(4),
            packageSuffix
        );
    }

    //---------------------------------------------------------------------------  
    public String toJavaPackageName(
        String qualifiedPackageName,
        String packageSuffix
    ) throws ServiceException {
        List<String> packageNameComponents = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(qualifiedPackageName, ":");
        while(tokenizer.hasMoreTokens()) {
            packageNameComponents.add(tokenizer.nextToken());
        }
        //javaPackageName
        StringBuffer javaPackageName = new StringBuffer();
        boolean openmdx1 = packageSuffix.length() != 4;
        for(
            int i = 0, iLimit = packageNameComponents.size(); 
            i < iLimit; 
            i++
        )  {
            StringBuffer target = i == 0 ? javaPackageName : javaPackageName.append('.');
            String source = packageNameComponents.get(i);
            if(openmdx1) {
                Names.openmdx1NamespaceElement(target, source);
            } else {
                AbstractNames.openmdx2NamespaceElement(target, source);
            }
        }
        return javaPackageName.append(
            '.'
        ).append(
            packageSuffix
        ).toString();
    }

    //---------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.Model_1_3#getCompositeReference(org.openmdx.model1.accessor.basic.cci.ModelElement_1_0)
     */
    public ModelElement_1_0 getCompositeReference(
        ModelElement_1_0 classDef
    ) throws ServiceException {
        for(ModelElement_1_0 elementDef : this.getContent()) {
            if(this.isReferenceType(elementDef)) {
                ModelElement_1_0 exposedEnd = this.getElement(elementDef.getValues("referencedEnd").get(0));
                Path type = (Path)exposedEnd.values("type").get(0);
                if(
                    AggregationKind.COMPOSITE.equals(exposedEnd.values("aggregation").get(0)) &&
                    this.isSubtypeOf(classDef, type)
                ) {
                    return elementDef;
                }
            }
        }
        return null;
    }

    //---------------------------------------------------------------------------
    public ModelElement_1_0[] getTypes(
        Path path
    ) throws ServiceException {
        AssociationDef[] assocDefs = this.getAssociationDefs(path);
        return new ModelElement_1_0[]{
            assocDefs[0].getReferencedType(), 
            assocDefs[1].getExposedType(), 
            assocDefs[1].getReferencedType()
        };
    }


    //------------------------------------------------------------------------
    // Implements Model_1_5
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.Model_1_5#isAssociationType(java.lang.Object)
     */
    public boolean isAssociationType(
        Object type
    ) throws ServiceException {
        ModelElement_1_1 typeDef = (ModelElement_1_1) this.getDereferencedType(type);
        return typeDef.isAssociationType();
    }


    //------------------------------------------------------------------------
    // Implements Model_1_6
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.Model_1_6#getLeastDerived(java.lang.String)
     */
    public String getLeastDerived(
        String qualifiedClassName
    ) throws ServiceException {
        Types: for(
            ModelElement_1_0 type = this.getElement(qualifiedClassName);
            true;
        ){
            for(Object s : type.values("supertype")) {
                ModelElement_1_0 superType = getElement(s);
                if(!superType.values("stereotype").contains(Stereotypes.ROOT)) {
                    type = superType;
                    continue Types;
                }
            }
            return (String) type.values("qualifiedName").get(0);
        }
    }

    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final String AUTHORITY_TYPE_NAME = "org:openmdx:base:Authority";
    static private final Path PROVIDER_ROOT_PATH = new Path("xri:@openmdx:org.omg.model1/provider/Mof");

    // One repository per class loader

    // Flag indicating whether repository is initialized
    private static boolean isSetup = false;
    // Set of loaded models
    private static Set<String> loadedModels = new HashSet<String>();
    // Repository containing loaded models
    private static Dataprovider_1_0 repository = null;
    // Channel to access repository
    private static RequestCollection channel = null;
    // Caches for associations, structural features and all model elements 
    private static volatile Map<String,List<AssociationDef>> associationDefMap = new HashMap<String,List<AssociationDef>>();
    private static volatile Map<Path,Map<Boolean,Map<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>>>> structuralFeatureDefMap = new HashMap<Path,Map<Boolean,Map<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>>>>();
    private static volatile Map<String,ModelElement_1_0> cache = new HashMap<String,ModelElement_1_0>();

    /**
     * <em>Sorted</em> array of deprecated model package names.
     * 
     * @see Model_1#TO_MODEL_PACKAGE_NAMES
     */
    private static String[] FROM_MODEL_PACKAGE_NAMES = {
        "org:oasis_open",
        "org:omg:primitiveTypes"   
    };

    /**
     * Array of canonical model package names, each entry corresponding to a 
     * given deprecated model package name.
     * 
     * @see Model_1#FROM_MODEL_PACKAGE_NAMES
     */
    private static String[] TO_MODEL_PACKAGE_NAMES = {
        "org:oasis-open",
        "org:omg:PrimitiveTypes"
    };

}

//--- End of File -----------------------------------------------------------
