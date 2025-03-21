/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: CompositeObjectDataBinding 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
 */
package org.openmdx.portal.servlet.databinding;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jmi.reflect.RefObject;
import #if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time #endif.Duration;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.omg.mof.spi.Names;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.DataBinding;
#if CLASSIC_CHRONO_TYPES import org.w3c.spi.ImmutableDatatypeFactory;#endif
import org.w3c.spi2.Datatypes;

/**
 * Allows to set/get features of composite objects.
 */
public class CompositeObjectDataBinding extends DataBinding {

    enum ParameterMode {
        ALL,
        OPTIONAL_ONLY,
        OMIT_OPTIONAL
    }
    
    /**
     * Constructor.
     *
     * @param parameterString
     */
    public CompositeObjectDataBinding(
        String parameterString
    ) {
        String[] parameters = parameterString.split("\\?");
        this.queryString = parameters.length > 0 ?
            parameters[0] :
            "";
        this.zeroAsNull = parameters.length > 1 ?
            "zeroAsNull=true".equals(parameters[1]) :
            Boolean.FALSE;
    }
    
    /**
     * Get query parameters.
     * 
     * @param parameterMode
     * @return
     */
    protected Map<String,String> getQueryParameters(
        ParameterMode parameterMode
    ) {
        Map<String,String> queryParameters = new HashMap<String,String>();
        String[] parameters = this.queryString.split(";");
        for(String parameter: parameters) {
            String[] nv = null;
            boolean include = false;
            if(parameter.startsWith("[") && parameter.endsWith("]")) {
                if(parameterMode != CompositeObjectDataBinding.ParameterMode.OMIT_OPTIONAL) {
                    nv = parameter.substring(1, parameter.length()-1).split("=");
                    include = true;
                }
            }
            else {
                if(parameterMode != CompositeObjectDataBinding.ParameterMode.OPTIONAL_ONLY) {
                    nv = parameter.split("=");
                    include = true;
                }
            }
            if(include) {
                queryParameters.put(
                    nv[0], 
                    nv[1]
                );
            }
        }
        return queryParameters;
    }

    /**
     * Get UUID.
     * 
     * @return
     */
    protected String uuidAsString(
    ) {
        return UUIDConversion.toUID(UUIDs.newUUID());
    }
    
    /**
     * Map string value to typed value.
     * 
     * @param value
     * @return
     */
    protected Object getParameterValue(
        String value
    ) {
        String type = "string";
        if(value.startsWith("(")) {
            type = value.substring(1, value.indexOf(")"));
            value = value.substring(value.indexOf(")") + 1);
        }
        Class<?> valueClass = null;
        if("string".equalsIgnoreCase(type)) {
            valueClass = String.class;
        }
        else if("date".equals(type)) {
            valueClass = Datatypes.DATE_CLASS;
        }
        else if("datetime".equalsIgnoreCase(type)) {
            valueClass = Datatypes.DATE_TIME_CLASS;
        }
        else if("short".equalsIgnoreCase(type)) {
            valueClass = Short.class;
        }
        else if("int".equalsIgnoreCase(type) || "integer".equalsIgnoreCase(type)) {
            valueClass = Integer.class;
        }
        else if("long".equalsIgnoreCase(type)) {
            valueClass = Long.class;
        }
        else if("decimal".equalsIgnoreCase(type)) {
            valueClass = BigDecimal.class;
        }
        else if("duration".equalsIgnoreCase(type)) {
            valueClass = Duration.class;
        }   
        else if("boolean".equalsIgnoreCase(type)) {
            valueClass = Boolean.class;
        }   
        return Datatypes.create(valueClass, value);
    }
    
