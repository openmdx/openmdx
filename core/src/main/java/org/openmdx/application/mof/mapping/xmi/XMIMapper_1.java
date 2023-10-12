/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: XMI Mapper
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
package org.openmdx.application.mof.mapping.xmi;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.omg.mof.cci.VisibilityKind;
import org.omg.mof.spi.Names;
import org.openmdx.application.mof.mapping.spi.AbstractMapper_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.wbxml.cci.StringTable;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;

/**
 * XMI Mapper
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class XMIMapper_1 extends AbstractMapper_1 {

    /**
     * Constructor 
     */
    public XMIMapper_1(
    ) {
        super(false, Names.XMI_PACKAGE_SUFFIX);
    }

    /**
     * The model1 schema location
     */
    private final static String MODEL1_XSD = Resources.toResourceXRI("org/omg/model1/" + Names.XMI_PACKAGE_SUFFIX + "/model1.xsd");

    /**
     * For multi-package export
     */
    private final static String DEFAULT_PROVIDER_NAME = "Mof";
    
    /**
     * 
     */
    public void externalize(
        String qualifiedPackageName,
        Model_1_0 model, 
        ZipOutputStream zip
    ) throws ServiceException {
        SysLog.trace("> Externalize");
        this.model = model;
        List<ModelElement_1_0> packagesToExport = this.getMatchingPackages(qualifiedPackageName);
        // Export all matching packages
        for(ModelElement_1_0 currentPackage: packagesToExport) {
            String currentPackageName = currentPackage.getQualifiedName();
            // Model as text/xml
            try {
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                this.createXMIModel(
                    currentPackageName, 
                    bs, 
                    "text/xml"
                );
                this.addToZip(zip, bs, this.model.getElement(currentPackageName), ".xml");
            } catch(Exception e) {
                throw new ServiceException(e);
            }
            // Model as application/vnd.openmdx-xmi.wbxml
            try {
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                this.createXMIModel(
                    currentPackageName, 
                    bs, 
                    "application/vnd.openmdx-xmi.wbxml" 
                );
                this.addToZip(zip, bs, this.model.getElement(currentPackageName), ".wbxml");
            } catch(Exception e) {
                throw new ServiceException(e);
            }
            // Model as schema
            try {
                ByteArrayOutputStream bs;
                PrintWriter pw =  new PrintWriter(bs = new ByteArrayOutputStream());
                this.createXMISchema(
                    currentPackageName, 
                    pw, 
                    true
                );
                this.addToZip(zip, bs, this.model.getElement(currentPackageName), ".xsd");
            } catch(Exception e) {
                throw new ServiceException(e);
            }
        }
    }

    /**
     * Externalize the model repository
     * 
     * @param model
     * @param bs
     * @param mimeType
     * @throws ServiceException
     */
    public void externalizeRepository(
        Model_1_0 model, 
        OutputStream bs,
        String mimeType
    ) throws ServiceException {
        SysLog.trace("> Externalize Repository");
        this.model = model;
        List<String> packagesToExport = new ArrayList<String>();
        for(ModelElement_1_0 packageToExport :  this.getMatchingPackages("%")) {
            packagesToExport.add(
                packageToExport.getQualifiedName()
            );
        }
        this.createXMIModel(
            packagesToExport, 
            bs, 
            mimeType, 
            true // allFeatures
        );
    }
    
    /**
     * Create an XMI Schema
     * 
     * @param packageName
     * @param outputStream
     * @param allFeatures
     * @throws ServiceException
     */
    private void createXMISchema(
        String packageName,
        PrintWriter outputStream,
        boolean allFeatures
    ) throws ServiceException {

        Map allCompositeAssociationEnds = new HashMap();

        for(
            Iterator<ModelElement_1_0> i = this.model.getContent().iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 modelElement = i.next();
            // Collect AssociationEnds of with containment='composite' for performance reasons.
            // The collection is used to determine the qualifiers of classes.
            if(modelElement.isAssociationEndType()) {
                if(AggregationKind.COMPOSITE.equals(modelElement.getAggregation())) {
                    allCompositeAssociationEnds.put(
                        modelElement.getType(), 
                        modelElement
                    );        
                }
            }
        }

        XMISchemaMapper XMISchemaWriter = new XMISchemaMapper(
            outputStream,
            this.model
        );
        XMISchemaWriter.writeSchemaHeader();

        // all model elements
        for(
                Iterator iterator = this.model.getContent().iterator();
                iterator.hasNext();
        ) {
            ModelElement_1_0 elementDef = (ModelElement_1_0)iterator.next();
            SysLog.trace("elementDef", elementDef.getQualifiedName());

            // PrimitiveType
            if(elementDef.isPrimitiveType()) {
                XMISchemaWriter.writePrimitiveType(elementDef);
            }
            // Class
            else if(
                elementDef.isClassType() ||
                elementDef.isStructureType()
            ) {
                XMISchemaWriter.writeComplexTypeHeader(elementDef);
                int compositeReferenceCount = 0;
                /**
                 * Write all class features
                 * phase 0: attributes and references stored as attributes
                 * phase 1: composite references
                 */
                for(
                    int featureType = 0;
                    featureType < 2;
                    featureType++
                ) {
                    if(featureType == 0) {
                        XMISchemaWriter.writeAttributeFeatureHeader(elementDef);
                    }

                    // iterate features
                    for(
                        Iterator i = elementDef.objGetList("feature").iterator();
                        i.hasNext();
                    ) {
                        ModelElement_1_0 featureDef = this.model.getElement(i.next());
                        SysLog.trace("processing feature", featureDef.getQualifiedName());
                        boolean isPublic = VisibilityKind.PUBLIC_VIS.equals(featureDef.objGetValue("visibility"));

                        // Attribute
                        if(this.model.isAttributeType(featureDef)) {  
                            boolean isChangeable = featureDef.isChangeable().booleanValue();
                            if(
                                    isPublic && 
                                    (isChangeable || allFeatures) 
                            ) {
                                if(featureType == 0) {
                                    XMISchemaWriter.writeAttribute(
                                        featureDef,
                                        this.model.getElementType(
                                            featureDef
                                        ).isClassType()
                                    );
                                }
                            }
                        }

                        // Reference
                        else if(this.model.isReferenceType(featureDef)) {  
                            ModelElement_1_0 referencedEnd = this.model.getElement(featureDef.getReferencedEnd());
                            boolean isNavigable = ((Boolean)referencedEnd.objGetValue("isNavigable")).booleanValue();
                            boolean isChangeable = 
                                featureDef.isChangeable().booleanValue() ||
                                //
                                // CR0004024
                                //
                                "org:openmdx:base:AuthorityHasProvider:provider".equals(referencedEnd.jdoGetObjectId().getLastSegment().toClassicRepresentation());
                            if(isPublic && isNavigable && (isChangeable || allFeatures)) {
                                if(this.model.referenceIsStoredAsAttribute(featureDef)) {
                                    if(featureType == 0) {
                                        XMISchemaWriter.writeReferenceStoredAsAttribute(
                                            featureDef
                                        );
                                    }
                                }
                                else if(!AggregationKind.NONE.equals(referencedEnd.getAggregation())) {
                                    if(featureType == 1) {
                                        if(compositeReferenceCount++ == 0) {
                                            XMISchemaWriter.writeCompositeReferenceFeatureHeader(elementDef);
                                        }
                                        XMISchemaWriter.writeReference(
                                            featureDef,
                                            elementDef
                                        );
                                    }
                                }
                            }
                        }
                    }
                    if(featureType == 0) {
                        XMISchemaWriter.writeAttributeFeatureFooter(elementDef);
                    }
                }

                XMISchemaWriter.writeCompositeReferenceFeatureFooter(elementDef, compositeReferenceCount);

                // Qualifiers        
                // Either the elementDef (which is a class) itself or one of its supertypes must 
                // have a composite AssocationEnd with a qualifier.
                for(
                        Iterator i = elementDef.objGetList("allSupertype").iterator();
                        i.hasNext();
                ) {
                    ModelElement_1_0 supertype = this.model.getDereferencedType(i.next());
                    ModelElement_1_0 modelAssociationEnd = null;
                    if((modelAssociationEnd = (ModelElement_1_0)allCompositeAssociationEnds.get(supertype.jdoGetObjectId())) != null) {
                        for(
                            int j = 0; 
                            j < modelAssociationEnd.objGetList("qualifierName").size(); 
                            j++
                        ) {
                            String qualifierName = (String)modelAssociationEnd.objGetList("qualifierName").get(j);
                            ModelElement_1_0 qualifierType = this.model.getDereferencedType(modelAssociationEnd.objGetList("qualifierType").get(j));
                            XMISchemaWriter.writeQualifierAttributes(
                                qualifierName, 
                                qualifierType.getQualifiedName(),
                                this.model.isPrimitiveType(qualifierType)
                            );    
                        }
                    }
                }
                XMISchemaWriter.writeComplexTypeFooter(elementDef);
            }

            // StructureType
            else if(
                elementDef.isStructureType()
            ) {
                XMISchemaWriter.writeComplexTypeHeader(elementDef);
                XMISchemaWriter.writeStructureFieldHeader(elementDef);

                // all class features
                for(
                    Iterator i = elementDef.objGetList("feature").iterator();
                    i.hasNext();
                ) {
                    ModelElement_1_0 featureDef = this.model.getElement(i.next());

                    // StructureField
                    if(featureDef.isStructureFieldType()) {
                        XMISchemaWriter.writeStructureField(
                            featureDef,
                            this.model.getElementType(
                                featureDef
                            ).isClassType()
                        );
                    }
                }
                XMISchemaWriter.writeStructureFieldFooter(elementDef);
                XMISchemaWriter.writeComplexTypeFooter(elementDef);
            }
        }    
        XMISchemaWriter.writeSchemaFooter();
    }

    /**
     * Externalize a single model element
     * 
     * @param XMIModelWriter
     * @param elementDef
     * @throws ServiceException
     */
    private void writeElement(
        XMIModelMapper XMIModelWriter,
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        if(elementDef.isClassType()) {
            XMIModelWriter.writeClass(elementDef);
        } else if(elementDef.isAttributeType()) {
            XMIModelWriter.writeAttribute(elementDef);
        } else if(elementDef.isAssociationType()) {
            XMIModelWriter.writeAssociation(elementDef);
        } else if(elementDef.isAssociationEndType()) {
            XMIModelWriter.writeAssociationEnd(elementDef);
        } else if(elementDef.isReferenceType()) {
            XMIModelWriter.writeReference(elementDef);
        } else if(elementDef.isOperationType()) {
            XMIModelWriter.writeOperation(elementDef);
        } else if(elementDef.isExceptionType()) {
            XMIModelWriter.writeException(elementDef);
        } else if(elementDef.isParameterType()) {
            XMIModelWriter.writeParameter(elementDef);
        } else if(elementDef.isPackageType()) {
            XMIModelWriter.writePackage(elementDef);
        } else if(elementDef.isPrimitiveType()) {
            XMIModelWriter.writePrimitiveType(elementDef);
        } else if(elementDef.isStructureType()) {
            XMIModelWriter.writeStructureType(elementDef);
        } else if(elementDef.isStructureFieldType()                                                                               ) {
            XMIModelWriter.writeStructureField(elementDef);
        } else if(elementDef.isAliasType()) {
            XMIModelWriter.writeAliasType(elementDef);
        } else if(elementDef.isConstraintType()) {
            // not exported in this version
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "can not export model element. Unsupported type",
                new BasicException.Parameter("element", elementDef)
            );
        }
    }

    /**
     * Externalize a single model package
     * 
     * @param forPackage
     * @param XMIModelWriter
     * @throws ServiceException
     */
    private void writePackage(String forPackage, XMIModelMapper XMIModelWriter)
        throws ServiceException {
        String segmentName = this.model.getElement(forPackage).getSegmentName();
        XMIModelWriter.writeSegmentHeader(segmentName);
        for(ModelElement_1_0 elementDef : this.model.getContent()){
            SysLog.trace("modelElement", elementDef.getQualifiedName());
            // CR0001066; only write model elements contained in the defining model (segment name)
            if(segmentName.equals(elementDef.getSegmentName())) {
                writeElement(XMIModelWriter, elementDef);
            }
        }    
        XMIModelWriter.writeSegmentFooter();
    }

    /**
     * Externalize a single model package
     * 
     * @param forPackage
     * @param out
     * @param mimeType
     * 
     * @throws ServiceException
     */
    private void createXMIModel(
        String forPackage,
        OutputStream out,
        String mimeType
    ) throws ServiceException {
        createXMIModel(
            Collections.singletonList(forPackage), 
            out, 
            mimeType, 
            false // allFeatires
         );
    }
    
    
    /**
     * Determine the provider name
     * 
     * @param forPackages
     * @return
     * @throws ServiceException
     */
    private String getProviderName(
        List<String> forPackages
    ) throws ServiceException {
        return DEFAULT_PROVIDER_NAME;
    }
    
    private void populateStringTable(
        StringTable target,
        List<String> source
    ) throws ServiceException{
        List<String> segmentNames = new ArrayList<String>();
        for(String packageName : source) {
            String segmentName = packageName.substring(0, packageName.lastIndexOf(':'));
            target.addString(segmentName);
            segmentNames.add(segmentName);
        }
        for(ModelElement_1_0 candidate : this.model.getContent()) {
            String segmentName  = candidate.getSegmentName();
            if(segmentNames.contains(segmentName)) {
                target.addString(candidate.jdoGetObjectId().toXRI());
            }
        }
    }
    
    /**
     * Externalize a set of model packages
     * 
     * @param forPackages
     * @param out
     * @param mimeType
     * @param allFeatures
     * 
     * @throws ServiceException
     */
    private void createXMIModel(
        List<String> forPackages,
        OutputStream out,
        String mimeType, 
        boolean allFeatures
    ) throws ServiceException {
        SysLog.trace("> createXMIModel", forPackages);
        System.out.println("INFO:    Model export as " + mimeType + " includes " + forPackages);
        XMIModelMapper xmiModelMapper = new XMIModelMapper(
            out,
            mimeType, 
            allFeatures
        );
        if(xmiModelMapper.isStringTablePopulatedExplicitely()) {
            populateStringTable(xmiModelMapper, forPackages);
        }
        xmiModelMapper.writeModelHeader(
            getProviderName(forPackages), 
            MODEL1_XSD
        );
        for(String forPackage : forPackages) {
            writePackage(forPackage, xmiModelMapper);
        }
        xmiModelMapper.writeModelFooter();
    }

}

