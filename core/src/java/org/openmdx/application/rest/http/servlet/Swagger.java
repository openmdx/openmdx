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
    private boolean setType(
        JSONObject node,
        ModelElement_1_0 typeDef,
        Multiplicity multiplicity,
        boolean complexTypeAsPath,
        String prefix
   ) throws ServiceException, JSONException {
        boolean isRef = false;
        ModelElement_1_0 dereferencedType = typeDef.getModel().getDereferencedType(typeDef);
        if(dereferencedType.isPrimitiveType()) {
            if(
                PrimitiveTypes.SHORT.equals(dereferencedType.getQualifiedName()) ||
                PrimitiveTypes.INTEGER.equals(dereferencedType.getQualifiedName())
            ) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Integer" + LIST_SUFFIX);
                    isRef = true;
                } else {
                    node.put("type", "integer");
                    node.put("format", "int32");
                }
            } else if(PrimitiveTypes.LONG.equals(dereferencedType.getQualifiedName())) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Long" + LIST_SUFFIX);
                    isRef = true;
                } else {
                    node.put("type", "integer");                    
                    node.put("format", "int64");
                }
            } else if(PrimitiveTypes.BOOLEAN.equals(dereferencedType.getQualifiedName())) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Boolean" + LIST_SUFFIX);
                    isRef = true;
                } else {
                    node.put("type", "boolean");
                }
            } else if(PrimitiveTypes.DATE.equals(dereferencedType.getQualifiedName())) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Date" + LIST_SUFFIX);
                    isRef = true;
                } else {
                    node.put("type", "string");
                    node.put("format", "date");
                }
            } else if(PrimitiveTypes.DATETIME.equals(dereferencedType.getQualifiedName())) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "DateTime" + LIST_SUFFIX);
                    isRef = true;
                } else {
                    node.put("type", "string");
                    node.put("format", "date-time");
                }
            } else if(PrimitiveTypes.DECIMAL.equals(dereferencedType.getQualifiedName())) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Decimal" + LIST_SUFFIX);
                    isRef = true;
                } else {
                    node.put("type", "number");
                }
            } else {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "String" + LIST_SUFFIX);
                    isRef = true;
                } else {
                    node.put("type", "string");
                }
            }
        } else {
            if(complexTypeAsPath) {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Path" + LIST_SUFFIX);
                    isRef = true;
                } else {
                    node.put("$ref", (prefix == null ? "" : prefix) + "Path");
                    isRef = true;
                }
            } else {
                if(!multiplicity.isSingleValued()) {
                    node.put("$ref", (prefix == null ? "" : prefix) + dereferencedType.getQualifiedName() + LIST_SUFFIX);
                    isRef = true;
                } else {
                    node.put("$ref", (prefix == null ? "" : prefix) + dereferencedType.getQualifiedName());
                    isRef = true;
                }
            }
        }
        return isRef;
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
        {
            JSONObject queryType = new JSONObject();
            queryType.put("name", "queryType");
            queryType.put("in", "query");
            queryType.put("required", false);
            JSONObject schema = new JSONObject();
            queryType.put("schema", schema);
            schema.put("type", "string");
            parameters.put(queryType);
        }
        // query
        {
            JSONObject query = new JSONObject();
            query.put("name", "query");
            query.put("in", "query");
            query.put("required", false);
            JSONObject schema = new JSONObject();
            query.put("schema", schema);
            schema.put("type", "string");
            parameters.put(query);
        }
        // position
        {
            JSONObject position = new JSONObject();
            position.put("name", "position");
            position.put("in", "query");
            position.put("required", false);
            JSONObject schema = new JSONObject();
            position.put("schema", schema);
            schema.put("type", "integer");
            parameters.put(position);
        }
        // size
        {
        JSONObject size = new JSONObject();
            size.put("name", "size");
            size.put("in", "query");
            size.put("required", false);
            JSONObject schema = new JSONObject();
            size.put("schema", schema);
            schema.put("type", "integer");
            parameters.put(size);
        }
        // groups
        {
            JSONObject groups = new JSONObject();
            groups.put("name", "groups");
            groups.put("in", "query");
            groups.put("required", false);
            JSONObject schema = new JSONObject();
            groups.put("schema", schema);
            schema.put("type", "string");
            parameters.put(groups);
        }
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
        JSONObject parameter = new JSONObject();
        parameter.put("name", name);
        parameter.put("in", "path");
        parameter.put("required", true);
        JSONObject schema = new JSONObject();
        parameter.put("schema", schema);
        schema.put("type", "string");
        return parameter;
    }

    /**
     * Create new request body for given name and type.
     * 
     * @param description
     * @param typeDef
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    private JSONObject newRequestBody(
        String description,
        ModelElement_1_0 typeDef,
        Map<Object,ModelElement_1_0> collectedTypeDefs
    ) throws JSONException, ServiceException {
        Model_1_0 model = typeDef.getModel();
        List<ModelElement_1_0> subtypeDefs = new ArrayList<ModelElement_1_0>();
        for(Object subtype: typeDef.objGetSet("allSubtype")) {
            ModelElement_1_0 subtypeDef = model.getElement(subtype);
            if(!Boolean.TRUE.equals(subtypeDef.isAbstract())) {
                subtypeDefs.add(subtypeDef);
            }
        }
        if(typeDef.isStructureType()) {
            this.collectNestedTypeDefs(
                typeDef,
                collectedTypeDefs
            );
        }
        JSONObject requestBody = new JSONObject();
        requestBody.put("description", description);
        requestBody.put("required", true);
        JSONObject content = new JSONObject();
        requestBody.put("content", content);
        JSONObject schema = new JSONObject();
        if(subtypeDefs.size() == 1) {
            schema.put("$ref", "#/components/schemas/" + typeDef.getQualifiedName() + TYPE_SUFFIX);
        } else {
            JSONArray oneOf = new JSONArray();
            for(ModelElement_1_0 subTypeDef: subtypeDefs) {
                JSONObject type = new JSONObject();
                type.put("$ref", "#/components/schemas/" + subTypeDef.getQualifiedName() + TYPE_SUFFIX);
                oneOf.put(type);
            }
            schema.put("oneOf", oneOf);
        }
        JSONObject applicationJson = new JSONObject();
        applicationJson.put("schema", schema);
        content.put("application/json", applicationJson);
        JSONObject applicationXml = new JSONObject();
        applicationXml.put("schema", schema);
        content.put("application/xml", applicationXml);
        requestBody.put("content", content);
        return requestBody;
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
        JSONObject requestBody,
        JSONObject responses
    ) throws JSONException {
        JSONObject operation = new JSONObject();
        operation.put("description", description);
        operation.put("operationId", operationId);
        operation.put("tags", tags);
        if(parameters != null) {
            operation.put("parameters", parameters);
        }
        if(requestBody != null) {
            operation.put("requestBody", requestBody);
        }
        operation.put("responses", responses);             
        return operation;
    }

    /**
     * Collect nested types for given structDef
     * 
     * @param structDef
     * @param collectedTypeDefs
     * @throws ServiceException
     */
    private void collectNestedTypeDefs(
        ModelElement_1_0 structDef,
        Map<Object,ModelElement_1_0> collectedTypeDefs
    ) throws ServiceException {
        Model_1_0 model = structDef.getModel();
        if(structDef.isStructureType()) {
            for(Object field: structDef.objGetMap("field").values()) {
                ModelElement_1_0 fieldDef = model.getElement(field);
                if(fieldDef.isStructureFieldType()) { 
                    ModelElement_1_0 fieldDefType = model.getDereferencedType(fieldDef.getType());
                    if(!fieldDefType.isPrimitiveType()) {
                        collectedTypeDefs.put(
                            fieldDefType.jdoGetObjectId(),
                            fieldDefType
                        );
                    }
                    if(fieldDefType.isStructureType()) {
                        this.collectNestedTypeDefs(
                            fieldDefType,
                            collectedTypeDefs
                        );
                    }
                }
            }
        }
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
        Multiplicity multiplicity,
        Map<Object,ModelElement_1_0> collectedTypeDefs
    ) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        JSONObject content = new JSONObject();
        JSONObject applicationJson = new JSONObject();
        JSONObject applicationXml = new JSONObject();
        JSONObject schema = new JSONObject();
        if(multiplicity == Multiplicity.LIST) {
            schema.put("$ref", "#/components/schemas/" + typeDef.getQualifiedName() + LIST_SUFFIX);
        } else {
            List<ModelElement_1_0> subtypeDefs = new ArrayList<ModelElement_1_0>();
            Model_1_0 model = typeDef.getModel();
            for(Object subtype: typeDef.objGetSet("allSubtype")) {
                ModelElement_1_0 subtypeDef = model.getElement(subtype);
                if(!Boolean.TRUE.equals(subtypeDef.isAbstract())) {
                    subtypeDefs.add(subtypeDef);
                    collectedTypeDefs.put(subtypeDef.jdoGetObjectId(), subtypeDef);
                }
            }
            if(typeDef.isStructureType()) {
                this.collectNestedTypeDefs(
                    typeDef,
                    collectedTypeDefs
                );
            }
            JSONArray oneOf = new JSONArray();
            for(ModelElement_1_0 subTypeDef: subtypeDefs) {
                JSONObject type = new JSONObject();
                this.setType(
                    type, 
                    subTypeDef, 
                    Multiplicity.SINGLE_VALUE, 
                    false, 
                    "#/components/schemas/"
                );
                oneOf.put(type);
            }
            schema.put("oneOf", oneOf);
            JSONObject discriminator = new JSONObject();
            discriminator.put("propertyName", TYPE_PROPERTY);
            schema.put("discriminator", discriminator);
        }
        applicationJson.put("schema", schema);
        content.put("application/json", applicationJson);
        applicationXml.put("schema", schema);
        content.put("application/xml", applicationXml);
        response.put("content", content);
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
            elements = new TreeMap<String,ModelElement_1_0>(
                model.getStructuralFeatureDefs(
                    typeDef, 
                    true, // includeSubtypes
                    true, // includeDerived
                    false // attributesOnly
                    )
                ).values();
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
                    boolean isRef = this.setType(
                        property,
                        elementTypeDef,
                        ModelHelper.getMultiplicity(elementDef),
                        !elementTypeDef.isStructureType(),
                        "#/components/schemas/"
                    );
                    boolean isReadOnly = 
                        !elementDef.isStructureFieldType() && 
                        !ModelHelper.isChangeable(elementDef);
                    if(!isRef) {
                        property.put("readOnly", isReadOnly);
                        property.put("description", "<span title=\"" + elementDef.getQualifiedName() + "" + "\">&laquo;" + ModelHelper.getMultiplicity(elementDef).code()  + "&raquo; " + elementTypeDef.getQualifiedName() + "</span>");
                    }
                    if(!excludeProperties.contains(elementDef.getName())) {
                        properties.put(
                            elementDef.getName(), 
                            property
                        );
                        if(
                            ModelHelper.getMultiplicity(elementDef) == Multiplicity.SINGLE_VALUE && 
                            !isReadOnly
                        ) {
                        }
                    }
                } catch(Exception e) {
                    new ServiceException(e).log();
                }
            }
        }
        {
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
        JSONObject typeDefinition = this.newDefinition(
            typeDef,
            subTypes,
            excludeProperties
        );
        for(ModelElement_1_0 subtypeDef: subtypeDefs) {
            this.putAllDefinitions(
                subtypeDef,
                definitions,
                Collections.<String>emptySet()
            );
        }
        definitions.put(
            typeDef.getQualifiedName(),
            typeDefinition
        );
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
                // Collect subtypes of referenceType
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
                    ModelElement_1_0 queryDef = model.getElement("org:openmdx:kernel:Query");
                    collectedTypeDefs.put(
                        queryDef.jdoGetObjectId(),
                        queryDef
                    );
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
                            // requestBody
                            null,
                            // responses
                            this.newResponse().put(
                                "default",
                                this.newObjectResponse(
                                    referencedTypeDef,
                                    "List&lt;" + referencedTypeDef.getQualifiedName() + "&gt;",
                                    Multiplicity.LIST,
                                    collectedTypeDefs
                                )
                            )
                        )
                    );
                    operations.put(
                        "post",
                        this.newOperation(
                            // operationId
                            Identifier.OPERATION_NAME.toIdentifier(referenceDef.getName(), null, "get", null, null),
                            // description
                            "Retrieves the value for the reference &laquo;" + referenceDef.getName() + "&raquo for the specified query.",
                            // tags
                            this.newTags().put(referenceDef.getName()),                             
                            // parameters
                            null,
                            // requestBody
                            this.newRequestBody(
                                "in",
                                queryDef,
                                collectedTypeDefs
                            ),
                            // responses
                            this.newResponse().put(
                                "default",
                                this.newObjectResponse(
                                    referencedTypeDef,
                                    "List&lt;" + referencedTypeDef.getQualifiedName() + "&gt;",
                                    Multiplicity.LIST,
                                    collectedTypeDefs
                                )
                            )
                        )
                    );
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
                            // requestBody
                            null,
                            // responses
                            this.newResponse().put(
                                "default",
                                this.newObjectResponse(
                                    referencedTypeDef,
                                    referencedTypeDef.getQualifiedName(),
                                    Multiplicity.SINGLE_VALUE,
                                    collectedTypeDefs
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
                                // requestBody
                                null,
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
                                ),
                                // requestBody
                                this.newRequestBody(
                                    "in", 
                                    referencedTypeDef,
                                    collectedTypeDefs
                                ),
                                // responses
                                this.newResponse().put(
                                    "default", 
                                    this.newObjectResponse(
                                        referencedTypeDef,
                                        referencedTypeDef.getQualifiedName(),
                                        Multiplicity.SINGLE_VALUE,
                                        collectedTypeDefs
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
                                ),
                                // requestBody
                                this.newRequestBody(
                                    "in",
                                    referencedTypeDef,
                                    collectedTypeDefs
                                ),
                                // responses
                                this.newResponse().put(
                                    "default",
                                    this.newObjectResponse(
                                        referencedTypeDef,
                                        referencedTypeDef.getQualifiedName(),
                                        Multiplicity.SINGLE_VALUE,
                                        collectedTypeDefs
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
                                null,
                                // requestBody
                                this.newRequestBody(
                                    "in",
                                    inParamTypeDef,
                                    collectedTypeDefs
                                ),
                                // responses
                                this.newResponse().put(
                                    "default",
                                    this.newObjectResponse(
                                        resultParamTypeDef,
                                        resultParamTypeDef.getQualifiedName(),
                                        Multiplicity.SINGLE_VALUE,
                                        collectedTypeDefs
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
            api.put("openapi", "3.0.0");
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
            // servers
            JSONArray servers = new JSONArray();
            JSONObject server = new JSONObject();
            server.put("url", basePath);
            servers.put(server);
            api.put("servers", servers);
            // paths
            JSONObject paths = new JSONObject();
            Map<Object,ModelElement_1_0> collectedTypeDefs = new HashMap<Object,ModelElement_1_0>();
            this.getPaths(
                paths,
                collectedTypeDefs
            );
            // components
            JSONObject components = new JSONObject();
            // schemas
            JSONObject schemas = new JSONObject();
            components.put("schemas", schemas);
            // Path
            {
                JSONObject schema = new JSONObject();
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
                schema.put("properties", properties);
                schemas.put("Path", schema);
            }
            // PathElement
            {
                JSONObject schema = new JSONObject();
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
                schema.put("properties", properties);
                schemas.put("PathElement", schema);
            }
            // PathList
            {
                JSONObject schema = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/components/schemas/PathElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                schema.put("properties", properties);
                schemas.put("Path" + LIST_SUFFIX, schema);
            }
            // StringElement
            {
                JSONObject schema = new JSONObject();
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
                schema.put("properties", properties);
                schemas.put("StringElement", schema);
            }
            // StringList
            {
                JSONObject schema = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/components/schemas/StringElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                schema.put("properties", properties);
                schemas.put("String" + LIST_SUFFIX, schema);
            }
            // IntegerElement
            {
                JSONObject schema = new JSONObject();
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
                schema.put("properties", properties);
                schemas.put("IntegerElement", schema);
            }
            // IntegerList
            {
                JSONObject schema = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/components/schemas/IntegerElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                schema.put("properties", properties);
                schemas.put("Integer" + LIST_SUFFIX, schema);
            }
            // LongElement
            {
                JSONObject schema = new JSONObject();
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
                schema.put("properties", properties);
                schemas.put("LongElement", schema);
            }
            // LongList
            {
                JSONObject schema = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/components/schemas/LongElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                schema.put("properties", properties);
                schemas.put("Long" + LIST_SUFFIX, schema);
            }
            // DateElement
            {
                JSONObject schema = new JSONObject();
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
                schema.put("properties", properties);
                schemas.put("DateElement", schema);
            }
            // DateList
            {
                JSONObject schema = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/components/schemas/DateElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                schema.put("properties", properties);
                schemas.put("Date" + LIST_SUFFIX, schema);
            }
            // DateTimeElement
            {
                JSONObject schema = new JSONObject();
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
                schema.put("properties", properties);
                schemas.put("DateTimeElement", schema);
            }
            // DateTimeList
            {
                JSONObject schema = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/components/schemas/DateTimeElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                schema.put("properties", properties);
                schemas.put("DateTime" + LIST_SUFFIX, schema);
            }                 
            // BooleanElement
            {
                JSONObject schema = new JSONObject();
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
                schema.put("properties", properties);
                schemas.put("BooleanElement", schema);
            }
            // BooleanList
            {
                JSONObject schema = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/components/schemas/BooleanElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                schema.put("properties", properties);
                schemas.put("Boolean" + LIST_SUFFIX, schema);
            }                 
            // DecimalElement
            {
                JSONObject schema = new JSONObject();
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
                schema.put("properties", properties);
                schemas.put("DecimalElement", schema);
            }
            // DecimalList
            {
                JSONObject schema = new JSONObject();
                JSONObject properties = new JSONObject();
                // _item
                {
                    JSONObject property = new JSONObject();
                    JSONObject items = new JSONObject();
                    property.put("type", "array");
                    items.put("$ref", "#/components/schemas/DecimalElement");
                    property.put("items", items);
                    properties.put("_item", property);
                }
                schema.put("properties", properties);
                schemas.put("Decimal" + LIST_SUFFIX, schema);
            }
            for(ModelElement_1_0 typeDef: collectedTypeDefs.values()) {
                if(!schemas.has(typeDef.getQualifiedName())) {
                    this.putAllDefinitions(
                        typeDef,
                        schemas,
                        Collections.<String>emptySet()
                    );
                }
                // List<Type>
                {
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
                        Model_1_0 model = typeDef.getModel();
                        List<ModelElement_1_0> subtypeDefs = new ArrayList<ModelElement_1_0>();
                        for(Object subtype: typeDef.objGetSet("allSubtype")) {
                            ModelElement_1_0 subtypeDef = model.getElement(subtype);
                            if(!Boolean.TRUE.equals(subtypeDef.isAbstract())) {
                                subtypeDefs.add(subtypeDef);
                            }
                        }
                        JSONArray oneOf = new JSONArray();
                        for(ModelElement_1_0 subTypeDef: subtypeDefs) {
                            JSONObject subType = new JSONObject();
                            this.setType(
                                subType, 
                                subTypeDef,
                                Multiplicity.SINGLE_VALUE, 
                                false, 
                                "#/components/schemas/"
                            );
                            oneOf.put(subType);
                        }
                        JSONObject property = new JSONObject();
                        property.put("type", "array");
                        JSONObject items = new JSONObject();
                        items.put("oneOf", oneOf);
                        JSONObject discriminator = new JSONObject();
                        discriminator.put("propertyName", TYPE_PROPERTY);
                        items.put("discriminator", discriminator);                        
                        property.put("items", items);
                        properties.put("objects", property);
                    }
                    type.put("properties", properties);
                    schemas.put(
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
                        property.put("$ref", "#/components/schemas/" + typeDef.getQualifiedName());
                        properties.put(typeDef.getQualifiedName().replace(":", "."), property);
                    }
                    type.put("properties", properties);
                    schemas.put(
                        typeDef.getQualifiedName() + TYPE_SUFFIX, 
                        type
                    );
                }
            }
            api.put("paths", paths);
            api.put("components", components);
            api.put("info", info);
            api.write(out);
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
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
