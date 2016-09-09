/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: XMI Model Importer
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application.mof.externalizer.xmi;

import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.resource.ResourceException;

import org.omg.mof.cci.DirectionKind;
import org.omg.mof.cci.ScopeKind;
import org.omg.mof.cci.VisibilityKind;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.application.mof.cci.ModelExceptions;
import org.openmdx.application.mof.externalizer.spi.DataproviderMode;
import org.openmdx.application.mof.externalizer.spi.ModelImporter_1;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1AggregationKind;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Association;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1AssociationEnd;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Attribute;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1ChangeableKind;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Class;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Consumer;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1DataType;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Generalization;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1ModelElement;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1MultiplicityRange;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Operation;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Package;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Parameter;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1TaggedValue;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1VisibilityKind;
import org.openmdx.base.dataprovider.cci.Channel;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.Stereotypes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Code;
import org.openmdx.kernel.log.SysLog;

@SuppressWarnings({"rawtypes","unchecked"})
public class XMIImporter_1 extends ModelImporter_1 implements UML1Consumer {

    XMIImporter_1(
        URL modelUrl,
        short xmiFormat,
        Map pathMap,
        PrintStream infos,
        PrintStream warnings,
        PrintStream errors
    ) {
    	super();
        this.modelUrl = modelUrl;
        this.xmiFormat = xmiFormat;
        this.pathMap = pathMap;
        this.infos = infos;
        this.warnings = warnings;
        this.errors = errors;
        this.hasErrors = false;
    }

    XMIImporter_1 newNestedImporter(
		URL packageURL
    ){
    	return new XMIImporter_1(
            packageURL,
            this.getXMIFormat(),
            this.getPathMap(),
            this.infos,
            this.warnings,
            this.errors
        );
    }
    
    //---------------------------------------------------------------------------
    public short getXMIFormat(
    ) {
        return this.xmiFormat;
    }

    //---------------------------------------------------------------------------
    public Map getPathMap(
    ) {
        return this.pathMap;
    }

    /* (non-Javadoc)
	 * @see org.openmdx.application.mof.externalizer.cci.ModelImporter_1_0#process(org.openmdx.base.dataprovider.cci.Channel)
	 */
	@Override
	public void process(
		Channel target
	) throws ResourceException {
        this.channel = target;
        try {
            this.beginImport();
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            this.invokeParser(
                null,
                new Stack()
            );
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            this.endImport();
        } catch(Exception e) {
            e.printStackTrace();
        }        
    }

    //---------------------------------------------------------------------------
    public void processNested(
        XMIImporter_1 importer,
        XMIReferenceResolver resolver,
        Stack scope
    ) throws ServiceException {
        this.channel = importer.channel;
        this.segments = importer.segments;
        this.invokeParser(
            resolver,
            scope
        );
    }

    //---------------------------------------------------------------------------
    private void info(
        String message
    ) {
        this.infos.println("INFO:    " + message);
    }

    //---------------------------------------------------------------------------
    private void invokeParser(
        XMIReferenceResolver _resolver,
        Stack scope
    ) throws ServiceException {
        XMIReferenceResolver resolver = _resolver;
        try {
            XMIParser xmiParser = null;
            this.info("Parsing url=" + this.modelUrl);
            if(XMI_FORMAT_POSEIDON == this.xmiFormat) {
                xmiParser = new XMI1Parser(this.infos, this.warnings, this.errors);
                if(resolver == null) {
                    resolver = new XMI1ReferenceResolver(new HashMap(), this.errors);
                    resolver.parse(this.modelUrl.toString());
                }
            } else if(XMI_FORMAT_MAGICDRAW == this.xmiFormat) {
                xmiParser = new XMI1Parser(this.infos, this.warnings, this.errors);
                if(resolver == null) {
                    resolver = new XMI1ReferenceResolver(new HashMap(), this.errors);
                    resolver.parse(this.modelUrl.toString());
                }
            } else if(XMI_FORMAT_RSM == this.xmiFormat) {
                xmiParser = new XMI20Parser(this.infos, this.warnings, this.errors);
                if(resolver == null) {
                    resolver = new XMI2ReferenceResolver(
                        new HashMap(),
                        new Stack(),
                        this.pathMap,
                        this.infos,
                        this.warnings,
                        this.errors, 
                        new HashMap<String, UML1AssociationEnd>()
                    );
                    resolver.parse(this.modelUrl.toString());
                }
            } else if(XMI_FORMAT_EMF == this.xmiFormat) {
                xmiParser = new XMI2Parser(this.infos, this.warnings, this.errors);
                if(resolver == null) {
                    resolver = new XMI2ReferenceResolver(
                        new HashMap(),
                        new Stack(),
                        this.pathMap,
                        this.infos,
                        this.warnings,
                        this.errors, 
                        null
                    );
                    resolver.parse(this.modelUrl.toString());
                }
            }
            if(resolver.hasErrors()) {
                throw new ServiceException(
                    ModelExceptions.MODEL_DOMAIN,
                    Code.ABORT,
                    "Parsing reported errors"
                );
            }
            else {
                xmiParser.parse(
                    this.modelUrl.toString(),
                    this,
                    resolver,
                    scope
                );
                if(this.hasErrors) {
                    throw new ServiceException(
                        ModelExceptions.MODEL_DOMAIN,
                        Code.ABORT,
                        "Parsing reported errors"
                    );
                }
            }
        }
        catch(Exception ex) {
            throw new ServiceException(ex);
        }
    }