    /**
     * Map parameter mode to query.
     * 
     * @param refPackage
     * @param parameterMode
     * @param pm
     * @return
     */
    protected Query newQuery(
        ParameterMode parameterMode,
        PersistenceManager pm
    ) {
        Query query = null;
        Map<String,String> queryParameters = this.getQueryParameters(parameterMode);
        String queryType = queryParameters.get("type").replace('.', ':');
        try {
            query = pm.newQuery(
            	Classes.getApplicationClass(
            		Names.toClassName(queryType, Names.JMI1_PACKAGE_SUFFIX)
            	)
            );
        }
        catch(Exception e) {
            SysLog.warning("Unable to create query for ", queryType);
        }
        if(query != null) {
            for(Map.Entry<String,String> queryParameter: queryParameters.entrySet()) {
                Object parameterValue = this.getParameterValue(queryParameter.getValue());
                String predicateName1 = parameterValue instanceof Boolean ?
                    "forAll" + Character.toUpperCase(queryParameter.getKey().charAt(0)) + queryParameter.getKey().substring(1) :
                    "thereExists" + Character.toUpperCase(queryParameter.getKey().charAt(0)) + queryParameter.getKey().substring(1);
                String predicateName2 = queryParameter.getKey();
                if(
                    !"type".equals(queryParameter.getKey()) &&
                    !"id".equals(queryParameter.getKey())
                ) {
                    try {
                        Method predicateMethod = null;
                        try {
                            predicateMethod = query.getClass().getMethod(predicateName1);
                        }
                        catch(NoSuchMethodException e) {
                            predicateMethod = query.getClass().getMethod(predicateName2);
                        }
                        Object predicate = predicateMethod.invoke(query);
                        Class<?> predicateClass = predicateMethod.getReturnType();
                        Method operatorMethod = null;
                        try {
                            operatorMethod = predicateClass.getMethod("equalTo", Object.class);
                        }
                        catch(NoSuchMethodException e) {
                            operatorMethod = predicateClass.getMethod("equalTo", boolean.class);
                        }
                        if(operatorMethod != null) {
                            operatorMethod.invoke(
                                predicate, 
                                parameterValue
                            );
                        }
                    }
                    catch(Exception e) {
                        SysLog.warning("Unknown predicate for query", Arrays.asList(queryType, predicateName1, predicateName2));
                    }                    
                }
            }
        }
        return query;
    }

    /**
     * Find composite.
     * 
     * @param object
     * @param qualifiedFeatureName
     * @param parameterMode
     * @return
     */
    protected RefObject findComposite(
        RefObject object,
        String qualifiedFeatureName,
        ParameterMode parameterMode
    ) {
        String[] referenceNames = this.getReferenceNames(qualifiedFeatureName);
        if(referenceNames != null) {
            RefObject referencedObject = this.getReferencedObject(object, referenceNames);
            if(referencedObject == null) {
                return null;
            }
            Collection<?> candidates = (Collection<?>)referencedObject.refGetValue(referenceNames[referenceNames.length-1]);
            Map<String,String> queryParameters = this.getQueryParameters(ParameterMode.ALL);
            if(queryParameters.keySet().contains("id")) {
                String id = queryParameters.get("id");
                for(Object composite: candidates) {
                    if(composite instanceof RefObject) {
                        if(((RefObject)composite).refMofId().endsWith(id)) {
                            return (RefObject)composite;
                        }
                    }
                }
                return null;
            }
            else {
                Query query = this.newQuery(
                    parameterMode,
                    JDOHelper.getPersistenceManager(object) 
                );
                if(candidates.isEmpty()) {
                    return null;
                }
                query.setCandidates(candidates);            
                List<?> resultSet = (List<?>)query.execute();
                return resultSet.isEmpty() ?
                    null :
                    (RefObject)resultSet.iterator().next();
            }
        }
        return null;
    }

    /**
     * Init object.
     * 
     * @param object
     * @param parameterMode
     */
    protected void initObject(
        RefObject object,
        ParameterMode parameterMode
    ) {
        Map<String,String> parameters = this.getQueryParameters(parameterMode);
        // Initialize composite with query parameters. This asserts that 
        // the newly created object matches the query
        for(Map.Entry<String,String> parameter: parameters.entrySet()) {
            if(
                !"type".equals(parameter.getKey()) &&
                !"id".equals(parameter.getKey())
            ) {
                try {
                    Object value = object.refGetValue(parameter.getKey());
                    if(value instanceof Collection) {
                        @SuppressWarnings("unchecked")
                        Collection<Object> values = (Collection<Object>)value;
                        values.add(
                            this.getParameterValue(parameter.getValue())
                        );
                    } else {
                        object.refSetValue(
                            parameter.getKey(), 
                            this.getParameterValue(parameter.getValue())
                        );
                    }
                } catch(Exception e) {
                    SysLog.warning("Can not set value for parameter", parameter);
                }                   
            }
        }        
    }
    
