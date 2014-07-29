/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: ModelImporter_1 class
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
package org.openmdx.application.mof.externalizer.spi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.omg.mof.cci.ScopeKind;
import org.omg.mof.cci.VisibilityKind;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderRequestProcessor;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.application.mof.cci.ModelExceptions;
import org.openmdx.application.mof.externalizer.cci.ModelImporter_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Common functions for model importers.
 */
@SuppressWarnings({"rawtypes","unchecked"})
abstract public class ModelImporter_1
implements ModelImporter_1_0 {

    protected class Qualifier
    {
        private String name = null;
        private String type = null;

        public Qualifier(String name, String type)
        {
            this.name = name;
            this.type = type;
        }

        public String getName() { return this.name; }

        public String getType() { return this.type; }
    }

    /**
     * The safe way to create a feature path
     * 
     * @param classPath
     * @param featureName
     * 
     * @return a new feature path
     */
    protected static Path newFeaturePath(
        Path classPath,
        String featureName
    ){
        return classPath.getParent().getChild(
            classPath.getLastComponent().getChild(featureName)
        );
    }
        
    //---------------------------------------------------------------------------
    private DataproviderRequestProcessor getChannel(
    ) throws ServiceException {
        if(this.channel == null) {
            this.channel = new DataproviderRequestProcessor(
                this.header,
                this.target
            );
        }
        return channel;
    }

    //---------------------------------------------------------------------------
    protected void createModelElement(
        List scope,
        MappedRecord object
    ) throws ServiceException {
        if(scope == null || ((scope.size() >= 2) && "Logical View".equals(scope.get(0)))) {
            // create segment on-demand
            String modelName = Object_2Facade.getPath(object).get(4);
            if(!this.segments.contains(modelName)) {
                MappedRecord segment;
                segment = Facades.newObject(
				    Object_2Facade.getPath(object).getPrefix(5),
				    "org:omg:model1:Segment"
				).getDelegate();
                this.getChannel().addCreateRequest(
                    segment
                );
                this.segments.add(modelName);
            }
            if(ModelAttributes.PACKAGE.equals(Object_2Facade.getObjectClass(object))) {
                try {
                    this.getChannel().addGetRequest(
                        Object_2Facade.getPath(object)
                    );
                    this.getChannel().addReplaceRequest(
                        object,
                        AttributeSelectors.NO_ATTRIBUTES,
                        null
                    );
                }
                catch(ServiceException e) {
                    if(e.getExceptionCode() != BasicException.Code.NOT_FOUND) {
                        throw e;
                    }
                    this.getChannel().addCreateRequest(
                        object,
                        AttributeSelectors.NO_ATTRIBUTES,
                        null
                    );                    
                }
            }
            else {
                try {
                    this.getChannel().addCreateRequest(
                        object,
                        AttributeSelectors.NO_ATTRIBUTES,
                        null
                    );
                }
                catch(ServiceException e) {
                    if(e.getCause().getExceptionCode() == BasicException.Code.DUPLICATE) {
                        // DUPLICATE exception must be caught here, otherwise the 
                        // TogetherExporterPlugin runs into problems (AssociationEnds)
                        // => the duplicate defintion of an object is ignored
                        SysLog.warning("[MOF C-5] The names of the contents of a Namespace must not collide. element=", object);
                    }
                    else {
                        throw e;
                    }
                }
            }
        }
    }

    //---------------------------------------------------------------------------
    protected void beginImport(
    ) throws ServiceException {

        SysLog.trace("clearing repository before import");

        // clear repository
        try {
            SysLog.trace("removing all elements");
            this.getChannel().addRemoveRequest(
                new Path("xri:@openmdx:org.omg.model1/provider/" + this.providerName)
            );
            this.endImport();
        } catch(ServiceException e) {
            if (e.getExceptionCode() != BasicException.Code.NOT_FOUND) {
                e.log();
            }
        }
        // beginImport clears all pending requests and prepares for
        // a new import    
        try {
            MessageRecord request = (MessageRecord) Records.getRecordFactory().createMappedRecord(MessageRecord.NAME);
            request.setPath(PROVIDER_ROOT_PATH.getDescendant("segment", "-", "beginImport"));
            request.setBody(null);
            this.getChannel().addOperationRequest(request);
            this.segments = new HashSet();
            // create repository root
            this.getChannel().addCreateRequest(
                Object_2Facade.newInstance(
                    new Path("xri:@openmdx:org.omg.model1/provider/" + this.providerName),
                    "org:openmdx:base:Provider"
                ).getDelegate()
            );
        } catch (ResourceException e) {
            throw new ServiceException(e);
        }
    }

    //---------------------------------------------------------------------------
    protected void endImport(
    ) throws ServiceException {
        try {
            MessageRecord request = (MessageRecord) Records.getRecordFactory().createMappedRecord(MessageRecord.NAME);
            request.setPath(PROVIDER_ROOT_PATH.getDescendant("segment", "-", "endImport"));
            request.setBody(null);
            this.getChannel().addOperationRequest(request);
        } catch (ResourceException e) {
            throw new ServiceException(e);
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Convert a string of the form 
     * 'ch::omex::address1'
     * to a string of the form 
     * 'ch:omex:address1'
     *
     */
    protected String nameToPathComponent(
        String name
    ) {
        if(name.length() == 0) {
            return new String();
        } else {
            StringBuilder component = new StringBuilder();
            int i = 0;
            while(i < name.length()-1) {
                if("::".equals(name.substring(i,i+2))) {
                    component.append(":");
                    i++;
                }
                else {
                    component.append(name.charAt(i));
                }
                i++;
            }
            return component.append(name.charAt(i)).toString();
        }
    }

    //---------------------------------------------------------------------------
    protected Path toElementPath(
        String modelName,
        String elementName
    ) throws ServiceException {
        if((modelName == null) || (modelName.length() == 0)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "element not in namespace. Top level elements not supported.",
                new BasicException.Parameter("element name", elementName)
            );
        }
        return new Path(
            new String[]{
                "org:omg:model1",
                "provider",
                this.providerName,
                "segment",
                modelName,
                "element",
                modelName + ":" + elementName
            }
        );
    }

    //---------------------------------------------------------------------------
    protected Set parseStereotype(
        String s
    ) {
        Set stereotypes = new HashSet();
        StringTokenizer tokenizer = new StringTokenizer(s, ", ");
        while(tokenizer.hasMoreTokens()) {
            stereotypes.add(tokenizer.nextToken());
        }
        return stereotypes;
    }

    //---------------------------------------------------------------------------
    /**
     * Parses a multiplicity text of the following form:
     * [["<<"] lowerBound [".." upperBound ] [">>"] BLANK ] string
     * When no upper bound is specified then upperBound = lowerBound.
     */
    protected String parseMultiplicity(
        String text,
        MappedRecord container,
        String elementName,
        StringBuffer multiplicity
    ) throws ServiceException {
        for(Multiplicity candidate : Multiplicity.values()){
            String value = candidate.toString();
            if(value.equals(text)) {
                multiplicity.append(value);
                return new String();
            }
        }
        if(ModelHelper.UNBOUND.equals(text)) {
            multiplicity.append(ModelHelper.UNBOUND);
            return new String();
        }

        String lowerBound = null;
        String upperBound = null;

        int stereotypeOpeningPos = text.indexOf("<<");
        int stereotypeClosingPos = text.indexOf(">>");
        int lastPos = text.indexOf(" ");
        int delimiterPos = text.indexOf("..");

        // if text does not contain a multiplicity return 1..1
        if((delimiterPos < 0) && (stereotypeOpeningPos < 0)) {
            multiplicity.append(Multiplicity.SINGLE_VALUE.toString());
            return text;
        }

        // single value or range?
        if(delimiterPos < 0) {
            lowerBound = text.substring(
                stereotypeOpeningPos < 0 ? 0 : stereotypeOpeningPos + 2,
                    stereotypeClosingPos < 0 ? (lastPos < 0 ? text.length() : lastPos) : stereotypeClosingPos
            ).trim();
            upperBound = lowerBound;
            Multiplicity l = ModelHelper.toMultiplicity(lowerBound);
            if(l != null) {
            	switch(l){
            		case LIST:
                        multiplicity.append(Multiplicity.LIST.toString());
                        return text.substring(lastPos).trim();
            		case SET:
                        multiplicity.append(Multiplicity.SET.toString());
                        return text.substring(lastPos).trim();
            		case SPARSEARRAY:
                        multiplicity.append(Multiplicity.SPARSEARRAY.toString());
                        return text.substring(lastPos).trim();
            		case STREAM:	
                        multiplicity.append(Multiplicity.STREAM.toString());
                        return text.substring(lastPos).trim();
                    case MAP: case OPTIONAL: case SINGLE_VALUE:
                        break; // TODO
            	}
            }
        } else {
            lowerBound = text.substring(
                stereotypeOpeningPos < 0 ? 0 : stereotypeOpeningPos + 2,
                    delimiterPos
            );
            upperBound = text.substring(
                delimiterPos + 2,
                stereotypeClosingPos < 0 ? (lastPos < 0 ? text.length() : lastPos) : stereotypeClosingPos
            );
        }

        // check lowerBound, upperBound
        try {
            Integer.decode(lowerBound);

            // check upperBound
            if(!"n".equals(upperBound)) {
                Integer.decode(upperBound);
            }
        } catch(NumberFormatException e) {
            SysLog.error("multiplicity must be of the form [\"<<\"] lowerBound [\"..\" upperBound ] [\">>\"]");
            throw new ServiceException(
                ModelExceptions.MODEL_DOMAIN,
                ModelExceptions.INVALID_MULTIPLICITY_FORMAT,
                "multiplicity must be of the form [\"<<\"] lowerBound [\"..\" upperBound ] [\">>\"]",
                new BasicException.Parameter("multiplicity", text),
                new BasicException.Parameter("lowerBound", lowerBound),
                new BasicException.Parameter("upperBound", upperBound),
                new BasicException.Parameter("container", Object_2Facade.getPath(container)),
                new BasicException.Parameter("element", elementName)
            );
        }
        multiplicity.append(
            lowerBound + ".." + upperBound
        );
        return text.substring(
            lastPos < 0 ? text.length() : lastPos
        ).trim();
    }

    //---------------------------------------------------------------------------
    /**
     * Create a reference for all navigable association ends.
     */
    protected void exportAssociationEndAsReference(
        MappedRecord associationEndDef1,
        MappedRecord associationEndDef2,
        MappedRecord associationDef,
        List scope
    ) throws ServiceException {
        try {
            Object_2Facade associationEndDef1Facade = Facades.asObject(associationEndDef1);
            Object_2Facade associationEndDef2Facade = Facades.asObject(associationEndDef2);        
            if(
                ((Boolean)associationEndDef2Facade.attributeValue("isNavigable")).booleanValue()
            ) {
                Object_2Facade referenceDefFacade = Object_2Facade.newInstance(
                    newFeaturePath(
                        ((Path)associationEndDef1Facade.attributeValue("type")),
                        (String)associationEndDef2Facade.attributeValue("name")
                    ),
                    ModelAttributes.REFERENCE
                );
                referenceDefFacade.addAllToAttributeValuesAsList("container",
                    associationEndDef1Facade.getAttributeValuesAsReadOnlyList("type")
                );
                referenceDefFacade.addAllToAttributeValuesAsList("type",
                    associationEndDef2Facade.getAttributeValuesAsReadOnlyList("type")
                );
                referenceDefFacade.addToAttributeValuesAsList("referencedEnd",
                    associationEndDef2Facade.getPath()
                );
                referenceDefFacade.addToAttributeValuesAsList("exposedEnd",
                    associationEndDef1Facade.getPath()
                );
                referenceDefFacade.addAllToAttributeValuesAsList("multiplicity",
                    associationEndDef2Facade.getAttributeValuesAsReadOnlyList("multiplicity")
                );
                referenceDefFacade.addAllToAttributeValuesAsList("isChangeable",
                    associationEndDef2Facade.getAttributeValuesAsReadOnlyList("isChangeable")
                );
                referenceDefFacade.addToAttributeValuesAsList("visibility",
                    VisibilityKind.PUBLIC_VIS
                );
                referenceDefFacade.addToAttributeValuesAsList("scope",
                    ScopeKind.INSTANCE_LEVEL
                );
                createModelElement(
                    scope,
                    referenceDefFacade.getDelegate()
                );
            }
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Verifies/completes association ends
     */
    protected void verifyAndCompleteAssociationEnds(
        MappedRecord associationEndDef1,
        MappedRecord associationEndDef2
    ) throws ServiceException {
        try {
            Object_2Facade associationEndDef1Facade = Facades.asObject(associationEndDef1);
            Object_2Facade associationEndDef2Facade = Facades.asObject(associationEndDef2);
            // end1.aggregation=COMPOSITE --> end2.isChangeable=false
            if(AggregationKind.COMPOSITE.equals(associationEndDef1Facade.attributeValue("aggregation"))) {
                associationEndDef2Facade.replaceAttributeValuesAsListBySingleton(
                    "isChangeable",
                    Boolean.FALSE
                );
            }
            if(AggregationKind.COMPOSITE.equals(associationEndDef2Facade.attributeValue("aggregation"))) {
                associationEndDef1Facade.replaceAttributeValuesAsListBySingleton(
                    "isChangeable",
                    Boolean.FALSE
                );
            }
            // end1.aggregation=COMPOSITE --> end2.aggregation=AggregationKind.NONE
            if(
                (AggregationKind.COMPOSITE.equals(associationEndDef1Facade.attributeValue("aggregation")) && !AggregationKind.NONE.equals(associationEndDef2Facade.getSingletonFromAttributeValuesAsList("aggregation"))) ||
                (AggregationKind.COMPOSITE.equals(associationEndDef2Facade.attributeValue("aggregation")) && !AggregationKind.NONE.equals(associationEndDef1Facade.getSingletonFromAttributeValuesAsList("aggregation")))
            ) {
                SysLog.error("Wrong aggregation. end1.aggregation='composite' --> end2.aggregation='none'");
                throw new ServiceException(
                    ModelExceptions.MODEL_DOMAIN,
                    ModelExceptions.INVALID_OPPOSITE_AGGREGATION_FOR_COMPOSITE_AGGREGATION,
                    "Wrong aggregation. end1.aggregation='composite' --> end2.aggregation='none'",
                    new BasicException.Parameter("end1", associationEndDef1Facade.getPath()),
                    new BasicException.Parameter("end1.aggregation", associationEndDef1Facade.attributeValue("aggregation")),
                    new BasicException.Parameter("end2", associationEndDef2Facade.getPath()),
                    new BasicException.Parameter("end2.aggregation", associationEndDef2Facade.attributeValue("aggregation"))
                );
            }
            // end1.aggregation=COMPOSITE --> end2.multiplicity="1..1"
            if(
                (AggregationKind.COMPOSITE.equals(associationEndDef1Facade.attributeValue("aggregation")) && !("1..1".equals(associationEndDef2Facade.getSingletonFromAttributeValuesAsList("multiplicity")) || "0..1".equals(associationEndDef2Facade.getSingletonFromAttributeValuesAsList("multiplicity")))) ||
                (AggregationKind.COMPOSITE.equals(associationEndDef2Facade.attributeValue("aggregation")) && !("1..1".equals(associationEndDef1Facade.getSingletonFromAttributeValuesAsList("multiplicity")) || "0..1".equals(associationEndDef1Facade.getSingletonFromAttributeValuesAsList("multiplicity"))))
            ) {
                SysLog.error("Wrong multiplicity. end1.aggregation='composite' --> end2.multiplicity='1..1'|'0..1'");
                throw new ServiceException(
                    ModelExceptions.MODEL_DOMAIN,
                    ModelExceptions.INVALID_MULTIPLICITY_FOR_COMPOSITE_AGGREGATION,
                    "Wrong multiplicity. end1.aggregation='composite' --> end2.multiplicity='1..1'|'0..1'",
                    new BasicException.Parameter("end1", associationEndDef1Facade.getPath()),
                    new BasicException.Parameter("end1.multiplicity", associationEndDef1Facade.attributeValue("multiplicity")),
                    new BasicException.Parameter("end1.aggregation", associationEndDef1Facade.attributeValue("aggregation")),
                    new BasicException.Parameter("end2", associationEndDef2Facade.getPath()),
                    new BasicException.Parameter("end2.multiplicity", associationEndDef2Facade.attributeValue("multiplicity")),
                    new BasicException.Parameter("end2.aggregation", associationEndDef2Facade.attributeValue("aggregation"))
                );
            }
            this.verifyNavigabilityOfCompositeAggregation(associationEndDef1);
            this.verifyNavigabilityOfCompositeAggregation(associationEndDef2);
            this.checkUnnecessaryQualifiers(associationEndDef1);
            this.checkUnnecessaryQualifiers(associationEndDef2);
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Verifies whether an association end with aggregation 'composite' is navigable
     */
    private void verifyNavigabilityOfCompositeAggregation(
        MappedRecord associationEndDef
    ) throws ServiceException {
        Object_2Facade associationEndDefFacade = Facades.asObject(associationEndDef);
        if(
            (AggregationKind.COMPOSITE.equals(associationEndDefFacade.attributeValue("aggregation")) ||
                AggregationKind.SHARED.equals(associationEndDefFacade.attributeValue("aggregation")))
                &&
                !((Boolean)associationEndDefFacade.attributeValue("isNavigable")).booleanValue()
        ) {
            SysLog.error("An association end with aggregation 'composite' should be navigable.");
            throw new ServiceException(
                ModelExceptions.MODEL_DOMAIN,
                ModelExceptions.COMPOSITE_AGGREGATION_NOT_NAVIGABLE,
                "An association end with aggregation 'composite' should be navigable.",
                new BasicException.Parameter("end", associationEndDefFacade.getPath())
            );
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Checks for unnecessary qualifiers, only navigable associations need a 
     * unique identifying qualifier
     */
    private void checkUnnecessaryQualifiers(
        MappedRecord associationEndDef
    ) throws ServiceException {
        Object_2Facade associationEndDefFacade = Facades.asObject(associationEndDef);
        if(
            (associationEndDefFacade.getSizeOfAttributeValuesAsList("qualifierName") > 0) &&
            !((Boolean)associationEndDefFacade.attributeValue("isNavigable")).booleanValue()
        ) {
            SysLog.error("Found association end with qualifier which is not navigable. Only navigable association ends need a unique identifying qualifier.");
            throw new ServiceException(
                ModelExceptions.MODEL_DOMAIN,
                ModelExceptions.UNNECESSARY_QUALIFIER_FOUND,
                "Found association end with qualifier which is not navigable. Only navigable association ends need a unique identifying qualifier.",
                new BasicException.Parameter("end", associationEndDefFacade.getPath())
            );
        }
    }

    //---------------------------------------------------------------------------
    protected void verifyAliasAttributeNumber(
        MappedRecord aliasTypeDef,
        int nAttributes
    ) throws ServiceException {
        if (nAttributes != 1)
        {
            SysLog.error("an alias type must specify exactly one attribute");
            throw new ServiceException(
                ModelExceptions.MODEL_DOMAIN,
                ModelExceptions.ALIAS_TYPE_REQUIRES_EXACTLY_ONE_ATTRIBUTE,
                "an alias type must specify exactly one attribute",
                new BasicException.Parameter("alias type", aliasTypeDef)
            );
        }
    }

    //---------------------------------------------------------------------------
    protected void verifyAliasAttributeName(
        MappedRecord aliasTypeDef,
        String attributeName
    ) throws ServiceException {
        if (attributeName.indexOf("::") == -1) {
            SysLog.error("the name of the single attribute of the alias type must be a qualified type name");
            throw new ServiceException(
                ModelExceptions.MODEL_DOMAIN,
                ModelExceptions.INVALID_ALIAS_ATTRIBUTE_NAME,
                "the name of the single attribute of the alias type must be a qualified type name",
                new BasicException.Parameter("alias type", aliasTypeDef),
                new BasicException.Parameter("attribute name", attributeName)
            );
        }
    }

    //---------------------------------------------------------------------------
    protected void verifyAssociationName(
        String associationName
    ) throws ServiceException {
        if (associationName == null || associationName.length() == 0)
        {
            SysLog.error("the name of an association cannot be empty");
            throw new ServiceException(
                ModelExceptions.MODEL_DOMAIN,
                ModelExceptions.ASSOCIATION_NAME_IS_EMPTY,
                "the name of an association cannot be empty"
            );
        }
    }

    //---------------------------------------------------------------------------
    protected void verifyAssociationEndName(
        MappedRecord associationDef,
        String associationEndName
    ) throws ServiceException {
        if(associationEndName == null || associationEndName.length() == 0) {
            SysLog.error("the name of an association end cannot be empty");
            throw new ServiceException(
                ModelExceptions.MODEL_DOMAIN,
                ModelExceptions.ASSOCIATION_END_NAME_IS_EMPTY,
                "the name of an association end cannot be empty",
                new BasicException.Parameter("association", associationDef)
            );
        }
    }

    //---------------------------------------------------------------------------
    /**
     * parse strings with the following EBNF syntax 
     * [ qualifierAttribute ':' qualifierType ] { ';' qualifierAttribute ':' qualifierType }
     */
    protected List parseAssociationEndQualifierAttributes(
        String _qualifierText
    ) throws ServiceException {
        List qualifierAttributes = new ArrayList();

        /**
         * qualifier types are in MOF syntax (with '::'). Since the standard
         * Java StringTokenizer cannot distinguish between ':' that separates
         * an attribute name from its attribute type and a '::' that is used 
         * to qualify attribute types, the input text is first converted from
         * MOF to Java syntax ('.' instead of '::'). This makes the lexical
         * analysis a lot easier and avoids having to write a dedicated tokenizer
         * that can handle the problem mentioned above.
         */
        String qualifierText = qualifiedNameToJava(_qualifierText);

        StringTokenizer tokenizer = new StringTokenizer(qualifierText.trim(), ":; \t", true);

        if(tokenizer.hasMoreTokens()) {
            String qualifierName = getNextToken(tokenizer, qualifierText);
            String delim = getNextToken(tokenizer, qualifierText);
            if(!delim.equals(":")) {
                SysLog.error("syntax error in qualifier declaration: missing ':'");
                throw new ServiceException(
                    ModelExceptions.MODEL_DOMAIN,
                    ModelExceptions.MISSING_COLON_IN_QUALIFIER_DECLARATION,
                    "syntax error in qualifier declaration: missing ':'",
                    new BasicException.Parameter("qualifier text", qualifierText)
                );
            }
            String qualifierType = javaToQualifiedName(getNextToken(tokenizer, qualifierText));
            Qualifier qualifier = new Qualifier(qualifierName, qualifierType);
            qualifierAttributes.add(qualifier);

            if(tokenizer.hasMoreTokens()) {
                delim = getNextToken(tokenizer, qualifierText);
                if (!delim.equals(";")) {
                    SysLog.error("syntax error in qualifier declaration: qualifier expressions must be separated by ';'");
                    throw new ServiceException(
                        ModelExceptions.MODEL_DOMAIN,
                        ModelExceptions.MISSING_SEMICOLON_IN_QUALIFIER_DECLARATION,
                        "syntax error in qualifier declaration: qualifier expressions must be separated by ';'",
                        new BasicException.Parameter("qualifier text", qualifierText)
                    );
                }
                while (delim.equals(";")) {
                    qualifierName = getNextToken(tokenizer, qualifierText);
                    delim = getNextToken(tokenizer, qualifierText);
                    if (!delim.equals(":")) {
                        SysLog.error("syntax error in qualifier declaration: missing ':'");
                        throw new ServiceException(
                            ModelExceptions.MODEL_DOMAIN,
                            ModelExceptions.MISSING_COLON_IN_QUALIFIER_DECLARATION,
                            "syntax error in qualifier declaration: missing ':'",
                            new BasicException.Parameter("qualifier text", qualifierText)
                        );
                    }
                    qualifierType = javaToQualifiedName(getNextToken(tokenizer, qualifierText));
                    qualifier = new Qualifier(qualifierName, qualifierType);
                    qualifierAttributes.add(qualifier);
                    delim = getNextToken(tokenizer, qualifierText);
                }
            }
        }
        return qualifierAttributes;
    }

    //---------------------------------------------------------------------------
    private String getNextToken(
        StringTokenizer tokenizer,
        String qualifierText
    ) throws ServiceException {
        String nextToken = new String();
        if(tokenizer.hasMoreTokens()) {
            nextToken = tokenizer.nextToken();
            // skip spaces and tabs
            while (tokenizer.hasMoreTokens() && ( nextToken.equals(" ") || nextToken.equals("\t") )) {
                nextToken = tokenizer.nextToken();
            }
            if(nextToken.equals(" ") || nextToken.equals("\t")) {
                SysLog.error("syntax error in qualifier declaration: unexpected end of expression");
                throw new ServiceException(
                    ModelExceptions.MODEL_DOMAIN,
                    ModelExceptions.UNEXPECTED_END_OF_QUALIFIER_DECLARATION,
                    "syntax error in qualifier declaration: unexpected end of expression",
                    new BasicException.Parameter("qualifier text", qualifierText)
                );
            }
        }
        return nextToken;
    }

    //---------------------------------------------------------------------------
    /**
     * convert qualified names for classes, packages, ... in MOF syntax
     * (e.g. org::openmdx::example::MyClass) into Java syntax (e.g. org.openmdx.example.MyClass)
     */
    private String javaToQualifiedName(
        String javaQualifiedName
    ) {
        String qualifiedName = new String();
        for(
                int i = 0;
                i < javaQualifiedName.length();
                i++
        ) {
            char ch = javaQualifiedName.charAt(i);
            if(ch == '.') {
                qualifiedName = qualifiedName + "::";
            }
            else {
                qualifiedName = qualifiedName + ch;
            }
        }
        return qualifiedName;
    }

    //---------------------------------------------------------------------------
    /**
     * convert qualified names for classes, packages, ... in MOF syntax
     * (e.g. org::openmdx::example::MyClass) into Java syntax (e.g. org.openmdx.example.MyClass)
     */
    private String qualifiedNameToJava(
        String qualifiedName
    ) {
        String javaQualifiedName = new String();
        for(
                int i = 0;
                i < qualifiedName.length();
                i++
        ) {
            char ch = qualifiedName.charAt(i);
            if(ch == ':') {
                if((i+1 < qualifiedName.length()) && (qualifiedName.charAt(i+1) == ':')) {
                    javaQualifiedName = javaQualifiedName + '.';
                    i++;
                }
                else {
                    javaQualifiedName = javaQualifiedName + ':';
                }
            }
            else {
                javaQualifiedName = javaQualifiedName + ch;
            }
        }
        return javaQualifiedName;
    }

    //---------------------------------------------------------------------------
    /**
     * Takes a qualified name and retrieves all name components except the last
     * name component, i.e. the scope of the given qualified name. If the given
     * name is not qualified, return an empty string, i.e. an empty scope. 
     */
    public String getScope(
        String qualifiedName
    ) {
        return (
                qualifiedName.lastIndexOf("::") == -1 ?
                    new String() :
                        qualifiedName.substring(0, qualifiedName.lastIndexOf("::"))
        );
    }

    //---------------------------------------------------------------------------
    /**
     * Retrieves the last name component of a given qualified name. If the given
     * name is not qualified (i.e. has no scope), return the whole given name.
     */
    public String getName(
        String qualifiedName
    ) {
        return (
                qualifiedName.lastIndexOf("::") == -1 ?
                    qualifiedName :
                        qualifiedName.substring(qualifiedName.lastIndexOf("::")+2)
        );
    }

    //---------------------------------------------------------------------------
    // Constants and Variables
    //---------------------------------------------------------------------------
    protected static final String DEFAULT_PARAMETER_MULTIPLICITY = "1..1";
    protected static final int DEFAULT_PARAMETER_MAX_LENGTH = 1000000;

    protected static final String DEFAULT_ATTRIBUTE_MULTIPLICITY = "1..1";
    protected static final int DEFAULT_ATTRIBUTE_MAX_LENGTH = 200;
    protected static final boolean DEFAULT_ATTRIBUTE_IS_UNIQUE = false;
    protected static final boolean DEFAULT_ATTRIBUTE_IS_LANGUAGE_NEUTRAL = true;

    static private final Path PROVIDER_ROOT_PATH = new Path("xri:@openmdx:org.omg.model1/provider/Mof");

    private DataproviderRequestProcessor channel = null;
    protected Set segments = null;
    protected ServiceHeader header = null;
    protected Dataprovider_1_0 target = null;
    protected String providerName = null;
    protected boolean hasErrors = false;

}

//--- End of File -----------------------------------------------------------