    //---------------------------------------------------------------------------
    public void processUMLPackage(
        UML1Package umlPackage
    ) throws ResourceException {
    	SysLog.detail("Processing package", umlPackage.getQualifiedName());
    	final ObjectRecord modelPackage = this.channel.newObjectRecord(
			toElementPath(
				nameToPathComponent(umlPackage.getQualifiedName()),
				umlPackage.getName()
			),
			ModelAttributes.PACKAGE
		);
    	DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(modelPackage, "isAbstract", Boolean.FALSE);
		DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(modelPackage, "visibility", VisibilityKind.PUBLIC_VIS);

		// annotation
		String annotation = this.getAnnotation(umlPackage);
		if (annotation.length() != 0) {
		    DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(modelPackage, "annotation", annotation);
		}
        this.createModelElement(
            null,
            modelPackage
        );
    }

    //---------------------------------------------------------------------------
    public void processUMLAssociation(
        UML1Association umlAssociation
    ) throws Exception {

        this.verifyAssociationName(umlAssociation.getName());

        // ModelAssociation object
        ObjectRecord associationDef = this.channel.newObjectRecord(
            toElementPath(
                nameToPathComponent(getScope(umlAssociation.getQualifiedName())),
                umlAssociation.getName()
            ),
            ModelAttributes.ASSOCIATION
        );
        // container
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationDef, "container", toElementPath(
		    nameToPathComponent(getScope(umlAssociation.getQualifiedName())),
		    getName(getScope(umlAssociation.getQualifiedName()))
		));

