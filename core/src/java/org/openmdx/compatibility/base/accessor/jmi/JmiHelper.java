/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: JmiHelper.java,v 1.12 2008/11/11 15:38:23 wfro Exp $
 * Description: JmiHelper class
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/11 15:38:23 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.compatibility.base.accessor.jmi;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.MarshallingSparseArray;
import org.openmdx.compatibility.base.collection.SparseArray;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;
import org.w3c.cci2.Datatypes;

//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public class JmiHelper {

    //-------------------------------------------------------------------------
    static class ToRefObjectValueMarshaller implements Marshaller {

        ToRefObjectValueMarshaller(
            Map<Path,RefObject> objectCache,
            PersistenceManager pm,
            Model_1_0 model,
            String typeName
        ) throws ServiceException {
            this.objectCache = objectCache;
            this.pm = pm;
            this.model = model;
            this.typeName = typeName;
        }

        public Object marshal(
            Object source
        ) throws ServiceException {
            if(this.model.isClassType(this.typeName)) {
                return source instanceof Path ? (
                        this.objectCache.containsKey(source) 
                        ? this.objectCache.get(source) 
                            : this.pm.getObjectById(source)
                ) : source;
            }
            else if(PrimitiveTypes.DATETIME.equals(this.typeName)) {
                return Datatypes.create(
                    Date.class, 
                    (String)source
                );
            }
            else if(PrimitiveTypes.DATE.equals(this.typeName)) {
                return Datatypes.create(
                    XMLGregorianCalendar.class, 
                    (String)source
                );
            }
            else {
                return source;
            }            
        }

        public Object unmarshal(
            Object source
        ) throws ServiceException {
            throw new UnsupportedOperationException("Unmarshal is not supported");
        }

        private final Map<Path,RefObject> objectCache;    
        private final PersistenceManager pm;
        private final Model_1_0 model;
        private final String typeName;
    }

    //-------------------------------------------------------------------------
    private static boolean areEqual(
        Object v1,
        Object v2
    ) {
        if(v1 == null) return v2 == null;
        if(v2 == null) return v1 == null;
        if(
                (v1 instanceof Comparable) && 
                (v2 instanceof Comparable) &&
                (v1.getClass().equals(v2.getClass()))
        ) {
            return ((Comparable)v1).compareTo(v2) == 0;
        }
        return v1.equals(v2);
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("cast")
    private static boolean setRefObjectValues(
        SparseList sourceValues,
        Marshaller marshaller,
        Object targetValues,
        ModelElement_1_0 featureDef,
        boolean replace,
        boolean removeTrailingEmptyStrings
    ) throws ServiceException {
        boolean modified = false;
        if(targetValues instanceof List) {
            if(!areEqual(targetValues, new MarshallingList(marshaller, (List)sourceValues))) {
                if(replace) {
                    ((List)targetValues).clear();
                }
                for(
                        ListIterator j = sourceValues.listIterator();
                        j.hasNext();
                ) {
                    if(j.nextIndex() < ((List)targetValues).size()) {
                        ((List)targetValues).set(
                            j.nextIndex(),
                            marshaller.marshal(j.next())
                        );
                    }
                    else {
                        ((List)targetValues).add(
                            j.nextIndex(),
                            marshaller.marshal(j.next())
                        );
                    }
                }
                modified = true;
            }
        }
        else if(targetValues instanceof Set) {
            if(!areEqual(targetValues, new MarshallingSet(marshaller, (Collection)sourceValues))) {
                if(replace) {
                    ((Set)targetValues).clear();
                }
                for(
                        Iterator j = sourceValues.iterator();
                        j.hasNext();
                ) {
                    ((Set)targetValues).add(
                        marshaller.marshal(j.next())
                    );
                }
                modified = true;
            }
        }
        else if(targetValues instanceof SparseArray) {
            if(!areEqual(new MarshallingSparseArray(marshaller, (SparseArray)sourceValues), targetValues)) {
                if(replace) {
                    ((SparseArray)targetValues).clear();
                }  
                for(
                        ListIterator j = sourceValues.populationIterator();
                        j.hasNext();
                ) {
                    ((SparseArray)targetValues).set(
                        j.nextIndex(),
                        marshaller.marshal(j.next())
                    );
                }
                modified = true;
            }
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "Unsupported collection type",
                new BasicException.Parameter("collection.type", targetValues.getClass().getName())
            );
        }
        return modified;
    }

    //------------------------------------------------------------------------
    public static boolean toRefObject(
        DataproviderObject_1_0 source,
        RefObject_1_0 target,
        Map<Path,RefObject> objectCache,
        PersistenceManager pm,
        boolean replace,
        boolean removeTrailingEmptyStrings
    ) throws ServiceException {
        boolean modified = false;
        String typeName = (String)source.values(SystemAttributes.OBJECT_CLASS).get(0);
        Model_1_0 model = ((RefPackage_1_0)target.refImmediatePackage()).refModel();
        ModelElement_1_0 classDef = model.getElement(typeName);
        for(String featureName: source.attributeNames()) {
            if(
                    SystemAttributes.OBJECT_CLASS.equals(featureName)
            ) {
                continue;
            }
            SparseList sourceValues = source.values(featureName);
            ModelElement_1_0 featureDef = model.getFeatureDef(
                classDef,
                featureName,
                true
            ); 
            if(featureDef == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_MEMBER_NAME, 
                    "attribute not found in class",
                    new BasicException.Parameter("class", classDef),
                    new BasicException.Parameter("attribute name", featureName)
                );
            }
            ModelElement_1_0 featureType = model.getElementType(
                featureDef
            );
            Marshaller marshaller = new ToRefObjectValueMarshaller(
                objectCache, 
                pm, 
                model,
                (String)featureType.values("qualifiedName").get(0)
            );
            String multiplicity = (String)featureDef.values("multiplicity").get(0);
            if(model.isReferenceType(featureDef)) {
                ModelElement_1_0 referencedEnd = model.getElement(
                    featureDef.values("referencedEnd").get(0)
                );
                if(!referencedEnd.values("qualifierType").isEmpty()) {
                    multiplicity = Multiplicities.LIST;
                }
                // Map aggregation none, multiplicity 0..n, no qualifier to <<set>>
                // in case <<list>> semantic is required it must be modeled as 
                // aggregation none, multiplicity 0..1, numeric qualifier
                else if(Multiplicities.MULTI_VALUE.equals(multiplicity)) {
                    multiplicity = Multiplicities.SET;
                }
            }
            /**
             * Store the attribute value to target according to the attribute's
             * multiplicity
             */     
            // OPTIONAL_VALUE
            if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
                Object targetValue = target.refGetValue(featureName);
                Object sourceValue = !sourceValues.isEmpty() ? marshaller.marshal(sourceValues.get(0)) : null;
                if(!areEqual(targetValue, sourceValue)) {
                    target.refSetValue(
                        featureName,
                        sourceValue
                    );
                    modified = true;
                }
            }
            // SINGLE_VALUE
            else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
                Object targetValue = target.refGetValue(featureName);
                Object sourceValue = marshaller.marshal(sourceValues.get(0));
                if(!areEqual(targetValue, sourceValue)) {
                    target.refSetValue(
                        featureName,
                        marshaller.marshal(sourceValues.get(0))
                    );
                    modified = true;
                }
            }
            // STREAM
            else if(Multiplicities.STREAM.equals(multiplicity)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED, 
                    "Stream value not supported.",
                    new BasicException.Parameter("multiplicity", multiplicity),
                    new BasicException.Parameter("attribute name", featureName)
                );        
            }
            // LIST
            else if(
                    Multiplicities.MULTI_VALUE.equals(multiplicity) || 
                    Multiplicities.LIST.equals(multiplicity)
            ) {
                List targetValues = (List)target.refGetValue(featureName);
                modified |= setRefObjectValues(
                    sourceValues,
                    marshaller,
                    targetValues,
                    featureDef,
                    replace,
                    removeTrailingEmptyStrings
                );
            }
            // SET
            else if(Multiplicities.SET.equals(multiplicity)) {
                Set targetValues = (Set)target.refGetValue(featureName);
                modified |= setRefObjectValues(
                    sourceValues,
                    marshaller,
                    targetValues,
                    featureDef,
                    replace,
                    removeTrailingEmptyStrings
                );
            }
            // SPARSEARRAY
            else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                SortedMap targetValues = (SortedMap)target.refGetValue(featureName);
                modified |= setRefObjectValues(
                    sourceValues,
                    marshaller,
                    targetValues,
                    featureDef,
                    replace,
                    removeTrailingEmptyStrings
                );
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED, 
                    "Unsupported multiplicity. Supported are [0..1|1..1|0..n|list|set|sparsearray]",
                    new BasicException.Parameter("multiplicity", multiplicity),
                    new BasicException.Parameter("attribute name", featureName)
                );        
            }
        }
        return modified;
    }

}

//--- End of File -----------------------------------------------------------
