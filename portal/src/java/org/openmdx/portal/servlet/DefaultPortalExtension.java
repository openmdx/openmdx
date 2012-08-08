/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: DefaultPortalExtension.java,v 1.96 2010/08/05 09:34:37 wfro Exp $
 * Description: DefaultEvaluator
 * Revision:    $Revision: 1.96 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/05 09:34:37 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 */
package org.openmdx.portal.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefStruct;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.ConditionType;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.BinaryValue;
import org.openmdx.portal.servlet.attribute.BooleanValue;
import org.openmdx.portal.servlet.attribute.CodeValue;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.attribute.NumberValue;
import org.openmdx.portal.servlet.attribute.ObjectReferenceValue;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.control.Control;
import org.openmdx.portal.servlet.control.GridControl;
import org.openmdx.portal.servlet.databinding.CompositeObjectDataBinding;
import org.openmdx.portal.servlet.databinding.ReferencedObjectDataBinding;
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.ui1.jmi1.FeatureDefinition;
import org.openmdx.ui1.jmi1.StructuralFeatureDefinition;

public class DefaultPortalExtension
  implements PortalExtension_1_0, Serializable {
  
    //-------------------------------------------------------------------------
    protected String toS(
        Object obj
    ) {
        return obj == null
          ? ""
          : (obj instanceof Collection) && ((Collection)obj).size() > 0
            ? ((Collection)obj).iterator().next().toString()
            : obj.toString();
    }
    
    //-------------------------------------------------------------------------
    protected String toNbspS(
        Object obj
    ) {
        return this.toS(obj).replace(" ", "&nbsp;");
    }

    
	//-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getTitle(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, short, java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    public String getTitle(
        RefObject_1_0 refObj,
        short locale,
        String localeAsString,
        ApplicationContext application
    ) {
    	return this.getTitle(
    		refObj, 
    		locale, 
    		localeAsString,
    		false, // asShortTitle = false
    		application
    	);
    }

	//-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getTitle(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, short, java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    public String getTitle(
        RefObject_1_0 refObj, 
        short locale,
        String localeAsString,
        boolean asShortTitle,
        ApplicationContext application
    ) {
      if(refObj == null) {
        return "#NULL";
      }
      if(JDOHelper.isNew(refObj) || !JDOHelper.isPersistent(refObj)) {
        return "Untitled";
      }
      Path p = refObj.refGetPath();
      Model_1_0 model = ((RefPackage_1_0)refObj.refOutermostPackage()).refModel();
      String objectClass = refObj.refClass().refMofId();
      
      try {
          ModelElement_1_0 classDef = model.getElement(objectClass);
          Map attributeDefs = model.getAttributeDefs(classDef, false, true);
          if(
            attributeDefs.keySet().contains("fullName") &&
            (refObj.refGetValue("fullName") != null)
          ) {
            return this.toS(refObj.refGetValue("fullName"));
          }
          else if(
            attributeDefs.keySet().contains("title") &&
            (refObj.refGetValue("title") != null)
          ) {
            return this.toS(refObj.refGetValue("title"));
          }
          else if(
            attributeDefs.keySet().contains("name") &&
            (refObj.refGetValue("name") != null) 
          ) {
            return this.toS(refObj.refGetValue("name"));
          }
          else if(
            attributeDefs.keySet().contains("description") &&
            (refObj.refGetValue("description") != null)
          ) {
            return this.toS(refObj.refGetValue("description"));
          }
          else {
            return p.getBase();
          }    
      }
      catch(ServiceException e) {
          e.log();
          SysLog.warning("can not evaluate. object", refObj.refMofId());
          return "#ERR (" + e.getMessage() + ")";
      }
    }
  
    //-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#isEnabled(java.lang.String, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, org.openmdx.portal.servlet.ApplicationContext)
     */
    public boolean isEnabled(
        String elementName, 
        RefObject_1_0 refObj,
        ApplicationContext applicationContext
    ) {
        return elementName != null;
    }
  
    //-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#isEnabled(org.openmdx.portal.servlet.control.Control, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, org.openmdx.portal.servlet.ApplicationContext)
     */
    public boolean isEnabled(
        Control control, 
        RefObject_1_0 refObj,
        ApplicationContext applicationContext
    ) {
        return control != null;
    }
    //-------------------------------------------------------------------------     
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#isEnabled(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, org.openmdx.portal.servlet.ApplicationContext)
     */
    public boolean isEnabled(
        RefObject_1_0 refObj,
        ApplicationContext applicationContext
    ) {
        return refObj != null;
    }
    
    //-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getIdentityQueryFilterClause(java.lang.String)
     */
    public org.openmdx.base.query.Filter getQuery(        
    	org.openmdx.ui1.jmi1.ValuedField field,
    	String filterValue,
    	int queryFilterStringParamCount,
    	ApplicationContext application
    ) {
        return null;
    }
    
    //-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getGridPageSize(java.lang.String)
     */
    public int getGridPageSize(
        String referencedTypeName
    ) {
        // default page size
        return 15;
    }
    
    //-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#hasGridColours(java.lang.String)
     */
    public boolean hasGridColours(
        String referencedTypeName
    ) {
        return false;
    }
    
    //-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#isLookupType(org.openmdx.model1.accessor.basic.cci.ModelElement_1_0)
     */
    public boolean isLookupType(
        ModelElement_1_0 classDef
    ) throws ServiceException {
        String qualifiedName = (String)classDef.objGetValue("qualifiedName");
        return 
            !"org:openmdx:base:BasicObject".equals(qualifiedName) &&
            !"org:openmdx:base:ContextCapable".equals(qualifiedName);
    }
    
    //-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getAutocompleter(org.openmdx.portal.servlet.ApplicationContext, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, java.lang.String)
     */
    public Autocompleter_1_0 getAutocompleter(
        ApplicationContext application,
        RefObject_1_0 context,
        String qualifiedFeatureName
    ) {
        try {
            Model_1_0 model = application.getModel();
            ModelElement_1_0 lookupType = null;
            // Get lookup type from model
            try {
                ModelElement_1_0 lookupFeature = model.getElement(qualifiedFeatureName);
                lookupType = model.getElement(lookupFeature.objGetValue("type"));
            }
            catch(Exception e) {
                try {
                    // Fallback to customized feature definitions
                    FeatureDefinition lookupFeature = application.getFeatureDefinition(qualifiedFeatureName);
                    if(lookupFeature instanceof StructuralFeatureDefinition) {
                        lookupType = model.getElement(((StructuralFeatureDefinition)lookupFeature).getType());
                    }
                }
                catch(Exception e0) {}
            }
            if(
                (lookupType != null) && 
                model.isClassType(lookupType) &&
                this.isLookupType(lookupType)
            ) {
                RefObject lookupObject = this.getLookupObject(
                    lookupType, 
                    context, 
                    application
                );
                Path lookupObjectIdentity = new Path(lookupObject.refMofId());
                Map<Integer,String> filterByFeatures = new TreeMap<Integer,String>();
                Map<Integer,String> filterByLabels = new TreeMap<Integer,String>();
                Map<Integer,String> lookupReferenceNames = new TreeMap<Integer,String>();
                ModelElement_1_0 lookupObjectClass = ((RefMetaObject_1)lookupObject.refMetaObject()).getElementDef();
                Map lookupObjectFeatures = model.getStructuralFeatureDefs(lookupObjectClass, true, false, false);
                ModelElement_1_0 extentCapableClass = model.getElement("org:openmdx:base:ExtentCapable");
                ModelElement_1_0 contextCapableClass = model.getElement("org:openmdx:base:ContextCapable");
                ModelElement_1_0 basicObjectClass = model.getElement("org:openmdx:base:BasicObject");
                // Find composite reference of lookup object which references objects of type lookup type
                int ii = 0;
                for(
                    Iterator i = lookupObjectFeatures.values().iterator(); 
                    i.hasNext();
                    ii++
                ) {
                    ModelElement_1_0 feature = (ModelElement_1_0)i.next();
                    if(model.isReferenceType(feature)) {
                        ModelElement_1_0 referencedEnd = model.getElement(feature.objGetValue("referencedEnd"));
                        ModelElement_1_0 referencedType = model.getElement(feature.objGetValue("type"));
                        List<Object> allReferencedTypes = new ArrayList<Object>();
                        for(Iterator j = referencedType.objGetList("allSubtype").iterator(); j.hasNext(); ) {
                            allReferencedTypes.addAll(
                                model.getElement(j.next()).objGetList("allSupertype")
                            );
                        }
                        if(
                            !referencedType.equals(extentCapableClass) && 
                            !referencedType.equals(contextCapableClass) && 
                            !referencedType.equals(basicObjectClass) &&
                            !AggregationKind.NONE.equals(referencedEnd.objGetValue("aggregation")) &&
                            allReferencedTypes.contains(lookupType.jdoGetObjectId()) 
                        ) {
                            String lookupReferenceName = (String)feature.objGetValue("name");
                            // Get default order by features for context object. Get all attributes
                            // which include the strings name, description, title or number and
                            // the attribute type is PrimitiveTypes.STRING
                            // Find reference of lookup object which references objects of type contextClass
                            Map lookupTypeAttributes = model.getAttributeDefs(lookupType, true, false);
                            for(Iterator k = lookupTypeAttributes.values().iterator(); k.hasNext(); ) {
                                ModelElement_1_0 attributeDef = (ModelElement_1_0)k.next();
                                ModelElement_1_0 attributeType = model.getElement(attributeDef.objGetValue("type"));
                                String attributeName = (String)attributeDef.objGetValue("name");
                                if(
                                    (attributeName.indexOf("name") >= 0 ||
                                    attributeName.indexOf("Name") >= 0 ||
                                    attributeName.indexOf("description") >= 0 ||
                                    attributeName.indexOf("Description") >= 0 ||
                                    attributeName.indexOf("title") >= 0 ||
                                    attributeName.indexOf("Title") >= 0 ||
                                    attributeName.indexOf("address") >= 0 ||
                                    attributeName.indexOf("Address") >= 0 ||
                                    attributeName.indexOf("number") >= 0 ||
                                    attributeName.indexOf("Number") >= 0) &&
                                    PrimitiveTypes.STRING.equals(attributeType.objGetValue("qualifiedName"))                                    
                                ) {
                                    int order = 10000 * (filterByFeatures.size() + 1);
                                    try {
                                        org.openmdx.ui1.jmi1.ElementDefinition field = application.getUiElementDefinition(
                                            (String)attributeDef.objGetValue("qualifiedName")
                                        );
                                        org.openmdx.ui1.jmi1.AssertableInspector referencedTypeInspector =
                                            application.getAssertableInspector((String)referencedType.objGetValue("qualifiedName"));                                        
                                        String referencedTypeLabel =  application.getLabel(
                                            referencedTypeInspector.getForClass()
                                        );
                                        int orderReferencedType = referencedTypeInspector.getOrder().size() > 2
                                            ? referencedTypeInspector.getOrder().get(2)
                                            : referencedTypeInspector.getOrder().size() > 1
                                                ? referencedTypeInspector.getOrder().get(1)
                                                : referencedTypeInspector.getOrder().size() > 0
                                                    ? referencedTypeInspector.getOrder().get(0)
                                                    : 0;                                        
                                        if(field.isActive()) {
                                            int locale = application.getCurrentLocaleAsIndex();
                                            // Order autocompleters by <order referenced type,index,field order>
                                            order =  field.getOrderObjectContainer().size() > 2
                                                ? 1000000*orderReferencedType + 10000*ii + 100*((Number)field.getOrderObjectContainer().get(1)).intValue() + ((Number)field.getOrderObjectContainer().get(2)).intValue()
                                                : field.getOrder().size() > 2
                                                    ? 1000000*orderReferencedType + 10000*ii + 100*((Number)field.getOrder().get(1)).intValue() + ((Number)field.getOrder().get(2)).intValue()
                                                    : 1000000*orderReferencedType + 10000*ii + order;   
                                            String label = locale < field.getLabel().size()
                                                ? field.getLabel().get(locale)
                                                : field.getLabel().size() == 0 ? attributeName : field.getLabel().get(0);
                                            lookupReferenceNames.put(
                                                new Integer(order), 
                                                lookupReferenceName
                                            );
                                            filterByLabels.put(
                                                new Integer(order),
                                                referencedTypeLabel + " / " + label
                                            );
                                            filterByFeatures.put(
                                                new Integer(order),
                                                attributeName
                                            );
                                        }
                                    } 
                                    catch(Exception e) {
                                        lookupReferenceNames.put(
                                            new Integer(order), 
                                            lookupReferenceName
                                        );
                                        filterByLabels.put(
                                            new Integer(order),
                                            attributeName
                                        );                                        
                                        filterByFeatures.put(
                                            new Integer(order),
                                            attributeName
                                        );
                                    }
                                }
                            }                
                        }
                    }
                }                
                if(
                    (lookupObject != null) &&
                    !lookupReferenceNames.isEmpty()
                ) {
                    ConditionType[] filterOperators = new ConditionType[filterByFeatures.size()];
                    for(int i = 0; i < filterByFeatures.size(); i++) {
                        filterOperators[i] = ConditionType.IS_LIKE;
                    }
                    return new FindObjectsAutocompleter(
                        lookupObjectIdentity,
                        (String[])lookupReferenceNames.values().toArray(new String[lookupReferenceNames.size()]),
                        (String)lookupType.objGetValue("qualifiedName"),
                        (String[])filterByFeatures.values().toArray(new String[filterByFeatures.size()]),
                        (String[])filterByLabels.values().toArray(new String[filterByLabels.size()]),
                        filterOperators,
                        (String[])filterByFeatures.values().toArray(new String[filterByFeatures.size()])
                    );
                }
            }        
        }
        catch(Exception e) {
            SysLog.warning("Error getting autocompleter", Arrays.asList(new String[]{context == null ? "N/A" : context.refMofId(), qualifiedFeatureName}));
            new ServiceException(e).log();
        }
        return null;
    }
        
    //-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getFindObjectsBaseFilter(org.openmdx.portal.servlet.ApplicationContext, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, java.lang.String)
     */
    @Override
    public List<Condition> getFindObjectsBaseFilter(
        ApplicationContext application,
        RefObject_1_0 context,
        String qualifiedFeatureName
    ) {
        return new ArrayList<Condition>();
    }
    
    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
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

    //-------------------------------------------------------------------------
    protected Locale getCurrentLocale(
        ApplicationContext application
    ) {
        String locale = application.getCurrentLocaleAsString();
        return new Locale(
            locale.substring(0, 2), 
            locale.substring(locale.indexOf("_") + 1)
        );      
    }
  
    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    static protected Map<String,Object> targetAsValueMap(
    	Object target
    ) {
    	return (Map<String,Object>)target;
    }
    
    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    static protected Collection<Object> valueAsCollection(
    	Object value
    ) {
    	return (Collection<Object>)value;
    }
    
    //-------------------------------------------------------------------------
    protected Object getValue(
    	AttributeValue valueHolder,
    	Object target,
    	String featureName,
    	ApplicationContext app
    ) {
    	if(valueHolder.getDataBinding() instanceof DataBinding_1_0) {
	        return ((DataBinding_1_0)valueHolder.getDataBinding()).getValue(
	            (RefObject)target, 
	            featureName
	        );
    	}
    	else {
	        return ((DataBinding_2_0)valueHolder.getDataBinding()).getValue(
	            (RefObject)target, 
	            featureName,
	            app
	        );    		
    	}
    }
    
    //-------------------------------------------------------------------------
    protected void setValue(
    	AttributeValue valueHolder,
    	Object target,
    	String featureName,
    	Object value,
    	ApplicationContext app
    ) {
    	if(valueHolder.getDataBinding() instanceof DataBinding_1_0) {
	        ((DataBinding_1_0)valueHolder.getDataBinding()).setValue(
	            (RefObject)target,
	            featureName,
	            value
	        );
    	}
    	else {
	        ((DataBinding_2_0)valueHolder.getDataBinding()).setValue(
	            (RefObject)target,
	            featureName,
	            value,
	            app
	        );    		
    	}
    }
    
    //-------------------------------------------------------------------------
    /**
     * Maps the request input to the specified object. The object must either
     * be instanceof RefObject_1_0 or Map.
     */
    public void updateObject(
        Object target,
        Map<String,Object[]> parameterMap,
        Map<String,Attribute> fieldMap,
        ApplicationContext app
    ) {
    	SysLog.trace("fieldMap", fieldMap);
    	SysLog.trace("parameterMap", parameterMap);
        Model_1_0 model = app.getModel();
        int count = 0;
        // Data bindings require multi-pass update of object
        Map<String,Attribute> updatedFeatures = new HashMap<String,Attribute>();
        while(count < 3) {
            // map object
            Set<String> modifiedFeatures = new HashSet<String>();
            for(
              Iterator i = parameterMap.keySet().iterator(); 
              i.hasNext(); 
            ) {
              Object key = i.next();        
              // field names are of the form 'feature[index][.false | .true]'
              // Suffix .true and .false for boolean fields only. 
              // If .false and .true fields are received ignore .false
              if(
                (key instanceof String) &&
                (((String)key).indexOf("[") >= 0) &&
                (!((String)key).endsWith(".false") || !parameterMap.keySet().contains(((String)key).substring(0, ((String)key).lastIndexOf(".false")) + ".true"))
              ) {        
                // attribute names are of the form <name>[tabIndex]
                // remove tabIndex to get full qualified feature name
                String featureName = ((String)key).substring(0, ((String)key).lastIndexOf("["));
                String featureTypeName = null;
                // Lookup feature in model repository
                try {
                    ModelElement_1_0 featureDef = model.getElement(featureName);
                    featureTypeName = (String)model.getElement(featureDef.objGetValue("type")).objGetValue("qualifiedName");
                }             
                catch(Exception e) {
                    try {
                        // Fallback: lookup feature in ui repository as feature definition
                        FeatureDefinition featureDef = app.getFeatureDefinition(featureName);
                        if(featureDef instanceof StructuralFeatureDefinition) {
                            featureTypeName = ((StructuralFeatureDefinition)featureDef).getType();
                        }
                    }
                    catch(Exception e0) {}                
                }        
                Attribute feature = (Attribute)fieldMap.get(featureName);
                if(feature != null) {        
                  // parse parameter values
                  List parameterValues = Arrays.asList((Object[])parameterMap.get(key));
                  StringTokenizer tokenizer = parameterValues.size() == 0 ? 
                	  new StringTokenizer("", "\n") : 
                	  new StringTokenizer((String)parameterValues.get(0), "\n\r");
                  List<String> newValues = new ArrayList<String>();
                  while(tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    if(!"#NULL".equals(token)) {
                      newValues.add(token);
                    }
                  }        
                  // accept?
                  AttributeValue valueHolder = feature.getValue();
                  boolean accept =
                      (valueHolder != null) &&
                      valueHolder.isChangeable() &&
                      !modifiedFeatures.contains(featureName);
                  SysLog.trace("accept feature", featureName + "=" + accept);
                  SysLog.trace("new values", newValues);        
                  if(accept) {
                	updatedFeatures.put(
                		featureName, 
                		feature
                	);
                    // text
                    if(valueHolder instanceof TextValue) {
                      SysLog.trace("Text value " + feature.getLabel(), Arrays.asList((Object[])parameterMap.get(key)));        
                      // single-valued
                      if(valueHolder.isSingleValued()) {
                        // cat all values into one string
                        String multiLineString = parameterValues.size() == 0 ?
                            "" :
                            (String)parameterValues.get(0);
                        String mappedNewValue = multiLineString.length() == 0 ? null : multiLineString;
                        if(target instanceof RefObject) {
                          boolean isModified = !DefaultPortalExtension.areEqual(
                        	  this.getValue(
                        		  valueHolder, 
                        		  target, 
                        		  featureName, 
                        		  app
                        	  ),
                              mappedNewValue
                          );
                          SysLog.trace("modify feature", featureName + "=" + isModified);
                          if(isModified) {
                        	  this.setValue(
                        		  valueHolder, 
                        		  target, 
                        		  featureName, 
                        		  mappedNewValue, 
                        		  app
                        	  );
                        	  modifiedFeatures.add(featureName);
                          }
                        }
                        else {
                            targetAsValueMap(target).put(
                                featureName,
                                mappedNewValue
                            );
                        }
                      }        
                      // multi-valued
                      else {
                        Collection<Object> values = null;
                        if(target instanceof RefObject) {
                            values = valueAsCollection(
                            	this.getValue(
                            		valueHolder, 
                            		target, 
                            		featureName, 
                            		app
                            	)
                            );
                        }
                        else {
                            values = valueAsCollection(targetAsValueMap(target).get(featureName));
                            if(values == null) {
                                targetAsValueMap(target).put(
                                  featureName,
                                  values = new ArrayList<Object>()
                                );
                            }
                        }
                        List<String> mappedNewValues = newValues;
                        boolean isModified = !DefaultPortalExtension.areEqual(
                            values,
                            mappedNewValues
                        );
                        SysLog.trace("modify feature", featureName + "=" + isModified);
                        if(isModified) {
                            if(target instanceof RefObject) {
                            	this.setValue(
                            		valueHolder, 
                            		target, 
                            		featureName, 
                            		mappedNewValues, 
                            		app
                            	);
                            }
                            else {
                                values.clear();
                                values.addAll(mappedNewValues);
                            }
                            modifiedFeatures.add(featureName);
                        }
                      }
                    }
        
                    // number
                    else if(valueHolder instanceof NumberValue) {
                      SysLog.trace("Number value " + feature.getLabel(), Arrays.asList((Object[])parameterMap.get(key)));
        
                      // single-valued
                      if(valueHolder.isSingleValued()) {
                        try {    
                          BigDecimal number = app.parseNumber(
                              newValues.size() == 0 ? "" : ((String)newValues.get(0)).trim()
                          );
                          if(number == null) {
                              number = valueHolder.isOptionalValued() ? 
                            	  null : 
                            	  new BigDecimal(0);
                          }
                          if(number == null) {
                              Object mappedNewValue = null;
                              if(target instanceof RefObject) {
                                boolean isModified = !DefaultPortalExtension.areEqual(
                                	this.getValue(
                                		valueHolder, 
                                		target, 
                                		featureName, 
                                		app
                                	),
                                    mappedNewValue
                                );
                                SysLog.trace("modify feature", featureName + "=" + isModified);
                                if(isModified) {
                                	this.setValue(
                                		valueHolder, 
                                		target, 
                                		featureName, 
                                		mappedNewValue, 
                                		app
                                	);
                                    modifiedFeatures.add(featureName);
                                }
                              }
                              else {
                                  targetAsValueMap(target).put(
                                      featureName,
                                      mappedNewValue
                                  );
                              }
                          }
                          else if(PrimitiveTypes.INTEGER.equals(featureTypeName)) {
                              Integer mappedNewValue = new Integer(number.intValue());
                              if(target instanceof RefObject) {
                                boolean isModified = !DefaultPortalExtension.areEqual(
                                	this.getValue(
                                		valueHolder, 
                                		target, 
                                		featureName, 
                                		app
                                	),
                                    mappedNewValue
                                );
                                SysLog.trace("modify feature", featureName + "=" + isModified);
                                if(isModified) {
                                	this.setValue(
                                		valueHolder, 
                                		target, 
                                		featureName, 
                                		mappedNewValue, 
                                		app
                                	);
                                    modifiedFeatures.add(featureName);
                                }
                              }
                              else {
                                  targetAsValueMap(target).put(
                                      featureName,
                                      mappedNewValue
                                  );
                              }
                          }
                          else if(PrimitiveTypes.LONG.equals(featureTypeName)) {
                              Long mappedNewValue = new Long(number.longValue());
                              if(target instanceof RefObject) {
                                  boolean isModified = !DefaultPortalExtension.areEqual(
                                	  this.getValue(
                                		  valueHolder, 
                                		  target, 
                                		  featureName, 
                                		  app
                                	  ),
                                      mappedNewValue
                                  );
                                  SysLog.trace("modify feature", featureName + "=" + isModified);
                                  if(isModified) {
                                	  this.setValue(
                                		  valueHolder, 
                                		  target, 
                                		  featureName, 
                                		  mappedNewValue, 
                                		  app
                                	  );
                                      modifiedFeatures.add(featureName);
                                  }
                              }
                              else {
                                  targetAsValueMap(target).put(
                                      featureName,
                                      mappedNewValue
                                  );
                              }
                          }
                          else if(PrimitiveTypes.DECIMAL.equals(featureTypeName)) {
                              BigDecimal mappedNewValue = number;
                              if(target instanceof RefObject) {
                                boolean isModified = !DefaultPortalExtension.areEqual(
                                	this.getValue(
                                		valueHolder, 
                                		target, 
                                		featureName, 
                                		app
                                	),
                                    mappedNewValue
                                );
                                if(isModified) {
                              	  	this.setValue(
                              	  		valueHolder, 
                              	  		target, 
                              	  		featureName, 
                              	  		mappedNewValue, 
                              	  		app
                              	  	);
                                    modifiedFeatures.add(featureName);
                                }
                              }
                              else {
                                  targetAsValueMap(target).put(
                                      featureName,
                                      mappedNewValue
                                  );
                              }
                          }
                          else { //if(PrimitiveTypes.SHORT.equals(featureTypeName)) {
                              Short mappedNewValue = new Short(number.shortValue());
                              if(target instanceof RefObject) {
                            	  boolean isModified = !DefaultPortalExtension.areEqual(
                            		  this.getValue(
	                                	  valueHolder, 
	                                	  target, 
	                                	  featureName, 
	                                	  app
                            		  ),
                                      mappedNewValue
                                  );
                                  SysLog.trace("modify feature", featureName + "=" + isModified);
                                  if(isModified) {
                                	  this.setValue(
                                		  valueHolder, 
                                		  target, 
                                		  featureName, 
                                		  mappedNewValue, 
                                		  app
                                	  );
                                      modifiedFeatures.add(featureName);
                                  }
                              }
                              else {
                                  targetAsValueMap(target).put(
                                      featureName,
                                      mappedNewValue
                                  );
                              }
                          }
                        }
                        catch(NumberFormatException e) {
                          app.addErrorMessage(
                              app.getTexts().getErrorTextCanNotEditNumber(),
                              new String[]{feature.getLabel(), (String)newValues.get(0), "can not parse number"}
                          );
                        }
                        catch(JmiServiceException e) {
                          e.log();
                          app.addErrorMessage(
                        	  app.getTexts().getErrorTextCanNotEditNumber(),
                        	  new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                          );
                        }
                        catch(Exception e) {
                        	new ServiceException(e).log();
                        	app.addErrorMessage(
                        	  app.getTexts().getErrorTextCanNotEditNumber(),
                            	new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                        	);
                        }
                      }
        
                      // multi-valued
                      else {
                        Collection<Object> values = null;
                        if(target instanceof RefObject) {
                            values = valueAsCollection(
                            	this.getValue(
                            		valueHolder, 
                            		target, 
                            		featureName, 
                            		app
                            	)
                            );
                        }
                        else {
                            values = valueAsCollection(targetAsValueMap(target).get(featureName));
                            if(values == null) {
                                targetAsValueMap(target).put(
                                  featureName,
                                  values = new ArrayList<Object>()
                                );
                            }
                        }
                        List<Object> mappedNewValues = new ArrayList<Object>();
                        for(Iterator j = newValues.iterator(); j.hasNext(); ) {
                          try {
                            String numberAsString = ((String)j.next()).trim();
                            BigDecimal number = app.parseNumber(numberAsString);
                            if(number != null) {
                              if(PrimitiveTypes.INTEGER.equals(featureTypeName)) {
                                mappedNewValues.add(
                                  new Integer(number.intValue())
                                );
                              }
                              else if(PrimitiveTypes.LONG.equals(featureTypeName)) {
                                mappedNewValues.add(
                                  new Long(number.longValue())
                                );
                              }
                              else if(PrimitiveTypes.DECIMAL.equals(featureTypeName)) {
                                mappedNewValues.add(
                                  number
                                );
                              }
                              else { // if(PrimitiveTypes.SHORT.equals(featureTypeName)) {
                                  mappedNewValues.add(
                                    new Short(number.shortValue())
                                  );
                              }
                            }
                            else {
                            	app.addErrorMessage(
                            		app.getTexts().getErrorTextCanNotEditNumber(),
                            		new String[]{feature.getLabel(), (String)newValues.get(0), "can not parse number"}
                            	);
                            }
                          }
                          catch(JmiServiceException e) {
                        	  e.log();
                        	  app.addErrorMessage(
                        		  app.getTexts().getErrorTextCanNotEditNumber(),
                        		  new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                        	  );
                          }
                          catch(Exception e) {
                            new ServiceException(e).log();
                            app.addErrorMessage(
                              app.getTexts().getErrorTextCanNotEditNumber(),
                              new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                            );
                          }
                        }
                        boolean isModified = !DefaultPortalExtension.areEqual(
                          values,
                          mappedNewValues
                        );
                        SysLog.trace("modify feature", featureName + "=" + isModified);
                        if(isModified) {
                            if(target instanceof RefObject) {
                          	  	this.setValue(
                          	  		valueHolder, 
                          	  		target, 
                          	  		featureName, 
                          	  		mappedNewValues, 
                          	  		app
                          	  	);
                            }
                            else {
                                values.clear();
                                values.addAll(mappedNewValues);
                            }
                            modifiedFeatures.add(featureName);
                        }
                      }
                    }
        
                    // date
                    else if(valueHolder instanceof DateValue) {
                      SysLog.trace("Date value " + feature.getLabel(), Arrays.asList((Object[])parameterMap.get(key)));
                      SimpleDateFormat dateParser = DateValue.getLocalizedDateFormatter(
                          featureName, 
                          true, 
                          app
                      );
                      SimpleDateFormat dateTimeParser = DateValue.getLocalizedDateTimeFormatter(
                          featureName, 
                          true, 
                          app
                      );
                      Calendar cal = new GregorianCalendar();
        
                      // single-valued
                      if(valueHolder.isSingleValued()) {
                        try {
                          if(newValues.size() == 0) {
                            Object mappedNewValue = null;
                            if(target instanceof RefObject) {
                            	boolean isModified = !DefaultPortalExtension.areEqual(
                            		this.getValue(
                            			valueHolder, 
                            			target, 
                            			featureName, 
                            			app
                            		),
                            		mappedNewValue
                            	);
                            	SysLog.trace("modify feature", featureName + "=" + isModified);
                            	if(isModified) {
                              	  	this.setValue(
                              	  		valueHolder, 
                              	  		target, 
                              	  		featureName, 
                              	  		mappedNewValue, 
                              	  		app
                              	  	);
                              	  	modifiedFeatures.add(featureName);
                            	}
                            }
                            else {
                                targetAsValueMap(target).put(
                                    featureName,
                                    mappedNewValue
                                );
                            }
                          }
                          else {
                            String newValue = (String)newValues.get(0);
                            Date mappedNewValue = null;
                            try {
                                mappedNewValue = dateTimeParser.parse(newValue);
                            }
                            catch(ParseException e) {
                                mappedNewValue = dateParser.parse(newValue);
                            }                        
                            if(mappedNewValue != null) {
                              cal.setTime(mappedNewValue);
                              if(cal.get(GregorianCalendar.YEAR) < 100) {
                            	  int currentYear = new GregorianCalendar().get(GregorianCalendar.YEAR);
                            	  int year = cal.get(GregorianCalendar.YEAR);
                            	  cal.add(
                            		  GregorianCalendar.YEAR, 
                            		  100 * (currentYear / 100 - (Math.abs(currentYear % 100 - year % 100) < 50 ? 0 : 1))
                            	  );
                              }
                              if(PrimitiveTypes.DATE.equals(featureTypeName)) {
                                XMLGregorianCalendar mappedNewValueDate = DefaultPortalExtension.xmlDatatypeFactory().newXMLGregorianCalendarDate(
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH) + 1,
                                    cal.get(Calendar.DAY_OF_MONTH),
                                    DatatypeConstants.FIELD_UNDEFINED
                                );
                                if(target instanceof RefObject) {
                                	boolean isModified = !DefaultPortalExtension.areEqual(
	                                	this.getValue(
	                                		valueHolder, 
	                                		target, 
	                                		featureName, 
	                                		app
	                                	),
	                                	mappedNewValue
                                	);
                                	SysLog.trace("modify feature", featureName + "=" + isModified);
                                	if(isModified) {
	                                  	this.setValue(
	                                  		valueHolder, 
	                                		target, 
	                                		featureName, 
	                                		mappedNewValueDate, 
	                                		app
	                                	);
	                                  	modifiedFeatures.add(featureName);
                                	}
                                }
                                else {
                                    targetAsValueMap(target).put(
                                        featureName,
                                        mappedNewValueDate
                                    );
                                }
                              }
                              else if(PrimitiveTypes.DATETIME.equals(featureTypeName)) {
                            	  mappedNewValue = cal.getTime();
                                  if(target instanceof RefObject) {
	                                  boolean isModified = !DefaultPortalExtension.areEqual(
	                                	  this.getValue(
	                                		  valueHolder, 
	                                		  target, 
	                                		  featureName, 
	                                		  app
	                                	  ),
	                                      mappedNewValue
	                                  );
	                                  SysLog.trace("modify feature", featureName + "=" + isModified);
	                                  if(isModified) {
	                                	  this.setValue(
	                                		  valueHolder, 
	                                		  target, 
	                                		  featureName, 
	                                		  mappedNewValue, 
	                                		  app
	                                	  );	                                	  
	                                      modifiedFeatures.add(featureName);
	                                  }
                                  }
                                  else {
                                      targetAsValueMap(target).put(
                                          featureName,
                                          mappedNewValue
                                      );
                                  }
                              }
                              else {
                                app.addErrorMessage(
                                  app.getTexts().getErrorTextCanNotEditDate(),
                                  new String[]{feature.getLabel(), featureTypeName, "date type not supported"}
                                );
                              }
                            }
                            else {
                              app.addErrorMessage(
                                app.getTexts().getErrorTextCanNotEditDate(),
                                new String[]{feature.getLabel(), (String)newValues.get(0), "can not parse date"}
                              );
                            }
                          }
                        }
                        catch(JmiServiceException e) {
                          e.log();
                          app.addErrorMessage(
                            app.getTexts().getErrorTextCanNotEditDate(),
                            new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                          );
                        }
                        catch(Exception e) {
                          new ServiceException(e).log();
                          app.addErrorMessage(
                            app.getTexts().getErrorTextCanNotEditDate(),
                            new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                          );
                        }
                      }
        
                      // multi-valued
                      else {
                        Collection<Object> values = null;
                        if(target instanceof RefObject) {
                            values = valueAsCollection(
                            	this.getValue(
                            		valueHolder, 
                            		target, 
                            		featureName, 
                            		app
                            	)
                            );
                        }
                        else {
                            values = valueAsCollection(targetAsValueMap(target).get(featureName));
                            if(values == null) {
                                targetAsValueMap(target).put(
                                  featureName,
                                  values = new ArrayList<Object>()
                                );
                            }
                        }
                        List<Object> mappedNewValues = new ArrayList<Object>();
                        for(Iterator j = newValues.iterator(); j.hasNext(); ) {
                          try {
                            String newValue = (String)j.next();
                            Date dateTime = null;
                            try {
                                dateTime = dateTimeParser.parse(newValue);
                            }
                            catch(ParseException e) {
                                dateTime = dateParser.parse(newValue);
                            }
                            if(dateTime != null) {
                              cal.setTime(dateTime);
                              if(PrimitiveTypes.DATE.equals(featureTypeName)) {
                                XMLGregorianCalendar date = DefaultPortalExtension.xmlDatatypeFactory().newXMLGregorianCalendarDate(
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH) + 1,
                                    cal.get(Calendar.DAY_OF_MONTH),
                                    DatatypeConstants.FIELD_UNDEFINED
                                );
                                mappedNewValues.add(date);
                              }
                              else if(PrimitiveTypes.DATETIME.equals(featureTypeName)) {
                                mappedNewValues.add(dateTime);
                              }
                              else {
                                app.addErrorMessage(
                                  app.getTexts().getErrorTextCanNotEditDate(),
                                  new String[]{feature.getLabel(), featureTypeName, "date type not supported"}
                                );
                              }
                            }
                            else {
                            	app.addErrorMessage(
                            		app.getTexts().getErrorTextCanNotEditDate(),
                            		new String[]{feature.getLabel(), (String)newValues.get(0), "can not parse date"}
                            	);
                            }
                          }
                          catch(JmiServiceException e) {
                            e.log();
                            app.addErrorMessage(
                            	app.getTexts().getErrorTextCanNotEditDate(),
                              	new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                            );
                          }
                          catch(Exception e) {
                            new ServiceException(e).log();
                            app.addErrorMessage(
                            	app.getTexts().getErrorTextCanNotEditDate(),
                              new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                            );
                          }
                        }
                        boolean isModified = !DefaultPortalExtension.areEqual(
                          values,
                          mappedNewValues
                        );
                        SysLog.trace("modify feature", featureName + "=" + isModified);
                        if(isModified) {
                            if(target instanceof RefObject) {
	                          	  this.setValue(
	                        		  valueHolder, 
	                        		  target, 
	                        		  featureName, 
	                        		  mappedNewValues, 
	                        		  app
	                        	  );
                            }
                            else {
                                values.clear();
                                values.addAll(mappedNewValues);
                            }
                            modifiedFeatures.add(featureName);
                        }
                      }
                    }
        
                    // object reference
                    else if(valueHolder instanceof ObjectReferenceValue) {
                      if(!((String)key).endsWith(".Title")) {
                    	SysLog.trace("ObjRef value " + feature.getLabel(), Arrays.asList((Object[])parameterMap.get(key)));        
                        // single-valued
                        if(valueHolder.isSingleValued()) {
                          String xri = null;
                          Object[] titleValues = (Object[])parameterMap.get(key + ".Title");
                          // xri of referenced object entered (manually) as title. If set
                          // and valid this overrides xri set in field (by lookup inspector).
                          // If set and invalid and newValues is empty report an error
                          boolean xriSetAsTitleIsInvalid = false;
                          if((titleValues != null) && (titleValues.length > 0)) {
                        	SysLog.trace("ObjRef title value", titleValues[0]);
                            if(titleValues[0].toString().length() == 0) {
                              xri = ""; // reference removed by user
                            }
                            else {
                              try {
                                  URL titleUrl = new URL((String)(titleValues[0]));
                                  if("xri".equals(titleUrl.getProtocol())) {
                                	  xri = (String)titleValues[0];
                                  }
                                  else {
	                                  String query = URLDecoder.decode(titleUrl.getQuery(), "UTF-8");
	                                  int parameterPos = -1;
	                                  if((parameterPos = query.indexOf(WebKeys.REQUEST_PARAMETER + "=")) >= 0) {
	                                      String parameter = query.substring(parameterPos + 10);
	                                      if(parameter.indexOf("xri:@openmdx:") >= 0 || parameter.indexOf("xri://@openmdx:") > 0) {
	                                          xri = Action.getParameter(
	                                              parameter,
	                                              Action.PARAMETER_OBJECTXRI
	                                          );
	                                      }
	                                  }
                                  }
                              }
                              catch(MalformedURLException e) {
                                xriSetAsTitleIsInvalid = true;
                              }
                              catch(UnsupportedEncodingException e) {
                                  xriSetAsTitleIsInvalid = true;
                              }
                            }
                          }
                          // xri entered as title is valid
                          if(xriSetAsTitleIsInvalid && newValues.size() == 0) {
                              // title N/A (object not available) and N/P (no permission) is set by show object. Ignore.
                              if(!((String)titleValues[0]).startsWith("N/A") && !((String)titleValues[0]).startsWith("N/P")) {
                            	  SysLog.trace("xri entered as title is not valid", titleValues);
                            	  app.addErrorMessage(
                            		  app.getTexts().getErrorTextInvalidObjectReference(),
                                      new String[]{feature.getLabel(), (String)titleValues[0]}
                                  );
                              }
                          }
                          // xri entered as title is either valid or xri is set in field
                          else {
                              if((xri == null) && (newValues.size() > 0)) {
                                xri = (String)newValues.get(0);
                              }
                              SysLog.trace("ObjRef xri", xri);
                              try {
                                Object mappedNewValue = (xri == null) || "".equals(xri) ? 
                                	null : 
                                		new Path(xri);
                                if(target instanceof RefObject) {
                                	mappedNewValue = mappedNewValue == null ?
                                		null : 
                                			JDOHelper.getPersistenceManager(target).getObjectById(
		                                		mappedNewValue
		                                	);
                                    boolean isModified = true;
                                    // force modify in case the referenced object does not exist
                                    try {
	                                    isModified = !DefaultPortalExtension.areEqual(
	                                    	this.getValue(
	                                    		valueHolder, 
	                                    		target, 
	                                    		featureName, 
	                                    		app
	                                    	),
	                                        mappedNewValue
	                                    );
                                  } catch(Exception e) {}
                                  SysLog.trace("modify feature", featureName + "=" + isModified);
                                  if(isModified) {
                                	  this.setValue(
                                		  valueHolder, 
                                		  target, 
                                		  featureName, 
                                		  mappedNewValue, 
                                		  app
                                	  );
                                      modifiedFeatures.add(featureName);
                                  }
                                }
                                else {
                                    targetAsValueMap(target).put(
                                        featureName,
                                        mappedNewValue
                                    );
                                }
                              }
                              catch(JmiServiceException e) {
                                e.log();
                                app.addErrorMessage(
                                	app.getTexts().getErrorTextCanNotEditObjectReference(),
                                  new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                                );
                              }
                              catch(Exception e) {
                                new ServiceException(e).log();
                                app.addErrorMessage(
                                	app.getTexts().getErrorTextCanNotEditObjectReference(),
                                	new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                                );
                              }
                          }
                        }
        
                        // multi-valued
                        else {
                          // not supported yet
                        }
        
                      }
                    }
        
                    // code
                    else if(valueHolder instanceof CodeValue) {
                      SysLog.trace("Code value " + feature.getLabel(), Arrays.asList((Object[])parameterMap.get(key)));
                      Map longTexts = ((CodeValue)valueHolder).getLongText(false, false);        
                      // single-valued
                      if(valueHolder.isSingleValued()) {
                        try {
                          if(longTexts == null) {
                        	  SysLog.warning("Can not get ValueContainer with name", featureName);
                              System.err.println("WARNING: can not get CodeValueContainer with name " + featureName + ". Add " + featureName + " to the name list of a CodeValueContainer");
                              longTexts = new TreeMap();
                          }
                          Short mappedNewValue = newValues.size() == 0 ? 
                        	  new Short((short)0) : 
                        	  (Short)longTexts.get(newValues.get(0).toString());
                          if(mappedNewValue != null) {
                              if(target instanceof RefObject) {
                                  boolean isModified = !DefaultPortalExtension.areEqual(
                                  	  this.getValue(
	                                	  valueHolder, 
	                                	  target, 
	                                	  featureName, 
	                                	  app
                                  	  ),
                                      mappedNewValue
                                  );
                                  SysLog.trace("modify feature", featureName + "=" + isModified);
                                  if(isModified) {
                                	  this.setValue(
                                		  valueHolder, 
                                		  target, 
                                		  featureName, 
                                		  mappedNewValue, 
                                		  app
                                	  );
                                      modifiedFeatures.add(featureName);
                                  }
                              }
                              else {
                                  targetAsValueMap(target).put(
                                      featureName,
                                      mappedNewValue
                                  );
                              }
                          }
                          else {
                        	  SysLog.warning("Unable to map code field", Arrays.asList(newValues.get(0).toString(), longTexts));
                          }
                        }
                        catch(JmiServiceException e) {
                          e.log();
                          app.addErrorMessage(
                        	  app.getTexts().getErrorTextCanNotEditCode(),
                        	  new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                          );
                        }
                        catch(Exception e) {
                          new ServiceException(e).log();
                          app.addErrorMessage(
                        	  app.getTexts().getErrorTextCanNotEditCode(),
                        	  new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                          );
                        }
                      }
        
                      // multi-valued
                      else {
                        Collection<Object> values = null;
                        if(target instanceof RefObject) {
                            values = valueAsCollection(
                            	this.getValue(
                            		valueHolder, 
                            		target, 
                            		featureName, 
                            		app
                            	)
                            );
                        }
                        else {
                          values = valueAsCollection(targetAsValueMap(target).get(featureName));
                          if(values == null) {
                              targetAsValueMap(target).put(
                                  featureName,
                                  values = new ArrayList<Object>()
                              );
                          }
                        }
                        List<Object> mappedNewValues = new ArrayList<Object>();
                        for(Iterator j = newValues.iterator(); j.hasNext(); ) {
                          try {
                            String longText = j.next().toString();
                            Short code = (Short)longTexts.get(longText);
                            SysLog.trace("code mapping", longText + " to code=" + code);
                            if(code != null) {
                              mappedNewValues.add(
                                code
                              );
                            }
                          }
                          catch(JmiServiceException e) {
                            e.log();
                            app.addErrorMessage(
                            	app.getTexts().getErrorTextCanNotEditCode(),
                            	new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                            );
                          }
                          catch(Exception e) {
                            new ServiceException(e).log();
                            app.addErrorMessage(
                            	app.getTexts().getErrorTextCanNotEditCode(),
                            	new String[]{feature.getLabel(), (String)newValues.get(0), e.getMessage()}
                            );
                          }
                        }
                        boolean isModified = !DefaultPortalExtension.areEqual(
                          values,
                          mappedNewValues
                        );
                        SysLog.trace("modify feature", featureName + "=" + isModified);
                        if(isModified) {
                            if(target instanceof RefObject) {
                          	  	this.setValue(
                          	  		valueHolder, 
                          	  		target, 
                          	  		featureName, 
                          	  		mappedNewValues, 
                          	  		app
                          	  	);
                            }
                            else {
                                values.clear();
                                values.addAll(mappedNewValues);
                            }
                            modifiedFeatures.add(featureName);
                        }
                      }
                    }
        
                    // boolean
                    else if(valueHolder instanceof BooleanValue) {
                      SysLog.trace("Boolean: " + feature.getLabel() + "=" + Arrays.asList((Object[])parameterMap.get(key)));
        
                      // single-valued
                      if(valueHolder.isSingleValued()) {
                        Boolean mappedNewValue =
                            new Boolean(
                                (newValues.size() > 0) &&
                                ("true".equals(newValues.get(0)) ||
                                "on".equals(newValues.get(0)) ||
                                app.getTexts().getTrueText().equals(newValues.get(0)))
                            );
                        if(target instanceof RefObject) {
                        	boolean isModified = !DefaultPortalExtension.areEqual(
                        		this.getValue(
	                        		valueHolder, 
	                        		target, 
	                        		featureName, 
	                        		app
	                        	),
                                mappedNewValue
                          );
                          SysLog.trace("modify feature", featureName + "=" + isModified);
                          if(isModified) {
                        	  this.setValue(
                        		  valueHolder, 
                        		  target, 
                        		  featureName, 
                        		  mappedNewValue, 
                        		  app
                        	  );
                              modifiedFeatures.add(featureName);
                          }
                        }
                        else {
                            targetAsValueMap(target).put(
                                featureName,
                                mappedNewValue
                            );
                        }
                      }
        
                      // multi-valued
                      else {
                        Collection<Object> values = null;
                        if(target instanceof RefObject) {
                            values = valueAsCollection(
                            	this.getValue(
                            		valueHolder, 
                            		target, 
                            		featureName, 
                            		app
                            	)
                            );
                        }
                        else {
                          values = valueAsCollection(targetAsValueMap(target).get(featureName));
                          if(values == null) {
                              targetAsValueMap(target).put(
                                  featureName,
                                  values = new ArrayList<Object>()
                              );
                          }
                        }
                        List<Object> mappedNewValues = new ArrayList<Object>();
                        for(Iterator j = newValues.iterator(); j.hasNext(); ) {
                            Object mappedNewValue = j.next();
                            mappedNewValues.add(
                                new Boolean(
                                    "true".equals(mappedNewValue) ||
                                    "on".equals(mappedNewValue) ||
                                    app.getTexts().getTrueText().equals(mappedNewValue)
                                )
                            );
                        }
                        boolean isModified = !DefaultPortalExtension.areEqual(
                            values,
                            mappedNewValues
                        );
                        SysLog.trace("modify feature", featureName + "=" + isModified);
                        if(isModified) {
                            if(target instanceof RefObject) {
                          	  this.setValue(
                        		  valueHolder, 
                        		  target, 
                        		  featureName, 
                        		  mappedNewValues, 
                        		  app
                        	  );
                            }
                            else {
                                values.clear();
                                values.addAll(mappedNewValues);
                            }
                            modifiedFeatures.add(featureName);
                        }
                      }
                    }
        
                    // binary
                    else if(valueHolder instanceof BinaryValue) {
                      SysLog.trace("Binary: " + feature.getLabel());
                      String fileNameInfo = app.getTempFileName("" + key, ".INFO");
        
                      // single-valued
                      if(valueHolder.isSingleValued()) {
        
                        // reset value to null
                        if(newValues.size() == 0) {
                          SysLog.trace("reset to null");
        
                          // reset bytes
                          try {
                            if(target instanceof RefObject) {
                          	  this.setValue(
                        		  valueHolder, 
                        		  target, 
                        		  featureName, 
                        		  null, 
                        		  app
                        	  );
                            }
                            else {
                                targetAsValueMap(target).put(
                                    featureName,
                                    null
                                );
                            }
                          } 
                          catch(Exception e) {}        
                          // reset name
                          try {
                            if(target instanceof RefObject) {
	                          	  this.setValue(
	                        		  valueHolder, 
	                        		  target, 
	                        		  featureName + "Name", 
	                        		  null, 
	                        		  app
	                        	  );
                            }
                            else {
                                targetAsValueMap(target).put(
                                    featureName + "Name",
                                    null
                                );
                            }
                          } 
                          catch(Exception e) {}        
                          // reset mimeType
                          try {
                            if(target instanceof RefObject) {
	                          	  this.setValue(
	                        		  valueHolder, 
	                        		  target, 
	                        		  featureName + "MimeType", 
	                        		  null, 
	                        		  app
	                        	  );
                            }
                            else {
                                targetAsValueMap(target).put(
                                    featureName + "MimeType",
                                    null
                                );
                            }
                          } 
                          catch(Exception e) {}
                        }
        
                        // get binary stream and store
                        else {
                          SysLog.trace("modify feature", featureName + "=true");
                          modifiedFeatures.add(featureName);
        
                          boolean uploadStreamValid = true;
                          
                          // get mimeType, name from .INFO
                          try {
                            BufferedReader reader =
                              new BufferedReader(
                                new InputStreamReader(new FileInputStream(fileNameInfo))
                              );
                            String mimeType = reader.readLine();
                            String name = reader.readLine();
                            reader.close();
        
                            // set mimeType
                            try {
                              if(target instanceof RefObject) {
                            	  this.setValue(
                            		  valueHolder, 
                            		  target, 
                            		  featureName + "MimeType", 
                            		  mimeType, 
                            		  app
                            	  );
                              }
                              else {
                                  targetAsValueMap(target).put(
                                      featureName + "MimeType",
                                      mimeType
                                  );
                              }
                            }
                            catch(Exception e) {
                              SysLog.warning("can not set mimeType for " + featureName);
                              new ServiceException(e).log();
                            }
        
                            // set name
                            try {
                              if(target instanceof RefObject) {
                            	  this.setValue(
                            		  valueHolder, 
                            		  target, 
                            		  featureName + "Name", 
                            		  name, 
                            		  app
                            	  );
                              }
                              else {
                                  targetAsValueMap(target).put(
                                      featureName + "Name",
                                      name
                                  );
                              }
                            } 
                            catch(Exception e) {
                            	SysLog.warning("can not set name for " + featureName);
                                new ServiceException(e).log();
                            }
                          }
                          catch(FileNotFoundException e) {
                        	  SysLog.error("can not open info of uploaded stream " + fileNameInfo);
                              new ServiceException(e).log();
                              uploadStreamValid = false;
                          }
                          catch(IOException e) {
                        	  SysLog.error("can not read info of uploaded stream " + fileNameInfo);
                              new ServiceException(e).log();
                              uploadStreamValid = false;
                          }
        
                          // set bytes
                          String location = app.getTempFileName((String)key, "");
                          if(uploadStreamValid) {
                              if(target instanceof RefObject) {
                                  try {
                                	  this.setValue(
                                		  valueHolder, 
                                		  target, 
                                		  featureName, 
                                		  org.w3c.cci2.BinaryLargeObjects.valueOf(new File(location)), 
                                		  app
                                	  );
                                  }
                                  catch(Exception e) {
                                	  SysLog.error("Unable to upload binary content", location);
                                      new ServiceException(e).log();
                                  }
                              }
                              else {
                                  try {
                                      byte[] bytes = null;
                                      InputStream is = new FileInputStream(location);
                                      ByteArrayOutputStream os = new ByteArrayOutputStream();
                                      int b = 0;
                                      while((b = is.read()) != -1) {
                                        os.write(b);
                                      }
                                      is.close();
                                      os.close();
                                      bytes = os.toByteArray();
                                      targetAsValueMap(target).put(
                                          featureName,
                                          bytes
                                      );
                                    }
                                    catch(Exception e) {
                                    	SysLog.error("Unable to upload binary content", location);
                                        new ServiceException(e).log();
                                    }
                              }
                          }
                        }
                      }
        
                      // multi-valued
                      else {
                    	 SysLog.error("multi-valued binary not supported for", featureName);
                      }
                    }
        
                    // unknown
                    else {
                    	app.addErrorMessage(
                    		app.getTexts().getErrorTextAttributeTypeNotSupported(),
                    		new String[]{feature.getLabel(), feature.getValue() == null ? null : feature.getValue().getClass().getName(), "attribute type not supported"}
                    	);
                    }
                  }
                }
              }
            }
            if(modifiedFeatures.isEmpty()) break;
            count++;
        }
        // Validate mandatory fields
        for(Attribute feature: updatedFeatures.values()) {
            try {
                if(
                    feature.getValue().getFieldDef().isMandatory && 
                    feature.getValue().getFieldDef().isChangeable
                ) {
                    Object value = target instanceof RefObject ?
                        this.getValue(feature.getValue(), target, feature.getName(), app) :
                        	targetAsValueMap(target).get(feature.getName());
                    if(
                        (value == null) || 
                        (value instanceof String && ((String)value).length() == 0) || 
                        (value instanceof Collection && ((Collection)value).isEmpty())
                    ) {
                    	app.addErrorMessage(
                    		app.getTexts().getErrorTextMandatoryField(),
                            new String[]{feature.getLabel()}
                        );
                    }
                }
            } 
            catch(Exception e) {}
        }
    }
    
    //-------------------------------------------------------------------------  
    /**
     * Returns classes which are in the composition hierarchy of
     * the specified type. Returns a map with the class name as
     * key and a set of reference names as members, whereas the
     * references are composite references of the class.
     */
    protected void createCompositionHierarchy(
    	ModelElement_1_0 ofType,
    	Map<String,Set<String>> hierarchy
    ) throws ServiceException {
    	Model_1_0 model = ofType.getModel();        
    	// add ofType to hierarchy
    	String currentTypeName = (String)ofType.objGetValue("qualifiedName");
    	if(hierarchy.get(currentTypeName) == null) {
    		hierarchy.put(
    			currentTypeName,
    			new HashSet<String>()
    		);
    	}    
    	// get all types which are involved in composition hierarchy
    	List<ModelElement_1_0> typesToCheck = new ArrayList<ModelElement_1_0>();
    	if(!ofType.objGetList("compositeReference").isEmpty()) {
    		typesToCheck.add(ofType);
    	}
    	else {
    		for(Iterator i = ofType.objGetList("allSubtype").iterator(); i.hasNext(); ) {
    			ModelElement_1_0 subtype = model.getElement(i.next());
    			if(
    				!ofType.objGetValue("qualifiedName").equals(subtype.objGetValue("qualifiedName")) &&
    				!subtype.objGetList("compositeReference").isEmpty()
    			) {
    				typesToCheck.add(subtype);
    			}
    		}
    	}        
    	for(Iterator i = typesToCheck.iterator(); i.hasNext(); ) {
    		ModelElement_1_0 type = (ModelElement_1_0)i.next();
    		ModelElement_1_0 compositeReference = model.getElement(type.objGetValue("compositeReference"));
    		ModelElement_1_0 exposingType = model.getElement(compositeReference.objGetValue("container"));
    		this.createCompositionHierarchy(
    			exposingType,
    			hierarchy
    		);
    		hierarchy.get(
    			exposingType.objGetValue("qualifiedName")
    		).add(
    			(String)compositeReference.objGetValue("name")
    		);
    	}
    }

    //-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getLookupObject(org.openmdx.model1.accessor.basic.cci.ModelElement_1_0, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, org.openmdx.portal.servlet.ApplicationContext, javax.jdo.PersistenceManager)
     */
    @Override
    public RefObject_1_0 getLookupObject(
    	ModelElement_1_0 lookupType,
    	RefObject_1_0 startFrom,
    	ApplicationContext application
    ) throws ServiceException {
    	Model_1_0 model = application.getModel();
    	PersistenceManager pm = JDOHelper.getPersistenceManager(startFrom);
    	String qualifiedNameLookupType = (String)lookupType.objGetValue("qualifiedName");
    	Map<String,Set<String>> compositionHierarchy = new HashMap<String,Set<String>>();
    	this.createCompositionHierarchy(
    		lookupType,
    		compositionHierarchy
    	);
    	SysLog.trace("composition hierarchy", compositionHierarchy);        
    	RefObject_1_0 objectToShow = null;        
    	// get object to show. This is the first object which is member
    	// of the composition hierarchy of the referenced object.
    	RefObject_1_0 current = startFrom;
    	while(true) {
    		for(
    			Iterator i = compositionHierarchy.keySet().iterator(); 
    			i.hasNext(); 
    		) {
    			if(
    				model.isSubtypeOf(current.refClass().refMofId(), i.next()) &&
    				!model.isSubtypeOf(current.refClass().refMofId(), qualifiedNameLookupType)
    			) {
    				objectToShow = current;
    				break;
    			}
    		}
    		Path currentIdentity = current.refGetPath();
    		// In case current is corrupt for some reason
    		if(currentIdentity == null) {
    			break;
    		}
    		if(
    			(objectToShow != null) ||
    			(currentIdentity.size() < 7)
    		) break;
    		// go to parent
    		currentIdentity = currentIdentity.getParent().getParent();
    		try {
    			current = (RefObject_1_0)pm.getObjectById(currentIdentity);
    		}
    		catch(Exception e) {
    			SysLog.warning("Can not get object", Arrays.asList((Object)currentIdentity, e.getMessage()));
    			break;
    		}
    	}        
    	// If not found get root object which is in the composition hierarchy
    	if(objectToShow == null) {
    		RefObject[] rootObject = application.getRootObject();
    		for(int i = 0; i < rootObject.length; i++) {
    			for(Iterator j = compositionHierarchy.keySet().iterator(); j.hasNext(); ) {
    				if(model.isSubtypeOf(rootObject[i].refClass().refMofId(), j.next())) {
    					objectToShow = (RefObject_1_0)rootObject[i];
    					break;
    				}
    			}
    			if(objectToShow != null) break;
    		}            
    	}        
    	// take first root object if nothing found
    	if(objectToShow == null) {
    		objectToShow = (RefObject_1_0)application.getRootObject()[0];
    	}            
    	return objectToShow;
    }
      
    //-------------------------------------------------------------------------
    /**
     * Get view required which allows to lookup referenced types
     */
    public ObjectView getLookupView(
        String id,
        ModelElement_1_0 lookupType,
        RefObject_1_0 startFrom,
        String filterValues,
        ApplicationContext application
    ) throws ServiceException {
        RefObject_1_0 lookupObject = this.getLookupObject(
            lookupType, 
            startFrom, 
            application
        );
        String qualifiedNameLookupType = (String)lookupType.objGetValue("qualifiedName");        
        ObjectView view = new ShowObjectView(
            id,
            null,
            lookupObject.refGetPath(),
            application,
            new LinkedHashMap<Path,Action>(),
            qualifiedNameLookupType,
            null //compositionHierarchy,
        );
        return view;
    }
    
    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#hasUserDefineableQualifier(org.openmdx.ui1.jmi1.Inspector, org.openmdx.portal.servlet.ApplicationContext)
     */
    public boolean hasUserDefineableQualifier(
        org.openmdx.ui1.jmi1.Inspector inspector,
        ApplicationContext application
    ) {
        return true;
    }
    
    //-------------------------------------------------------------------------
    /**
     * The default implementation shows the grid content according to the user
     * settings. The default value is true if no user setting is found.
     */
    public boolean showGridContentOnInit(
        GridControl gridControl,
        ApplicationContext application
    ) {
        String showRowsOnInitPropertyName = gridControl.getPropertyName(
            gridControl.getQualifiedReferenceName(),
            Grid.PROPERTY_SHOW_ROWS_ON_INIT
        );
        return application.getSettings().getProperty(showRowsOnInitPropertyName) != null
            ? Boolean.valueOf(application.getSettings().getProperty(showRowsOnInitPropertyName)).booleanValue()
            : true;        
    }
    
    //-------------------------------------------------------------------------
    /**
     * The default implementation renders the unmodified text value, i.e. calls
     * p.write(value)
     */
    public void renderTextValue(
        ViewPort p,
        String value,
        boolean asWiki
    ) throws ServiceException {
        // Map email addresses to mailto:...
        int pos = 0;
        int fromIndex = 0;
        while((pos = value.indexOf("&#64;", fromIndex)) >= 0) {
            int start = pos-1;
            while(start >= 0) {
                char c = value.charAt(start);
                if(!Character.isLetterOrDigit(c) && (c != '.') && (c != '-') && (c != '_')) break;
                start--;
            }
            int end = pos+5;
            boolean suffixHasDot = false;
            int posParams = -1;
            while(end < value.length()) {
                char c = value.charAt(end);
                // Mail URL ends with whitespace or opening tag
                if(Character.isWhitespace(c) || (c == '<')) break;
                if(c == '?' && posParams < 0) posParams = end;
                suffixHasDot |= c == '.';
                end++;
            }
            if((start+1 < pos) && (end-1 > pos) && suffixHasDot) {
                String address = value.substring(start+1, end);
                String addressTitle = posParams > 0 ?
                    value.substring(start+1, posParams) :
                    value.substring(start+1, end);
                String href = asWiki ?
                	"mailto:" + address :
                	"<a href=\"mailto:" + address + "\">" + addressTitle + "</a>";
                value = value.substring(0, start+1) + href + value.substring(end);
                fromIndex = start + href.length() + 1;
            }
            else {
                fromIndex = pos + 1;
            }
        }
        // Map phone number to <a href="tel:...
        fromIndex = 0;
        while(
        	((pos = value.indexOf(" +", fromIndex)) >= 0) ||
        	(fromIndex == 0 && ((pos = value.indexOf("+")) == 0))
        ) {
            int start = value.charAt(pos) == '+' ? pos : pos + 1;
            int end = start;
            while(end < value.length()) {
                char c = value.charAt(end);
                if(!Character.isDigit(c) && !Character.isWhitespace(c) && (c != '+') && (c != '(') && (c != ')') && (c != '-')) break;
                end++;
            }
            if(end > start + 10) {
                String address = value.substring(start, end);
                String href =  asWiki ?
                	"tel:" + address :
                   	"<a href=\"tel:" + address + "\">" + address + "</a>";                		
                value = value.substring(0, start) + href + value.substring(end);
                fromIndex = start + href.length();
            }
            else {
                fromIndex = pos + 1;
            }
        }        
        // Map substrings starting with well-known protocols to <a href...
        // Do not need to generate HTML tags if text is postprocessed with wiki renderer
        if(!asWiki) {
	        for(
	            Iterator i = WELL_KNOWN_PROTOCOLS.iterator();
	            i.hasNext();
	        ) {
	            String protocol = (String)i.next();
	            fromIndex = 0;
	            while((pos = value.indexOf(protocol, fromIndex)) >= 0) {
	                // protocol must start after whitespace or after closing tag
	                if((pos == 0) || Character.isWhitespace(value.charAt(pos-1)) || ('>' == value.charAt(pos-1))) {
	                    int posEnd = pos+1;
	                    while(posEnd < value.length()) {
	                        if(
	                            ('<' == value.charAt(posEnd)) || 
	                            Character.isWhitespace(value.charAt(posEnd))
	                        ) {
	                            break;
	                        }
	                        posEnd++;
	                    }
	                    String address = value.substring(pos, posEnd);
	                    String end = value.substring(posEnd);
	                    String href = "<a href=\"" + address + "\">" + address + "</a>";
	                    value = value.substring(0,pos) + href + end;
	                    fromIndex = pos + href.length();
	                }
	                else {
	                    fromIndex = pos+1;
	                }
	            }
	        }
        }                
        p.write(value);
    }
    
    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getDateStyle(java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    public int getDateStyle(
       String qualifiedFeatureName,
       ApplicationContext application
    ) {
        return java.text.DateFormat.SHORT;
    }

    //-------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getTimeStyle(java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    public int getTimeStyle(
       String qualifiedFeatureName,
       ApplicationContext application
    ) {
        return java.text.DateFormat.MEDIUM;        
    }
    
    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getDataBinding(java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    public DataBinding getDataBinding(
       String dataBindingName
    ) {
        if(
            (dataBindingName != null) && 
            dataBindingName.startsWith(CompositeObjectDataBinding.class.getName())
        ) {
            return new CompositeObjectDataBinding(
                dataBindingName.indexOf("?") < 0 ?
                    "" :
                    dataBindingName.substring(dataBindingName.indexOf("?") + 1)
            );
        }
        else if(ReferencedObjectDataBinding.class.getName().equals(dataBindingName)) {
            return new ReferencedObjectDataBinding();
        }
        else {
            return new DefaultDataBinding();
        }
    }
    
    //-------------------------------------------------------------------------
    /**
     * The default implementation returns target as result, i.e. after an operation
     * invocation the view shown to the user remains unchanged.
     */
    public RefObject_1_0 handleOperationResult(
        RefObject_1_0 target, 
        String operationName, 
        RefStruct params, 
        RefStruct result
    ) throws ServiceException {
        return null;
    }

    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3690195425844146744L;

    protected static final Set<String> WELL_KNOWN_PROTOCOLS = 
        new HashSet<String>(Arrays.asList("http:/", "https:/", "Outlook:", "file:/"));
      
    /**
     * A lazy initialized DatatypeFactory instance
     */
    private static DatatypeFactory datatypeFactory = null;

    /**
     * @return a Datatype Factory Instance
     */
    protected static synchronized DatatypeFactory xmlDatatypeFactory(
    ){
        if(DefaultPortalExtension.datatypeFactory == null) try {
        	DefaultPortalExtension.datatypeFactory = DatatypeFactory.newInstance();
        } 
        catch (DatatypeConfigurationException e) {
          throw new RuntimeServiceException(e);
        }
        return DefaultPortalExtension.datatypeFactory;
    }
    
}

//--- End of File -----------------------------------------------------------
