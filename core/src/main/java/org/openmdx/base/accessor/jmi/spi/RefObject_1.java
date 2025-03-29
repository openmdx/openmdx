/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: RefObject_1 class
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.accessor.jmi.spi;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Level;

import javax.jdo.JDOException;
import javax.jdo.JDOUserException;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.InteractionSpec;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.InteractionSpec;
#endif

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.accessor.spi.DateMarshaller;
import org.openmdx.base.accessor.spi.DateTimeMarshaller;
import org.openmdx.base.accessor.spi.DurationMarshaller;
import org.openmdx.base.accessor.spi.IntegerMarshaller;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingMap;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.Persistency;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.format.DateTimeFormat;
#if CLASSIC_CHRONO_TYPES import org.w3c.format.DateTimeFormat;#endif

//---------------------------------------------------------------------------
/**
 * Implementation of RefObject_1_0.
 * <p>
 * This implementation supports lightweight serialization. The only member is a
 * handle to the class object.
 */
@SuppressWarnings({"rawtypes","unchecked"})
class RefObject_1
    implements Jmi1Object_1_0, Serializable, PersistenceCapable, org.openmdx.base.persistence.spi.Cloneable<RefObject>
{

    /**
     * Constructor
     *
     * @param object
     * @param refClass
     */
    public RefObject_1(
        ObjectView_1_0 object,
        RefClass refClass
    ) {
        try {
            this.object = object;
            RefRootPackage_1 refPackage = (RefRootPackage_1) refClass.refOutermostPackage();
            InteractionSpec objContext = object.getInteractionSpec();
            Object refContext = refPackage.refInteractionSpec();
            if(
                InteractionSpecs.NULL == objContext || (
                    objContext == null ? refContext == null : objContext.equals(refContext)
                )
            ){
                this.refClass = refClass;
            } else {
                this.refClass = refPackage.refPackage(
                    objContext
                ).refClass(
                    refClass.refMofId()
                );
            }
        } catch(RuntimeException exception) {
            throw new JmiServiceException(exception);
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Implements {@code java.io.Serializable}
     */
    private static final long serialVersionUID = -276854474114899063L;

    private static final Collection<String> EXCEPTION_WRAPPERS = Arrays.asList(
        "org.openmdx.kernel.exception.BasicException",
        "javax.resource.ResourceException"
    );

    private transient RefObject metaObject = null;
    private transient ModelElement_1_0 refClassDef = null;

    private static final byte[] EMPTY_LARGE_OBJECT = new byte[] {};
    private static final Path ROOT_PATH = new Path(new String[] {});

    /**
     * @serial
     */
    private ObjectView_1_0 object;

    /**
     * @serial
     */
    private RefClass refClass;

    /**
     * Accessing a mandatory but uninitialized value leads to a
     * {@code NullPointerException} in case of the following types:
     */
    private static final List<String> NULL_AVERSE_TYPES = Arrays.asList(
    	PrimitiveTypes.BOOLEAN,
    	PrimitiveTypes.SHORT,
    	PrimitiveTypes.INTEGER,
    	PrimitiveTypes.LONG
    );

    // -------------------------------------------------------------------------
    private void assertStructuralFeature(
            ModelElement_1_0 elementDef
    ) throws ServiceException {
        if (!this.object.getModel().isStructuralFeatureType(elementDef)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE,
                new BasicException.Parameter("model element", elementDef)
            );
        }
    }

    // -------------------------------------------------------------------------
    private boolean isAttributeOrReferenceStoredAsAttribute(
            ModelElement_1_0 elementDef
    ) throws ServiceException {
        Model_1_0 model = this.object.getModel();
        return
        !model.isOperationType(elementDef) &&
        (model.isAttributeType(elementDef) ||
            model.referenceIsStoredAsAttribute(elementDef));
    }

    // -------------------------------------------------------------------------
    private void assertOperation(
            ModelElement_1_0 elementDef
    ) throws ServiceException {
        if (!this.object.getModel().isOperationType(elementDef)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.OPERATION,
                new BasicException.Parameter("model element", elementDef)
            );
        }
    }

    // -------------------------------------------------------------------------
    private ModelElement_1_0 getType(
            ModelElement_1_0 elementDef
    ) throws ServiceException {
        return this.object.getModel().getElementType(
            elementDef
        );
    }

    // -------------------------------------------------------------------------
    private ModelElement_1_0 getFeature(
            String featureName
    ) throws ServiceException {
        Model_1_0 model = this.object.getModel();

        // Fully qualified feature name. Lookup in model
        if (featureName.indexOf(':') >= 0) {
            return model.getElement(featureName);
        }
        // Get all features of class and find feature with featureName
        else {
            if(this.refClassDef == null) {
                this.refClassDef = model.getElement(this.refClass.refMofId());
            }
            ModelElement_1_0 feature = model.getFeatureDef(
                this.refClassDef,
                featureName,
                false
            );
            if(feature == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "feature not found",
                    new BasicException.Parameter("class name", this.refClass().refMofId()),
                    new BasicException.Parameter("feature", featureName)
                );
            }
            return feature;
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Remove the optional "Container" suffix from the qualifier name
     *
     * @param qualifierName
     *
     * @return the qualifier name without "Container" suffix
     */
    private String removeContainerSuffix(
        String qualifierName
    ){
        return qualifierName.endsWith("Container") || qualifierName.endsWith("container") ?
            qualifierName.substring(0, qualifierName.length() - "Container".length()) :
            qualifierName;
    }

    private Object getValue(
            ModelElement_1_0 featureDef,
            Object qualifier
    ) throws ServiceException {

        Model_1_0 model = featureDef.getModel();
        boolean isReference = model.isReferenceType(featureDef);
        boolean isAttribute = model.isAttributeType(featureDef);
        boolean isReferenceStoredAsAttribute = isReference && model.referenceIsStoredAsAttribute(featureDef);

        if (!isAttribute && !isReference) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE,
                new BasicException.Parameter("model element", featureDef)
            );
        }

        ModelElement_1_0 type = this.getType(featureDef);
        String qualifiedTypeName = type.getQualifiedName();
        String featureName = featureDef.getName();

        /**
         * Attribute or Reference stored as attribute. Don't care about
         * qualifier which can anyway only an index. The caller is responsible
         * to get the required element from the collection.
         */
        if (isAttribute || isReferenceStoredAsAttribute) {
            if (qualifier != null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "qualifier must be null in case of attributes and references stored as attribute",
                    new BasicException.Parameter("feature", featureDef),
                    new BasicException.Parameter("qualifier", qualifier)
                );
            }
            Multiplicity multiplicity = ModelHelper.getMultiplicity(featureDef);
            Marshaller marshaller = this.refOutermostPackage().getMarshaller(qualifiedTypeName);
            switch(multiplicity) {
                case SINGLE_VALUE: {
					final Object mandatoryValue = this.object.objGetValue(featureName);
					if(mandatoryValue == null && NULL_AVERSE_TYPES.contains(qualifiedTypeName)){
						throw Throwables.initCause(
							new NullPointerException(
								"The primitive type's value is not yet set and returning null is not possible"
							),
							null, // cause
							BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.ILLEGAL_STATE,
							new BasicException.Parameter("interaction-spec", this.object.getInteractionSpec()),
							new BasicException.Parameter(BasicException.Parameter.XRI, this.object.jdoGetObjectId()),
							new BasicException.Parameter("transactional-object-id", this.object.jdoGetTransactionalObjectId()),
							new BasicException.Parameter("object-class", this.object.objGetClass()),
							new BasicException.Parameter("feature-name", featureName),
							new BasicException.Parameter("feature-type", qualifiedTypeName),
							new BasicException.Parameter("multiplicity",  multiplicity)
						);
					}
					return marshaller.marshal(mandatoryValue);
                }
                case OPTIONAL:
                    return marshaller.marshal(this.object.objGetValue(featureName));
                case STREAM:
                    return this.object.objGetValue(featureName);
                case LIST:
                    return new MarshallingList(marshaller, this.object.objGetList(featureName));
                case SET:
                    return new MarshallingSet(marshaller, this.object.objGetSet(featureName));
                case SPARSEARRAY:
                    return new MarshallingSortedMap(marshaller, this.object.objGetSparseArray(featureName));
                case MAP:
                    return new MarshallingMap(this.refOutermostPackage(), this.object.objGetContainer(featureName));
                default:
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Unsupported multiplicity",
                        new BasicException.Parameter("feature", featureDef),
                        new BasicException.Parameter("type", type),
                        new BasicException.Parameter("actual-multiplicity", multiplicity),
                        new BasicException.Parameter("supported-multiplicity", (Object[])Multiplicity.values())
                    );
            }
        }

        /**
         * TODO multi-valued qualifier support, if necessary
         * Reference (not stored as attribute)
         */
        else if (isReference) {
            // Class type qualifier
            if (qualifier instanceof RefObject) {

                /**
                 * TODO multi-valued qualifier support 
                 *
                 * Get qualifier of exposing association end. This qualifier is
                 * used to construct the reference filter:
                 */
                String exposedEndName = model.getElement(
                    featureDef.getExposedEnd()
                ).getName();
                ModelElement_1_0 referencedEnd = model.getElement(featureDef.getReferencedEnd());
                List<?> qualifierNames = referencedEnd.objGetList("qualifierName");
                String qualifierName = removeContainerSuffix(
                    (String) qualifierNames.get(0)
                );
                Container_1_0 container = ((RefObject_1_0)qualifier).refDelegate().objGetContainer(
                    qualifierName
                ).subMap(
                    new Filter(
                        new IsInCondition(
                            Quantifier.THERE_EXISTS,
                            exposedEndName,
                            true, // IS_IN,
                            this.object.jdoGetObjectId()
                        )
                    )
                );
                return new RefContainer_1(
                    this.refOutermostPackage(),
                    container
                );
            } else {
                //
                // Primitive type or null qualifier
                //
                RefRootPackage_1 rootPkg = this.refOutermostPackage();
                if(
                    ModelHelper.isCompositeEnd(featureDef, true) ||
                    ModelHelper.isSharedEnd(featureDef, true)
                ) {
                    TransientContainerId containerId = rootPkg.refPersistenceManager().getContainerId(this);
                    return containerId == null ? null : rootPkg.refPersistenceManager().getObjectById(containerId.getParent());
                }
                else {
                    if(
                        (qualifier instanceof String) &&
                        (((String) qualifier).indexOf(';') >= 0)
                    ) {
                        return rootPkg.marshal(
                            ((DataObjectManager_1_0) rootPkg.refDelegate()).getObjectById(
                                this.refGetPath().getDescendant(
                                    featureDef.getName(),
                                    (String) qualifier
                                )
                            )
                        );
                    } else {
                        Container_1_0 container = this.object.objGetContainer(featureName);
                        try {
                            return qualifier == null ? new RefContainer_1(
                                rootPkg,
                                container
                             ) : rootPkg.marshal(
                                 container.get(qualifier.toString())
                             );
                        }  catch (ServiceException e) {
                            //
                            // in case of 0..1 multiplicity allow null as return value
                            //
                            if(
                                e.getExceptionCode() != BasicException.Code.NOT_FOUND ||
                                !Multiplicity.OPTIONAL.code().equals(featureDef.getMultiplicity())
                            ) {
                                throw new JmiServiceException(e, this);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    final long getValue(
        ModelElement_1_0 featureDef,
        Object value,
        long position
    ) throws ServiceException {
        boolean isAttribute = this.object.getModel().isAttributeType(featureDef);
        if (!isAttribute) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE,
                new BasicException.Parameter("model element", featureDef)
            );
        }
        ModelElement_1_0 type = this.getType(featureDef);
        String qualifiedTypeName = type.getQualifiedName();
        String featureName = featureDef.getName();

        // STREAM
        if (ModelHelper.getMultiplicity(featureDef).isStreamValued()) {
            try {
                if (PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                    CharacterLargeObject largeObject = (CharacterLargeObject) this.object.objGetValue(featureName);
                    largeObject.getContent((java.io.Writer) value, position);
                    return largeObject.getLength().longValue();
                } else if (PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                    BinaryLargeObject largeObject = (BinaryLargeObject) this.object.objGetValue(featureName);
                    largeObject.getContent((java.io.OutputStream) value, position);
                    return largeObject.getLength().longValue();
                } else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "unsupported stream type. Supported are [string|binary]",
                        new BasicException.Parameter("feature", featureDef),
                        new BasicException.Parameter("type", type)
                    );
                }
            } catch (IOException exception) {
                throw new ServiceException(exception);
            }
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "unsupported multiplicity. Supported are [stream]",
                new BasicException.Parameter("feature", featureDef),
                new BasicException.Parameter("type", type)
            );
        }
    }

    // -------------------------------------------------------------------------
    private void setValue(
            ModelElement_1_0 featureDef,
            Object value
    ) throws ServiceException {
        this.assertStructuralFeature(featureDef);

        ModelElement_1_0 type = this.getType(featureDef);
        String qualifiedTypeName = type.getQualifiedName();
        String featureName = featureDef.getName();

        /**
         * Attribute or Reference stored as attribute.
         */
        if (this.isAttributeOrReferenceStoredAsAttribute(featureDef)) {
            Multiplicity multiplicity = ModelHelper.getMultiplicity(featureDef);
            if (multiplicity.isSingleValued() || multiplicity.isStreamValued()) {
                if (this.object.getModel().isClassType(type) && "org:openmdx:base:Aspect:core".equals(featureDef.getQualifiedName())){
                    this.object.objSetValue(
                        featureName,
                        this.refOutermostPackage().unmarshalUnchecked(value)
                    );
                } else {
                    Marshaller marshaller = this.refOutermostPackage().getMarshaller(qualifiedTypeName);
                    this.object.objSetValue(
                        featureName,
                        marshaller.unmarshal(value)
                    );
                }
            }

            /**
             * In case of multi-valued attributes clear() and addAll()
             */
            else {
                Object newValue = null;
                if (
                        (value != null) &&
                        (value.getClass().isArray())
                ) {
                    newValue = new ArrayList<>();
                    for (int i = 0; i < Array.getLength(value); i++) {
                        final Object element =
                            (value instanceof short[]) ?  Short.valueOf(((short[]) value)[i]) :
                            (value instanceof int[]) ? Integer.valueOf(((int[]) value)[i]) :
                            (value instanceof long[]) ? Long.valueOf(((long[]) value)[i]) :
                            (value instanceof boolean[])  ? Boolean.valueOf(((boolean[]) value)[i]) :
                            ((Object[]) value)[i];
                        ((List) newValue).add(element);
                    }
                }
                else {
                    newValue = value;
                }
                Object values = this.getValue(featureDef, null);
                if (values != newValue) {
                    switch(multiplicity) {
                        case SPARSEARRAY: {
                            ((SortedMap) values).clear();
                            if (newValue instanceof Collection) {
                                int i = 0;
                                for(
                                        Iterator j = ((Collection) newValue).iterator();
                                        j.hasNext();
                                ) {
                                    ((SortedMap) values).put(
                                        Integer.valueOf(i++),
                                        j.next()
                                    );
                                }
                            }
                            else {
                                ((SortedMap) values).putAll((SortedMap) newValue);
                            }
                        } break;
                        case LIST: case SET: {
                            Collection target = (Collection) values;
                            target.clear();
                            target.addAll((Collection) newValue);
                        } break;
                        default:
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "unknown multiplicity",
                                new BasicException.Parameter("feature", featureDef),
                                new BasicException.Parameter("multiplicity", multiplicity)
                            );
                    }
                }
            }
        }

        /**
         * References (not supported)
         */
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "set supported only for attributes and references stored as attributes",
                new BasicException.Parameter("feature", featureDef),
                new BasicException.Parameter("type", type)
            );
        }
    }

    // -------------------------------------------------------------------------
    final void setValue(
        ModelElement_1_0 featureDef,
        Object newValue,
        long length
    ) throws ServiceException {
        boolean isAttribute = this.object.getModel().isAttributeType(featureDef);
        if (!isAttribute) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE,
                new BasicException.Parameter("model element", featureDef)
            );
        }

        ModelElement_1_0 type = this.getType(featureDef);

        // STREAM
        if (ModelHelper.getMultiplicity(featureDef).isStreamValued()) {
            String qualifiedTypeName = type.getQualifiedName();
            String featureName = featureDef.getName();
            if (PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                this.object.objSetValue(
                    featureName,
                    CharacterLargeObjects.valueOf(
                        (java.io.Reader)newValue,
                        CharacterLargeObjects.asLength(length)
                    )
                );
            }  else if (PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                this.object.objSetValue(
                    featureName,
                    BinaryLargeObjects.valueOf(
                        (java.io.InputStream) newValue,
                        BinaryLargeObjects.asLength(length)
                    )
                );
            } else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "unsupported stream type. Supported are [string|binary]",
                    new BasicException.Parameter("feature", featureDef),
                    new BasicException.Parameter("type", type)
                );
            }
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "unsupported multiplicity. Supported are [stream]",
                new BasicException.Parameter("feature", featureDef),
                new BasicException.Parameter("type", type)
            );
        }
    }

    // -------------------------------------------------------------------------
    /**
     * args contains one element which is of type RefStruct
     */
    private Object invokeOperation(
            ModelElement_1_0 featureDef,
            List<?> args
    ) throws ServiceException {
        SysLog.log(Level.FINEST, "Sys|refMofId={0},featureDef={1}|args={2}", this.object.jdoGetObjectId(), featureDef, args);
        this.assertOperation(featureDef);

        // get the type names of 'in' parameter and 'result'
        String qualifiedNameResultType = null;
        String qualifiedNameInParamType = null;
        for (
            Iterator<?> i = featureDef.objGetList("content").iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 paramDef = this.object.getModel().getElement(i.next());
            ModelElement_1_0 paramDefType = this.getType(paramDef);
            if ("in".equals(paramDef.getName())) {
                qualifiedNameInParamType = paramDefType.getQualifiedName();
            }
            else if ("result".equals(paramDef.getName())) {
                qualifiedNameResultType = paramDefType.getQualifiedName();
            }
        }
        if (qualifiedNameInParamType == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "no parameter with name \"in\" defined for operation",
                new BasicException.Parameter("operation", featureDef)
            );
        }
        if (qualifiedNameResultType == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "no parameter with name \"result\" defined for operation",
                new BasicException.Parameter("operation", featureDef)
            );
        }
        RefPackage_1_0 refPackage = this.refOutermostPackage();
        RefStruct_1_0 input = (RefStruct_1_0) (
            args.size() == 1 && args.get(0) instanceof RefStruct_1_0 ? args.get(0) :
            this.refOutermostPackage().refCreateStruct(qualifiedNameInParamType,args)
        );
        RefStruct_1_0 output = (RefStruct_1_0) refPackage.refCreateStruct(
            qualifiedNameResultType,
            (List<?>)null // output record will be updated by method invocation
        );
        try {
            this.object.execute(
                InteractionSpecs.newMethodInvocationSpec(
                    featureDef.getName(),
                    this.getInteractionVerb(Boolean.TRUE.equals(featureDef.objGetValue("isQuery")))
                ),
                input.refDelegate(),
                output.refDelegate()
              );
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
        return output;
    }

    /**
     * Tells, whether an operation must be invoked immediately or not
     *
     * @return {@code SYNC_SEND_RECEIVE} if an operation must be invoked immediately
     *
     * @see InteractionSpec.SYNC_SEND_RECEIVE
     * @see InteractionSpec.SYNC_SEND
     */
    private int getInteractionVerb(
        boolean query
    ) {
        return !query && this.jdoGetPersistenceManager().currentUnitOfWork().getOptimistic() ?
            InteractionSpec.SYNC_SEND :
            InteractionSpec.SYNC_SEND_RECEIVE;
    }

    // -------------------------------------------------------------------------
    // RefObject_1
    // operations which might be useful for subclasses
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    /**
     * feature must be attribute or reference stored as attribute
     */
    private Object refGetValue(
            String featureName,
            int index
    ) {
        Object value = null;
        try {
            value = this.getValue(this.getFeature(featureName), null);
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        if (value instanceof Collection) {
            if (value instanceof List) {
                return ((List<?>) value).get(index);
            }
            else if (value instanceof SortedMap) {
                return ((SortedMap<Integer,?>) value).get(Integer.valueOf(index));
            }
            else {
                throw new JmiServiceException(
                    this,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "operation only supported for features instanceof [List|SparseArray]",
                    new BasicException.Parameter("value", value)
                );
            }
        }

        // single-valued --> index = 0
        else if (index == 0) {
            return value;
        }

        // index > 0 not supported
        else {
            throw new JmiServiceException(
                this,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "index must be 0 in case of a non-collection value",
                new BasicException.Parameter("feature", featureName),
                new BasicException.Parameter("value", value)
            );
        }
    }

    // -------------------------------------------------------------------------
    private Object refGetValue(
            String featureName,
            String qualifier
    ) {
        try {
            Object map = this.getValue(this.getFeature(featureName), null);
            if (map instanceof Map<?,?>) {
                return ((Map<?,?>) map).get(qualifier);
            }
            if (map instanceof RefContainer<?>){

                RefContainer<?> container = (RefContainer<?>)map;

                #if CLASSIC_CHRONO_TYPES
                final Object persistent = RefContainer.PERSISTENT;
                final Object reassignable = RefContainer.REASSIGNABLE;
                #else
                final QualifierType persistent = QualifierType.PERSISTENT;
                final QualifierType reassignable = QualifierType.REASSIGNABLE;
                #endif

                return
                    qualifier.startsWith("!") ? container.refGet(persistent, qualifier.substring(1)) :
                    qualifier.startsWith("*") ? container.refGet(reassignable, qualifier.substring(1)) :
                    container.refGet(reassignable, qualifier);
            }
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "index must be 0 in case of a non-collection value",
                new BasicException.Parameter("feature", featureName),
                new BasicException.Parameter("value", map)
            );
        } catch (RuntimeServiceException | ServiceException exception) {
            throw new JmiServiceException(exception, this);
        }
    }

    // -------------------------------------------------------------------------
    @Override
    final public Object refGetValue(
        String featureName,
        Object qualifier
    ) {
        try {
            Model_1_0 model = this.object.getModel();
            ModelElement_1_0 featureDef = this.getFeature(featureName);
            Object value = null;
            if (
                    model.isAttributeType(featureDef) ||
                    model.referenceIsStoredAsAttribute(featureDef)
            ) {
                // TODO: check modeled qualifier type instead of qualifier class
                if (qualifier instanceof String) {
                    value = this.refGetValue(featureName, (String) qualifier);
                }
                // must be numeric
                else {
                    value = this.refGetValue(
                        featureName,
                        ((Integer) IntegerMarshaller.NORMALIZE.unmarshal(qualifier)).intValue()
                    );
                }
            }
            else {
                value = this.getValue(featureDef, qualifier);
            }
            return value;
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @Override
    final public long refGetValue(
        String featureName,
        Object value,
        long position) {
        try {
            ModelElement_1_0 featureDef = this.getFeature(featureName);
            return this.getValue(featureDef, value, position);
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final protected void refSetValue(
        String featureName,
        int index,
        Object value
    ) {
        try {
            Object viewContext = this.refOutermostPackage().refInteractionSpec();
            Object values = viewContext == null ? this.getValue(this.getFeature(featureName), null) : null;

            // set indexed element in case of Collection values. For convenience
            // if collection.size() == index the element is added.
            if (values instanceof Collection) {
                if (values instanceof List) {
                    List<Object> valuesAsList = (List<Object>) values;
                    if (valuesAsList.size() == index) {
                        valuesAsList.add(value);
                    }
                    else {
                        valuesAsList.set(index, value);
                    }
                }
                else if (values instanceof SortedMap) {
                    ((SortedMap<Integer,Object>) values).put(Integer.valueOf(index), value);
                }
                else {
                    throw new JmiServiceException(
                        this,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        "operation only supported for features instanceof [List|SparseArray]",
                        new BasicException.Parameter("values", values)
                    );
                }
            }

            // reset value in case of non-collection attributes only supported
            // when index=0
            else if (index == 0) {
                this.setValue(this.getFeature(featureName), value);
            }

            // index > 0 not supported
            else {
                throw new JmiServiceException(
                    this,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "index must be 0 for non-collection values",
                    new BasicException.Parameter("values", values)
                );
            }
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @Override
    final public void refSetValue(
        String featureName,
        Object newValue,
        long length) {
        try {
            ModelElement_1_0 featureDef = this.getFeature(featureName);
            this.setValue(featureDef, newValue, length);
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final protected void refAddValue(
        String featureName,
        int index,
        Object value
    ) {
        Object values = null;
        try {
            values = this.getValue(this.getFeature(featureName), null);
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        if (values instanceof List) {
            ((List<Object>) values).add(index, value);
        } else {
            throw new JmiServiceException(
                this,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "operation only supported for features instanceof [List]",
                new BasicException.Parameter("values", values)
            );
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings({
    })
    final protected void refAddValue(
        String featureName,
        Object value
    ) {
        Object values = null;
        try {
            values = this.getValue(this.getFeature(featureName), null);
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        if (values instanceof List || values instanceof Set) {
            ((Collection<Object>) values).add(value);
        }
        else if (value instanceof SortedMap) {
            ((SortedMap<Integer,Object>) values).put(
                Integer.valueOf(((Integer) ((SortedMap<?,?>) values).lastKey()).intValue() + 1),
                value
            );
        }
        else if (values instanceof RefContainer && value instanceof RefObject_1_0) {

            #if CLASSIC_CHRONO_TYPES
            Object qualifier = RefContainer.REASSIGNABLE;
            #else
            QualifierType qualifier = QualifierType.REASSIGNABLE;
            #endif

            ((RefContainer)values).refAdd(
                    qualifier,
                    org.openmdx.base.naming.TransactionalSegment.getClassicRepresentationOfNewInstance(),
                    (RefObject) value
            );
        }
        else {
            throw new JmiServiceException(
                this,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "operation only supported for features instanceof [Set|List|SparseArray|Container]",
                new BasicException.Parameter("value class", values.getClass().getName()),
                new BasicException.Parameter("values", values)
            );
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings({})
    final protected void refRemoveValue(
        String featureName,
        int index
    ) {
        Object values = null;
        try {
            values = this.getValue(this.getFeature(featureName), null);
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        if (values instanceof List) {
            ((List<Object>) values).remove(index);
        }
        else if (values instanceof SortedMap) {
            ((SortedMap<Integer,Object>) values).remove(Integer.valueOf(index));
        }
        else {
            throw new JmiServiceException(
                this,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "operation only supported for features instanceof [List|SparseArray]",
                new BasicException.Parameter("values", values)
            );
        }
    }

    // -------------------------------------------------------------------------
    // RefObject interface
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    @Override
    final public boolean refIsInstanceOf(
        RefObject objType,
        boolean considerSubtypes
    ) {
        try {
            if (!this.object.getModel().isClassType(objType)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "objType must be a class type",
                    new BasicException.Parameter("objType", objType)
                );
            }
            return this.object.getModel().isInstanceof(this.object, objType);
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @Override
    final public RefClass refClass(
    ) {
        return this.refClass;
    }

    // -------------------------------------------------------------------------
    @Override
    final public RefFeatured refImmediateComposite(
    ) {
        Path path = this.refGetPath();
        return path == null || path.size() == 1 ?
            null :
            this.refOutermostPackage().refObject(path.getPrefix(path.size() - 2));
    }

    // -------------------------------------------------------------------------
    @Override
    final public RefFeatured refOutermostComposite(
    ) {
        throw new UnsupportedOperationException();
    }

    // -------------------------------------------------------------------------
    @Override
    final public void refDelete(
    ) {
        try {
            ReducedJDOHelper.getPersistenceManager(this.object).deletePersistent(this.object);
        } catch(RuntimeException exception) {
            throw new JmiServiceException(exception);
        }
    }

    // -------------------------------------------------------------------------
    // RefFeatured interface
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    @Override
    final public Object refGetValue(
        RefObject feature
    ) {
        try {
            Object value = this.getValue(((RefMetaObject_1) feature).getElementDef(), null);
            return value;
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @Override
    final public Object refGetValue(
        String featureName
    ) {
        try {
            return this.getValue(this.getFeature(featureName), null);
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @Override
    final public void refSetValue(
        RefObject feature,
        Object value
    ) {
        try {
            this.setValue(((RefMetaObject_1) feature).getElementDef(), value);
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @Override
    final public void refSetValue(
        String featureName,
        Object value
    ) {
        try {
            this.setValue(this.getFeature(featureName), value);
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public Object refInvokeOperation(
        RefObject requestedOperation,
        List args
    ) {
        try {
            return this.invokeOperation(
                (ModelElement_1_0) requestedOperation,
                args
            );
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    /**
     * Convert a ServiceException to a RefException
     *
     * @param exception
     *
     * @return the corresponding RefException
     *
     * @exception JmiServiceException in case of conversion failure
     */
    private RefException toRefException(
        ServiceException exception
    ){
        BasicException cursor = exception.getCause();
        while(
            cursor != null &&
            EXCEPTION_WRAPPERS.contains(cursor.getExceptionClass())
        ){
            cursor = cursor.getCause();
        }
        String exceptionType = cursor == null ? null : cursor.getParameter("class");
        if (exceptionType == null) {
            //
            // Exception type is missing
            //
            return new RefException_1(exception);
        } else try {
            //
            // Try to map to a user-defined exception.
            //
            return this.refOutermostPackage().refMapping().getExceptionConstructor(
                exceptionType,
                this.refImmediatePackage().refMofId()
            ).newInstance(
                new ServiceException(cursor)
            );
        } catch (Exception failure) {
            //
            // Do not use a RefException_1 in case of failure!
            //
            RuntimeServiceException exceptionStack = new RuntimeServiceException(failure);
            exceptionStack.getCause(null).initCause(exception);
            throw new JmiServiceException(exceptionStack, this);
        }
    }

    final public Object refInvokeOperation(
        String operationName,
        List args
    ) throws RefException {
        try {
            return this.invokeOperation(
                this.getFeature(operationName),
                args
            );
        } catch (ServiceException exception) {
            throw this.toRefException(exception);
        } catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    // RefBaseObject
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    @Override
    final public RefObject refMetaObject(
    ) {
        try {
            if (this.metaObject == null) {
                this.metaObject = new RefMetaObject_1(
                    this.object.getModel().getElement(this.refClass().refMofId())
                );
            }
            return this.metaObject;
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @Override
    final public RefPackage refImmediatePackage(
    ) {
        return "org:openmdx:base:Authority".equals(this.refClass().refMofId())
        ? this.refOutermostPackage().refPackage(this.refGetPath().getSegment(0).toClassicRepresentation())
            : this.refClass().refImmediatePackage();
    }

    // -------------------------------------------------------------------------
    @Override
    final public RefRootPackage_1 refOutermostPackage(
    ) {
        return (RefRootPackage_1) this.refClass().refOutermostPackage();
    }

    // -------------------------------------------------------------------------
    @Override
    final public String refMofId(
    ) {
        Path identity = this.object.jdoGetObjectId();
        return identity == null ? null : identity.toXRI();
    }

    // -------------------------------------------------------------------------
    final public Collection refVerifyConstraints(
        boolean deepVerify
    ) {
        Collection<ServiceException> violationSource = this.object.getModel().verifyObject(
            this.object,
            deepVerify,
            false // verifyDerived
        );
        if (violationSource == null) {
            return null;
        }
        else {
            String thisElement = this.object.jdoGetObjectId().toString();
            JmiException[] violationTarget = new JmiException[violationSource.size()];
            Iterator<ServiceException> violationIterator = violationSource.iterator();
            for (int i = 0; i < violationTarget.length; i++) {
                ServiceException serviceException = violationIterator.next();
                boolean thisElementInError =
                    thisElement != null &&
                    thisElement.equals(this.getElementInError(serviceException));
                violationTarget[i] = thisElementInError
                ? new JmiServiceException(serviceException, this)
                : new JmiServiceException(serviceException);
            }
            return Arrays.asList(violationTarget);
        }
    }

    // -------------------------------------------------------------------------
    private String getElementInError(
        ServiceException serviceException
    ) {
        return serviceException.getCause(
        ).getCause(
            BasicException.Code.DEFAULT_DOMAIN
        ).getParameter(
            "elementInError"
        );
    }

    // -------------------------------------------------------------------------
    // RefObject_1_0
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    @Override
    final public Path refGetPath(
    ) {
        try {
            return this.object.jdoGetObjectId();
        } catch (JDOException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @Override
    public Set<String> refDefaultFetchGroup(
    ) {
        try {
            // fetch group of object
            Set<String> fetchGroup = this.object.objDefaultFetchGroup();

            // add non-derived features
            RefMetaObject_1 objectDef = (RefMetaObject_1) this.refMetaObject();
            fetchGroup.addAll(this.object.getModel().getAttributeDefs(
                objectDef.getElementDef(),
                false,
                false).keySet()
            );
            return fetchGroup;
        }
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @Override
    final public void refInitialize(
        boolean setRequiredToNull,
        boolean setOptionalToNull,
        boolean emptyMultivalued
    ) {
        try {
            ModelElement_1_0 elementDef = ((RefMetaObject_1)this.refMetaObject()).getElementDef();
            for (
                Iterator<ModelElement_1_0> i = elementDef.objGetMap("allFeature").values().iterator();
                i.hasNext();
            ) {
                ModelElement_1_0 featureDef = i.next();
                if(
                    this.isAttributeOrReferenceStoredAsAttribute(featureDef) &&
                    Persistency.getInstance().isPersistentAttribute(featureDef)
                ) {
                    ModelElement_1_0 type = this.object.getModel().getElementType(featureDef);
                    switch(ModelHelper.getMultiplicity(featureDef)) {
                        case OPTIONAL: {
                            if(setOptionalToNull) {
                                this.setValue(featureDef, null);
                            }
                        } break;
                        case SINGLE_VALUE: {
                            if (setRequiredToNull) {
                                this.setValue(featureDef, null);
                            } else {
                                String qualifiedTypeName = type.getQualifiedName();
                                // only initialize if null
                                if(this.object.objGetValue(featureDef.getName()) == null) {
                                    if(PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                                        this.setValue(featureDef, "");
                                    } else if (PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                                        this.setValue(featureDef, Boolean.FALSE);
                                    } else if (PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                                        this.setValue(
                                                featureDef,
                                                DateTimeMarshaller.NORMALIZE.marshal(
                                                        org.w3c.format.DateTimeFormat.BASIC_UTC_FORMAT.format(
                                                                #if CLASSIC_CHRONO_TYPES new java.util.Date() #else java.time.Instant.now()#endif
                                                        )
                                                )
                                        );
                                    } else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                                        this.setValue(
                                            featureDef,
                                            DateMarshaller.NORMALIZE.marshal("20000101")
                                        );
                                    } else if (PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                                        this.setValue(featureDef, URI.create("xri://+null"));
                                    } else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                                        this.setValue(
                                            featureDef,
                                            DurationMarshaller.NORMALIZE.marshal("P0M")
                                        );
                                    } else if (PrimitiveTypes.SHORT .equals(qualifiedTypeName)) {
                                        this.setValue(featureDef, Short.valueOf((short) 0));
                                    } else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                                        this.setValue(featureDef, Integer.valueOf(0));
                                    } else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                                        this.setValue(featureDef, Long.valueOf(0L));
                                    } else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                                        this.setValue(featureDef, BigDecimal.ZERO);
                                    } else if (PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                                        this.setValue(featureDef, EMPTY_LARGE_OBJECT);
                                    } else if (this.object.getModel().isStructureType(type)) {
                                        throw new UnsupportedOperationException("Initialization of structs not supported");
                                    } else if (this.object.getModel().isClassType(type)) {
                                        SysLog.detail("Initialization of object references not supported", featureDef);
                                    } else if (PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)) {
                                        this.setValue(featureDef, ROOT_PATH);
                                    } else if(
                                        "org:omg:model1:PrimitiveType".equals(qualifiedTypeName)
                                    ) {
                                        SysLog.detail("Initialization of user defined primitive types not supported", featureDef);
                                    } else {
                                        throw new UnsupportedOperationException(
                                            "unsupported type " + type
                                        );
                                    }
                                }
                            }
                        } break;
                        case SET: {
                            final Set<Object> values = this.object.objGetSet(featureDef.getName());
                            if(emptyMultivalued) {
                                values.clear();
                            }
                        } break;
                        case LIST: {
                            final List<Object> values = this.object.objGetList(featureDef.getName());
                            if(emptyMultivalued) {
                                values.clear();
                            }
                        } break;
                        case SPARSEARRAY: {
                            final SortedMap<Integer, Object> values = this.object.objGetSparseArray(featureDef.getName());
                            if(emptyMultivalued) {
                                values.clear();
                            }
                        } break;
                        case MAP: {
                            final Map values = this.object.objGetMap(featureDef.getName());
                            if(emptyMultivalued) {
                                values.clear();
                            }
                        } break;
                        case STREAM:
                            // not initialized
                            break;
                    }
                }
            }
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refAddValue(
        String featureName,
        Object qualifier,
        Object value
    ) {
        try {
            if (qualifier == null) {
                this.refAddValue(featureName, value);
            }
            else {
                Object container = this.getValue(
                    this.getFeature(featureName),
                    null
                );
                if (
                        (container instanceof List) &&
                        (qualifier instanceof Number)
                ) {
                    ((List<Object>) container).add(
                        ((Number) qualifier).intValue(),
                        value
                    );
                }
                else if (
                        (container instanceof RefContainer_1) &&
                        (value instanceof RefObject_1_0)
                ) {
                    RefContainer_1 refContainer = (RefContainer_1) container;
                    (((RefObject_1_0) value).refDelegate()).objMove(
                        refContainer.refDelegate(),
                        qualifier.toString()
                    );
                    if (value instanceof RefObject_1) {
                        ((RefObject_1) value).refSetOutermostPackage(
                            refContainer.refOutermostPackage()
                        );
                    }
                } else {
                    throw new JmiServiceException(
                        this,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        "a) feature type = Reference and qualifier is RefObject_1_0; b) feature type = collection type attribute and qualifier is Number",
                        new BasicException.Parameter(
                            "values", container == null ? null : container.getClass().getName()
                        )
                    );
                }
            }
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @Override
    final public ObjectView_1_0 refDelegate(
    ) {
        return this.object;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_1#refSetOutermostPackage(javax.jmi.reflect.RefPackage)
     */
    private void refSetOutermostPackage(
        RefPackage newPackage
    ) {
        RefRootPackage_1 oldPackage = this.refOutermostPackage();
        if (oldPackage != newPackage) {
            oldPackage.unregister(this.object);
            this.refClass = newPackage.refClass(
                this.refClass.refMofId()
            );
            if(newPackage instanceof Marshaller) try {
                ((Marshaller)newPackage).marshal(this.object);
            }  catch (ServiceException exception) {
                throw new JmiServiceException(exception);
            }
        }
    }

    // --------------------------------------------------------------------------
    // Implements Serializable
    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------
    /**
     * Save the data of the <tt>Object_1_0</tt> instance to a stream (that is,
     * serialize it).
     *
     * @serialData The objects data
     */
    private synchronized void writeObject(
        java.io.ObjectOutputStream stream
    ) throws java.io.IOException {
        stream.defaultWriteObject();
    }

    // --------------------------------------------------------------------------
    /**
     * Reconstitute the <tt>Object_1_0</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.refOutermostPackage().register(
            this.object,
            this
        );
    }

    // -------------------------------------------------------------------------
    // Object
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    @Override
    public String toString(
    ) {
        return this.getClass().getName() + " delegating to " + this.object;
    }

    // -------------------------------------------------------------------------
    @Override
    public boolean equals(
        Object that
    ) {
        return
        that instanceof RefObject_1_0 &&
        this.object.equals(((RefObject_1_0) that).refDelegate());
    }

    // -------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode(
    ) {
        return this.object.hashCode();
    }


    // -------------------------------------------------------------------------
    // Implements PersistenceCapable
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object,
     *      int[])
     */
    @Override
    public void jdoCopyFields(
        Object other,
        int[] fieldNumbers
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer,
     *      java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsToObjectId(
        Object oid
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier,
     *      java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsToObjectId(
        ObjectIdFieldSupplier fm,
        Object oid
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoGetObjectId()
     */
    @Override
    public Object jdoGetObjectId(
    ) {
        return this.object.jdoIsPersistent() ? this.object.jdoGetObjectId() : null;
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoGetPersistenceManager()
     */
    @Override
    public PersistenceManager_1_0 jdoGetPersistenceManager(
    ) {
        return refOutermostPackage().refPersistenceManager();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
     */
    @Override
    public Object jdoGetTransactionalObjectId(
    ) {
        return this.object.jdoGetTransactionalObjectId();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    @Override
    public Object jdoGetVersion(
    ) {
        return this.object.jdoGetVersion();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDeleted()
     */
    @Override
    public boolean jdoIsDeleted(
    ) {
        return this.object.jdoIsDeleted();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    @Override
    public boolean jdoIsDetached(
    ) {
        return this.object.jdoIsDetached();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDirty()
     */
    @Override
    public boolean jdoIsDirty(
    ) {
        return this.object.jdoIsDirty();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoIsNew()
     */
    @Override
    public boolean jdoIsNew(
    ) {
        return this.object.jdoIsNew();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoIsPersistent()
     */
    @Override
    public boolean jdoIsPersistent(
    ) {
        return this.object.jdoIsPersistent();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoIsTransactional()
     */
    @Override
    public boolean jdoIsTransactional(
    ) {
        return this.object.jdoIsTransactional();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    @Override
    public void jdoMakeDirty(
        String fieldName
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager,
     *      java.lang.Object)
     */
    @Override
    public PersistenceCapable jdoNewInstance(
        StateManager sm,
        Object oid
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    @Override
    public PersistenceCapable jdoNewInstance(
        StateManager sm
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    @Override
    public Object jdoNewObjectIdInstance(
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    @Override
    public Object jdoNewObjectIdInstance(
        Object o
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    @Override
    public void jdoProvideField(
        int fieldNumber
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    @Override
    public void jdoProvideFields(
        int[] fieldNumbers
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    @Override
    public void jdoReplaceField(
        int fieldNumber
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    @Override
    public void jdoReplaceFields(
        int[] fieldNumbers
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    @Override
    public void jdoReplaceFlags(
    ) {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     *
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    @Override
    public void jdoReplaceStateManager(
        StateManager sm
    ) throws SecurityException {
        throw new UnsupportedOperationException("This JDO operation is not supported by openMDX");
    }

    //--------------------------------------------------------------------
    // Implements Cloneable
    //--------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Cloneable#openmdxjdoClone()
     */
    @Override
    public RefObject openmdxjdoClone(String... exclude) {
        try {
            return this.refClass().refCreateInstance(
                Collections.singletonList(
                    this.refDelegate().openmdxjdoClone(exclude)
                )
            );
        } catch (Exception exception) {
            throw new JDOUserException(
                "Object could not be cloned",
                exception,
                this
            );
        }
    }

}
