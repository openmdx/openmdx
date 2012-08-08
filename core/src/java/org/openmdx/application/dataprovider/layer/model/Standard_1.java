/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Standard_1.java,v 1.7 2009/06/04 14:46:33 hburger Exp $
 * Description: Model layer
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/04 14:46:33 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.model;

import static org.openmdx.application.dataprovider.layer.type.Strict_1.STATE1_STATE_CAPABLE_CLASS;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Directions;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.base.text.format.DatatypeFormat;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Standard implementation for model layer.
 * 
 * What is done here:
 * <ul>
 * <li> replacement of find requests for OBJECT_INSTANCE_OF by OBJECT_CLASS IS_IN
 *      all subtypes of original class </li>
 * <li> set OBJECT_INSTANCE_OF attribute on DataproviderObjects bound for the
 *      application layer </li>
 * <li> remove derived attributes, except some technical ones. This also removes
 *      OBJECT_INSTANCE_OF on objects bound for the persistence layer. </li>
 * <li> conversion of strings to Path, where needed. </li>
 * </ul>
 */
public class Standard_1 extends SystemAttributes_1 {

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.OptimisticLocking_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {
        super.activate(id, configuration, delegation);
        this.datatypeFormat = configuration.isOn(
            SharedConfigurationEntries.XML_DATATYPES
        ) ? DatatypeFormat.newInstance(true) : null;
        this.notifyPreDelete = configuration.isOn(
            LayerConfigurationEntries.NOTIFY_PRE_DELETE
        );
        this.optimisticLocking = 
            configuration.isOn(LayerConfigurationEntries.OPTIMISTIC_LOCKING) || 
            "whenModified".equalsIgnoreCase(configuration.getFirstValue(LayerConfigurationEntries.OPTIMISTIC_LOCKING));        
    }

    // --------------------------------------------------------------------------
    /**
     * Tells whether XML datatype formatting is required
     * @return
     */
    protected final boolean useDatatypes(
    ){
        return this.datatypeFormat != null;
    }

