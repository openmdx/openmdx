/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XMIImporter_1.java,v 1.41 2008/09/10 08:55:31 hburger Exp $
 * Description: XMI Model Importer
 * Revision:    $Revision: 1.41 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:31 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.model1.importer.xmi;

import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.omg.model1.code.DirectionKind;
import org.omg.model1.code.ScopeKind;
import org.omg.model1.code.VisibilityKind;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Code;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.ModelExceptions;
import org.openmdx.model1.code.Stereotypes;
import org.openmdx.model1.importer.spi.ModelImporter_1;
import org.openmdx.model1.uml1.UML1AggregationKind;
import org.openmdx.model1.uml1.UML1Association;
import org.openmdx.model1.uml1.UML1AssociationEnd;
import org.openmdx.model1.uml1.UML1Attribute;
import org.openmdx.model1.uml1.UML1ChangeableKind;
import org.openmdx.model1.uml1.UML1Class;
import org.openmdx.model1.uml1.UML1Consumer;
import org.openmdx.model1.uml1.UML1DataType;
import org.openmdx.model1.uml1.UML1Generalization;
import org.openmdx.model1.uml1.UML1ModelElement;
import org.openmdx.model1.uml1.UML1MultiplicityRange;
import org.openmdx.model1.uml1.UML1Operation;
import org.openmdx.model1.uml1.UML1Package;
import org.openmdx.model1.uml1.UML1Parameter;
import org.openmdx.model1.uml1.UML1TaggedValue;
import org.openmdx.model1.uml1.UML1VisibilityKind;

