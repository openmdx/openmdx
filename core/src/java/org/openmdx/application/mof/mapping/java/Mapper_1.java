/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Mapper
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 * 
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.application.mof.mapping.java;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.omg.mof.cci.VisibilityKind;
import org.omg.mof.spi.AbstractNames;
import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.application.mof.mapping.cci.AttributeDef;
import org.openmdx.application.mof.mapping.cci.ClassDef;
import org.openmdx.application.mof.mapping.cci.ClassifierDef;
import org.openmdx.application.mof.mapping.cci.Mapper_1_1;
import org.openmdx.application.mof.mapping.cci.MappingTypes;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.OperationDef;
import org.openmdx.application.mof.mapping.cci.ReferenceDef;
import org.openmdx.application.mof.mapping.cci.StructDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.java.metadata.ClassMetaData;
import org.openmdx.application.mof.mapping.java.metadata.FieldMetaData;
import org.openmdx.application.mof.mapping.spi.AbstractMapper_1;
import org.openmdx.application.mof.repository.accessor.ModelElement_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.repository.cci.ReferenceRecord;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;

/**
 * Standard Mapper
 */
public class Mapper_1 extends AbstractMapper_1 implements Mapper_1_1 {

    /**
     * Constructor.
     * 
     * @param mappingFormat
     *            mapping format defined MapperFactory_1.
     * @param packageSuffix
     *            The suffix for the package to be generated in (without leading
     *            dot), e.g. 'cci'.
     * @param fileExtension
     *            The file extension (without leading point), e.g. 'java'.
     */
    public Mapper_1(
        String mappingFormat,
        String packageSuffix,
        String fileExtension
    ) throws ServiceException {
        super(packageSuffix);
        this.fileExtension = fileExtension;
        if (mappingFormat.startsWith(MappingTypes.JPA3 + ':')) {
            this.format = Format.JPA3;
        } else {
            this.format =
                MappingTypes.CCI2.equals(mappingFormat) ? Format.CCI2 :
                MappingTypes.JMI1.equals(mappingFormat) ? Format.JMI1 :
                MappingTypes.JPA3.equals(mappingFormat) ? Format.JPA3 :
                null;
        }
        this.primitiveTypeMapper = newPrimitiveTypeMapper();
    }