        // annotation
        String annotation = this.getAnnotation(umlAssociation);
        if (annotation.length() != 0)
        {
        	DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationDef, "annotation", annotation);
        }

        // stereotype
        DataproviderMode.DATAPROVIDER_2.addAllToAttributeValuesAsList(associationDef, "stereotype",(Collection<?>) umlAssociation.getStereotypes());

        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationDef, "isAbstract", Boolean.FALSE);
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationDef, "visibility", VisibilityKind.PUBLIC_VIS);
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationDef, "isDerived", Boolean.valueOf(umlAssociation.isDerived()));
        this.createModelElement(
            null, 
            associationDef
        );
        ObjectRecord associationEnd1Def = this.processAssociationEnd(
            (UML1AssociationEnd)umlAssociation.getConnection().get(0),
            associationDef
        );
        ObjectRecord associationEnd2Def = this.processAssociationEnd(
            (UML1AssociationEnd)umlAssociation.getConnection().get(1),
            associationDef
        );
        if(XMI_FORMAT_POSEIDON == this.xmiFormat) {
            /**
             * Poseidon XMI/UML format
             * NOTE:
             * To comply with our MOF model implementation we change aggregation and 
             * qualifier assignments. Client aggregation and qualifier attributes 
             * now belong to the supplier side and supplier aggregation and qualifier 
             * attributes now belong to the client side.
             */

            // swap 'aggregation' to comply with our MOF model
            String temp = (String)DataproviderMode.DATAPROVIDER_2.getSingletonFromAttributeValuesAsList(associationEnd1Def, "aggregation");
            DataproviderMode.DATAPROVIDER_2.replaceAttributeValuesAsListBySingleton(associationEnd1Def, "aggregation", DataproviderMode.DATAPROVIDER_2.getSingletonFromAttributeValuesAsList(associationEnd2Def, "aggregation"));
            DataproviderMode.DATAPROVIDER_2.replaceAttributeValuesAsListBySingleton(associationEnd2Def, "aggregation", temp);

            // swap 'qualifierName' to comply with our MOF model
            List tempQualifier = new ArrayList(DataproviderMode.DATAPROVIDER_2.getAttributeValuesAsReadOnlyList(associationEnd1Def, "qualifierName"));
            DataproviderMode.DATAPROVIDER_2.replaceAttributeValuesAsList(associationEnd1Def, "qualifierName", DataproviderMode.DATAPROVIDER_2.getAttributeValuesAsReadOnlyList(associationEnd2Def, "qualifierName"));
            DataproviderMode.DATAPROVIDER_2.replaceAttributeValuesAsList(associationEnd2Def, "qualifierName", (Collection<?>) tempQualifier);

            // swap 'qualifierType' to comply with our MOF model
            tempQualifier = new ArrayList(DataproviderMode.DATAPROVIDER_2.getAttributeValuesAsReadOnlyList(associationEnd1Def, "qualifierType"));
            DataproviderMode.DATAPROVIDER_2.replaceAttributeValuesAsList(associationEnd1Def, "qualifierType", DataproviderMode.DATAPROVIDER_2.getAttributeValuesAsReadOnlyList(associationEnd2Def, "qualifierType"));
            DataproviderMode.DATAPROVIDER_2.replaceAttributeValuesAsList(associationEnd2Def, "qualifierType", (Collection<?>) tempQualifier);
        }
        else if (XMI_FORMAT_MAGICDRAW == this.xmiFormat)
        {
            /**
             * MagicDraw XMI/UML format
             * NOTE:
             * To comply with our MOF model implementation we must change the 
             * aggregation assignments for association ends.
             */

            // swap 'aggregation' to comply with our MOF model
            String temp = (String)DataproviderMode.DATAPROVIDER_2.getSingletonFromAttributeValuesAsList(associationEnd1Def, "aggregation");
            DataproviderMode.DATAPROVIDER_2.replaceAttributeValuesAsListBySingleton(associationEnd1Def, "aggregation", DataproviderMode.DATAPROVIDER_2.getSingletonFromAttributeValuesAsList(associationEnd2Def, "aggregation"));
            DataproviderMode.DATAPROVIDER_2.replaceAttributeValuesAsListBySingleton(associationEnd2Def, "aggregation", temp);
        }

        this.verifyAndCompleteAssociationEnds(
            associationEnd1Def,
            associationEnd2Def
        );
        this.exportAssociationEndAsReference(
            associationEnd1Def,
            associationEnd2Def,
            associationDef,
            null
        );
        this.exportAssociationEndAsReference(
            associationEnd2Def,
            associationEnd1Def,
            associationDef,
            null
        );
        this.createModelElement(
            null, 
            associationEnd1Def
        );
        this.createModelElement(
            null, 
            associationEnd2Def
        );
    }

    //---------------------------------------------------------------------------
    private ObjectRecord processAssociationEnd(
        UML1AssociationEnd umlAssociationEnd,
        ObjectRecord associationDef
    ) throws Exception {
        final String associationEndName = umlAssociationEnd.getName();
		this.verifyAssociationEndName(
            associationDef,
            associationEndName
        );
        if(XMI_FORMAT_POSEIDON == this.xmiFormat) {
            // Note: 
            // In the Poseidon XMI/UML format qualifiers are entered by using 
            // the association end name. The association end names consist of the
            // association end name and the qualifiers in square brackets (name and
            // qualfiers are separated by a new line). Therefore the real 
            // association end name and the qualifiers must be extracted.

            // extract/parse association end name and qualifiers
            // Note: 
            // Poseidon and MagicDraw use UMLAttributes for their internal qualifier 
            // representation, therefore the internal qualifier representation has 
            // to be mapped
            for(
                Iterator it = this.toAssociationEndQualifiers(associationEndName).iterator();
                it.hasNext();
            ) {
                Qualifier qualifier = (Qualifier)it.next();
                UML1Attribute attribute = new UML1Attribute("", qualifier.getName());
                attribute.setType(qualifier.getType());
                umlAssociationEnd.getQualifier().add(attribute);
            }
            umlAssociationEnd.setName(
                this.toAssociationEndName(associationEndName)
            );
        }
        ObjectRecord associationEndDef = this.channel.newObjectRecord(
            newFeaturePath(
                associationDef.getResourceIdentifier(),
                associationEndName
            ),
            ModelAttributes.ASSOCIATION_END
        );
        // name
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationEndDef, "name", associationEndName);
        // annotation
        String annotation = this.getAnnotation(umlAssociationEnd);
        if (annotation.length() != 0) {
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationEndDef, "annotation", annotation);
        }
        // type
        if(umlAssociationEnd.getParticipant() == null) {
            throw new ServiceException(
                ModelExceptions.MODEL_DOMAIN,
                ModelExceptions.INVALID_ATTRIBUTE_TYPE,
                "type is null for association",
                new BasicException.Parameter("association", umlAssociationEnd.getQualifiedName()),
        		new BasicException.Parameter("id", umlAssociationEnd.getId()),
        		new BasicException.Parameter("name", associationEndName),
        		new BasicException.Parameter("qualifiedName", umlAssociationEnd.getQualifiedName())
            );
        }
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationEndDef, "type", toElementPath(
		    nameToPathComponent(getScope(umlAssociationEnd.getParticipant())),
		    getName(umlAssociationEnd.getParticipant())
		));
        // multiplicity
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationEndDef, "multiplicity", this.toMOFMultiplicity(umlAssociationEnd.getMultiplicityRange()));
        // container
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationEndDef, "container", associationDef.getResourceIdentifier());
        // isChangeable
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationEndDef, "isChangeable", Boolean.valueOf(
		    this.toMOFChangeability(umlAssociationEnd.getChangeability())
		));
        // aggregation
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationEndDef, "aggregation", this.toMOFAggregation(umlAssociationEnd.getAggregation()));
        // isNavigable
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationEndDef, "isNavigable", Boolean.valueOf(umlAssociationEnd.isNavigable()));
        // qualifiers
        for (
            Iterator it = umlAssociationEnd.getQualifier().iterator();
            it.hasNext();
        ) {
            UML1Attribute qualifier = (UML1Attribute)it.next();
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationEndDef, "qualifierName", qualifier.getName());
            try {
                DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(associationEndDef, "qualifierType", toElementPath(
				    nameToPathComponent(getScope(qualifier.getType())),
				    getName(qualifier.getType())
				));
            }
            catch(Exception e) {
                throw new ServiceException(
                    e,
                    ModelExceptions.MODEL_DOMAIN,
                    ModelExceptions.INVALID_PARAMETER_DECLARATION,
                    "can not get qualifier type for association end",
                    new BasicException.Parameter("association end", umlAssociationEnd.getQualifiedName()),
                    new BasicException.Parameter("qualifier type", qualifier.getType())
                );
            }
        }

        return associationEndDef;
    }

    //---------------------------------------------------------------------------
    public void processUMLClass(
        UML1Class umlClass
    ) throws Exception {
        // depending on stereotype, the given class must be treated differently
        if (umlClass.getStereotypes().contains(Stereotypes.STRUCT)) {
            this.processStructureType(umlClass);
        }
        else if (umlClass.getStereotypes().contains(Stereotypes.ALIAS)) {
            this.processAliasType(umlClass);
        }
        else {
            this.processClass(umlClass);
        }
    }

    //---------------------------------------------------------------------------
    private void processStructureType(
        UML1Class umlClass
    ) throws Exception {

        SysLog.detail("Processing structure type", umlClass.getQualifiedName());

        // ModelClass object
        ObjectRecord structureTypeDef = this.channel.newObjectRecord(
            toElementPath(
                nameToPathComponent(getScope(umlClass.getQualifiedName())),
                umlClass.getName()
            ),
            ModelAttributes.STRUCTURE_TYPE
        );
        // container
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(structureTypeDef, "container", toElementPath(
		    nameToPathComponent(getScope(umlClass.getQualifiedName())),
		    getName(getScope(umlClass.getQualifiedName()))
		));

        /**
         * skip stereotype because its value 'Struct'
         * was marked to note the difference between
         * ordinary classes and structure types
         */

        // annotation
        String annotation = this.getAnnotation(umlClass);
        if (annotation.length() != 0) {
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(structureTypeDef, "annotation", annotation);
        }
        // supertype
        SortedSet superTypePaths = new TreeSet();
        for (
            Iterator it = umlClass.getSuperclasses().iterator();
            it.hasNext();
        ) {
            String superclass = (String)it.next();
            superTypePaths.add(
                toElementPath(
                    nameToPathComponent(getScope(superclass)),
                    getName(superclass)
                )
            );
        }
        // add supertypes in sorted order
        DataproviderMode.DATAPROVIDER_2.addAllToAttributeValuesAsList(structureTypeDef, "supertype", superTypePaths);

        // isAbstract attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(structureTypeDef, "isAbstract", Boolean.valueOf(umlClass.isAbstract()));

        // visibility attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(structureTypeDef, "visibility", VisibilityKind.PUBLIC_VIS);

        // isSingleton attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(structureTypeDef, "isSingleton", Boolean.FALSE);

        this.createModelElement(
            null,
            structureTypeDef
        );

        for(
            Iterator it = umlClass.getAttributes().iterator();
            it.hasNext();
        ) {
            this.processStructureField(
                (UML1Attribute)it.next(),
                structureTypeDef
            );
        }
    }

    //---------------------------------------------------------------------------
    private void processStructureField(
        UML1Attribute umlAttribute,
        ObjectRecord aContainer
    ) throws Exception {

        SysLog.detail("Processing structure field", umlAttribute.getName());

        ObjectRecord structureFieldDef = this.channel.newObjectRecord(
            newFeaturePath(
                aContainer.getResourceIdentifier(),
                umlAttribute.getName()
            ),
            ModelAttributes.STRUCTURE_FIELD
        );
        // container
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(structureFieldDef, "container", aContainer.getResourceIdentifier());

        // maxLength attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(structureFieldDef, "maxLength", Integer.valueOf(this.getAttributeMaxLength(umlAttribute)));

        // multiplicity attribute
        // openMDX uses attribute stereotype to indicate multiplicity
        // this allows to use multiplicities like set, list, ...
        // if no multiplicity has been modeled, the default multiplicity is taken
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(structureFieldDef, "multiplicity", umlAttribute.getStereotypes().size() > 0
		? umlAttribute.getStereotypes().iterator().next()
		    : umlAttribute.getMultiplicityRange() != null
		    ? this.toMOFMultiplicity(umlAttribute.getMultiplicityRange())
		        : DEFAULT_ATTRIBUTE_MULTIPLICITY);

        // annotation
        String annotation = this.getAnnotation(umlAttribute);
        if (annotation.length() != 0) {
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(structureFieldDef, "annotation", annotation);
        }
        if(umlAttribute.getType() == null) {
            this.error("Undefined type for field " + umlAttribute.getQualifiedName());
        }
        else {
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(structureFieldDef, "type", toElementPath(
			    nameToPathComponent(getScope(umlAttribute.getType())),
			    getName(umlAttribute.getType())
			));
            this.createModelElement(
                null,
                structureFieldDef
            );
        }
    }

    //---------------------------------------------------------------------------
    private void processAliasType(
        UML1Class umlClass
    ) throws Exception {

        SysLog.detail("Processing alias type", umlClass.getQualifiedName());

        // ModelClass object
        ObjectRecord aliasTypeDef = this.channel.newObjectRecord(
            toElementPath(
                nameToPathComponent(getScope(umlClass.getQualifiedName())),
                umlClass.getName()
            ),
            ModelAttributes.ALIAS_TYPE
        );
        // container
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(
        	aliasTypeDef, 
        	"container", 
        	toElementPath(
        		nameToPathComponent(getScope(umlClass.getQualifiedName())),
        		getName(getScope(umlClass.getQualifiedName()))
        	)
		);


        // annotation
        String annotation = this.getAnnotation(umlClass);
        if (annotation.length() != 0)
        {
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(aliasTypeDef, "annotation", annotation);
        }

        // isAbstract attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(aliasTypeDef, "isAbstract", Boolean.valueOf(umlClass.isAbstract()));

        // visibility attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(aliasTypeDef, "visibility", VisibilityKind.PUBLIC_VIS);

        // isSingleton attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(aliasTypeDef, "isSingleton", Boolean.FALSE);

        // type
        List attributes = umlClass.getAttributes();
        this.verifyAliasAttributeNumber(
            aliasTypeDef, 
            attributes.size()
        );

        UML1Attribute attribute = (UML1Attribute)umlClass.getAttributes().get(0);
        this.verifyAliasAttributeName(
            aliasTypeDef, 
            attribute.getName()
        );
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(
        	aliasTypeDef, 
        	"type", 
        	toElementPath(
        		nameToPathComponent(getScope(attribute.getName())),
		    	getName(attribute.getName())
        	)
        );

        // create element
        this.createModelElement(
            null,
            aliasTypeDef
        );
    }

    //---------------------------------------------------------------------------
    private void processClass(
        UML1Class umlClass
    ) throws Exception {
        SysLog.detail("Processing class", umlClass.getQualifiedName());

        // ModelClass object
        ObjectRecord classDef = this.channel.newObjectRecord(
            toElementPath(
                nameToPathComponent(getScope(umlClass.getQualifiedName())),
                umlClass.getName()
            ),
            ModelAttributes.CLASS
        );

        // container
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(classDef, "container", toElementPath(
		    nameToPathComponent(getScope(umlClass.getQualifiedName())),
		    getName(getScope(umlClass.getQualifiedName()))
		));

        // stereotype
        DataproviderMode.DATAPROVIDER_2.addAllToAttributeValuesAsList(classDef, "stereotype",(Collection<?>) umlClass.getStereotypes());

        // annotation
        String annotation = this.getAnnotation(umlClass);
        if (annotation.length() != 0) {
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(classDef, "annotation", annotation);
        }

        // supertype
        SortedSet superTypePaths = new TreeSet();
        for (
            Iterator it = umlClass.getSuperclasses().iterator();
            it.hasNext();
        ) {
            String superclass = (String)it.next();
            superTypePaths.add(
                toElementPath(
                    nameToPathComponent(getScope(superclass)),
                    getName(superclass)
                )
            );
        }
        // add supertypes in sorted order
        DataproviderMode.DATAPROVIDER_2.addAllToAttributeValuesAsList(classDef, "supertype", superTypePaths);

        // isAbstract attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(classDef, "isAbstract", Boolean.valueOf(umlClass.isAbstract()));

        // visibility attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(classDef, "visibility", VisibilityKind.PUBLIC_VIS);

        // isSingleton attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(classDef, "isSingleton", Boolean.FALSE);

        SysLog.detail("Class", classDef.getResourceIdentifier());
        this.createModelElement(
            null, 
            classDef
        );

        // process attributes of this class
        for (
            Iterator it = umlClass.getAttributes().iterator();
            it.hasNext();
        ) {
            this.processAttribute((UML1Attribute)it.next());
        }

        // process operations of this class
        for (
            Iterator it = umlClass.getOperations().iterator();
            it.hasNext();
        ) {
            this.processOperation(
                (UML1Operation)it.next(), 
                classDef
            );
        }
    }

    //---------------------------------------------------------------------------
    private void processAttribute(
        UML1Attribute umlAttribute
    ) throws Exception {
        SysLog.detail("Processing attribute", umlAttribute.getName());

        Path containerPath = toElementPath(
            nameToPathComponent(getScope(getScope(umlAttribute.getQualifiedName()))),
            getName(getScope(umlAttribute.getQualifiedName()))
        );

        ObjectRecord attributeDef = this.channel.newObjectRecord(
            newFeaturePath(
                containerPath,
                umlAttribute.getName()
            ),
            ModelAttributes.ATTRIBUTE
        );

        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(attributeDef, "container", containerPath);
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(attributeDef, "visibility", this.toMOFVisibility(umlAttribute.getVisiblity()));
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(attributeDef, "uniqueValues", Boolean.valueOf(DEFAULT_ATTRIBUTE_IS_UNIQUE));
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(attributeDef, "isLanguageNeutral", Boolean.valueOf(DEFAULT_ATTRIBUTE_IS_LANGUAGE_NEUTRAL));
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(attributeDef, "maxLength", Integer.valueOf(this.getAttributeMaxLength(umlAttribute)));

        boolean isDerived = this.isAttributeDerived(umlAttribute);
        boolean isChangeable = this.toMOFChangeability(umlAttribute.getChangeability());
        if(isDerived && isChangeable) {
            this.warning("Attribute <" + attributeDef.getResourceIdentifier() + "> is derived AND changeable. Derived attributes MUST NOT be changeable. Continuing with isChangeable=false!");
            isChangeable = false;
        }

        if(umlAttribute.getType() == null) {
            this.error("Undefined type for attribute " + umlAttribute.getQualifiedName());
        }
        else {
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(attributeDef, "type", toElementPath(
			    nameToPathComponent(getScope(umlAttribute.getType())),
			    getName(umlAttribute.getType())
			));
        }

        // openMDX uses attribute stereotype to indicate multiplicity
        // this allows to use multiplicities like set, list, ...
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(attributeDef, "multiplicity", umlAttribute.getStereotypes().size() > 0
		? umlAttribute.getStereotypes().iterator().next()
		    : umlAttribute.getMultiplicityRange() != null
		    ? this.toMOFMultiplicity(umlAttribute.getMultiplicityRange())
		        : DEFAULT_ATTRIBUTE_MULTIPLICITY);
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(attributeDef, "scope", ScopeKind.INSTANCE_LEVEL);
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(attributeDef, "isDerived", Boolean.valueOf(isDerived));
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(attributeDef, "isChangeable", Boolean.valueOf(isChangeable));

        // annotation
        String annotation = this.getAnnotation(umlAttribute);
        if (annotation.length() != 0)
        {
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(attributeDef, "annotation", annotation);
        }

        SysLog.detail("Attribute", attributeDef.getResourceIdentifier());
        this.createModelElement(
            null, 
            attributeDef
        );
    }

    //---------------------------------------------------------------------------
    private void processOperation(
        UML1Operation umlOperation,
        ObjectRecord aContainer
    ) throws ServiceException, ResourceException{
        SysLog.detail("Processing operation", umlOperation.getName());
        String operationName = umlOperation.getName();
        ObjectRecord operationDef = this.channel.newObjectRecord(
            newFeaturePath(
                aContainer.getResourceIdentifier(),
                umlOperation.getName()
            ),
            ModelAttributes.OPERATION
        );
        // container
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(operationDef, "container", aContainer.getResourceIdentifier());

        // stereotype
        boolean isException = false;
        if(umlOperation.getStereotypes().size() > 0) {
            DataproviderMode.DATAPROVIDER_2.addAllToAttributeValuesAsList(operationDef, "stereotype",(Collection<?>) umlOperation.getStereotypes());

            // well-known stereotype <<exception>>
            if(DataproviderMode.DATAPROVIDER_2.attributeValuesAsListContains(operationDef, "stereotype", Stereotypes.EXCEPTION)) {
                operationDef.getValue().setRecordName(
                    ModelAttributes.EXCEPTION
                );
                DataproviderMode.DATAPROVIDER_2.clearAttributeValuesAsList(operationDef, "stereotype");
                isException = true;
            }
        }

        // annotation
        String annotation = this.getAnnotation(umlOperation);
        if (annotation.length() != 0)
        {
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(operationDef, "annotation", annotation);
        }

        // visibility
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(operationDef, "visibility", VisibilityKind.PUBLIC_VIS);

        // scope
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(operationDef, "scope", ScopeKind.INSTANCE_LEVEL);

        // isQuery
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(operationDef, "isQuery", Boolean.valueOf(umlOperation.isQuery()));

        // parameters
        List parameters = umlOperation.getParametersWithoutReturnParameter();

        if(parameters.size() > 0) {

            /**
             * In openMDX all operations have exactly one parameter with name 'in'. The importer
             * supports two forms how parameters may be specified:
             * 1) p0:t0, p1:t1, ..., pn:tn. In this case a class with stereotype <parameter> is created
             *    and p0, ..., pn are added as class attributes. Finally, a parameter with name 'in'
             *    is created with the created parameter type.
             * 2) in:t. In this case the the parameter with name 'in' is created with the specified type.
             */

            /**
             * Create the parameter type class. We need this class only in case 1. Because we only know
             * at the end whether we really need it, create it anyway but do not add it to the repository.
             */
            String capOperationName =
                operationName.substring(0,1).toUpperCase() +
                operationName.substring(1);

            ObjectRecord parameterType = this.channel.newObjectRecord(
                new Path(
                	aContainer.getResourceIdentifier() + capOperationName + "Params"
                ),
                ModelAttributes.STRUCTURE_TYPE
            );
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(parameterType, "visibility", VisibilityKind.PUBLIC_VIS);
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(parameterType, "isAbstract", Boolean.FALSE);
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(parameterType, "isSingleton", Boolean.FALSE);
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(parameterType, "container",DataproviderMode.DATAPROVIDER_2.getSingletonFromAttributeValuesAsList(aContainer, "container"));

            /**
             * Create parameters either as STRUCTURE_FIELD of parameterType (case 1) 
             * or as PARAMETER of modelOperation (case 2)
             */
            boolean createParameterType = true;
            boolean parametersCreated = false;
            for(
                Iterator it = parameters.iterator();
                it.hasNext();
            ) {
                UML1Parameter aParameter = (UML1Parameter)it.next();
                ObjectRecord parameterDef = this.processParameter(
                    aParameter,
                    parameterType
                );
                /**
                 * Case 2: Parameter with name 'in'. Create object as PARAMETER.
                 */
                String fullQualifiedParameterName = parameterDef.getResourceIdentifier().getLastSegment().toClassicRepresentation();
                if("in".equals(fullQualifiedParameterName.substring(fullQualifiedParameterName.lastIndexOf(':') + 1))) {
                    // 'in' is the only allowed parameter
                    if(parametersCreated) {
                        SysLog.error("Parameter format must be [p0:T0...pn:Tn | in:T], where T must be a class with stereotype " + Stereotypes.STRUCT);
                        throw new ServiceException(
                            ModelExceptions.MODEL_DOMAIN,
                            ModelExceptions.INVALID_PARAMETER_DECLARATION,
                            "Parameter format must be [p0:T0...pn:Tn | in:T], where T must be a class with stereotype " + Stereotypes.STRUCT,
                            new BasicException.Parameter("operation", operationDef.getResourceIdentifier())
                        );
                    }
                    parameterType = this.channel.newObjectRecord(
                        (Path)DataproviderMode.DATAPROVIDER_2.attributeValue(parameterDef, "type"),
                        "org:omg:model1:Parameter"
                    );
                    createParameterType = false;
                }
                /**
                 * Case 1: Parameter is attribute of parameter type. Create object as ATTRIBUTE.
                 */
                else {
                    // 'in' is the only allowed parameter
                    if(!createParameterType) {
                        SysLog.error("Parameter format must be [p0:T0, ... ,pn:Tn | in:T], where T must be a class with stereotype " + ModelAttributes.STRUCTURE_TYPE);
                        throw new ServiceException(
                            ModelExceptions.MODEL_DOMAIN,
                            ModelExceptions.INVALID_PARAMETER_DECLARATION,
                            "Parameter format must be [p0:T0, ... ,pn:Tn | in:T], where T must be a class with stereotype " + ModelAttributes.STRUCTURE_TYPE,
                            new BasicException.Parameter("operation", operationDef.getResourceIdentifier())
                        );
                    }
                    this.createModelElement(
                        null,
                        parameterDef
                    );
                    parametersCreated = true;
                }

            }
            /**
             * Case 1: parameter type must be created
             */
            if(createParameterType) {
                this.createModelElement(
                    null,
                    parameterType
                );
            }
            // in-parameter
            ObjectRecord inParameterDef = this.channel.newObjectRecord(
                newFeaturePath(
                    operationDef.getResourceIdentifier(),
                    "in"
                ),
                ModelAttributes.PARAMETER
            );
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(inParameterDef, "container", operationDef.getResourceIdentifier());
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(inParameterDef, "direction", DirectionKind.IN_DIR);
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(inParameterDef, "multiplicity", "1..1");
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(inParameterDef, "type", parameterType.getResourceIdentifier());
            this.createModelElement(
                null,
                inParameterDef
            );
        }

        // void in-parameter
        else {
            ObjectRecord inParameterDef = this.channel.newObjectRecord(
                newFeaturePath(
                    operationDef.getResourceIdentifier(),
                    "in"
                ),
                ModelAttributes.PARAMETER
            );
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(inParameterDef, "container", operationDef.getResourceIdentifier());
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(inParameterDef, "direction", DirectionKind.IN_DIR);
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(inParameterDef, "multiplicity", "1..1");
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(inParameterDef, "type", toElementPath(
			    nameToPathComponent("org::openmdx::base"),
			    "Void"
			));
            this.createModelElement(
                null,
                inParameterDef
            );
        }
        // Note:
        // return parameter is ignored for exceptions (operations with stereotype
        // exception)
        if(!isException) {
            ObjectRecord resultDef = this.channel.newObjectRecord(
                newFeaturePath(
                    operationDef.getResourceIdentifier(),
                    "result"
                ),
                ModelAttributes.PARAMETER
            );
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(resultDef, "container", operationDef.getResourceIdentifier());
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(resultDef, "direction", DirectionKind.RETURN_DIR);
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(resultDef, "multiplicity", "1..1");

            if(umlOperation.getReturnParameter().getType() == null) {
                this.error("Undefined return type for operation " + umlOperation.getQualifiedName());
            }
            else {
                DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(resultDef, "type", toElementPath(
				    nameToPathComponent(getScope(umlOperation.getReturnParameter().getType())),
				    getName(umlOperation.getReturnParameter().getType())
				));
                this.createModelElement(
                    null,
                    resultDef                   
                );
            }
        }

        // exceptions
        String allExceptions = this.getOperationExceptions(umlOperation);
        if (allExceptions != null) {
            StringTokenizer exceptions = new StringTokenizer(allExceptions, ",; ");
            while(exceptions.hasMoreTokens()) {
                String qualifiedExceptionName = exceptions.nextToken().trim();
                if (qualifiedExceptionName.indexOf("::") == -1) {
                    this.errors.println("Found invalid exception declaration <" + qualifiedExceptionName + "> for the operation " + umlOperation.getQualifiedName() + "; this exception is ignored unless a valid qualified exception name is specified.");
                }
                else {
                    String qualifiedClassName = qualifiedExceptionName.substring(0, qualifiedExceptionName.lastIndexOf("::"));
                    String scope = getScope(qualifiedClassName);
                    String name = getName(qualifiedClassName);
                    if (scope.length() == 0 || name.length() == 0)
                    {
                        this.errors.println("Found invalid exception declaration <" + qualifiedExceptionName + "> for the operation " + umlOperation.getQualifiedName() + "; this exception is ignored unless a valid qualified exception name is specified.");
                    }
                    else {
                        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(operationDef, "exception", newFeaturePath(
						    toElementPath(nameToPathComponent(scope), name),
						    getName(qualifiedExceptionName)
						));
                    }
                }
            }
        }
        this.createModelElement(
            null,
            operationDef
        );
    }

    //---------------------------------------------------------------------------
    private ObjectRecord processParameter(
        UML1Parameter umlParameter,
        ObjectRecord parameterType
    ) throws ServiceException, ResourceException{
        SysLog.detail("Processing parameter", umlParameter.getName());

        ObjectRecord parameterDef = this.channel.newObjectRecord(
            newFeaturePath(
                parameterType.getResourceIdentifier(),
                umlParameter.getName()
            ),
            ModelAttributes.STRUCTURE_FIELD
        );

        // container
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(parameterDef, "container", parameterType.getResourceIdentifier());

        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(parameterDef, "maxLength", Integer.valueOf(DEFAULT_PARAMETER_MAX_LENGTH));

        if(umlParameter.getType() == null) {
            this.error("Undefined type for parameter " + umlParameter.getQualifiedName());
        }
        else {
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(parameterDef, "type", toElementPath(
			    nameToPathComponent(getScope(umlParameter.getType())),
			    getName(umlParameter.getType())
			));
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(parameterDef, "multiplicity", umlParameter.getStereotypes().size() > 0 ?
			umlParameter.getStereotypes().iterator().next() :
			    DEFAULT_PARAMETER_MULTIPLICITY);
        }

        return parameterDef;

    }

    //---------------------------------------------------------------------------
    public void processUMLDataType(
        UML1DataType umlDataType
    ) throws Exception {
        SysLog.detail("Processing primitive type", umlDataType.getQualifiedName());

        // ModelPrimitiveType object
        ObjectRecord primitiveTypeDef = this.channel.newObjectRecord(
            toElementPath(
                nameToPathComponent(getScope(umlDataType.getQualifiedName())),
                umlDataType.getName()
            ),
            ModelAttributes.PRIMITIVE_TYPE
        );

        // container
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(primitiveTypeDef, "container", toElementPath(
		    nameToPathComponent(getScope(umlDataType.getQualifiedName())),
		    getName(getScope(umlDataType.getQualifiedName()))
		));

        /**
         * skip stereotype because its value 'Primitive'
         * was marked to note the difference between
         * ordinary classes and primitive types
         */

        // annotation
        String annotation = this.getAnnotation(umlDataType);
        if (annotation.length() != 0) {
            DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(primitiveTypeDef, "annotation", annotation);
        }

        // isAbstract attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(primitiveTypeDef, "isAbstract", Boolean.valueOf(umlDataType.isAbstract()));

        // isSingleton attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(primitiveTypeDef, "isSingleton", Boolean.FALSE);

        // visibility attribute
        DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(primitiveTypeDef, "visibility", VisibilityKind.PUBLIC_VIS);

        SysLog.detail("Primitive type", primitiveTypeDef.getResourceIdentifier());
        createModelElement(
            null, 
            primitiveTypeDef
        );
    }

    //---------------------------------------------------------------------------
    public void processUMLGeneralization(UML1Generalization genDef) {
        //
    }

    //---------------------------------------------------------------------------
    private String toMOFVisibility(
        UML1VisibilityKind umlVisibility
    ) {
        if(UML1VisibilityKind.PRIVATE.equals(umlVisibility)) {
            return VisibilityKind.PRIVATE_VIS;
        }
        else if(UML1VisibilityKind.PUBLIC.equals(umlVisibility)) {
            return VisibilityKind.PUBLIC_VIS;
        }
        else
        {
            return VisibilityKind.PUBLIC_VIS;
        }
    }

    //---------------------------------------------------------------------------
    private String toMOFAggregation(
        UML1AggregationKind umlAggregation
    ) {
        if(UML1AggregationKind.COMPOSITE.equals(umlAggregation)) {
            return AggregationKind.COMPOSITE;
        }
        else if(UML1AggregationKind.AGGREGATE.equals(umlAggregation)) {
            return AggregationKind.SHARED;
        }
        else
        {
            return AggregationKind.NONE;
        }
    }

    //---------------------------------------------------------------------------
    private String toMOFMultiplicity(
        UML1MultiplicityRange range
    ) {
        return new StringBuilder(
        ).append(
            range.getLower()
        ).append(
            ".."
        ).append(
            "-1".equals(range.getUpper()) || "*".equals(range.getUpper()) ? 
                "n" : 
                range.getUpper()
        ).toString();
    }

    //---------------------------------------------------------------------------
    private boolean toMOFChangeability(
        UML1ChangeableKind umlChangeability
    ) {
        return UML1ChangeableKind.CHANGEABLE.equals(umlChangeability);
    }

    //---------------------------------------------------------------------------
    private boolean isAttributeDerived(
        UML1Attribute attribute
    ) {
        // if feature isDerived is set return it
        if(attribute.isDerived() != null) {
            return attribute.isDerived().booleanValue();
        }
        // otherwise try to retrieve the isDerived information from tagged values
        for(
                Iterator it = attribute.getTaggedValues().iterator();
                it.hasNext();
        ) {
            UML1TaggedValue taggedValue = (UML1TaggedValue)it.next();
            if("derived".equals(taggedValue.getType().getName()) && "true".equals(taggedValue.getDataValue())) {
                return true;
            }
        }
        return false;
    }

    //---------------------------------------------------------------------------
    private String getAnnotation(
        UML1ModelElement modelElement
    ) {
        StringBuilder annotation = new StringBuilder();
        for(String comment: modelElement.getComment()) {
            annotation.append(comment);
        }
        return annotation.toString();
    }

    //---------------------------------------------------------------------------
    private int getAttributeMaxLength(
        UML1Attribute attribute
    ) {
        // the information about the maxLength of an attribute is stored as a
        // tagged value  (openMDX choice)
        for(
                Iterator it = attribute.getTaggedValues().iterator();
                it.hasNext();
        ) {
            UML1TaggedValue taggedValue = (UML1TaggedValue)it.next();
            if ("maxLength".equals(taggedValue.getType().getName()))
            {
                return Integer.parseInt(taggedValue.getDataValue());
            }
        }
        return DEFAULT_ATTRIBUTE_MAX_LENGTH;
    }

    //---------------------------------------------------------------------------
    private String getOperationExceptions(
        UML1Operation operation
    ) {
        for(String comment: operation.getComment()) {
            if(comment.startsWith(THROWS_EXCEPTION_PREFIX)) {
                return comment.substring(THROWS_EXCEPTION_PREFIX.length());
            }
        }
        for(           
                Iterator it = operation.getTaggedValues().iterator();
                it.hasNext();
        ) {
            UML1TaggedValue taggedValue = (UML1TaggedValue)it.next();
            if ("exceptions".equals(taggedValue.getType().getName())) {
                return taggedValue.getDataValue();
            }
        }
        return null;
    }

    //---------------------------------------------------------------------------
    private String toAssociationEndName(
        String assEndNameWithQualifier
    ) {
        if (assEndNameWithQualifier != null && assEndNameWithQualifier.indexOf((char)10) != -1)
        {
            return assEndNameWithQualifier.substring(
                0,
                assEndNameWithQualifier.indexOf((char)10)
            );
        }
        else
        {
            return assEndNameWithQualifier;
        }
    }

    //---------------------------------------------------------------------------
    private List toAssociationEndQualifiers(
        String assEndNameWithQualifier
    ) throws ServiceException, ResourceException {
        List qualifiers = new ArrayList();
        if (assEndNameWithQualifier != null && assEndNameWithQualifier.indexOf('[') != -1)
        {
            String qualifierText = assEndNameWithQualifier.substring(
                assEndNameWithQualifier.indexOf('[') + 1,
                assEndNameWithQualifier.indexOf(']')
            );
            qualifiers = this.parseAssociationEndQualifierAttributes(qualifierText);
        }
        return qualifiers;
    }

    //---------------------------------------------------------------------------
    private void warning(
        String text
    ) {
        SysLog.warning(text);
        this.warnings.println("WARNING: " + text);
    }

    //---------------------------------------------------------------------------
    private void error(
        String text
    ) {
        SysLog.error(text);
        this.errors.println("ERROR:   " + text);
        this.hasErrors = true;
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
    public static final short XMI_FORMAT_POSEIDON = 1;
    public static final short XMI_FORMAT_MAGICDRAW = 2;
    public static final short XMI_FORMAT_RSM = 3;
    public static final short XMI_FORMAT_EMF = 4;
    public static final String THROWS_EXCEPTION_PREFIX = "@throws";

    private PrintStream infos = null;
    private PrintStream errors = null;
    private PrintStream warnings = null;
    private final URL modelUrl;
    private final short xmiFormat;
    private final Map pathMap;

}

//--- End of File -----------------------------------------------------------