@SuppressWarnings("unchecked")
public class XMIImporter_1
extends ModelImporter_1
implements UML1Consumer {

    //---------------------------------------------------------------------------
    public XMIImporter_1(
        URL modelUrl,
        short xmiFormat,
        Map pathMap
    ) {
        this(
            modelUrl,
            xmiFormat,
            pathMap,
            System.out,
            System.err,
            System.err
        );
    }

    //---------------------------------------------------------------------------
    public XMIImporter_1(
        File file,
        short xmiFormat,
        Map pathMap
    ) throws MalformedURLException {
        this(
            file,
            xmiFormat,
            pathMap,
            System.out,
            System.err,
            System.err
        );
    }

    //---------------------------------------------------------------------------
    public XMIImporter_1(
        File file,
        short xmiFormat,
        Map pathMap,
        PrintStream infos,
        PrintStream warnings,
        PrintStream errors
    ) throws MalformedURLException {
        this(
            file.toURL(),
            xmiFormat,
            pathMap,
            infos,
            warnings,
            errors
        );
    }

    //---------------------------------------------------------------------------
    public XMIImporter_1(
        URL modelUrl,
        short xmiFormat,
        Map pathMap,
        PrintStream infos,
        PrintStream warnings,
        PrintStream errors
    ) {
        this.modelUrl = modelUrl;
        this.xmiFormat = xmiFormat;
        this.pathMap = pathMap;
        this.infos = infos;
        this.warnings = warnings;
        this.errors = errors;
        this.hasErrors = false;
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

    //---------------------------------------------------------------------------
    public void process(
        ServiceHeader header,
        Dataprovider_1_0 target,
        String providerName
    ) throws ServiceException {

        this.header = header;
        this.target = target;
        this.providerName = providerName;

        this.beginImport();
        this.invokeParser(
            null,
            new Stack()
        );
        this.endImport();
    }

    //---------------------------------------------------------------------------
    public void processNested(
        XMIImporter_1 importer,
        XMIReferenceResolver resolver,
        Stack scope
    ) throws ServiceException {

        this.header = importer.header;
        this.target = importer.target;
        this.providerName = importer.providerName;
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
            }
            else if(XMI_FORMAT_MAGICDRAW == this.xmiFormat) {
                xmiParser = new XMI1Parser(this.infos, this.warnings, this.errors);
                if(resolver == null) {
                    resolver = new XMI1ReferenceResolver(new HashMap(), this.errors);
                    resolver.parse(this.modelUrl.toString());
                }
            }
            else if(XMI_FORMAT_RSM == this.xmiFormat) {
                xmiParser = new XMI2Parser(this.infos, this.warnings, this.errors);
                if(resolver == null) {
                    resolver = new XMI2ReferenceResolver(
                        new HashMap(),
                        new Stack(),
                        this.pathMap,
                        this.infos,
                        this.warnings,
                        this.errors
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
    ) throws ServiceException {

        SysLog.info("Processing package " + umlPackage.getQualifiedName());

        DataproviderObject modelPackage = new DataproviderObject(
            toElementPath(
                nameToPathComponent(umlPackage.getQualifiedName()),
                umlPackage.getName()
            )
        );

        modelPackage.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.PACKAGE);
        modelPackage.values("isAbstract").add(new Boolean(false));
        modelPackage.values("visibility").add(VisibilityKind.PUBLIC_VIS);

        // annotation
        String annotation = this.getAnnotation(umlPackage);
        if (annotation.length() != 0)
        {
            modelPackage.values("annotation").add(annotation);
        }

        SysLog.info("package=" + modelPackage);
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
        DataproviderObject associationDef = new DataproviderObject(
            toElementPath(
                nameToPathComponent(getScope(umlAssociation.getQualifiedName())),
                umlAssociation.getName()
            )
        );

        // object_class
        associationDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ASSOCIATION);

        // container
        associationDef.values("container").add(
            toElementPath(
                nameToPathComponent(getScope(umlAssociation.getQualifiedName())),
                getName(getScope(umlAssociation.getQualifiedName()))
            )
        );

        // annotation
        String annotation = this.getAnnotation(umlAssociation);
        if (annotation.length() != 0)
        {
            associationDef.values("annotation").add(annotation);
        }

        // stereotype
        associationDef.values("stereotype").addAll(umlAssociation.getStereotypes());

        associationDef.values("isAbstract").add(new Boolean(false));
        associationDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);
        associationDef.values("isDerived").add(
            new Boolean(umlAssociation.isDerived())
        );

        createModelElement(null, associationDef);

        DataproviderObject associationEnd1Def = this.processAssociationEnd(
            (UML1AssociationEnd)umlAssociation.getConnection().get(0),
            associationDef
        );

        DataproviderObject associationEnd2Def = this.processAssociationEnd(
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
            String temp = (String)associationEnd1Def.values("aggregation").get(0);
            associationEnd1Def.values("aggregation").set(
                0,
                associationEnd2Def.values("aggregation").get(0)
            );
            associationEnd2Def.values("aggregation").set(0, temp);

            // swap 'qualifierName' to comply with our MOF model
            List tempQualifier = new ArrayList(associationEnd1Def.values("qualifierName"));
            associationEnd1Def.values("qualifierName").clear();
            associationEnd1Def.values("qualifierName").addAll(
                associationEnd2Def.values("qualifierName")
            );
            associationEnd2Def.values("qualifierName").clear();
            associationEnd2Def.values("qualifierName").addAll(tempQualifier);

            // swap 'qualifierType' to comply with our MOF model
            tempQualifier = new ArrayList(associationEnd1Def.values("qualifierType"));
            associationEnd1Def.values("qualifierType").clear();
            associationEnd1Def.values("qualifierType").addAll(
                associationEnd2Def.values("qualifierType")
            );
            associationEnd2Def.values("qualifierType").clear();
            associationEnd2Def.values("qualifierType").addAll(tempQualifier);
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
            String temp = (String)associationEnd1Def.values("aggregation").get(0);
            associationEnd1Def.values("aggregation").set(
                0,
                associationEnd2Def.values("aggregation").get(0)
            );
            associationEnd2Def.values("aggregation").set(0, temp);
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
        this.createModelElement(null, associationEnd1Def);
        this.createModelElement(null, associationEnd2Def);
    }

    //---------------------------------------------------------------------------
    private DataproviderObject processAssociationEnd(
        UML1AssociationEnd umlAssociationEnd,
        DataproviderObject associationDef
    ) throws Exception {

        this.verifyAssociationEndName(
            associationDef,
            umlAssociationEnd == null ? null : umlAssociationEnd.getName()
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
                    Iterator it = this.toAssociationEndQualifiers(umlAssociationEnd.getName()).iterator();
                    it.hasNext();
            ) {
                Qualifier qualifier = (Qualifier)it.next();
                UML1Attribute attribute = new UML1Attribute("", qualifier.getName());
                attribute.setType(qualifier.getType());
                umlAssociationEnd.getQualifier().add(attribute);
            }
            umlAssociationEnd.setName(
                this.toAssociationEndName(umlAssociationEnd.getName())
            );
        }

        DataproviderObject associationEndDef = new DataproviderObject(
            new Path(associationDef.path().toString() + "::" + umlAssociationEnd.getName())
        );

        // name
        associationEndDef.values("name").add(umlAssociationEnd.getName());

        // annotation
        String annotation = this.getAnnotation(umlAssociationEnd);
        if (annotation.length() != 0)
        {
            associationEndDef.values("annotation").add(annotation);
        }

        // object_class
        associationEndDef.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.ASSOCIATION_END
        );

        // type
        if(umlAssociationEnd.getParticipant() == null) {
            throw new ServiceException(
                ModelExceptions.MODEL_DOMAIN,
                ModelExceptions.INVALID_ATTRIBUTE_TYPE,
                "type is null for association",
                new BasicException.Parameter("association", umlAssociationEnd.getQualifiedName())
            );
        }
        associationEndDef.values("type").add(
            toElementPath(
                nameToPathComponent(getScope(umlAssociationEnd.getParticipant())),
                getName(umlAssociationEnd.getParticipant())
            )
        );

        // multiplicity
        associationEndDef.values("multiplicity").add(
            this.toMOFMultiplicity(umlAssociationEnd.getMultiplicityRange())
        );

        // container
        associationEndDef.values("container").add(
            associationDef.path()
        );

        // isChangeable
        associationEndDef.values("isChangeable").add(
            new Boolean(
                this.toMOFChangeability(umlAssociationEnd.getChangeability())
            )
        );

        // aggregation
        associationEndDef.values("aggregation").add(
            this.toMOFAggregation(umlAssociationEnd.getAggregation())
        );

        // isNavigable
        associationEndDef.values("isNavigable").add(
            new Boolean(umlAssociationEnd.isNavigable())
        );

        // qualifiers
        for (
                Iterator it = umlAssociationEnd.getQualifier().iterator();
                it.hasNext();
        ) {
            UML1Attribute qualifier = (UML1Attribute)it.next();
            associationEndDef.values("qualifierName").add(qualifier.getName());
            try {
                associationEndDef.values("qualifierType").add(
                    toElementPath(
                        nameToPathComponent(getScope(qualifier.getType())),
                        getName(qualifier.getType())
                    )
                );
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
        if (umlClass.getStereotypes().contains(Stereotypes.STRUCT))
        {
            this.processStructureType(umlClass);
        }
        else if (umlClass.getStereotypes().contains(Stereotypes.ALIAS))
        {
            this.processAliasType(umlClass);
        }
        else
        {
            this.processClass(umlClass);
        }
    }

    //---------------------------------------------------------------------------
    private void processStructureType(
        UML1Class umlClass
    ) throws Exception {

        SysLog.info("Processing structure type " + umlClass.getQualifiedName());

        // ModelClass object
        DataproviderObject structureTypeDef = new DataproviderObject(
            toElementPath(
                nameToPathComponent(getScope(umlClass.getQualifiedName())),
                umlClass.getName()
            )
        );

        // object_class
        structureTypeDef.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.STRUCTURE_TYPE
        );

        // container
        structureTypeDef.values("container").add(
            toElementPath(
                nameToPathComponent(getScope(umlClass.getQualifiedName())),
                getName(getScope(umlClass.getQualifiedName()))
            )
        );

        /**
         * skip stereotype because its value 'Struct'
         * was marked to note the difference between
         * ordinary classes and structure types
         */

        // annotation
        String annotation = this.getAnnotation(umlClass);
        if (annotation.length() != 0)
        {
            structureTypeDef.values("annotation").add(annotation);
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
        for(
                Iterator it = superTypePaths.iterator();
                it.hasNext();
        ) {
            structureTypeDef.values("supertype").add(it.next());
        }

        // isAbstract attribute
        structureTypeDef.values("isAbstract").add(
            new Boolean(umlClass.isAbstract())
        );

        // visibility attribute
        structureTypeDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

        // isSingleton attribute
        structureTypeDef.values("isSingleton").add(
            new Boolean(false)
        );

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
        DataproviderObject aContainer
    ) throws Exception {

        SysLog.info("Processing structure field " + umlAttribute.getName());

        DataproviderObject structureFieldDef = new DataproviderObject(
            new Path(aContainer.path().toString() + "::" + umlAttribute.getName())
        );

        // object_class
        structureFieldDef.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.STRUCTURE_FIELD
        );

        // container
        structureFieldDef.values("container").add(aContainer.path());

        // maxLength attribute
        structureFieldDef.values("maxLength").add(new Integer(this.getAttributeMaxLength(umlAttribute)));

        // multiplicity attribute
        // openMDX uses attribute stereotype to indicate multiplicity
        // this allows to use multiplicities like set, list, ...
        // if no multiplicity has been modeled, the default multiplicity is taken
        structureFieldDef.values("multiplicity").add(
            umlAttribute.getStereotypes().size() > 0
            ? umlAttribute.getStereotypes().iterator().next()
                : umlAttribute.getMultiplicityRange() != null
                ? this.toMOFMultiplicity(umlAttribute.getMultiplicityRange())
                    : DEFAULT_ATTRIBUTE_MULTIPLICITY
        );

        // annotation
        String annotation = this.getAnnotation(umlAttribute);
        if (annotation.length() != 0)
        {
            structureFieldDef.values("annotation").add(annotation);
        }

        if(umlAttribute.getType() == null) {
            this.error("Undefined type for field " + umlAttribute.getQualifiedName());
        }
        else {
            structureFieldDef.values("type").add(
                toElementPath(
                    nameToPathComponent(getScope(umlAttribute.getType())),
                    getName(umlAttribute.getType())
                )
            );
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

        SysLog.info("Processing alias type " + umlClass.getQualifiedName());

        // ModelClass object
        DataproviderObject aliasTypeDef = new DataproviderObject(
            toElementPath(
                nameToPathComponent(getScope(umlClass.getQualifiedName())),
                umlClass.getName()
            )
        );

        // object_class
        aliasTypeDef.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.ALIAS_TYPE
        );

        // container
        aliasTypeDef.values("container").add(
            toElementPath(
                nameToPathComponent(getScope(umlClass.getQualifiedName())),
                getName(getScope(umlClass.getQualifiedName()))
            )
        );


        // annotation
        String annotation = this.getAnnotation(umlClass);
        if (annotation.length() != 0)
        {
            aliasTypeDef.values("annotation").add(annotation);
        }

        // isAbstract attribute
        aliasTypeDef.values("isAbstract").add(
            new Boolean(umlClass.isAbstract())
        );

        // visibility attribute
        aliasTypeDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

        // isSingleton attribute
        aliasTypeDef.values("isSingleton").add(
            new Boolean(false)
        );

        // type
        List attributes = umlClass.getAttributes();
        this.verifyAliasAttributeNumber(aliasTypeDef, attributes.size());

        UML1Attribute attribute = (UML1Attribute)umlClass.getAttributes().get(0);
        this.verifyAliasAttributeName(aliasTypeDef, attribute.getName());
        aliasTypeDef.values("type").add(
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

        SysLog.info("Processing class " + umlClass.getQualifiedName());

        // ModelClass object
        DataproviderObject classDef = new DataproviderObject(
            toElementPath(
                nameToPathComponent(getScope(umlClass.getQualifiedName())),
                umlClass.getName()
            )
        );

        // object_class
        classDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.CLASS);

        // container
        classDef.values("container").add(
            toElementPath(
                nameToPathComponent(getScope(umlClass.getQualifiedName())),
                getName(getScope(umlClass.getQualifiedName()))
            )
        );

        // stereotype
        classDef.values("stereotype").addAll(
            umlClass.getStereotypes()
        );

        // annotation
        String annotation = this.getAnnotation(umlClass);
        if (annotation.length() != 0)
        {
            classDef.values("annotation").add(annotation);
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
        for(
                Iterator it = superTypePaths.iterator();
                it.hasNext();
        ) {
            classDef.values("supertype").add(it.next());
        }

        // isAbstract attribute
        classDef.values("isAbstract").add(
            new Boolean(umlClass.isAbstract())
        );

        // visibility attribute
        classDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

        // isSingleton attribute
        classDef.values("isSingleton").add(
            new Boolean(false)
        );

        SysLog.info("class=" + classDef);
        createModelElement(null, classDef);

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
            this.processOperation((UML1Operation)it.next(), classDef);
        }
    }

    //---------------------------------------------------------------------------
    private void processAttribute(
        UML1Attribute umlAttribute
    ) throws Exception {
        SysLog.info("Processing attribute " + umlAttribute.getName());

        Path containerPath = toElementPath(
            nameToPathComponent(getScope(getScope(umlAttribute.getQualifiedName()))),
            getName(getScope(umlAttribute.getQualifiedName()))
        );

        DataproviderObject attributeDef = new DataproviderObject(
            new Path(containerPath.toString() + "::" + umlAttribute.getName())
        );

        attributeDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ATTRIBUTE);

        attributeDef.values("container").add(containerPath);
        attributeDef.values("visibility").add(this.toMOFVisibility(umlAttribute.getVisiblity()));
        attributeDef.values("uniqueValues").add(new Boolean(DEFAULT_ATTRIBUTE_IS_UNIQUE));
        attributeDef.values("isLanguageNeutral").add(new Boolean(DEFAULT_ATTRIBUTE_IS_LANGUAGE_NEUTRAL));
        attributeDef.values("maxLength").add(new Integer(this.getAttributeMaxLength(umlAttribute)));

        boolean isDerived = this.isAttributeDerived(umlAttribute);
        boolean isChangeable = this.toMOFChangeability(umlAttribute.getChangeability());
        if(isDerived && isChangeable) {
            this.warning("Attribute <" + attributeDef.path().toString() + "> is derived AND changeable. Derived attributes MUST NOT be changeable. Continuing with isChangeable=false!");
            isChangeable = false;
        }

        if(umlAttribute.getType() == null) {
            this.error("Undefined type for attribute " + umlAttribute.getQualifiedName());
        }
        else {
            attributeDef.values("type").add(
                toElementPath(
                    nameToPathComponent(getScope(umlAttribute.getType())),
                    getName(umlAttribute.getType())
                )
            );
        }

        // openMDX uses attribute stereotype to indicate multiplicity
        // this allows to use multiplicities like set, list, ...
        attributeDef.values("multiplicity").add(
            umlAttribute.getStereotypes().size() > 0
            ? umlAttribute.getStereotypes().iterator().next()
                : umlAttribute.getMultiplicityRange() != null
                ? this.toMOFMultiplicity(umlAttribute.getMultiplicityRange())
                    : DEFAULT_ATTRIBUTE_MULTIPLICITY
        );
        attributeDef.values("scope").add(ScopeKind.INSTANCE_LEVEL);
        attributeDef.values("isDerived").add(new Boolean(isDerived));
        attributeDef.values("isChangeable").add(new Boolean(isChangeable));

        // annotation
        String annotation = this.getAnnotation(umlAttribute);
        if (annotation.length() != 0)
        {
            attributeDef.values("annotation").add(annotation);
        }

        SysLog.info("attribute=" + attributeDef);
        createModelElement(null, attributeDef);
    }

    //---------------------------------------------------------------------------
    private void processOperation(
        UML1Operation umlOperation,
        DataproviderObject aContainer
    ) throws Exception {
        SysLog.info("Processing operation " + umlOperation.getName());

        String operationName = umlOperation.getName();

//      Path containerPath = toElementPath(
//      nameToPathComponent(getScope(getScope(umlOperation.getQualifiedName()))),
//      getName(getScope(umlOperation.getQualifiedName()))
//      ); 
//      DataproviderObject operationDef = new DataproviderObject(
//      new Path(containerPath.toString() + "::" + umlOperation.getName())
//      );
        DataproviderObject operationDef = new DataproviderObject(
            new Path(aContainer.path().toString() + "::" + umlOperation.getName())
        );

        // object_class
        operationDef.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.OPERATION
        );

        // container
        operationDef.values("container").add(aContainer.path());

        // stereotype
        boolean isException = false;
        if(umlOperation.getStereotypes().size() > 0) {
            operationDef.values("stereotype").addAll(umlOperation.getStereotypes());

            // well-known stereotype <<exception>>
            if(operationDef.values("stereotype").contains(Stereotypes.EXCEPTION)) {
                operationDef.clearValues(SystemAttributes.OBJECT_CLASS).add(
                    ModelAttributes.EXCEPTION
                );
                operationDef.clearValues("stereotype");
                isException = true;
            }
        }

        // annotation
        String annotation = this.getAnnotation(umlOperation);
        if (annotation.length() != 0)
        {
            operationDef.values("annotation").add(annotation);
        }

        // visibility
        operationDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

        // scope
        operationDef.values("scope").add(ScopeKind.INSTANCE_LEVEL);

        // isQuery
        operationDef.values("isQuery").add(
            new Boolean(umlOperation.isQuery())
        );

        // parameters
        List parameters = umlOperation.getParametersWithoutReturnParameter();

        if(parameters.size() > 0)
        {

            /**
             * In SPICE all operations have excatly one parameter with name 'in'. The importer
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

            DataproviderObject parameterType = new DataproviderObject(
                new Path(
                    aContainer.path().toString() + capOperationName + "Params"
                )
            );
            parameterType.values(SystemAttributes.OBJECT_CLASS).add(
                ModelAttributes.STRUCTURE_TYPE
            );
            parameterType.values("visibility").add(VisibilityKind.PUBLIC_VIS);
            parameterType.values("isAbstract").add(new Boolean(false));
            parameterType.values("isSingleton").add(new Boolean(false));
            parameterType.values("container").addAll(
                aContainer.values("container")
            );

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

                DataproviderObject parameterDef = this.processParameter(
                    aParameter,
                    parameterType
                );

                /**
                 * Case 2: Parameter with name 'in'. Create object as PARAMETER.
                 */
                String fullQualifiedParameterName = parameterDef.path().getBase();
                if("in".equals(fullQualifiedParameterName.substring(fullQualifiedParameterName.lastIndexOf(':') + 1))) {

                    // 'in' is the only allowed parameter
                    if(parametersCreated) {
                        SysLog.error("Parameter format must be [p0:T0...pn:Tn | in:T], where T must be a class with stereotype " + Stereotypes.STRUCT);
                        throw new ServiceException(
                            ModelExceptions.MODEL_DOMAIN,
                            ModelExceptions.INVALID_PARAMETER_DECLARATION,
                            "Parameter format must be [p0:T0...pn:Tn | in:T], where T must be a class with stereotype " + Stereotypes.STRUCT,
                            new BasicException.Parameter("operation", operationDef.path().toString())
                        );
                    }
                    parameterType = new DataproviderObject(
                        (Path)parameterDef.values("type").get(0)
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
                            new BasicException.Parameter("operation", operationDef.path().toString())
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
            DataproviderObject inParameterDef = new DataproviderObject(
                new Path(
                    operationDef.path().toString() + "::in"
                )
            );
            inParameterDef.values(SystemAttributes.OBJECT_CLASS).add(
                ModelAttributes.PARAMETER
            );
            inParameterDef.values("container").add(
                operationDef.path()
            );
            inParameterDef.values("direction").add(
                DirectionKind.IN_DIR
            );
            inParameterDef.values("multiplicity").add(
                "1..1"
            );
            inParameterDef.values("type").add(
                parameterType.path()
            );
            this.createModelElement(
                null,
                inParameterDef
            );
        }

        // void in-parameter
        else {
            DataproviderObject inParameterDef = new DataproviderObject(
                new Path(
                    operationDef.path().toString() + "::in"
                )
            );
            inParameterDef.values(SystemAttributes.OBJECT_CLASS).add(
                ModelAttributes.PARAMETER
            );
            inParameterDef.values("container").add(
                operationDef.path()
            );
            inParameterDef.values("direction").add(
                DirectionKind.IN_DIR
            );
            inParameterDef.values("multiplicity").add(
                "1..1"
            );
            inParameterDef.values("type").add(
                toElementPath(
                    nameToPathComponent("org::openmdx::base"),
                    "Void"
                )
            );
            this.createModelElement(
                null,
                inParameterDef
            );
        }

        // Note:
        // return parameter is ignored for exceptions (operations with stereotype
        // exception)
        if(!isException) {
            DataproviderObject resultDef = new DataproviderObject(
                new Path(
                    operationDef.path().toString() + "::result"
                )
            );
            resultDef.values(SystemAttributes.OBJECT_CLASS).add(
                ModelAttributes.PARAMETER
            );
            resultDef.values("container").add(
                operationDef.path()
            );
            resultDef.values("direction").add(
                DirectionKind.RETURN_DIR
            );
            resultDef.values("multiplicity").add(
                "1..1"
            );

            if(umlOperation.getReturnParameter().getType() == null) {
                this.error("Undefined return type for operation " + umlOperation.getQualifiedName());
            }
            else {
                resultDef.values("type").add(
                    toElementPath(
                        nameToPathComponent(getScope(umlOperation.getReturnParameter().getType())),
                        getName(umlOperation.getReturnParameter().getType())
                    )
                );
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
                        operationDef.values("exception").add(
                            new Path(
                                toElementPath(nameToPathComponent(scope), name).toString() + "::" +
                                getName(qualifiedExceptionName)
                            )
                        );
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
    private DataproviderObject processParameter(
        UML1Parameter umlParameter,
        DataproviderObject parameterType
    ) throws Exception {
        SysLog.info("Processing parameter " + umlParameter.getName());

        DataproviderObject parameterDef = new DataproviderObject(
            new Path(
                parameterType.path().toString() + "::" + umlParameter.getName()
            )
        );

        // object_class
        parameterDef.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.STRUCTURE_FIELD
        );

        // container
        parameterDef.values("container").add(
            parameterType.path()
        );

        parameterDef.values("maxLength").add(new Integer(DEFAULT_PARAMETER_MAX_LENGTH));

        if(umlParameter.getType() == null) {
            this.error("Undefined type for parameter " + umlParameter.getQualifiedName());
        }
        else {
            parameterDef.values("type").add(
                toElementPath(
                    nameToPathComponent(getScope(umlParameter.getType())),
                    getName(umlParameter.getType())
                )
            );
            parameterDef.values("multiplicity").add(
                umlParameter.getStereotypes().size() > 0 ?
                    umlParameter.getStereotypes().iterator().next() :
                        DEFAULT_PARAMETER_MULTIPLICITY
            );
        }

        return parameterDef;

    }

    //---------------------------------------------------------------------------
    public void processUMLDataType(
        UML1DataType umlDataType
    ) throws Exception {
        SysLog.info("Processing primitive type " + umlDataType.getQualifiedName());

        // ModelPrimitiveType object
        DataproviderObject primitiveTypeDef = new DataproviderObject(
            toElementPath(
                nameToPathComponent(getScope(umlDataType.getQualifiedName())),
                umlDataType.getName()
            )
        );

        // object_class
        primitiveTypeDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.PRIMITIVE_TYPE);

        // container
        primitiveTypeDef.values("container").add(
            toElementPath(
                nameToPathComponent(getScope(umlDataType.getQualifiedName())),
                getName(getScope(umlDataType.getQualifiedName()))
            )
        );

        /**
         * skip stereotype because its value 'Primitive'
         * was marked to note the difference between
         * ordinary classes and primitive types
         */

        // annotation
        String annotation = this.getAnnotation(umlDataType);
        if (annotation.length() != 0)
        {
            primitiveTypeDef.values("annotation").add(annotation);
        }

        // isAbstract attribute
        primitiveTypeDef.values("isAbstract").add(
            new Boolean(umlDataType.isAbstract())
        );

        // isSingleton attribute
        primitiveTypeDef.values("isSingleton").add(
            new Boolean(false)
        );

        // visibility attribute
        primitiveTypeDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

        SysLog.info("primitive type=" + primitiveTypeDef);
        createModelElement(null, primitiveTypeDef);
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
            "-1".equals(range.getUpper()) ? "n" : range.getUpper()
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
    ) throws ServiceException {
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
    public static final String THROWS_EXCEPTION_PREFIX = "@throws";

    private PrintStream infos = null;
    private PrintStream errors = null;
    private PrintStream warnings = null;
    private final URL modelUrl;
    private final short xmiFormat;
    private final Map pathMap;


}

//--- End of File -----------------------------------------------------------