    /**
     * Create composite object. 
     * @param object
     * @param qualifiedReferenceName
     * @return
     */
    protected RefObject createComposite(
        RefObject object,
        String qualifiedReferenceName
    ) {
        Map<String,String> queryParameters = this.getQueryParameters(ParameterMode.ALL);
        RefObject_1_0 composite = null;
        if(queryParameters.keySet().contains("type")) {
            String typeName = queryParameters.get("type").replace('.', ':');
            composite = (RefObject_1_0)object.refOutermostPackage().refClass(typeName).refCreateInstance(null);
            composite.refInitialize(false, false, false);
            this.initObject(
                composite, 
                ParameterMode.ALL
            );
            String[] referenceNames = this.getReferenceNames(qualifiedReferenceName);
            RefObject referencedObject = this.getReferencedObject(object, referenceNames);
            @SuppressWarnings("unchecked")
			RefContainer<RefObject_1_0> container = (RefContainer<RefObject_1_0>)referencedObject.refGetValue(
                referenceNames[referenceNames.length-1]
            );
            container.refAdd(
                QualifierType.REASSIGNABLE,
                queryParameters.keySet().contains("id") ? 
                    queryParameters.get("id") : 
                    this.uuidAsString(), 
                composite
            );
        }    
        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.DataBinding#getValue(javax.jmi.reflect.RefObject, java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public Object getValue(
        RefObject object, 
        String qualifiedFeatureName,
        ApplicationContext app
    ) {
        try {
            RefObject composite = this.findComposite(
                object, 
                qualifiedFeatureName,
                ParameterMode.ALL
            );
            if(composite == null) {
                composite = this.findComposite(
                    object, 
                    qualifiedFeatureName,
                    ParameterMode.OMIT_OPTIONAL
                );            
            }
            if(composite != null) {
                String attributeName = this.getAttributeName(qualifiedFeatureName);            
                return composite.refGetValue(attributeName);
            }
            else {
                return null;
            }
        }
        catch(Exception e) {
        	SysLog.detail("Unable to get composite object. Can not get value", Arrays.asList(object.refMofId(), qualifiedFeatureName, e.getMessage()));
            return null;
        }
    }

    /**
     * Set new value for attribute. Subclasses can override this default
     * behavior, e.g. delete or disable the composite in case newValue is empty.
     * @param object
     * @param attributeName
     * @param newValue
     */
    protected void updateComposite(
        RefObject object,
        String attributeName,
        Object newValue
    ) {
        object.refSetValue(
            attributeName, 
            newValue
        );        
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.DataBinding#setValue(javax.jmi.reflect.RefObject, java.lang.String, java.lang.Object, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public void setValue(
        RefObject object, 
        String qualifiedFeatureName, 
        Object newValue,
        ApplicationContext app
    ) {
        try {
            RefObject composite = this.findComposite(
                object, 
                qualifiedFeatureName,
                ParameterMode.ALL
            );
            if(composite == null) {
                composite = this.findComposite(
                    object, 
                    qualifiedFeatureName,
                    ParameterMode.OMIT_OPTIONAL
                );
                if(composite != null) {
                    this.initObject(
                        composite,
                        ParameterMode.OPTIONAL_ONLY
                    );
                }
            }
            boolean newValueIsZero = 
                (newValue instanceof Number) && 
                ((Number)newValue).longValue() == 0L;
            if(
                (composite == null) && 
                (newValue != null) && 
                (!newValueIsZero || !this.zeroAsNull) &&
                !((newValue instanceof Collection) && ((Collection<?>)newValue).isEmpty())
            ) {
                try {
                    composite = this.createComposite(
                        object,
                        qualifiedFeatureName
                    );
                } catch(Exception e) {
                	SysLog.warning("Unable to create composite object. Can not set value", Arrays.asList(object.refMofId(), qualifiedFeatureName, e.getMessage()));
                }
            }                
            if(composite != null) {
                String attributeName = this.getAttributeName(qualifiedFeatureName);      
                Object oldValue = composite.refGetValue(attributeName);
                if(oldValue instanceof Collection) {
                    @SuppressWarnings("unchecked")
                    Collection<Object> values = (Collection<Object>)oldValue;
                    values.clear();
                    if(newValue instanceof Collection) {
                        values.addAll((Collection<?>)newValue);
                    }
                    else {
                        values.add(newValue);
                    }
                }
                else {
                    this.updateComposite(
                        composite,
                        attributeName, 
                        newValue
                    );
                }
            }
        }
        catch(Exception e) {
        	SysLog.warning("Unable to get composite object. Can not set value", Arrays.asList(object.refMofId(), qualifiedFeatureName, e.getMessage()));
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected final String queryString;
    protected final boolean zeroAsNull;
    
}
