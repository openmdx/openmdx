/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Role_1.java,v 1.24 2008/09/10 08:55:21 hburger Exp $
 * Description: Role_1 plugin
 * Revision:    $Revision: 1.24 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:21 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.layer.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.code.Stereotypes;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObjectFilter;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.naming.PathComponent;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;


//---------------------------------------------------------------------------
/**
 * Plugin which implements the generic model classes org:openmdx:compatibility:role1:Role 
 * and RoleType.
 *
 * @author anyff
 *
 */
@SuppressWarnings("unchecked")
public class Role_1
extends Standard_1 {

    //---------------------------------------------------------------------------
    // PathNObject
    //---------------------------------------------------------------------------

    //---------------------------------------------------------------------------
    class PathNObject {

        //---------------------------------------------------------------------------
        public PathNObject(Path path, ModelElement_1_0 object) {
            this.path = path;
            this.object = object;
        }

        //---------------------------------------------------------------------------
        public ModelElement_1_0 object() {
            return this.object;
        }

        //---------------------------------------------------------------------------
        public Path path() {
            return this.path;
        }

        //---------------------------------------------------------------------------
        // Variables
        //---------------------------------------------------------------------------
        private ModelElement_1_0 object = null;
        private Path path = null;
    }

    //---------------------------------------------------------------------------
    // QualifierAndRoleClass
    //---------------------------------------------------------------------------
    // contains the qualifier of the RoleType which leads to a certain 
    // role instance class and the type of this class
    class QualifierAndRoleClass {

        //---------------------------------------------------------------------------
        public QualifierAndRoleClass(
            String[] qualifiers,
            String[] qualifiersReferenceEnds,
            String roleClass,
            String qualifierLeadingToRoleClass
        ) throws ServiceException {
            if (qualifiers.length != qualifiersReferenceEnds.length) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "qualifiers and qualifiersReferenceEnds must have same length .",
                    new BasicException.Parameter("qualifiers", (Object[])qualifiers),
                    new BasicException.Parameter(
                        "qualifiersReferenceEnds",
                        (Object[])qualifiersReferenceEnds));
            }
            this.qualifiers = qualifiers;
            this.qualifiersReferenceEnds = qualifiersReferenceEnds;
            this.roleClass = roleClass;
            this.qualifierLeadingToRoleClass = qualifierLeadingToRoleClass;
        }

        //---------------------------------------------------------------------------        
        public String[] qualifiers() {
            return this.qualifiers;
        }

        //---------------------------------------------------------------------------        
        public String[] qualifiersReferenceEnds() {
            return this.qualifiersReferenceEnds;
        }

        //---------------------------------------------------------------------------        
        public String roleClass() {
            return this.roleClass;
        }

        //---------------------------------------------------------------------------        
        /** 
         * Get the qualifiers name which leads to the role class. This is 
         * typically 'id' or 'name'.
         * 
         * @return String name of the qualifier
         */
        public String qualifierLeadingToRoleClass() {
            return this.qualifierLeadingToRoleClass;
        }

        //---------------------------------------------------------------------------
        // Variables
        //---------------------------------------------------------------------------        
        private String[] qualifiersReferenceEnds = null;
        private String[] qualifiers = null;
        private String roleClass = null;
        private String qualifierLeadingToRoleClass = null;
    }

    //---------------------------------------------------------------------------
    // RoleTypeVirtualMap
    //---------------------------------------------------------------------------
    // Mapping virtual role types to role types
    class RoleTypeVirtualMap {

        //---------------------------------------------------------------------------
        public RoleTypeVirtualMap(
            List virtualPaths,
            List originalPaths,
            List virtualClass,
            List originalClass
        ) throws ServiceException {
            if (virtualPaths == null
                    || originalPaths == null
                    || virtualClass == null
                    || originalClass == null) {
                if (virtualPaths != null
                        || originalPaths != null
                        || virtualClass != null
                        || originalClass != null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        "Missing definitions for role type mapping.",
                        new BasicException.Parameter("virtualPaths", virtualPaths),
                        new BasicException.Parameter("originalPaths", originalPaths),
                        new BasicException.Parameter("virtualClasses", virtualClass),
                        new BasicException.Parameter(
                            "originalClasses",
                            originalClass));
                }
            }
            else if (
                    virtualPaths.size() != originalPaths.size()
                    || virtualClass.size() != originalClass.size()) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "Not the same count of original and virtual paths of virtual class and original class mappings.",
                    new BasicException.Parameter("virtualPaths", virtualPaths),
                    new BasicException.Parameter("originalPaths", originalPaths),
                    new BasicException.Parameter("virtualClasses", virtualClass),
                    new BasicException.Parameter("originalClasses", originalClass));
            }
            for (int i = 0; i < virtualPaths.size(); i++) {
                // tbd verification of paths with model 
                Path virtual = new Path((String)virtualPaths.get(i));
                Path original = new Path((String)originalPaths.get(i));
                roleTypeVirtualToOriginalMap.put(toReferencePath(virtual), original);
                SysLog.trace(
                    "RoleType Mapping: virtual path: "
                    + virtual
                    + " to real path: "
                    + original);
            }

            for (int i = 0; i < originalClass.size(); i++) {
                originalToVirtualClass.put(originalClass.get(i), virtualClass.get(i));
                SysLog.trace(
                    "RoleType Mapping: virtual class: "
                    + virtualClass.get(i)
                    + " to real class: "
                    + originalClass.get(i));
            }
        }

        /**
         * Convert the path supplied to a reference path. Reference path
         * is one containing wildcards for ids and it does not contain
         * the last id entry, if there is one.
         */ 
        protected Path toReferencePath(
            Path path
        ) {
            Path virtPath = null;
            if (path != null && path.size() > 1) {
                virtPath = path.getPrefix(2);

                int size = path.size();
                for (int i = 2; i < size; i++) {
                    if (i % 2 == 0) {
                        virtPath.add(":*");
                    }
                    else {
                        virtPath.add(path.get(i));
                    }
                }
                // cut final wildcard
                if (virtPath.getBase().equals(":*")) {
                    virtPath = virtPath.getParent();
                }
            }
            return virtPath;
        }

        /**
         * Return last component of path if it is an id.
         */
        protected String getBaseId(
            Path path
        ) {
            if ((path.size() % 2) == 1) {
                return path.getBase();
            }
            else {
                return null;
            }
        }        

        /**
         * Return true if the path is one of the configured paths leading to a
         * virtual RoleType.
         */
        public boolean isVirtualPath(Path path) {
            return roleTypeVirtualToOriginalMap.containsKey(toReferencePath(path));
        }

        /**
         * Assert that it is not a virtual path and throw exception if it is.
         */
        public void assertNoModificationByVirtualPath(
            Path path
        ) throws ServiceException {
            if (isVirtualPath(path)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Path is a virtual path to a role type. Modifications/creation is not allowed. Use original path instead.",
                    new BasicException.Parameter("virtual path", path),
                    new BasicException.Parameter(
                        "original path",
                        remapVirtualPath(path)));
            }
        }

        /**
         * Return the path to the original RoleType if the path supplied is a 
         * virtual path. Else return null.
         */
        public Path remapVirtualPath(
            Path path
        ) {
            String baseId = getBaseId(path);
            Path originalPath =
                (Path)roleTypeVirtualToOriginalMap.get(toReferencePath(path));
            if (originalPath != null) {
                originalPath = new Path(originalPath);
                if (baseId != null) {
                    originalPath.add(baseId);
                }
            }
            SysLog.trace("remapping path to: " + originalPath);
            return originalPath;
        }

        /**
         * Return the corresponding virtual class of the role type to the original 
         * class of the role type specified.
         */
        public String getVirtualClassName(String originalClassName) {
            return (String)originalToVirtualClass.get(originalClassName); 
        }

        //---------------------------------------------------------------------------
        // Variables
        //---------------------------------------------------------------------------
        private final Map originalToVirtualClass = new HashMap();
        private final Map roleTypeVirtualToOriginalMap = new HashMap();
    }

    //---------------------------------------------------------------------------
    // RoleTypeReference
    //---------------------------------------------------------------------------
    class RoleTypeReference {

        //---------------------------------------------------------------------------
        public RoleTypeReference(
        ) {
            roleTypeReferenceMap = new HashMap();
        }

        //---------------------------------------------------------------------------
        /** 
         * prepare roleTypeReferenceMap. 
         * <p>
         * roleTypeReferenceMap holds for each RoleType class the role instance 
         * class it is referencing, all the references qualifiers and the 
         * referenceEnd's name (which occurs in a path) 
         */
        public void init(
            Map roleClassToRoleReferenceMap,
            Model_1_0 model
        ) throws ServiceException {
            ModelElement_1_0 roleTypeBase = model.getDereferencedType("org:openmdx:compatibility:role1:RoleType");
            for(
                    Iterator i = roleTypeBase.getValues("allSubtype").iterator();
                    i.hasNext();
            ) {
                Path roleTypePath = (Path)i.next();
                ModelElement_1_0 roleType = model.getDereferencedType(roleTypePath);

                // only role role types which are not direct subclasses of RoleType
                if(
                        (roleType != roleTypeBase)  && 
                        !roleTypeBase.values("subtype").contains(roleTypePath)
                ) {
                    Path referencedClassName = null;
                    List attributeNames = new ArrayList();
                    List referenceEnds = new ArrayList();

                    // now get the references in the roleType
                    for (
                            Iterator refIter = roleType.values("content").iterator();
                            refIter.hasNext();
                    ) {
                        ModelElement_1_0 reference = model.getElement(refIter.next());
                        if(model.isReferenceType(reference)) {
                            Path referencedEndPath = (Path) reference.values("referencedEnd").get(0);
                            ModelElement_1_0 referencedEnd = model.getElement(referencedEndPath);
                            if (referencedClassName == null) {
                                referencedClassName = (Path) referencedEnd.values("type").get(0);                        
                                // must only be done once per RoleType classes which 
                                // have the same path:
                                Role_1.this.prepareReferencePaths(
                                    roleType,
                                    (Path)referencedEnd.values("type").get(0),
                                    roleClassToRoleReferenceMap
                                );
                            }
                            else if(!referencedClassName.equals(referencedEnd.values("type").get(0))) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.ASSERTION_FAILURE, 
                                    "References from RoleType to Role instances must all lead to the same Role instance class.",
                                    new BasicException.Parameter("RoleType", roleType.values("qualifiedName"))
                                );
                            }
                            // else they are the same which is ok
                            attributeNames.add(referencedEnd.values("qualifierName").get(0));
                            referenceEnds.add(referencedEnd.values("name").get(0));
                        }
                    }

                    // need to find qualifier name which leads to the class 
                    // (the one of the reference from the container to the class)
                    if (referencedClassName != null) {
                        ModelElement_1_0 referencedClass = model.getElement(referencedClassName); 
                        ModelElement_1_0 compositeReference = model.getElement(referencedClass.getValues("compositeReference").get(0));
                        ModelElement_1_0 referencedEnd = model.getElement(compositeReference.getValues("referencedEnd").get(0));                                        
                        String qualifier = (String)referencedEnd.getValues("qualifierName").get(0);
                        this.setQualifiers(
                            roleTypePath.getBase(),
                            (String[]) attributeNames.toArray(new String[0]),
                            (String[]) referenceEnds.toArray(new String[0]),
                            referencedClassName.getBase(),
                            qualifier
                        );
                    }
                    else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE, 
                            "Missing reference to role instance in model for RoleType.",
                            new BasicException.Parameter("RoleType", roleType.values("qualifiedName"))
                        );
                    }
                }
            }
        } 

        //---------------------------------------------------------------------------
        /**
         * set qualifiers and roleClass for role type. returns any existing old
         * entry.
         */ 
        protected QualifierAndRoleClass setQualifiers(
            String roleTypeClass,
            String[] qualifiers,
            String[] qualifiersReferenceEnds,
            String roleClass,
            String qualifierLeadingToRoleClass
        ) throws ServiceException {
            Object entry = roleTypeReferenceMap.get(roleTypeClass);
            if (entry == null) {
                roleTypeReferenceMap.put(
                    roleTypeClass,
                    new QualifierAndRoleClass(
                        qualifiers,
                        qualifiersReferenceEnds,
                        roleClass,
                        qualifierLeadingToRoleClass));
            }
            return entry == null ? null : (QualifierAndRoleClass)entry;
        }

        //---------------------------------------------------------------------------
        /** 
         * Get all the qualifiers for this roleTypeClass. 
         * <p>
         * throws exception if roleTypeClass is unknown.
         */
        public String[] getQualifiers(
            String roleTypeClass
        ) throws ServiceException {
            String[] qualifiers = null;
            Object obj = roleTypeReferenceMap.get(roleTypeClass);
            if (obj != null) {
                QualifierAndRoleClass entry = (QualifierAndRoleClass)obj;
                qualifiers = entry.qualifiers();
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "unknown class of RoleType. RoleType classes must be in model.",
                    new BasicException.Parameter(
                        "RoleTypeClass",
                        roleTypeClass));
            }
            return qualifiers;
        }

        //---------------------------------------------------------------------------
        /** 
         * Get the qualifier of reference leading to the role instance class
         * for this role type class. 
         * <p>
         * throws exception if roleTypeClass is unknown.
         */
        public String getQualifierLeadingToRoleClass(
            String roleTypeClass
        ) throws ServiceException {
            String qualifier = null;
            Object obj = roleTypeReferenceMap.get(roleTypeClass);
            if (obj != null) {
                QualifierAndRoleClass entry = (QualifierAndRoleClass)obj;
                qualifier = entry.qualifierLeadingToRoleClass();
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "unknown class of RoleType. RoleType classes must be in model.",
                    new BasicException.Parameter(
                        "RoleTypeClass",
                        roleTypeClass));
            }
            return qualifier;
        }

        //---------------------------------------------------------------------------
        public String getQualifierForReferenceEnd(
            String roleTypeClass, 
            String pathComponent
        ) throws ServiceException {
            String qualifier = null;
            Object obj = roleTypeReferenceMap.get(roleTypeClass);
            if (obj != null) {
                QualifierAndRoleClass entry = (QualifierAndRoleClass)obj;
                String[] referenceEnds = entry.qualifiersReferenceEnds();
                String[] qualifiers = entry.qualifiers();
                for (int i = 0;
                i < referenceEnds.length && qualifier == null;
                i++) {
                    if (referenceEnds[i].equals(pathComponent)) {
                        qualifier = qualifiers[i];
                    }
                }
            }
            if (qualifier == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "unknown class of RoleType or associationEnd therein.",
                    new BasicException.Parameter("RoleTypeClass", roleTypeClass),
                    new BasicException.Parameter(
                        "AssociationEnd",
                        pathComponent));
            }
            return qualifier;
        }

        //---------------------------------------------------------------------------
        /**
         * get the role class for the roleTypeClass for the qualifier.
         * <p>
         * throws exception if roleTypeClass is unknown
         */
        public String getRoleClass(
            String roleTypeClass
        ) throws ServiceException {
            String roleClass = null;
            Object obj = roleTypeReferenceMap.get(roleTypeClass);
            if (obj != null) {
                QualifierAndRoleClass entry = (QualifierAndRoleClass)obj;
                roleClass = entry.roleClass();
            }

            if (roleClass == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "unknown class of RoleType or qualifier therein.",
                    new BasicException.Parameter(
                        "RoleTypeClass",
                        roleTypeClass));
            }
            return roleClass;
        }

        //---------------------------------------------------------------------------
        // Variables
        //---------------------------------------------------------------------------
        private final HashMap roleTypeReferenceMap;
    }

    //---------------------------------------------------------------------------
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception, ServiceException {

        super.activate(id, configuration, delegation);

        // check for old configuration entries
        if (configuration.getValues("RoleReferencePaths").length != 0
                || configuration.getValues("RoleTypeReferencePaths").length != 0) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "These configuration options are no longer supported. The settings are read from the model directly.",
                new BasicException.Parameter(
                    "option",
                "[RoleReferencePaths, RoleTypeReferencePaths]"));
        }

        if (configuration
                .getValues("RoleTypeRemappingVirtualReferencePath")
                .length
                != 0
                || configuration.getValues("RoleTypeRemappingOriginalPath").length != 0
                || configuration.getValues("RoleTypeRemappingVirtualClass").length != 0
                || configuration.getValues("RoleTypeRemappingOriginalClass").length
                != 0) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "These configuration options are no longer supported, use the new ones instead.",
                new BasicException.Parameter(
                    "old options",
                "[RoleTypeRemappingVirtualReferencePath, RoleTypeRemappingOriginalPath, RoleTypeRemappingVirtualClass, RoleTypeRemappingOriginalClass]"),
                new BasicException.Parameter(
                    "new options",
                    LayerConfigurationEntries
                    .ROLE_TYPE_MAPPING_VIRTUAL_REFERENCE_PATH,
                    LayerConfigurationEntries.ROLE_TYPE_MAPPING_REAL_PATH,
                    LayerConfigurationEntries.ROLE_TYPE_MAPPING_VIRTUAL_CLASS,
                    LayerConfigurationEntries.ROLE_TYPE_MAPPING_REAL_CLASS)
            );
        }

        // prepare role type remapping map
        try {

            roleTypeVirtualMap =
                new RoleTypeVirtualMap(
                    configuration.values(
                        LayerConfigurationEntries
                        .ROLE_TYPE_MAPPING_VIRTUAL_REFERENCE_PATH),
                        configuration.values(
                            LayerConfigurationEntries.ROLE_TYPE_MAPPING_REAL_PATH),
                            configuration.values(
                                LayerConfigurationEntries.ROLE_TYPE_MAPPING_VIRTUAL_CLASS),
                                configuration.values(
                                    LayerConfigurationEntries.ROLE_TYPE_MAPPING_REAL_CLASS));
        }
        catch (ServiceException se) {
            throw new ServiceException(
                se,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Error in configuration of Options.",
                new BasicException.Parameter(
                    "Option",
                    LayerConfigurationEntries
                    .ROLE_TYPE_MAPPING_VIRTUAL_REFERENCE_PATH),
                    new BasicException.Parameter(
                        "Option",
                        LayerConfigurationEntries.ROLE_TYPE_MAPPING_REAL_PATH),
                        new BasicException.Parameter(
                            "Option",
                            LayerConfigurationEntries.ROLE_TYPE_MAPPING_VIRTUAL_CLASS),
                            new BasicException.Parameter(
                                "Option",
                                LayerConfigurationEntries.ROLE_TYPE_MAPPING_REAL_CLASS));
        }
        List models = configuration.values(SharedConfigurationEntries.MODEL);
        if (models.size() > 0) {
            this.model = (Model_1_0)models.get(0);
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "A model must be configured with options 'modelPackage' and 'packageImpl'"
            );
        }
        this.referencePathClassMap = new HashMap();
        Map roleClassToReferencePathMap = new HashMap();
        this.inspectModelForReferencePaths(
            null,
            model.getElement(AUTHORITY_TYPE_NAME),
            referencePathClassMap,
            roleClassToReferencePathMap);
        this.roleTypeReference = new RoleTypeReference();
        roleTypeReference.init(roleClassToReferencePathMap, this.model);
    }

    /**
     * returns the actual time
     */
    protected String timeNow() {
        String timePoint = DateFormat.getInstance().format(new Date());
        return timePoint;
    }

    /** 
     * Prepare the reference paths (a path containing no id's just references)
     * for the role type / role mapping.
     * <p>
     * This must not be done for each RoleType, Role combination. For RoleTypes
     * which have the same parent it must only be done once. 
     * 
     * @param roleType  the RoleType class 
     * @param roleInstancePath  path to the corresponding role instance
     */
    void prepareReferencePaths(
        ModelElement_1_0 roleTypeDef,
        Path rolePath,
        Map roleClassToRoleReferenceMap
    ) throws ServiceException {

        ModelElement_1_0 roleDef = this.model.getElement(rolePath.getBase());

        // the map should contain the path to one of the supertypes of roleType:
        // iterate in reverse, to get closer parents first
        List roleReferences = new ArrayList();
        for (Iterator i = roleDef.values("allSupertype").iterator();
        i.hasNext();
        ) {
            Path roleReference =
                (Path)roleClassToRoleReferenceMap.get(((Path)i.next()).getBase());
            if (roleReference != null) {
                roleReferences.add(roleReference);
            }
        }

        // get path to roleType.
        // The map contains the parent of roleType as entry 
        Path roleTypeReferencePath = null;
        for (Iterator iter = roleTypeDef.values("supertype").iterator();
        iter.hasNext() && roleTypeReferencePath == null;
        ) {
            String supertype = ((Path)iter.next()).getBase();
            if (!supertype.equals("org:openmdx:base:BasicObject")
                    && !supertype.equals("org:openmdx:compatibility:role1:RoleType")) {
                // hope we have it now. How else could we find out?
                roleTypeReferencePath =
                    (Path)roleClassToRoleReferenceMap.get(supertype);
            }
        }

        if ((roleTypeReferencePath == null) || (roleReferences.size() == 0)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "no mapping between Role and RoleType.",
                new BasicException.Parameter(
                    "RoleType",
                    roleTypeDef.values("qualifiedName")),
                    new BasicException.Parameter(
                        "RoleType reference path",
                        roleTypeReferencePath),
                        new BasicException.Parameter(
                            "Role",
                            roleDef.values("qualifiedName")),
                            new BasicException.Parameter(
                                "Role instance reference path",
                                roleReferences));
        }
        else {
            Path roleReference = (Path)roleReferences.get(0);
            if (!roleTypePathPatternMap.containsKey(roleReference)) {
                this.roleTypePathPatternMap.put(
                    roleReference,
                    roleTypeReferencePath);
                this.roleTypePathPatternMap.put(
                    roleTypeReferencePath,
                    roleReference);
                this.allRoleTypeReferencePaths.add(roleTypeReferencePath);
                SysLog.trace(
                    "roleType to role mapping: "
                    + roleReference
                    + " to "
                    + roleTypeReferencePath);
            }
        }
    }

    // --------------------------------------------------------------------------
    /** 
     * Find all the classes which are reachable from the startPoint.path or 
     * its subclasses through a reference. Add those classes to the 
     * reachableClasses collection, together with the path leading to the new 
     * class.
     * 
     * @param of  path up to the startClass 
     * @param closure  list of all class and Path, new found ones
     *                           get added.
     */
    private void getTransitiveClosure(
        PathNObject ofClass,
        List closure
    ) throws ServiceException {

        ModelElement_1_0 type = ofClass.object();
        Path path = ofClass.path();

        // for each subtype of type (this contains also the type itself)
        for (Iterator i = type.values("allSubtype").iterator(); i.hasNext();) {
            Path instancePath = path == null ? null : new Path(path);
            Path subTypeModelPath = (Path)i.next();
            ModelElement_1_0 subTypeObj =
                this.model.getDereferencedType(subTypeModelPath);
            for (Iterator j = subTypeObj.values("content").iterator();
            j.hasNext();
            ) {
                ModelElement_1_0 contentObj = this.model.getElement(j.next());

                // note: we really need a composite aggregation here. Introducing
                // shared aggregation requires first defining the semantics of a 
                // shared aggregation together with roles. 

                // check if it is a reference
                if (this.model.isReferenceType(contentObj)) {
                    //contentObj.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.REFERENCE)) {
                    Path refendPath = (Path)contentObj.values("referencedEnd").get(0);
                    ModelElement_1_0 refend = this.model.getElement(refendPath);

                    // only interested in aggregated objects
                    if (AggregationKind
                            .COMPOSITE
                            .equals(refend.values("aggregation").get(0))) {
                        // System.out.println(" old criteria  instancePath: " + instancePath + " contentObj: " + contentObj.getValues("qualifiedName"));

                        Path nextClassTypePath = (Path)refend.values("type").get(0);
                        ModelElement_1_0 nextClassType =
                            this.model.getDereferencedType(nextClassTypePath);
                        String pathComponent = (String)refend.values("name").get(0);
                        if (path == null) {
                            nextClassTypePath = new Path(pathComponent);
                        }
                        else {
                            nextClassTypePath = new Path(instancePath);
                            nextClassTypePath.add(pathComponent);
                        }
                        closure.add(new PathNObject(nextClassTypePath, nextClassType));
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Prepare the referencePathClassMap iterative
     */
    private void inspectModelForReferencePaths(
        Path path,
        ModelElement_1_0 rootElement,
        Map referencePathToObjectMap,
        Map roleClassToRoleReferencePathMap
    ) throws ServiceException {
        List openClassesAndPaths = new ArrayList();
        Set visitedClasses = new HashSet();
        PathNObject startPoint = new PathNObject(path, rootElement);
        openClassesAndPaths.add(startPoint);      
        while (openClassesAndPaths.size() > 0) {
            startPoint = (PathNObject) openClassesAndPaths.remove(0);        
            if(visitedClasses.add(startPoint.object())) {
                referencePathToObjectMap.put(
                    startPoint.path(), 
                    startPoint.object()
                );
                roleClassToRoleReferencePathMap.put(
                    startPoint.object().values("qualifiedName").get(0),
                    startPoint.path()
                );
                this.getTransitiveClosure(
                    startPoint, 
                    openClassesAndPaths
                );
            }
        }
    }

    //---------------------------------------------------------------------------
    /** 
     * Get the path consisting of the references in the path supplied. 
     * 
     * @param path 
     * 
     * @return new path
     */ 
    private Path getPathWithRemovedObjectIds(
        Path path
    ) {
        Path result = new Path(path);
        for(int i = 0; i < result.size(); i++) {
            result.remove(i);
        }
        return result;
    }

    //---------------------------------------------------------------------------
    /** 
     * For a path to Role, this returns the corresponding path to RoleType and 
     * for a path to RoleType returns the corresponding path to the Role 
     * instances.
     * <p> 
     * The path supplied may be the original path and may contain role entries
     * <p>
     * The path returned does not contain the last id segment. This allows 
     * usage of this method for path's with or without the last id.
     * 
     * @return role type reference path
     * 
     * @throws ServiceException if objectPath does not match a configured RoleType to Role mapping.
     */ 
    private Path getRoleTypesPath(
        Path objectPath
    ) throws ServiceException {
        Path normalizedObjectPath = new Path(objectPath);
        this.toRolelessPath(normalizedObjectPath);
        this.toStatelessPath(normalizedObjectPath);
        Path roleTypePathPattern =
            (Path)this.roleTypePathPatternMap.get(
                this.getPathWithRemovedObjectIds(normalizedObjectPath));

        // complete path pattern with ids of objectPath
        if (roleTypePathPattern != null) {
            Path roleTypesPath = null;
            for (int i = 0; i < roleTypePathPattern.size(); i++) {
                if (i == 0) {
                    roleTypesPath =
                        new Path(new String[] { normalizedObjectPath.get(0)});
                }
                else {
                    roleTypesPath.add(normalizedObjectPath.get(i * 2));
                }
                roleTypesPath.add(roleTypePathPattern.get(i));
                if (!normalizedObjectPath
                        .get((i * 2) + 1)
                        .equals(roleTypePathPattern.get(i))) {
                    break;
                }
            }
            return roleTypesPath;
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Missing path pattern in RoleType to Role mapping.",
                new BasicException.Parameter("path", objectPath));
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Helper method for reading RoleType's from DB
     * <p>
     * The roleTypeCache serves as cache. The roleTypes get accessed
     * several times during a request. To prevent multiple access to storage,
     * they are stored in roleTypeCache and can be reused later on. 
     * <p>
     * Does not return null.
     * 
     * @param role  role for which the roleType is searched
     * @param roleTypeCache cache which holds already retrieved role types
     * @param header used for access to storage
     * @param roleTypeBasePath  path at which roleTypes are.
     */
    private DataproviderObject getRoleType(
        ServiceHeader header,
        DataproviderRequest request,
        String role,
        Map roleTypeCache,
        Path roleTypeBasePath
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("getRoleType");
        if (roleTypeBasePath == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "RoleObject_1.getRoleType(): empty roleTypeBasePath not allowed.",
                new BasicException.Parameter("role", role));
        }

        DataproviderObject roleType = null;
        ServiceException exception = null;

        Path roleTypePath = new Path(roleTypeBasePath);
        roleTypePath.add(role);

        roleType = (DataproviderObject)roleTypeCache.get(roleTypePath);
        if (roleType == null) { // not in cache, access from storage
            DataproviderReply roleTypeReply = null;

            DataproviderRequest roleRequest =
                new DataproviderRequest(
                    new DataproviderObject(roleTypePath),
                    DataproviderOperations.OBJECT_RETRIEVAL,
                    AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                    null);
            roleRequest.contexts().putAll(request.contexts());

            // TODO OBJECT_TYPE of the roleType must be set here
            roleRequest.context(DataproviderRequestContexts.OBJECT_TYPE).set(0,"UNDEF");

            try {
                // get through this plugin to allow for virtual role types path mapping
                StopWatch_1.instance().startTimer("getRoleType-DB");
                roleTypeReply = get(header, roleRequest);
                StopWatch_1.instance().stopTimer("getRoleType-DB");
            }
            catch (ServiceException e) {
                exception = e;
            }

            if (exception != null
                    || roleTypeReply == null
                    || (roleTypeReply != null
                            && roleTypeReply.getObjects().length == 0)) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "role type not found at path.",
                    new BasicException.Parameter("path", roleTypeBasePath),
                    new BasicException.Parameter("role type", role));
            }
            else {
                // valid reply, add to cache
                roleType = roleTypeReply.getObject();
                roleTypeCache.put(roleTypePath, roleType);
            }
        }
        StopWatch_1.instance().stopTimer("getRoleType");
        return roleType;
    }

    //---------------------------------------------------------------------------
    /** 
     * Get the object states. In presence of stated classes this may return 
     * several states. On the first invocation the states get added to the 
     * request context, on the further invocations these states are returned.
     * Like that, a DB access can be saved in case of set operation. 
     * <p>
     * The states returned are only valid ones and are ordered ascendingly 
     * according to validFrom.
     * <p>
     * If there is no time range, all valid states are returned.
     * <p>
     * object must be valid at header.requestedAt, header.requestedFor. 
     * <p>
     * throws NOT_FOUND exception if the states can not be found. 
     */
    private DataproviderObject[] findCoreObjectStates(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("findCoreObjectStates");
        DataproviderObject[] results = null;

        // check if it was already retrieved
        if (request.contexts().containsKey(ROLE_OBJECT_STATES)) {
            results =
                (DataproviderObject[])request.contexts().get(ROLE_OBJECT_STATES);
        }
        else { // must retrieve the states
            Path noRolePath = toRolelessPath(new Path(request.path()));
            DataproviderRequest findRequest = null;

            // get the object from storage.
            // (do not replace with a find() request, the core must be present
            // in all the cases where findCoreObjectStates() gets called.)
            findRequest =
                new DataproviderRequest(
                    new DataproviderObject(noRolePath),
                    DataproviderOperations.ITERATION_START,
                    null,
                    0,
                    Integer.MAX_VALUE,
                    Directions.ASCENDING,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null);
            findRequest.contexts().putAll(request.contexts());
            results =
                new DataproviderObject[] {
                super.get(header, findRequest).getObject()};

            // must throw exception if not found; get() for the non stated case
            // throws exception anyway.
            if (results.length == 0) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "No valid object or object states found. Are there valid states to update?",
                    new BasicException.Parameter("path", request.path()),
                    new BasicException.Parameter(
                        "request.validFrom",
                        request.object().getValues(State_1_Attributes.VALID_FROM)),
                        new BasicException.Parameter(
                            "request.validTo",
                            request.object().getValues(State_1_Attributes.VALID_TO)));
            }
            request.contexts().put(ROLE_OBJECT_STATES, results);
        }
        StopWatch_1.instance().stopTimer("findCoreObjectStates");
        return results;
    }

    //---------------------------------------------------------------------------
    /**
     * assert multiplicity of attributes defined to have at least one value 
     * of the new role to add. Only check for attributes of the role.
     * <p>
     * This is only needed in the case of an overhanging update. In this case,
     * type.strict does not check for the attributes existence if it is defined
     * to exist.
     */
    private void assertRoleAttributeMultiplicity(
        DataproviderObject request,
        DataproviderObject roleType
    ) throws ServiceException {
        ModelElement_1_0 roleInstanceClass =
            this.model.getDereferencedType(
                this.roleTypeReference.getRoleClass(
                    (String)roleType.getValues(SystemAttributes.OBJECT_CLASS).get(
                        0)));

        ModelElement_1_0 requestClass =
            this.model.getDereferencedType(
                request.getValues(SystemAttributes.OBJECT_CLASS).get(0));

        for (Iterator si = requestClass.getValues("allSupertype").iterator();
        si.hasNext();
        ) {
            String classNameToCheck = ((Path)si.next()).getBase();

            // if the class is derived from roleInstanceClass it must be 
            // checked if it has an attribute with required values.
            ModelElement_1_0 supertype =
                this.model.getDereferencedType(classNameToCheck);

            if (supertype
                    .getValues("allSupertype")
                    .contains(roleInstanceClass.getValues("subtype").get(0))) {
                // check for required attributes
                for (Iterator content = supertype.getValues("content").iterator();
                content.hasNext();
                ) {
                    ModelElement_1_0 modelContent =
                        this.model.getElement(((Path)content.next()).getBase());
                    if ("org:omg:model1:Attribute"
                            .equals(
                                modelContent.getValues(SystemAttributes.OBJECT_CLASS).get(0))
                                && (
                                        (String)modelContent.getValues("multiplicity").get(
                                            0)).startsWith(
                                            "1..")
                                            && (request
                                                    .getValues((String)modelContent.getValues("name").get(0))
                                                    == null
                                                    || request
                                                    .getValues((String)modelContent.getValues("name").get(0))
                                                    .isEmpty())) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            "trying to extend valid period of state. Multiplicity of attribute violated",
                            new BasicException.Parameter("request", request),
                            new BasicException.Parameter(
                                "attribute",
                                modelContent.getValues("name").get(0)));
                    }
                }
            }
        }
    }

    //---------------------------------------------------------------------------
    /** 
     * check that the object has valid role information of the current role.
     * <ul> 
     * <li> role must be configured as RoleType. </li>
     * <li> the class of the object must correspond to the role  </li>
     * </ul>
     * returns the RoleType model class of the role.
     * 
     * @param  role   role to check
     * @param  roleTypeCache  Cache containing roleTypes retrieved already.
     * @param  header   header for getting RoleType from storage
     * @param  request  request as received
     * 
     * @return roleType of the role specified
     */ 
    private DataproviderObject assertCompleteRoleType(
        ServiceHeader header,
        DataproviderRequest request,
        String role, 
        Map roleTypeCache,
        DataproviderObject_1_0 requestObject
    ) throws ServiceException {
        DataproviderObject roleType = null;

        //
        // check for existence of the RoleType
        //
        Path roleTypeBasePath = this.getRoleTypesPath(requestObject.path());
        roleType =
            getRoleType(header, request, role, roleTypeCache, roleTypeBasePath);

        if (roleType != null) { // case not found and warn only it may be null
            //
            // check for correct class
            //
            SparseList objectClass =
                roleType.getValues(SystemAttributes.OBJECT_CLASS);

            // objectClass may be null if NO_ATTRIBUTES was selected. This is
            // asserted in some other layer.
            if (objectClass != null) {
                ModelElement_1_0 requestClass =
                    this.model.getDereferencedType(
                        requestObject.values(SystemAttributes.OBJECT_CLASS).get(0));

                ModelElement_1_0 roleInstanceClass =
                    this.model.getDereferencedType(
                        this.roleTypeReference.getRoleClass(
                            (String)objectClass.get(0)));

                if (!classBelongsToRole(requestClass, roleInstanceClass)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Object is not of correct class for role.",
                        new BasicException.Parameter(
                            "object path",
                            requestObject.path()),
                            new BasicException.Parameter("role", role),
                            new BasicException.Parameter(
                                SystemAttributes.OBJECT_CLASS,
                                requestObject.values(SystemAttributes.OBJECT_CLASS).get(0)),
                                new BasicException.Parameter(
                                    "required object_class",
                                    roleInstanceClass.getValues("qualifiedName")));
                }
            }
        }
        return roleType;
    }

    // --------------------------------------------------------------------------
    /** 
     * A role can lead to different classes which are an extension 
     * of the role. A certain subRole may require a certain core 
     * class because it is only derived from this class but not from 
     * another class at the role. This is asserted here.
     * <p>
     * loadedObjects is an array to contain different states of an object.
     */
    private void assertCoreRoleExtensionClass(
        ServiceHeader header,
        DataproviderRequest request,
        String role,
        Map roleTypeCache, 
        List requiredRoles,
        DataproviderObject[] loadedObjects,
        DataproviderObject requested
    ) throws ServiceException {
        String loadedCoreClassName = null;

        ModelElement_1_0 requestClass =
            this.model.getDereferencedType(
                requested.values(SystemAttributes.OBJECT_CLASS).get(0));

        for (int l = 0; l < loadedObjects.length; l++) {
            DataproviderObject loaded = loadedObjects[l];
            String nextLoadedCoreClassName = null;

            if (requiredRoles.size() > 1) {
                String coreRole = (String)requiredRoles.get(1);
                nextLoadedCoreClassName =
                    (String)loaded
                    .getValues(
                        coreRole
                        + ROLE_ATTRIBUTE_SEPARATOR
                        + SystemAttributes.OBJECT_CLASS)
                        .get(0);
            }
            else {
                // no required roles: must be core itself
                nextLoadedCoreClassName =
                    (String)loaded.getValues(SystemAttributes.OBJECT_CLASS).get(0);
            }

            // it is unnessecary to do the check for all loaded object states
            // if they have the same core class.
            if (!nextLoadedCoreClassName.equals(loadedCoreClassName)) {
                loadedCoreClassName = nextLoadedCoreClassName;

                ModelElement_1_0 coreClass =
                    this.model.getDereferencedType(loadedCoreClassName);

                ArrayList diffList = new ArrayList();
                Path superType = null;
                for (Iterator si =
                    requestClass.getValues("allSupertype").iterator();
                si.hasNext();
                ) {
                    superType = (Path)si.next();
                    if (!coreClass.getValues("allSupertype").contains(superType)) {
                        diffList.add(superType.getBase());
                    }
                    // else just try next
                }

                // remaining classes must all be classes of the role (the class itself
                // or an extension.)
                Path roleTypeBasePath = this.getRoleTypesPath(requested.path());
                DataproviderObject roleType =
                    this.getRoleType(
                        header,
                        request,
                        role,
                        roleTypeCache,
                        roleTypeBasePath);

                ModelElement_1_0 roleInstanceClass =
                    this.model.getDereferencedType(
                        roleTypeReference.getRoleClass(
                            (String)roleType.getValues(
                                SystemAttributes.OBJECT_CLASS).get(
                                    0)));

                for (Iterator i = diffList.iterator(); i.hasNext();) {
                    String type = (String)i.next();

                    if (!classBelongsToRole(this.model.getDereferencedType(type),
                        roleInstanceClass)) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            "Existing core role class does not allow subclass for new role.",
                            new BasicException.Parameter("new role", role),
                            new BasicException.Parameter(
                                "new requested class",
                                requested.values(SystemAttributes.OBJECT_CLASS).get(0)),
                                new BasicException.Parameter(
                                    "existing core class",
                                    coreClass),
                                    new BasicException.Parameter("missing class", type));
                    }
                }
            }
        }
    }

    //---------------------------------------------------------------------------
    /**
     * assert that role is not null.
     */
    private void assertRoleNotNull(
        String role, 
        DataproviderObject object
    ) throws ServiceException {
        if (role == null || role.length() == 0) {
            ServiceException error =
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Object is role but path does not specify a role.",
                    new BasicException.Parameter(
                        SystemAttributes.OBJECT_CLASS,
                        object.values(SystemAttributes.OBJECT_CLASS).get(0)),
                        new BasicException.Parameter("object path", object.path()));
            throw error;
        }
    }

    // --------------------------------------------------------------------------
    /** 
     * Ensure that the qualifying attributes for the role to update
     * have unique values. Only the qualifying attributes values in the request
     * object are checked. Any existing values were checked on role creation,
     * overhanging states must be checked separately. 
     * <p>
     * This includes a search on the db for each of the qualifying attributes. 
     */
    private void assertMaintainingUniqueRoleInstance(
        ServiceHeader header, 
        DataproviderRequest request,  
        DataproviderObject roleType,
        List requiredRoles,
        DataproviderObject[] loadedStates,
        boolean replaceOp
    ) throws ServiceException {
        String roleName = roleType.path().getBase();
        DataproviderObject requested = request.object();

        String[] qualifiers =
            roleTypeReference.getQualifiers(
                (String)roleType.values(SystemAttributes.OBJECT_CLASS).get(0));

        for (int i = 0; i < qualifiers.length; i++) {
            // first check if the attribute is part of the request object
            if (request.object().attributeNames().contains(qualifiers[i])) {
                // there is a qualifying attribute in the request,
                // need to know to which role it belongs (Note: it must be 
                // present in any non overhanging updates)

                Object qualifyingValue = null;
                if (request.object().values(qualifiers[i]).size() > 0) {
                    qualifyingValue = request.object().values(qualifiers[i]).get(0);
                }

                String qualiRole =
                    findRoleForAttribute(qualifiers[i], loadedStates, requiredRoles);

                // qualiRole == null -> it must be the qualifier, which is asserted
                // to be unique elsewhere.
                if (qualiRole != null) {
                    String roleCompleteQualifyingAttribute = null;

                    if (qualiRole.length() > 0) {
                        roleCompleteQualifyingAttribute =
                            qualiRole + ROLE_ATTRIBUTE_SEPARATOR + qualifiers[i];
                    }
                    else { // it's an attribute of the core
                        roleCompleteQualifyingAttribute = qualifiers[i];
                    }
                    // qualifying attribute value is not allowed to become null
                    if (qualifyingValue == null) {
                        if (replaceOp) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "no value for qualifying role type attribute.",
                                new BasicException.Parameter(
                                    "qualifying attribute",
                                    qualifiers[i]),
                                    new BasicException.Parameter("role", roleName),
                                    new BasicException.Parameter(
                                        "object.path",
                                        requested.path()));
                        }
                        else {
                            // in an modify Operation it does not matter if the 
                            // attributes value is null or the attribute is not 
                            // present at all
                            request.object().attributeNames().remove(qualifiers[i]);
                        }
                    }
                    else {
                        // assert uniqueness for the whole periode of the update
                        assertUniquenessOfQualifyingAttribute(
                            header,
                            request,
                            requested,
                            roleName,
                            roleCompleteQualifyingAttribute,
                            qualifyingValue,
                            requested.getValues(State_1_Attributes.VALID_FROM) == null
                            || requested
                            .getValues(State_1_Attributes.VALID_FROM)
                            .isEmpty()
                            ? null
                                : (String)requested.getValues(
                                    State_1_Attributes.VALID_FROM).get(
                                        0),
                                        requested.getValues(State_1_Attributes.VALID_TO) == null
                                        || requested.getValues(State_1_Attributes.VALID_TO).isEmpty()
                                        ? null
                                            : (String)requested.getValues(
                                                State_1_Attributes.VALID_TO).get(
                                                    0));
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    /** 
     * Ensure that the qualifying attributes for the role to create or update
     * have unique values.
     * <p> 
     * If an object becomes a role for the first time it has to be ensured that
     * qualifying attribute is the same in all existing states (the qualifying
     * attribute may be in a subertype of role, thus already present). Then
     * it must be ensured that the qualifying attribute does not have the
     * same value in another object with that role at that time. 
     * <p>
     * This includes a search on the db for each of the qualifying attributes.
     * 
     * @param loadedStates states of the object in db (possibly just one) 
     */
    private void assertCreatingUniqueRoleInstance(
        ServiceHeader header, 
        DataproviderRequest request,  
        DataproviderObject roleType,
        List requiredRoles,
        DataproviderObject[] loadedStates
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("assertUniqueRoleInstance");
        String roleName = roleType.path().getBase();
        DataproviderObject requested = request.object();
        String updateFrom = null;
        String updateTo = null;

        updateFrom =
            request.object().getValues(State_1_Attributes.VALID_FROM) == null
            ? null
                : (String)request.object().getValues(
                    State_1_Attributes.VALID_FROM).get(
                        0);

        updateTo =
            request.object().getValues(State_1_Attributes.VALID_TO) == null
            ? null
                : (String)request.object().getValues(
                    State_1_Attributes.VALID_TO).get(
                        0);

        String[] qualifiers =
            this.roleTypeReference.getQualifiers(
                (String)roleType.values(SystemAttributes.OBJECT_CLASS).get(0));

        for (int i = 0; i < qualifiers.length; i++) {
            String qualifyingAttribute = qualifiers[i];

            // try finding the qualifying attribute in the qualifier of the role.
            String qualifier =
                this.roleTypeReference.getQualifierLeadingToRoleClass(
                    (String)roleType.values(SystemAttributes.OBJECT_CLASS).get(0));

            if (!qualifier.equals(qualifyingAttribute)) {
                // its not the qualifier, try with the attributes

                String attrRole =
                    this.findRoleForAttribute(
                        qualifyingAttribute,
                        loadedStates,
                        requiredRoles);

                if (attrRole == null) {
                    // it is the first state of this class with this role, in 
                    // this case findRoleForAttribute cant find the role.
                    // In that case we can be sure that it belongs to the 
                    // current role
                    if (this
                            .classHasAttribute(
                                request.object().getValues(SystemAttributes.OBJECT_CLASS),
                                qualifyingAttribute)) {

                        // get the qualifying value immediately
                        Object qualifyingValue =
                            request.object().values(qualifyingAttribute).get(0);
                        if (qualifyingValue == null) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "Qualifying attributes value may not be null.",
                                new BasicException.Parameter(
                                    "qualifying attribute",
                                    qualifyingAttribute),
                                    new BasicException.Parameter("role", roleName),
                                    new BasicException.Parameter(
                                        "object",
                                        request.object()));
                        }
                        this.assertUniquenessOfQualifyingAttribute(
                            header,
                            request,
                            requested,
                            roleName,
                            roleName + ROLE_ATTRIBUTE_SEPARATOR + qualifyingAttribute,
                            qualifyingValue,
                            updateFrom,
                            updateTo);
                    }
                    else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            "Qualifying role type attributes name does not match any attribute nor the role class qualifier name.",
                            new BasicException.Parameter(
                                "qualifying attribute",
                                qualifyingAttribute),
                                new BasicException.Parameter("role", roleName),
                                new BasicException.Parameter(
                                    "object.path",
                                    requested.path()));
                    }
                }
                else {
                    // the qualifying attribute already exists in the states.
                    // For each period of a certain value of the attribute, the 
                    // uniqueness of the value has to be checked.
                    Object lastQualifyingValue = null;
                    String lastValidFrom = null;
                    String lastValidTo = null;
                    String roleCompleteQualifyingAttribute = null;

                    roleCompleteQualifyingAttribute =
                        attrRole.length() == 0
                        ? qualifyingAttribute
                            : attrRole + ROLE_ATTRIBUTE_SEPARATOR + qualifyingAttribute;

                    for (int stateNum = 0;
                    stateNum < loadedStates.length;
                    stateNum++) {
                        DataproviderObject loaded = loadedStates[stateNum];
                        boolean continuingState = true;
                        Object qualifyingValue = null;

                        qualifyingValue =
                            loaded.values(roleCompleteQualifyingAttribute).get(0);

                        if (qualifyingValue == null) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "Qualifying attributes value may not be null.",
                                new BasicException.Parameter(
                                    "qualifying attribute",
                                    qualifyingAttribute),
                                    new BasicException.Parameter("role", roleName),
                                    new BasicException.Parameter("state", loaded));
                        }

                        // first value for this qualifying attribute 
                        if (lastQualifyingValue == null) {
                            lastQualifyingValue = qualifyingValue;
                            if (loaded.getValues(State_1_Attributes.VALID_FROM)
                                    != null) {
                                lastValidFrom =
                                    (String)loaded.getValues(
                                        State_1_Attributes.VALID_FROM).get(
                                            0);
                            }
                            if (loaded.getValues(State_1_Attributes.VALID_TO) != null) {
                                lastValidTo =
                                    (String)loaded.getValues(
                                        State_1_Attributes.VALID_TO).get(
                                            0);
                            }
                        }
                        else if (
                                lastQualifyingValue != null
                                && lastQualifyingValue.equals(qualifyingValue)
                                && (lastValidTo != null
                                        && loaded.getValues(State_1_Attributes.VALID_FROM) != null
                                        && lastValidTo.equals(
                                            loaded.getValues(State_1_Attributes.VALID_FROM).get(
                                                0)))) {
                            // they are consecutive and have the same value, bundle!
                            if (loaded.getValues(State_1_Attributes.VALID_TO) != null) {
                                lastValidTo =
                                    (String)loaded.getValues(
                                        State_1_Attributes.VALID_TO).get(
                                            0);
                            }
                            continuingState = true;
                        }
                        else {
                            // different value or non consecutive states.
                            continuingState = false;
                        }

                        if (continuingState == false
                                || stateNum
                                == loadedStates.length - 1 // last state has to be saved!
                        ) {
                            // must only check within update period.
                            if (updateFrom != null) {
                                if (lastValidFrom == null) {
                                    lastValidFrom = updateFrom;
                                }
                                else if (lastValidFrom.compareTo(updateFrom) < 0) {
                                    lastValidFrom = updateFrom;
                                }
                                // else leave lastValidFrom
                            }
                            if (updateTo != null) {
                                if (lastValidTo == null) {
                                    lastValidTo = updateTo;
                                }
                                else if (lastValidTo.compareTo(updateTo) > 0) {
                                    lastValidTo = updateTo;
                                }
                                // else leave lastValidTo
                            }

                            assertUniquenessOfQualifyingAttribute(
                                header,
                                request,
                                requested,
                                roleName,
                                roleCompleteQualifyingAttribute,
                                lastQualifyingValue,
                                lastValidFrom,
                                lastValidTo);
                            // else, id does not need to be checked

                            if (stateNum < loadedStates.length - 1) {
                                // set lastXXX 
                                lastQualifyingValue = null;
                                lastValidFrom =
                                    loaded.getValues(State_1_Attributes.VALID_FROM) == null
                                    ? null
                                        : (String)loaded.getValues(
                                            State_1_Attributes.VALID_FROM).get(
                                                0);
                                lastValidTo =
                                    loaded.getValues(State_1_Attributes.VALID_TO) == null
                                    ? null
                                        : (String)loaded.getValues(
                                            State_1_Attributes.VALID_TO).get(
                                                0);
                            }
                        }
                    }
                }
            }
        }
        StopWatch_1.instance().stopTimer("assertUniqueRoleInstance");
    }

    // --------------------------------------------------------------------------
    /**
     * Assert the uniqueness of the specified attribute for objects which have
     * the role roleName within the timespan [validFrom, validTo]
     * <p> 
     * the attribute which is tested is the roleCompleteQualifyingAttribute. 
     * With the value qualifyingValue. The roleCompleteQualifyingAttribute must
     * not be of the actual role to insert. It may even be of the core.
     * 
     */
    private void assertUniquenessOfQualifyingAttribute(
        ServiceHeader header,
        DataproviderRequest request,
        DataproviderObject requested,
        String roleName,
        String roleCompleteQualifyingAttribute,
        Object qualifyingValue, 
        String validFrom,
        String validTo
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("assertUniqueQualifyingAttribute");
        // first assert that the value is valid, it may not start or end with /
        if (qualifyingValue instanceof String
                && (((String)qualifyingValue).charAt(0) == '/'
                    || ((String)qualifyingValue).endsWith("/"))) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "value for qualifying role type attribute may not start or end with /.",
                new BasicException.Parameter(
                    "qualifying attribute",
                    roleCompleteQualifyingAttribute),
                    new BasicException.Parameter("value", qualifyingValue),
                    new BasicException.Parameter("object.path", requested.path()));
        }

        Path rolelessRequestPath = toRolelessPath(new Path(request.path()));
        DataproviderRequest findRequest =
            new DataproviderRequest(
                new DataproviderObject(rolelessRequestPath.getParent()),
                DataproviderOperations.ITERATION_START,
                new FilterProperty[] {
                    new FilterProperty(
                        Quantors.THERE_EXISTS,
                        roleCompleteQualifyingAttribute,
                        FilterOperators.IS_IN,
                        qualifyingValue
                    )
                },
                0,
                10,
                request.direction(),
                AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                null);
        addRoleFilter(findRequest, roleName, true);
        findRequest.contexts().putAll(request.contexts());
        DataproviderReply reply = null;
        int replyCount = -1; // invalid setting as default
        // some persistence layers throw exception, some return empty collection
        try {
            StopWatch_1.instance().startTimer("assertUniqueQualifyingAttribute-DB");
            reply = super.find(header, findRequest);
            if (reply.getObjects() != null) {
                replyCount = reply.getObjects().length;
            }
            else {
                replyCount = 0;
            }
            StopWatch_1.instance().stopTimer("assertUniqueQualifyingAttribute-DB");

        }
        catch (ServiceException se) {
            if (se.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                replyCount = 0;
            }
            else {
                replyCount = -1;
                throw se;
            }
        }

        // in case of updates, the object itself may already be present
        // in case of states, there may exist several states of the same object
        if (replyCount > 0) {
            for (int i = 0; i < reply.getObjects().length; i++) {
                if (!reply.getObjects()[i].path().startsWith(rolelessRequestPath)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.DUPLICATE,
                        "value of qualifying role type attribute exists already.",
                        new BasicException
                        .Parameter[] {
                            new BasicException.Parameter(
                                "qualifying attribute",
                                roleCompleteQualifyingAttribute),
                                new BasicException.Parameter("role", roleName),
                                new BasicException.Parameter(
                                    "requested.path",
                                    requested.path()),
                                    new BasicException.Parameter(
                                        "existing.path",
                                        reply.getObjects()[i].path()),
                                        // only system attributes! new BasicException.Parameter("existing.value", reply.getObject().getValues(roleCompleteQualifyingAttribute)),
                                        new BasicException.Parameter("number of objects", replyCount)});
                }
            }
        }
        StopWatch_1.instance().stopTimer("assertUniqueQualifyingAttribute");
    }

    // --------------------------------------------------------------------------
    /** 
     * Find the role for a specifying Attribute. A specifying attribute is the
     * one used as a qualifier for a reference from a role type to a role class.
     * <p>
     * The attribute must be part of the role class or it's superclasses. 
     * It may not be an attribute of a subclass of
     * the role class. This is special for specifying attributes, normal 
     * attributes could also occur in derived classes.
     * <p>
     * Sometimes the object id is used as specifying attribute in the model. 
     * This would not be detected null is returned. If the attribute is not 
     * part of a role but of the core itself, an empty string is returned.
     * 
     * @param specifyingAttribute attribute to search for
     * @param roleType roleType to start with
     * @return null - specifyingAttribute is not an attribute
     *          empty String - specifyingAttribute is part of the core
     *          role - specifyingAttribute is part of that role
     */ 
    private String findRoleForSpecifyingAttribute(
        ServiceHeader header,
        DataproviderRequest request,
        String specifyingAttribute,
        DataproviderObject _roleType
    ) throws ServiceException {
        DataproviderObject roleType = _roleType;
        String roleName = null;
        boolean attributeIsContained = true;
        boolean firstRun = true;

        while (roleType != null && roleName == null && attributeIsContained) {
            // get role instance class

            ModelElement_1_0 roleClass =
                this.model.getDereferencedType(
                    roleTypeReference.getRoleClass(
                        (String)roleType.values(SystemAttributes.OBJECT_CLASS).get(0)));

            // is it part of this class?
            List content = roleClass.values("content");
            for (int i = 0; i < content.size() && roleName == null; i++) {
                if (specifyingAttribute
                        .equals(
                            ((Path)content.get(i)).getLastComponent().getLastField())) {
                    roleName = roleType.path().getBase();
                }
            }
            // check if it is contained at all (but only if not already found)
            if (roleName == null) {
                attributeIsContained =
                    ((HashMap)roleClass.values("attribute").get(0)).containsKey(
                        specifyingAttribute);
                if (firstRun && attributeIsContained) {
                    firstRun = false;
                }
                else if (!firstRun && !attributeIsContained) {
                    // the attribute is not contained any more, but it was 
                    // with the last role type. This is the case if the 
                    // attribute is part of A derived class of the role
                    // instance class, which belongs to the same role type. 
                    roleName = roleType.path().getBase();
                }
            }

            // get core role 
            if (roleName == null && attributeIsContained) {
                if (!roleType.values(ROLETYPE_CORE_ROLE_ATTRIBUTE).isEmpty()) {
                    // perhaps its part of the core role
                    String coreRole =
                        (String)roleType.values(ROLETYPE_CORE_ROLE_ATTRIBUTE).get(0);
                    roleType =
                        getRoleType(
                            header,
                            request,
                            coreRole,
                            this.roleTypes,
                            roleType.path().getParent());
                }
                else {
                    roleType = null;
                }
            }
        }

        String result = null;
        if (roleName != null) {
            // roleName has been found 
            result = roleName;
        }
        else if (roleType == null) {
            // its a core attribute, return empty string 
            result = new String();
        }
        else if (!attributeIsContained && firstRun) {
            // specifyingAttribute is not an attribute, probably its an id
            if (roleType != null
                    && roleType.getValues(SystemAttributes.OBJECT_CLASS) != null) {
                String qualifier =
                    roleTypeReference.getQualifierLeadingToRoleClass(
                        (String)roleType.getValues(SystemAttributes.OBJECT_CLASS).get(
                            0));
                if (!specifyingAttribute.equals(qualifier)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "RoleType reference qualifier matches neither any attributes nor the qualifier of the role instance class.",
                        new BasicException.Parameter("roleName", roleName),
                        new BasicException.Parameter(
                            "RoleType path",
                            roleType.path()),
                            new BasicException.Parameter(
                                "specifyingAttribute",
                                specifyingAttribute),
                                new BasicException.Parameter(
                                    "qualifierToRoleClass",
                                    qualifier));
                }
            }
            result = null;
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "unexpected state of program.",
                new BasicException.Parameter("roleName", roleName),
                new BasicException.Parameter("RoleType path", roleType.path()),
                new BasicException.Parameter(
                    "attributeIsContained",
                    attributeIsContained),
                    new BasicException.Parameter("firstRun", firstRun));
        }
        return result;
    }


    // -------------------------------------------------------------------------
    /**
     * Get the path to the role instance through the role type path. 
     * The role type path exactly lead to a role instance, it must inlcude the 
     * value of the qualifying attribute and it may not be any longer
     * <p> 
     * Unless the id of the role instance object serves as qualifying attribute
     * a search in the persistence layer must be executed. If the object can
     * not be found a NOT_FOUND exception is propagated. 
     * <p>
     * If no attribute matches the modelled qualifying attribtue, it is tried 
     * the object id. In this case no search takes place, the path is 
     * calculated. A NOT_FOUND exception will raise only later, on the access 
     * to the object through the calculated path. 
     * 
     * @param header   
     * @param pathToRoleInstance
     * 
     * @return path to role instance object (not null!)
     */
    private  Path getRoleInstancePathThroughRoleType(
        ServiceHeader header, 
        DataproviderRequest request,
        Path  pathToRoleInstance
    ) throws ServiceException {
        Path roleInstancePath = null;

        // the last element must be the identifying element for the role instance
        String id =
            pathToRoleInstance.remove(pathToRoleInstance.size() - 1);

        // the one before must be the association leading to the class
        String assoc =
            pathToRoleInstance.remove(pathToRoleInstance.size() - 1);

        // the next part must be the role name 
        String roleName =
            pathToRoleInstance.remove(pathToRoleInstance.size() - 1);

        // need roleType class to get the identifying attribute

        // exception is thrown if not found!
        DataproviderObject roleType =
            getRoleType(
                header,
                request,
                roleName,
                this.roleTypes,
                pathToRoleInstance);

        // if it's found the OBJECT_CLASS is set!
        String roleTypeClass =
            (String)roleType.values(SystemAttributes.OBJECT_CLASS).get(0);

        // now get the corresponding path
        Path correspondingPath = this.getRoleTypesPath(pathToRoleInstance);
        String roleAttribute =
            roleTypeReference.getQualifierForReferenceEnd(roleTypeClass, assoc);

        String rolePrefix =
            findRoleForSpecifyingAttribute(
                header,
                request,
                roleAttribute,
                roleType);

        if (rolePrefix == null) {
            // its most probably the object id itself
            // just add id to path
            roleInstancePath = correspondingPath.add(id);
        }
        else {
            DataproviderReply reply = null;

            DataproviderRequest newRequest =
                new DataproviderRequest(
                    new DataproviderObject(correspondingPath),
                    DataproviderOperations.ITERATION_START,
                    request.attributeSelector(),
                    request.attributeSpecifier());
            newRequest.contexts().putAll(request.contexts());

            // add filter for role 
            addRoleFilter(newRequest, roleName, true);

            if (rolePrefix.length() > 0) {
                roleAttribute =
                    rolePrefix + ROLE_ATTRIBUTE_SEPARATOR + roleAttribute;
            }
            // else its a core attribute

            // add Filter for the specifying attribute
            newRequest.addAttributeFilterProperty(
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    roleAttribute,
                    FilterOperators.IS_IN,
                    id 
                )
            );

            // if a not found exception is thrown it's ok
            reply = super.find(header, newRequest);

            if (reply == null
                    || reply.getObjects() == null
                    || reply.getObjects().length == 0) {
                // throw not found exception to get a unified behaviour
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "no role instance object found on accessing through RoleType.",
                    new BasicException.Parameter("role", roleName),
                    new BasicException.Parameter(
                        "RoleType path",
                        pathToRoleInstance),
                        new BasicException.Parameter(
                            "Role instance path",
                            correspondingPath),
                            new BasicException.Parameter(
                                "unique attribute",
                                roleTypeReference.getQualifierForReferenceEnd(
                                    roleTypeClass,
                                    assoc)),
                                    new BasicException.Parameter("unique attributes value", id));
            }

            // assert that there is at most one object found
            if (reply.getObjects().length > 1) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Several objects with the same unique attribute found on accessing through RoleType.",
                    new BasicException.Parameter("role", roleName),
                    new BasicException.Parameter(
                        "RoleType path",
                        pathToRoleInstance),
                        new BasicException.Parameter(
                            "Role instance path",
                            correspondingPath),
                            new BasicException.Parameter(
                                "unique attribute",
                                roleTypeReference.getQualifierForReferenceEnd(
                                    roleTypeClass,
                                    assoc)),
                                    new BasicException.Parameter("value searched", id));
            }

            roleInstancePath = reply.getObject().path();
        }
        return roleInstancePath;
    }


    // --------------------------------------------------------------------------
    /**
     * convert a search for a role instance through the role type path to 
     * direct search for the role instance. The path of the request is 
     * adapted to direct path to the role instance and the role is added as
     * a filter request to the AttributeFilters of the request.
     * <p>
     * There may also be legal paths leading beyond the RoleType (if the 
     * RoleType serves as container)!
     * 
     * @param header original header needed for internal calls
     * @param request original request which is used to propagate request.
     */
    private void convertRoleTypeToRoleInstanceSearch(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        // path which goes beyond roleType: 
        // ch::omex::testRole1/provider/Test/segment/Standard/abstractRootRoleType/idRTArara1/roleClassRoleARoleA/rcra object[0] role idRTAra1/roleClassFree
        // referencePath: provider/segment/abstractRootRoleType/roleClassRoleARoleA/roleClassFree
        Path referencePath = getPathWithRemovedObjectIds(request.path());
        Path roleTypePath = null;
        // System.out.println(" convertRoleTypeToRoleInstanceSearch: treating path: " + request.path());
        // path which goes beyond roleType: 
        // ch::omex::testRole1/provider/Test/segment/Standard/abstractRootRoleType/idRTArara1/roleClassRoleARoleA/rcra object[0] role idRTAra1/roleClassFree
        for (int i = 0;
        i < this.allRoleTypeReferencePaths.size() && roleTypePath == null;
        i++) {
            if (referencePath
                    .startsWith((Path)allRoleTypeReferencePaths.get(i))) {
                roleTypePath = (Path)allRoleTypeReferencePaths.get(i);
            }
        }

        if (roleTypePath != null
                && request.path().size()
                > roleTypePath.size() * 2
                + 1 // else its a find path leading to roleType itself
        ) {
            ModelElement_1_0 roleTypeSuperClass =
                (ModelElement_1_0)this.referencePathClassMap.get(roleTypePath);

            String referenceName =
                request.path().get(roleTypePath.size() * 2 + 1);
            ModelElement_1_0 reference = null;

            for (Iterator subIter =
                roleTypeSuperClass.getValues("subtype").iterator();
            subIter.hasNext() && reference == null;
            ) {
                ModelElement_1_0 roleTypeClass =
                    model.getDereferencedType(subIter.next());

                // check for the reference in the roleTypeClass:
                for (Iterator features =
                    roleTypeClass.getValues("feature").iterator();
                features.hasNext() && reference == null;
                ) {
                    String featureName = ((Path)features.next()).getBase();
                    if (featureName
                            .endsWith(PathComponent.FIELD_DELIMITER + referenceName)) {
                        reference = model.getDereferencedType(featureName);
                    }
                }
            }
            // must find out if it is a reference leading to a Role instance.
            if (reference != null) {
                boolean isRole = false;
                ModelElement_1_0 referencedType =
                    model.getDereferencedType(
                        ((Path)reference.getValues("type").get(0)).getBase());
                for (Iterator s =
                    referencedType.getValues("allSupertype").iterator();
                s.hasNext() && !isRole;
                ) {
                    isRole =
                        "org:openmdx:compatibility:role1:Role".equals(((Path)s.next()).getBase());
                }
                if (isRole) {
                    // note: we try to avoid the contained objects. Introducing
                    // shared aggregation requires first defining the semantics 
                    // of a shared aggregation together with roles.

                    // and to be sure, it also should not be a containment.
                    ModelElement_1_0 referencedEnd =
                        model.getDereferencedType(
                            reference.getValues("referencedEnd").get(0));

                    if (AggregationKind
                            .SHARED
                            .equals(referencedEnd.getValues("aggregation").get(0))) {
                        // it's ok, replace the path
                        String roleName = request.path().get(roleTypePath.size() * 2);

                        if (request.path().size() == roleTypePath.size() * 2 + 2) {
                            // it is a search for a role instance object
                            Path correspondingPath =
                                (Path)roleTypePathPatternMap.get(roleTypePath);
                            int pos = (correspondingPath.size() * 2) - 1;

                            request.path().remove(pos); // from common super to roletype
                            request.path().remove(pos); // RoleTypeId
                            request.path().remove(pos);
                            // from RoleType to Role instance class
                            request.path().add(pos, correspondingPath.getBase());

                            // its a search for the objects of this role,
                            // add the in_role filter to the request
                            request.addAttributeFilterProperty(
                                new FilterProperty(
                                    Quantors.THERE_EXISTS,
                                    "object_inRole",
                                    FilterOperators.IS_IN,
                                    roleName ));
                        }
                        // it may be a search not for role instances but for objects which
                        // are associated to a role. 
                        // In this case the referencePath must lead beyond 
                        else {
                            int roleInstancePathEndPos = (roleTypePath.size() * 2) + 3;
                            // extract the path to the role instance
                            Path roleInstancePath =
                                request.path().getPrefix(roleInstancePathEndPos);

                            Path instancePath =
                                getRoleInstancePathThroughRoleType(
                                    header,
                                    request,
                                    roleInstancePath);
                            int pos = instancePath.size();

                            List roles =
                                getRequiredRoles(
                                    header,
                                    request,
                                    roleName,
                                    this.roleTypes,
                                    instancePath,
                                    new HashMap());
                            // roles are sorted from outermost to innermost invert it
                            // by inserting all to the same position
                            for (Iterator i = roles.iterator(); i.hasNext();) {
                                instancePath.add(pos, (String)i.next());
                                instancePath.add(pos, ROLE_PATH_ENTRY);
                            }

                            // now add other parts from path                    
                            instancePath.addAll(
                                request.path().getSuffix(roleInstancePathEndPos));
                            request.path().setTo(instancePath);
                        }

                        SysLog.trace(
                            " convertRoleTypeToRoleInstancePath: new path : "
                            + request.path());
                    }
                }
            }
        }
    }

    //---------------------------------------------------------------------------
    /** 
     * If it is a delete request for a RoleType, throw an exception as it is
     * no longer allowed to delete RoleTypes. 
     * <P>
     * The problem arises in the case of stated role objects. If deletion of 
     * roletypes is allowed and the role types themselfs are not stated, it is 
     * possible to get a role object (in history) which has a non existing 
     * role type.
     * 
     * @param  header   
     * @param  request  
     */ 
    private void assertRoleTypeRemove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        // is it a RoleType which is to be removed
        if (!allRoleTypeReferencePaths
                .contains(getPathWithRemovedObjectIds(request.path()))) {
            return; // not a role type, just return
        }

        // avoid deletion of role types: 
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "Removal of RoleType is not supported. It may still be in use in role objects.",
            new BasicException.Parameter("request.path", request.path()));
    }

    //---------------------------------------------------------------------------
    /** 
     * check that the required roles are present in the objects.
     * 
     * if role is not null, it is also checked, that role is not already present
     * in the object. Otherwise role is not checked at all.
     * 
     * @param requiredRoles roles which must be present in object
     * @param role          if role is specified, it may not exist in object
     * @param objects       objects to check for role
     */
    private void assertRequiredRoles(
        List requiredRoles,
        String role,
        DataproviderObject_1_0[] objects
    ) throws ServiceException {
        String roleToCheck = null;
        String roleClass = null;
        DataproviderObject_1_0 object = null;

        for (int i = 0; i < objects.length; i++) {
            object = objects[i];
            // check for required roles:
            for (Iterator reqIter = requiredRoles.iterator();
            reqIter.hasNext();
            ) {
                roleToCheck = (String)reqIter.next();
                // use the one attribute which is present for all roles
                roleClass =
                    roleToCheck
                    + ROLE_ATTRIBUTE_SEPARATOR
                    + SystemAttributes.OBJECT_CLASS;

                if (roleToCheck.equals(role)) {
                    // make sure this role is not already present
                    if (object.getValues(roleClass) != null
                            && !object.getValues(roleClass).isEmpty()) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            "Trying to create a role which is already present.",
                            new BasicException.Parameter("role to create", role),
                            new BasicException.Parameter("object", object));
                    }
                    // else ok, role is not present
                }
                else {
                    // these roles must be present
                    if (object.getValues(roleClass) == null
                            || object.getValues(roleClass).isEmpty()) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            "Missing required role for role.",
                            new BasicException.Parameter("role", role),
                            new BasicException.Parameter("role missing", roleToCheck),
                            new BasicException.Parameter("object", object));
                    }
                    // else ok, role is present
                }
            }
        }
    }

    //---------------------------------------------------------------------------
    /** 
     * extract the role from the path if present. 
     * <p>
     * if there are several role entries towards the end of the path, the
     * last is returned. Path entries somewhere in between are not treated.
     * <p>
     * In case of statefull roles there may be requests with "validState" or 
     * "historyState" added to the path. The role must be specified before 
     * those.
     * 
     * @param path   path with objectId 
     * @return roleId or null
     */ 
    private String getRoleFromPath(
        Path path
    ) {
        int pos = 0;
        String roleId = null;

        int size = path.size();
        for (pos = size - 1;
        !path.get(pos).equals(this.ROLE_PATH_ENTRY) && pos > 0;
        pos--) {
            //
        }

        if (pos > 0
                && // no role found
                pos + 1
                < size // "role" as last component, no roleId
        ) {
            // only if roleId is last entry or followed by validState or 
            // historyState
            if (pos + 2 == size
                    || (pos + 2 < size
                            && (path.get(pos + 2).equals(State_1_Attributes.REF_HISTORY)
                                    || path.get(pos + 2).equals(State_1_Attributes.REF_VALID)
                                    || path.get(pos + 2).equals(State_1_Attributes.REF_STATE)))) {
                roleId = path.get(pos + 1);
            }
        }
        return roleId;
    }    

    // --------------------------------------------------------------------------
    /**
     * remove the role specifiers from the path supplied. The same path is also
     * returned for easier usage.
     * <p>
     * if there are several role entries towards the end of the path, all
     * of them get removed to get a "normal" path. If there are further
     * role entries in the path, they are not treated. 
     * <p>
     * In case of stateful roles there may be requests with "validState" or 
     * "historyState" added to the path. These components are maintained and
     * role entries immediately before them are removed.
     * 
     * @param path  path containing id
     * 
     * @return  the path supplied
     */
    private Path toRolelessPath(
        Path path
    ) {
        boolean goOn = false;
        int pos = path.size();

        do {
            if (path.get(pos - 2).equals(ROLE_PATH_ENTRY)) {
                path.remove(pos - 1);
                path.remove(pos - 2);
                goOn = true;
                pos = pos - 2;
            }
            else if (
                    path.get(pos - 2).equals(State_1_Attributes.REF_HISTORY)
                    || path.get(pos - 2).equals(State_1_Attributes.REF_VALID)
                    || path.get(pos - 2).equals(State_1_Attributes.REF_STATE)) {
                goOn = true;
                pos = pos - 2;
            }
            else if (
                    path.get(pos - 1).equals(State_1_Attributes.REF_HISTORY)
                    || path.get(pos - 1).equals(State_1_Attributes.REF_VALID)
                    || path.get(pos - 1).equals(State_1_Attributes.REF_STATE)) {
                goOn = true;
                pos = pos - 1;
            }
            else {
                goOn = false;
            }
        }
        while (goOn && pos > 0);
        return path;
    }        


    // --------------------------------------------------------------------------
    /**
     * convert the path and remove the state specifiers from it. State 
     * specifiers are "validState" or "historyState".
     * 
     * @param path  path ro remove state specifiers
     * 
     * @return  the path supplied
     */
    private Path toStatelessPath(
        Path path
    ) {
        int pos = path.size();
        if (path.get(pos - 2).equals(State_1_Attributes.REF_HISTORY)
                || path.get(pos - 2).equals(State_1_Attributes.REF_VALID)
                || path.get(pos - 2).equals(State_1_Attributes.REF_STATE)) {
            path.remove(pos - 1);
            path.remove(pos - 2);
        }
        else if (
                path.get(pos - 1).equals(State_1_Attributes.REF_HISTORY)
                || path.get(pos - 1).equals(State_1_Attributes.REF_VALID)
                || path.get(pos - 1).equals(State_1_Attributes.REF_STATE)) {
            path.remove(pos - 1);
        }
        return path;
    }        


    //---------------------------------------------------------------------------
    /**
     * Complete the path supplied with the roleComponentes. The roleComponents
     * are a path snippet for the roles and the required roles of a role. If 
     * there is a state specifier present in the path, the snippet is 
     * inserted before the snippet.
     * 
     * @param rolePath  role path snippet to insert
     * @param path      path to insert to
     */
    private void addRolePathToPath(
        String[] roleComponents,
        Path path
    ) {
        int pos = path.size();
        if (path.get(pos - 2).equals(State_1_Attributes.REF_HISTORY)
                || path.get(pos - 2).equals(State_1_Attributes.REF_VALID)
                || path.get(pos - 2).equals(State_1_Attributes.REF_STATE)) {
            path.addAll(pos - 2, roleComponents);
        }
        else {
            path.addAll(roleComponents);
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Determine if one of the classes or it's superclasses contains the 
     * attribute.
     * 
     * @param classes list of classNames 
     * @param the name of the attribute the classes should contain
     * 
     * @return true if the attribute is contained
     */
    private boolean classHasAttribute(
        List classes, 
        String attribute
    ) throws ServiceException {
        Iterator classIter = classes.iterator();
        while (classIter.hasNext()) {
            String className = (String)classIter.next();
            ModelElement_1_0 roleClass = this.model.getDereferencedType(className);
            if (roleClass != null) {
                Map modelAttributes = (Map)roleClass.values("attribute").get(0);
                if(modelAttributes.containsKey(attribute)){
                    return true;
                }
            }
        }
        return false;               
    }    

    //---------------------------------------------------------------------------
    /**
     * Decide if this attribute is part of a role or of the core.
     * 
     * @param  attribute   name of the attribute
     * 
     * @return true if it is part of a role
     */
    private boolean isRoleAttribute(
        String attributeName,
        Path   requestPath
    ) throws ServiceException {
        boolean isRoleAttribute = false;
        String attributeNameWithSeparator = ":" + attributeName;
        Path referencePath =
            this.getPathWithRemovedObjectIds(
                this.toStatelessPath(this.toRolelessPath(new Path(requestPath))));
        SysLog.trace("request path", requestPath);
        SysLog.trace("reference path", referencePath);
        ModelElement_1_0 objClassAtPath =
            (ModelElement_1_0)this.referencePathClassMap.get(referencePath);
        SysLog.trace("referenced class", objClassAtPath);
        if (objClassAtPath == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "referenced class can not be determined",
                new BasicException.Parameter("reference", referencePath));
        }

        // get the class at this path
        Path subClassPath = null;
        ModelElement_1_0 subClass = null;
        boolean found = false;

        // the attribute may be part of this class or of one of its derived
        // classes 
        for (Iterator i = objClassAtPath.values("allSubtype").iterator();
        i.hasNext() && !found;
        ) {
            subClassPath = (Path)i.next();
            subClass = this.model.getDereferencedType(subClassPath);
            for (Iterator contentIter = subClass.values("content").iterator();
            contentIter.hasNext() && !found;
            ) {
                String qualifiedName = ((Path)contentIter.next()).getBase();
                if (qualifiedName.endsWith(attributeNameWithSeparator)) {
                    found = true;
                }
            }
        }

        if (!found) {
            // it was not found in the class or its subclasses, most 
            // probably its some kind of system attribute.
            isRoleAttribute = false;
        }
        else if (classIsRole(subClass)) {
            isRoleAttribute = true;
        }
        else {
            isRoleAttribute = false;
        }
        return isRoleAttribute;
    }

    //---------------------------------------------------------------------------
    /** 
     * Determine if the class or one of its superclasses is a role
     * 
     * @param roleClass class to check
     * 
     * @return true if it is a role false otherwise
     */
    private boolean classIsRole(
        ModelElement_1_0 _roleClass
    ) throws ServiceException {
        ModelElement_1_0 roleClass = _roleClass;
        boolean isRole;
        Path path;
        // is this class or one of the superclasses a role
        isRole = roleClass.values("stereotype").contains(ROLE_STEREOTYPE);
        for (Iterator superIter =
            roleClass.values("allSupertype").populationIterator();
        superIter.hasNext() && !isRole;
        ) {
            path = (Path)superIter.next();
            roleClass = this.model.getDereferencedType(path);
            isRole =
                roleClass.getValues("stereotype") != null
                && roleClass.getValues("stereotype").contains(ROLE_STEREOTYPE);
        }
        return isRole;
    }

    //---------------------------------------------------------------------------
    /** 
     * Determine if the class belongs to the same role as the specified
     * superclass.  
     * 
     * @param roleClass class to check
     * @param superclass  the same or it's superclass
     * 
     * @return true if it belongs to the same role false otherwise
     */
    private boolean classBelongsToRole( 
        ModelElement_1_0 _roleClass,
        ModelElement_1_0 superClass
    ) throws ServiceException {
        ModelElement_1_0 roleClass = _roleClass;
        Object superClassName = superClass.getValues("qualifiedName").get(0);
        Path path = null;

        // there may be multiple supertypes for a class, each must be checked.
        ArrayList supers = new ArrayList();

        // search from roleClass upwards until the superclass is met or 
        // one of the class holds the stereotype role.
        while (roleClass != null
                && !roleClass.getValues("qualifiedName").get(0).equals(superClassName)
                && !(roleClass.getValues("stereotype") != null
                        && roleClass.getValues("stereotype").contains(ROLE_STEREOTYPE))) {
            if (roleClass.getValues("supertype") != null) {
                for (Iterator i = roleClass.getValues("supertype").iterator();
                i.hasNext();
                ) {
                    path = (Path)i.next();

                    supers.add(this.model.getDereferencedType(path));
                }
            }

            if (supers.size() > 0
                    && !roleClass.getValues("qualifiedName").get(0).equals(
                        superClassName)) {
                roleClass = (ModelElement_1_0)supers.remove(0);
            }
            else {
                roleClass = null;
            }
        }
        return (
                roleClass != null
                && roleClass.getValues("qualifiedName").get(0).equals(superClassName));
    }

    //---------------------------------------------------------------------------
    /** 
     * Find the role this attribute belongs to concerning that it must
     * be contained in one of the roles specified. It may be in any of 
     * the states supplied.
     * 
     * This works for existing attributes as well as for new optional 
     * attributes which are stored for the first time.
     * <p>
     * In case of extensions the class holding the attribute may
     * not occur in the object_class attribute of any role, even though
     * one or more of the roles object_class may be derived from a 
     * parent class containing that attribute.
     * <p>
     * The assumption used here is that the attribute belongs to the 
     * innermost role of which the class or its super classes can
     * hold the attribute.
     * <p>
     * The states are not checked for consistency, eg. that the attribute 
     * is at the same role, if present. 
     * 
     * @param  attr  the attribute to assign
     * @param  loadedStates states of the object as loaded from storage (must
     * contain core and object_class attributes)
     * @param roles roles in which to search for the attribute
     * 
     * @return role of the attribute which is an empty string if the attribute
     * belongs to the innermost core or null if the attribute was not found
     * at all. 
     * 
     */
    private String findRoleForAttribute(
        String attr,
        DataproviderObject[] loadedStates,  
        List roles
    ) throws ServiceException {
        String attrRole = null;
        List classNames = null;

        for (int i = 0; i < loadedStates.length && attrRole == null; i++) {
            //
            // first check the classes without roles
            // 
            if (classHasAttribute(loadedStates[i]
                                               .getValues(SystemAttributes.OBJECT_CLASS),
                                               attr)) {
                attrRole = "";
            }
            else {
                //
                // if not found the roles must be searched 
                //
                // rolesIter gets the outermost role first, iterate in reverse:
                ListIterator rolesIter = roles.listIterator(roles.size());
                String searchRole = null;
                while (rolesIter.hasPrevious() && attrRole == null) {
                    searchRole = (String)rolesIter.previous();
                    classNames =
                        loadedStates[i].getValues(
                            searchRole
                            + ROLE_ATTRIBUTE_SEPARATOR
                            + SystemAttributes.OBJECT_CLASS);

                    if (classNames != null) {
                        // if new role is contained required roles
                        if (classHasAttribute(classNames, attr)) {
                            attrRole = searchRole;
                        }
                    }
                }
            }
        }
        return attrRole;
    }

    //---------------------------------------------------------------------------
    /**
     * Create an object containing the attributes supplied in request with the
     * correct role specifiers added. 
     * <p>
     * check that the update happens on the same role, where the same role is
     * specified by the same role creation date. An update may prolong 
     * a role if all the required attributes (1..x) or qualifying attribute 
     * exist and it stays within the objects validity. 
     * <p>
     * 
     * @param header   original header from call
     * @param request  original object from call
     * @param role     role the request object is in
     * @param roleTypeCache cache of retrieved roleTypes
     * @param updateOp whether it is an update operation (true) or a modify (false)
     */
    private DataproviderObject createExpandedObject(
        ServiceHeader header, 
        DataproviderRequest request, 
        String role,
        Map roleTypeCache,
        boolean updateOp
    ) throws ServiceException {                
        DataproviderObject delta = null;
        DataproviderObject existingRoleInstanceState = null;
        Path noRolePath = toRolelessPath(new Path(request.path()));
        String attributeName = null;

        delta = new DataproviderObject(noRolePath);

        DataproviderObject[] states = findCoreObjectStates(header, request);

        String requestFrom =
            request.object().getValues(State_1_Attributes.VALID_FROM) == null
            ? null
                : (String)request.object().getValues(
                    State_1_Attributes.VALID_FROM).get(
                        0);

        String requestTo =
            request.object().getValues(State_1_Attributes.VALID_TO) == null
            ? null
                : (String)request.object().getValues(
                    State_1_Attributes.VALID_TO).get(
                        0);

        // 
        // check that [requestFrom, requestTo] is within the objects validity frame 
        //
        if ((requestFrom == null
                && !states[0].values(State_1_Attributes.VALID_FROM).isEmpty())
                || (!states[0].values(State_1_Attributes.VALID_FROM).isEmpty()
                        && requestFrom.compareTo(
                            (String) states[0].getValues(State_1_Attributes.VALID_FROM).get(0))
                            < 0)
                            || (requestTo == null
                                    && !states[states.length
                                               - 1].values(State_1_Attributes.VALID_TO).isEmpty())
                                               || (!states[states.length
                                                           - 1].values(State_1_Attributes.VALID_TO).isEmpty()
                                                           && requestFrom.compareTo(
                                                               (String) states[states.length
                                                                               - 1].getValues(State_1_Attributes.VALID_TO).get(0))
                                                                               > 0)) {

            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Role update spreads beyond object validity.",
                new BasicException.Parameter("path", request.path()),
                new BasicException.Parameter("role", role),
                new BasicException.Parameter(
                    "object.validFrom",
                    states[0].values(State_1_Attributes.VALID_FROM)),
                    new BasicException.Parameter(
                        "object.validTo",
                        states[states.length - 1].values(State_1_Attributes.VALID_TO)),
                        new BasicException.Parameter("request.validFrom", requestFrom),
                        new BasicException.Parameter("request.validTo", requestTo));

        }

        // 
        // don't allow updates through several consecutive role states.
        // (testing for existence of required attributes would become 
        //  too complicated.)
        // an overhang beyond the existing role is allowed
        // 

        int overhangingStatesAtStartIndex = -1;
        // index up to which the states are overhanging
        int overhangingStatesAtEndIndex = -1;
        // index from which the states are overhanging

        String roleIndicationAttr =
            role + ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_CLASS;
        for (int i = 0; i < states.length; i++) {

            if (states[i].getValues(roleIndicationAttr) != null
                    && !states[i].getValues(roleIndicationAttr).isEmpty()) {
                // any of the states containing the role already would do
                existingRoleInstanceState = states[i];
            }
            else {
                if (existingRoleInstanceState == null) {
                    // did not yet find a state supporting the role
                    overhangingStatesAtStartIndex = i;
                }
                else if (overhangingStatesAtEndIndex == -1) {
                    // set only on first occurence
                    // after finding a state supporting the role. There may not
                    // be any holes within the role "instance". So these two 
                    // indices are enough.
                    overhangingStatesAtEndIndex = i;
                }
                else {
                    // this is an error as the role ended and starts now new
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Role update hits two existing role instances.",
                        new BasicException.Parameter("path", request.path()),
                        new BasicException.Parameter("role", role),
                        new BasicException.Parameter(
                            "request.validFrom",
                            requestFrom),
                            new BasicException.Parameter("request.validTo", requestTo),
                            new BasicException.Parameter(
                                "second role instance validFrom",
                                states[i].values(State_1_Attributes.VALID_FROM).get(0)));
                }
            }
        }

        //  
        // check that the role is present in one of the states at least
        //
        // if the creation time was not set, the role is empty
        if (existingRoleInstanceState == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Object does not have role.",
                new BasicException.Parameter("path", request.path()),
                new BasicException.Parameter("role", role));
        }

        //
        // if the update is overhanging, check that the extended part is valid 
        //
        DataproviderObject roleType =
            assertCompleteRoleType(
                header,
                request,
                role,
                roleTypeCache,
                request.object());

        if (overhangingStatesAtStartIndex > -1
                || overhangingStatesAtEndIndex > -1) {
            List requiredRoles =
                getRequiredRoles(
                    header,
                    request,
                    role,
                    roleTypeCache,
                    request.path(),
                    new HashMap());

            assertRoleAttributeMultiplicity(request.object(), roleType);

            // first at the beginning
            if (overhangingStatesAtStartIndex > -1) {
                DataproviderObject[] overhangingStates;
                overhangingStates =
                    new DataproviderObject[overhangingStatesAtStartIndex + 1];
                for (int i = 0; i < overhangingStatesAtStartIndex; i++) {
                    overhangingStates[i] = states[i];
                }

                assertRequiredRoles(requiredRoles, role, overhangingStates);

                assertCoreRoleExtensionClass(
                    header,
                    request,
                    role,
                    roleTypeCache,
                    requiredRoles,
                    overhangingStates,
                    request.object());

                assertCreatingUniqueRoleInstance(
                    header,
                    request,
                    roleType,
                    requiredRoles,
                    overhangingStates);
            }

            // then at the end
            if (overhangingStatesAtEndIndex > -1) {
                DataproviderObject[] overhangingStates;
                overhangingStates =
                    new DataproviderObject[states.length
                                           - overhangingStatesAtEndIndex];
                for (int i = 0; i < overhangingStates.length; i++) {
                    overhangingStates[i] = states[i + overhangingStatesAtEndIndex];
                }
                assertRequiredRoles(requiredRoles, role, overhangingStates);

                assertCoreRoleExtensionClass(
                    header,
                    request,
                    role,
                    roleTypeCache,
                    requiredRoles,
                    overhangingStates,
                    request.object());

                assertCreatingUniqueRoleInstance(
                    header,
                    request,
                    roleType,
                    requiredRoles,
                    overhangingStates);
            }
        }

        // 
        // check that the qualifying attributes are unique.
        //
        List roles =
            getRequiredRoles(
                header,
                request,
                role,
                roleTypeCache,
                noRolePath,
                new HashMap());
        // must assert all qualifying attributes because of overhanging updates
        assertMaintainingUniqueRoleInstance(
            header,
            request,
            roleType,
            roles,
            states,
            updateOp);

        // 
        // create update request
        //
        for (Iterator i = request.object().attributeNames().iterator();
        i.hasNext();
        ) {
            attributeName = (String)i.next();

            // object_class and creation can not change!
            if (!attributeName.equals(SystemAttributes.CREATED_AT)
                    && !attributeName.equals(SystemAttributes.CREATED_BY)
                    && !attributeName.equals(SystemAttributes.OBJECT_CLASS)
                    && attributeName.indexOf(':')<0
            ){
                String attrRole =
                    this.findRoleForAttribute(attributeName, states, roles);
                if (attrRole == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "attribute does not exist in role",
                        new BasicException.Parameter("attribute", attributeName),
                        new BasicException.Parameter("role", roles));
                }
                String attrWithRole = null;
                if (attrRole.length() > 0) {
                    attrWithRole =
                        attrRole + ROLE_ATTRIBUTE_SEPARATOR + attributeName;
                }
                else {
                    attrWithRole = attributeName;
                }
                delta.values(attrWithRole).addAll(
                    request.object().getValues(attributeName));
            }
        }

        // now add some required attributes

        // at least the core must be present for each state, otherwise there would 
        // have been an exception.
        delta.clearValues(SystemAttributes.OBJECT_CLASS).addAll(
            states[0].getValues(SystemAttributes.OBJECT_CLASS));

        for (Iterator roleIter = roles.iterator(); roleIter.hasNext();) {
            String roleName = (String)roleIter.next();
            delta
            .clearValues(
                roleName
                + ROLE_ATTRIBUTE_SEPARATOR
                + SystemAttributes.OBJECT_CLASS)
                .addAll(
                    existingRoleInstanceState.getValues(
                        roleName
                        + ROLE_ATTRIBUTE_SEPARATOR
                        + SystemAttributes.OBJECT_CLASS));
        }

        delta.clearValues(SystemAttributes.MODIFIED_AT).add(
            0,
            timeNow());

        /*CR0000381
      String now = timeNow();
      delta.clearValues(role + ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_CREATED_AT).
          add(now);
      delta.clearValues(role + ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_MODIFIED_AT).
          add(now);
         */
        return delta;
    }


    //---------------------------------------------------------------------------
    /**
     * Prepare a DataproviderObject for removing all the attributes related 
     * with the roles specified in the roles list.
     * 
     * @param header for access to storage
     * @param dpo  object from which attributes have to be removed
     * @param deletingRole    role which's attributes have to be removed
     * @param roleTypeCache   cache of retrieved roleTypes
     */
    private DataproviderObject removeRoleDependentAttributes(
        ServiceHeader header,
        DataproviderRequest request,
        DataproviderObject dpo,
        String deletingRole,
        Map roleTypeCache
    ) throws ServiceException  {
        DataproviderObject cutter = null;
        List roles = null;

        cutter = new DataproviderObject(dpo.path());

        for (Iterator iter = dpo.attributeNames().iterator();
        iter.hasNext();
        ) {
            String attrWithRole = (String)iter.next();
            String role = null;
            int pos = attrWithRole.indexOf(ROLE_ATTRIBUTE_SEPARATOR);
            if (pos > 0) {
                role = attrWithRole.substring(0, pos);
                roles =
                    this.getRequiredRoles(
                        header,
                        request,
                        role,
                        roleTypeCache,
                        dpo.path(),
                        new HashMap());
                if (roles.contains(deletingRole)) {
                    cutter.values(attrWithRole).clear();
                }
            }
        }
        if (cutter.getValues(SystemAttributes.OBJECT_CLASS) == null) {
            // need to maintain the object class of the core, otherwise
            // state can't judge if it is statefull
            cutter.values(SystemAttributes.OBJECT_CLASS).addAll(
                dpo.values(SystemAttributes.OBJECT_CLASS));
        }

        return cutter;
    }

    //---------------------------------------------------------------------------
    /** 
     * Create an update object containing just the attributes of the new role.
     * The attributes names are expanded with the role.
     * <p>
     * The requiredRoles indicate which roles the new role is dependent on.
     * It must be a sorted list from the outermost (most specific) to the 
     * inermost role. The assertion for the loadedObjects to contain all of 
     * these roles must already have been done.
     * 
     * This includes:
     * <ul>
     * <li> append the new attributes with role specifier </li>
     * <li> ensure that the new attributes are part of one of the classes of the role </li>
     * </ul>
     * 
     * @param loadedStates  states of the object to add role 
     * @param reqObject  object which contains the new values
     * @param role       role specifier to use
     * @param requiredRoles  roles required for creation of this role, including 
     *                        the role to create (starting with that one). May 
     *                        not be null.
     * 
     * @return updateObject with expanded attribute names
     * 
     */
    private DataproviderObject createUpdateObject(
        DataproviderObject[] loadedStates,
        DataproviderObject reqObject,
        String role,
        List   requiredRoles
    ) throws ServiceException {
        DataproviderObject result =
            new DataproviderObject(
                toStatelessPath(new Path(loadedStates[0].path())));
        // this must be the class which belongs to the role!
        // it can also be one of the extensions of the class for the role.
        List reqMostSpecificClassName =
            reqObject.getValues(SystemAttributes.OBJECT_CLASS);

        // this is the class of the required role already present in the 
        // object
        List loadedMostSpecificClassName = null;
        // must get it from loadedStates, only there the information is present

        // it may be that there are expanded classes in roles. Must find 
        // most expanded class of all the loaded ones. Otherwise attributes 
        // of this class which are present in update request would be 
        // implied to the role

        for (int i = 0; i < loadedStates.length; i++) {
            List loadedClassName = null;
            if (requiredRoles != null
                    && requiredRoles.size() > 1
                    && requiredRoles.get(1) != null) {
                loadedClassName =
                    loadedStates[i].getValues(
                        requiredRoles.get(1)
                        + ROLE_ATTRIBUTE_SEPARATOR
                        + SystemAttributes.OBJECT_CLASS);
            }
            else {
                loadedClassName =
                    loadedStates[i].getValues(SystemAttributes.OBJECT_CLASS);
            }

            if (loadedMostSpecificClassName == null) {
                loadedMostSpecificClassName = loadedClassName;
            }
            else if (
                    !loadedClassName.get(0).equals(
                        loadedMostSpecificClassName.get(0))) {
                ModelElement_1_0 loadedClass =
                    this.model.getDereferencedType(loadedClassName.get(0));
                ModelElement_1_0 loadedMostSpecificClass =
                    this.model.getDereferencedType(loadedClassName.get(0));

                if (loadedClass.getValues("allSupertype").size()
                        > loadedMostSpecificClass.getValues("allSupertype").size()) {
                    // loadedMostSpecificClass should be a supertype of 
                    // loadedClass as it has a shorter allSupertype list.

                    ModelElement_1_0 tmp = loadedClass;
                    loadedClass = loadedMostSpecificClass;
                    loadedMostSpecificClass = tmp;

                    loadedMostSpecificClassName = loadedClassName;
                }

                // assert that it is true
                if (!loadedMostSpecificClass
                        .getValues("allSupertype")
                        .contains(loadedClass.getValues("subtype"))) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "role classes of states are not extensions from each other.",
                        new BasicException.Parameter("role", role),
                        new BasicException.Parameter(
                            "role class of state1",
                            loadedMostSpecificClass.getValues("qualifiedName")),
                            new BasicException.Parameter(
                                "role class of state2",
                                loadedMostSpecificClass.getValues("qualifiedName")),
                                new BasicException.Parameter(
                                    "object path",
                                    reqObject.path()));
                }
            }
        }

        // object_class is needed for saving. It is the same as in the loaded.
        // It should be the same for all states.
        result.values(SystemAttributes.OBJECT_CLASS).set(
            0,
            loadedStates[0].getValues(SystemAttributes.OBJECT_CLASS).get(0));

        // The attributes to add must be part of the reqMostSpecific class and
        // may not be part of the loadedMostSpecificClass (Each class holds
        // its attributes and all the derived ones.) In the request there
        // may also be attributes which don't belong to either class. This is 
        // ok as the create request for a certain role may be made within any
        // other role.
        String attributeName = null;
        for (Iterator attIter = reqObject.attributeNames().iterator();
        attIter.hasNext();
        ) {
            attributeName = (String)attIter.next();

            if (attributeName.equals(State_1_Attributes.VALID_FROM)
                    || attributeName.equals(State_1_Attributes.VALID_TO)
                    || attributeName.equals(SystemAttributes.MODIFIED_AT)
                    || attributeName.equals(SystemAttributes.MODIFIED_BY)) {
                if (reqObject.getValues(attributeName) != null
                        && !reqObject.getValues(attributeName).isEmpty()) {
                    result.values(attributeName).set(
                        0,
                        reqObject.getValues(attributeName).get(0));
                }
            }
            // don't modify those system attributes
            else if (
                    !attributeName.equals(SystemAttributes.CREATED_AT)
                    && !attributeName.equals(SystemAttributes.CREATED_BY)
                    && !attributeName.equals(SystemAttributes.OBJECT_CLASS)
                    && !attributeName.equals(RoleAttributes.IN_ROLE)
                    && !attributeName.equals(RoleAttributes.HAS_ROLE)
                    && !attributeName.equals(RoleAttributes.ROLE_TYPE)) {
                if (classHasAttribute(reqMostSpecificClassName, attributeName)
                        && !classHasAttribute(loadedMostSpecificClassName, attributeName)) {
                    // it's a new valid attribute, add it                       
                    result.values(
                        role + ROLE_ATTRIBUTE_SEPARATOR + attributeName).addAll(
                            reqObject.getValues(attributeName));
                }
            }
        }
        result
        .values(
            role + ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_CLASS)
            .add(reqMostSpecificClassName.get(0));
        return result;
    }

    //---------------------------------------------------------------------------
    /** 
     * Remove attributes which don't fit to one of the roles specified and 
     * cut the role specifier from the remaining attributes.
     * 
     * Remove the technical fields [role].core and [role].object_class.
     * 
     * Attributes without a role specifier are left unchanged.
     * 
     * Add the attribute hasRole to show all the roles the object has. This is 
     * not to confuse whith "inRole" which shows the actual role of the object.
     * 
     * @param    roles     list specifying the roles wanted
     * @param    obj       object to treat
     */ 
    private void reduceToRolesAndSpecifiedAttributes(
        List roles, 
        DataproviderObject_1_0 obj,
        DataproviderRequest request
    ) {
        String attrWithRole = null;
        String role = null;
        String attr = null;
        Set allRoles = new HashSet();
        int separatorPos = 0;
        Map renamedAttributes = new HashMap();

        Iterator attrIter = obj.attributeNames().iterator();
        while (attrIter.hasNext()) {
            role = null;
            attrWithRole = (String)attrIter.next();
            separatorPos = attrWithRole.indexOf(ROLE_ATTRIBUTE_SEPARATOR);
            if (separatorPos > 0) { // there is a role
                role = attrWithRole.substring(0, separatorPos);
                allRoles.add(role); // collect all roles of this object
                attr = attrWithRole.substring(separatorPos + 1);
            }
            else {
                attr = attrWithRole;
            }

            // is the attribute part of the result
            if (request.attributeSelector() == AttributeSelectors.NO_ATTRIBUTES
                    || (request.attributeSelector() != AttributeSelectors.ALL_ATTRIBUTES
                            && // all attributes received from super are typical
                            request
                            .attributeSelector()
                            != AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES
                            && // the only remaining option:
                            request.attributeSelector()
                            == AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES
                            && !request.attributeSpecifierAsMap().containsKey(attr)
                            && !attr.equals(SystemAttributes.MODIFIED_AT)
                            && !attr.equals(SystemAttributes.MODIFIED_BY)
                            && !attr.equals(SystemAttributes.CREATED_AT)
                            && !attr.equals(SystemAttributes.CREATED_BY)
                            && !attr.equals(SystemAttributes.OBJECT_CLASS)
                            && !attr.equals(State_1_Attributes.VALID_FROM)
                            && !attr.equals(State_1_Attributes.VALID_TO))) {
                attrIter.remove(); // the attribute is not part of the result
            }
            else if (role != null) { // is it part of the wanted roles
                if (roles.contains(role)
                        && // if it is in the roles required
                        !attrWithRole.endsWith(
                            ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_CLASS)
                            /* CR0000381
          &&
          !attrWithRole.endsWith(ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_CREATED_AT) &&
          !attrWithRole.endsWith(ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_MODIFIED_AT) 
                             */
                ) {
                    renamedAttributes.put(// cut off role specifier
                        attr, obj.values(attrWithRole));
                }
                attrIter.remove(); // remove all with role spec.
            }
            // else it is an attribute without role which stays 
        }

        // now add the renamed attributes again
        Iterator entriesIter = renamedAttributes.entrySet().iterator();
        Map.Entry entry = null;
        while (entriesIter.hasNext()) {
            entry = (Map.Entry)entriesIter.next();
            obj.values((String)entry.getKey()).addAll(
                (SparseList)entry.getValue());
        }

        // now add allRoles
        if (allRoles.size() > 0) {
            obj.clearValues(RoleAttributes.HAS_ROLE).addAll(allRoles);
        }
    } 

    //---------------------------------------------------------------------------
    /**
     * Get all the roles which are required for the basicRole specified.
     * 
     * The roles are ordered from the outermost to the innermost role. The 
     * outermost is the basicRole specified as a parameter.
     * 
     * @param header       needed for getting role types
     * @param basicRole    role for which all required roles are determined.
     * @param roleTypeCache cache with roleTypes loaded so far
     * @param objPath      path of object for that the roles are determined.
     * @param requiredRoleTypeObjects the dataprovider objects of the role types
     *                      returned get added to this map by there role name.
     * 
     * @return  ordered list of all roles starting with basicRole.
     */
    private List getRequiredRoles(
        ServiceHeader header,
        DataproviderRequest request,
        String basicRole, 
        Map roleTypeCache,
        Path objPath,
        Map requiredRoleTypeObjects
    ) throws ServiceException {        
        DataproviderObject roleType = null;
        List coreRoles = new ArrayList();

        if (basicRole != null) {
            Path roleTypeBasePath = this.getRoleTypesPath(objPath);
            roleType =
                getRoleType(
                    header,
                    request,
                    basicRole,
                    roleTypeCache,
                    roleTypeBasePath);

            //
            // now check for core roles        
            //
            coreRoles.add(basicRole);
            requiredRoleTypeObjects.put(basicRole, roleType);
            while (roleType != null
                    && !roleType.values(ROLETYPE_CORE_ROLE_ATTRIBUTE).isEmpty()) {
                String coreRole =
                    (String)roleType.values(ROLETYPE_CORE_ROLE_ATTRIBUTE).get(0);
                coreRoles.add(coreRole);
                requiredRoleTypeObjects.put(coreRole, roleType);
                roleType =
                    getRoleType(
                        header,
                        request,
                        coreRole,
                        roleTypeCache,
                        roleTypeBasePath);
            }
        }
        return coreRoles;
    }

    //---------------------------------------------------------------------------
    /**
     * Setup Reply according to the role demanded:
     * <ul>
     * <li> set roleId with roles </li>
     * <li> complete path with most specific role </li>
     * <li> remove role specifiers from attributeNames </li>
     * <li> remove attributes not belongig to the roles and extensions </li>
     * </ul>
     *
     * @param    header    request header
     * @param    reply     the native reply 
     * @param    role      required role
     * @param    roleTypeCache cache containing roles required 
     * @param    request   original request containing attribute specifiers
     * 
     * @return   the role prepared reply
     *
     * @exception   ServiceException
     *              on failure
     */
    private DataproviderReply completeReply(
        ServiceHeader header,
        DataproviderRequest request,
        DataproviderReply reply,
        String requestedRole,
        Path virtualRoleTypePath,        
        Map roleTypeCache
    ) throws ServiceException {

        if (virtualRoleTypePath != null) {

            for (int i = 0; i < reply.getObjects().length; i++) {
                // get or find on virtual role type path. The objects returned must contain 
                // the virtual role type path as demanded in the query.

                // add id if needed
                Path virtual = new Path(virtualRoleTypePath);
                if (!virtual
                        .getBase()
                        .equals(reply.getObjects()[i].path().getBase())) {
                    virtual.add(reply.getObjects()[i].path().getBase());
                }
                reply.getObjects()[i].path().setTo(virtual);

                String virtualClassName =
                    roleTypeVirtualMap.getVirtualClassName(
                        (String)reply.getObjects()[i].values(
                            SystemAttributes.OBJECT_CLASS).get(
                                0));
                if (virtualClassName == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Missing virtual class for remapped role type. (Configuration entries "
                        + LayerConfigurationEntries.ROLE_TYPE_MAPPING_REAL_CLASS
                        + " and "
                        + LayerConfigurationEntries.ROLE_TYPE_MAPPING_VIRTUAL_CLASS
                        + ")",
                        new BasicException.Parameter(
                            "path",
                            reply.getObjects()[i].path()),
                            new BasicException.Parameter(
                                "role",
                                reply.getObjects()[i].values(
                                    SystemAttributes.OBJECT_CLASS).get(
                                        0)));
                }
                else {
                    reply.getObjects()[i].clearValues(
                        SystemAttributes.OBJECT_CLASS).add(
                            virtualClassName);
                }
            }
        }

        List replyObjects = new ArrayList();

        Map requiredRoleObjects = new HashMap();
        List requiredRoles = null;
        requiredRoles =
            getRequiredRoles(
                header,
                request,
                requestedRole,
                roleTypeCache,
                toRolelessPath(new Path(request.path())),
                requiredRoleObjects);

        DataproviderObject original = null;
        DataproviderObject prepared = null;
        for (int i = 0; i < reply.getObjects().length; i++) {
            if (i == 0) {
                // get a copy for adding other roles.
                original = new DataproviderObject(reply.getObjects()[i], true);
            }
            prepared = reply.getObjects()[i];
            this.prepareReplyObject(
                header,
                request,
                prepared,
                requestedRole,
                requiredRoles,
                roleTypeCache);
            replyObjects.add(prepared);
        }

        if (reply.getObjects().length == 1
                && request.operation()
                == DataproviderOperations
                .OBJECT_RETRIEVAL //            && request.operation() != DataproviderOperations.ITERATION_CONTINUATION
                //            && request.operation() != DataproviderOperations.ITERATION_START
                //            && request.operation() != DataproviderOperations.OBJECT_MONITORING
                //            && request.operation() != DataproviderOperations.OBJECT_OPERATION
                //            && request.operation() != DataproviderOperations.OBJECT_REMOVAL
                && prepared.getValues(RoleAttributes.HAS_ROLE) != null) {
            // supply all roles at once; requested role must be first in reply,
            // and was already added above.
            List rolesAndBase =
                new ArrayList(prepared.getValues(RoleAttributes.HAS_ROLE));
            rolesAndBase.add(null); // add null for base
            for (Iterator r = rolesAndBase.iterator(); r.hasNext();) {
                String replyRole = (String)r.next();

                // got requested already                
                if (!(requestedRole == null && replyRole == null)
                        && !(replyRole != null && replyRole.equals(requestedRole))) {
                    DataproviderObject current =
                        new DataproviderObject(original, true);
                    try {
                        requiredRoles =
                            getRequiredRoles(
                                header,
                                request,
                                replyRole,
                                roleTypeCache,
                                toRolelessPath(new Path(request.path())),
                                requiredRoleObjects);

                        this.prepareReplyObject(
                            header,
                            request,
                            current,
                            replyRole,
                            requiredRoles,
                            roleTypeCache);
                        replyObjects.add(current);
                    }
                    catch (ServiceException se) {
                        // just ignore. If anything goes wrong one of the 
                        // additional roles will be missing. As these are only 
                        // provided for performance reasons, the main result
                        // is still valid. If the erronous role is accessed 
                        // later on, the exception will reoccur.
                    }
                }

                // in any case add roletype, they have not already been added.
                for (Iterator o = requiredRoleObjects.values().iterator();
                o.hasNext();
                ) {
                    DataproviderObject role =
                        new DataproviderObject((DataproviderObject)o.next());
                    replyObjects.add(role);
                }
            }
        }
        DataproviderReply newReply = super.completeReply(
            request,
            new DataproviderReply(replyObjects)
        );
        newReply.contexts().putAll(reply.contexts());
        return newReply;
    }

    //---------------------------------------------------------------------------
    /**
     * Does the object support the role?
     * <p>
     * Uses attribute role.object_class as indicator to decide whether the
     * role is present in the object or not.
     * 
     * @param obj  object to check for role
     * @param role role to check for
     * 
     * @return true if object has role or the role specified was null
     */
    private boolean objectHasRole(
        DataproviderObject_1_0 obj, 
        String role
    ) {
        if(role != null) {
            List indicator = obj.getValues(role + ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_CLASS);
            return (indicator != null) && !indicator.isEmpty();
        }
        else {
            return true;
        }
    }    

    //---------------------------------------------------------------------------
    /**
     * Adds a filter for the specified role to a request
     * 
     * @param    request   the request which needs a filter
     * @param    role      the role to filter for
     * @param    wantedRole specify if the role must be present or not
     *                       (if it is a positive criteria)
     * 
     * @return   same request with added role
     */    
    private DataproviderRequest addRoleFilter(    
        DataproviderRequest request,
        String role,
        boolean wantedRole
    ) {
        if (wantedRole) {
            request
            .addAttributeFilterProperty(new FilterProperty(
                Quantors.THERE_EXISTS,
                role + ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_CLASS,
                // existence of attribute means that role is supported
                FilterOperators.IS_NOT_IN
            ));
        }
        else {
            request
            .addAttributeFilterProperty(new FilterProperty(
                Quantors.FOR_ALL,
                role + ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_CLASS,
                // existence of attribute means that role is supported
                FilterOperators.IS_IN
            ));
        }
        return request;
    }        

    /**
     * Detect if the iteration is over the roles of an object.
     * 
     * @param path
     */
    private boolean isRoleIteration(Path path) {
        return path.endsWith(new String[]{ROLE_PATH_ENTRY});
    }

    //-----------------------------------------------------------------------
    /**
     * Iteration over the roles of an object. 
     * <p>
     * This is special comparing to all other methods in this plugin, because 
     * only in this case different roles are returned. In all other methods, 
     * the objects returned are exactly in one role. The other difference is
     * that this plugin normally does not create objects to return. Objects 
     * received get truncated to the role attributes, but are unchanged 
     * otherwise. Here new objects are created for each role present.
     * <p>
     * Iteration for large result sets are not supported, because the roles 
     * of an object are expected to be a very reduced set.
     * <p> 
     * Only non roled attributes are supported as search attributes.
     * <p>
     * Filters are not supported. Filtering on the core's attributes is stupid
     * because there is just one core object. Filtering on role attributes is 
     * critical because what happens to the other roles. Filtering for HAS_ROLE, 
     * IN_ROLE, or OBJECT_INSTANCE_OF, OBJECT_CLASS is probably the only 
     * which could be usefull. But as there are not too many roles expected, 
     * this is also left out for now. 
     * <p>
     * 
     * @param header
     * @param request
     * @return
     * @throws ServiceException
     */
    private DataproviderReply findRoleIteration(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        List replyObjects = new ArrayList();
        if (request.operation() == DataproviderOperations.ITERATION_START) {

            //
            //          Enable filtering:
            //            
            DataproviderObjectFilter filter;
            try {
                filter = new DataproviderObjectFilter(request.attributeFilter());
            }
            catch (RuntimeException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Invalid attribute filter",
                    new BasicException.Parameter(
                        "attributeFilter",
                        (Object[])request.attributeFilter()));
            }

            // 
            //          if (request.attributeFilter().length > 0) throw new ServiceException(
            //                StackedException.DEFAULT_DOMAIN,
            //                StackedException.NOT_SUPPORTED,
            //                new BasicException.Parameter[] { new BasicException.Parameter("filters", request.attributeFilter())},
            //                "No filters allowed for iterating roles."
            //          );

            //            for (int i = 0; i < attrFilters.length; i++) {                 
            //                if (attrFilters[i] != null && 
            //                    attrFilters[i].name() != null
            //                ) {
            //                    if (attrFilters[i].name().equals(RoleAttributes.HAS_ROLE) 
            //                        || attrFilters[i].name().equals(RoleAttributes.IN_ROLE)
            //                        || attrFilters[i].name().equals(SystemAttributes.OBJECT_CLASS)
            //                        || attrFilters[i].name().equals(SystemAttributes.OBJECT_INSTANCE_OF)
            //                    ) {
            //                        throw new ServiceException(
            //                            StackedException.DEFAULT_DOMAIN,
            //                            StackedException.NOT_SUPPORTED, 
            //                            new BasicException.Parameter[]{
            //                                new BasicException.Parameter(
            //                                    "FilterProperty.name", 
            //                                    attrFilters[i].name()
            //                                )
            //                            },
            //                            "FilterProperty for attribute not supported when iterating over the roles of an object." 
            //                        );
            //                    }
            //                }
            //            }
            // get object with all roles:
            Path noRolePath = toRolelessPath(new Path(request.path()));
            // must remove last role entry
            if (noRolePath.getBase().equals(ROLE_PATH_ENTRY)) {
                noRolePath.remove(noRolePath.size() - 1);
            }

            DataproviderRequest localRequest =
                new DataproviderRequest(
                    new DataproviderObject(noRolePath),
                    DataproviderOperations.OBJECT_RETRIEVAL,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null);
            localRequest.contexts().putAll(request.contexts());

            DataproviderReply superReply = super.get(header, localRequest);

            // now prepare a reply object for each of the roles the object has
            for (Iterator a = superReply.getObject().attributeNames().iterator();
            a.hasNext();
            ) {
                String attribute = (String)a.next();

                if (attribute
                        .endsWith(
                            ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_CLASS)) {
                    int pos = attribute.indexOf(ROLE_ATTRIBUTE_SEPARATOR);
                    String role = attribute.substring(0, pos);

                    DataproviderObject object =
                        new DataproviderObject(superReply.getObject());

                    List requiredRoles =
                        getRequiredRoles(
                            header,
                            request,
                            role,
                            this.roleTypes,
                            noRolePath,
                            new HashMap());

                    prepareReplyObject(
                        header,
                        request,
                        object,
                        role,
                        requiredRoles,
                        this.roleTypes);
                    //
                    //                  Enable filtering:
                    //            
                    if (filter.accept(object))
                        replyObjects.add(object);

                }
            }
        }
        else if (
                request.operation()
                == DataproviderOperations.ITERATION_CONTINUATION) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "iteration continuation not supported for role iteration.",
                new BasicException.Parameter("request.path", request.path()));
        }

        DataproviderReply reply = new DataproviderReply(replyObjects);

        reply.context(DataproviderReplyContexts.HAS_MORE).set(0, Boolean.FALSE);
        reply.context(DataproviderReplyContexts.TOTAL).set(0, new Integer(replyObjects.size()));
        reply.context(DataproviderReplyContexts.REFERENCE_FILTER).set(
            0,
            request.path().toString());
        return reply;
    }

    //-----------------------------------------------------------------------
    /** 
     * Prepare the object as a reply object for the role specified.
     * 
     * @param header
     * @param request
     * @param object
     * @param role
     * @param requiredRoles
     * @param roleTypeCache
     * @throws ServiceException
     */
    private void prepareReplyObject(
        ServiceHeader header,
        DataproviderRequest request, 
        DataproviderObject_1_0 object,
        String role, 
        List requiredRoles,
        Map roleTypeCache
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("RoleObject_1.prepReply");
        try {
            String[] rolePathEnding = null;

            int rolesSize = requiredRoles.size();
            rolePathEnding = new String[rolesSize * 2];
            for (int r = 0; r < rolesSize; r++) {
                rolePathEnding[2 * r] = this.ROLE_PATH_ENTRY;
                rolePathEnding[2 * r + 1] =
                    (String)requiredRoles.get(rolesSize - 1 - r);
            }

            // check that the object supports the role demanded
            if (objectHasRole(object, role)) {
                if (role != null) {
                    object.clearValues(RoleAttributes.IN_ROLE).addAll(requiredRoles);
                    this.addRolePathToPath(rolePathEnding, object.path());

                    // get most specific object_class for the  hierarchy
                    // most specific object class is the one of the outermost
                    // role.                            
                    object.clearValues(SystemAttributes.OBJECT_CLASS).addAll(
                        object.values(
                            role
                            + ROLE_ATTRIBUTE_SEPARATOR
                            + SystemAttributes.OBJECT_CLASS));

                    // add derived attribute roleType (no checking done here,
                    // just assume the roleType exists.) 
                    Path roleTypePath =
                        this.getRoleTypesPath(object.path()).add(
                            object.path().getBase());
                    object.clearValues(RoleAttributes.ROLE_TYPE).add(0, roleTypePath);
                    this.assertCompleteRoleType(
                        header,
                        request,
                        role,
                        roleTypeCache,
                        object);
                    this.assertRequiredRoles(
                        requiredRoles,
                        null,
                        new DataproviderObject_1_0[] { object });
                }
                this.reduceToRolesAndSpecifiedAttributes(
                    requiredRoles,
                    object,
                    request);
            }
            else {
                // can not remove single object from reply (no access)
                // can not create new reply because context would be lost
                // the only thing which remains:
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "reply object is not in expected role. can not complete reply.",
                    new BasicException.Parameter("reply object", object),
                    new BasicException.Parameter("role", role));
            }
        }
        finally {
            StopWatch_1.instance().stopTimer("RoleObject_1.prepReply");
        }
    }

    //-----------------------------------------------------------------------
    private DataproviderRequest removeRoleAttributeSpecifier(
        DataproviderRequest request
    ) throws ServiceException {
        DataproviderRequest replyRequest = null;
        final Set derivedRoleAttributes = new HashSet();
        derivedRoleAttributes.add(RoleAttributes.HAS_ROLE);
        derivedRoleAttributes.add(RoleAttributes.IN_ROLE);
        derivedRoleAttributes.add(RoleAttributes.ROLE_TYPE);

        boolean hasDerived = false;
        List newSpecifier = new ArrayList();

        for (Iterator a = derivedRoleAttributes.iterator();
        a.hasNext() && !hasDerived;
        ) {
            hasDerived = request.attributeSpecifierAsMap().containsKey(a.next());
        }

        if (hasDerived) {
            for (int i = 0; i < request.attributeSpecifier().length; i++) {
                if (!derivedRoleAttributes
                        .contains(request.attributeSpecifier()[i].name())) {
                    newSpecifier.add(request.attributeSpecifier()[i]);
                }
            }
            replyRequest =
                new DataproviderRequest(
                    request.object(),
                    request.operation(),
                    request.attributeFilter(),
                    request.position(),
                    request.size(),
                    request.direction(),
                    request.attributeSelector(),
                    (AttributeSpecifier[])newSpecifier.toArray(
                        new AttributeSpecifier[newSpecifier.size()]));
            replyRequest.contexts().putAll(request.contexts());
        }
        else {
            replyRequest = request;
        }
        return replyRequest;
    }

    //-----------------------------------------------------------------------
    public DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("RoleObject_1.set");
        String role = null;
        DataproviderReply reply = null;                
        role = this.getRoleFromPath(request.path());
        if(role != null) {
            SysLog.trace("role: ", role); 
            DataproviderObject[] states = this.findCoreObjectStates(
                header, 
                request
            );
            boolean hasRole = false;
            for(
                    int i = 0; 
                    i < states.length && !hasRole; 
                    i++
            ) {
                hasRole = states[i].containsAttributeName(role + ROLE_ATTRIBUTE_SEPARATOR + SystemAttributes.OBJECT_CLASS);
            }            
            if (hasRole) {
                // this role already exists for this object:
                if (request.object().attributeNames().size() > 3) {
                    reply = this.replace(header, request);
                }
                else {
                    // empty request, just return empty object
                    reply = new DataproviderReply(request.object());
                }
            }
            else {
                // this role does not yet exist:
                reply = this.create(header, request);
            }
        }
        else {
            boolean doCreate = false;
            DataproviderRequest newRequest = 
                new DataproviderRequest(
                    new DataproviderObject(request.path().getParent()),
                    DataproviderOperations.ITERATION_START, 
                    AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                    null
                );
            newRequest.addAttributeFilterProperty(
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    SystemAttributes.OBJECT_IDENTITY,
                    FilterOperators.IS_IN,
                    request.path().toUri()
                )
            );
            newRequest.contexts().putAll(request.contexts());          
            DataproviderObject[] existingObjs =
                super.find(
                    header,
                    newRequest
                ).getObjects();              
            if(existingObjs.length == 0) {
                doCreate = true;
            }
            else {
                doCreate = false;
                request.object().setDigest(existingObjs[0].getDigest());    
            }
            if(doCreate) {
                request.object().clearValues(
                    SystemAttributes.CREATED_BY
                ).addAll(
                    request.object().values(SystemAttributes.MODIFIED_BY)
                );
                request.object().clearValues(
                    SystemAttributes.CREATED_AT
                ).addAll(
                    request.object().values(SystemAttributes.MODIFIED_AT)
                );            
                reply = this.create(header, request); 
            }
            else {
                reply = this.replace(header, request);
            }
        }
        StopWatch_1.instance().stopTimer("RoleObject_1.set");
        return this.completeReply(
            request,
            reply
        );
    }

    //---------------------------------------------------------------------------
    /**
     * Get the object specified by the requests's path.
     * 
     * Returns the object in the role specified in path. If there is no role 
     * specified, one of the topmost non abstract classes in the role
     * hierarchy are returned. Those roles are exclusive. 
     *
     * @param       header
     *              request header
     * @param       _request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest _request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("RoleObject_1.get");
        String role = null;
        Path virtualRoleTypePath = null;
        DataproviderReply superReply = null;

        // remove attributeSpecifiers for derived attributes of role (they will 
        // get added in completeReply.

        DataproviderRequest request = removeRoleAttributeSpecifier(_request);

        convertRoleTypeToRoleInstanceSearch(header, request);

        role = this.getRoleFromPath(request.path());
        if (role != null) {
            StopWatch_1.instance().startTimer("role1GetRole");
            SysLog.trace("role: ", role);
            Path noRolePath = toRolelessPath(new Path(request.path()));

            // no use to add attribute filter because the filters don't 
            // get evaluated (at least by the in memory persistence.)
            DataproviderRequest localRequest =
                new DataproviderRequest(
                    new DataproviderObject(noRolePath),
                    DataproviderOperations.OBJECT_RETRIEVAL,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null);
            localRequest.contexts().putAll(request.contexts());

            superReply = super.get(header, localRequest);
            StopWatch_1.instance().stopTimer("role1GetRole");
        }
        else {
            // an object without roles is searched
            StopWatch_1.instance().startTimer("role1GetNoRole");

            // check for RoleType remapping
            Path originalRoleTypePath =
                roleTypeVirtualMap.remapVirtualPath(request.path());

            if (originalRoleTypePath != null) {
                virtualRoleTypePath = request.path();
                request.path().setTo(originalRoleTypePath);
            }

            superReply = super.get(header, request);
            StopWatch_1.instance().stopTimer("role1GetNoRole");
        }

        DataproviderReply reply =
            super.completeReply(
                request,
                this.completeReply(
                    header,
                    request,
                    superReply,
                    role,
                    virtualRoleTypePath,
                    this.roleTypes));
        StopWatch_1.instance().stopTimer("RoleObject_1.get");
        return reply;
    }

    //---------------------------------------------------------------------------
    /**
     * Find the objects specified by the references and filter properties, the 
     * role can be specified through the inRole attribute in a clause like:
     * <p>
     * THERE_EXISTS inRole IS_IN <roleSpecifier>   
     * <p>
     * where roleSpecifier must be exactly one role. The objects returned are 
     * in this role.
     * <p>
     * It can be searched for all objects not having a certain role:
     * <p>
     * FOR_ALL hasRole IS_NOT_IN <roleSpecifier>
     * <p>
     * where roleSpecifier can be any roles. The objects returned are in no
     * role at all.
     * <p>
     * Attributes serving as a serach criteria must be either part of the 
     * core objects or part of the role specified. 
     * 
     *
     * @param       header
     *              request header
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("RoleObject_1.find");
        DataproviderReply superReply = null;
        String replyRole = null; // role of the reply
        DataproviderRequest findRequest = null;
        Path virtualRoleTypePath = null;

        if (isRoleIteration(request.path())) {
            /** NOTE: direct return statement here */
            DataproviderReply reply = findRoleIteration(header, request);
            StopWatch_1.instance().stopTimer("RoleObject_1.find");
            return reply;
        }

        convertRoleTypeToRoleInstanceSearch(header, request);

        replyRole = this.getRoleFromPath(request.path());

        if (replyRole != null) {
            // If the role is statefull, requests for different states of
            // one role become possible. The path looks something like:
            // objPath/objId/role/roleId/historyState   or
            // objPath/objId/role/roleId/validState

            toRolelessPath(request.path()); // change the path of request !!!

            // only states with that role should be found, add the role as 
            // an attribute filter 
            addRoleFilter(request, replyRole, true);
        }

        // normaly the role is not contained in path. Rather a role must be
        // specified with inRole. 
        FilterProperty[] attrFilters = request.attributeFilter();
        boolean hasRole = false;
        String filterRole = null; // role contained in filter
        ArrayList objectClassFilters = new ArrayList();

        for (int i = 0; i < attrFilters.length && filterRole == null; i++) {
            if (attrFilters[i] != null && attrFilters[i].name() != null) {
                if (attrFilters[i].name().equals(RoleAttributes.HAS_ROLE)) {
                    hasRole = true;
                }
                else if (attrFilters[i].name().equals(RoleAttributes.IN_ROLE)) {
                    // get the role for all attributes
                    filterRole = (String)attrFilters[i].getValue(0);
                }
                else if (
                        attrFilters[i].name().equals(SystemAttributes.OBJECT_CLASS)
                        && attrFilters[i].operator() == FilterOperators.IS_IN) {
                    objectClassFilters.addAll(attrFilters[i].values());
                }
                else if (
                        attrFilters[i].name().equals(SystemAttributes.OBJECT_INSTANCE_OF)
                        && attrFilters[i].operator() == FilterOperators.IS_IN) {
                    objectClassFilters.addAll(attrFilters[i].values());
                }
            }
        }

        if ((!hasRole && filterRole == null)
                && request.operation() == DataproviderOperations.ITERATION_START
                && objectClassFilters.size() > 0) {
            // if no role is specified but a class is specified, the class is
            // not allowed to be a roled class
            for (Iterator c = objectClassFilters.iterator(); c.hasNext();) {
                String objectClass = (String)c.next();
                ModelElement_1_0 modelClass =
                    this.model.getDereferencedType(objectClass);
                if (modelClass != null && classIsRole(modelClass)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        "For finding a role class through a filter, a role must be specified too.",
                        new BasicException.Parameter(
                            "filtered class",
                            modelClass.getValues("qualifiedName").get(0)));
                }
            }
        }

        if ((hasRole || filterRole != null)
                && request.operation() == DataproviderOperations.ITERATION_START) {
            SysLog.trace(" filterRole: ", filterRole);

            replyRole = filterRole;

            AttributeSpecifier[] specifiers =
                new AttributeSpecifier[request.attributeSpecifier().length];

            // need to copy the attribute specifiers
            for (int i = 0; i < specifiers.length; i++) {
                String attrName = request.attributeSpecifier()[i].name();
                if (isRoleAttribute(attrName, request.path())) {
                    if (request.attributeSpecifier()[i].order() == Orders.ANY) {

                        specifiers[i] =
                            new AttributeSpecifier(
                                filterRole + ROLE_ATTRIBUTE_SEPARATOR + attrName,
                                request.attributeSpecifier()[i].position(),
                                request.attributeSpecifier()[i].size(),
                                request.attributeSpecifier()[i].direction());
                    }
                    else {
                        specifiers[i] =
                            new AttributeSpecifier(
                                filterRole + ROLE_ATTRIBUTE_SEPARATOR + attrName,
                                request.attributeSpecifier()[i].position(),
                                request.attributeSpecifier()[i].order());
                    }
                }
                else {
                    specifiers[i] = request.attributeSpecifier()[i];
                }
            }

            // prepare a new request out of the existing one,
            // with changed attributeFilters:
            findRequest =
                new DataproviderRequest(
                    new DataproviderObject(toRolelessPath(new Path(request.path()))),
                    DataproviderOperations.ITERATION_START,
                    null,
                    request.position(),
                    request.size(),
                    request.direction(),
                    AttributeSelectors.ALL_ATTRIBUTES,
                    specifiers);
            findRequest.contexts().putAll(request.contexts());

            for (int i = 0; i < attrFilters.length; i++) {
                // attrFilters[i].name() may not be null here because of roleSelect
                if (attrFilters[i].name().equals(RoleAttributes.HAS_ROLE)) {
                    if (attrFilters[i].operator() == FilterOperators.IS_NOT_IN
                            && attrFilters[i].quantor() == Quantors.FOR_ALL) {
                        for (int j = 0; j < attrFilters[i].getValues().length; j++) {
                            addRoleFilter(
                                findRequest,
                                (String)attrFilters[i].getValue(j),
                                false);
                        }
                    }
                    else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "Only FOR_ALL ... IS_NOT_IN is supported for attribute.",
                            new BasicException.Parameter(
                                "attribute",
                                RoleAttributes.HAS_ROLE),
                                new BasicException.Parameter(
                                    "filter.operator",
                                    attrFilters[i].operator()),
                                    new BasicException.Parameter(
                                        "filter.quantor",
                                        attrFilters[i].quantor()));
                    }
                }
                else if (attrFilters[i].name().equals(RoleAttributes.IN_ROLE)) {
                    if (attrFilters[i].operator() == FilterOperators.IS_IN
                            && attrFilters[i].quantor() == Quantors.THERE_EXISTS
                            && attrFilters[i].getValues().length == 1)
                        // for now just one role is supported
                    {
                        addRoleFilter(findRequest, filterRole, true);
                    }
                    else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "Only THERE_EXISTS ... IS_IN with one value is supported for attribute.",
                            new BasicException.Parameter(
                                "attribute",
                                RoleAttributes.IN_ROLE),
                                new BasicException.Parameter(
                                    "filter.values",
                                    attrFilters[i].getValues()),
                                    new BasicException.Parameter(
                                        "filter.operator",
                                        attrFilters[i].operator()),
                                        new BasicException.Parameter(
                                            "filter.quantor",
                                            attrFilters[i].quantor()));
                    }
                }
                else if (
                        attrFilters[i].name().equals(SystemAttributes.OBJECT_CLASS)) {
                    // if it is object_class of a role, it can only occur as 
                    // object_class for that role and must be prefixed with role.
                    ModelElement_1_0 modelClass =
                        this.model.getDereferencedType(attrFilters[i].getValue(0));
                    if (modelClass != null && classIsRole(modelClass)) {
                        FilterProperty filter =
                            new FilterProperty(
                                attrFilters[i].quantor(),
                                filterRole
                                + ROLE_ATTRIBUTE_SEPARATOR
                                + attrFilters[i].name(),
                                attrFilters[i].operator(),
                                attrFilters[i].getValues());
                        findRequest.addAttributeFilterProperty(filter);
                    }
                    else {
                        findRequest.addAttributeFilterProperty(attrFilters[i]);
                    }
                }
                else if (
                        attrFilters[i].name().equals(
                            SystemAttributes.OBJECT_INSTANCE_OF)) {
                    if (attrFilters[i].operator() == FilterOperators.IS_IN
                            && attrFilters[i].quantor() == Quantors.THERE_EXISTS
                            && attrFilters[i].getValues().length
                            == 1 // for now just one instanceOf class is supported
                    ) {
                        // instance_of has to be translated if the class is a role class
                        ModelElement_1_0 modelClass =
                            this.model.getDereferencedType(attrFilters[i].getValue(0));
                        ModelElement_1_0 subtype = null;
                        Set subClasses = new HashSet();

                        if (modelClass != null && classIsRole(modelClass)) {
                            // add class and subtypes to object_class clause, but
                            // only if subtypes are not roles themselfs
                            for (Iterator subIter =
                                modelClass.values("subtype").iterator();
                            subIter.hasNext();
                            ) {

                                Path subPath = (Path)subIter.next();
                                subtype = model.getDereferencedType(subPath);
                                if (classBelongsToRole(subtype, modelClass)) {
                                    subClasses.add(subPath.getBase());
                                }
                            }

                            FilterProperty filter =
                                new FilterProperty(
                                    Quantors.THERE_EXISTS,
                                    filterRole
                                    + ROLE_ATTRIBUTE_SEPARATOR
                                    + SystemAttributes.OBJECT_CLASS,
                                    FilterOperators.IS_IN,
                                    subClasses.toArray());
                            findRequest.addAttributeFilterProperty(filter);

                        }
                        else {
                            findRequest.addAttributeFilterProperty(attrFilters[i]);
                        }
                    }
                    else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "Only THERE_EXISTS ... IS_IN with just one value is supported for attribute.",
                            new BasicException.Parameter(
                                "attribute",
                                SystemAttributes.OBJECT_INSTANCE_OF),
                                new BasicException.Parameter(
                                    "filter.values",
                                    attrFilters[i].getValues()),
                                    new BasicException.Parameter(
                                        "filter.operator",
                                        attrFilters[i].operator()),
                                        new BasicException.Parameter(
                                            "filter.quantor",
                                            attrFilters[i].quantor()));
                    }
                }
                else {
                    // attributes of the other filter conditions have to be checked
                    // if they have to be prefixed with the role
                    String attrName = attrFilters[i].name();
                    if (isRoleAttribute(attrName, request.path())) {
                        // if it is a role attribute it can only be of the specified role
                        FilterProperty filter =
                            new FilterProperty(
                                attrFilters[i].quantor(),
                                filterRole + ROLE_ATTRIBUTE_SEPARATOR + attrName,
                                attrFilters[i].operator(),
                                attrFilters[i].getValues());
                        findRequest.addAttributeFilterProperty(filter);
                    }
                    else {
                        findRequest.addAttributeFilterProperty(attrFilters[i]);
                    }
                }
            }
        }
        else if (
                request.operation()
                == DataproviderOperations.ITERATION_CONTINUATION) {
            // first get my iteration information
            Role_1Iterator roleIterator =
                (Role_1Iterator)Role_1Iterator.deserialize(
                    (byte[])request.context(DataproviderReplyContexts.ITERATOR).get(
                        0));

            // now set the remaining iterator information for the following layers
            request.context(DataproviderReplyContexts.ITERATOR).set(
                0,
                roleIterator.getIterator());

            replyRole = roleIterator.getRole();

            findRequest = request;
        }
        else { // some search which does not touch roles 

            // check for RoleType remapping
            Path originalRoleTypePath =
                roleTypeVirtualMap.remapVirtualPath(request.path());

            if (originalRoleTypePath != null) {
                virtualRoleTypePath = request.path();
                request.path().setTo(originalRoleTypePath);
            }

            findRequest = request;
        }

        // an object without roles is searched or an object with iteration_continuation
        superReply = super.find(header, findRequest);
        DataproviderReply reply =
            completeReply(
                header,
                request,
                superReply,
                replyRole,
                virtualRoleTypePath,
                this.roleTypes);

        // must do it even if replyRole is null because we cant find out
        // if it was set.

        reply.context(DataproviderReplyContexts.ITERATOR).set(
            0,
            Role_1Iterator.serialize(
                new Role_1Iterator(
                    replyRole,
                    (byte[])reply.context(DataproviderReplyContexts.ITERATOR).get(
                        0))));
        StopWatch_1.instance().stopTimer("RoleObject_1.find");
        return reply;
    }

    //---------------------------------------------------------------------------
    /**
     * Create a new object or a new role to an existing object.
     * <p>
     * If a new role to an existing object is to be created the name of the
     * role must be set in the path (.../role/roleName). 
     * <p>
     * Attributes not belonging to the new Role are ignored, even if they have
     * changed a value. 
     * 
     * 
     * @param     header     request header
     * @param     request    the request
     *
     * @return        the reply
     *
     * @exception ServiceException   on failure
     */
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("RoleObject_1.create");
        DataproviderReply reply = null;
        String newRole = null;

        // the first processing class in model layer must remove derived attributes
        removeNonPersistentAttributes(request.object());

        ModelElement_1_0 objClass =
            this.model.getDereferencedType(
                request.object().getValues(
                    SystemAttributes.OBJECT_CLASS).get(
                        0));

        if (classIsRole(objClass)) {
            newRole = getRoleFromPath(request.path());
            SysLog.trace("create role:", newRole);

            DataproviderObject roleType = null;

            assertRoleNotNull(newRole, request.object());

            roleType =
                assertCompleteRoleType(
                    header,
                    request,
                    newRole,
                    this.roleTypes,
                    request.object());

            List requiredRoles =
                getRequiredRoles(
                    header,
                    request,
                    newRole,
                    this.roleTypes,
                    request.path(),
                    new HashMap());

            // coreStates must be in ascendingOrder regarding their validFrom date
            DataproviderObject[] coreStates =
                findCoreObjectStates(header, request);

            assertRequiredRoles(requiredRoles, newRole, coreStates);

            assertCoreRoleExtensionClass(
                header,
                request,
                newRole,
                this.roleTypes,
                requiredRoles,
                coreStates,
                request.object());

            assertCreatingUniqueRoleInstance(
                header,
                request,
                roleType,
                requiredRoles,
                coreStates);

            DataproviderObject updateObject =
                createUpdateObject(
                    coreStates,
                    request.object(),
                    newRole,
                    requiredRoles);

            StopWatch_1.instance().startTimer("create-DB");
            DataproviderRequest modifyRequest =
                new DataproviderRequest(
                    updateObject,
                    DataproviderOperations.OBJECT_MODIFICATION,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null);
            modifyRequest.contexts().putAll(request.contexts());
            reply = super.modify(header, modifyRequest);
            StopWatch_1.instance().stopTimer("create-DB");
        }
        else {
            // there is no role present, just save.
            roleTypeVirtualMap.assertNoModificationByVirtualPath(request.path());
            reply = super.create(header, request);
        }

        completeReply(header, request, reply, newRole, null, this.roleTypes);
        StopWatch_1.instance().stopTimer("RoleObject_1.create");

        return reply;
    }

    //---------------------------------------------------------------------------
    /**
     * Modifies some of the values of one or more attributes. The values are 
     * compared and changed by index in the values array. If the new value is 
     * null, the old is left unchanged. It is not possible to delete single 
     * values.
     * <p>
     * All attributes must be part of the role specified in path or one of its
     * core roles. An exception will be thrown otherwise.
     * <p>
     * The Attributes inRole, hasRole are not used in treating the request, but
     * will be correctly set on the reply. 
     * <p>
     * In case of statefull roles, header.requestedAt must be a time where the 
     * role to change exists.
     *
     * @param       header     request header
     * @param       request    the request
     *
     * @return      the reply
     *
     * @exception   ServiceException   on failure
     */
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("RoleObject_1.modify");
        String role = null;
        DataproviderReply superReply = null;

        // the first processing class in model layer must remove derived attributes
        removeNonPersistentAttributes(request.object());

        String objClassName =
            (String)request.object().getValues(
                SystemAttributes.OBJECT_CLASS).get(
                    0);

        ModelElement_1_0 objClass =
            this.model.getDereferencedType(objClassName);

        if (classIsRole(objClass)) {
            role = getRoleFromPath(request.path());
            SysLog.trace("modify in role: ", role);

            assertRoleNotNull(role, request.object());

            DataproviderObject expanded =
                createExpandedObject(
                    header,
                    request,
                    role,
                    this.roleTypes,
                    MODIFICATION_OP);

            DataproviderRequest modifyRequest =
                new DataproviderRequest(
                    expanded,
                    DataproviderOperations.OBJECT_MODIFICATION,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null);
            modifyRequest.contexts().putAll(request.contexts());

            superReply = super.modify(header, modifyRequest);
        }
        else {
            // an object without roles is modified, use super implementation
            roleTypeVirtualMap.assertNoModificationByVirtualPath(request.path());
            superReply = super.modify(header, request);
        }

        DataproviderReply reply =
            completeReply(
                header,
                request,
                superReply,
                role,
                null,
                this.roleTypes);
        StopWatch_1.instance().stopTimer("RoleObject_1.modify");
        return reply;
    }

    //---------------------------------------------------------------------------
    /**
     * Replaces the values of one or more attributes. 
     * <p>
     * All attributes must be part of the role specified in path or one of its
     * core roles. An exception will be thrown otherwise.
     * <p>
     * The Attributes inRole, hasRole are not used in treating the request, but
     * will be correctly set on the reply. 
     * <p>
     * In case of statefull roles, header.requestedAt must be a time where the 
     * role to change exists.
     * 
     * @param     header
     *                request header
     * @param     request
     *                the request
     *
     * @return        the reply
     *
     * @exception ServiceException
     *                on failure
     */
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("RoleObject_1.replace");
        String role = null;
        DataproviderReply superReply = null;

        // the first processing class in model layer must remove derived attributes
        removeNonPersistentAttributes(request.object());

        String objClassName =
            (String)request.object().getValues(
                SystemAttributes.OBJECT_CLASS).get(
                    0);

        ModelElement_1_0 objClass =
            this.model.getDereferencedType(objClassName);

        if (classIsRole(objClass)) {
            role = getRoleFromPath(request.path());
            SysLog.trace("replace in role: ", role);

            assertRoleNotNull(role, request.object());

            DataproviderObject expanded =
                createExpandedObject(
                    header,
                    request,
                    role,
                    this.roleTypes,
                    REPLACE_OP);

            DataproviderRequest replaceRequest =
                new DataproviderRequest(
                    expanded,
                    DataproviderOperations.OBJECT_REPLACEMENT,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null);
            replaceRequest.contexts().putAll(request.contexts());

            superReply = super.replace(header, replaceRequest);
        }
        else {
            // an object without roles is modified, use super implementation
            roleTypeVirtualMap.assertNoModificationByVirtualPath(request.path());
            superReply = super.replace(header, request);
        }

        DataproviderReply reply =
            completeReply(
                header,
                request,
                superReply,
                role,
                null,
                this.roleTypes);
        StopWatch_1.instance().stopTimer("RoleObject_1.replace");
        return reply;
    }

    //---------------------------------------------------------------------------
    /**
     * Removes an object's role and it's dependent roles. Removing without
     * a role specified in path removes the entire object and all its roles.
     *
     * @param       header   request header
     * @param       request  the request
     *
     * @return      the object in it's original state, before the remove. 
     *               In the role specified in path.
     *
     * @exception   ServiceException  on failure
     */
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("RoleObject_1.remove");
        try {
            DataproviderReply superReply = null;
            String role = getRoleFromPath(request.path());
            if (role != null) {
                SysLog.trace(" role: ", role);
                Path noRolePath = toRolelessPath(new Path(request.path()));

                // get the object from storage this will be used for reply 
                // because the reply is based on the original object.
                DataproviderRequest getRequest =
                    new DataproviderRequest(
                        new DataproviderObject(noRolePath),
                        DataproviderOperations.OBJECT_RETRIEVAL,
                        AttributeSelectors.ALL_ATTRIBUTES,
                        null);
                getRequest.contexts().putAll(request.contexts());
                superReply = super.get(header, getRequest);
                DataproviderObject replaceObject =
                    this.removeRoleDependentAttributes(
                        header,
                        request,
                        (DataproviderObject)superReply.getObject().clone(),
                        role,
                        this.roleTypes);
                DataproviderRequest replaceRequest =
                    new DataproviderRequest(
                        replaceObject,
                        DataproviderOperations.OBJECT_REPLACEMENT,
                        AttributeSelectors.ALL_ATTRIBUTES,
                        null);
                replaceRequest.contexts().putAll(request.contexts());
                super.replace(header, replaceRequest);
            }
            else {
                this.assertRoleTypeRemove(header, request);
                roleTypeVirtualMap.assertNoModificationByVirtualPath(request.path());
                superReply = super.remove(header, request);
            }
            DataproviderReply reply =
                completeReply(
                    header,
                    request,
                    superReply,
                    role,
                    null,
                    this.roleTypes);

            return reply;
        }
        finally {
            StopWatch_1.instance().stopTimer("RoleObject_1.remove");
        }   
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
    private static final String ROLE_ATTRIBUTE_SEPARATOR = "$";

    private final String ROLE_PATH_ENTRY = "role";
    private final String ROLE_STEREOTYPE = Stereotypes.ROLE;

    private final boolean REPLACE_OP = true;
    private final boolean MODIFICATION_OP = false;  

    private String AUTHORITY_TYPE_NAME = "org:openmdx:base:Authority";

    private final String ROLETYPE_CORE_ROLE_ATTRIBUTE = "coreRole";

    // name of the entry which gets added to the request.context() in 
    // findCoreObjectStates()
    public final String ROLE_OBJECT_STATES = "RoleObjectStates";

    private Model_1_0 model = null;

    // mapping from RoleType to the corresponding role class. The elements
    // are of type QualifierAndRoleClass, they also contain the mapping 
    // attribute. 
    private RoleTypeReference roleTypeReference = null;

    // map role and corresponding RoleType. path patterns are object paths
    // with removed object ids.
    private Map roleTypePathPatternMap = new HashMap(); 
    private List allRoleTypeReferencePaths = new ArrayList(); // only roletypes

    // map virtual role type reference paths to the original objects
    private RoleTypeVirtualMap roleTypeVirtualMap = null;

    // map containing all referencePath's in the model and the classes they 
    // lead to. This is not primarely for RoleType processing!
    private Map referencePathClassMap = null;           

    // Cache for role types. RoleTypes are added on-demand to the cache.
    // RoleTypes removed from the persistency storage are never removed from
    // the cache. 
    // IMPORTANT: Removing roleTypes is not supported by this implementation.
    private Map roleTypes = new HashMap();

}

//--- End of File -----------------------------------------------------------
