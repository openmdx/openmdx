/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Swagger
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2014-2016, OMEX AG, Switzerland
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

package org.openmdx.application.rest.http.servlet;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.omg.mof.spi.Identifier;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.json.stream.JSONArray;
import org.openmdx.base.json.stream.JSONException;
import org.openmdx.base.json.stream.JSONObject;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.Model_1Factory;

/**
 * Swagger
 *
 */
public class Swagger {

    /**
     * Constructor 
     *
     * @param typeDef
     */
    public Swagger(
        ModelElement_1_0 typeDef
    ) {
        this.typeDef = typeDef;
    }

    /**
     * Set tags for given type.
     * 
     * @param node
     * @param typeDef
     * @param multiplicity
     * @param complexTypeAsPath
     * @param prefix
     * @throws ServiceException
     * @throws JSONException
     */
    private void setType(
        JSONObject node,
        ModelElement_1_0 typeDef,
        Multiplicity multiplicity,
        boolean complexTypeAsPath,
        String prefix
   ) throws ServiceException, JSONException {
        ModelElement_1_0 dereferencedType = typeDef.getModel().getDereferencedType(typeDef);
        if(dereferencedType.isPrimitiveType()) {
            if(
                PrimitiveTypes.SHORT.equals(dereferencedType.getQualifiedName()) ||
                PrimitiveTypes.INTEGER.equals(dereferencedType.getQualifiedName())
                ) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Integer" + LIST_SUFFIX);
                } else {
                    node.put("type", "integer");
                    node.put("format", "int32");
                }
            } else if(PrimitiveTypes.LONG.equals(dereferencedType.getQualifiedName())) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Long" + LIST_SUFFIX);
                } else {
                    node.put("type", "integer");                    
                    node.put("format", "int64");
                }
            } else if(PrimitiveTypes.BOOLEAN.equals(dereferencedType.getQualifiedName())) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Boolean" + LIST_SUFFIX);
                } else {
                    node.put("type", "boolean");
                }
            } else if(PrimitiveTypes.DATE.equals(dereferencedType.getQualifiedName())) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Date" + LIST_SUFFIX);
                } else {
                    node.put("type", "string");
                    node.put("format", "date");
                }
            } else if(PrimitiveTypes.DATETIME.equals(dereferencedType.getQualifiedName())) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "DateTime" + LIST_SUFFIX);
                } else {
                    node.put("type", "string");
                    node.put("format", "date-time");
                }
            } else if(PrimitiveTypes.DECIMAL.equals(dereferencedType.getQualifiedName())) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Decimal" + LIST_SUFFIX);
                } else {
                    node.put("type", "number");
                }
            } else {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "String" + LIST_SUFFIX);
                } else {
                    node.put("type", "string");
                }
            }
        } else {
            if(complexTypeAsPath) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Path" + LIST_SUFFIX);
                } else {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Path");
                }
            } else {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + dereferencedType.getQualifiedName() + LIST_SUFFIX);
                } else {
                    node.put("$ref", (prefix == null ? "" : prefix) + dereferencedType.getQualifiedName());
                }
            }
        }
    }

    /**
     * Create new parameters.
     * 
     * @return
     * @throws JSONException
     */
    private JSONArray newQueryParameters(
    ) throws JSONException {
        JSONArray parameters = this.newParameters();
        // queryType
        JSONObject queryType = new JSONObject();
        queryType.put("name", "queryType");
        queryType.put("in", "query");
        queryType.put("required", false);
        queryType.put("type", "string");
        parameters.put(queryType);
        // query
        JSONObject query = new JSONObject();
        query.put("name", "query");
        query.put("in", "query");
        query.put("required", false);
        query.put("type", "string");
        parameters.put(query);
        // position
        JSONObject position = new JSONObject();
        position.put("name", "position");
        position.put("in", "query");
        position.put("required", false);
        position.put("type", "integer");
        parameters.put(position);
        // size
        JSONObject size = new JSONObject();
        size.put("name", "size");
        size.put("in", "query");
        size.put("required", false);
        size.put("type", "integer");
        parameters.put(size);
        // groups
        JSONObject groups = new JSONObject();
        groups.put("name", "groups");
        groups.put("in", "query");
        groups.put("required", false);
        groups.put("type", "string");
        parameters.put(groups);
        return parameters;
    }

    /**
     * Create new path parameter for the given name.
     * 
     * @param name
     * @return
     * @throws JSONException
     */
    private JSONObject newPathParameter(
        String name
    ) throws JSONException {
        JSONObject qualifier = new JSONObject();
        qualifier.put("name", name);
        qualifier.put("in", "path");
        qualifier.put("required", true);
        qualifier.put("type", "string");
        return qualifier;
    }

    /**
     * Create new body parameter for given name and type.
     * 
     * @param name
     * @param typeDef
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    private JSONObject newBodyParameter(
        String name,
        ModelElement_1_0 typeDef
    ) throws JSONException, ServiceException {
        JSONObject qualifier = new JSONObject();
        qualifier.put("name", name);
        qualifier.put("in", "body");
        qualifier.put("required", true);
        JSONObject schema = new JSONObject();
        schema.put("$ref", "#/definitions/" + typeDef.getQualifiedName() + TYPE_SUFFIX);
        qualifier.put("schema", schema);
        return qualifier;
    }

    /**
     * Create new operation with given tags.
     * 
     * @param operationId
     * @param description
     * @param tags
     * @param parameters
     * @param responses
     * @return
     * @throws JSONException
     */
    private JSONObject newOperation(
        String operationId,
        String description,
        JSONArray tags,
        JSONArray parameters,
        JSONObject responses
    ) throws JSONException {
        JSONObject operation = new JSONObject();
        operation.put("description", description);
        operation.put("operationId", operationId);
        operation.put("tags", tags);
        JSONArray consumes = new JSONArray();
        consumes.put("application/json");
        consumes.put("application/xml");
        operation.put("consumes", consumes);
        JSONArray produces = new JSONArray();
        produces.put("application/json");
        produces.put("application/xml");
        operation.put("produces", produces);
        operation.put("parameters", parameters);
        operation.put("responses", responses);             
        return operation;
    }

    /**
     * Create response element for given type.
     * 
     * @param typeDef
     * @param multiplicity
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    private JSONObject newObjectResponse(
        ModelElement_1_0 typeDef,
        String description,
        Multiplicity multiplicity
    ) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        JSONObject schema = new JSONObject();
        this.setType(
            schema, 
            typeDef, 
            multiplicity, 
            false, 
            "#/definitions/"
        );
        response.put("schema", schema);
        response.put("description", description);
        return response;
    }

    /**
     * Create new StackTrace response.
     * 
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    private JSONObject newErrorResponse(
        String description
    ) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        response.put("description", description);
        return response;
    }

    /**
     * Create empty response element.
     * 
     * @return
     * @throws JSONException
     */
    private JSONObject newResponse(
    ) throws JSONException {
        return new JSONObject();
    }

    /**
     * Create empty parameters element.
     * 
     * @return
     * @throws JSONException
     */
    private JSONArray newParameters(
    ) throws JSONException {
        return new JSONArray();
    }

    /**
     * Create empty tags element.
     * 
     * @return
     */
    private JSONArray newTags(
    ) {
        return new JSONArray();
    }

    /**
     * Create the definition for the given type.
     * 
     * @param typeDef
     * @param withDiscriminator
     * @param subTypes
     * @param excludeProperties
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    private JSONObject newDefinition(
        ModelElement_1_0 typeDef,
        boolean withDiscriminator,
        JSONArray subTypes,
        Set<String> excludeProperties
    ) throws JSONException, ServiceException {
        Model_1_0 model = typeDef.getModel();
        JSONObject typeDefinition = new JSONObject();
        JSONObject properties = new JSONObject();
        Collection<?> elements = null;
        if(typeDef.isStructureType()) {
            elements = typeDef.objGetMap("field").values();
        } else {
            if(SWAGGER_SUPPORTS_POLYMORPHISM) {
                elements = typeDef.objGetMap("feature").values();                     
            } else {
                elements = new TreeMap<String,ModelElement_1_0>(
                    model.getStructuralFeatureDefs(
                        typeDef, 
                        true, // includeSubtypes
                        true, // includeDerived
                        false // attributesOnly
                        )
                    ).values();
            }
        }
        JSONArray required = new JSONArray();
        for(Object element: elements) {
            ModelElement_1_0 elementDef = model.getElement(element);
            if(
                elementDef.isStructureFieldType() || 
                elementDef.isAttributeType() ||
                elementDef.isReferenceStoredAsAttribute()
            ) {
                JSONObject property = new JSONObject();
                try {
                    ModelElement_1_0 elementTypeDef = model.getDereferencedType(elementDef.getType());
                    property.put("description", "<span title=\"" + elementDef.getQualifiedName() + "" + "\">&laquo;" + ModelHelper.getMultiplicity(elementDef).code()  + "&raquo; " + elementTypeDef.getQualifiedName() + "</span>");
                    this.setType(
                        property,
                        elementTypeDef,
                        ModelHelper.getMultiplicity(elementDef),
                        true,
                        "#/definitions/"
                    );
                    boolean isReadOnly = 
                        !elementDef.isStructureFieldType() && 
                        !ModelHelper.isChangeable(elementDef);
                    property.put("readOnly", isReadOnly);
                    if(!excludeProperties.contains(elementDef.getName())) {
                        properties.put(
                            elementDef.getName(), 
                            property
                        );
                        if(
                            ModelHelper.getMultiplicity(elementDef) == Multiplicity.SINGLE_VALUE && 
                            !isReadOnly
                        ) {
                            if(SWAGGER_SUPPORTS_POLYMORPHISM) {
                                required.put(elementDef.getName());
                            }
                        }
                    }
                } catch(Exception e) {
                    new ServiceException(e).log();
                }
            }
        }
        if(withDiscriminator) {
            typeDefinition.put("discriminator", TYPE_PROPERTY);
            // @type
            {
                JSONObject property = new JSONObject();
                property.put("type", "string");
                if(subTypes != null && subTypes.length() > 0) {
                    property.put("enum", subTypes);
                }
                if(!excludeProperties.contains(TYPE_PROPERTY)) {
                    properties.put(TYPE_PROPERTY, property);
                }
            }
            // @id
            {
                JSONObject property = new JSONObject();
                property.put("type", "string");
                if(!excludeProperties.contains(ID_PROPERTY)) {
                    properties.put(ID_PROPERTY, property);
                }
            }
            // @href
            {
                JSONObject property = new JSONObject();
                property.put("type", "string");
                if(!excludeProperties.contains(HREF_PROPERTY)) {
                    properties.put(HREF_PROPERTY, property);
                }
            }
            if(!typeDef.isStructureType()) {
                // @version
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "string");
                    if(!excludeProperties.contains(VERSION_PROPERTY)) {
                        properties.put(VERSION_PROPERTY, property);
                    }
                }
            }
        }
        if(SWAGGER_SUPPORTS_POLYMORPHISM && subTypes != null) {
            typeDefinition.put("subTypes", subTypes);
        }
        typeDefinition.put("properties", properties);
        if(required.length() > 0) {
            typeDefinition.put("required", required);
        }
        return typeDefinition;
    }

    /**
     * Create all definitions for the given type and its sub-types.
     * 
     * @param typeDef
     * @param withDiscriminator
     * @param definitions
     * @param excludeProperties
     * @throws JSONException
     * @throws ServiceException
     */
    private void putAllDefinitions(
        ModelElement_1_0 typeDef,
        boolean withDiscriminator,
        JSONObject definitions,
        Set<String> excludeProperties
    ) throws JSONException, ServiceException {
        Model_1_0 model = typeDef.getModel();
        Set<Object> subtypes = typeDef.objGetSet("allSubtype");
        List<ModelElement_1_0> subtypeDefs = new ArrayList<ModelElement_1_0>();
        JSONArray subTypes = new JSONArray();
        if(!"org:openmdx:base:ExtentCapable".equals(typeDef.getQualifiedName())) {
            for(Object subtype: subtypes) {
                ModelElement_1_0 subtypeDef = model.getElement(subtype);
                if(!subtypeDef.getQualifiedName().equals(typeDef.getQualifiedName())) {
                    subTypes.put(subtypeDef.getQualifiedName());
                    subtypeDefs.add(subtypeDef);
                }
            }
        }
        JSONObject typeDefinition = null;
        definitions.put(
            typeDef.getQualifiedName(),
            typeDefinition = this.newDefinition(
                typeDef,
                withDiscriminator,
                subTypes,
                excludeProperties
            )
        );
        Set<String> excludePropertiesSubtype = new HashSet<String>(excludeProperties);
        excludePropertiesSubtype.addAll(
            typeDefinition.getJSONObject("properties").keySet()
            );
        if(SWAGGER_SUPPORTS_POLYMORPHISM) {
            for(ModelElement_1_0 subtypeDef: subtypeDefs) {
                this.putAllDefinitions(
                    subtypeDef,
                    false, // no discriminator for sub-types
                    definitions,
                    excludePropertiesSubtype
                );
            }
        }
    }

    /**
     * Get paths.
     * 
     * @param paths
     * @param collectedTypeDefs
     * @throws ServiceException
     * @throws JSONException
     */
    private void getPaths(
        JSONObject paths,
        Map<Object,ModelElement_1_0> collectedTypeDefs
    ) throws ServiceException, JSONException {
        Model_1_0 model = Model_1Factory.getModel();
        collectedTypeDefs.put(
            this.typeDef.jdoGetObjectId(),
            this.typeDef
        );
        // ./reference
        Map<String,Object> references = new TreeMap<String,Object>(this.typeDef.objGetMap("reference"));
        for(Object reference: references.values()) {
            ModelElement_1_0 referenceDef = model.getElement(reference);
            if(
                !"extent".equals(referenceDef.getName()) &&
                referenceDef.isReferenceType() &&
                (ModelHelper.isSharedEnd(referenceDef, false) || ModelHelper.isCompositeEnd(referenceDef, false))
            ) {
                ModelElement_1_0 referencedTypeDef = model.getElement(referenceDef.getType());
                collectedTypeDefs.put(
                    referencedTypeDef.jdoGetObjectId(), 
                    referencedTypeDef
                );
                Set<String> subtypeNames = new TreeSet<String>();
                for(Object subtype: referencedTypeDef.objGetSet("allSubtype")) {
                    ModelElement_1_0 subtypeDef = model.getElement(subtype);
                    if(!Boolean.TRUE.equals(subtypeDef.isAbstract())) {
                        subtypeNames.add(subtypeDef.getQualifiedName());
                    }
                }
                String subtypesDescription = "<pre>";
                for(String subtypeName: subtypeNames) {
                    subtypesDescription += subtypeName + "\n";
                }
                subtypesDescription += "</pre>";
                // Operations for pattern /reference
                {
                    JSONObject operations = new JSONObject().put(
                        "get",
                        this.newOperation(
                            // operationId
                            Identifier.OPERATION_NAME.toIdentifier(referenceDef.getName(), null, "get", null, "s"),
                            // description
                            " Retrieves the value for the reference &laquo;" + referenceDef.getName() + "&raquo; for the specified query.",
                            // tags
                            this.newTags().put(referenceDef.getName()),                             
                            // parameters
                            this.newQueryParameters(),
                            // responses
                            this.newResponse().put(
                                "default",
                                this.newObjectResponse(
                                    referencedTypeDef,
                                    "List<" + referencedTypeDef.getQualifiedName() + ">",
                                    Multiplicity.LIST
                                )
                            )
                        )
                    );
                    if(Boolean.TRUE.equals(model.getElement(referenceDef.getReferencedEnd()).isChangeable())) {
                        operations.put(
                            "post", 
                            this.newOperation(
                                // operationId
                                Identifier.OPERATION_NAME.toIdentifier(referenceDef.getName(), null, "add", null, null),
                                // description
                                "Adds the specified element to the set of the values for the reference &laquo;" + referenceDef.getName() + "&raquo; using an implementation-specific, reassignable qualifier. The element must be of type:" + subtypesDescription,
                                // tags
                                this.newTags().put(referenceDef.getName()),                             
                                // parameters
                                this.newParameters().put(
                                    this.newBodyParameter("object", referencedTypeDef)
                                ),
                                // responses
                                this.newResponse().put(
                                    "default",
                                    this.newObjectResponse(
                                        referencedTypeDef,
                                        referencedTypeDef.getQualifiedName(),
                                        Multiplicity.SINGLE_VALUE
                                    )
                                )
                            )
                        );
                    }
                    paths.put(
                        "/" + referenceDef.getName(),
                        operations
                    );
                }
                // Operations for pattern /reference/{id}
                {
                    JSONObject operations = new JSONObject().put(
                        "get",
                        this.newOperation(
                            // operationId
                            Identifier.OPERATION_NAME.toIdentifier(referenceDef.getName(), null, "get", null, null),
                            // description
                            "Retrieves the value for the reference &laquo;" + referenceDef.getName() + "&raquo; for the specified qualifier &laquo;id&raquo;. The returned value is of type:" + subtypesDescription,
                            // tags
                            this.newTags().put(referenceDef.getName()),                             
                            // parameters
                            this.newParameters().put(
                                this.newPathParameter("id")
                            ),
                            // responses
                            this.newResponse().put(
                                "default",
                                this.newObjectResponse(
                                    referencedTypeDef,
                                    referencedTypeDef.getQualifiedName(),
                                    Multiplicity.SINGLE_VALUE
                                )
                            ).put(
                                Integer.toString(HttpServletResponse.SC_NOT_FOUND),
                                this.newErrorResponse("Object with given qualifier not found")
                            )
                        )
                    );
                    if(Boolean.TRUE.equals(model.getElement(referenceDef.getReferencedEnd()).isChangeable())) {
                        operations.put(
                            "delete", 
                            this.newOperation(
                                // operationId
                                Identifier.OPERATION_NAME.toIdentifier(referenceDef.getName(), null, "delete", null, null),
                                // description
                                "Deletes the value for the reference &laquo;" + referenceDef.getName() + "&raquo; with the specified qualifier &laquo;id&raquo;.",
                                // tags
                                this.newTags().put(referenceDef.getName()),
                                // parameters
                                this.newParameters().put(
                                    this.newPathParameter("id")
                                ),
                                // responses
                                this.newResponse().put(
                                    Integer.toString(HttpServletResponse.SC_OK),
                                    this.newErrorResponse("Object deleted")
                                )
                            )
                        ).put(
                            "put", 
                            this.newOperation(
                                // operationId
                                Identifier.OPERATION_NAME.toIdentifier(referenceDef.getName(), null, "update", null, null),
                                // description
                                "Updates the value for the reference &laquo;" + referenceDef.getName() + "&raquo; with the specified qualifier &laquo;id&raquo;.",
                                // tags
                                this.newTags().put(referenceDef.getName()),
                                // parameters
                                this.newParameters().put(
                                    this.newPathParameter("id")
                                ).put(
                                    this.newBodyParameter(
                                        "object", 
                                        referencedTypeDef
                                    )
                                ),
                                // responses
                                this.newResponse().put(
                                    "default", 
                                    this.newObjectResponse(
                                        referencedTypeDef,
                                        referencedTypeDef.getQualifiedName(),
                                        Multiplicity.SINGLE_VALUE
                                    )
                                ).put(
                                    Integer.toString(HttpServletResponse.SC_BAD_REQUEST),
                                    this.newErrorResponse("Unable to update object")
                                )
                            )
                        ).put(
                            "post", 
                            this.newOperation(
                                // operationId
                                Identifier.OPERATION_NAME.toIdentifier(referenceDef.getName(), null, "addQualified", null, null),
                                // description
                                "Adds the specified element to the set of the values for the reference &laquo;" + referenceDef.getName() + "&raquo; using the specified, reassignable qualifier &laquo;id&raquo;. The element must be of type:" + subtypesDescription,
                                // tags
                                this.newTags().put(referenceDef.getName()),                             
                                // parameters
                                this.newParameters().put(
                                    this.newPathParameter("id")
                                ).put(
                                    this.newBodyParameter("object", referencedTypeDef)
                                ),
                                // responses
                                this.newResponse().put(
                                    "default",
                                    this.newObjectResponse(
                                        referencedTypeDef,
                                        referencedTypeDef.getQualifiedName(),
                                        Multiplicity.SINGLE_VALUE
                                    )
                                )
                            )
                        );
                    }
                    paths.put(
                        "/" + referenceDef.getName() + "/{id}",
                        operations
                    );
                }
            }
        }
        // ./operation
        Map<String,Object> operations = new TreeMap<String,Object>(this.typeDef.objGetMap("operation"));
        for(Object element: operations.values()) {
            ModelElement_1_0 operationDef = model.getElement(element);
            if(operationDef.isOperationType()) {
                {
                    ModelElement_1_0 inParamDef = null;
                    ModelElement_1_0 resultParamDef = null;
                    for(Object param: operationDef.objGetSet("parameter")) {
                        ModelElement_1_0 paramDef = model.getElement(param);
                        if("in".equals(paramDef.getName())) {
                            inParamDef = paramDef;
                        } else if("result".equals(paramDef.getName())) {
                            resultParamDef = paramDef;
                        }
                    }
                    ModelElement_1_0 inParamTypeDef = model.getElement(inParamDef.getType());
                    collectedTypeDefs.put(
                        inParamTypeDef.jdoGetObjectId(), 
                        inParamTypeDef
                    );
                    ModelElement_1_0 resultParamTypeDef = model.getElement(resultParamDef.getType());
                    collectedTypeDefs.put(
                        resultParamTypeDef.jdoGetObjectId(), 
                        resultParamTypeDef
                    );
                    paths.put(
                        "/" + operationDef.getName(),
                        new JSONObject().put(
                            "post", 
                            this.newOperation(
                                // operationId
                                Identifier.OPERATION_NAME.toIdentifier(operationDef.getName(), null, null, null, null),
                                // description
                                "Invoke operation<pre>" + operationDef.getName() + "(in: " + inParamTypeDef.getQualifiedName() + "): " + resultParamTypeDef.getQualifiedName() + "</pre>",
                                // tags
                                this.newTags().put(operationDef.getName()),                             
                                // parameters
                                this.newParameters().put(
                                    this.newBodyParameter("in", inParamTypeDef)
                                ),
                                // responses
                                this.newResponse().put(
                                    "default",
                                    this.newObjectResponse(
                                        resultParamTypeDef,
                                        resultParamTypeDef.getQualifiedName(),
                                        Multiplicity.SINGLE_VALUE
                                    )
                                )
                            )
                        )
                    );
                }
            }
        }
    }

    /**
     * Write Swagger-compliant API.
     * 
     * @param out
     * @throws ServiceException
     */
    public void writeAPI(
        PrintWriter out,
        String host,
        String basePath,
        String description
    ) throws ServiceException {
        try {
            JSONObject api = new JSONObject();
            api.put("swagger", "2.0");
            if(basePath != null) {
                api.put("basePath", basePath);
            }
            if(host != null) {
                api.put("host", host);
            }
            // info
            JSONObject info = new JSONObject();
            info.put("title", this.typeDef.getQualifiedName());
            if(description != null) {
                info.put("description", description);
            }
            String version = "";
            if(this.typeDef.getQualifiedName().indexOf(":") > 0) {
                String packageName = this.typeDef.getQualifiedName().substring(0, this.typeDef.getQualifiedName().lastIndexOf(":"));
                int pos = packageName.length() - 1;
                while(pos >= 0 && Character.isDigit(packageName.charAt(pos))) {
                    version = packageName.charAt(pos) + version;
                    pos--;
                }
                version = version + ".0";
            }
            info.put("version", version.length() < 3 ? "1.0" : version);
            // paths
            JSONObject paths = new JSONObject();
            Map<Object,ModelElement_1_0> collectedTypeDefs = new HashMap<Object,ModelElement_1_0>();
            this.getPaths(
                paths,
                collectedTypeDefs
            );
            // definitions
            JSONObject definitions = new JSONObject();
            // Path
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // @href
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "string");
                    properties.put(HREF_PROPERTY, property);
                }
                // $
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "string");
                    properties.put(ATTRIBUTE_VALUE_PROPERTY, property);                         
                }
                definition.put("properties", properties);
                definitions.put("Path", definition);
            }
            // PathElement
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // @href
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "string");
                    properties.put(HREF_PROPERTY, property);
                }
                // @index
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "integer");
                    property.put("format", "int32");
                    properties.put(INDEX_PROPERTY, property);
                }
                // $
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "string");
                    properties.put(ATTRIBUTE_VALUE_PROPERTY, property);                         
                }
                definition.put("properties", properties);
                definitions.put("PathElement", definition);
            }
            // PathList
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/definitions/PathElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                definition.put("properties", properties);
                definitions.put("Path" + LIST_SUFFIX, definition);
            }
            // StringElement
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // @index
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "integer");
                    property.put("format", "int32");
                    properties.put(INDEX_PROPERTY, property);
                }
                // $
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "string");
                    properties.put(ATTRIBUTE_VALUE_PROPERTY, property);                         
                }
                definition.put("properties", properties);
                definitions.put("StringElement", definition);
            }
            // StringList
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/definitions/StringElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                definition.put("properties", properties);
                definitions.put("String" + LIST_SUFFIX, definition);
            }
            // IntegerElement
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // @index
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "integer");
                    property.put("format", "int32");
                    properties.put(INDEX_PROPERTY, property);
                }
                // $
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "integer");
                    property.put("format", "int32");                     
                    properties.put(ATTRIBUTE_VALUE_PROPERTY, property);                         
                }
                definition.put("properties", properties);
                definitions.put("IntegerElement", definition);
            }
            // IntegerList
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/definitions/IntegerElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                definition.put("properties", properties);
                definitions.put("Integer" + LIST_SUFFIX, definition);
            }
            // LongElement
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // @index
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "integer");
                    property.put("format", "int32");
                    properties.put(INDEX_PROPERTY, property);
                }
                // $
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "integer");
                    property.put("format", "int64");
                    properties.put(ATTRIBUTE_VALUE_PROPERTY, property);                         
                }
                definition.put("properties", properties);
                definitions.put("LongElement", definition);
            }
            // LongList
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/definitions/LongElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                definition.put("properties", properties);
                definitions.put("Long" + LIST_SUFFIX, definition);
            }
            // DateElement
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // @index
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "integer");
                    property.put("format", "int32");
                    properties.put(INDEX_PROPERTY, property);
                }
                // $
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "string");
                    property.put("format", "date");
                    properties.put(ATTRIBUTE_VALUE_PROPERTY, property);                         
                }                     
                definition.put("properties", properties);
                definitions.put("DateElement", definition);
            }
            // DateList
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/definitions/DateElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                definition.put("properties", properties);
                definitions.put("Date" + LIST_SUFFIX, definition);
            }
            // DateTimeElement
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // @index
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "integer");
                    property.put("format", "int32");
                    properties.put(INDEX_PROPERTY, property);
                }
                // $
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "string");
                    property.put("format", "date-time");
                    properties.put(ATTRIBUTE_VALUE_PROPERTY, property);                         
                }                     
                definition.put("properties", properties);
                definitions.put("DateTimeElement", definition);
            }
            // DateTimeList
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/definitions/DateTimeElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                definition.put("properties", properties);
                definitions.put("DateTime" + LIST_SUFFIX, definition);
            }                 
            // BooleanElement
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // @index
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "integer");
                    property.put("format", "int32");
                    properties.put(INDEX_PROPERTY, property);
                }
                // $
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "boolean");
                    properties.put(ATTRIBUTE_VALUE_PROPERTY, property);                         
                }                     
                definition.put("properties", properties);
                definitions.put("BooleanElement", definition);
            }
            // BooleanList
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/definitions/BooleanElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                definition.put("properties", properties);
                definitions.put("Boolean" + LIST_SUFFIX, definition);
            }                 
            // DecimalElement
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // @index
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "integer");
                    property.put("format", "int32");
                    properties.put(INDEX_PROPERTY, property);
                }
                // $
                {
                    JSONObject property = new JSONObject();
                    property.put("type", "number");
                    properties.put(ATTRIBUTE_VALUE_PROPERTY, property);                         
                }
                definition.put("properties", properties);
                definitions.put("DecimalElement", definition);
            }
            // DecimalList
            {
                JSONObject definition = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/definitions/DecimalElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                definition.put("properties", properties);
                definitions.put("Decimal" + LIST_SUFFIX, definition);
            }                 
            for(ModelElement_1_0 typeDef: collectedTypeDefs.values()) {
                if(!definitions.has(typeDef.getQualifiedName())) {
                    this.putAllDefinitions(
                        typeDef,
                        true, // withDiscriminator
                        definitions,
                        Collections.<String>emptySet()
                        );
                }
                // List<Type>
                if(!typeDef.isStructureType()) {
                    JSONObject type = new JSONObject();
                    JSONObject properties = new JSONObject();
                    // @type
                    {
                        JSONObject property = new JSONObject();
                        property.put("type", "string");
                        properties.put(TYPE_PROPERTY, property);
                    }
                    // @href
                    {
                        JSONObject property = new JSONObject();
                        property.put("type", "string");
                        properties.put(HREF_PROPERTY, property);
                    }
                    // @hasMore
                    {
                        JSONObject property = new JSONObject();
                        property.put("type", "string");
                        properties.put(HAS_MORE_PROPERTY, property);
                    }
                    // @total
                    {
                        JSONObject property = new JSONObject();
                        property.put("type", "string");
                        properties.put(TOTAL_PROPERTY, property);
                    }
                    // objects
                    {
                        JSONObject property = new JSONObject();
                        property.put("type", "array");
                        JSONObject items = new JSONObject();
                        this.setType(
                            items, 
                            typeDef,
                            Multiplicity.SINGLE_VALUE,
                            false, 
                            "#/definitions/"
                        );
                        property.put("items", items);
                        properties.put("objects", property);
                    }
                    type.put("properties", properties);
                    definitions.put(
                        typeDef.getQualifiedName() + LIST_SUFFIX, 
                        type
                    );
                }
                // @T Type
                {
                    JSONObject type = new JSONObject();
                    JSONObject properties = new JSONObject();
                    // qualifiedTypeName
                    {
                        JSONObject property = new JSONObject();
                        property.put("$ref", "#/definitions/" + typeDef.getQualifiedName());
                        properties.put(typeDef.getQualifiedName().replace(":", "."), property);
                    }
                    type.put("properties", properties);
                    definitions.put(
                        typeDef.getQualifiedName() + TYPE_SUFFIX, 
                        type
                    );
                }
            }
            api.put("paths", paths);
            api.put("definitions", definitions);
            api.put("info", info);
            api.write(out);
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final boolean SWAGGER_SUPPORTS_POLYMORPHISM = false;
    private static final String TYPE_PROPERTY = "@type";
    private static final String HREF_PROPERTY = "@href";
    private static final String HAS_MORE_PROPERTY = "@hasMore";
    private static final String TOTAL_PROPERTY = "@total";
    private static final String VERSION_PROPERTY = "@version";
    private static final String INDEX_PROPERTY = "@index";
    private static final String ID_PROPERTY = "@id";
    private static final String ATTRIBUTE_VALUE_PROPERTY = "$";
    private static final String LIST_SUFFIX = "$List";
    private static final String TYPE_SUFFIX = "$Type";

    private final ModelElement_1_0 typeDef;
    
}
