/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: DefaultPortalExtension
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
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
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
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefStruct;
#if JAVA_8
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
#else
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
#endif

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.accessor.jmi.spi.RefPackage_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.IsGreaterCondition;
import org.openmdx.base.query.IsGreaterOrEqualCondition;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsLikeCondition;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SoundsLikeCondition;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.action.AbstractAction;
import org.openmdx.portal.servlet.action.CancelAction;
import org.openmdx.portal.servlet.action.CreateAction;
import org.openmdx.portal.servlet.action.DeleteAction;
import org.openmdx.portal.servlet.action.EditAction;
import org.openmdx.portal.servlet.action.EditAsNewAction;
import org.openmdx.portal.servlet.action.FindObjectAction;
import org.openmdx.portal.servlet.action.FindObjectsAction;
import org.openmdx.portal.servlet.action.FindSearchFieldValuesAction;
import org.openmdx.portal.servlet.action.InvokeOperationAction;
import org.openmdx.portal.servlet.action.LogoffAction;
import org.openmdx.portal.servlet.action.MacroAction;
import org.openmdx.portal.servlet.action.MultiDeleteAction;
import org.openmdx.portal.servlet.action.NewObjectAction;
import org.openmdx.portal.servlet.action.ObjectGetAttributesAction;
import org.openmdx.portal.servlet.action.ReloadAction;
import org.openmdx.portal.servlet.action.SaveAction;
import org.openmdx.portal.servlet.action.SaveSettingsAction;
import org.openmdx.portal.servlet.action.SelectAndEditObjectAction;
import org.openmdx.portal.servlet.action.SelectAndNewObjectAction;
import org.openmdx.portal.servlet.action.SelectLocaleAction;
import org.openmdx.portal.servlet.action.SelectObjectAction;
import org.openmdx.portal.servlet.action.SelectPerspectiveAction;
import org.openmdx.portal.servlet.action.SelectViewportAction;
import org.openmdx.portal.servlet.action.SetPanelStateAction;
import org.openmdx.portal.servlet.action.UiGetOperationDialogAction;
import org.openmdx.portal.servlet.action.UiGridAddColumnFilterAction;
import org.openmdx.portal.servlet.action.UiGridAddObjectAction;
import org.openmdx.portal.servlet.action.UiGridAddOrderAnyAction;
import org.openmdx.portal.servlet.action.UiGridAddOrderAscendingAction;
import org.openmdx.portal.servlet.action.UiGridAddOrderDescendingAction;
import org.openmdx.portal.servlet.action.UiGridGetRowMenuAction;
import org.openmdx.portal.servlet.action.UiGridMoveDownObjectAction;
import org.openmdx.portal.servlet.action.UiGridMoveUpObjectAction;
import org.openmdx.portal.servlet.action.UiGridPageNextAction;
import org.openmdx.portal.servlet.action.UiGridPagePreviousAction;
import org.openmdx.portal.servlet.action.UiGridSelectFilterAction;
import org.openmdx.portal.servlet.action.UiGridSelectReferenceAction;
import org.openmdx.portal.servlet.action.UiGridSetColumnFilterAction;
import org.openmdx.portal.servlet.action.UiGridSetCurrentFilterAsDefaultAction;
import org.openmdx.portal.servlet.action.UiGridSetHideRowsOnInitAction;
import org.openmdx.portal.servlet.action.UiGridSetOrderAnyAction;
import org.openmdx.portal.servlet.action.UiGridSetOrderAscendingAction;
import org.openmdx.portal.servlet.action.UiGridSetOrderDescendingAction;
import org.openmdx.portal.servlet.action.UiGridSetPageAction;
import org.openmdx.portal.servlet.action.UiGridSetShowRowsOnInitAction;
import org.openmdx.portal.servlet.action.UiGridSwapColumnOrderAction;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.BinaryValue;
import org.openmdx.portal.servlet.attribute.BooleanValue;
import org.openmdx.portal.servlet.attribute.CodeValue;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.attribute.FieldDef;
import org.openmdx.portal.servlet.attribute.NullValue;
import org.openmdx.portal.servlet.attribute.NumberValue;
import org.openmdx.portal.servlet.attribute.ObjectReferenceValue;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.component.Grid;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.control.Control;
import org.openmdx.portal.servlet.control.EditInspectorControl;
import org.openmdx.portal.servlet.control.InspectorControl;
import org.openmdx.portal.servlet.control.ShowInspectorControl;
import org.openmdx.portal.servlet.control.UiGridControl;
import org.openmdx.portal.servlet.databinding.CompositeObjectDataBinding;
import org.openmdx.portal.servlet.databinding.JoiningListDataBinding;
import org.openmdx.portal.servlet.databinding.ReferencedObjectDataBinding;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;
import org.openmdx.ui1.jmi1.ElementDefinition;
import org.openmdx.ui1.jmi1.FeatureDefinition;
import org.openmdx.ui1.jmi1.StructuralFeatureDefinition;
import org.w3c.cci2.MutableDatatypeFactory;

/**
 * DefaultPortalExtension
 *
 */
public class DefaultPortalExtension implements PortalExtension_1_0, Serializable {
  
	/**
	 * DefaultActionFactory
	 *
	 */
	public static class DefaultActionFactory implements ActionFactory {

		public AbstractAction getAction(
			short event
		) {
			switch(event) {
				case SaveAction.EVENT_ID:
					return new SaveAction();
				case CancelAction.EVENT_ID:
					return new CancelAction();
				case FindObjectsAction.EVENT_ID:
					return new FindObjectsAction();
				case UiGridSelectReferenceAction.EVENT_ID:
					return new UiGridSelectReferenceAction();
				case UiGridPageNextAction.EVENT_ID:
					return new UiGridPageNextAction();
				case UiGridSetPageAction.EVENT_ID:
					return new UiGridSetPageAction();
				case UiGridPagePreviousAction.EVENT_ID:
					return new UiGridPagePreviousAction();
				case UiGridSelectFilterAction.EVENT_ID:
					return new UiGridSelectFilterAction();
				case UiGridSetColumnFilterAction.EVENT_ID:
					return new UiGridSetColumnFilterAction();
				case UiGridAddColumnFilterAction.EVENT_ID:
					return new UiGridAddColumnFilterAction();
				case UiGridSetOrderAscendingAction.EVENT_ID:
					return new UiGridSetOrderAscendingAction();
				case UiGridAddOrderAscendingAction.EVENT_ID:
					return new UiGridAddOrderAscendingAction();
				case UiGridSetOrderDescendingAction.EVENT_ID:
					return new UiGridSetOrderDescendingAction();
				case UiGridAddOrderDescendingAction.EVENT_ID:
					return new UiGridAddOrderAscendingAction();
				case UiGridSetOrderAnyAction.EVENT_ID:
					return new UiGridSetOrderAnyAction();
				case UiGridAddOrderAnyAction.EVENT_ID:
					return new UiGridAddOrderAnyAction();
				case UiGridSetCurrentFilterAsDefaultAction.EVENT_ID:
					return new UiGridSetCurrentFilterAsDefaultAction();
				case UiGridAddObjectAction.EVENT_ID:
					return new UiGridAddObjectAction();
				case UiGridSetShowRowsOnInitAction.EVENT_ID:
					return new UiGridSetShowRowsOnInitAction();
				case UiGridGetRowMenuAction.EVENT_ID:
					return new UiGridGetRowMenuAction();
				case UiGridSetHideRowsOnInitAction.EVENT_ID:
					return new UiGridSetHideRowsOnInitAction();
				case FindObjectAction.EVENT_ID:
					return new FindObjectAction();
				case SetPanelStateAction.EVENT_ID:
					return new SetPanelStateAction();
				case SaveSettingsAction.EVENT_ID:
					return new SaveSettingsAction();
				case LogoffAction.EVENT_ID:
					return new LogoffAction();		
				case SelectObjectAction.EVENT_ID:
					return new SelectObjectAction();
				case ReloadAction.EVENT_ID:
					return new ReloadAction();
				case SelectAndEditObjectAction.EVENT_ID:
					return new SelectAndEditObjectAction();
				case SelectAndNewObjectAction.EVENT_ID:
					return new SelectAndNewObjectAction();
				case EditAction.EVENT_ID:
					return new EditAction();
				case NewObjectAction.EVENT_ID:
					return new NewObjectAction();
				case SelectLocaleAction.EVENT_ID:
					return new SelectLocaleAction();
				case DeleteAction.EVENT_ID:
					return new DeleteAction();
				case MultiDeleteAction.EVENT_ID:
					return new MultiDeleteAction();
				case InvokeOperationAction.EVENT_ID:
					return new InvokeOperationAction();
				case MacroAction.EVENT_ID:
					return new MacroAction();
				case ObjectGetAttributesAction.EVENT_ID:
					return new ObjectGetAttributesAction();
				case SelectPerspectiveAction.EVENT_ID:
					return new SelectPerspectiveAction();
				case SelectViewportAction.EVENT_ID:
					return new SelectViewportAction();
				case UiGridMoveDownObjectAction.EVENT_ID:
					return new UiGridMoveDownObjectAction();				
				case UiGridMoveUpObjectAction.EVENT_ID:
					return new UiGridMoveUpObjectAction();
				case UiGetOperationDialogAction.EVENT_ID:
					return new UiGetOperationDialogAction();
				case EditAsNewAction.EVENT_ID:
					return new EditAsNewAction();
				case CreateAction.EVENT_ID:
					return new CreateAction();
				case FindSearchFieldValuesAction.EVENT_ID:
					return new FindSearchFieldValuesAction();
				case UiGridSwapColumnOrderAction.EVENT_ID:
					return new UiGridSwapColumnOrderAction();
				default:
					return null;
			}
		}

	}
	
	/**
	 * DefaultControlFactory
	 *
	 */
	public static class DefaultControlFactory implements ControlFactory {
		
	    /**
	     * Reset controls cache.
	     */
	    public synchronized void reset(
	    ) {
	        this.gridControls.clear();
	        this.inspectorControls.clear();
	        this.fieldDefs.clear();	        
	    }
	    
	    /**
	     * Return UUID.
	     * 
	     * @return
	     */
	    protected String uuidAsString(
	    ) {
	        return UUIDConversion.toUID(UUIDs.newUUID());
	    }
	    
	    /**
	     * Create new instance of a ShowInspectorControl. Override
	     * for custom-specific implementation.
	     * 
	     * @param id
	     * @param perspective
	     * @param locale
	     * @param localeAsIndex
	     * @param controlFactory
	     * @param wizardDefinitionFactory
	     * @param inspectorDef
	     * @param forClass
	     * @return
	     */
	    protected ShowInspectorControl newShowInspectorControl(
            String id, 
            int perspective,
            String locale,
            int localeAsIndex,
            PortalExtension_1_0.ControlFactory controlFactory,
            WizardDefinitionFactory wizardDefinitionFactory,
            org.openmdx.ui1.jmi1.Inspector inspectorDef,
            String forClass	    			    		
	    ) {
	    	return new ShowInspectorControl(
	    		id,
	    		perspective,
	    		locale,
	    		localeAsIndex,
	    		controlFactory,
	    		wizardDefinitionFactory,
	    		inspectorDef,
	    		forClass
	    	);
	    }
	    