    // --------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0#epilog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[], org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply[])
     */
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        super.epilog(header, requests, replies);
        this.propagateDigest(replies);
    }
    
    // --------------------------------------------------------------------------
    protected String getObjectClassName(
        MappedRecord object
    ){
        if(object != null) {
            return ObjectHolder_2Facade.getObjectClass(object);
        }
        return null;
    }
    
    // --------------------------------------------------------------------------
    /**
     * An object is freed from derived attributes in the RoleObject_1. But if an
     * object is retrieved and not returned to the client but only to a 
     * higher layer, and the same object or a copy of it is saved again, the 
     * derived attributes of this object have to be removed again. 
     * <p>
     * For example Standard itself adds the derived attribute object_instanceOf.
     * But there may be other derived attributes added in state or role.
     * <p> 
     * To avoid multiple scanning of the entire object, object_instanceOf is 
     * used as a trigger for a new scan for derived attributes. 
     * 
     * @param  object  object to remove derived attributes from 
     */
    private void triggeredRemoveDerivedAttributes(
        MappedRecord object
    ) throws ServiceException {
        if(this.getObjectClassName(object) != null) {
            this.removeNonPersistentAttributes(object);
        }
    }

    // --------------------------------------------------------------------------
    /**
     * remove the attributes which are in the model as derived
     */
    @SuppressWarnings("unchecked")
    protected void removeNonPersistentAttributes(
        MappedRecord object
    ) throws ServiceException {
        ObjectHolder_2Facade facade = null;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
        String objectClassName = this.getObjectClassName(object);
        ModelElement_1_0 objClass = 
            objectClassName == null ? null :
            getModel().getDereferencedType(objectClassName);
        if (object != null) {
            Map<?,?> modelAttributes = objClass == null ? null : (Map<?,?>)objClass.objGetValue("attribute");
            String attributeName = null;
            for (
                Iterator<String> i = facade.getValue().keySet().iterator(); 
                i.hasNext();
            ) {
                attributeName = i.next();
                // remove derived attributes except the attributes listed below 
                // NOTE: This is a hack and must be fixed as soon as each layer can have 
                // its own model, i.e. persistence layer uses persistence model, application 
                // layer uses application model, etc.
                if (
                    facade.getAttributeValues(attributeName) != null
                    && !attributeName.equals(SystemAttributes.MODIFIED_AT)
                    && !attributeName.equals(SystemAttributes.MODIFIED_BY)
                    && !attributeName.equals(SystemAttributes.CREATED_AT)
                    && !attributeName.equals(SystemAttributes.CREATED_BY)
                    && !attributeName.equals(SystemAttributes.REMOVED_AT)
                    && !attributeName.equals(SystemAttributes.REMOVED_BY)
                    && !attributeName.equals(SystemAttributes.OBJECT_CLASS)
                 ) {
                    if(modelAttributes != null) {
                        if (!SystemAttributes.OBJECT_CLASS.equals(attributeName)) {
                            ModelElement_1_0 attributeDef = (ModelElement_1_0)modelAttributes.get(attributeName);
                            // non-modeled attributes are not removed. 
                            // NOTE: This is a hack and must be fixed as soon as each layer can have 
                            // its own model, i.e. persistence layer uses persistence model, application 
                            // layer uses application model, etc.
                            if (attributeDef == null) {
                                if(attributeName.equals(SystemAttributes.OBJECT_INSTANCE_OF)) {
                                    i.remove();
                                }
                            }
                            // remove derived attributes
                            else if(
                                (attributeDef.objGetList("isDerived") != null) && 
                                ((Boolean)attributeDef.objGetValue("isDerived")).booleanValue()
                            ) {
                                i.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Set object_instanceOf to the object
     */
    private void setInstanceOf(
        MappedRecord object
    ) throws ServiceException {
        Set<String> classes = this.getInstanceOf(object);
        if(classes != null) {
            ObjectHolder_2Facade facade = null;
            try {
                facade = ObjectHolder_2Facade.newInstance(object);
            } catch (ResourceException exception) {}
            facade.clearAttributeValues(SystemAttributes.OBJECT_INSTANCE_OF).addAll(
                classes
            );
        }
    }

    // --------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.SystemAttributes_1#instanceOfBasicObject(org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject)
     */
    protected boolean isInstanceOfBasicObject(
        MappedRecord object
    ) throws ServiceException {
        Set<String> classes = this.getInstanceOf(object);
        return classes != null ?
            classes.contains("org:openmdx:base:BasicObject") :
            super.isInstanceOfBasicObject(object);
    }

    // --------------------------------------------------------------------------
    /**
     * Evaluate an object's superclasses.
     * 
     * @param object
     * 
     * @return the object's class and superclasses.
     * 
     * @throws ServiceException 
     */
    protected Set<String> getInstanceOf(
        MappedRecord object
    ) throws ServiceException{
        String objectClassName = this.getObjectClassName(object);
        //
        // objectClassName may be null if for the attribute selector said
        // no_attributes, just don't set instanceOf.
        //
        if(objectClassName == null) return null;
        ModelElement_1_0 objClass = getModel().getDereferencedType(objectClassName);
        if(objClass == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "class not found",
                new BasicException.Parameter("object", object),
                new BasicException.Parameter("class", objClass)
            );
        }
        // evaluate the set of classes
        Set<String> classes = new HashSet<String>();
        for (
            Iterator<?> i = objClass.objGetList("allSupertype").iterator();
            i.hasNext();
        ) {
            classes.add(((Path)i.next()).getBase());
        }
        return classes;
    }

    // --------------------------------------------------------------------------
//    /** 
//     * Convert attribute values of type String to Path, if the model demands 
//     * a Path.
//     * <p>
//     * All entries in the reference HashMap are converted.
//     * <p>
//     * <i><u>CR20006520</u><br>
//     * Remove & Touch no longer works since some non-derived
//     * attributes might not be in the default fetch set.</i>
//     * 
//     * @param object object to treat
//     */
//    @SuppressWarnings("unchecked")
//    private void convertPaths(
//        MappedRecord object
//    ) throws ServiceException {
//        ObjectHolder_2Facade facade = null;
//        try {
//            facade = ObjectHolder_2Facade.newInstance(object);
//        } 
//        catch (ResourceException e) {
//            throw new ServiceException(e);
//        }
//        ModelElement_1_0 baseObjClass = this.getObjectClass(object);
//        if(
//            baseObjClass != null &&
//            this.getModel().isClassType(baseObjClass)
//        ) {      
//            ModelElement_1_0 objectClass = null;      
//            for(String attributeName: (Set<String>)facade.getDelegate().keySet()){
//                if(!facade.attributeValues(attributeName).isEmpty()) {
//                    objectClass = baseObjClass;
//                    if(
//                        !objectClass.objGetList("reference").isEmpty() &&
//                        ((Map<?,?>) objectClass.objGetValue("reference")).containsKey(attributeName) &&
//                        (facade.getAttributeValues(attributeName) != null)
//                    ) {
//                        // it must be a path, convert all values
//                        for(
//                            ListIterator<Object> v = facade.getAttributeValues(attributeName).populationIterator();
//                            v.hasNext(); 
//                        ) {
//                            Object value = v.next();
//                            if(value instanceof String) {
//                                v.set(new Path((String) value));
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    // --------------------------------------------------------------------------
//    private boolean isOfTypePath(
//        String _attributeName, 
//        MappedRecord object
//    ) throws ServiceException {
//        String attributeName = _attributeName;
//        boolean doConvert = false;
//        String typeName = ObjectHolder_2Facade.getObjectClass(object);
//        // get unqualified attribute name
//        attributeName = attributeName.substring(
//            attributeName.lastIndexOf(":") + 1
//        );
//        attributeName = attributeName.substring(
//            attributeName.lastIndexOf("$") + 1
//        );
//        if(typeName == null) {
//            throw new ServiceException(
//                BasicException.Code.DEFAULT_DOMAIN,
//                BasicException.Code.ASSERTION_FAILURE,
//                SystemAttributes.OBJECT_CLASS + " must not be null",
//                new BasicException.Parameter("object", object),
//                new BasicException.Parameter("attribute", attributeName)
//            );
//        }
//        ModelElement_1_0 type = getModel().getDereferencedType(typeName);
//        if (type != null) {
//            Boolean nonPathType = this.attributeIsInstanceOf(
//                (Map<?,?>)type.objGetValue("attribute"),
//                attributeName,
//                NON_PATH_TYPES
//            );
//            if(nonPathType == null) {
//                if("object_stateId".equals(attributeName)) {
//                    doConvert = true;
//                } 
//                else {
//                    throw new ServiceException(
//                        BasicException.Code.DEFAULT_DOMAIN,
//                        BasicException.Code.ASSERTION_FAILURE,
//                        "Unknown attribute for object class.",
//                        new BasicException.Parameter("attribute", attributeName),
//                        new BasicException.Parameter("object_class", typeName)
//                    );
//                }            
//            } 
//            else {
//                if(nonPathType.booleanValue()) doConvert = true;
//            }
//        }
//        return doConvert;   
//    }        

    // --------------------------------------------------------------------------
    protected Boolean attributeIsInstanceOf(
        Map<?,?> attributeDefs,
        String attributeName,
        Collection<?> candidates
    ) throws ServiceException {
        ModelElement_1_0 attributeDef = (ModelElement_1_0) attributeDefs.get(attributeName);
        return attributeDef == null ? null : Boolean.valueOf(
            candidates.contains(
                getModel().getElementType(
                    attributeDef
                ).objGetValue("qualifiedName")
            )
        );
    }

    // --------------------------------------------------------------------------
    protected boolean attributeMightBeInstanceOfAnXMLDatatype(
        Map<?,?> attributeDefs,
        String attributeName
    ) throws ServiceException {
        Boolean xmlDatatype = attributeIsInstanceOf(
            attributeDefs,
            attributeName,
            XML_DATATYPE_TYPES
        );
        return xmlDatatype == null ? (
                SystemAttributes.CREATED_AT.equals(attributeName) ||
                SystemAttributes.MODIFIED_AT.equals(attributeName)
        ) : xmlDatatype.booleanValue();
    }

    // --------------------------------------------------------------------------

    /**
     * Set the derived attribute 'identity' in case the class supports this feature,
     * e.g. ch:omex:generic:BasicObject
     * 
     * @param request
     * @param object
     */
    protected void setIdentity(
        DataproviderRequest request, 
        MappedRecord object
    ) throws ServiceException {
        ObjectHolder_2Facade facade = null;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        if(facade.getObjectClass() != null) {
            if(
                this.getModel().isSubtypeOf(facade.getObjectClass(), "org:openmdx:base:ExtentCapable") &&
                (!facade.getValue().keySet().contains(SystemAttributes.OBJECT_IDENTITY))                      
            ) {
                facade.clearAttributeValues(SystemAttributes.OBJECT_IDENTITY).add(
                    facade.getPath().toXRI()
                );
            }
        }      
    }

    // --------------------------------------------------------------------------
    /**
     * Touches all object features with object.values(featureName) for all 
     * non-derived features. This way the feature is added to the set
     * of attributes of object. This allows to minimize roundtrips.
     * @param touchNonDerivedFeatures 
     * @param features 
     */
    private void adjustEmptyFeatureSet(
        MappedRecord object,
        boolean touchNonDerivedFeatures, 
        Set<String> features
    ) throws ServiceException {
        ObjectHolder_2Facade facade = null;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        String objectClass = facade.getObjectClass();
        ModelElement_1_0 classDef = null;
        try {
            ModelElement_1_0 elementDef = getModel().getElement(
                objectClass
            ); 
            classDef = ModelAttributes.STRUCTURE_TYPE.equals(elementDef.objGetClass()) ? null : elementDef;
        } 
        catch(ServiceException e){
            classDef = null;
        }
        if(classDef == null) {
            return;
        }
        for(
            Iterator<?> i = ((Map<?,?>)classDef.objGetValue("allFeature")).values().iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 featureDef = (ModelElement_1_0)i.next();
            boolean touch;
            boolean attribute;
            if(this.getModel().isAttributeType(featureDef)) {
                attribute = true;
                touch = touchNonDerivedFeatures && !((Boolean)featureDef.objGetValue("isDerived")).booleanValue();
            } 
            else if (
                this.getModel().isReferenceType(featureDef) && 
                this.getModel().referenceIsStoredAsAttribute(featureDef)
            ) {
                attribute = true;
                touch = touchNonDerivedFeatures && !this.getModel().referenceIsDerived(featureDef); 
            } 
            else {
                attribute = false;
                touch = false;
            }
            if(attribute) {
                String feature = (String)featureDef.objGetValue("name");
                if(touch) {
                    facade.attributeValues(feature);
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void adjustEmptyFeatureSet(
        MappedRecord object, 
        boolean touchNonDerivedFeatures
    ) throws ServiceException {
        ObjectHolder_2Facade facade = null;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        Set<String> features = new HashSet<String>(facade.getValue().keySet());
        for(String attributeName : features) {
            if(attributeName.lastIndexOf(':') > 0 || attributeName.indexOf('$') > 0) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "Scoped features not supported",
                    new BasicException.Parameter("object", object),
                    new BasicException.Parameter("attribute", attributeName)
                );
            } 
        }
        this.adjustEmptyFeatureSet(
            object,
            touchNonDerivedFeatures, 
            features
        );
    }

    //--------------------------------------------------------------------------
    /**
     * Converting<ul>
     * <li><code>javax.xml.XMLGregorianCalendar</code>
     * <li><code>javax.xml.Duration</code>
     * </ul>values to <code>String</code> values.
     * 
     * @param object the <code>DataproviderObject</code> to be converted 
     * 
     * @throws ServiceException 
     */
    @SuppressWarnings("unchecked")
    private void convertXMLDatatypeValues(
        MappedRecord object
    ) throws ServiceException{
        ObjectHolder_2Facade facade = null;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
        for(String attributeName: (Set<String>)facade.getValue().keySet()) {
            for(
                ListIterator<Object> j = facade.attributeValues(attributeName).populationIterator();
                j.hasNext();
            ) {
                Object value = j.next();
                if(
                    value instanceof Duration || 
                    value instanceof XMLGregorianCalendar
                ) {
                    j.set(this.datatypeFormat.unmarshal(value));
                } else {
                    break;
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected void removeContexts(
        MappedRecord object
    ) throws ServiceException {
        ObjectHolder_2Facade facade;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        for(Iterator<String> i = facade.getValue().keySet().iterator(); i.hasNext(); ) {
            String attributeName = i.next();
            if(attributeName.startsWith(SystemAttributes.CONTEXT_PREFIX)) {
                i.remove();
            }
        }
    }
    
    // --------------------------------------------------------------------------
    /**
     * Set known derived features and do some v2 -> v3 compatibility handling.
     * <p>
     * <i><u>CR20006520</u><br>
     * Remove & Touch no longer works since some non-derived
     * attributes might not be in the default fetch set.</i>
     */
    protected void completeObject(
        DataproviderRequest request,
        MappedRecord object
    ) throws ServiceException {
        this.setInstanceOf(object);
        this.setIdentity(request, object);
        this.removeContexts(object);
        this.adjustEmptyFeatureSet(
            object, 
            request.attributeSelector() == AttributeSelectors.ALL_ATTRIBUTES
        );
        this.completeDatatypes(object);
    }

    // --------------------------------------------------------------------------
    protected void completeDatatypes(
        MappedRecord object
    ) throws ServiceException{
        if(this.useDatatypes()) {
            this.convertXMLDatatypeValues(object);
        }
    }
    
    // --------------------------------------------------------------------------
    protected DataproviderReply completeReply(
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        for(MappedRecord object : reply.getObjects()) {
            this.completeObject(
                request,
                object
            );
        }
        return reply;
    }

    // --------------------------------------------------------------------------
    protected DataproviderRequest prepareRequest(
        DataproviderRequest request
    ) throws ServiceException {
        return prepareDatatypes(request);
    }
    
    // --------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected DataproviderRequest prepareDatatypes(
        DataproviderRequest request
    ) throws ServiceException {
        if(
            this.datatypeFormat != null &&
            request.object() != null
        ) {
            MappedRecord object = request.object();
            ObjectHolder_2Facade facade = null;
            try {
                facade = ObjectHolder_2Facade.newInstance(object);
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            String objectType = getObjectClassName(object);
            if(objectType == null) {
                objectType = (String) request.context(
                    DataproviderRequestContexts.OBJECT_TYPE
                ).get(
                    0
                );
            }
            ModelElement_1_0 objectClass = objectType == null ? 
                null : 
                this.getModel().getDereferencedType(objectType);
            Map<?,?> featureDefs = objectClass == null ?
                null :
                (Map<?,?>) objectClass.objGetValue("allFeature");
            if(featureDefs != null) { 
                for(String attributeName: (Set<String>)facade.getValue().keySet()) {
                    if(this.attributeMightBeInstanceOfAnXMLDatatype(featureDefs, attributeName)) {
                        for (
                            ListIterator<Object> j = facade.attributeValues(attributeName).populationIterator();
                            j.hasNext();                        
                        ) {
                            j.set(
                                this.datatypeFormat.marshal(j.next())                            
                            );
                        }
                    }
                }
            }
        }
        if(request.operation() == DataproviderOperations.ITERATION_START) {        
            DataproviderRequest findRequest = request;
            List<FilterProperty> mappedFilterProperties = new ArrayList<FilterProperty>();
            ModelElement_1_0 classDef = null;
            for(FilterProperty requestedFilterProperty : request.attributeFilter()) {
                if(SystemAttributes.OBJECT_INSTANCE_OF.equals(requestedFilterProperty.name())) {
                    //
                    // Add all subtypes to OBJECT_CLASS
                    //
                    if(
                        (requestedFilterProperty.operator() == FilterOperators.IS_IN) &&
                        (requestedFilterProperty.quantor()  == Quantors.THERE_EXISTS) &&
                        (requestedFilterProperty.getValues().length == 1)  
                    ) {
                        classDef = getModel().getDereferencedType(requestedFilterProperty.getValue(0));
                        if(classDef != null) { 
                            FilterProperty mappedFilterProperty = mapInstanceOfFilterProperty(
                                request,
                                classDef
                            );
                            if(mappedFilterProperty != null) {
                                mappedFilterProperties.add(mappedFilterProperty);
                            } 
                        }
                        else {
                            SysLog.info(
                                "Skipping filter property OBJECT_INSTANCE_OF", 
                                requestedFilterProperty.getValue(0)
                            );
                        }
                    } 
                    else {
                        throw new UnsupportedOperationException(
                            getClass().getName() + 
                            " supports 'THERE_EXISTS object_instanceOf IS_IN' clauses with with exactly one class only"
                        );
                    }                                     
                } 
                else {
                    mappedFilterProperties.add(
                        requestedFilterProperty
                    );  
                }
            }
            if(this.datatypeFormat != null) {
                if(classDef == null) {
                    String objectType = (String) request.context(
                        DataproviderRequestContexts.OBJECT_TYPE
                    ).get(0);
                    if(objectType != null) classDef = getModel().getDereferencedType(
                        objectType
                    );
                    if(classDef == null) {
                        classDef = getModel().getTypes(request.path())[2];
                    }
                }
                if(classDef != null) {
                    Map<?,?> featureDefs = (Map<?,?>)classDef.objGetValue("allFeature"); 
                    if (featureDefs != null) {
                        for(ListIterator<FilterProperty> i = mappedFilterProperties.listIterator(); i.hasNext();) {
                            FilterProperty requestedFilterProperty = i.next();
                            if(this.attributeMightBeInstanceOfAnXMLDatatype(featureDefs, requestedFilterProperty.name())) {
                                Object[] requestValues = requestedFilterProperty.getValues();
                                Object[] mappedValues = new Object[requestValues.length];
                                for(int j = 0; j < requestValues.length; j++) {
                                    mappedValues[j] = this.datatypeFormat.marshal(requestValues[j]);
                                }
                                i.set(
                                    new FilterProperty(
                                        requestedFilterProperty.quantor(),
                                        requestedFilterProperty.name(),
                                        requestedFilterProperty.operator(),
                                        mappedValues
                                    )
                                );
                            }
                        }
                    }
                }
            }
            try {
                findRequest = new DataproviderRequest(
                    ObjectHolder_2Facade.newInstance(request.path()).getDelegate(),
                    DataproviderOperations.ITERATION_START,
                    mappedFilterProperties.toArray(
                        new FilterProperty[mappedFilterProperties.size()]
                    ),
                    request.position(),
                    request.size(),
                    request.direction(),
                    request.attributeSelector(),
                    request.attributeSpecifier()
                );
            } 
            catch(ResourceException e) {
                throw new ServiceException(e);
            }
            findRequest.contexts().putAll(
                request.contexts()
            );
            return findRequest;
        } 
        else {
            return request;
        }
    }
    
    /**
     * Map an <code>OBJECT_INSTANCE_OF</code> filter property
     * 
     * @param request the original request
     * @param classDef the class' model element
     * 
     * @return the mapped filter property; 
     * or <code>null</code> if the filter property should be ignored
     * 
     * @throws ServiceException
     */
    protected FilterProperty mapInstanceOfFilterProperty(
        DataproviderRequest request,
        ModelElement_1_0 classDef
    ) throws ServiceException {
        String qualifiedName = (String) classDef.objGetValue("qualifiedName");
        if("org:openmdx:base:BasicObject".equals(qualifiedName)) {
            // Adding the filter property OBJECT_CLASS for BasicObject typically results
            // in a long list of subclasses which is expensive to process for database 
            // systems. Eliminating the BasicObject filter could result in returning objects
            // which are not instance of BasicObject. However this should never happen because
            // BasicObject's and non-BasicObject's must never be mixed in the same database table.
            SysLog.info(
                "Skipping filter property 'object_instanceof'", 
                qualifiedName
            );
            return null;
        } else {
            Set<String> subClasses = new HashSet<String>();
            for(Object path : classDef.objGetList("allSubtype")) {
                subClasses.add(((Path)path).getBase());
            }
            return new FilterProperty(
                Quantors.THERE_EXISTS ,
                SystemAttributes.OBJECT_CLASS,  
                FilterOperators.IS_IN,
                subClasses.toArray()
            );
        }
    };

    /**
     * Verify the digest of an object to be modified
     * 
     * @param header
     * @param request
     * 
     * @throws ServiceException CONCURRENT_ACCESS_FAILURE
     * in case of a digest mismatch.
     */
    protected void verifyDigest(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(this.optimisticLocking){
            MappedRecord afterImage = request.object();
            if(this.isModified(afterImage)){
                MappedRecord beforeImage = this.getBeforeImage(header, request);
                this.propagateDigest(beforeImage);
                ObjectHolder_2Facade beforeImageFacade = null;
                ObjectHolder_2Facade afterImageFacade = null;
                try {
                    beforeImageFacade = ObjectHolder_2Facade.newInstance(beforeImage);
                    afterImageFacade = ObjectHolder_2Facade.newInstance(afterImage);
                } 
                catch (ResourceException e) {
                    throw new ServiceException(e);
                }
                if(!Arrays.equals((byte[])beforeImageFacade.getVersion(), (byte[])afterImageFacade.getVersion())) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN, 
                        BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                        "Digest mismatch",
                        new BasicException.Parameter(
                            "path",
                            request.path()
                        ),
                        new BasicException.Parameter(
                            "beforeImage.version",
                            beforeImageFacade.getVersion()
                        ),
                        new BasicException.Parameter(
                            "afterImage.version",
                            afterImageFacade.getVersion()
                        ),
                        new BasicException.Parameter(
                            "beforeImage",
                            beforeImage
                        ),
                        new BasicException.Parameter(
                            "afterImage",
                            afterImage
                        )
                    );
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected boolean isModified(
        MappedRecord afterImage
    ) throws ServiceException {
        ObjectHolder_2Facade facade = null;
        try {
            facade = ObjectHolder_2Facade.newInstance(afterImage);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        for(String attribute : (Set<String>)facade.getValue().keySet()) {
            if(
                !SystemAttributes.OBJECT_CLASS.equals(attribute) &&
                !SystemAttributes.MODIFIED_AT.equals(attribute) &&
                !SystemAttributes.MODIFIED_BY.equals(attribute) &&
                !attribute.startsWith(SystemAttributes.CONTEXT_PREFIX)
            ){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculates and sets an object's digest
     * 
     * @throws ServiceException 
     */
    private void propagateDigest(
        MappedRecord object
    ) throws ServiceException{
        ObjectHolder_2Facade facade = null;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        if(
            facade.getVersion() != null || // Do not override digest
            !this.isInstanceOfBasicObject(object) // relies on BasicObject's modifiedAt feature
        ) {
            return;
        }
        String objectClassName = this.getObjectClassName(object);
        if(STATE1_STATE_CAPABLE_CLASS.equals(objectClassName)) {
            SparseList<?> objectVersion = facade.getAttributeValues(SystemAttributes.VERSION);
            if(objectVersion != null) {
                Number integerVersion = (Number) objectVersion.get(0);
                if(integerVersion != null) {
                    BigInteger bigintegerVersion = BigInteger.valueOf(integerVersion.longValue());
                    facade.setVersion(
                        bigintegerVersion.toByteArray()
                    );
                }
            }
        } 
        else {
            SparseList<?> modifiedAt = facade.getAttributeValues(SystemAttributes.MODIFIED_AT);
            if(modifiedAt == null) return;
            Object source = modifiedAt.get(0);
            if(source != null) {
                facade.setVersion(
                    UnicodeTransformation.toByteArray(source.toString())                    
                );
            }
        }
    }

    /**
     * Propagate the digest
     * 
     * @param replies
     * @throws ServiceException
     */
    protected void propagateDigest(
        DataproviderReply[] replies        
    ) throws ServiceException {
        if (this.optimisticLocking){
            for(DataproviderReply reply : replies) {
                for(MappedRecord object : reply.getObjects()) {
                    this.propagateDigest(object);
                }
            }
        }
    }
    
    // --------------------------------------------------------------------------
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return this.completeReply(
            request,
            super.get(header,prepareRequest(request))
        );
    }

    // --------------------------------------------------------------------------
    public DataproviderReply find(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        return this.completeReply(
            request,
            super.find(header,prepareRequest(request))
        );
    }

    // --------------------------------------------------------------------------
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.triggeredRemoveDerivedAttributes(request.object());
        return this.completeReply(
            request,
            super.create(header,prepareRequest(request))
        );
    }

    // --------------------------------------------------------------------------
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.triggeredRemoveDerivedAttributes(request.object());
        this.verifyDigest(header,request);        
        return this.completeReply(
            request,
            super.modify(header,prepareRequest(request))
        );
    }    

    //--------------------------------------------------------------------------
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.triggeredRemoveDerivedAttributes(request.object());
        this.verifyDigest(header,request);        
        return this.completeReply(
            request,
            super.replace(header,prepareRequest(request))
        );
    }

    //--------------------------------------------------------------------------
    /**
     * Called before given object is deleted. 
     */
    protected void notifyPreDelete(
        Path objectIdentity
    ) {
        //
    }

    // --------------------------------------------------------------------------
    private void deleteComposites(
        ServiceHeader header,
        MappedRecord _object
    ) throws ServiceException {
        MappedRecord object = _object;
        String objectClass = this.getObjectClassName(object);
        if(objectClass == null) {
            try {
                object = super.get(
                    header,
                    new DataproviderRequest(
                        ObjectHolder_2Facade.newInstance(ObjectHolder_2Facade.getPath(object)).getDelegate(),
                        DataproviderOperations.OBJECT_RETRIEVAL,
                        AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                        null
                    )
                ).getObject();
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            objectClass = this.getObjectClassName(object);
        }
        Map<?,?> references = (Map<?,?>)getModel().getElement(
            objectClass
        ).objGetValue("reference");
        for(Object i : references.values()) {
            ModelElement_1_0 featureDef = (ModelElement_1_0)i;
            ModelElement_1_0 referencedEnd = getModel().getElement(
                featureDef.objGetValue("referencedEnd")
            );
            boolean referenceIsCompositeAndChangeable = 
                getModel().isReferenceType(featureDef) &&
                AggregationKind.COMPOSITE.equals(referencedEnd.objGetValue("aggregation")) &&
                ((Boolean)referencedEnd.objGetValue("isChangeable")).booleanValue();
            if(referenceIsCompositeAndChangeable) {
                String reference = (String)featureDef.objGetValue("name");
                MappedRecord[] composites;
                try {
                    composites = super.find(
                        header,
                        new DataproviderRequest(
                            ObjectHolder_2Facade.newInstance(ObjectHolder_2Facade.getPath(object).getChild(reference)).getDelegate(),
                            DataproviderOperations.ITERATION_START,
                            null,
                            0,
                            Integer.MAX_VALUE,
                            Directions.ASCENDING,
                            AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                            null
                        )
                    ).getObjects();
                } 
                catch (ResourceException e) {
                    throw new ServiceException(e);
                }
                List<Path> compositeIdentities = new ArrayList<Path>();
                // Remove composites of composites
                for(
                    int j = 0;
                    j < composites.length;
                    j++
                ) {
                    MappedRecord composite = composites[j];
                    this.deleteComposites(
                        header,
                        composite
                    );
                    compositeIdentities.add(
                        ObjectHolder_2Facade.getPath(composite)
                    );
                }
                // Remove composites
                for(Path compositeIdentity : compositeIdentities){
                    if(this.notifyPreDelete) {
                        this.notifyPreDelete(
                            compositeIdentity
                        );
                    }
                    try {
                        super.remove(
                            header,
                            new DataproviderRequest(
                                ObjectHolder_2Facade.newInstance(compositeIdentity).getDelegate(),
                                DataproviderOperations.OBJECT_REMOVAL,
                                AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                                null
                            )
                        );
                    } 
                    catch (ResourceException e) {
                        throw new ServiceException(e);
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(this.notifyPreDelete) {
            this.deleteComposites(
                header,
                request.object()
            );
            this.notifyPreDelete(
                request.path()
            );
        }
        return completeReply(
            request,
            super.remove(
                header,
                this.prepareRequest(request)
            )
        );
    }

    // --------------------------------------------------------------------------
    public DataproviderReply operation(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return completeReply(
            request,
            super.operation(header,prepareRequest(request))
        );
    }

    //--------------------------------------------------------------------------
    protected ModelElement_1_0 getObjectClass(
        MappedRecord object
    ) throws ServiceException {
        String objectClassName = this.getObjectClassName(object);
        return objectClassName == null ?
            null :
            this.getModel().getDereferencedType(objectClassName);
    }

    // --------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.BeforeImageCachingLayer_1#getBeforeImage(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    protected MappedRecord getBeforeImage(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        MappedRecord object = super.getBeforeImage(header, request);
        if(this.useDatatypes()) this.convertXMLDatatypeValues(object);
        return object;
    }

    //--------------------------------------------------------------------------

    /**
     * If true, calls notifyPreDelete() before an object is removed.
     * notifiyPreDelete() is called for the removed object and recursively
     * for each of its composite objects.   
     */
    private boolean notifyPreDelete;

    /**
     * Tells whether the plug-in is active of inactive.
     */
    private boolean optimisticLocking = false;

    /**
     * Not <code>null</code> if <code>String</code> values for<ol>
     * <li><code>org::w3c::date</code>
     * <li><code>org::w3c::dateTime</code>
     * <li><code>org::w3c::duration</code>
     * </ol>should be converted to their corresponding XML datatypes<ol>
     * <li><code>javax.xml.XMLGregorianCalendar</code>
     * <li><code>javax.xml.XMLGregorianCalendar</code>
     * <li><code>javax.xml.Duration</code>
     * </ol>and vice versa.
     * 
     * @see LayerConfigurationEntries#XML_DATATYPES
     */
    private DatatypeFormat datatypeFormat;

//    private final static Collection<String> NON_PATH_TYPES = Arrays.asList(
//        PrimitiveTypes.STRING,
//        PrimitiveTypes.ANYURI
//    );
//
    private final static Collection<String> XML_DATATYPE_TYPES = Arrays.asList(
        PrimitiveTypes.DATE,
        PrimitiveTypes.DATETIME,
        PrimitiveTypes.DURATION
    );

}