    // ---------------------------------------------------------------------------
    /**
     * Is called for all ModelAttribute features of a class including supertypes.
     * This method must check whether modelAttribute.container = modelClass and
     * behave accordingly.
     * 
     * @param jdoMetaDataMapper
     * @param jdoSliceMetaDataMapper
     * @param ormMetaDataMapper
     * @param ormSliceMetaDataMapper
     */
    void mapAttribute(
        ModelElement_1_0 classDef,
        ModelElement_1_0 attributeDef,
        QueryMapper queryMapper,
        InstanceMapper instanceMapper,
        AbstractMetaDataMapper ormMetaDataMapper,
        AbstractMetaDataMapper ormSliceMetaDataMapper)
        throws ServiceException {

        SysLog.trace("attribute", attributeDef.jdoGetObjectId());
        Multiplicity multiplicity = ModelHelper.toMultiplicity(attributeDef.getMultiplicity());
        boolean isDerived =
            attributeDef.isDerived().booleanValue();
        // required for ...Class.create...() operations
        this.processedAttributes.add(attributeDef);
        try {
            if ((this.format == Format.JPA3)
                || VisibilityKind.PUBLIC_VIS.equals(attributeDef
                    .objGetValue("visibility"))) {
                AttributeDef mAttributeDef =
                    new AttributeDef(attributeDef, this.model);
                ClassDef mClassDef =
                    new ClassDef(classDef, this.model, this.metaData);
                // setter/getter interface only if modelAttribute.container =
                // modelClass.
                // Otherwise inherit from super interfaces.
                if (((this.format == Format.JPA3) && (((ClassMetaData) mClassDef
                    .getClassMetaData()).getBaseClass() == null))
                    || attributeDef.getContainer().equals(
                        classDef.jdoGetObjectId())) {
                    // query interface
                    if (queryMapper != null) {
                        queryMapper.mapStructuralFeature(
                            mClassDef,
                            mAttributeDef);
                    }
                    // Note:
                    // Set operations in interfaces are generated only if the
                    // attribute
                    // is changeable and is not derived.

                    if(multiplicity == null) {
                    	//
                        // set/get interface with lower and upper bound, e.g. 0..n
                    	//
                        if (instanceMapper != null) {
                            instanceMapper.mapAttributeGetList(mAttributeDef);
                        }
	                    if (this.format == Format.CCI2) {
	                    	ormMetaDataMapper.mapAttribute(mAttributeDef);
	                    } else if (this.format == Format.JPA3) {
                            ormSliceMetaDataMapper.mapAttribute(mAttributeDef);
                            ormMetaDataMapper.mapSize(mAttributeDef);
                        }
                    } else {
                    	switch(multiplicity) {
	                    	case OPTIONAL: {
	                    		//
	                            // set/get interface 0..1
	                    		//
	                            if (instanceMapper != null) {
	                                instanceMapper.mapAttributeGet0_1(mAttributeDef);
	                            }	
	                            if (this.format == Format.JPA3 || this.format == Format.CCI2) {
	                                ormMetaDataMapper.mapAttribute(mAttributeDef);
	                            }
	                            if (!isDerived) {
	                                if (instanceMapper != null) {
	                                    instanceMapper
	                                        .mapAttributeSet0_1(mAttributeDef);
	                                }
	                            }
	                    	} break;
	                    	case SINGLE_VALUE: {
	                    		//
	                            // set/get interface 1..1
	                    		//
	                            if (instanceMapper != null) {
	                                instanceMapper.mapAttributeGet1_1(mAttributeDef);
	                            }
	                            if (this.format == Format.JPA3  || this.format == Format.CCI2) {
	                                ormMetaDataMapper.mapAttribute(mAttributeDef);
	                            }
	                            if (!isDerived) {
	                                if (instanceMapper != null) {
	                                    instanceMapper
	                                        .mapAttributeSet1_1(mAttributeDef);
	                                }
	                            }
	                    	} break;
	                    	case LIST: {
	                    		//
	                            // set/get interface list
	                    		//
	                            if (instanceMapper != null) {
	                                instanceMapper.mapAttributeGetList(mAttributeDef);
	                            }
	                            if (this.format == Format.JPA3 || this.format == Format.CCI2) {
	                                FieldMetaData fieldMetaData = instanceMapper.getFieldMetaData(
	                                	mAttributeDef.getQualifiedName()
	                                );
	                                if(this.format == Format.CCI2) {
	                                	ormMetaDataMapper.mapAttribute(mAttributeDef);
	                                } else { 
	                                    Integer embedded = fieldMetaData == null ? null : fieldMetaData.getEmbedded();
	                                	if (embedded == null) {
	    	                                ormSliceMetaDataMapper.mapAttribute(mAttributeDef);
	    	                                ormMetaDataMapper.mapSize(mAttributeDef);
	    	                            } else {
	    	                                ormMetaDataMapper.mapEmbedded(
	    	                                    mAttributeDef,
	    	                                    fieldMetaData
	    	                                );
	    	                            }
	                                }
	                            }
	                            if (!isDerived) {
	                                if (instanceMapper != null) {
	                                    instanceMapper
	                                        .mapAttributeSetList(mAttributeDef);
	                                }
	                            }
	                    	} break;
	                    	case SET: {
	                    		//
	                            // set/get interface set
	                    		//
	                            if (instanceMapper != null) {
	                                instanceMapper.mapAttributeGetSet(mAttributeDef);
	                            }
	                            if (this.format == Format.JPA3 || this.format == Format.CCI2) {
	                                FieldMetaData fieldMetaData = instanceMapper.getFieldMetaData(
	                                	mAttributeDef.getQualifiedName()
	                                );
	                                if(this.format == Format.CCI2) {
	                                	ormMetaDataMapper.mapAttribute(mAttributeDef);
	                                } else { 
	                                    Integer embedded = fieldMetaData == null ? null : fieldMetaData.getEmbedded();
	    	                            if (embedded == null) {
	    	                                ormSliceMetaDataMapper.mapAttribute(mAttributeDef);
	    	                                ormMetaDataMapper.mapSize(mAttributeDef);
	    	                            } else {
	    	                                ormMetaDataMapper.mapEmbedded(
	    	                                    mAttributeDef,
	    	                                    fieldMetaData
	    	                                );
	    	                            }
	                                }
	                            }
	                            if (!isDerived) {
	                                if (instanceMapper != null) {
	                                    instanceMapper.mapAttributeSetSet(mAttributeDef);
	                                }
	                            }
	                    	} break;
	                    	case SPARSEARRAY: {
	                    		//
	                            // set/get interface sparsearray
	                    		//
	                            if (instanceMapper != null) {
	                                instanceMapper.mapAttributeGetSparseArray(mAttributeDef);
	                            }
	                            if (this.format == Format.CCI2) {
	                            	ormMetaDataMapper.mapAttribute(mAttributeDef);
	                            } else if (this.format == Format.JPA3) {
	                                ormSliceMetaDataMapper.mapAttribute(mAttributeDef);
	                                ormMetaDataMapper.mapSize(mAttributeDef);
	                            }
	                            if (!isDerived) {
	                                if (instanceMapper != null) {
	                                    instanceMapper.mapAttributeSetSparseArray(mAttributeDef);
	                                }
	                            }
	                    	} break;
	                    	case MAP: {
	                    		//
	                            // set/get interface map
	                    		//
	                            if (instanceMapper != null) {
	                                instanceMapper.mapAttributeGetMap(mAttributeDef);
	                            }
	                    	} break;
	                    	case STREAM: {
	                    		//
	                            // set/get interface stream
	                    		//
	                            if (instanceMapper != null) {
	                                instanceMapper.mapAttributeGetStream(mAttributeDef);
	                            }
	                            if (!isDerived) {
	                                if (instanceMapper != null) {
	                                    instanceMapper.mapAttributeSetStream(mAttributeDef);
	                                }
	                            }
	                    	} break;
                    	}
                    }
                }
            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    // ---------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    void mapReference(
        ModelElement_1_0 classDef,
        ModelElement_1_0 referenceDef,
        QueryMapper queryMapper,
        InstanceMapper instanceMapper,
        AbstractMetaDataMapper ormMetaDataMapper,
        AbstractMetaDataMapper ormSliceMetaDataMapper,
        boolean inherited)
        throws ServiceException {
        SysLog.trace("reference", referenceDef.jdoGetObjectId());
        ModelElement_1_0 referencedEnd =
            this.model.getElement(referenceDef.getReferencedEnd());
        ModelElement_1_0 association =
            this.model.getElement(referencedEnd.getContainer());
        ClassDef mClassDef = new ClassDef(classDef, this.model, this.metaData);
        SysLog.trace("referencedEnd", referencedEnd);
        SysLog.trace("association", association);
        // setter/getter interface only if modelAttribute.container == modelClass.
        // Otherwise inherit from super interfaces.
        boolean includeInClass =
            ((this.format == Format.JPA3) && (((ClassMetaData) mClassDef
                .getClassMetaData()).getBaseClass() == null))
                || referenceDef.getContainer().equals(
                    classDef.jdoGetObjectId());
        Multiplicity multiplicity = ModelHelper.toMultiplicity(referenceDef.getMultiplicity());
        String visibility = (String) referenceDef.objGetValue("visibility");
        List<?> qualifierNames = referencedEnd.objGetList("qualifierName");
        List<?> qualifierTypes = referencedEnd.objGetList("qualifierType");
        boolean isChangeable =
            referenceDef.isChangeable().booleanValue();
        boolean isDerived =
            association.isDerived().booleanValue();
        boolean includeExtensions = this.format == Format.JMI1 && !inherited;
        // Check whether this reference is stored as attribute
        // required for ...Class.create...() operations
        if (this.model.referenceIsStoredAsAttribute(referenceDef)) {
            SysLog.trace("the reference is stored as attribute", referenceDef.getQualifiedName());
            ModelElement_1_0 referenceAsAttribute = new ModelElement_1(referenceDef);
            this.processedAttributes.add(referenceAsAttribute);
            if (!qualifierNames.isEmpty()) {
                SysLog.trace(qualifierNames.toString(), qualifierTypes);
                String newMultiplicity =
                    qualifierTypes.size() == 1
                        && PrimitiveTypes.STRING.equals(((Path) qualifierTypes
						.get(0)).getLastSegment().toClassicRepresentation()) ? Multiplicity.MAP.code()
                        : ModelHelper.UNBOUND;
                SysLog.trace("Adjust multiplicity to "
                    + ModelHelper.UNBOUND, newMultiplicity);
                // 0..n association set multiplicity to 0..n, 
                // this ensures that the instance creator uses a multivalued parameter for this reference attribute
                ReferenceRecord referenceAsAttributeRecord = (ReferenceRecord) referenceAsAttribute.getDelegate();
                referenceAsAttributeRecord.put(ReferenceRecord.Member.multiplicity, newMultiplicity);
            }
        }
        try {
            if (VisibilityKind.PUBLIC_VIS.equals(visibility)) {
                ReferenceDef mReferenceDef =
                    new ReferenceDef(
                        referenceDef, 
                        this.model
                    );
                /**
                 * References are read-only if they are not changeable. In
                 * addition they are also read-only if the reference is derived
                 * and stored as attribute.
                 */
                boolean isReadOnly = !isChangeable || (isDerived && this.model.referenceIsStoredAsAttribute(referenceDef));
                if(includeInClass && (queryMapper != null) && !inherited) {
                    queryMapper.mapStructuralFeature(
                        mClassDef,
                        mReferenceDef
                    );
                }
                if(this.model.referenceIsStoredAsAttribute(referenceDef)) {
                    if(includeInClass && (this.format == Format.JPA3)) {
                        if (qualifierNames.isEmpty()) {
                        	if(multiplicity != null) {
                        		switch(multiplicity){
	                        		case OPTIONAL: case SINGLE_VALUE: {
	                                    ormMetaDataMapper.mapReference(mReferenceDef);
	                        		} break;
	                        		case LIST: case SET: case SPARSEARRAY: {
	                                    ormSliceMetaDataMapper.mapReference(mReferenceDef);
	                                    ormMetaDataMapper.mapSize(mReferenceDef);
	                        		} break;
                                    case MAP: case STREAM:
                                        // TODO not mapped yet
                                        break;
	                        			
                        		}
                        	}
                        } else {
                            ormSliceMetaDataMapper.mapReference(mReferenceDef);
                            ormMetaDataMapper.mapSize(mReferenceDef);
                        }
                    }
                }
                // no qualifier, multiplicity must be [0..1|1..1|0..n]
                if (qualifierNames.isEmpty()) {
                    // 0..1
                    if (Multiplicity.OPTIONAL == multiplicity) {
                        if (includeInClass && !inherited) {
                            if (instanceMapper != null) {
                                // get
                                instanceMapper.mapReferenceGetx_1NoQualifier(
                                    mReferenceDef,
                                    true, // optional
                                    true // referencedEnd
                                    );
                                if (!isReadOnly) {
                                    // set
                                    instanceMapper.mapReferenceSetNoQualifier(
                                        mReferenceDef,
                                        true, // optional 
                                        true // referencedEnd
                                    );
                                    if (includeExtensions) {
                                        instanceMapper
                                            .mapReferenceRemoveOptional(mReferenceDef);
                                    }
                                }
                            }
                        }
                    }
                    // 1..1
                    else if (Multiplicity.SINGLE_VALUE == multiplicity) {
                        if (includeInClass && !inherited) {
                            if (instanceMapper != null) {
                                // get
                                instanceMapper.mapReferenceGetx_1NoQualifier(
                                    mReferenceDef,
                                    false, // optional
                                    true // referencedEnd
                                    );
                                if (!isReadOnly) {
                                    // set
                                    instanceMapper.mapReferenceSetNoQualifier(
                                        mReferenceDef,
                                        false, // optional 
                                        true // referencedEnd
                                    );
                                }
                            }
                        }
                    }
                    // 0..n
                    else {
                        if (includeInClass) {
                            if (instanceMapper != null) {
                                instanceMapper.mapReferenceGet0_nNoQuery(
                                    mReferenceDef,
                                    inherited);
                            }
                        }
                    }
                }
                // 0..n association where qualifier qualifies 1..1, 0..1, 0..n
                else {
                    if ((Multiplicity.OPTIONAL == multiplicity | Multiplicity.SINGLE_VALUE == multiplicity)) {
                        if (includeInClass && instanceMapper != null) {
                            if (this.model.referenceIsStoredAsAttribute(referenceDef)) {
                                instanceMapper.mapReferenceGet0_nNoQuery(
                                    mReferenceDef,
                                    inherited);
                            } else {
                                instanceMapper
                                    .mapReferenceGet0_nWithQuery(mReferenceDef);
                                if ((Multiplicity.OPTIONAL == multiplicity)) {
                                    instanceMapper
                                        .mapReferenceGet0_1WithQualifier(mReferenceDef);
                                } else {
                                    instanceMapper
                                        .mapReferenceGet1_1WithQualifier(mReferenceDef);
                                }
                                if (this.format == Format.JPA3) {
                                    ormMetaDataMapper
                                        .mapReference(mReferenceDef);
                                }
                            }
                        }
                    } else {
                        //
                        // !qualifiesUniquely. qualifier is required
                        //
                        if (!this.model
                            .referenceIsStoredAsAttribute(referenceDef)) {
                            if (includeInClass) {
                                if (instanceMapper != null) {
                                    instanceMapper
                                        .mapReferenceGet0_nWithQualifier(
                                            mReferenceDef,
                                            inherited);
                                }
                                if (this.format == Format.JPA3) {
                                    // TODO: feature not yet implemented for JPA3.
                                    // Do not generate meta data 
//                                  ormMetaDataMapper.mapReference(mReferenceDef);
                                }
                            }
                        }
                        /**
                         * It is NOT possible to have this situation. If a
                         * qualifier does not qualify uniquely, then it must be
                         * a non-primitive type. (If the qualifier type is
                         * primitive, then the multiplicity must be 0..1 or
                         * 1..1, i.e. the qualifier qualifies uniquely [openMDX
                         * Constraint].) Non-primitive, ambiguous qualifiers are
                         * used to indicate the owner of the associated element.
                         * The owner is only used if the reference is NOT stored
                         * as attribute (otherwise you would know your
                         * associated elements).
                         */
                        else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "reference with non-primitive, ambiguous qualifier cannot be stored as attribute",
                                new BasicException.Parameter("reference", referenceDef.jdoGetObjectId()),
                                new BasicException.Parameter("qualifier",qualifierNames.get(0))
                             );
                        }
                    }
                    // add with qualifier
                    if (multiplicity != null && multiplicity.isSingleValued() && this.model.isPrimitiveType(qualifierTypes.get(0))) {
                        if (!isReadOnly) {
                            if (includeInClass) {
                                if (instanceMapper != null) {
                                    instanceMapper.mapReferenceAddWithQualifier(mReferenceDef);
                                }
                            }
                        }
                    }
                    if (includeExtensions) {
                        // add without qualifier
                        if (!isReadOnly) {
                            if (includeInClass) {
                                if (instanceMapper != null) {
                                    instanceMapper.mapReferenceAddWithoutQualifier(mReferenceDef);
                                }
                            }
                        }
                        // remove with qualifier if qualifier qualifies uniquely
                        if ((Multiplicity.OPTIONAL == multiplicity | Multiplicity.SINGLE_VALUE == multiplicity)) {

                            if (!isReadOnly) {
                                if (includeInClass) {
                                    if (instanceMapper != null) {
                                        instanceMapper.mapReferenceRemoveWithQualifier(mReferenceDef);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    // ---------------------------------------------------------------------------
    void mapOperation(
        ModelElement_1_0 classDef,
        ModelElement_1_0 operationDef,
        InstanceMapper instanceMapper)
        throws ServiceException {
        SysLog.trace("operation", operationDef.jdoGetObjectId());
        boolean includeInClass =
            this.format == Format.JPA3
                || operationDef.getContainer().equals(
                    classDef.jdoGetObjectId());
        if (VisibilityKind.PUBLIC_VIS.equals(operationDef
            .objGetValue("visibility"))) {
            try {
                OperationDef mOperationDef =
                    new OperationDef(operationDef, this.model);
                if (includeInClass) {
                    instanceMapper.mapOperation(mOperationDef);
                }
            } catch (Exception ex) {
                throw new ServiceException(ex).log();
            }
        }
    }

    // ---------------------------------------------------------------------------
    void mapException(
        ModelElement_1_0 exceptionDef,
        ExceptionMapper exceptionMapper)
        throws ServiceException {

        SysLog.trace("exception", exceptionDef.jdoGetObjectId());
        if (VisibilityKind.PUBLIC_VIS.equals(exceptionDef
            .objGetValue("visibility"))) {
            try {
                exceptionMapper.mapException();
            } catch (Exception ex) {
                throw new ServiceException(ex).log();
            }
        }
    }

    // ---------------------------------------------------------------------------
    boolean mapAssociation(
        ModelElement_1_0 associationDef,
        AssociationMapper associationMapper)
        throws ServiceException {

        SysLog.trace("association", associationDef.jdoGetObjectId());
        if (VisibilityKind.PUBLIC_VIS.equals(associationDef
            .objGetValue("visibility")))
            try {
                return associationMapper.mapAssociation();
            } catch (Exception exception) {
                throw new ServiceException(exception).log();
            }
        else {
            return false;
        }
    }

    // ---------------------------------------------------------------------------
    void mapBeginClass(
        ModelElement_1_0 classDef,
        ClassMapper classMapper,
        InstanceMapper instanceMapper,
        InterfaceMapper interfaceMapper,
        AbstractMetaDataMapper ormMetaDataMapper,
        AbstractMetaDataMapper ormSliceMetaDataMapper)
        throws ServiceException {

        SysLog.trace("class", classDef.jdoGetObjectId());
        boolean isAbstract =
            classDef.isAbstract().booleanValue();
        try {
            if (this.format == Format.JMI1 && !isAbstract) {
                classMapper.mapBegin();
            }
            instanceMapper.mapBegin();
            if (this.format == Format.JPA3) {
                if (interfaceMapper != null) {
                    interfaceMapper.mapBegin();
                }
                ormMetaDataMapper.mapBegin(instanceMapper.isSliceHolder());
                ormSliceMetaDataMapper.mapBegin(instanceMapper.isSliceHolder());
            }
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
        this.processedAttributes = new ArrayList<ModelElement_1_0>();
    }

    // ---------------------------------------------------------------------------
    void mapObjectCreator(
        ModelElement_1_0 classDef,
        ModelElement_1_0 supertypeDef,
        ClassMapper classMapper)
        throws ServiceException {

        SysLog.trace("class", classDef.jdoGetObjectId());

        // traverse all processed attributes (this includes all regular
        // attributes and
        // those references that are stored as attributes) to find out which
        // attributes are mandatory
        List<AttributeDef> allAttributes = new ArrayList<AttributeDef>();
        List<AttributeDef> requiredAttributes = new ArrayList<AttributeDef>();
        for (Iterator<ModelElement_1_0> i = this.processedAttributes.iterator(); i
            .hasNext();) {
            ModelElement_1_0 attributeDef = i.next();
            // attribute member of class, non-derived and public
            if (((supertypeDef == null) || !supertypeDef
                .objGetList("feature")
                .contains(attributeDef.jdoGetObjectId()))
                && !Boolean.TRUE.equals(attributeDef.isDerived())
                && VisibilityKind.PUBLIC_VIS.equals(attributeDef
                    .objGetValue("visibility"))) {
                AttributeDef att = new AttributeDef(attributeDef, this.model);
                allAttributes.add(att);
                // required attribute
                String multiplicity =
                    attributeDef.getMultiplicity();
                SysLog.trace(attributeDef
                    .getQualifiedName()
                    .toString(), multiplicity);
                if (Multiplicity.SINGLE_VALUE.code().equals(attributeDef
                    .objGetValue("multiplicity"))) {
                    requiredAttributes.add(att);
                } else {
                    /* isAllAttributesRequired = false; */
                }
            }
        }

        try {
            // creators
            if (supertypeDef == null) {

                // check whether all attributes are mandatory and whether there
                // are some
                // mandatory attributes at all; if so, do not generate creator
                // (otherwise we get a duplicate creator definition)
                if (/* !isAllAttributesRequired && */requiredAttributes.size() > 0) {
                    classMapper
                        .mapInstanceCreatorRequiredAttributes(requiredAttributes);
                }
            }
            // extenders
            else {
                ClassDef mSuperclassDef =
                    new ClassDef(supertypeDef, this.model, this.metaData);
                // check whether all attributes are mandatory and whether there
                // are some
                // mandatory attributes at all; if so, do not generate creator
                // (otherwise we get a duplicate creator definition)
                if (/* !isAllAttributesRequired && */requiredAttributes.size() > 0) {
                    classMapper.mapInstanceExtenderRequiredAttributes(
                        mSuperclassDef,
                        requiredAttributes);
                }
            }
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    // ---------------------------------------------------------------------------
    void mapEndClass(
        ModelElement_1_0 classDef,
        ClassMapper classMapper,
        InstanceMapper instanceMapper,
        InterfaceMapper interfaceMapper,
        AbstractMetaDataMapper ormMetaDataMapper,
        AbstractMetaDataMapper ormSliceMetaDataMapper)
        throws ServiceException {

        if (this.format == Format.JMI1) {
            // add additional template references to context
            List<StructuralFeatureDef> structuralFeatures =
                new ArrayList<StructuralFeatureDef>();
            List<OperationDef> operations = new ArrayList<OperationDef>();
            for (Iterator<?> i = classDef.objGetList("feature").iterator(); i
                .hasNext();) {
                ModelElement_1_0 feature = this.model.getElement(i.next());
                if (VisibilityKind.PUBLIC_VIS.equals(feature
                    .objGetValue("visibility"))) {
                    if (feature.isAttributeType()) {
                        structuralFeatures.add(new AttributeDef(
                            feature,
                            this.model));
                    } else if (feature.isReferenceType()) {
                        ModelElement_1_0 referencedEnd = this.model.getElement(feature.getReferencedEnd());
                        List<?> qualifierTypes =
                            referencedEnd.objGetList("qualifierType");
                        // skip references for which a qualifier exists and the
                        // qualifier is
                        // not a primitive type
                        if (qualifierTypes.isEmpty() || this.model.isPrimitiveType(qualifierTypes.get(0))) {
                            structuralFeatures.add(
                                new ReferenceDef(
                                    feature,
                                    this.model
                                )
                            );
                        }
                    } else if (feature.isOperationType()) {
                        operations.add(new OperationDef(feature, this.model));
                    }
                }
            }
        }
        boolean classIsAbstract =
            classDef.isAbstract().booleanValue();
        try {
            if (this.format == Format.JMI1 && !classIsAbstract) {
                // standard creators
                mapObjectCreator(classDef, null, classMapper);
                // narrow creators
                SysLog.trace("creators for", classDef.jdoGetObjectId());
                SysLog.trace("supertypes", classDef.objGetList("allSupertype"));
                for (Iterator<?> i =
                    classDef.objGetList("allSupertype").iterator(); i.hasNext();) {
                    ModelElement_1_0 supertype =
                        this.model.getDereferencedType(i.next());
                    if (!supertype.jdoGetObjectId().equals(
                        classDef.jdoGetObjectId())) {
                        SysLog.trace("creating", supertype.jdoGetObjectId());
                        this.mapObjectCreator(classDef, supertype, classMapper);
                    } else {
                        SysLog.trace("skipping", supertype.jdoGetObjectId());
                    }
                }
                classMapper.mapEnd();
            }
            // impl for non-abstract and abstract classes (JMI plugins)
            instanceMapper.mapEnd();
            if (this.format == Format.JPA3) {
                if (interfaceMapper != null) {
                    interfaceMapper.mapEnd();
                }
                boolean process =
                    instanceMapper.isSliceHolder()
                        || instanceMapper.hasSlices();
                ormSliceMetaDataMapper.setProcess(process);
                ormMetaDataMapper.mapEnd(ormSliceMetaDataMapper);
            }
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    // ---------------------------------------------------------------------------
    void mapBeginQuery(ClassifierDef mClassifierDef, QueryMapper queryMapper)
        throws ServiceException {
        if (queryMapper != null) try {
            queryMapper.mapBegin(mClassifierDef);
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    // ---------------------------------------------------------------------------
    void mapEndQuery(QueryMapper queryMapper)
        throws ServiceException {
        if (queryMapper != null) try {
            queryMapper.mapEnd();
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    // ---------------------------------------------------------------------------
    void mapBeginStructure(
        ModelElement_1_0 structDef,
        StructureMapper structureMapper)
        throws ServiceException {
        SysLog.trace("struct", structDef.jdoGetObjectId());
        try {
            structureMapper.mapBegin();
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
        this.processedAttributes = new ArrayList<ModelElement_1_0>();
    }

    // --------------------------------------------------------------------------------
    /**
     * Is called for all StructureField features of a class including supertypes.
     * This method must check whether modelAttribute.container = modelClass and
     * behave accordingly.
     */
    void mapStructureField(
        ModelElement_1_0 classDef,
        ModelElement_1_0 structureFieldDef,
        QueryMapper queryMapper,
        StructureMapper structureMapper,
        boolean jmi)
        throws ServiceException {

        SysLog.trace("structure field", structureFieldDef.jdoGetObjectId());
        Multiplicity multiplicity = ModelHelper.toMultiplicity(
            structureFieldDef.getMultiplicity()
        );

        // required for ...Class.create...() operations
        this.processedAttributes.add(structureFieldDef);
        try {
            AttributeDef mStructureFieldDef = new AttributeDef(structureFieldDef, this.model);
            StructDef mStructDef = new StructDef(classDef, this.model);
            // getter interface only if modelAttribute.container = modelClass.
            // Otherwise inherit from super interfaces.
            if (this.format == Format.JPA3
                || structureFieldDef.getContainer().equals(
                    classDef.jdoGetObjectId())) {
                // query interface
                if (queryMapper != null) {
                    queryMapper.mapStructuralFeature(
                        mStructDef,
                        mStructureFieldDef);
                }
                if(multiplicity == null) {
                    // get interface with lower and upper bound, e.g. 0..n
                    structureMapper.mapFieldGetList(mStructureFieldDef);
                } else {
                	switch(multiplicity) {
	                	case OPTIONAL:
	                        // get interface 0..1
	                        structureMapper.mapFieldGet0_1(mStructureFieldDef);
	                        break;
	                	case SINGLE_VALUE:
	                        // get interface 1..1
	                        structureMapper.mapFieldGet1_1(mStructureFieldDef);
	                        break;
	                	case LIST:
	                        // get interface list
	                        structureMapper.mapFieldGetList(mStructureFieldDef);
	                        break;
	                	case SET:
	                        // get interface set
	                        structureMapper.mapFieldGetSet(mStructureFieldDef);
	                        break;
	                	case SPARSEARRAY:
	                        // get interface sparsearray
	                        structureMapper.mapFieldGetSparseArray(mStructureFieldDef);
	                        break;
	                	case STREAM:
	                        // get interface stream
	                        structureMapper.mapFieldGetStream(mStructureFieldDef);
	                        break;
	                	case MAP: 
	                		// TODO verify whether this branch is correct	                		
	                        structureMapper.mapFieldGetList(mStructureFieldDef);
	                        break;
                	}
                }
            }
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    // ---------------------------------------------------------------------------
    void mapEndStructure(StructureMapper structureMapper)
        throws ServiceException {
        try {
            structureMapper.mapEnd();
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    // --------------------------------------------------------------------------------
    void mapBeginPackage(String forPackage, PackageMapper packageMapper)
        throws ServiceException {
        try {
            packageMapper.mapBegin(forPackage);
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    // --------------------------------------------------------------------------------
    void mapEndPackage(String forPackage, PackageMapper packageMapper)
        throws ServiceException {
        try {
            packageMapper.mapEnd();
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    // --------------------------------------------------------------------------------
    void mapObjectMarshaller(
        ModelElement_1_0 classDef,
        PackageMapper packageMapper)
        throws ServiceException {
        try {
            ClassDef mClassDef =
                new ClassDef(classDef, this.model, this.metaData);
            packageMapper.mapClassAccessor(mClassDef);
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }

    }

    // --------------------------------------------------------------------------------
    private void mapStructureCreator(
        ModelElement_1_0 structDef,
        PackageMapper packageMapper)
        throws ServiceException {
        try {
            StructDef mStructDef = new StructDef(
                structDef, 
                this.model
            );
            packageMapper.mapStructCreator(mStructDef);
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    // --------------------------------------------------------------------------------
    // Generates create query operations for a struct or a class in the current
    // package
    private void mapQueryCreator(
        ClassifierDef mClassifierDef,
        PackageMapper packageMapper)
        throws ServiceException {
        try {
            packageMapper.mapQueryCreator(mClassifierDef);
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    // ---------------------------------------------------------------------------
    protected Model_1_0 getModel() {
        return super.model;
    }

    // ---------------------------------------------------------------------------
    /**
     * Externalizes given packageContent and stores result in the jar output
     * stream.
     * 
     * @param qualifiedPackageName
     *            fully qualified, ':' separated name of a model package. '%' as
     *            last character is allowed as wildcard, e.g. 'org:%' exports
     *            all models contained in package 'org', 'org:openmdx:%' exports
     *            all models contained in package 'org:openmdx'. All models are
     *            written to os.
     * @param model
     * @param os
     * 
     * @throws ServiceException
     */
    public void externalize(
        String qualifiedPackageName,
        Model_1_0 model,
        ZipOutputStream os
    ) throws ServiceException {
        externalize(
            qualifiedPackageName, 
            model, 
            os, 
            null // openmdxjdoMetadataDirectory
        );
    }

    /**
     * Externalizes given packageContent and stores result in the jar output
     * stream.
     * 
     * @param qualifiedPackageName
     *            fully qualified, ':' separated name of a model package. '%' as
     *            last character is allowed as wildcard, e.g. 'org:%' exports
     *            all models contained in package 'org', 'org:openmdx:%' exports
     *            all models contained in package 'org:openmdx'. All models are
     *            written to os.
     * @param model
     * @param zip
     * @param openmdxjdoMetadataDirectory
     *            base directory for .openmdxjdo meta data files
     * 
     * @throws ServiceException
     */
    public void externalize(
        String qualifiedPackageName,
        Model_1_0 model,
        ZipOutputStream zip,
        String openmdxjdoMetadataDirectory
    ) throws ServiceException {

        SysLog.trace("Exporting", qualifiedPackageName);
        super.model = model;
        this.metaData = new MetaData_2(openmdxjdoMetadataDirectory);
        List<ModelElement_1_0> packagesToExport =
            this.getMatchingPackages(qualifiedPackageName);
        final boolean jmi1 = this.format == Format.JMI1;
        final boolean cci2 = this.format == Format.CCI2;
        final boolean jpa3 = this.format == Format.JPA3;

        try {
            // allocate streams one time
            ByteArrayOutputStream pkgFile =
                jpa3 ? null : new ByteArrayOutputStream();
            ByteArrayOutputStream classFile =
                jmi1 ? new ByteArrayOutputStream() : null;
            ByteArrayOutputStream instanceFile = new ByteArrayOutputStream();
            ByteArrayOutputStream interfaceFile = new ByteArrayOutputStream();
            ByteArrayOutputStream sliceInstanceFile =
                jpa3 ? new ByteArrayOutputStream() : null;
            ByteArrayOutputStream ormMetaDataFile =
                jpa3 | cci2 ? new ByteArrayOutputStream() : null;
            ByteArrayOutputStream structFile =
                jpa3 ? null : new ByteArrayOutputStream();
            ByteArrayOutputStream queryFile =
                cci2 ? new ByteArrayOutputStream() : null;
            ByteArrayOutputStream exceptionFile =
                jpa3 ? null : new ByteArrayOutputStream();
            ByteArrayOutputStream associationFile =
                cci2 ? new ByteArrayOutputStream() : null;

            Writer packageWriter =
                jpa3 ? null : new OutputStreamWriter(pkgFile);
            Writer classWriter =
                jmi1 ? new OutputStreamWriter(classFile) : null;
            Writer queryWriter =
                cci2 ? new OutputStreamWriter(queryFile) : null;
            Writer instanceWriter = new OutputStreamWriter(instanceFile);
            Writer interfaceWriter = new OutputStreamWriter(interfaceFile);
            Writer sliceInstanceWriter =
                jpa3 ? new OutputStreamWriter(sliceInstanceFile) : null;
            Writer ormMetaDataWriter =
                jpa3 | cci2 ? new OutputStreamWriter(ormMetaDataFile) : null;
            CharArrayWriter ormSliceMetaDataWriter =
                jpa3 ? new CharArrayWriter() : null;
            Writer structWriter =
                jpa3 ? null : new OutputStreamWriter(structFile);
            // Export matching packages
            PrintWriter ormMetaDataPrintWriter = null;
            if (jpa3 | cci2) {
                ormMetaDataPrintWriter = new PrintWriter(ormMetaDataWriter);
                if(jpa3) {
	                StandardMetaDataMapper.fileHeader(ormMetaDataPrintWriter);
                } else {
                    NativeMetaDataMapper.fileHeader(ormMetaDataPrintWriter);
                }
                ormMetaDataPrintWriter.flush();
            }
            for (Iterator<ModelElement_1_0> pkgs = packagesToExport.iterator(); pkgs
                .hasNext();) {
                ModelElement_1_0 currentPackage = pkgs.next();
                String currentPackageName =
                    currentPackage.getQualifiedName();
                if (!this.excludePackage(currentPackageName)) {
                    SysLog.detail("Processing package", currentPackageName);
                    PackageMapper packageMapper = null;
                    if (jmi1) {
                        pkgFile.reset();
                        packageMapper =
                            new PackageMapper(
                                packageWriter,
                                getModel(),
                                this.format,
                                this.packageSuffix,
                                this.metaData, 
                                getPrimitiveTypeMapper());
                        // initialize package
                        this.mapBeginPackage(currentPackageName, packageMapper);
                    }
                    // Process package content
                    for (Iterator<ModelElement_1_0> i =
                        getModel().getContent().iterator(); i.hasNext();) {
                        ModelElement_1_0 element = i.next();
                        if (this
                            .getModel()
                            .isLocal(element, currentPackageName)) {
                            // Only generate elements which are content of the
                            // model package.
                            // Do not generate for imported model elements
                            SysLog.trace("processing package element", element
                                .jdoGetObjectId());
                            // org:omg:model1:Class
                            if (this.getModel().isClassType(element)) {
                                ClassDef classDef =
                                    new ClassDef(
                                        element,
                                        this.model,
                                        this.metaData);
                                if (this.includeClass(classDef)
                                    && !this.excludeClass(classDef)) {
                                    if ((this.format != Format.JPA3)
                                        || !classDef.isMixIn()
                                        || (((ClassMetaData) classDef
                                            .getClassMetaData()).getTable() != null)) {
                                        SysLog.detail(
                                            "Processing class",
                                            classDef.getQualifiedName());
                                        if (jmi1) {
                                            if (!classDef.isAbstract()) {
                                                this.mapObjectMarshaller(
                                                    element,
                                                    packageMapper);
                                            }
                                            this.mapQueryCreator(
                                                classDef,
                                                packageMapper);
                                            classFile.reset();
                                        }
                                        if (cci2) {
                                            queryFile.reset();
                                        }
                                        instanceFile.reset();
                                        if (jpa3) {
                                            sliceInstanceFile.reset();
                                            ormSliceMetaDataWriter.reset();
                                        }
                                        ClassMapper classMapper = jmi1 ? new ClassMapper(
                                            element,
                                            classWriter,
                                            getModel(),
                                            this.format,
                                            this.packageSuffix,
                                            this.metaData, 
                                            getPrimitiveTypeMapper()
                                         ) : null;
                                        InstanceMapper instanceMapper = new InstanceMapper(
                                            element,
                                            instanceWriter,
                                            sliceInstanceWriter,
                                            getModel(),
                                            this.format,
                                            this.packageSuffix,
                                            this.metaData, 
                                            getPrimitiveTypeMapper()
                                        );
                                        InterfaceMapper interfaceMapper = jpa3 && instanceMapper.hasSPI() ? new InterfaceMapper(
                                            element,
                                            interfaceWriter,
                                            getModel(),
                                            Format.SPI2,
                                            Names.SPI2_PACKAGE_SUFFIX,
                                            this.metaData, 
                                            getPrimitiveTypeMapper()
                                         ) : null;
                                        AbstractMetaDataMapper ormMetaDataMapper = jpa3 ? new StandardMetaDataMapper(
                                            element,
                                            ormMetaDataWriter,
                                            getModel(),
                                            this.format,
                                            this.packageSuffix,
                                            null,
                                            this.metaData,
                                            getPrimitiveTypeMapper(), 
                                            geObjectRepositoryMetadataPlugin()
                                         ) : cci2 ? new NativeMetaDataMapper(
                                                 element,
                                                 ormMetaDataWriter,
                                                 getModel(),
                                                 this.format,
                                                 this.packageSuffix,
                                                 null,
                                                 this.metaData,
                                                 getPrimitiveTypeMapper(), 
                                                 geObjectRepositoryMetadataPlugin()
                                          ) : null;
                                        AbstractMetaDataMapper ormSliceMetaDataMapper = jpa3 ? new StandardMetaDataMapper(
                                            element,
                                            ormSliceMetaDataWriter,
                                            getModel(),
                                            this.format,
                                            this.packageSuffix,
                                            InstanceMapper.SLICE_CLASS_NAME,
                                            this.metaData,
                                            getPrimitiveTypeMapper(), 
                                            geObjectRepositoryMetadataPlugin()
                                         ) : null;
                                        QueryMapper queryMapper = cci2 ? new QueryMapper(
                                                queryWriter,
                                                getModel(),
                                                this.format,
                                                this.packageSuffix,
                                                this.metaData, 
                                                getPrimitiveTypeMapper()
                                        ) : null;
                                        this.mapBeginClass(
                                            element,
                                            classMapper,
                                            instanceMapper,
                                            interfaceMapper,
                                            ormMetaDataMapper,
                                            ormSliceMetaDataMapper
                                        );
                                        this.mapBeginQuery(
                                            classDef,
                                            queryMapper
                                        );
                                        // Map class features
                                        for (Object f : getFeatures(element, instanceMapper, false)) {
                                            ModelElement_1_0 feature = f instanceof ModelElement_1_0 ? 
                                                (ModelElement_1_0) f
                                                    : getModel().getElement(f);
                                            SysLog.trace("processing class feature", feature.jdoGetObjectId());
                                            if (feature.isAttributeType()) {
                                                this.mapAttribute(
                                                    element,
                                                    feature,
                                                    queryMapper,
                                                    instanceMapper,
                                                    ormMetaDataMapper,
                                                    ormSliceMetaDataMapper);
                                            } else if (feature.isReferenceType()) {
                                                this.mapReference(
                                                    element,
                                                    feature,
                                                    queryMapper,
                                                    instanceMapper,
                                                    ormMetaDataMapper,
                                                    ormSliceMetaDataMapper,
                                                    false // inherited
                                                    );
                                            } else if (feature.isOperationType()) {
                                                this.mapOperation(
                                                    element,
                                                    feature,
                                                    instanceMapper);
                                            }
                                        }
                                        this.mapEndQuery(queryMapper);
                                        this.mapEndClass(
                                            element,
                                            classMapper,
                                            instanceMapper,
                                            interfaceMapper,
                                            ormMetaDataMapper,
                                            ormSliceMetaDataMapper);
                                        instanceWriter.flush();
                                        String elementName =
                                            instanceMapper.getClassName();
                                        this.addToZip(
                                            zip,
                                            instanceFile,
                                            element,
                                            elementName,
                                            "." + this.fileExtension);
                                        if (jpa3) {
                                            if (interfaceMapper != null) {
                                                interfaceWriter.flush();
                                                this.addToZip(
                                                    zip,
                                                    interfaceFile,
                                                    element,
                                                    elementName,
                                                    "." + this.fileExtension,
                                                    true,
                                                    Names.SPI2_PACKAGE_SUFFIX);
                                            }
                                            sliceInstanceWriter.flush();
                                            this.addToZip(
                                                zip,
                                                sliceInstanceFile,
                                                element,
                                                elementName,
                                                InstanceMapper.SLICE_CLASS_NAME
                                                    + "." + this.fileExtension,
                                                true,
                                                Names.JPA3_PACKAGE_SUFFIX);
                                        }
                                        if (cci2) {
                                            queryWriter.flush();
                                            this.addToZip(
                                                zip,
                                                queryFile,
                                                element,
                                                elementName,
                                                "Query." + this.fileExtension);
                                        }
                                        if (jmi1 && !classDef.isAbstract()) {
                                            classWriter.flush();
                                            this.addToZip(
                                                zip,
                                                classFile,
                                                element,
                                                elementName,
                                                "Class." + this.fileExtension);
                                        }
                                    }
                                }
                            }
                            // org:omg:model1:StructureType
                            else if (getModel().isStructureType(element)) {
                                SysLog.trace(
                                    "processing structure type",
                                    element.jdoGetObjectId()
                                );
                                StructDef mStructDef =
                                    new StructDef(
                                        element, 
                                        getModel()
                                    );
                                if (jmi1) {
                                    this.mapStructureCreator(
                                        element,
                                        packageMapper
                                     );
                                }
                                if (structFile != null) {
                                    structFile.reset();
                                }
                                if (cci2) queryFile.reset();
                                StructureMapper structureMapper = jpa3 ? null : new StructureMapper(
                                        element,
                                        structWriter,
                                        getModel(),
                                        this.format,
                                        this.packageSuffix,
                                        this.metaData, 
                                        getPrimitiveTypeMapper()
                                );
                                QueryMapper queryMapper = cci2 ? new QueryMapper(
                                    queryWriter,
                                    getModel(),
                                    this.format,
                                    this.packageSuffix,
                                    this.metaData, 
                                    getPrimitiveTypeMapper()
                                ) : null;
                                if (structureMapper != null) {
                                    this.mapBeginStructure(
                                        element,
                                        structureMapper);
                                }
                                this.mapBeginQuery(mStructDef, queryMapper);
                                // StructureFields
                                for (
                                	Iterator<?> j = element.objGetList("content").iterator(); 
                                	j.hasNext();
                                ) {
                                    ModelElement_1_0 feature = getModel().getElement(j.next());
                                    SysLog.trace(
                                        "processing structure field",
                                        feature.jdoGetObjectId());
                                    if (structureMapper != null && feature.isStructureFieldType()) {
                                        this.mapStructureField(
                                            element,
                                            feature,
                                            queryMapper,
                                            structureMapper,
                                            jmi1);
                                    }
                                }
                                this.mapEndQuery(queryMapper);
                                String elementName = Identifier.CLASS_PROXY_NAME.toIdentifier(element.getName());
                                if (structureMapper != null) {
                                    this.mapEndStructure(structureMapper);
                                    structWriter.flush();
                                    this.addToZip(
                                        zip,
                                        structFile,
                                        element,
                                        elementName,
                                        "." + this.fileExtension);
                                }
                                if (cci2) {
                                    queryWriter.flush();
                                    this.addToZip(
                                        zip,
                                        queryFile,
                                        element,
                                        elementName,
                                        "Query." + this.fileExtension);
                                }
                            }
                            // org:omg:model1:Exception
                            else if (element.isExceptionType()) {
                                SysLog.trace("processing exception", element
                                    .jdoGetObjectId());
                                if (!jpa3) {
                                    exceptionFile.reset();
                                    Writer exceptionWriter = new OutputStreamWriter(exceptionFile);
                                    ExceptionMapper exceptionMapper = new ExceptionMapper(
                                        element,
                                        exceptionWriter,
                                        getModel(),
                                        this.format,
                                        packageSuffix,
                                        this.metaData, 
                                        getPrimitiveTypeMapper()
                                    );
                                    this.mapException(element, exceptionMapper);
                                    exceptionWriter.flush();
                                    String elementName = Identifier.CLASS_PROXY_NAME.toIdentifier(
                                        element.getName(), 
                                        null, // removablePrefix
                                        null, // prependablePrefix
                                        "exception", // removableSuffix
                                        "exception" // appendableSuffix
                                    );
                                    this.addToZip(
                                        zip,
                                        exceptionFile,
                                        element,
                                        elementName,
                                        "." + this.fileExtension
                                     );
                                }
                            }
                            // org:omg:model1:AssociationType
                            else if (getModel().isAssociationType(element)) {
                                SysLog.trace("processing association", element
                                    .jdoGetObjectId());
                                if (cci2) {
                                    associationFile.reset();
                                    Writer associationWriter = new OutputStreamWriter(associationFile);
                                    AssociationMapper associationMapper = new AssociationMapper(
                                        element,
                                        associationWriter,
                                        getModel(),
                                        this.format,
                                        this.packageSuffix,
                                        this.metaData, 
                                        getPrimitiveTypeMapper()
                                    );
                                    if (mapAssociation(
                                        element,
                                        associationMapper)) {
                                        associationWriter.flush();
                                        this.addToZip(
                                            zip,
                                            associationFile,
                                            element,
                                            associationMapper.associationName,
                                            "." + this.fileExtension
                                        );
                                    }
                                }
                            } else {
                                SysLog.trace("Ignoring element", element
                                    .jdoGetObjectId());
                            }
                        } else {
                            SysLog.trace(
                                "Skipping non-package element",
                                element.jdoGetObjectId());
                        }
                    }
                    if (jmi1) {
                        // flush package
                        this.mapEndPackage(currentPackageName, packageMapper);
                        packageWriter.flush();
                        this
                            .addToZip(
                                zip,
                                pkgFile,
                                currentPackage,
                                AbstractNames
                                    .openmdx2PackageName(
                                        new StringBuffer(),
                                        currentPackage.getName()
                                    ).toString(),
                                '.' + this.fileExtension);
                    }
                }
            }
            if (jpa3 | cci2) {
            	if(jpa3) {
	                StandardMetaDataMapper.fileFooter(ormMetaDataPrintWriter);
            	} else {
	                NativeMetaDataMapper.fileFooter(ormMetaDataPrintWriter);
            	}
                ormMetaDataPrintWriter.flush();
                ormMetaDataWriter.flush();
                ZipEntry zipEntry = new JarEntry(jpa3 ? "META-INF/orm.xml" : "META-INF/openmdxorm.properties");
                zipEntry.setSize(ormMetaDataFile.size());
                zip.putNextEntry(zipEntry);
                ormMetaDataFile.writeTo(zip);
            }
        } catch (Exception ex) {
            throw new ServiceException(ex).log();
        }
        SysLog.trace("done");
    }

    /**
     * 
     * @param classDef
     * @param instanceMapper
     * @param inherited
     * @return
     */
    private Collection<?> getFeatures(
        ModelElement_1_0 classDef,
        InstanceMapper instanceMapper,
        boolean inherited)
        throws ServiceException {
        return inherited || format == Format.JPA3 ? instanceMapper.getFeatures(
            inherited).values() : classDef.objGetList("feature");
    }

    /**
     * Test whether the given model is in an archive
     * 
     * @param packageName
     *            the qualified model package name
     * 
     * @return <code>true</code> if model is in an archive
     */
    private boolean excludePackage(String packageName) {
        if (EXCLUDED_PACKAGES.contains(packageName)) {
            return true;
        } else {
            String[] components = packageName.split(":");
            StringBuilder resource = new StringBuilder();
            for (int i = 0; i < components.length; i++) {
                resource
                    .append(
                        i == 0 ? "" : i == components.length - 1 ? "/xmi1/"
                            : "/")
                    .append(components[i]);

            }
            return artifactIsInArchive(resource.append(".xml"));
        }
    }

    /**
     * Test whether the given JDO metadata is in an archive
     * 
     * @param className
     * 
     * @return <code>true</code> if model is in an archive
     */
    private boolean excludeClass(ClassDef classDef) {
        String className = classDef.getQualifiedName();
        if (EXCLUDED_CLASSES.contains(className)) {
            return true;
        } else {
            String[] components = className.split(":");
            StringBuilder resource = new StringBuilder();
            for (int i = 0; i < components.length; i++) {
                resource
                    .append(
                        i == 0 ? "" : i == components.length - 1 ? "/jdo2/"
                            : "/")
                    .append(components[i]);

            }
            return artifactIsInArchive(resource.append(".jdo"));
        }
    }

    private boolean includeClass(ClassDef classDef) {
        return (this.format != Format.JPA3) || !classDef.isAbstract()
            || classDef.getSuperClassDef(true) == null;
    }
    
    protected PrimitiveTypeMapper newPrimitiveTypeMapper(){
        return new StandardPrimitiveTypeMapper();
    }

    protected PrimitiveTypeMapper getPrimitiveTypeMapper(){
        return this.primitiveTypeMapper;
    }
    
    protected ObjectRepositoryMetadataPlugin geObjectRepositoryMetadataPlugin(){
        return new StandardObjectRepositoryMetadataPlugin();
    }
    
    /**
     * Test whether the given artifact is in an archive
     * 
     * @param name
     * 
     * @return <code>true</code> if model is in an archive
     */
    private static boolean artifactIsInArchive(CharSequence iri) {
        String uri = iri.toString();
        URL url = Resources.getResource(uri);
        SysLog.detail(uri, url);
        return url != null && "jar".equals(url.getProtocol());
    }

    // --------------------------------------------------------------------------------
    // Members
    // --------------------------------------------------------------------------------
    /**
     * Packages not to be processed
     */
    private final static Collection<String> EXCLUDED_PACKAGES =
        Arrays.asList("org:omg:PrimitiveTypes:PrimitiveTypes" // due to a
                                                              // conflict
                                                              // between jmi and
                                                              // jmi1
            );

    private final static Collection<String> EXCLUDED_CLASSES = Arrays.asList();

    private MetaData_1_0 metaData;

    private final String fileExtension;

    private List<ModelElement_1_0> processedAttributes = null;

    private final Format format;

    private final PrimitiveTypeMapper primitiveTypeMapper;
    
}

// --- End of File -----------------------------------------------------------