	    /**
	     * Create a new instance of an EditInspectorControl. Override for
	     * custom-specific implementation.
	     * 
	     * @param id
	     * @param locale
	     * @param localeAsIndex
	     * @param controlFactory
	     * @param inspectorDef
	     * @return
	     */
	    protected EditInspectorControl newEditInspectorControl(
            String id,
            String locale,
            int localeAsIndex,
            PortalExtension_1_0.ControlFactory controlFactory,
            org.openmdx.ui1.jmi1.Inspector inspectorDef	    		
	    ) {
	    	return new EditInspectorControl(
	    		id,
	    		locale,
	    		localeAsIndex,
	    		controlFactory,
	    		inspectorDef
	    	);	    	
	    }
	    
	    /**
	     * Create new instance of a GridControl. Override for
	     * custom-specific implementation.
	     * 
	     * @param id
	     * @param locale
	     * @param localeAsIndex
	     * @param controlFactory
	     * @param gridDef
	     * @param containerClass
	     * @param paneIndex
	     * @return
	     */
	    protected UiGridControl newGridControl(
            String id,
            String locale,
            int localeAsIndex,
            PortalExtension_1_0.ControlFactory controlFactory,
            org.openmdx.ui1.jmi1.ObjectContainer gridDef,
            String containerClass,
            int paneIndex	    		
	    ) {
	    	return new UiGridControl(
	    		id,
	    		locale,
	    		localeAsIndex,
	    		controlFactory,
	    		gridDef,
	    		containerClass,
	    		paneIndex
	    	);
	    }
	    
	    /* (non-Javadoc)
	     * @see org.openmdx.portal.servlet.PortalExtension_1_0.ControlFactory#createGridControl(java.lang.String, int, java.lang.String, int, org.openmdx.ui1.jmi1.Tab, int, java.lang.String)
	     */
	    @Override
	    public synchronized UiGridControl createGridControl(
	        String id,
	        int perspective,
	        String locale,
	        int localeAsIndex,
	        org.openmdx.ui1.jmi1.Tab tabDef,
	        int paneIndex,
	        String containerClass
	    ) {
	        org.openmdx.ui1.jmi1.ObjectContainer objectContainer = (org.openmdx.ui1.jmi1.ObjectContainer)tabDef.getMember().get(0);
	        String key = null;
	        synchronized(objectContainer) {
	            key = perspective + "*" + containerClass + "*" + objectContainer.refMofId() + "*" + paneIndex + "*" + locale;
	        }
	        UiGridControl gridControl = (UiGridControl)this.gridControls.get(key);
	        if(gridControl == null) {
	            this.gridControls.put(
	                key,
	                gridControl = this.newGridControl(
	                    id,
	                    locale,
	                    localeAsIndex,
	                    this,
	                    objectContainer,
	                    containerClass,
	                    paneIndex
	                )
	            );
	        }
	        return gridControl;
	    }

	    /* (non-Javadoc)
	     * @see org.openmdx.portal.servlet.PortalExtension_1_0.ControlFactory#createShowInspectorControl(java.lang.String, int, java.lang.String, int, org.openmdx.ui1.jmi1.Inspector, java.lang.String)
	     */
	    @Override
	    public synchronized ShowInspectorControl createShowInspectorControl(
	        String id,
	        int perspective,
	        String locale,
	        int localeAsIndex,
	        org.openmdx.ui1.jmi1.Inspector inspectorDef,
	        String forClass,
	        WizardDefinitionFactory wizardFactory
	    ) {
	        String key = perspective + "*" + forClass + "*Show*" + locale;
	        ShowInspectorControl inspectorControl = (ShowInspectorControl)this.inspectorControls.get(key);
	        if(inspectorControl == null) {
	            inspectorControl = this.newShowInspectorControl(
	                id == null ? this.uuidAsString() : id,
	                perspective,
	                locale,
	                localeAsIndex,
	                this,
	                wizardFactory,
	                inspectorDef,
	                forClass
	            );
	            this.inspectorControls.put(
	                key,
	                inspectorControl
	            );
	        }
	        return inspectorControl;
	    }

	    /* (non-Javadoc)
	     * @see org.openmdx.portal.servlet.PortalExtension_1_0.ControlFactory#createEditInspectorControl(java.lang.String, int, java.lang.String, int, org.openmdx.ui1.jmi1.Inspector, java.lang.String)
	     */
	    @Override
	    public synchronized EditInspectorControl createEditInspectorControl(
	        String id,
	        int perspective,
	        String locale,
	        int localeAsIndex,
	        org.openmdx.ui1.jmi1.Inspector inspectorDef,
	        String forClass
	    ) {
	        String key = perspective + "*" + forClass + "*Edit*" + locale;
	        EditInspectorControl inspectorControl = (EditInspectorControl)this.inspectorControls.get(key);
	        if(inspectorControl == null) {
	            inspectorControl = this.newEditInspectorControl(
	                id == null ? this.uuidAsString() : id,
	                locale,
	                localeAsIndex,
	                this,
	                inspectorDef
	            );
	            this.inspectorControls.put(
	                key,
	                inspectorControl
	            );
	        }
	        return inspectorControl;
	    }

	    /* (non-Javadoc)
	     * @see org.openmdx.portal.servlet.PortalExtension_1_0.ControlFactory#createControl(java.lang.String, java.lang.String, int, java.lang.Class, java.lang.Object[])
	     */
	    @Override
	    public synchronized Control createControl(
	        String id,
	        String locale,
	        int localeAsIndex,
	        Class<?> controlClass,
	        Object... parameter
	    ) throws ServiceException {
	        try {
	        	// Signature w/o parameters
	        	Constructor<?> cons = controlClass.getConstructor(
	                new Class[]{
	                    String.class,
	                    String.class,
	                    int.class
	                }
	            );
	        	try {
		            return (Control)cons.newInstance(
		                new Object[]{
		                    id == null 
		                    	? this.uuidAsString() 
		                    	: id,
		                    locale,
		                    Integer.valueOf(localeAsIndex)
		                }
		            );
	        	} catch(Exception e) {
	        		throw new ServiceException(e);
	        	}
	        } catch(NoSuchMethodException ignore) {
		        try {
		        	// Signature w/ parameters
		            Constructor<?> cons = controlClass.getConstructor(
		                new Class[]{
		                    String.class,
		                    String.class,
		                    int.class,
		                    Object[].class
		                }
		            );
		            return (Control)cons.newInstance(
		                new Object[]{
		                    id == null ? this.uuidAsString() : id,
		                    locale,
		                    Integer.valueOf(localeAsIndex),
		                    parameter
		                }
		            );
		        } catch(Exception e) {
		            throw new ServiceException(e);
		        }
	        }
	    }

	    /* (non-Javadoc)
	     * @see org.openmdx.portal.servlet.PortalExtension_1_0.ControlFactory#getAttributeValue(org.openmdx.ui1.jmi1.ValuedField, java.lang.Object, org.openmdx.portal.servlet.ApplicationContext)
	     */
	    @Override
	    public AttributeValue createAttributeValue(
	        org.openmdx.ui1.jmi1.ValuedField field,
	        Object object,
	        ApplicationContext app
	    ) throws ServiceException {
	      Path fieldIdentity = null;
	      try {
	          fieldIdentity = field.refGetPath();
	      } catch(ConcurrentModificationException e) {
		      // Retry to get the refMofId if the first attempt fails
		      // Getting the refMofId the first time modifies a JMI object
		      // JMI objects are not thread-safe which may throw a 
		      // ConcurrentModificationException. This and other methods of 
		      // openMDX/Portal are not synchronized a) for performance reasons 
		      // and b) because ui objects are updated very rarely (typically 
		      // only the first time a control is initialized)
	          try { 
	          	Thread.sleep(10); 
	          } catch(Exception ignore) {
	  			SysLog.trace("Exception ignored", ignore);
	          }
	          fieldIdentity = field.refGetPath();
	      }
	      SysLog.trace("mapping field", field);
	      Model_1_0 model = null;
	      ModelElement_1_0 classDef = null;
	      // RefObject_1_0: derive class from object
	      if(object instanceof RefObject_1_0) {
	          model = ((RefPackage_1)((RefObject_1_0)object).refOutermostPackage()).refModel();
	          classDef = ((RefMetaObject_1)((RefObject)object).refMetaObject()).getElementDef();
	      } else if(object instanceof Map) {
		      // Map: object must be class name
	          model = Model_1Factory.getModel();
	          try {
	        	  @SuppressWarnings("unchecked")
	        	  Map<String,Object> objectAsMap = (Map<String,Object>)object;
	              classDef = model.getElement(objectAsMap.get(SystemAttributes.OBJECT_CLASS));
	          }  catch(Exception e) {}
	      }
	      AttributeValue value = NullValue.createNullValue();
	      FieldDef fieldDef = (FieldDef)this.fieldDefs.get(fieldIdentity);
	      // Number / Code
	      if(field instanceof org.openmdx.ui1.jmi1.NumberField) {
	        org.openmdx.ui1.jmi1.NumberField f = (org.openmdx.ui1.jmi1.NumberField)field;
	        String qualifiedTypeName = null;
	        String qualifiedClassName = null;
	        if(object instanceof RefObject_1_0) {
	        	qualifiedClassName = ((RefObject_1_0)object).refClass().refMofId();
	        } else if(object instanceof Map) {
	        	  @SuppressWarnings("unchecked")
	        	  Map<String,Object> objectAsMap = (Map<String,Object>)object;
	        	  qualifiedClassName = (String)objectAsMap.get(SystemAttributes.OBJECT_CLASS);
	        }
	        if(qualifiedClassName != null) {
	            try {
	                ModelElement_1_0 compositeReference = model.getElement((Path)classDef.objGetValue("compositeReference"));
	                ModelElement_1_0 typeDef = model.getElement(compositeReference.getType());
	                qualifiedTypeName = (String)typeDef.getQualifiedName();
	            } catch(Exception e) {}
	        }
	        // return code value in case a code table is defined for feature for the instance-level class
	        if(
	            (qualifiedClassName != null) &&
	            (app.getCodes() != null) &&
	            app.getCodes().getLongText(qualifiedClassName + ":" + f.getFeatureName(), (short)0, true, true) != null
	        ) {
	            if(fieldDef == null) {
	                this.fieldDefs.put(
	                    fieldIdentity,
	                    fieldDef = FieldDef.createFieldDef(app, field)
	                );
	            }
	            value = CodeValue.createCodeValue(
	                object,
	                fieldDef,
	                app,
	                qualifiedClassName + ":" + f.getFeatureName()
	            );
	        } else if(
	            (app.getCodes() != null) &&
	            (qualifiedTypeName != null) &&
	            (app.getCodes().getLongText(qualifiedTypeName + ":" + f.getFeatureName(), (short)0, true, true) != null)
	        ) {
		        // Each concrete class has a compositeReference, i.e. is referenced by a 
		        // parent class with aggregation kind = composite. Determine the type, i.e. 
		        // class of the composite reference. The referenced class with all its subclasses 
		        // defines a class hierarchy. The ObjectInspectorServlet allows to define codes 
		        // either for a) a fully qualified attribute name or b) for an attribute of a class 
		        // hierarchy. Option b) allows to define individual code tables for each class 
		        // hierarchy in case the attribute is member of an abstract root class.
	            if(fieldDef == null) {
	                this.fieldDefs.put(
	                    fieldIdentity,
	                    fieldDef = FieldDef.createFieldDef(app, field)
	                );
	            }
	            value = CodeValue.createCodeValue(
	                object,
	                fieldDef,
	                app,
	                qualifiedTypeName + ":" + f.getFeatureName()
	            );
	        } else if(
	            (app.getCodes() != null) &&
	            app.getCodes().getLongText(f.getQualifiedFeatureName(), (short)0, true, true) != null) {
	            if(fieldDef == null) {
	                this.fieldDefs.put(
	                    fieldIdentity,
	                    fieldDef = FieldDef.createFieldDef(app, field)
	                );
	            }
	            value = CodeValue.createCodeValue(
	                object,
	                fieldDef,
	                app,
	                f.getQualifiedFeatureName()
	              );
	        } else {
	            org.openmdx.ui1.jmi1.NumberField numberField = (org.openmdx.ui1.jmi1.NumberField)field;
	            if(fieldDef == null) {
	                this.fieldDefs.put(
	                    fieldIdentity,
	                    fieldDef = FieldDef.createNumberFieldDef(app, numberField)
	                );
	            }
	            value = NumberValue.createNumberValue(
	                object,
	                fieldDef,
	                f.isHasThousandsSeparator(),
	                f.getMinValue(),
	                f.getMaxValue(),
	                app
	            );
	        }
	      } else if(field instanceof org.openmdx.ui1.jmi1.DateField) {
	          org.openmdx.ui1.jmi1.DateField f = (org.openmdx.ui1.jmi1.DateField)field;
	          if(fieldDef == null) {
	              this.fieldDefs.put(
	                  fieldIdentity,
	                  fieldDef = FieldDef.createDateFieldDef(app, f)
	              );
	          }
	          value = DateValue.createDateValue(
	              object,
	              fieldDef,
	              app
	          );
	      } else if(field instanceof org.openmdx.ui1.jmi1.ObjectReferenceField) {
	          org.openmdx.ui1.jmi1.ValuedField f = field;
	          if(fieldDef == null) {
	            this.fieldDefs.put(
	                  fieldIdentity,
	                  fieldDef = FieldDef.createFieldDef(app, f)
	              );
	          }
	          value = ObjectReferenceValue.createObjectReferenceValue(
	              object,
	              fieldDef,
	              app
	          );
	      } else if(field instanceof org.openmdx.ui1.jmi1.TextField) {
	          org.openmdx.ui1.jmi1.TextField f = (org.openmdx.ui1.jmi1.TextField)field;
	          if(fieldDef == null) {
	            this.fieldDefs.put(
	                  fieldIdentity,
	                  fieldDef = FieldDef.createFieldDef(app, f)
	              );
	          }
	          value = TextValue.createTextValue(
	              object,
	              fieldDef,
	              false,
	              Integer.MAX_VALUE,
	              app
	          );
	      } else if(field instanceof org.openmdx.ui1.jmi1.TextBox) {
	          org.openmdx.ui1.jmi1.TextBox f = (org.openmdx.ui1.jmi1.TextBox)field;
	          if(fieldDef == null) {
	            this.fieldDefs.put(
	                  fieldIdentity,
	                  fieldDef = FieldDef.createFieldDef(app, f)
	              );
	          }
	          value = TextValue.createTextValue(
	              object,
	              fieldDef,
	              f.isPassword(),
	              f.getMaxLength(),
	              app
	          );
	      } else if(field instanceof org.openmdx.ui1.jmi1.CheckBox) {
	          org.openmdx.ui1.jmi1.ValuedField f = field;
	          if(fieldDef == null) {
	            this.fieldDefs.put(
	                  fieldIdentity,
	                  fieldDef = FieldDef.createFieldDef(app, f)
	              );
	          }
	          value = BooleanValue.createBooleanValue(
	              object,
	              fieldDef,
	              app
	          );
	      } else if(field instanceof org.openmdx.ui1.jmi1.DocumentBox) {
	          org.openmdx.ui1.jmi1.DocumentBox f = (org.openmdx.ui1.jmi1.DocumentBox)field;
	          if(fieldDef == null) {
	            this.fieldDefs.put(
	                  fieldIdentity,
	                  fieldDef = FieldDef.createBinaryFieldDef(app, f)
	              );
	          }
	          value = BinaryValue.createBinaryValue(
	              object,
	              fieldDef,
	              app
	          );
	      }
	      return value;
	    }
	    
