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

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.omg.mof.cci.ScopeKind;
import org.omg.mof.cci.VisibilityKind;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.application.mof.cci.ModelExceptions;
import org.openmdx.application.mof.externalizer.cci.ModelImporter_1_0;
import org.openmdx.base.dataprovider.cci.Channel;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;

/**
 * Common functions for model importers.
 */
@SuppressWarnings({"rawtypes","unchecked"})
abstract public class ModelImporter_1 implements ModelImporter_1_0 {

	/**
	 * Constructor
	 */
    protected ModelImporter_1(
    ) {
		super();
	}

	protected Set segments = null;
    protected Channel channel = null;
    protected boolean hasErrors = false;

    protected static final String DEFAULT_PARAMETER_MULTIPLICITY = "1..1";
    protected static final int DEFAULT_PARAMETER_MAX_LENGTH = 1000000;
    
    protected static final String DEFAULT_ATTRIBUTE_MULTIPLICITY = "1..1";
    protected static final int DEFAULT_ATTRIBUTE_MAX_LENGTH = 200;
    protected static final boolean DEFAULT_ATTRIBUTE_IS_UNIQUE = false;
    protected static final boolean DEFAULT_ATTRIBUTE_IS_LANGUAGE_NEUTRAL = true;
    
    protected static final String PROVIDER_NAME = "Mof";
    private static final Path PROVIDER_ROOT_PATH = new Path(
    	"xri://@openmdx*org.omg.model1/provider"
    ).getChild(
    	PROVIDER_NAME
    );
    
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
    protected void createModelElement(
        List scope,
        ObjectRecord object
    ) throws ResourceException{
        if(scope == null || ((scope.size() >= 2) && "Logical View".equals(scope.get(0)))) {
            // create segment on-demand
            final Path objectId = object.getResourceIdentifier();
			final String modelName = objectId.getSegment(4).toClassicRepresentation();
            if(!this.segments.contains(modelName)) {
                final Path segmentId = objectId.getPrefix(5);
				ObjectRecord segment = this.channel.newObjectRecord(
            		segmentId,
				    "org:omg:model1:Segment"
				);
				this.channel.addCreateRequest(segment);
                this.segments.add(modelName);
            }
            final String objectType = object.getValue().getRecordName();
			if(ModelAttributes.PACKAGE.equals(objectType)) {
                try {
					if(this.channel.addGetRequest(objectId) != null) {
						this.channel.addUpdateRequest(object);
					} else {
	                    this.channel.addCreateRequest(object);
					}
                } catch(ResourceException e) {
                	// TODO for data provider stack 1 only!
                	final BasicException cause = BasicException.toExceptionStack(e);
                    if(cause.getExceptionCode() == BasicException.Code.NOT_FOUND) {
	                    this.channel.addCreateRequest(object);
                    } else {
                        throw e;
                    }
                }
            } else {
                try {
                    this.channel.addCreateRequest(
                        object
                    );
                } catch(ResourceException e) {
                	final BasicException cause = BasicException.toExceptionStack(e);
                    if(cause.getExceptionCode() == BasicException.Code.DUPLICATE) {
                        // DUPLICATE exception must be caught here, otherwise the 
                        // TogetherExporterPlugin runs into problems (AssociationEnds)
                        // => the duplicate defintion of an object is ignored
                        SysLog.warning("[MOF C-5] The names of the contents of a Namespace must not collide. element=", object);
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    //---------------------------------------------------------------------------
    protected void beginImport(
    ) throws ResourceException{

        SysLog.trace("clearing repository before import");

        // clear repository
        try {
            SysLog.trace("removing all elements");
            this.channel.addRemoveRequest(PROVIDER_ROOT_PATH);
            this.endImport();
        } catch(ResourceException e) {
            if (BasicException.toExceptionStack(e).getExceptionCode() != BasicException.Code.NOT_FOUND) {
                Throwables.log(e);
            }
        }
        // beginImport clears all pending requests and prepares for
        // a new import    
        this.channel.addOperationRequest(
        	this.channel.newMessageRecord(
       			PROVIDER_ROOT_PATH.getDescendant("segment", "-", "beginImport")
       		)
        );
        this.segments = new HashSet();
        // create repository root
        this.channel.addCreateRequest(
    		this.channel.newObjectRecord(
                PROVIDER_ROOT_PATH,
                "org:openmdx:base:Provider"
            )
        );
    }

    //---------------------------------------------------------------------------
    protected void endImport(
    ) throws ResourceException{
        this.channel.addOperationRequest(
        	this.channel.newMessageRecord(
       			PROVIDER_ROOT_PATH.getDescendant("segment", "-", "endImport")
       		)
        );
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
            return "";
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
    ) throws ResourceException {
        if((modelName == null) || (modelName.length() == 0)) {
        	throw ResourceExceptions.initHolder(
        		new NotSupportedException(
    				"element not in namespace. Top level elements not supported.",
        			BasicException.newEmbeddedExceptionStack(	
		                BasicException.Code.DEFAULT_DOMAIN,
		                BasicException.Code.NOT_SUPPORTED,
		                new BasicException.Parameter("element name", elementName)
		            )
		        )
            );
        }
        return PROVIDER_ROOT_PATH.getDescendant(
            "segment",
            modelName,
            "element",
            modelName + ":" + elementName
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
     * Create a reference for all navigable association ends.
     */
    protected void exportAssociationEndAsReference(
        ObjectRecord associationEndDef1,
        ObjectRecord associationEndDef2,
        ObjectRecord associationDef,
        List scope
    ) throws ResourceException {
        try {
            if(
                ((Boolean)DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef2, "isNavigable")).booleanValue()
            ) {
                ObjectRecord referenceDef = this.channel.newObjectRecord(
                    newFeaturePath(
                        ((Path)DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef1, "type")),
                        (String)DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef2, "name")
                    ),
                    ModelAttributes.REFERENCE
                );
                DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(referenceDef, "container", DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef1, "type"));
                DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(referenceDef, "type", DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef2, "type"));
                DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(referenceDef, "referencedEnd", associationEndDef2.getResourceIdentifier());
                DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(referenceDef, "exposedEnd", associationEndDef1.getResourceIdentifier());
                DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(referenceDef, "multiplicity", DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef2, "multiplicity"));
                DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(referenceDef, "isChangeable", DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef2, "isChangeable"));
                DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(referenceDef, "visibility", VisibilityKind.PUBLIC_VIS);
                DataproviderMode.DATAPROVIDER_2.addToAttributeValuesAsList(referenceDef, "scope", ScopeKind.INSTANCE_LEVEL);
                createModelElement(
                    scope,
                    referenceDef
                );
            }
        } catch(RuntimeException e) {
        	throw ResourceExceptions.toResourceException(e);
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Verifies/completes association ends
     */
    protected void verifyAndCompleteAssociationEnds(
        ObjectRecord associationEndDef1,
        ObjectRecord associationEndDef2
    ) throws ResourceException {
        try {
            // end1.aggregation=COMPOSITE --> end2.isChangeable=false
            if(AggregationKind.COMPOSITE.equals(DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef1, "aggregation"))) {
                DataproviderMode.DATAPROVIDER_2.replaceAttributeValuesAsListBySingleton(associationEndDef2, "isChangeable", Boolean.FALSE);
            }
            if(AggregationKind.COMPOSITE.equals(DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef2, "aggregation"))) {
                DataproviderMode.DATAPROVIDER_2.replaceAttributeValuesAsListBySingleton(associationEndDef1, "isChangeable", Boolean.FALSE);
            }
            // end1.aggregation=COMPOSITE --> end2.aggregation=AggregationKind.NONE
            if(
                (AggregationKind.COMPOSITE.equals(DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef1, "aggregation")) && !AggregationKind.NONE.equals(DataproviderMode.DATAPROVIDER_2.getSingletonFromAttributeValuesAsList(associationEndDef2, "aggregation"))) ||
                (AggregationKind.COMPOSITE.equals(DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef2, "aggregation")) && !AggregationKind.NONE.equals(DataproviderMode.DATAPROVIDER_2.getSingletonFromAttributeValuesAsList(associationEndDef1, "aggregation")))
            ) {
				throw newModelException(
	                    "Wrong aggregation. end1.aggregation='composite' --> end2.aggregation='none'",
	                    ModelExceptions.INVALID_OPPOSITE_AGGREGATION_FOR_COMPOSITE_AGGREGATION,
	                    new BasicException.Parameter("end1", associationEndDef1.getResourceIdentifier()),
	                    new BasicException.Parameter("end1.aggregation", DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef1, "aggregation")),
	                    new BasicException.Parameter("end2", associationEndDef2.getResourceIdentifier()),
	                    new BasicException.Parameter("end2.aggregation", DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef2, "aggregation"))
		        	);
                
            }
            // end1.aggregation=COMPOSITE --> end2.multiplicity="1..1"
            if(
                (AggregationKind.COMPOSITE.equals(DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef1, "aggregation")) && !("1..1".equals(DataproviderMode.DATAPROVIDER_2.getSingletonFromAttributeValuesAsList(associationEndDef2, "multiplicity")) || "0..1".equals(DataproviderMode.DATAPROVIDER_2.getSingletonFromAttributeValuesAsList(associationEndDef2, "multiplicity")))) ||
                (AggregationKind.COMPOSITE.equals(DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef2, "aggregation")) && !("1..1".equals(DataproviderMode.DATAPROVIDER_2.getSingletonFromAttributeValuesAsList(associationEndDef1, "multiplicity")) || "0..1".equals(DataproviderMode.DATAPROVIDER_2.getSingletonFromAttributeValuesAsList(associationEndDef1, "multiplicity"))))
            ) {
				throw newModelException(
	                    "Wrong multiplicity. end1.aggregation='composite' --> end2.multiplicity='1..1'|'0..1'",
	                    ModelExceptions.INVALID_MULTIPLICITY_FOR_COMPOSITE_AGGREGATION,
	                    new BasicException.Parameter("end1", associationEndDef1.getResourceIdentifier()),
	                    new BasicException.Parameter("end1.multiplicity", DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef1, "multiplicity")),
	                    new BasicException.Parameter("end1.aggregation", DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef1, "aggregation")),
	                    new BasicException.Parameter("end2", associationEndDef2.getResourceIdentifier()),
	                    new BasicException.Parameter("end2.multiplicity", DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef2, "multiplicity")),
	                    new BasicException.Parameter("end2.aggregation", DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef2, "aggregation"))
		        	);
                
            }
            this.verifyNavigabilityOfCompositeAggregation(associationEndDef1);
            this.verifyNavigabilityOfCompositeAggregation(associationEndDef2);
            this.checkUnnecessaryQualifiers(associationEndDef1);
            this.checkUnnecessaryQualifiers(associationEndDef2);
        } catch(RuntimeException e) {
        	e.printStackTrace(); // TODO
        	throw ResourceExceptions.toResourceException(e);
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Verifies whether an association end with aggregation 'composite' is navigable
     */
    private void verifyNavigabilityOfCompositeAggregation(
        ObjectRecord associationEndDef
    ) throws ResourceException {
    	if(
		    (AggregationKind.COMPOSITE.equals(DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef, "aggregation")) ||
		        AggregationKind.SHARED.equals(DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef, "aggregation")))
		        &&
		        !((Boolean)DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef, "isNavigable")).booleanValue()
		) {
			throw newModelException(
		            "An association end with aggregation 'composite' should be navigable.",
		            ModelExceptions.COMPOSITE_AGGREGATION_NOT_NAVIGABLE,
		            new BasicException.Parameter("end", associationEndDef.getResourceIdentifier())
		    	);
		}
    }

    //---------------------------------------------------------------------------
    /**
     * Checks for unnecessary qualifiers, only navigable associations need a 
     * unique identifying qualifier
     */
    private void checkUnnecessaryQualifiers(
        ObjectRecord associationEndDef
    ) throws ResourceException {
    	if(
		    (DataproviderMode.DATAPROVIDER_2.attributeHasValue(associationEndDef, "qualifierName")) &&
		    !((Boolean)DataproviderMode.DATAPROVIDER_2.attributeValue(associationEndDef, "isNavigable")).booleanValue()
		) {
			throw newModelException(
		            "Found association end with qualifier which is not navigable. Only navigable association ends need a unique identifying qualifier.",
		            ModelExceptions.UNNECESSARY_QUALIFIER_FOUND,
		            new BasicException.Parameter("end", associationEndDef.getResourceIdentifier())
		    	);
		}
    }

    //---------------------------------------------------------------------------
    protected void verifyAliasAttributeNumber(
        MappedRecord aliasTypeDef,
        int nAttributes
    ) throws ResourceException {
        if (nAttributes != 1)
			throw newModelException(
	                "an alias type must specify exactly one attribute",
	                ModelExceptions.ALIAS_TYPE_REQUIRES_EXACTLY_ONE_ATTRIBUTE,
	                new BasicException.Parameter("alias type", aliasTypeDef),
	                new BasicException.Parameter("alias type", aliasTypeDef)
	        	);
    }

    //---------------------------------------------------------------------------
    protected void verifyAliasAttributeName(
        MappedRecord aliasTypeDef,
        String attributeName
    ) throws ResourceException {
        if (attributeName.indexOf("::") == -1)
			throw newModelException(
	                "the name of the single attribute of the alias type must be a qualified type name",
	                ModelExceptions.INVALID_ALIAS_ATTRIBUTE_NAME,
	                new BasicException.Parameter("alias type", aliasTypeDef),
	                new BasicException.Parameter("attribute name", attributeName)
	        	);
    }

    //---------------------------------------------------------------------------
    protected void verifyAssociationName(
        String associationName
    ) throws ResourceException {
        if (associationName == null || associationName.length() == 0)
			throw newModelException(
                "the name of an association cannot be empty",
                ModelExceptions.ASSOCIATION_NAME_IS_EMPTY
        	);
    }

    //---------------------------------------------------------------------------
    protected void verifyAssociationEndName(
        MappedRecord associationDef,
        String associationEndName
    ) throws ResourceException {
        if(associationEndName == null || associationEndName.length() == 0)
			throw newModelException(
                "the name of an association end cannot be empty",
                ModelExceptions.ASSOCIATION_END_NAME_IS_EMPTY,
                new BasicException.Parameter("association", associationDef)
        	);
    }

    //---------------------------------------------------------------------------
    /**
     * parse strings with the following EBNF syntax 
     * [ qualifierAttribute ':' qualifierType ] { ';' qualifierAttribute ':' qualifierType }
     */
    protected List parseAssociationEndQualifierAttributes(
        String _qualifierText
    ) throws ResourceException {
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
            	throw newModelException(
                    "syntax error in qualifier declaration: missing ':'",
                    ModelExceptions.MISSING_COLON_IN_QUALIFIER_DECLARATION,
                    new BasicException.Parameter("qualifier text", qualifierText)
            	);
            }
            String qualifierType = javaToQualifiedName(getNextToken(tokenizer, qualifierText));
            Qualifier qualifier = new Qualifier(qualifierName, qualifierType);
            qualifierAttributes.add(qualifier);

            if(tokenizer.hasMoreTokens()) {
                delim = getNextToken(tokenizer, qualifierText);
                if (!delim.equals(";")) {
                	throw newModelException(
                        "syntax error in qualifier declaration: qualifier expressions must be separated by ';'",
                        ModelExceptions.MISSING_SEMICOLON_IN_QUALIFIER_DECLARATION,
                        new BasicException.Parameter("qualifier text", qualifierText)
                	);
                }
                while (delim.equals(";")) {
                    qualifierName = getNextToken(tokenizer, qualifierText);
                    delim = getNextToken(tokenizer, qualifierText);
                    if (!delim.equals(":")) {
                    	throw newModelException(
                            "syntax error in qualifier declaration: missing ':'",
                            ModelExceptions.MISSING_COLON_IN_QUALIFIER_DECLARATION,
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
    ) throws ResourceException {
        String nextToken = new String();
        if(tokenizer.hasMoreTokens()) {
			nextToken = tokenizer.nextToken();
            // skip spaces and tabs
            while (tokenizer.hasMoreTokens() && ( nextToken.equals(" ") || nextToken.equals("\t") )) {
                nextToken = tokenizer.nextToken();
            }
            if(nextToken.equals(" ") || nextToken.equals("\t")) {
            	throw newModelException(
            		"syntax error in qualifier declaration: unexpected end of expression", 
            		ModelExceptions.UNEXPECTED_END_OF_QUALIFIER_DECLARATION,
					new BasicException.Parameter("qualifier text", qualifierText)
            	);
            }
		}
        return nextToken;
    }

	private static ResourceException newModelException(
		final String exceptionMessage,
		final int exceptionCode,
		final BasicException.Parameter... exceptionParameters
	) throws ResourceException {
        SysLog.error(exceptionMessage);
		return ResourceExceptions.initHolder(
			new ResourceException(
		        exceptionMessage,
				BasicException.newEmbeddedExceptionStack(	
		            ModelExceptions.MODEL_DOMAIN,
		            exceptionCode,
		            exceptionParameters
		        )
		    )
		);
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
    

    //------------------------------------------------------------------------
    // Class Qualifier
    //------------------------------------------------------------------------
    
    protected static class Qualifier {
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

}
