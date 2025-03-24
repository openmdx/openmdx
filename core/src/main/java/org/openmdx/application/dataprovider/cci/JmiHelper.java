/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: JmiHelper class
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
package org.openmdx.application.dataprovider.cci;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import #if JAVA_8 javax.resource.cci.MappedRecord #else jakarta.resource.cci.MappedRecord #endif;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSparseArray;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.LenientPathComparator;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.w3c.cci2.SparseArray;
#if CLASSIC_CHRONO_TYPES import org.w3c.spi.ImmutableDatatypeFactory;#endif
import org.w3c.spi2.Datatypes;

//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public class JmiHelper {

    /**
     * The default set of ignorable features
     */
    private static final Collection<String> DEFAULT_SET_OF_IGNORABLE_FEATURES = Arrays.asList(
        SystemAttributes.OBJECT_CLASS
    );
    
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
                if(source == null) {
                    return null;
                } else if(source instanceof Path) {
                    if(this.objectCache.containsKey(source)) {
                        Object object = this.objectCache.get(source);
                        return ReducedJDOHelper.getPersistenceManager(object) == this.pm ? object : this.pm.getObjectById(
                            ReducedJDOHelper.getTransactionalObjectId(object)
                        );
                    } else {
                        return this.pm.getObjectById(source);
                    }
                } else {
                    return ReducedJDOHelper.getPersistenceManager(source) == this.pm ? source : this.pm.getObjectById(
                        ReducedJDOHelper.getObjectId(source)
                    );
                }
            } else if(PrimitiveTypes.DATETIME.equals(this.typeName)) {
                return Datatypes.DATE_TIME_CLASS.isInstance(source) ? source : Datatypes.create(
                    Datatypes.DATE_TIME_CLASS,
                    (String)source
                );
            } else if(PrimitiveTypes.DATE.equals(this.typeName)) {                
                return Datatypes.DATE_CLASS.isInstance(source) ? source : Datatypes.create(
                    Datatypes.DATE_CLASS,
                    (String)source
                );
            } else if(PrimitiveTypes.DURATION.equals(this.typeName)) {                
                return Datatypes.DURATION_CLASS.isInstance(source) ? source : Datatypes.create(
                    Datatypes.DURATION_CLASS,
                    (String)source
                );
            } else if(PrimitiveTypes.ANYURI.equals(this.typeName)) {                
                return source instanceof URI ? source : Datatypes.create(
                    URI.class, 
                    (String)source
                );
            } else {
                return source;
            }            
        }

        public Object unmarshal(
            Object source
        ) throws ServiceException {
            throw new UnsupportedOperationException();
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
        return
            v1 == null ? v2 == null :
            v2 == null ? false :
            LenientPathComparator.isComparable(v1) ? LenientPathComparator.getInstance().compare(v1, v2) == 0 :
            v1.equals(v2);
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("rawtypes")
    private static void setRefObjectValues(
        Object sourceValues,
        Marshaller marshaller,
        Object targetValues,
        ModelElement_1_0 featureDef, 
        boolean compareWithBeforeImage
    ) throws ServiceException {
        if(targetValues instanceof List) {
            if(!compareWithBeforeImage || !areEqual(targetValues, new MarshallingList(marshaller, (List)sourceValues))) {
                ((List)targetValues).clear();
                for(
                    ListIterator j = ((List)sourceValues).listIterator();
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
            }
        }
        else if(targetValues instanceof Set) {
            if(!compareWithBeforeImage || !areEqual(targetValues, new MarshallingSet(marshaller, (Collection)sourceValues))) {
                ((Set)targetValues).clear();
                for(
                    Iterator j = ((Collection)sourceValues).iterator();
                    j.hasNext();
                ) {
                    ((Set)targetValues).add(
                        marshaller.marshal(j.next())
                    );
                }
            }
        }
        else if(targetValues instanceof SparseArray) {
            if(!compareWithBeforeImage || !areEqual(new MarshallingSparseArray(marshaller, (SparseArray)sourceValues), targetValues)) {
                ((SparseArray)targetValues).clear();
                for(
                    ListIterator j = ((SparseArray)sourceValues).populationIterator();
                    j.hasNext();
                ) {
                    ((SparseArray)targetValues).put(
                        Integer.valueOf(j.nextIndex()),
                        marshaller.marshal(j.next())
                    );
                }
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
    }

    //------------------------------------------------------------------------
    public static void toRefObject(
        MappedRecord source,
        RefObject target,
        Map<Path,RefObject> objectCache,
        Collection<String> ignorableFeatures, 
        boolean compareWithBeforeImage
    ) throws ServiceException {
        PersistenceManager pm = ReducedJDOHelper.getPersistenceManager(target);
        String typeName = Object_2Facade.getObjectClass(source);
        Object_2Facade facade = Facades.asObject(source);
        Model_1_0 model = ((RefPackage_1_0)target.refImmediatePackage()).refModel();
        ModelElement_1_0 classDef = model.getElement(typeName);
        Features: for(String featureName: (Set<String>)facade.getValue().keySet()) {
            if((ignorableFeatures == null ? DEFAULT_SET_OF_IGNORABLE_FEATURES : ignorableFeatures).contains(featureName)) {
                continue Features;
            }
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
            if(Boolean.TRUE.equals(featureDef.isDerived())) {
                continue Features;
            }
            ModelElement_1_0 featureType = model.getElementType(
                featureDef
            );
            Marshaller marshaller = new ToRefObjectValueMarshaller(
                objectCache, 
                pm, 
                model,
                featureType.getQualifiedName()
            );
            //
            // Store the attribute value to target according to the attribute's multiplicity
            // 
            Multiplicity multiplicity = ModelHelper.getMultiplicity(featureDef);
			switch(multiplicity){
	            case OPTIONAL: {
	                Object sourceValue = facade.attributeValue(featureName);                
	                if(compareWithBeforeImage){
	                    Object targetValue = target.refGetValue(featureName);
	                    if(!areEqual(targetValue, sourceValue)) {
	                        target.refSetValue(
	                            featureName,
	                            marshaller.marshal(sourceValue)
	                        );
	                    }
	                } else {
	                    target.refSetValue(
	                        featureName,
	                        marshaller.marshal(sourceValue)
	                    );
	                }
	            } break;
	            case SINGLE_VALUE: {
	                Object sourceValue = facade.attributeValue(featureName);                
	                //
	                // before image should not be retrieved in case of primitive types!
	                //
	                target.refSetValue(
	                    featureName,
	                    marshaller.marshal(sourceValue)
	                );
	            } break;
	            case LIST: case SET: case SPARSEARRAY: {
	                Object sourceValues = facade.attributeValues(featureName);                
	                setRefObjectValues(
	                    sourceValues,
	                    marshaller,
	                    target.refGetValue(featureName),
	                    featureDef, 
	                    compareWithBeforeImage
	                );
	            } break;
				default:
	                throw new ServiceException(
	                    BasicException.Code.DEFAULT_DOMAIN,
	                    BasicException.Code.NOT_SUPPORTED, 
	                    "Unsupported multiplicity. Supported are 0..1, 1..1, list, set, sparsearray and in case of references 0..n",
	                    new BasicException.Parameter("multiplicity", multiplicity),
	                    new BasicException.Parameter("attribute name", featureName)
	                );        
            }
        }
    }

}

//--- End of File -----------------------------------------------------------