	    //-------------------------------------------------------------------------
	    // Members
	    //-------------------------------------------------------------------------	    
	    private Map<String,UiGridControl> gridControls = new HashMap<String,UiGridControl>();
	    private Map<String,InspectorControl> inspectorControls = new HashMap<String,InspectorControl>();
	    private final Map<Path,FieldDef> fieldDefs = new HashMap<Path,FieldDef>();	    
	    
	}
	
    /**
     * Return toString() of the given object. If object is a collection 
     * return toString() of first element.
     * 
     * @param obj
     * @return
     */
    public String toPlain(
        Object obj
    ) {
        String s = obj == null ? "" : 
    		(obj instanceof Collection) && !((Collection<?>)obj).isEmpty() ? 
    			((Collection<?>)obj).iterator().next().toString() : 
    				obj.toString();
    	return s;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.PortalExtension_1_0#getTitle(java.lang.Object, org.openmdx.portal.servlet.Action, java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
	 */
	@Override
    public String getTitle(
    	Object obj,
    	Action action, 
    	String title, 
    	ApplicationContext app
    ) {
		return title;
    }

    /**
     * Return title of refObj.
     * 
     * @param obj
     * @param locale
     * @param localeAsString
     * @param app
     * @return
     */
    public String getTitle(
        RefObject_1_0 obj,
        short locale,
        String localeAsString,
        ApplicationContext app
    ) {
    	return this.getTitle(
    		obj, 
    		locale, 
    		localeAsString,
    		false, // asShortTitle = false
    		app
    	);
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getTitle(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, short, java.lang.String, boolean, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public String getTitle(
    	RefObject_1_0 refObj, 
    	short locale,
    	String localeAsString,
    	boolean asShortTitle,
    	ApplicationContext application
    	) {
    	if(refObj == null) {
    		return this.toPlain("#NULL");
    	}
    	if(JDOHelper.isNew(refObj) || !JDOHelper.isPersistent(refObj)) {
    		return this.toPlain("Untitled");
    	}
    	Path p = refObj.refGetPath();
    	Model_1_0 model = ((RefPackage_1_0)refObj.refOutermostPackage()).refModel();
    	String objectClass = refObj.refClass().refMofId();
    	try {
    		ModelElement_1_0 classDef = model.getElement(objectClass);
    		Map<String,ModelElement_1_0> attributeDefs = model.getAttributeDefs(classDef, false, true);
    		if(
    			attributeDefs.keySet().contains("fullName") &&
    			(refObj.refGetValue("fullName") != null)
    		) {
    			return this.toPlain(refObj.refGetValue("fullName"));
    		} else if(
    			attributeDefs.keySet().contains("title") &&
    			(refObj.refGetValue("title") != null)
    		) {
    			return this.toPlain(refObj.refGetValue("title"));
    		} else if(
    			attributeDefs.keySet().contains("name") &&
    			(refObj.refGetValue("name") != null) 
    		) {
    			return this.toPlain(refObj.refGetValue("name"));
    		} else if(
    			attributeDefs.keySet().contains("description") &&
    			(refObj.refGetValue("description") != null)
    		) {
    			return this.toPlain(refObj.refGetValue("description"));
    		} else {
    			return p.getLastSegment().toClassicRepresentation();
    		}
    	} catch(ServiceException e) {
    		e.log();
    		SysLog.warning("can not evaluate. object", refObj.refMofId());
    		return "#ERR (" + e.getMessage() + ")";
    	}
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#hasPermission(java.lang.String, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, java.util.Set, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public boolean hasPermission(
        String elementName, 
        RefObject_1_0 object,
        ApplicationContext app,
        String action
    ) {
        return false;
    }
  
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#hasPermission(org.openmdx.portal.servlet.control.Control, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, org.openmdx.portal.servlet.ApplicationContext, java.lang.String)
     */
    @Override
    public boolean hasPermission(
        Control control, 
        RefObject_1_0 object,
        ApplicationContext app,
        String action        
    ) {    	
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#isEnabled(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public boolean hasPermission(
        RefObject_1_0 object,
        ApplicationContext app,
        String action
    ) {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getIdentityQueryFilterClause(java.lang.String)
     */
    @Override
    public org.openmdx.base.query.Filter getQuery(        
    	String qualifiedFeatureName,
    	String filterValue,
    	int queryFilterStringParamCount,
    	ApplicationContext app
    ) throws ServiceException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getGridPageSize(java.lang.String)
     */
    @Override
    public int getGridPageSize(
        String referencedTypeName
    ) {
        // default page size
        return 15;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getGridRowBackColor(java.lang.String)
     */
    @Override
    public String[] getGridRowColors(
        RefObject_1_0 obj
    ) {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#isLookupType(org.openmdx.model1.accessor.basic.cci.ModelElement_1_0)
     */
    @Override
    public boolean isLookupType(
        ModelElement_1_0 classDef
    ) throws ServiceException {
        String qualifiedName = (String)classDef.getQualifiedName();
        return 
            !"org:openmdx:base:BasicObject".equals(qualifiedName) &&
            !"org:openmdx:base:ContextCapable".equals(qualifiedName);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getAutocompleter(org.openmdx.portal.servlet.ApplicationContext, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, java.lang.String)
     */
    @Override
    public Autocompleter_1_0 getAutocompleter(
        ApplicationContext app,
        RefObject_1_0 context,
        String qualifiedFeatureName,
        String restrictToType
    ) {
        try {
            Model_1_0 model = app.getModel();
            ModelElement_1_0 lookupType = null;
            // Get lookup type from model
            try {
                ModelElement_1_0 lookupFeature = model.getElement(qualifiedFeatureName);
                lookupType = model.getElement(lookupFeature.getType());
            } catch(Exception e) {
                try {
                    // Fallback to customized feature definitions
                    FeatureDefinition lookupFeature = app.getFeatureDefinition(qualifiedFeatureName);
                    if(lookupFeature instanceof StructuralFeatureDefinition) {
                        lookupType = model.getElement(((StructuralFeatureDefinition)lookupFeature).getType());
                    }
                } catch(Exception e0) {}
            }
            if(lookupType != null && restrictToType != null) {
            	ModelElement_1_0 subtype = model.getElement(restrictToType);
            	if(model.isSubtypeOf(subtype, lookupType)) {
            		lookupType = subtype;
            	}
            }
            if(
                (lookupType != null) && 
                model.isClassType(lookupType) &&
                this.isLookupType(lookupType)
            ) {
                RefObject lookupObject = this.getLookupObject(
                    lookupType, 
                    context, 
                    app
                );
                Path lookupObjectIdentity = new Path(lookupObject.refMofId());
                Map<Integer,String> filterByFeatures = new TreeMap<Integer,String>();
                Map<Integer,String> orderByFeatures = new TreeMap<Integer,String>();
                Map<Integer,String> filterByLabels = new TreeMap<Integer,String>();
                Map<Integer,String> lookupReferenceNames = new TreeMap<Integer,String>();
                ModelElement_1_0 lookupObjectClass = ((RefMetaObject_1)lookupObject.refMetaObject()).getElementDef();
                Map<String,ModelElement_1_0> lookupObjectFeatures = model.getStructuralFeatureDefs(lookupObjectClass, true, false, false);
                ModelElement_1_0 extentCapableClass = model.getElement("org:openmdx:base:ExtentCapable");
                ModelElement_1_0 contextCapableClass = model.getElement("org:openmdx:base:ContextCapable");
                ModelElement_1_0 basicObjectClass = model.getElement("org:openmdx:base:BasicObject");
                // Find composite reference of lookup object which references objects of type lookup type
                int ii = 0;
                for(ModelElement_1_0 feature: lookupObjectFeatures.values()) {
                    if(model.isReferenceType(feature)) {
                        ModelElement_1_0 referencedEnd = model.getElement(feature.getReferencedEnd());
                        ModelElement_1_0 referencedType = model.getElement(feature.getType());
                        List<Object> allReferencedTypes = new ArrayList<Object>();
                        for(Iterator<Object> j = referencedType.objGetList("allSubtype").iterator(); j.hasNext(); ) {
                            allReferencedTypes.addAll(
                                model.getElement(j.next()).objGetList("allSupertype")
                            );
                        }
                        if(
                            !referencedType.equals(extentCapableClass) && 
                            !referencedType.equals(contextCapableClass) && 
                            !referencedType.equals(basicObjectClass) &&
                            !AggregationKind.NONE.equals(referencedEnd.getAggregation()) &&
                            allReferencedTypes.contains(lookupType.jdoGetObjectId()) 
                        ) {
                            String lookupReferenceName = (String)feature.getName();
                            // Get default order by features for context object. Get all attributes
                            // which include the strings name, description, title or number and
                            // the attribute type is PrimitiveTypes.STRING
                            // Find reference of lookup object which references objects of type contextClass
                            Map<String,ModelElement_1_0> lookupTypeAttributes = model.getAttributeDefs(lookupType, true, false);
                            for(Iterator<ModelElement_1_0> k = lookupTypeAttributes.values().iterator(); k.hasNext(); ) {
                                ModelElement_1_0 attributeDef = (ModelElement_1_0)k.next();
                                ModelElement_1_0 attributeType = model.getElement(attributeDef.getType());
                                String attributeName = attributeDef.getName();
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
                                    PrimitiveTypes.STRING.equals(attributeType.getQualifiedName())                                    
                                ) {
                                    int order = 10000 * (filterByFeatures.size() + 1);
                                    try {
                                        org.openmdx.ui1.jmi1.ElementDefinition field = app.getUiElementDefinition(
											attributeDef.getQualifiedName()
                                        );
                                        org.openmdx.ui1.jmi1.AssertableInspector referencedTypeInspector =
                                            app.getAssertableInspector(referencedType.getQualifiedName());
                                        String referencedTypeLabel =  app.getLabel(
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
                                            int locale = app.getCurrentLocaleAsIndex();
                                            // Order autocompleters by <order referenced type,index,field order>
                                            order =  field.getOrderObjectContainer().size() > 2 
                                            	? 1000000*orderReferencedType + 10000*ii + 100*((Number)field.getOrderObjectContainer().get(1)).intValue() + ((Number)field.getOrderObjectContainer().get(2)).intValue() 
                                            	: field.getOrder().size() > 2 
                                            		? 1000000*orderReferencedType + 10000*ii + 100*((Number)field.getOrder().get(1)).intValue() + ((Number)field.getOrder().get(2)).intValue() 
                                            		: 1000000*orderReferencedType + 10000*ii + order;   
                                            String label = locale < field.getLabel().size() 
                                            	? field.getLabel().get(locale) 
                                            	: field.getLabel().isEmpty() ? attributeName : field.getLabel().get(0);
                                            lookupReferenceNames.put(
                                                Integer.valueOf(order), 
                                                lookupReferenceName
                                            );
                                            filterByLabels.put(
                                                order,
                                                referencedTypeLabel + " / " + label
                                            );
                                            filterByFeatures.put(
                                                order,
                                                attributeName
                                            );
                                            orderByFeatures.put(
                                            	order,
                                            	Boolean.TRUE.equals(field.isSortable()) ? attributeName : ""
                                            );
                                        }
                                    } catch(Exception e) {
                                        lookupReferenceNames.put(
                                            order, 
                                            lookupReferenceName
                                        );
                                        filterByLabels.put(
                                            order,
                                            attributeName
                                        );                                        
                                        filterByFeatures.put(
                                            order,
                                            attributeName
                                        );
                                        orderByFeatures.put(
                                        	order,
                                        	""
                                        );
                                    }
                                }
                            }
                        }
                    }
                    ii++;
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
                        (String)lookupType.getQualifiedName(),
                        (String[])filterByFeatures.values().toArray(new String[filterByFeatures.size()]),
                        (String[])filterByLabels.values().toArray(new String[filterByLabels.size()]),
                        filterOperators,
                        (String[])orderByFeatures.values().toArray(new String[orderByFeatures.size()])
                    );
                }
            }
        } catch(Exception e) {
            SysLog.warning("Error getting autocompleter", Arrays.asList(new String[]{context == null ? "N/A" : context.refMofId(), qualifiedFeatureName}));
            Throwables.log(e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getFindObjectsBaseFilter(org.openmdx.portal.servlet.ApplicationContext, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, java.lang.String)
     */
    @Override
    public List<Condition> getFindObjectsBaseFilter(
        ApplicationContext app,
        RefObject_1_0 context,
        String qualifiedFeatureName
    ) {
        return new ArrayList<>();
    }

    /**
     * Get current locale
     * @param app
     * @return
     */
    protected Locale getCurrentLocale(
        ApplicationContext app
    ) {
        String locale = app.getCurrentLocaleAsString();
        return new Locale(
            locale.substring(0, 2), 
            locale.substring(locale.indexOf("_") + 1)
        );      
    }

    /**
     * Cast object to Map.
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    static protected Map<String,Object> targetAsValueMap(
    	Object object
    ) {
    	return (Map<String,Object>)object;
    }

    /**
     * Cast object to Collection
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    static protected Collection<Object> valueAsCollection(
    	Object object
    ) {
    	return (Collection<Object>)object;
    }

    /**
     * Get attribute value.
     * 
     * @param valueHolder
     * @param target
     * @param featureName
     * @param app
     * @return
     */
    protected Object getValue(
    	AttributeValue valueHolder,
    	Object target,
    	String featureName,
    	ApplicationContext app
    ) {
        return valueHolder.getDataBinding().getValue(
            (RefObject)target, 
            featureName,
            app
        );
    }

    /**
     * Set attribute value.
     * 
     * @param valueHolder
     * @param target
     * @param featureName
     * @param value
     * @param app
     */
    protected void setValue(
    	AttributeValue valueHolder,
    	Object target,
    	String featureName,
    	Object value,
    	ApplicationContext app
    ) {
        valueHolder.getDataBinding().setValue(
            (RefObject)target,
            featureName,
            value,
            app
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#updateObject(java.lang.Object, java.util.Map, java.util.Map, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public void updateObject(
    	Object target,
    	Map<String,String[]> parameterMap,
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
    		Set<String> modifiedFeatures = new HashSet<>();
    		for(
    			Iterator<String> i = parameterMap.keySet().iterator(); 
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
    					featureTypeName = model.getElement(featureDef.getType()).getQualifiedName();
    				} catch(Exception e) {
    					try {
    						// Fallback: lookup feature in ui repository as feature definition
    						FeatureDefinition featureDef = app.getFeatureDefinition(featureName);
    						if(featureDef instanceof StructuralFeatureDefinition) {
    							featureTypeName = ((StructuralFeatureDefinition)featureDef).getType();
    						}
    					} catch(Exception ignore) {}                
    				}        
    				Attribute feature = (Attribute)fieldMap.get(featureName);
    				if(feature != null) {        
    					// Parse parameter values
    					List<Object> parameterValues = Arrays.asList((Object[])parameterMap.get(key));
    					StringTokenizer tokenizer = parameterValues.isEmpty() 
    						? new StringTokenizer("", "\n", true) 
    						: new StringTokenizer((String)parameterValues.get(0), "\n\r", true);
						List<String> newValues = new ArrayList<>();
						boolean lastTokenIsNewLine = false;
						while(tokenizer.hasMoreTokens()) {
							String token = tokenizer.nextToken();
							if(!"#NULL".equals(token)) {
								if("\n".equals(token)) {
									if(lastTokenIsNewLine) {
										newValues.add("");
									}
									lastTokenIsNewLine = true;
								} else if("\r".equals(token)) {
									// Skip
								} else {
									newValues.add(token);
									lastTokenIsNewLine = false;
								}
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
							// String
							if(valueHolder instanceof TextValue) {
								// single-valued
								if(valueHolder.isSingleValued()) {
									// cat all values into one string
									String multiLineString = parameterValues.isEmpty() 
										? "" 
										: (String)parameterValues.get(0);
									String mappedNewValue = multiLineString.isEmpty() ? null : multiLineString;
									// Mandatory attributes must not be set to null
									mappedNewValue = valueHolder.isOptionalValued() || mappedNewValue != null 
										? mappedNewValue 
										: "";
									if(target instanceof RefObject) {
										Object value = this.getValue(
											valueHolder, 
											target, 
											featureName, 
											app
										);
										this.setValue(
											valueHolder, 
											target, 
											featureName, 
											value instanceof Collection 
												? Collections.singletonList(mappedNewValue) 
												: mappedNewValue, 
											app
										);
										modifiedFeatures.add(featureName);
									} else {
										targetAsValueMap(target).put(
											featureName,
											mappedNewValue
										);
									}
								} else {
									// multi-valued
									Collection<Object> values;
									if(target instanceof RefObject) {
										values = valueAsCollection(
											this.getValue(
												valueHolder, 
												target, 
												featureName, 
												app
											)
										);
									} else {
										values = valueAsCollection(targetAsValueMap(target).get(featureName));
										if(values == null) {
											targetAsValueMap(target).put(
												featureName,
												values = new ArrayList<>()
											);
										}
									}
									List<String> mappedNewValues = newValues;
									if(target instanceof RefObject) {
										this.setValue(
											valueHolder, 
											target, 
											featureName, 
											mappedNewValues, 
											app
										);
									} else {
										values.clear();
										values.addAll(mappedNewValues);
									}
									modifiedFeatures.add(featureName);
								}
							} else if(valueHolder instanceof NumberValue) {
								// Number
								// single-valued
								if(valueHolder.isSingleValued()) {
									try {    
										BigDecimal number = app.parseNumber(
											newValues.isEmpty() 
												? "" 
												: ((String)newValues.get(0)).trim()
										);
										if(number == null) {
											// Mandatory attributes must not be set to null
											number = valueHolder.isOptionalValued() || number != null 
												? number 
												: BigDecimal.ZERO;
											Object mappedNewValue = null;
											if(target instanceof RefObject) {
												Object value = this.getValue(
													valueHolder, 
													target, 
													featureName, 
													app
												);
												this.setValue(
													valueHolder, 
													target, 
													featureName, 
													value instanceof Collection 
														? Collections.singletonList(mappedNewValue) 
														: mappedNewValue, 
													app
												);
												modifiedFeatures.add(featureName);
											} else {
												targetAsValueMap(target).put(
													featureName,
													mappedNewValue
												);
											}
										} else if(PrimitiveTypes.INTEGER.equals(featureTypeName)) {
											Integer mappedNewValue = number.intValue();
											if(target instanceof RefObject) {
												Object value = this.getValue(
													valueHolder, 
													target, 
													featureName, 
													app
												);
												this.setValue(
													valueHolder, 
													target, 
													featureName, 
													value instanceof Collection 
														? Collections.singletonList(mappedNewValue) 
														: mappedNewValue, 
													app
												);
												modifiedFeatures.add(featureName);
											} else {
												targetAsValueMap(target).put(
													featureName,
													mappedNewValue
												);
											}
										} else if(PrimitiveTypes.LONG.equals(featureTypeName)) {
											Long mappedNewValue = number.longValue();
											if(target instanceof RefObject) {
												Object value = this.getValue(
													valueHolder, 
													target, 
													featureName, 
													app
												);
												this.setValue(
													valueHolder, 
													target, 
													featureName, 
													value instanceof Collection 
														? Collections.singletonList(mappedNewValue) 
														: mappedNewValue, 
													app
												);
												modifiedFeatures.add(featureName);
											} else {
												targetAsValueMap(target).put(
													featureName,
													mappedNewValue
												);
											}
										} else if(PrimitiveTypes.DECIMAL.equals(featureTypeName)) {
											BigDecimal mappedNewValue = number;
											if(target instanceof RefObject) {
												Object value = this.getValue(
													valueHolder, 
													target, 
													featureName, 
													app
												);
												this.setValue(
													valueHolder, 
													target, 
													featureName, 
													value instanceof Collection 
														? Collections.singletonList(mappedNewValue) 
														: mappedNewValue, 
													app
												);
												modifiedFeatures.add(featureName);
											} else {
												targetAsValueMap(target).put(
													featureName,
													mappedNewValue
												);
											}
										} else {
											Short mappedNewValue = number.shortValue();
											if(target instanceof RefObject) {
												Object value = this.getValue(
													valueHolder, 
													target, 
													featureName, 
													app
												);
												this.setValue(
													valueHolder, 
													target, 
													featureName, 
													value instanceof Collection 
														? Collections.singletonList(mappedNewValue) 
														: mappedNewValue, 
													app
												);
												modifiedFeatures.add(featureName);
											} else {
												targetAsValueMap(target).put(
													featureName,
													mappedNewValue
												);
											}
										}
									} catch(Exception e) {
										SysLog.detail(e.getMessage(), e.getCause());
										app.addErrorMessage(
											app.getTexts().getErrorTextCanNotEditNumber(),
											new String[]{feature.getLabel(), newValues.get(0), "can not parse number"}
										);
									}
								} else {
									// multi-valued
									Collection<Object> values;
									if(target instanceof RefObject) {
										values = valueAsCollection(
											this.getValue(
												valueHolder, 
												target, 
												featureName, 
												app
											)
										);
									} else {
										values = valueAsCollection(targetAsValueMap(target).get(featureName));
										if(values == null) {
											targetAsValueMap(target).put(
												featureName,
												values = new ArrayList<>()
											);
										}
									}
									List<Object> mappedNewValues = new ArrayList<>();
									for(Iterator<String> j = newValues.iterator(); j.hasNext(); ) {
										try {
											String numberAsString = j.next().trim();
											BigDecimal number = app.parseNumber(numberAsString);
											if(number != null) {
												if(PrimitiveTypes.INTEGER.equals(featureTypeName)) {
													mappedNewValues.add(
														number.intValue()
													);
												} else if(PrimitiveTypes.LONG.equals(featureTypeName)) {
													mappedNewValues.add(
														number.longValue()
													);
												} else if(PrimitiveTypes.DECIMAL.equals(featureTypeName)) {
													mappedNewValues.add(
														number
													);
												} else { // if(PrimitiveTypes.SHORT.equals(featureTypeName)) {
													mappedNewValues.add(
														number.shortValue()
													);
												}
											} else {
												app.addErrorMessage(
													app.getTexts().getErrorTextCanNotEditNumber(),
													new String[]{feature.getLabel(), newValues.get(0), "can not parse number"}
												);
											}
										} catch(Exception e) {
											SysLog.detail(e.getMessage(), e.getCause());
											app.addErrorMessage(
												app.getTexts().getErrorTextCanNotEditNumber(),
												new String[]{feature.getLabel(), newValues.get(0), e.getMessage()}
											);
										}
									}
									if(target instanceof RefObject) {
										this.setValue(
											valueHolder, 
											target, 
											featureName, 
											mappedNewValues, 
											app
										);
									} else {
										values.clear();
										values.addAll(mappedNewValues);
									}
									modifiedFeatures.add(featureName);
								}
							} else if(valueHolder instanceof DateValue) {
								// Date
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
										if(newValues.isEmpty()) {
											Object mappedNewValue = null;
											// Mandatory attributes must not be set to null
											mappedNewValue = valueHolder.isOptionalValued() || mappedNewValue != null
												? mappedNewValue 
												: PrimitiveTypes.DATE.equals(featureTypeName) 
													? #if CLASSIC_CHRONO_TYPES new java.util.Date #else java.time.Instant.ofEpochMilli#endif(0)
													: DefaultPortalExtension.xmlDatatypeFactory().newXMLGregorianCalendar(1, 1, 1, 0, 0, 0, 0, 0);
											if(valueHolder.isOptionalValued()) {
												if(target instanceof RefObject) {
													Object value = this.getValue(
														valueHolder, 
														target, 
														featureName, 
														app
													);
													this.setValue(
														valueHolder, 
														target, 
														featureName, 
														value instanceof Collection 
															? Collections.singletonList(mappedNewValue) 
															: mappedNewValue, 
														app
													);
													modifiedFeatures.add(featureName);
												} else {
													targetAsValueMap(target).put(
														featureName,
														mappedNewValue
													);
												}
											}
										} else {
											String newValue = newValues.get(0);
											#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif mappedNewValue;
											try {
												mappedNewValue = dateTimeParser.parse(newValue)#if !CLASSIC_CHRONO_TYPES .toInstant()#endif;
											} catch(ParseException e) {
												mappedNewValue = dateParser.parse(newValue)#if !CLASSIC_CHRONO_TYPES .toInstant()#endif;
											}                        
											if(mappedNewValue != null) {
												cal.setTime(#if CLASSIC_CHRONO_TYPES mappedNewValue #else Date.from(mappedNewValue) #endif);
												if(cal.get(GregorianCalendar.YEAR) < 100) {
													int currentYear = new GregorianCalendar().get(GregorianCalendar.YEAR);
													int year = cal.get(GregorianCalendar.YEAR);
													cal.add(
														GregorianCalendar.YEAR, 
														100 * (currentYear / 100 - (Math.abs(currentYear % 100 - year % 100) < 50 ? 0 : 1))
													);
												}
												// date
												if(PrimitiveTypes.DATE.equals(featureTypeName)) {
													#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif mappedNewValueDate =
															            #if CLASSIC_CHRONO_TYPES DefaultPortalExtension.xmlDatatypeFactory().newXMLGregorianCalendarDate(
																			cal.get(Calendar.YEAR),
																			cal.get(Calendar.MONTH) + 1,
																			cal.get(Calendar.DAY_OF_MONTH),
																			DatatypeConstants.FIELD_UNDEFINED)
																		#else java.time.LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
																		#endif;
													if(target instanceof RefObject) {
														Object value = this.getValue(
															valueHolder, 
															target, 
															featureName, 
															app
														);
														this.setValue(
															valueHolder, 
															target, 
															featureName, 
															value instanceof Collection 
																? Collections.singletonList(mappedNewValueDate) 
																: mappedNewValueDate, 
															app
														);
														modifiedFeatures.add(featureName);
													} else {
														targetAsValueMap(target).put(
															featureName,
															mappedNewValueDate
														);
													}
												} else if(PrimitiveTypes.DATETIME.equals(featureTypeName)) {
													// dateTime
													mappedNewValue = cal.getTime()#if !CLASSIC_CHRONO_TYPES .toInstant()#endif;
													if(target instanceof RefObject) {
														Object value = this.getValue(
															valueHolder, 
															target, 
															featureName, 
															app
														);
														this.setValue(
															valueHolder, 
															target, 
															featureName, 
															value instanceof Collection 
																? Collections.singletonList(mappedNewValue) 
																: mappedNewValue, 
															app
														);
														modifiedFeatures.add(featureName);
													} else {
														targetAsValueMap(target).put(
															featureName,
															mappedNewValue
														);
													}
												} else {
													app.addErrorMessage(
														app.getTexts().getErrorTextCanNotEditDate(),
														new String[]{feature.getLabel(), featureTypeName, "date type not supported"}
													);
												}
											} else {
												app.addErrorMessage(
													app.getTexts().getErrorTextCanNotEditDate(),
													new String[]{feature.getLabel(), newValues.get(0), "can not parse date"}
												);
											}
										}
									} catch(Exception e) {
										SysLog.detail(e.getMessage(), e.getCause());
										app.addErrorMessage(
											app.getTexts().getErrorTextCanNotEditDate(),
											new String[]{feature.getLabel(), newValues.get(0), e.getMessage()}
										);
									}
								} else {
									// multi-valued
									Collection<Object> values;
									if(target instanceof RefObject) {
										values = valueAsCollection(
											this.getValue(
												valueHolder, 
												target, 
												featureName, 
												app
											)
										);
									} else {
										values = valueAsCollection(targetAsValueMap(target).get(featureName));
										if(values == null) {
											targetAsValueMap(target).put(
												featureName,
												values = new ArrayList<Object>()
											);
										}
									}
									List<Object> mappedNewValues = new ArrayList<Object>();
									for(Iterator<String> j = newValues.iterator(); j.hasNext(); ) {
										try {
											String newValue = j.next();
											#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif dateTime = null;
											try {
												dateTime = dateTimeParser.parse(newValue)#if !CLASSIC_CHRONO_TYPES .toInstant()#endif;
											} catch(ParseException e) {
												dateTime = dateParser.parse(newValue)#if !CLASSIC_CHRONO_TYPES .toInstant()#endif;
											}
											if(dateTime != null) {
												cal.setTime(#if CLASSIC_CHRONO_TYPES dateTime #else Date.from(dateTime) #endif);
												if(PrimitiveTypes.DATE.equals(featureTypeName)) {
													#if CLASSIC_CHRONO_TYPES
													final java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
													javax.xml.datatype.XMLGregorianCalendar date = org.w3c.spi.DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
														calendar.get(java.util.Calendar.YEAR),
														calendar.get(java.util.Calendar.MONTH) + 1,
														calendar.get(java.util.Calendar.DAY_OF_MONTH),
														javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED
													);
													#else
													java.time.LocalDate date = java.time.LocalDate.now();
													#endif
													mappedNewValues.add(date);
												} else if(PrimitiveTypes.DATETIME.equals(featureTypeName)) {
													mappedNewValues.add(dateTime);
												} else {
													app.addErrorMessage(
														app.getTexts().getErrorTextCanNotEditDate(),
														new String[]{feature.getLabel(), featureTypeName, "date type not supported"}
													);
												}
											} else {
												app.addErrorMessage(
													app.getTexts().getErrorTextCanNotEditDate(),
													new String[]{feature.getLabel(), newValues.get(0), "can not parse date"}
												);
											}
										} catch(Exception e) {
											SysLog.detail(e.getMessage(), e.getCause());
											app.addErrorMessage(
												app.getTexts().getErrorTextCanNotEditDate(),
												new String[]{feature.getLabel(), newValues.get(0), e.getMessage()}
											);
										}
									}
									if(target instanceof RefObject) {
										this.setValue(
											valueHolder, 
											target, 
											featureName, 
											mappedNewValues, 
											app
										);
									} else {
										values.clear();
										values.addAll(mappedNewValues);
									}
									modifiedFeatures.add(featureName);
								}
							} else if(valueHolder instanceof ObjectReferenceValue) {
								// object reference
								if(!((String)key).endsWith(".Title")) {
									// single-valued
									if(valueHolder.isSingleValued()) {
										String xri = null;
										Object[] titleValues = (Object[])parameterMap.get(key + ".Title");
										// xri of referenced object entered (manually) as title. If set
										// and valid this overrides xri set in field (by lookup inspector).
										// If set and invalid and newValues is empty report an error
										boolean xriSetAsTitleIsInvalid = false;
										if((titleValues != null) && (titleValues.length > 0)) {
											if(titleValues[0].toString().isEmpty()) {
												xri = ""; // reference removed by user
											} else {
												try {
													URL titleUrl = new URL((String)(titleValues[0]));
													if("xri".equals(titleUrl.getProtocol())) {
														xri = (String)titleValues[0];
													} else {
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
												} catch (MalformedURLException | UnsupportedEncodingException e) {
													xriSetAsTitleIsInvalid = true;
												}
                                            }
										}
										// xri entered as title is valid
										if(xriSetAsTitleIsInvalid && newValues.isEmpty()) {
											// title N/A (object not available) and N/P (no permission) is set by show object. Ignore.
											if(!((String)titleValues[0]).startsWith("N/A") && !((String)titleValues[0]).startsWith("N/P")) {
												app.addErrorMessage(
													app.getTexts().getErrorTextInvalidObjectReference(),
													new String[]{feature.getLabel(), (String)titleValues[0]}
												);
											}
										} else {
											// xri entered as title is either valid or xri is set in field
											if((xri == null) && (!newValues.isEmpty())) {
												xri = (String)newValues.get(0);
											}
											try {
												Object mappedNewValue = (xri == null) || "".equals(xri) 
													? null 
													: new Path(xri);
												// Mandatory attributes must not be set to null
												if(valueHolder.isOptionalValued() || mappedNewValue != null) {
													if(target instanceof RefObject) {
														mappedNewValue = mappedNewValue == null 
															? null 
															: JDOHelper.getPersistenceManager(target).getObjectById(mappedNewValue);
														this.setValue(
															valueHolder, 
															target, 
															featureName, 
															mappedNewValue, 
															app
														);
														modifiedFeatures.add(featureName);
													} else {
														targetAsValueMap(target).put(
															featureName,
															mappedNewValue
														);
													}
												}
											} catch(Exception e) {
												SysLog.detail(e.getMessage(), e.getCause());
												app.addErrorMessage(
													app.getTexts().getErrorTextCanNotEditObjectReference(),
													new String[]{feature.getLabel(), newValues.get(0), e.getMessage()}
												);
											}
										}
									} else {
										// multi-valued
										// not supported yet
									}
								}
							} else if(valueHolder instanceof CodeValue) {
								// Code
								// Single-valued
								if(valueHolder.isSingleValued()) {
									try {
										Short mappedNewValue = null;
										try {
											mappedNewValue = newValues.isEmpty() 
												? (short)0 
												: Short.parseShort(newValues.get(0));
										} catch(Exception ignore) {}
										// Mandatory attributes must not be set to null
										if(mappedNewValue != null) {
											if(target instanceof RefObject) {
												Object value = this.getValue(
													valueHolder, 
													target, 
													featureName, 
													app
												);
												this.setValue(
													valueHolder, 
													target, 
													featureName, 
													value instanceof Collection ? Collections.singletonList(mappedNewValue) : mappedNewValue, 
														app
												);
												modifiedFeatures.add(featureName);
											} else {
												targetAsValueMap(target).put(
													featureName,
													mappedNewValue
												);
											}
										} else {
											SysLog.warning("Unable to map code field", newValues.get(0));
										}
									} catch(Exception e) {
										SysLog.detail(e.getMessage(), e.getCause());
										app.addErrorMessage(
											app.getTexts().getErrorTextCanNotEditCode(),
											new String[]{feature.getLabel(), newValues.get(0), e.getMessage()}
										);
									}
								} else {
    								// multi-valued
									Collection<Object> values;
									if(target instanceof RefObject) {
										values = valueAsCollection(
											this.getValue(
												valueHolder, 
												target, 
												featureName, 
												app
											)
										);
									} else {
										values = valueAsCollection(targetAsValueMap(target).get(featureName));
										if(values == null) {
											targetAsValueMap(target).put(
												featureName,
												values = new ArrayList<>()
											);
										}
									}
									List<Object> mappedNewValues = new ArrayList<>();
									for(Iterator<String> j = newValues.iterator(); j.hasNext(); ) {
										try {
											String codeAsString = j.next();
											if(!codeAsString.isEmpty()) {
												mappedNewValues.add(Short.parseShort(codeAsString));
											}
										} catch(Exception e) {
											SysLog.detail(e.getMessage(), e.getCause());
											app.addErrorMessage(
												app.getTexts().getErrorTextCanNotEditCode(),
												new String[]{feature.getLabel(), newValues.get(0), e.getMessage()}
											);
										}
									}
									if(target instanceof RefObject) {
										this.setValue(
											valueHolder, 
											target, 
											featureName, 
											mappedNewValues, 
											app
										);
									} else {
										values.clear();
										values.addAll(mappedNewValues);
									}
									modifiedFeatures.add(featureName);
								}
							} else if(valueHolder instanceof BooleanValue) {
								// Boolean        
								// single-valued
								if(valueHolder.isSingleValued()) {
									Boolean mappedNewValue =
										!newValues.isEmpty() &&
												("true".equals(newValues.get(0)) ||
														"on".equals(newValues.get(0)) ||
														app.getTexts().getTrueText().equals(newValues.get(0)));
									// Mandatory attributes must not be null
									mappedNewValue = valueHolder.isOptionalValued() || mappedNewValue != null 
										? mappedNewValue 
										: Boolean.FALSE;
									if(target instanceof RefObject) {
										Object value = this.getValue(
											valueHolder, 
											target, 
											featureName, 
											app
										);
										this.setValue(
											valueHolder, 
											target,
											featureName, 
											value instanceof Collection 
												? Collections.singletonList(mappedNewValue) 
												: mappedNewValue, 
											app
										);
										modifiedFeatures.add(featureName);
									} else {
										targetAsValueMap(target).put(
											featureName,
											mappedNewValue
										);
									}
								} else {
									// Multi-valued
									Collection<Object> values;
									if(target instanceof RefObject) {
										values = valueAsCollection(
											this.getValue(
												valueHolder, 
												target, 
												featureName, 
												app
											)
										);
									} else {
										values = valueAsCollection(targetAsValueMap(target).get(featureName));
										if(values == null) {
											targetAsValueMap(target).put(
												featureName,
												values = new ArrayList<>()
											);
										}
									}
									List<Object> mappedNewValues = new ArrayList<>();
									for(Iterator<String> j = newValues.iterator(); j.hasNext(); ) {
										Object mappedNewValue = j.next();
										mappedNewValues.add(
											"true".equals(mappedNewValue) ||
													"on".equals(mappedNewValue) ||
													app.getTexts().getTrueText().equals(mappedNewValue)
										);
									}
									if(target instanceof RefObject) {
										this.setValue(
											valueHolder, 
											target, 
											featureName, 
											mappedNewValues, 
											app
										);
									} else {
										values.clear();
										values.addAll(mappedNewValues);
									}
									modifiedFeatures.add(featureName);
								}
							} else if(valueHolder instanceof BinaryValue) {
								// Binary
								String fileNameInfo = app.getTempFileName("" + key, ".INFO");
								// single-valued
								if(valueHolder.isSingleValued()) {
									// reset value to null
									if(newValues.isEmpty()) {
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
											} else {
												targetAsValueMap(target).put(
													featureName,
													null
												);
											}
										} catch(Exception ignore) {}        
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
											} else {
												targetAsValueMap(target).put(
													featureName + "Name",
													null
												);
											}
										} catch(Exception ignore) {}        
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
											} else {
												targetAsValueMap(target).put(
													featureName + "MimeType",
													null
												);
											}
										} catch(Exception ignore) {}
									} else {
										// get binary stream and store
										modifiedFeatures.add(featureName);
										boolean uploadStreamValid = true;
										// get mimeType, name from .INFO
										try {
										    final String mimeType;
										    final String name;
										    try(
    											BufferedReader reader = new BufferedReader(
													new InputStreamReader(new FileInputStream(fileNameInfo))
												)
    										){
    											mimeType = reader.readLine();
    											name = reader.readLine();
										    }
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
												} else {
													targetAsValueMap(target).put(
														featureName + "MimeType",
														mimeType
													);
												}
											} catch(Exception e) {
												SysLog.warning("can not set mimeType for " + featureName);
									            Throwables.log(e);
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
												} else {
													targetAsValueMap(target).put(
														featureName + "Name",
														name
													);
												}
											} catch(Exception e) {
												SysLog.warning("can not set name for " + featureName);
									            Throwables.log(e);
											}
										} catch(FileNotFoundException e) {
											SysLog.error("can not open info of uploaded stream " + fileNameInfo);
								            Throwables.log(e);
											uploadStreamValid = false;
										} catch(IOException e) {
											SysLog.error("can not read info of uploaded stream " + fileNameInfo);
								            Throwables.log(e);
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
												} catch(Exception e) {
													SysLog.error("Unable to upload binary content", location);
										            Throwables.log(e);
												}
											} else {
												try {
													final byte[] bytes;
													try(
    													InputStream is = new FileInputStream(location);
    													ByteArrayOutputStream os = new ByteArrayOutputStream();
													){
    													int b = 0;
    													while((b = is.read()) != -1) {
    														os.write(b);
    													}
    													bytes = os.toByteArray();
													}
													targetAsValueMap(target).put(
														featureName,
														bytes
													);
												} catch(Exception e) {
													SysLog.error("Unable to upload binary content", location);
										            Throwables.log(e);
												}
											}
										}
									}
								} else {
									// multi-valued
									SysLog.error("multi-valued binary not supported for", featureName);
								}
							} else {
								// unknown
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
    				Object value = target instanceof RefObject 
    					? this.getValue(feature.getValue(), target, feature.getName(), app) 
    					: targetAsValueMap(target).get(feature.getName());
					if(
						(value == null) || 
						(value instanceof String && ((String)value).length() == 0) || 
						(value instanceof Collection && ((Collection<?>)value).isEmpty())
					) {
						app.addErrorMessage(
							app.getTexts().getErrorTextMandatoryField(),
							new String[]{feature.getLabel()}
						);
					}
    			}
    		} catch(Exception ignore) {}
    	}
    }

	/**
	 * Store object. Edit object in case of doCreate=false. Create new object if
	 * doCreate=true and add to container parent.refGetValue(forReference).
	 * 
	 * @param parameterMap
	 * @param attributeMap
	 * @throws ServiceException
	 */
    @Override
	public boolean storeObject(
		RefObject_1_0 parent,
		RefObject_1_0 object,
	    Map<String,String[]> parameterMap,
	    Map<String, Attribute> attributeMap,
	    boolean doCreate,
	    String forReference,
	    ApplicationContext app
	) throws ServiceException {
		if(doCreate) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(parent);
			Transaction tx = pm.currentTransaction();
			synchronized(tx) {
				try {
					tx.begin();
					this.updateObject(
						object,
					    parameterMap,
					    attributeMap,
					    app
					);
					if(app.getErrorMessages().isEmpty()) {
						Object[] qualifiers = (Object[])parameterMap.get("qualifier");
						if(qualifiers == null) {
							qualifiers = new String[] {
								UUIDConversion.toUID(UUIDs.newUUID())
							};
						}
						// Prevent CONCURRENT_MODIFICATION in case the parent was updated by some other user
						pm.refresh(parent);
						DataBinding dataBinding = null;
						try {
							ElementDefinition elementDefinition = app.getUiElementDefinition(
								parent.refClass().refMofId() + ":" + forReference
							);
							dataBinding = elementDefinition == null 
								? null 
								: elementDefinition.getDataBindingName() == null 
									? null
									: this.getDataBinding(elementDefinition.getDataBindingName());
						} catch(Exception ignore) {}
						if(dataBinding != null) {
							dataBinding.setValue(
								parent, 
								forReference, 
								object,
								app
							);
						} else {
							Object container = parent.refGetValue(forReference);
							((RefContainer<RefObject>)container).refAdd(
							    org.oasisopen.cci2.QualifierType.REASSIGNABLE,
							    qualifiers.length > 0 ? (String) qualifiers[0] : "",
							    object
							);
						}
						tx.commit();
						return true;
					} else {
						try {
							tx.rollback();
						} catch(Exception e1) {}
					}
				} catch(Exception e) {
					try {
						tx.rollback();
			        } catch(Exception ignore) {
						SysLog.trace("Exception ignored", ignore);
					}
					throw new ServiceException(e);
				}
			}
		} else {
			PersistenceManager pm = JDOHelper.getPersistenceManager(object);
			Transaction tx = pm.currentTransaction();
			synchronized(tx) {
				try {
					tx.begin();
					app.getPortalExtension().updateObject(
						object,
					    parameterMap,
					    attributeMap,
					    app
					);
					if(app.getErrorMessages().isEmpty()) {
						tx.commit();
						return true;
					} else {
						try {
							tx.rollback();
						} catch(Exception ignore) {}
					}
				} catch(Exception e) {
					try {
						tx.rollback();
					} catch(Exception e1) {}
					throw new ServiceException(e);
				}
			}
		}
		return false;
	}

    /**
     * Returns classes which are in the composition hierarchy of
     * the specified type. Returns a map with the class name as
     * key and a set of reference names as members, whereas the
     * references are composite references of the class.
     * 
     * @param ofType
     * @param hierarchy
     * @throws ServiceException
     */
    protected void createCompositionHierarchy(
    	ModelElement_1_0 ofType,
    	Map<String,Set<String>> hierarchy
    ) throws ServiceException {
    	Model_1_0 model = ofType.getModel();        
    	// add ofType to hierarchy
    	String ofTypeName = ofType.getQualifiedName();
    	if(hierarchy.get(ofTypeName) != null) {
    		return;
    	}
		hierarchy.put(
			ofTypeName,
			new HashSet<String>()
		);   
    	// get all types which are involved in composition hierarchy
    	List<ModelElement_1_0> typesToCheck = new ArrayList<ModelElement_1_0>();
    	if(ofType.objGetValue("compositeReference") != null) {
    		typesToCheck.add(ofType);
    	} else {
    		for(Iterator<Object> i = ofType.objGetList("allSubtype").iterator(); i.hasNext(); ) {
    			ModelElement_1_0 subtype = model.getElement(i.next());
    			if(
    				!ofType.getQualifiedName().equals(subtype.getQualifiedName()) &&
    				subtype.objGetValue("compositeReference") != null
    			) {
    				typesToCheck.add(subtype);
    			}
    		}
    	}
    	for(Iterator<ModelElement_1_0> i = typesToCheck.iterator(); i.hasNext(); ) {
    		ModelElement_1_0 type = i.next();
    		ModelElement_1_0 compositeReference = model.getElement((Path)type.objGetValue("compositeReference"));
    		ModelElement_1_0 exposingType = model.getElement(compositeReference.getContainer());
    		this.createCompositionHierarchy(
    			exposingType,
    			hierarchy
    		);
    		hierarchy.get(
    			exposingType.getQualifiedName()
    		).add(
                    compositeReference.getName()
    		);
    	}
    }

    /**
     * Return true if referenceDef is a lookup reference for the given lookup type. Override
     * for custom-specific behavior. By default a reference is a lookup reference if its
     * composition type is either composite or shared and if the referenced type is a super 
     * type of the lookup type. 
     *
     * @param referenceDef
     * @param lookupType
     * @return
     * @throws ServiceException
     */
    protected boolean isLookupReference(
    	ModelElement_1_0 referenceDef,
    	ModelElement_1_0 lookupType
    ) throws ServiceException {
    	Model_1_0 model = referenceDef.getModel();
		ModelElement_1_0 referencedType = model.getElement(referenceDef.getType());
    	return
			(ModelHelper.isCompositeEnd(referenceDef, false) || ModelHelper.isSharedEnd(referenceDef, false)) &&
			!"org:openmdx:base:ExtentCapable".equals(referencedType.getQualifiedName()) &&
			model.isSubtypeOf(lookupType, referencedType);
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getLookupObject(org.openmdx.model1.accessor.basic.cci.ModelElement_1_0, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, org.openmdx.portal.servlet.ApplicationContext, javax.jdo.PersistenceManager)
     */
    @Override
    public RefObject_1_0 getLookupObject(
    	ModelElement_1_0 lookupType,
    	RefObject_1_0 startFrom,
    	ApplicationContext app
    ) throws ServiceException {
    	Model_1_0 model = app.getModel();
    	PersistenceManager pm = JDOHelper.getPersistenceManager(startFrom);
    	String qualifiedNameLookupType = lookupType.getQualifiedName();
    	ModelElement_1_0 startFromType = model.getElement(startFrom.refClass().refMofId());
    	// First check whether startFrom has a reference with type lookupType
    	{
    		for(Object reference: startFromType.objGetMap("reference").values()) {
    			ModelElement_1_0 referenceDef = (ModelElement_1_0)reference;
    			if(this.isLookupReference(referenceDef, lookupType)) {
    				return startFrom;
    			}
    		}
    	}
    	// Second try to find the start object in the composition hierarchy of the lookup type
    	{
	    	Map<String,Set<String>> compositionHierarchy = new HashMap<String,Set<String>>();
	    	this.createCompositionHierarchy(
	    		lookupType,
	    		compositionHierarchy
	    	);
	    	RefObject_1_0 objectToShow = null;        
	    	// get object to show. This is the first object which is member
	    	// of the composition hierarchy of the referenced object.
	    	RefObject_1_0 current = startFrom;
	    	while(true) {
	    		for(
	    			Iterator<String> i = compositionHierarchy.keySet().iterator(); 
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
	    		} catch(Exception e) {
	    			SysLog.warning("Can not get object", Arrays.asList((Object)currentIdentity, e.getMessage()));
	    			break;
	    		}
	    	}   
	    	// If not found get root object which is in the composition hierarchy
	    	if(objectToShow == null) {
	    		RefObject[] rootObject = app.getRootObject();
	    		for(int i = 0; i < rootObject.length; i++) {
	    			for(Iterator<String> j = compositionHierarchy.keySet().iterator(); j.hasNext(); ) {
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
	    		objectToShow = (RefObject_1_0)app.getRootObject()[0];
	    	}            
	    	return objectToShow;
    	}
    }
      
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getLookupView(java.lang.String, org.openmdx.base.mof.cci.ModelElement_1_0, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public ObjectView getLookupView(
        String id,
        ModelElement_1_0 lookupType,
        RefObject_1_0 startFrom,
        String filterValues,
        ApplicationContext app
    ) throws ServiceException {
        RefObject_1_0 lookupObject = this.getLookupObject(
            lookupType, 
            startFrom, 
            app
        );
        String qualifiedNameLookupType = lookupType.getQualifiedName();
        ObjectView view = new ShowObjectView(
            id,
            null,
            (RefObject_1_0)app.getNewPmData().getObjectById(lookupObject.refGetPath()),
            app,
			new LinkedHashMap<>(),
            null, // no nextPrevActions
            qualifiedNameLookupType,
            null, // resourcePathPrefix
            null, // navigationTarget
            null // isReadOnly
        );
        return view;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#hasUserDefineableQualifier(org.openmdx.ui1.jmi1.Inspector, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public boolean hasUserDefineableQualifier(
        org.openmdx.ui1.jmi1.Inspector inspector,
        ApplicationContext app
    ) {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#showGridContentOnInit(org.openmdx.portal.servlet.control.GridControl, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public boolean showGridContentOnInit(
        UiGridControl gridControl,
        ApplicationContext app
    ) {
        String propertyName = gridControl.getPropertyName(
            gridControl.getQualifiedReferenceName(),
            UserSettings.SHOW_ROWS_ON_INIT.getName()
        );
        return app.getSettings().getProperty(propertyName) != null
            ? Boolean.valueOf(app.getSettings().getProperty(propertyName)).booleanValue()
            : true;        
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#renderTextValue(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
     */
    @Override
    public void renderTextValue(
        ViewPort p,
        AttributeValue attributeValue,
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
	            Iterator<String> i = WELL_KNOWN_PROTOCOLS.iterator();
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
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getDateStyle(java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public int getDateStyle(
       String qualifiedFeatureName,
       ApplicationContext app
    ) {
        return java.text.DateFormat.SHORT;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getTimeStyle(java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public int getTimeStyle(
       String qualifiedFeatureName,
       ApplicationContext app
    ) {
        return java.text.DateFormat.MEDIUM;        
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getTimeZone(java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public TimeZone getTimeZone(
        String qualifiedFeatureName,
        ApplicationContext app
    ) {
    	return TimeZone.getTimeZone(app.getCurrentTimeZone());    	
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getDataBinding(java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public DataBinding getDataBinding(
       String dataBindingName
    ) {
        if((dataBindingName != null) && dataBindingName.startsWith(CompositeObjectDataBinding.class.getName())) {
            return new CompositeObjectDataBinding(
                dataBindingName.indexOf("?") < 0 ? "" : dataBindingName.substring(dataBindingName.indexOf("?") + 1)
            );
        } else if((dataBindingName != null) && dataBindingName.startsWith(JoiningListDataBinding.class.getName())) {
	        return new JoiningListDataBinding(
	            dataBindingName.indexOf("?") < 0 ? "" : dataBindingName.substring(dataBindingName.indexOf("?") + 1)            	
	        );
        } else if(ReferencedObjectDataBinding.class.getName().equals(dataBindingName)) {
            return new ReferencedObjectDataBinding();
        } else {
            return new DefaultDataBinding();
        }
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.PortalExtension_1_0#getDefaultCssClassFieldGroup(org.openmdx.portal.servlet.attribute.AttributeValue, org.openmdx.portal.servlet.ApplicationContext)
	 */
	@Override
	public String getDefaultCssClassFieldGroup(
		AttributeValue attributeValue,
		ApplicationContext app
	) {
		return AttributeValue.DEFAULT_CSS_CLASS.toString();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.PortalExtension_1_0#getDefaultCssClassObjectContainer(org.openmdx.portal.servlet.attribute.AttributeValue, org.openmdx.portal.servlet.ApplicationContext)
	 */
	@Override
	public String getDefaultCssClassObjectContainer(
		AttributeValue attributeValue, 
		ApplicationContext app
	) {
		return "";
	}

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#handleOperationResult(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, java.lang.String, javax.jmi.reflect.RefStruct, javax.jmi.reflect.RefStruct)
     */
    public RefObject_1_0 handleOperationResult(
        RefObject_1_0 target, 
        String operationName, 
        RefStruct params, 
        RefStruct result
    ) throws ServiceException {
        return null;
    }

	/* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getNewUserRole(org.openmdx.portal.servlet.ApplicationContext, org.openmdx.base.naming.Path)
     */
    @Override
    public String getNewUserRole(
    	ApplicationContext app, 
    	Path requestedObjectIdentity
    ) {
    	// Return new user role depending on the segment name of the requested object: principal@segment.
	    return app.getCurrentUserRole().substring(0, app.getCurrentUserRole().indexOf("@") + 1) + requestedObjectIdentity.getSegment(4).toClassicRepresentation();
    }

	/* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getGridActions(org.openmdx.portal.servlet.view.Grid)
     */
    @Override
    public List<Action> getGridActions(
    	ObjectView view,
    	Grid grid
    ) throws ServiceException {
    	return Collections.<Action>emptyList();
    }
    
    /**
     * Create new instance of action factory. Override for
     * custom-specific implementation.
     * 
     * @return
     */
    protected ActionFactory newActionFactory(
    ) {
    	return new DefaultActionFactory();
    }
        
	/* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getActionFactory()
     */
    @Override
    public ActionFactory getActionFactory(
    ) {
    	if(this.actionFactory == null) {
    		this.actionFactory = newActionFactory();
    	}
    	return this.actionFactory;
    }

    /**
     * Create new instance of control factory. Override for
     * custom-specific implemenation.
     * 
     * @return
     */
    protected ControlFactory newControlFactory(
    ) {
    	return new DefaultControlFactory();
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getControlFactory()
     */
    @Override
    public ControlFactory getControlFactory(
    ) {
    	if(this.controlFactory == null) {
    		this.controlFactory = newControlFactory();
    	}
    	return this.controlFactory;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#checkPrincipal(org.openmdx.base.naming.Path, java.lang.String, javax.jdo.PersistenceManager)
     */
    @Override
    public boolean checkPrincipal(
        Path realmIdentity,
        String principalName,
        PersistenceManager pm
    ) throws ServiceException {
    	return false;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getUserRoles(org.openmdx.base.naming.Path, java.lang.String, javax.jdo.PersistenceManager)
     */
    @Override
    public List<String> getUserRoles(
        Path realmIdentity,
        String principalName,
        PersistenceManager pm
    )  throws ServiceException {
    	return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getAdminPrincipal(java.lang.String)
     */
    @Override
    public String getAdminPrincipal(
        String realmName
    ) {
        return ADMIN_PRINCIPAL_PREFIX + realmName;
    }
  
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#isRootPrincipal(java.lang.String)
     */
    @Override
    public boolean isRootPrincipal(
        String principalName
    ) {
        return principalName.startsWith(ROOT_PRINCIPAL_NAME);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#setLastLoginAt(org.openmdx.base.naming.Path, java.lang.String, java.lang.String, javax.jdo.PersistenceManager)
     */
    @Override
    public void setLastLoginAt(
    	Path realmIdentity,
    	String segmentName,
    	String principalName,
    	PersistenceManager pm    	
    ) throws ServiceException {
    	// no op
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getAutostartUrl(org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public String getAutostartUrl(
    	HttpSession session,
    	ApplicationContext app
    ) {
        return app.getSettings().getProperty(UserSettings.AUTOSTART_URL.getName());
    }

    /**
     * DefaultConditionParser
     *
     */
    public static class DefaultQueryConditionParser implements QueryConditionParser {

		private int offset;
    	private final String qualifiedFeatureName;
    	private final Condition defaultCondition;
		
    	public DefaultQueryConditionParser(
        	final String qualifiedFeatureName,
        	final Condition defaultCondition
        ) {
    		this.offset = 0;
    		this.qualifiedFeatureName = qualifiedFeatureName;
    		this.defaultCondition = defaultCondition;
    	}

    	@Override
    	public Condition parse(
    		String token
    	) {
    		String featureName = this.qualifiedFeatureName.substring(this.qualifiedFeatureName.lastIndexOf(":") + 1);
    		if(token.startsWith(">=")) {
    			this.offset = 2;
    			return
    				new IsGreaterOrEqualCondition(
    					Quantifier.THERE_EXISTS,
    					featureName,
    					true,
    					new Object[]{}
    				);
    		} else if(token.startsWith("<=")) {
    			this.offset = 2;
    			return new IsGreaterCondition(
    				Quantifier.THERE_EXISTS,
    				featureName,
    				false,
    				new Object[]{}
    			);
    		} else if(token.startsWith("<>")) {
    			this.offset = 2;
    			return new IsInCondition(
    				Quantifier.THERE_EXISTS,
    				featureName,
    				false,
    				new Object[]{}
    			);
    		} else if(token.startsWith("<")) {
    			this.offset = 1;
    			return new IsGreaterOrEqualCondition(
    				Quantifier.THERE_EXISTS,
    				featureName,
    				false,
    				new Object[]{}
    			);
    		} else if(token.startsWith(">")) {
    			this.offset = 1;
    			return new IsGreaterCondition(
    				Quantifier.THERE_EXISTS,
    				featureName,
    				true,
    				new Object[]{}
    			);
    		} else if(token.startsWith("*")) {
    			this.offset = 1;
    			return new SoundsLikeCondition(
    				Quantifier.THERE_EXISTS,
    				featureName,
    				true,
    				new Object[]{}
    			);
    		} else if(token.startsWith("!*")) {
    			this.offset = 2;
    			return new SoundsLikeCondition(
    				Quantifier.THERE_EXISTS,
    				featureName,
    				false,
    				new Object[]{}
    			);
    		} else if(token.startsWith("%")) {
    			this.offset = 1;
    			return new IsLikeCondition(
    				Quantifier.THERE_EXISTS,
    				featureName,
    				true,
    				new Object[]{}
    			);
    		} else if(token.startsWith("!%")) {
    			this.offset = 2;
    			return new IsLikeCondition(
    				Quantifier.THERE_EXISTS,
    				featureName,
    				false,
    				new Object[]{}
    			);
    		} else if(token.startsWith("=")) {
    			this.offset = 1;
    			return new IsInCondition(
    				Quantifier.THERE_EXISTS,
    				featureName,
    				true,
    				new Object[]{}
    			);
    		} else if(token.startsWith("!=")) {
    			this.offset = 2;
    			return new IsInCondition(
    				Quantifier.THERE_EXISTS,
    				featureName,
    				false,
    				new Object[]{}
    			);
    		} else {
    			this.offset = 0;
    			return this.defaultCondition;
    		}
    	}

    	@Override
    	public int getOffset(
    	) {
    		return this.offset;
    	}

    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.PortalExtension_1_0#getConditionParser(org.openmdx.ui1.jmi1.ValuedField, org.openmdx.base.query.Condition)
     */
    @Override
    public QueryConditionParser getQueryConditionParser(
    	final String qualifiedFeatureName,
    	final Condition defaultCondition
    ) {
    	return new DefaultQueryConditionParser(
    		qualifiedFeatureName,
    		defaultCondition
    	);
    }

    /**
     * CodeSearchFieldDef
     *
     */
    static class CodeSearchFieldDef extends SearchFieldDef {

    	public CodeSearchFieldDef(
    		String qualifiedReferenceName,
    		String featureName,
    		String codeValueContainerName
    	) {
    		super(qualifiedReferenceName, featureName);
    		this.codeValueContainerName = codeValueContainerName;
    	}
    	
		@Override
		public List<String> findValues(
			Object object,
			String pattern,
			ApplicationContext app
		) throws ServiceException {
			Map<Short,String> codeTexts = app.getCodes().getLongTextByCode(this.codeValueContainerName, app.getCurrentLocaleAsIndex(), true);
			Set<String> values = new TreeSet<String>();
			int count = 0;
			for(String codeText: codeTexts.values()) {
				if(
					pattern != null && 
					!pattern.isEmpty() && 
					(codeText.toLowerCase().indexOf(pattern.toLowerCase()) >= 0 || "*".equals(pattern))
				) {
					values.add(codeText);
					count++;
					if(count > 30) break;
				}
			}
			return new ArrayList<String>(values);
		}

		private final String codeValueContainerName;

    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.PortalExtension_1_0#getSearchFieldDef(java.lang.Object, java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
	 */
	@Override
	public SearchFieldDef getSearchFieldDef(
		String qualifiedReferenceName,
		String featureName,
		ApplicationContext app
	) throws ServiceException {		
		if(
			qualifiedReferenceName != null &&
			featureName != null &&
			app.getCodes() != null
		) {
			Model_1_0 model = app.getModel();
			ModelElement_1_0 referenceDef = null;
			try {
				referenceDef = model.getElement(qualifiedReferenceName);
			} catch(Exception ignore) {
				SysLog.trace("Exception ignored", ignore);
			}
			if(referenceDef != null && referenceDef.isReferenceType()) {
				ModelElement_1_0 referencedType = model.getElement(referenceDef.getType());
				if(referencedType != null && referencedType.isClassType()) {
					List<ModelElement_1_0> types = new ArrayList<ModelElement_1_0>();
					for(Object subtype: referencedType.objGetList("allSupertype")) {
						types.add(model.getElement(subtype));
					}					
					for(Object subtype: referencedType.objGetList("allSubtype")) {
						types.add(model.getElement(subtype));
					}
					for(ModelElement_1_0 type: types) {
						String codeValueContainerName = type.getQualifiedName() + ":" + featureName;
						if(app.getCodes().getLongText(codeValueContainerName,  (short)0, true, true) != null) {
							return new CodeSearchFieldDef(
								qualifiedReferenceName, 
								featureName, 
								codeValueContainerName
							);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * @return the servletContext
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * @param servletContext the servletContext to set
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3690195425844146744L;

    protected static final Set<String> WELL_KNOWN_PROTOCOLS = 
        new HashSet<String>(Arrays.asList("http:/", "https:/", "Outlook:", "file:/"));
      
    public static final String ADMIN_PRINCIPAL_PREFIX = "admin-";
    public static final String ROOT_REALM_NAME = "Root";
    public static final String ROOT_PRINCIPAL_NAME = ADMIN_PRINCIPAL_PREFIX + ROOT_REALM_NAME;
    
    /**.
     * @return a Datatype Factory Instance
     */
    protected static DatatypeFactory xmlDatatypeFactory(
    ){
        return MutableDatatypeFactory.xmlDatatypeFactory();
    }

    private ActionFactory actionFactory;
    private ControlFactory controlFactory;
    private ServletContext servletContext;
}
