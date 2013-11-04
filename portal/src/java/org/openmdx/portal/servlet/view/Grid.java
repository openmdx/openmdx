/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Grid
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ExceptionListener;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Extension;
import org.openmdx.base.query.IsBetweenCondition;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsLikeCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.DataBinding;
import org.openmdx.portal.servlet.Filter;
import org.openmdx.portal.servlet.Filters;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.PortalExtension_1_0.QueryConditionParser;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.UserSettings;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.GridAddObjectAction;
import org.openmdx.portal.servlet.action.GridAddOrderAnyAction;
import org.openmdx.portal.servlet.action.GridAddOrderAscendingAction;
import org.openmdx.portal.servlet.action.GridAddOrderDescendingAction;
import org.openmdx.portal.servlet.action.GridMoveDownObjectAction;
import org.openmdx.portal.servlet.action.GridMoveUpObjectAction;
import org.openmdx.portal.servlet.action.GridPageNextAction;
import org.openmdx.portal.servlet.action.GridPagePreviousAction;
import org.openmdx.portal.servlet.action.GridSelectFilterAction;
import org.openmdx.portal.servlet.action.GridSetAlignmentNarrowAction;
import org.openmdx.portal.servlet.action.GridSetAlignmentWideAction;
import org.openmdx.portal.servlet.action.GridSetCurrentFilterAsDefaultAction;
import org.openmdx.portal.servlet.action.GridSetHideRowsOnInitAction;
import org.openmdx.portal.servlet.action.GridSetOrderAnyAction;
import org.openmdx.portal.servlet.action.GridSetOrderAscendingAction;
import org.openmdx.portal.servlet.action.GridSetOrderDescendingAction;
import org.openmdx.portal.servlet.action.GridSetPageAction;
import org.openmdx.portal.servlet.action.GridSetShowRowsOnInitAction;
import org.openmdx.portal.servlet.action.MultiDeleteAction;
import org.openmdx.portal.servlet.action.NewObjectAction;
import org.openmdx.portal.servlet.action.SaveGridAction;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.FieldDef;
import org.openmdx.portal.servlet.attribute.ObjectReferenceValue;
import org.openmdx.portal.servlet.control.GridControl;

/**
 * Grid
 *
 */
/**
 * Grid
 *
 */
public abstract class Grid extends ControlState implements Serializable {
  
	/**
	 * LockedTextValue
	 *
	 */
	static class LockedTextValue extends AttributeValue {

        public LockedTextValue(
            Object object,
            String featureName,
            String qualifiedFeatureName,
            ApplicationContext app
       ) {
	        super(
	        	object, 
	        	new FieldDef(
	        		featureName,
	        		qualifiedFeatureName,
                    Multiplicity.SINGLE_VALUE.toString(),
                    false, // isChangeable
                    true, // isMandatory
                    null, null, null, null, null, null,
                    app.getPortalExtension().getDataBinding(null)
                ),
	        	app
	        );
        }

        @Override
        public Object getDefaultValue(
        ) {
        	return WebKeys.LOCKED_VALUE;
        }

		@Override
        public String getStringifiedValue(
            ViewPort p,
            boolean multiLine,
            boolean forEditing,
            boolean shortFormat
        ) {
			return WebKeys.LOCKED_VALUE;
        }
		
	}
	
    /**
     * Constructor 
     *
     * @param control
     * @param view
     * @param lookupType
     */
    public Grid(
        GridControl control,
        ObjectView view,
        String lookupType
    ) {
        super(
            control,
            view
        );
        ApplicationContext app = this.view.getApplicationContext();
        Texts_1_0 texts = app.getTexts();
        this.columnSortOrders = new HashMap<String,Short>(control.getInitialColumnSortOrders());
        this.lookupType = lookupType;
        this.showRows = this.showGridContentOnInit();
        this.dataBinding = app.getPortalExtension().getDataBinding(
            control.getObjectContainer().getDataBindingName() 
        );
        // Filters
        if(control.getObjectContainer().isReferenceIsStoredAsAttribute()) {
            this.filters = new Filter[]{};
        } else {
            String containerId = control.getContainerId();
            String baseFilterId = containerId.substring(containerId.indexOf("Ref:") + 4);
            Filters filters = app.getFilters(
                control.getQualifiedReferenceName()
            );
            // Default filter
            String filterPropertyName = control.getPropertyName(
                control.getContainerId(),
                UserSettings.DEFAULT_FILTER.getName()
            );
            // Fallback to old-style property name for default filter
            if(
                (app.getSettings().getProperty(filterPropertyName) == null) &&
                (baseFilterId.indexOf(":") < 0) 
            ) {
                filterPropertyName = control.getPropertyName(
                    control.getQualifiedReferenceName(),
                    UserSettings.DEFAULT_FILTER.getName()
                );               
            }
            // Override DEFAULT filter, if at least ALL and DEFAULT filter are defined
            // and settings contains a default filter declaration
            Filter defaultFilter = null;
            if(app.getSettings().getProperty(filterPropertyName) != null) {
            	String filterAsXML = new String(
            		Base64.decode(app.getSettings().getProperty(filterPropertyName))
            	);
            	final List<Exception> parseExceptions = new ArrayList<Exception>();
            	try {
	                defaultFilter = (Filter)JavaBeans.fromXML(
	                	filterAsXML,
	                	new ExceptionListener(){
							@Override
	                        public void exceptionThrown(
	                        	Exception e
	                        ) {
								parseExceptions.add(e);
	                        }                		
	                	}
	                );
            	} catch(Exception e) {
            		parseExceptions.add(e);
            	}
                if(!parseExceptions.isEmpty()) {
                	for(Exception parseException: parseExceptions) {
                		ServiceException e = new ServiceException(
                			parseException,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.PARSE_FAILURE,
                            "Unable to parse user-defined filter",
                            new BasicException.Parameter("filter", filterAsXML),
                            new BasicException.Parameter("property", filterPropertyName),
                            new BasicException.Parameter("user", app.getUserHomeIdentityAsPath())
                        );
                		SysLog.warning(e.getMessage(), e.getCause());
                	}
                }
                defaultFilter.setName(Filters.DEFAULT_FILTER_NAME);
                defaultFilter.setGroupName("0");
                defaultFilter.setIconKey(WebKeys.ICON_FILTER_DEFAULT);                
            }
            this.filters = filters.getPreparedFilters(
                baseFilterId,
                defaultFilter
            );
        }
        // Creators and template rows
        Model_1_0 model = app.getModel();
        org.openmdx.ui1.jmi1.ObjectContainer objectContainer = control.getObjectContainer();
        this.isComposite = !objectContainer.isReferenceIsStoredAsAttribute();
        this.isChangeable = objectContainer.isChangeable();
        Map<String,Action> objectCreators = null;
        List<Object[]> templateRows = null;                
        if(!this.isChangeable) {
            objectCreators = null;
            this.addObjectAction = null;
            this.removeObjectAction = null;
            this.moveUpObjectAction = null;
            this.moveDownObjectAction = null;
        } else if(!this.isComposite) {
          Action addObjectAction = null;
          Action removeObjectAction = null;
          Action moveUpObjectAction = null;
          Action moveDownObjectAction = null;
          objectCreators = null;
          try {
        	  // objectContainer.getReferenceName() contains the unqualified
        	  // name of the reference. Lookup the reference in model.
        	  ModelElement_1_0 reference = model.getFeatureDef(
	              model.getElement(control.getContainerClass()), 
	              objectContainer.getReferenceName(), 
	              false
        	  );
        	  addObjectAction = new Action(
        		  GridAddObjectAction.EVENT_ID,
        		  new Action.Parameter[]{
        			  new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refGetPath().toXRI()),                        
        			  new Action.Parameter(Action.PARAMETER_NAME, "+"),
        			  new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(control.getPaneIndex())),
        			  new Action.Parameter(Action.PARAMETER_REFERENCE, (String)reference.objGetValue("qualifiedName"))
        		  },
        		  "add object",
        		  true
        	  );
        	  removeObjectAction = new Action(
        		  GridAddObjectAction.EVENT_ID,
        		  new Action.Parameter[]{
        			  new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refGetPath().toXRI()),                        
        			  new Action.Parameter(Action.PARAMETER_NAME, "-"),
        			  new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(control.getPaneIndex())),
        			  new Action.Parameter(Action.PARAMETER_REFERENCE, (String)reference.objGetValue("qualifiedName"))
        		  },
        		  "remove object",
        		  true
        	  );
        	  moveUpObjectAction = new Action(
        		  GridMoveUpObjectAction.EVENT_ID,
        		  new Action.Parameter[]{
        			  new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refGetPath().toXRI()),                        
        			  new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(control.getPaneIndex())),
        			  new Action.Parameter(Action.PARAMETER_REFERENCE, (String)reference.objGetValue("qualifiedName"))
        		  },
        		  "move up object",
        		  true
        	  );
        	  moveDownObjectAction = new Action(
        		  GridMoveDownObjectAction.EVENT_ID,
        		  new Action.Parameter[]{
        			  new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refGetPath().toXRI()),                        
        			  new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(control.getPaneIndex())),
        			  new Action.Parameter(Action.PARAMETER_REFERENCE, (String)reference.objGetValue("qualifiedName"))
        		  },
        		  "move down object",
        		  true
        	  );
          } catch(ServiceException e) {
        	  SysLog.warning(e.getMessage(), e.getCause());
          }
          this.addObjectAction = addObjectAction;
          this.removeObjectAction = removeObjectAction;
          this.moveUpObjectAction = moveUpObjectAction;
          this.moveDownObjectAction = moveDownObjectAction;
        } else {
            this.addObjectAction = null;
            this.removeObjectAction = null;
            this.moveUpObjectAction = null;
            this.moveDownObjectAction = null;
            objectCreators = new TreeMap<String,Action>(); // sorted map of creators. key is inspector label
            templateRows = new ArrayList<Object[]>();
            try {
                ModelElement_1_0 referencedType = model.getElement(objectContainer.getReferencedTypeName());
                for(
                    Iterator i = referencedType.objGetList("allSubtype").iterator(); 
                    i.hasNext();
                ) {
                    ModelElement_1_0 subtype = model.getElement(i.next());
                    if(!((Boolean)subtype.objGetValue("isAbstract")).booleanValue()) {
                        String forClass = (String)subtype.objGetValue("qualifiedName");
                        org.openmdx.ui1.jmi1.AssertableInspector assertableInspector = app.getAssertableInspector(forClass);
                        if(assertableInspector.isChangeable()) {
                            // Object creator
                            objectCreators.put(
                                ApplicationContext.getOrderAsString(assertableInspector.getOrder()) + ":" + forClass,
                                new Action(
                                    NewObjectAction.EVENT_ID,
                                    new Action.Parameter[]{
                                        new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refGetPath().toXRI()),                        
                                        new Action.Parameter(Action.PARAMETER_FOR_REFERENCE, objectContainer.getReferenceName()),
                                        new Action.Parameter(Action.PARAMETER_FOR_CLASS, forClass)
                                    },
                                    app.getLabel(forClass),
                                    app.getIconKey(forClass),
                                    true
                                )
                            );
                            // Template row for each creator
                            int nCols = java.lang.Math.min(
                                objectContainer.getMember().size(),
                                Grid.MAX_COLUMNS
                            ); 
                            Map<String,String> templateRowObject = new HashMap<String,String>();
                            templateRowObject.put(
                                SystemAttributes.OBJECT_CLASS,
                                forClass
                            );
                            Object[] templateRow = new Object[nCols+1];      
                            // First column contains full information about object
                            AttributeValue objRef = ObjectReferenceValue.createObjectReferenceValue(
                                templateRowObject,
                                new FieldDef(
                                    "identity",
                                    "org:openmdx:base:ExtentCapable:identity",
                                    Multiplicity.SINGLE_VALUE.toString(),
                                    false,
                                    true,
                                    app.getIconKey(forClass),
                                    null, null, null, null, null,
                                    app.getPortalExtension().getDataBinding(null)
                                ),
                                app
                            );
                            templateRow[0] = objRef;
                            for(int j = 0; j < nCols; j++) {
                                org.openmdx.ui1.jmi1.ValuedField fieldDef = (org.openmdx.ui1.jmi1.ValuedField)objectContainer.getMember().get(j);
                                // Special treatment of identity
                                if(SystemAttributes.OBJECT_IDENTITY.equals(fieldDef.getFeatureName())) {
                                    templateRow[j+1] = ObjectReferenceValue.createObjectReferenceValue(
                                        templateRowObject,
                                        new FieldDef(
                                            "identity",
                                            "org:openmdx:base:ExtentCapable:identity",
                                            Multiplicity.SINGLE_VALUE.toString(),
                                            false,
                                            true,
                                            null, null, null, null, null, null,
                                            app.getPortalExtension().getDataBinding(null)
                                        ),
                                        app
                                    );
                                } else {
                                    templateRow[j+1] = app.createAttributeValue(
                                        fieldDef,
                                        templateRowObject
                                    );
                                }
                            }
                            templateRows.add(templateRow);
                        }
                    }
                }
            } catch(ServiceException e) {
            	SysLog.warning(e.getMessage(), e.getCause());
            }
        }    
        this.objectCreators = objectCreators == null ? 
            null : 
            (Action[])objectCreators.values().toArray(new Action[objectCreators.size()]);     
        this.templateRows = templateRows == null ? 
            null : 
            templateRows.toArray(new Object[templateRows.size()]);        
        // return no action in case of non changeable grids
        this.multiDeleteAction = (this.getObjectCreator() == null) || (this.getAddObjectAction() != null) ? 
            null : 
            new Action(
                MultiDeleteAction.EVENT_ID, 
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refGetPath().toXRI()),                                                  
                },
                app.getTexts().getDeleteTitle(), 
                WebKeys.ICON_DELETE,
                true
            );
        this.saveAction = new Action(
            SaveGridAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refGetPath().toXRI()),                        
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(control.getPaneIndex())),
                new Action.Parameter(Action.PARAMETER_REFERENCE, control.getId())
            },
            texts.getSaveTitle(),
            WebKeys.ICON_SAVE,
            true
        );
        this.setCurrentFilterAsDefaultAction = new Action(
            GridSetCurrentFilterAsDefaultAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refGetPath().toXRI()),                        
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(control.getPaneIndex())),                      
                new Action.Parameter(Action.PARAMETER_REFERENCE, control.getId())     
            },
            texts.getSetCurrentAsDefaultText(),
            WebKeys.ICON_FILTER_SET_AS_DEFAULT,
            true
        );
        // Show first page
        String showRowsOnInitPropertyName = control.getPropertyName(
            control.getQualifiedReferenceName(),
            UserSettings.SHOW_ROWS_ON_INIT.getName()
        );
        if(app.getSettings().getProperty(showRowsOnInitPropertyName) != null) {
            this.setShowGridContentOnInit(
                Boolean.valueOf(app.getSettings().getProperty(showRowsOnInitPropertyName)).booleanValue()
            );
        }
        // Rows
        this.refresh(false);
    }

    /**
     * Get grid control.
     * 
     * @return
     */
    public GridControl getGridControl(
    	) {
    	return (GridControl)this.control;
    }

    /**
     * Get showRows setting.
     * 
     * @return
     */
    public boolean getShowRows(
    ) {
        return this.showRows;
    }
  
    /**
     * Set show rows setting.
     * 
     * @param newValue
     */
    public void setShowRows(
        boolean newValue
    ) {
        this.showRows = newValue;
    }
  
    /**
     * Get grid addObject action.
     * 
     * @return
     */
    public Action getAddObjectAction(
    ) {
        return this.addObjectAction;
    }

    /**
     * Get grid removeObject action.
     * 
     * @return
     */
    public Action getRemoveObjectAction(
    ) {
        return this.removeObjectAction;
    }

    /**
     * Get grid moveUpObject action.
     * 
     * @return
     */
    public Action getMoveUpObjectAction(
    ) {
    	return this.moveUpObjectAction;
    }
    
    /**
     * Get grid moveDownObject action.
     * 
     * @return
     */
    public Action getMoveDownObjectAction(
    ) {
    	return this.moveDownObjectAction;
    }
    
    /**
     * Return true if grid operates on composite association.
     * 
     * @return
     */
    public boolean isComposite(
    ) {
        return this.isComposite;
    }

    /**
     * Return true if grid is changeable, i.e. objects can be added / removed.
     * 
     * @return
     */
    public boolean isChangeable(
    ) {
        return this.isChangeable;
    }    
    
    /**
     * Get object creators.
     * 
     * @return Returns the objectCreators.
     */
    public Action[] getObjectCreators(
    ) {
        return objectCreators;
    }

    /**
     * Get template row.
     * 
     * @return
     */
    public Object[] getTemplateRow(
    ) {
        return this.templateRows;
    }

    /**
     * Get create object actions.
     * 
     * @return
     */
    public Action[] getObjectCreator(
    ) {
        return this.objectCreators;
    }
  
    /**
     * Get grid multi-delete action.
     * 
     * @return
     */
    public Action getMultiDeleteAction(
    ) {
        return this.multiDeleteAction;
    }
  
    /**
     * Get grid save action.
     * 
     * @return
     */
    public Action getSaveAction(
    ) {
        return this.saveAction;
    }

    /**
     * Get grid pageNext action.
     * 
     * @param isEnabled
     * @return
     */
    public Action getPageNextAction(
        boolean isEnabled
    ) {
        return new Action(
            GridPageNextAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refGetPath().toXRI()),                        
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(this.getGridControl().getPaneIndex())),         
                new Action.Parameter(Action.PARAMETER_REFERENCE, control.getId())       
            },
            "",
            isEnabled ? WebKeys.ICON_NEXT : WebKeys.ICON_NEXT_DISABLED,
            isEnabled
        );
    }

    /**
     * Get grid pagePrevious action.
     * 
     * @param isEnabled
     * @return
     */
    public Action getPagePreviousAction(
        boolean isEnabled
    ) {
        return new Action(
            GridPagePreviousAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refGetPath().toXRI()),                        
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(this.getGridControl().getPaneIndex())),
                new Action.Parameter(Action.PARAMETER_REFERENCE, control.getId())       
            },
            "",
            isEnabled ? WebKeys.ICON_PREVIOUS : WebKeys.ICON_PREVIOUS_DISABLED,
            isEnabled
        );
    }
      
    /**
     * Get grid pageNextFast action.
     * 
     * @param isEnabled
     * @return
     */
    public Action getPageNextFastAction(
        boolean isEnabled
    ) {
        GridControl gridControl = this.getGridControl();
        int paneIndex = gridControl.getPaneIndex();
        int next10 = java.lang.Math.min(this.getLastPage(), this.getCurrentPage() + 10);
        return new Action(
            GridSetPageAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.view.getRefObject().refGetPath().toXRI()),
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(paneIndex)), 
                new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                new Action.Parameter(Action.PARAMETER_PAGE, Integer.toString(next10))
            },
            ">>",
            isEnabled ? WebKeys.ICON_NEXT_FAST : WebKeys.ICON_NEXT_FAST_DISABLED,
            isEnabled
        );
    }
    
    /**
     * Get grid pagePreviousFast action.
     * 
     * @param isEnabled
     * @return
     */
    public Action getPagePreviousFastAction(
        boolean isEnabled
    ) {
        GridControl gridControl = this.getGridControl();
        int paneIndex = gridControl.getPaneIndex();
        int back10 = java.lang.Math.max(0, this.getCurrentPage() - 10);
        return new Action(
            GridSetPageAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.view.getRefObject().refGetPath().toXRI()),
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(paneIndex)), 
                new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                new Action.Parameter(Action.PARAMETER_PAGE, Integer.toString(back10))
            },
            "<<",
            isEnabled ? WebKeys.ICON_PREVIOUS_FAST : WebKeys.ICON_PREVIOUS_FAST_DISABLED,
            isEnabled
        );        
    }
      
    /**
     * Get setCurrentFilterAsDefault action.
     * 
     * @return
     */
    public Action getSetCurrentFilterAsDefaultAction(
    ) {
        return this.setCurrentFilterAsDefaultAction;
    }
      
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.ControlState#refresh(boolean)
     */
    @Override
    public void refresh(
    	boolean refreshData
    ) {
    	ApplicationContext app = this.view.getApplicationContext();
    	Model_1_0 model = app.getModel();
    	GridControl gridControl = this.getGridControl();
    	// Show row selectors
    	this.showRowSelectors = false;
    	if(lookupType != null) {
    		this.showRowSelectors = false;
    		try {
    			this.showRowSelectors = GridControl.getShowRowSelectors(
    				lookupType,
    				model.getElement(gridControl.getObjectContainer().getReferencedTypeName()),
    				model
    			);
    		} catch(Exception ignore) {}          
    	}
    	if(this.currentFilter == null) {
    		this.selectFilter(
    			Filters.DEFAULT_FILTER_NAME, 
    			null 
    		);
    	} else {
    		String filterName = this.currentFilter.getName();
    		this.selectFilter(
    			filterName,
    			this.filterValues.get(filterName) == null 
    				? "" 
    				: this.filterValues.get(filterName)
    			);
    	}
    }
  
    /**
     * Set current filter as default filter.
     * 
     * @throws ServiceException
     */
    public void setCurrentFilterAsDefault(
    ) throws ServiceException {
    	if(this.currentFilter != null) {
    		Filter defaultFilter = new Filter(
    			Filters.DEFAULT_FILTER_NAME,
    			null,
    			"0",
    			WebKeys.ICON_FILTER_DEFAULT,
    			null,
    			this.currentFilter.getCondition(),
    			this.currentFilter.getOrderSpecifier(),
    			// Copy query extensions from current filter to default filter.
    			// Otherwise predefined filter XMLs containing query extensions
    			// can not be set as default filter
    			this.currentFilter.getExtension(),
    			this.getGridControl().getContainerId(), 
    			this.view.getApplicationContext().getCurrentUserRole(),
    			this.view.getObjectReference().getXRI()
    		);
    		String filterAsXml = JavaBeans.toXML(defaultFilter);
    		Properties settings = this.view.getApplicationContext().getSettings();
    		settings.setProperty(
    			this.getGridControl().getPropertyName(
    				this.getGridControl().getContainerId(),
    				UserSettings.DEFAULT_FILTER.getName()
    			),
    			Base64.encode(filterAsXml.getBytes())
    		);
    		this.setFilter(
    			Filters.DEFAULT_FILTER_NAME,
    			defaultFilter
    		);
    	}
    }
  
    /**
     * Returns true if row selectors (check boxes) should be shown for each grid 
     * row. This allows the user to select a row of the grid. 
     * The method returns true, if
     * <ul>
     *   <li>lookupType != null and the grid contains objects which are instance of lookupType</li>
     * </ul>
     * false otherwise.
     * 
     * @return
     */
    public boolean showRowSelectors(
    ) {
    	return this.showRowSelectors;
    }
  
    /**
     * Get rows for current page.
     * 
     * @param pm
     * @return
     */
    public Object[] getRows(
    	PersistenceManager pm
    ) {
    	return this.getRows(
    		pm,
    		false // allColumns
    	);
    }

    /**
     * Get rows for current page.
     * 
     * @param pm
     * @param allColumns if true ignore showMaxMember setting. I.e. return all columns 
     *        which are customized for grid. 
     * @return
     */
    public Object[] getRows(
    	PersistenceManager pm,
    	boolean allColumns
    ) {
    	List<Object[]> rows = new ArrayList<Object[]>();      
    	if(this.showRows) {
    		int newPageSize = this.setPageRequestNewPageSize;
    		int newPage = this.setPageRequestNewPage;
    		if(newPage > 0) {
    			this.showTotalRows = true;
    		}
    		List filteredObjects =  this.getFilteredObjects(
    			pm,
    			this.showTotalRows,
    			this.currentFilter
    		);
    		if(this.showTotalRows) {
    			this.totalRows = filteredObjects.size();
    		}
    		ApplicationContext app = this.view.getApplicationContext();
    		GridControl gridControl = this.getGridControl();
    		int currentPageSize = this.getPageSize();
    		newPageSize = (newPageSize <= 0) || (newPageSize > MAX_PAGE_SIZE) 
    			? currentPageSize 
    			: newPageSize;
    		if(newPageSize != currentPageSize) {
    			this.numberOfPages = Integer.MAX_VALUE;       
    			Properties settings = app.getSettings();
    			settings.setProperty(
    				this.getGridControl().getPropertyName(
    					this.getGridControl().getQualifiedReferenceName(),
    					UserSettings.PAGE_SIZE.getName()
    				),
    				"" + newPageSize
    			);
    		}
    		// Try to keep current row. Otherwise set current row 
    		// to first row of selected page
    		int firstRow = newPage * currentPageSize;
    		if(newPage == this.currentPage) {
    			firstRow = this.currentRow >= 0 
    				? this.currentRow - currentPageSize
    				: firstRow;
    		} else if(newPage == this.currentPage + 1) {
    			firstRow = this.currentRow >= 0
    				? this.currentRow
    				: firstRow;
    		} else if(newPage == this.currentPage - 1) {
    			firstRow = this.currentRow >= 0
    				? this.currentRow - currentPageSize - newPageSize
    				: firstRow; 
    		}
    		firstRow = Math.max(0, firstRow);
    		// Set starting position to firstRow. If firstRow exceeds number
    		// of maximum rows then page up until an existing row is found
    		Iterator i = null;
    		if(filteredObjects != null) {
    			newPage = firstRow / newPageSize;
    			int lastEmptyPage = newPage + 1;
    			while(true) {
    				boolean hasNext = false;
    				try {
    					i = filteredObjects.listIterator(firstRow);
    					hasNext = i.hasNext();
    				} catch(Exception e) {
    					hasNext = false;
    				}
    				if(hasNext || (newPage == 0)) {
    					if(newPage == lastEmptyPage - 1) break;
    					newPage++;
    				} else {
    					lastEmptyPage = newPage;
    					newPage -= 10;                  
    					if(newPage < 0) newPage = 0;
    				}
    				firstRow = newPage * currentPageSize;
    			}
    		}
    		if(i == null) {
    			i = new ArrayList().iterator();
    		}
    		this.currentPage = new Double(Math.ceil((double)firstRow / (double)newPageSize)).intValue();
    		this.currentRow = firstRow;          
    		this.currentPageSize = newPageSize;
    		int nCols = Math.min(
				gridControl.getObjectContainer().getMember().size(),
				Grid.MAX_COLUMNS
			);
    		if(!allColumns) {
    			nCols = Math.min(
        			gridControl.getShowMaxMember(),
        			nCols
        		);
    		}
    		nCols += 1;
    		try {
    			while(i.hasNext()) {
    				RefObject_1_0 rowObject = (RefObject_1_0)i.next();
    				Object[] row = new Object[nCols];      
    				// first column contains full information about object
    				AttributeValue objRef = ObjectReferenceValue.createObjectReferenceValue(
    					rowObject,
    					new FieldDef(
    						"identity",
    						"org:openmdx:base:ExtentCapable:identity",
    						Multiplicity.SINGLE_VALUE.toString(),
    						false,
    						true,
    						null, null, null, null, null, null,
    						app.getPortalExtension().getDataBinding(null)
    					),
    					app
    				);
    				row[0] = objRef;               
    				for(int j = 1; j < nCols; j++) {
    					org.openmdx.ui1.jmi1.ValuedField fieldDef = (org.openmdx.ui1.jmi1.ValuedField)gridControl.getObjectContainer().getMember().get(j-1);
    					// special treatment of identity
    					if(SystemAttributes.OBJECT_IDENTITY.equals(fieldDef.getFeatureName())) {
    						row[j] = ObjectReferenceValue.createObjectReferenceValue(                      
    							rowObject,
    							new FieldDef(
    								"identity",
    								"org:openmdx:base:ExtentCapable:identity",
    								Multiplicity.SINGLE_VALUE.toString(),
    								false,
    								true,
    								null, null, null, null, null, null,
    								app.getPortalExtension().getDataBinding(null)
    							),
    							app
    						);
    					} else {
    						try {
    							if(!app.getPortalExtension().hasPermission(fieldDef.getQualifiedFeatureName(), rowObject, app, WebKeys.PERMISSION_REVOKE_SHOW)) {
    								row[j] = app.createAttributeValue(
    									fieldDef, 
    									rowObject
    								);
    							} else {
    								row[j] = new LockedTextValue(
    									rowObject,
    									fieldDef.getFeatureName(),
    									fieldDef.getQualifiedFeatureName(), 
    									app 
    								);
    							}
    						} catch(Exception e) {
    							row[j] = null;
    						}
    					}
    				}
    				rows.add(row);
    				this.currentRow++;
    				if(rows.size() % newPageSize == 0) break;
    			}
    			if(!i.hasNext()) {
    				this.numberOfPages = newPage+1;
    			}
    		} catch(Exception e) {
    			ServiceException e0 = new ServiceException(e);
    			e0.log();
    		}
    	}
    	return rows.toArray(new Object[rows.size()]);
    }

    /**
     * Set current page.
     * 
     * @param newPage
     * @param newPageSize
     */
    public void setPage(
    	int newPage,
    	int newPageSize
    ) {
    	this.setPageRequestNewPage = newPage;
    	this.setPageRequestNewPageSize = newPageSize;
    }
  
    /**
     * Get current page number.
     * 
     * @return
     */
    public int getCurrentPage(
    ) {
    	return this.currentPage;
    }

    /**
     * Get last page number if known.
     * 
     * @return
     */
    public int getLastPage(
    	) {
    	return java.lang.Math.max(0, this.numberOfPages - 1);
    }
  
    /**
     * Get page size for this grid.
     * 
     * @return
     */
    public int getPageSize(
    ) {
    	ApplicationContext app = this.view.getApplicationContext();
    	GridControl gridControl = this.getGridControl();
    	if(this.currentPageSize < 0) {
    		String pageSizePropertyName = this.getGridControl().getPropertyName(
    			this.getGridControl().getQualifiedReferenceName(),
    			UserSettings.PAGE_SIZE.getName()
    		);
    		if(app.getSettings().getProperty(pageSizePropertyName) != null) {
    			this.currentPageSize = 
    				Short.parseShort(app.getSettings().getProperty(pageSizePropertyName));
    		} else {
    			this.currentPageSize = 
    				app.getPortalExtension().getGridPageSize(
    					gridControl.getObjectContainer().getReferencedTypeName()
    				);
    		}
    	}
    	return this.currentPageSize;
    }

    /**
     * Get filtered objects. This method is implemented by a concrete subclass.
     * If preCalcListSize is true, the size of the returned list is pre-calculated,
     * i.e. size() returns the pre-calculated size. If set to false, the size() is
     * determined by iterating the list, which can be resource-consuming.
     * 
     * @param pm
     * @param preCalcListSize
     * @param filter
     * @return
     */
    abstract public List<RefObject_1_0> getFilteredObjects(
    	PersistenceManager pm,
    	boolean preCalcListSize,
        Filter filter
    );

    /**
     * Select filter.
     * 
     * @param filterName
     * @param filterValues
     */
    public void selectFilter(
    	String filterName,
    	String filterValues
    ) {
    	GridControl gridControl = this.getGridControl();  
		// reset column order indicators if new filter is set
		this.columnSortOrders.clear();
		this.columnSortOrders.putAll(
			gridControl.getInitialColumnSortOrders()
		);
		this.filterValues.clear();
		this.currentFilterValue = null;
		// can apply filter only to containers
		Filter filter = this.getFilter(filterName);
		if(filter == null) {
			SysLog.detail("filter " + filterName + " not found");
		} else {
			this.currentFilter = filter;
			int i = 0;
			for(Condition condition : this.currentFilter.getCondition()) {
				for(Object value : condition.getValue()){
					// Replace ? by filter value entered by user
					// Replace ${name} by attribute value
					if(
						"?".equals(value) ||
						(value instanceof String && ((String)value).startsWith("${") && ((String)value).endsWith("}")) 
					) {
						this.currentFilter = new Filter(
							this.currentFilter.getName(),
							this.currentFilter.getLabel(),
							this.currentFilter.getGroupName(),
							this.currentFilter.getIconKey(),
							this.currentFilter.getOrder(),
							this.currentFilter.getCondition(),
							this.currentFilter.getOrderSpecifier(),
							this.currentFilter.getExtension(),
							this.getGridControl().getQualifiedReferenceName(), 
							this.view.getApplicationContext().getCurrentUserRole(), 
							this.view.getObjectReference().getXRI()
						);
						Condition newCondition = (Condition)condition.clone();
						// ?
						if("?".equals(value)) {
    						List<String> values = new ArrayList<String>();
    						StringTokenizer tokenizer = new StringTokenizer(filterValues, ",;");
    						while(tokenizer.hasMoreTokens()) {
    							values.add(tokenizer.nextToken());
    						}
    						if(values.isEmpty()) {
    							values.add("");
    						}
    						newCondition.setValue(
    							values.toArray(new Object[values.size()])
    						);
						} else {
							// ${name}
							String feature = (String)value;
							feature = feature.substring(2);
							feature = feature.substring(0, feature.length() - 1);
							newCondition.setValue(
								new Object[]{
									this.view.getObjectReference().getObject().refGetValue(feature)
								}
							);
						}
						this.currentFilter.getCondition(
						).set(
							i,
							newCondition
						);
					}
				}
				i++;
			}
		}
		SysLog.detail("selected filter ", this.currentFilter);
		this.numberOfPages = Integer.MAX_VALUE;
		this.setPage(
			0, 
			-1 // do not change page size
		);
    }

    /**
     * Add column filter to current filter.
     * 
     * @param filterName
     * @param filterValues
     * @param add
     * @param newPageSize
     */
    public void setColumnFilter(
    	String filterName,
    	String filterValues,
    	boolean add,
    	int newPageSize
    ) {
    	ApplicationContext app = this.view.getApplicationContext();
    	GridControl gridControl = this.getGridControl();      
    	this.filterValues.put(
    		filterName, 
    		this.currentFilterValue = (filterValues == null ? "" : filterValues)
    	);
    	// Clear column sort indicators if new filter is set
    	if(!add) {
    		this.columnSortOrders.clear();
    		this.columnSortOrders.putAll(
    			gridControl.getInitialColumnSortOrders()
    		);
    	}
    	// Find column definition for given column name
    	Object columnDef = null;
    	if(SystemAttributes.OBJECT_IDENTITY.equals(filterName)) {
    		columnDef = this.getGridControl().getObjectContainer().getReferencedTypeName() + ":" + SystemAttributes.OBJECT_IDENTITY;
    	} else {
	    	for(
	    		int i = 0; 
	    		(i < gridControl.getObjectContainer().getMember().size()) && (i < MAX_COLUMNS); 
	    		i++
	    	) {
	    		org.openmdx.ui1.jmi1.ValuedField column = (org.openmdx.ui1.jmi1.ValuedField)gridControl.getObjectContainer().getMember().get(i);
	    		if(filterName.equals(column.getFeatureName())) {
	    			columnDef = column;
	    			break;
	    		}	    		
	    	}
    	}
    	if(columnDef != null) {
			Filter filter = add ? this.currentFilter : this.getFilter("All");
			Map<String,List<Condition>> conditions = new HashMap<String,List<Condition>>();
			for(Condition condition: filter.getCondition()) {
				if(conditions.containsKey(condition.getFeature())) {
					conditions.get(
						condition.getFeature()
					).add(
						condition
					);
				} else {
					conditions.put(
						condition.getFeature(),
						new ArrayList<Condition>(Collections.singletonList(condition))
					);
				}
			}
			Map<String,Extension> extensions = new HashMap<String,Extension>();
			for(Extension extension: filter.getExtension()) {
				extensions.put(
					extension.getClause(),
					extension
				);
			}
			List<OrderSpecifier> orderSpecifiers = new ArrayList<OrderSpecifier>(
				filter.getOrderSpecifier()
			);
			String qualifiedFeatureName = null;
			String featureName = null;
			if(columnDef instanceof org.openmdx.ui1.jmi1.ValuedField) {
				org.openmdx.ui1.jmi1.ValuedField valuedField = (org.openmdx.ui1.jmi1.ValuedField)columnDef;
				qualifiedFeatureName = valuedField.getQualifiedFeatureName();
				featureName = valuedField.getFeatureName();
			} else {
				qualifiedFeatureName = (String)columnDef;
				featureName = qualifiedFeatureName.substring(qualifiedFeatureName.lastIndexOf(":") + 1);
			}
			// Reset condition if in set of active filter features 
			if(this.activeFilterFeatures.contains(featureName)) {
				conditions.remove(featureName);
			}
			// Number
			if(columnDef instanceof org.openmdx.ui1.jmi1.NumberField) {
				// Code
				Map<String,Short> shortTexts = null;
				Map<String,Short> longTexts = null;
				if(app.getCodes() != null) {
					if(app.getCodes().getShortText(qualifiedFeatureName, (short)0, true, true) != null) {
    					shortTexts = app.getCodes().getShortTextByText(
    						qualifiedFeatureName, 
    						app.getCurrentLocaleAsIndex(), 
    						true
    					);
    					longTexts = app.getCodes().getLongTextByText(
    						qualifiedFeatureName,
    						app.getCurrentLocaleAsIndex(),
    						true
    					);
					} else {
						String qualifiedFeatureName2 = 
							this.getGridControl().getObjectContainer().getReferencedTypeName() + 
							":" +
							featureName;
    					if(app.getCodes().getShortText(qualifiedFeatureName2, (short)0, true, true) != null) {
	    					shortTexts = app.getCodes().getShortTextByText(
	    						qualifiedFeatureName2, 
	    						app.getCurrentLocaleAsIndex(), 
	    						true
	    					);
	    					longTexts = app.getCodes().getLongTextByText(
	    						qualifiedFeatureName2,
	    						app.getCurrentLocaleAsIndex(),
	    						true
	    					);
    					}
					}
				}
				if(shortTexts != null) {
					SysLog.detail("Code filter values", filterValues);
					StringTokenizer andExpr = new StringTokenizer(filterValues, "&");
					while(andExpr.hasMoreTokens()) {
						QueryConditionParser parser = app.getPortalExtension().getQueryConditionParser(
							qualifiedFeatureName, 
							new IsInCondition(
								Quantifier.THERE_EXISTS,
								featureName,
								true,
								(Object[])null
							)
						);
						List<Object> values = new ArrayList<Object>();
						StringTokenizer orExpr = new StringTokenizer(andExpr.nextToken().trim(), ";");
						Condition condition = null;
						while(orExpr.hasMoreTokens()) {
							String token = orExpr.nextToken().trim();
							condition = parser.parse(token);
							try {
								String trimmedToken = token.substring(
									parser.getOffset()
								).trim();
								Short code = (Short)shortTexts.get(trimmedToken);
								if(code == null) {
									// Try to match a short text
									{
								    	String trimmedTokenUpper = trimmedToken.toUpperCase();
								    	String lastMatch = null;
								    	for(Entry<String,Short> entry: shortTexts.entrySet()) {
								    		if(entry.getKey().toUpperCase().indexOf(trimmedTokenUpper) >= 0) {
								    			if(lastMatch == null || entry.getKey().length() < lastMatch.length()) {
								    				code = entry.getValue();
								    				lastMatch = entry.getKey();
								    			}
								    		}
								    	}
									}
									// Try to match a long text
									if(code == null) {
								    	String trimmedTokenUpper = trimmedToken.toUpperCase();
								    	String lastMatch = null;
								    	for(Entry<String,Short> entry: longTexts.entrySet()) {
								    		if(entry.getKey().toUpperCase().indexOf(trimmedTokenUpper) >= 0) {
								    			if(lastMatch == null || entry.getKey().length() < lastMatch.length()) {
								    				code = entry.getValue();
								    				lastMatch = entry.getKey();
								    			}
								    		}
								    	}
									}
									if(code == null) {
    									try {
    										code = new Short(trimmedToken);
    									} catch(Exception e) {}
									}
								}
								if(code == null) {
									SysLog.detail("can not map token to code", trimmedToken);
									values.add((short)-1);
								} else {
									values.add(code);
								}
							} catch(NumberFormatException ignore) {}
						}
						if(!values.isEmpty()) {
							String stringifiedValues = "";
							for(Object value: values) {
								stringifiedValues += (stringifiedValues.isEmpty() ? "" : ",") + value;
							}
							org.openmdx.base.query.Filter customQuery = null;
							try {
								customQuery = app.getPortalExtension().getQuery(
									qualifiedFeatureName,
									stringifiedValues,
									0, // paramCount
									app
								);
							} catch(Exception ignore) {}
							if(customQuery != null) {
								for(Condition customQueryCondition: customQuery.getCondition()) {
									if(conditions.containsKey(customQueryCondition.getFeature())) {
										conditions.get(
											customQueryCondition.getFeature()
										).add(
											customQueryCondition
										);
									} else {
										conditions.put(
											customQueryCondition.getFeature(), 
											new ArrayList<Condition>(Collections.singletonList(customQueryCondition))
										);
									}
								}
								for(Extension customQueryExtension: customQuery.getExtension()) {
									extensions.put(
										customQueryExtension.getClause(),
										customQueryExtension
									);
								}
							} else {
    							condition.setValue(values.toArray());
    							if(conditions.containsKey(condition.getFeature())) {
    								conditions.get(
    									condition.getFeature()
    								).add(
    									condition
    								);
    							} else {
	    							conditions.put(
	    								condition.getFeature(),
	    								new ArrayList<Condition>(Collections.singletonList(condition))
	    							);
    							}
    							this.activeFilterFeatures.add(featureName);
    						}
						}
					}
				} else {
    				// Number
					SysLog.detail("Number filter values", filterValues);
					StringTokenizer andExpr = new StringTokenizer(filterValues, "&");
					while(andExpr.hasMoreTokens()) {
						QueryConditionParser parser = app.getPortalExtension().getQueryConditionParser(
							qualifiedFeatureName, 
							new IsInCondition(
								Quantifier.THERE_EXISTS,
								featureName,
								true,
								(Object[])null
							)
						);
						List<Object> values = new ArrayList<Object>();
						Condition condition = null;
						StringTokenizer orExpr = new StringTokenizer(andExpr.nextToken().trim(), ";");
						while(orExpr.hasMoreTokens()) {
							String token = orExpr.nextToken().trim();
							condition = parser.parse(token);
							BigDecimal num = app.parseNumber(
								token.substring(parser.getOffset()).trim()
							);
							if(num != null) {        
								// Only integers as filter values. BigDecimal
								// should not be used because serializing of
								// filters with XMLEncoder does not work
								values.add(
									new Long(num.longValue())
								);
							}
						}
						if(!values.isEmpty()) {
							condition.setValue(
								values.toArray(new Object[values.size()])
							);
							if(conditions.containsKey(condition.getFeature())) {
								conditions.get(
									condition.getFeature()
								).add(
									condition
								);
							} else {
								conditions.put(
									condition.getFeature(),
									new ArrayList<Condition>(Collections.singletonList(condition))
								);
							}
							this.activeFilterFeatures.add(featureName);
						}
					}
				}
			} else if(columnDef instanceof org.openmdx.ui1.jmi1.DateField) {
    			// Date
				SysLog.detail("Date filter values", filterValues);
				SimpleDateFormat dateParser = (SimpleDateFormat)SimpleDateFormat.getDateInstance(
					java.text.DateFormat.SHORT,
					this.getCurrentLocale()
				);
				StringTokenizer andExpr = new StringTokenizer(filterValues, "&");
				while(andExpr.hasMoreTokens()) {
					QueryConditionParser parser = app.getPortalExtension().getQueryConditionParser(
						qualifiedFeatureName, 
						new IsBetweenCondition(
							Quantifier.THERE_EXISTS,
							featureName,
							true,
							null,
							null
						)
					);
					Condition condition = null;
					List<Object> values = new ArrayList<Object>();
					StringTokenizer orExpr = new StringTokenizer(andExpr.nextToken().trim(), ";");
					while(orExpr.hasMoreTokens()) {
						String token = orExpr.nextToken().trim();
						condition = parser.parse(token);
						try {
							values.add(
								dateParser.parse(token.substring(parser.getOffset()).trim())
							);
						} catch(ParseException e) {}
					}
					if(
						(condition instanceof IsBetweenCondition) &&
						(values.size() < 2)
					) {
						Date day;
						if(values.isEmpty()) {
							day = new Date();
						} else try {
							day = (Date)values.get(0);
						} catch(IllegalArgumentException e) {
							day = new Date();
						}
						Calendar nextDay = new GregorianCalendar();
						nextDay.setTime(day);
						nextDay.add(Calendar.DAY_OF_MONTH, 1);
						values.clear();
						values.add(day);
						values.add(nextDay.getTime());
					}
					if(!values.isEmpty()) {
						condition.setValue(
							values.toArray(new Object[values.size()])
						);
						if(conditions.containsKey(condition.getFeature())) {
							conditions.get(
								condition.getFeature()
							).add(
								condition
							);
						} else {
							conditions.put(
								condition.getFeature(),
								new ArrayList<Condition>(Collections.singletonList(condition))							
							);
						}
						this.activeFilterFeatures.add(featureName);
					}
				}
			} else if(columnDef instanceof org.openmdx.ui1.jmi1.CheckBox) {
    			// Boolean
				SysLog.detail("Boolean filter values", filterValues);
				List<Boolean> values = new ArrayList<Boolean>();
				StringTokenizer tokenizer = new StringTokenizer(filterValues, ";");
				while(tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if(app.getTexts().getTrueText().equals(token)) {
						values.add(
							Boolean.TRUE
						);
					} else {
						values.add(
							Boolean.FALSE
						);
					}
				}
				if(!values.isEmpty()) {
					Condition condition = new IsInCondition(
						Quantifier.THERE_EXISTS,
						featureName,
						true,
						values.toArray(new Object[values.size()])
					);
					if(conditions.containsKey(featureName)) {
						conditions.get(
							featureName
						).add(
							condition
						);
					} else {
						conditions.put(
							featureName,
							new ArrayList<Condition>(Collections.singletonList(condition))
						);							
					}
					this.activeFilterFeatures.add(featureName);
				}
			} else {
    			// String
				SysLog.detail("String filter values", filterValues);
				// Reset condition if in set of active filter features
				{
					org.openmdx.base.query.Filter customQuery = null;
					try {
						customQuery = app.getPortalExtension().getQuery(
							qualifiedFeatureName,
							"",
							0, // paramCount
							app
						);
					} catch(Exception ignore) {}
					if(customQuery != null && this.activeFilterFeatures.contains(featureName)) {
						for(Condition customQueryCondition: customQuery.getCondition()) {
							conditions.remove(customQueryCondition.getFeature());
						}
						for(Extension customQueryExtension: customQuery.getExtension()) {
							extensions.remove(customQueryExtension.getClause());
						}
					}
				}
				Pattern pattern = Pattern.compile("[\\s\\&]*(?:(?:\"([^\"]*)\")|([^\\s\"\\&]+))");
				Matcher matcher = pattern.matcher(filterValues);
				while(matcher.find()) {
					for(int j = 1; j <= matcher.groupCount(); j++) {
						String andExpr = matcher.group(j);
						if(andExpr != null) {
							// if getQuery() returns a query extension it must be merged with 
							// the base query which might also contain a query extension
							org.openmdx.base.query.Filter customQuery = null;
							try {
								customQuery = app.getPortalExtension().getQuery(
									qualifiedFeatureName,
									andExpr,
									0, // paramCount
									app
								);
							} catch(Exception ignore) {}
							if(customQuery != null) {
								for(Condition customQueryCondition: customQuery.getCondition()) {
									if(conditions.containsKey(customQueryCondition.getFeature())) {
										conditions.get(
											customQueryCondition.getFeature()
										).add(
											customQueryCondition
										);
									} else {
										conditions.put(
											customQueryCondition.getFeature(),
											new ArrayList<Condition>(Collections.singletonList(customQueryCondition))
										);
									}
								}
								for(Extension customQueryExtension: customQuery.getExtension()) {
									extensions.put(
										customQueryExtension.getClause(),
										customQueryExtension
									);
								}
    							this.activeFilterFeatures.add(featureName);
							} else {
								QueryConditionParser conditionParser = app.getPortalExtension().getQueryConditionParser(
									qualifiedFeatureName, 
									new IsLikeCondition(
    									Quantifier.THERE_EXISTS,
    									featureName,
    									true,
    									(Object[])null
    								)
								);
								Condition condition = null;
								List<Object> values = new ArrayList<Object>();
								StringTokenizer orExpr = new StringTokenizer(andExpr.trim(), ";");
								while(orExpr.hasMoreTokens()) {
									String token = orExpr.nextToken().trim();
									condition = conditionParser.parse(token);
									String trimmedToken = token.substring(conditionParser.getOffset()).trim();
									if(condition instanceof IsLikeCondition) {
										trimmedToken = app.getWildcardFilterValue(trimmedToken);
									}
									values.add(trimmedToken);
								}
								if(!values.isEmpty()) {
									condition.setValue(values.toArray());
									if(conditions.containsKey(condition.getFeature())) {
										conditions.get(
											condition.getFeature()
										).add(
											condition
										);
									} else {
										conditions.put(
											condition.getFeature(),
											new ArrayList<Condition>(Collections.singletonList(condition))
										);
									}
	    							this.activeFilterFeatures.add(featureName);
								}
							}
						}
					}
				}
			}
			List<Condition> expandedConditions = new ArrayList<Condition>();
			for(List<Condition> conditionValues: conditions.values()) {
				for(Condition condition: conditionValues) {
					expandedConditions.add(condition);
				}
			}
			this.currentFilter = new Filter(
				filter.getName(),
				filter.getLabel(),
				filter.getGroupName(),
				WebKeys.ICON_DEFAULT,
				null,
				expandedConditions,
				orderSpecifiers,
				new ArrayList<Extension>(extensions.values()),
				this.getGridControl().getContainerId(), 
				this.view.getApplicationContext().getCurrentUserRole(), 
				this.view.getObjectReference().getXRI()
			);
    	}
    	SysLog.detail("selected filter", this.currentFilter);
    	this.numberOfPages = Integer.MAX_VALUE;
    	this.setPage(
    		0,
    		newPageSize
    	);
    }
  
    /**
     * Set order for given feature.
     * 
     * @param feature
     * @param order
     */
    public void setOrder(
    	String feature,
    	short order
    ) {
    	GridControl gridControl = this.getGridControl();
    	this.columnSortOrders.put(
    		feature,
    		new Short(order)
    	);
    	// apply filter to container
    	if(this.currentFilter != null) {
            List<OrderSpecifier> orderSpecifier = new ArrayList<OrderSpecifier>(this.currentFilter.getOrderSpecifier());
    		// Lookup column to be ordered
    		org.openmdx.ui1.jmi1.ValuedField column = null;        
    		for(
    			int i = 0; 
    			(i < gridControl.getObjectContainer().getMember().size()) && (i < MAX_COLUMNS); 
    			i++
    		) {
    			column = (org.openmdx.ui1.jmi1.ValuedField)gridControl.getObjectContainer().getMember().get(i);
    			if(feature.equals(column.getFeatureName())) {
    				break;
    			}
    		}
    		if(!(column instanceof org.openmdx.ui1.jmi1.ObjectReferenceField)) {
    			String orderByFeature = feature;
    			// Set/add order specifier
    			boolean found = false;
    			for(
    				int i = 0; 
    				i < orderSpecifier.size(); 
    				i++
    			) {
    				if(orderByFeature.equals(((OrderSpecifier)orderSpecifier.get(i)).getFeature())) {
    					orderSpecifier.set(
    						i,
    						new OrderSpecifier(
    							orderByFeature,
    							SortOrder.valueOf(order)
    							)
    						);
    					found = true;
    					break;
    				}
    			}
    			if(!found) {
    				orderSpecifier.add(
    					new OrderSpecifier(
    						orderByFeature,
    						SortOrder.valueOf(order)
    						)         
    					);
    			}
    			this.currentFilter = new Filter(
    				this.currentFilter.getName(),
    				this.currentFilter.getLabel(),
    				this.currentFilter.getGroupName(),
    				this.currentFilter.getIconKey(),
    				this.currentFilter.getOrder(),
    				this.currentFilter.getCondition(),
    				orderSpecifier,
    				this.currentFilter.getExtension(),
    				this.getGridControl().getQualifiedReferenceName(), 
    				this.view.getApplicationContext().getCurrentUserRole(), 
    				this.view.getObjectReference().getXRI()                 
    				);
    		}
    	}
    	SysLog.detail("Order by filter", this.currentFilter);
    	this.numberOfPages = Integer.MAX_VALUE;
    	this.setPage(
    		0,
    		-1 // do not change page size
    	);
    }
  
    /**
     * Get order for given feature.
     * 
     * @param feature
     * @return
     */
    public short getOrder(
    	String feature
    ) {
	    Short order = (Short)this.columnSortOrders.get(feature);
	    return order == null
	      ? SortOrder.UNSORTED.code()
	      : order.shortValue();
    }

    /**
     * Get current filter.
     * 
     * @return
     */
    public Filter getCurrentFilter(
    ) {
    	return this.currentFilter;
    }
  
    /**
     * Return true if any filter has filter values.
     * 
     * @return
     */
    public boolean hasFilterValues(
    ) {
    	for(String filterValue: this.filterValues.values()) {
    		if(filterValue != null && filterValue.length() != 0) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Get filter value for given filter.
     * 
     * @param filterName
     * @return
     */
    public String getFilterValue(
	    String filterName
    ) {
	    return this.filterValues.get(filterName) == null 
	    	? "" 
	    	: this.filterValues.get(filterName);
    }
  
    /**
     * Get current filter value.
     * 
     * @return
     */
    public String getCurrentFilterValue(
    ) {
    	return this.currentFilterValue;
    }
  
    /**
     * Get firstPage action.
     * 
     * @return
     */
    public Action getFirstPageAction(
    ) {
        GridControl gridControl = this.getGridControl();
        int paneIndex = gridControl.getPaneIndex();
        return new Action(
            GridSetPageAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.view.getRefObject().refGetPath().toXRI()),
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(paneIndex)), 
                new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                new Action.Parameter(Action.PARAMETER_PAGE, "0")
            },
            "|<&nbsp;",
            WebKeys.ICON_FIRST,
            true
       );
    }
    
    /**
     * Get selectFilter action.
     * 
     * @param filter
     * @return
     */
    public Action getSelectFilterAction(
        Filter filter
    ) {
        ApplicationContext app = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        HtmlEncoder_1_0 encoder = app.getHtmlEncoder();
        return
            new Action(
                GridSelectFilterAction.EVENT_ID,
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.view.getRefObject().refGetPath().toXRI()),
                    new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(gridControl.getPaneIndex())),
                    new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),
                    new Action.Parameter(Action.PARAMETER_NAME, encoder.encode(filter.getName(), false))
                },
                filter.getLabel(app.getCurrentLocaleAsIndex()),
                filter.getIconKey(),
                true
            );
    }
  
    /**
     * Get all filters for this grid.
     * 
     * @return
     */
    public Filter[] getFilters(
    ) {
        return this.filters;
    }
    
    /**
     * Get filter with given name.
     * 
     * @param filterName
     * @return
     */
    public Filter getFilter(
        String filterName
    ) {
        Filter[] filters = this.getFilters();
        for(int i = 0; i < filters.length; i++) {
            if((filters[i] != null) && filterName.equals(filters[i].getName())) {
                return filters[i];
            }
        }
        return null;
    }
    
    /**
     * Set filter with given name.
     * 
     * @param filterName
     * @param filter
     */
    public void setFilter(
        String filterName,
        Filter filter
    ) {
        Filter[] filters = this.getFilters();
        for(int i = 0; i < filters.length; i++) {
            if((filters[i] != null) && filterName.equals(filters[i].getName())) {
                filters[i] = filter;
                break;
            }
        }
    }
    
    /**
     * Get action for toggling column order.
     * 
     * @param forFeature
     * @return
     */
    public Action getTogglingColumnOrderAction(
    	String forFeature
    ) {
    	ApplicationContext app = this.view.getApplicationContext();  
    	GridControl gridControl = this.getGridControl();
    	short order = this.getOrder(forFeature);
    	int paneIndex = gridControl.getPaneIndex();
    	// toggle ANY -> ASCENDING -> DESCENDING -> ANY
    	// show icon of current ordering
    	try {
    		switch(SortOrder.valueOf(order)) {
    			case UNSORTED:
    				return new Action(
    					GridSetOrderAscendingAction.EVENT_ID,
    					new Action.Parameter[]{
    						new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
    						new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
    						new Action.Parameter(Action.PARAMETER_NAME, forFeature)
    					},
    					app.getTexts().getSortAscendingText(),
    					app.getTexts().getSortAscendingText(),
    					WebKeys.ICON_SORT_ANY,
    					true
    				);
    			case ASCENDING:
    				return new Action(
    					GridSetOrderDescendingAction.EVENT_ID,
    					new Action.Parameter[]{
    						new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
    						new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
    						new Action.Parameter(Action.PARAMETER_NAME, forFeature)
    					},
    					app.getTexts().getSortDescendingText(),
    					app.getTexts().getSortDescendingText(),
    					WebKeys.ICON_SORT_UP,
    					true
    				);
    			case DESCENDING:
    				return new Action(
    					GridSetOrderAnyAction.EVENT_ID,
    					new Action.Parameter[]{
    						new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
    						new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
    						new Action.Parameter(Action.PARAMETER_NAME, forFeature)
    					},
    					app.getTexts().getDisableSortText(),
    					app.getTexts().getDisableSortText(),
    					WebKeys.ICON_SORT_DOWN,
    					true
    				);
    			default: return null; // unreachable statement 
    		} 
    	} catch (IllegalArgumentException exception) {
    		return new Action(
    			Action.EVENT_NONE,
    			new Action.Parameter[]{},
    			app.getTexts().getSortAscendingText(),
    			app.getTexts().getSortAscendingText(),
    			WebKeys.ICON_SORT_ANY,
    			true
    		);          
    	}
    }

    /**
     * Get grid action to add column order.
     * 
     * @param forFeature
     * @return
     */
    public Action getColumnOrderAddAction(
    	String forFeature
    	) {
    	ApplicationContext application = this.view.getApplicationContext();  
    	GridControl gridControl = this.getGridControl();
    	short order = this.getOrder(forFeature);
    	// toggle ANY -> ASCENDING -> DESCENDING -> ANY
    	// show icon of current ordering
    	int paneIndex = gridControl.getPaneIndex();
    	try {
    		switch(SortOrder.valueOf(order)) {
    			case UNSORTED:
    				return new Action(
    					GridAddOrderAscendingAction.EVENT_ID,
    					new Action.Parameter[]{
    						new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
    						new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
    						new Action.Parameter(Action.PARAMETER_NAME, forFeature)
    					},
    					application.getTexts().getSortAscendingText(),
    					WebKeys.ICON_SORT_ANY,
    					true
    				);
    			case ASCENDING:
    				return new Action(
    					GridAddOrderDescendingAction.EVENT_ID,
    					new Action.Parameter[]{
    						new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
    						new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
    						new Action.Parameter(Action.PARAMETER_NAME, forFeature)
    					},
    					application.getTexts().getSortDescendingText(),
    					WebKeys.ICON_SORT_UP,
    					true
    				);
    			case DESCENDING:
    				return new Action(
    					GridAddOrderAnyAction.EVENT_ID,
    					new Action.Parameter[]{
    						new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
    						new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
    						new Action.Parameter(Action.PARAMETER_NAME, forFeature)
    					},
    					application.getTexts().getDisableSortText(),
    					WebKeys.ICON_SORT_DOWN,
    					true
    				);
    			default:  
    				return null; // unreachable statement
    		} 
    	} catch (IllegalArgumentException exception) {
    		return new Action(
    			Action.EVENT_NONE,
    			new Action.Parameter[]{},
    			application.getTexts().getSortAscendingText(),
    			WebKeys.ICON_SORT_ANY,
    			true
    		);
    	}
    }
    
    /**
     * Set showSearchForm property to given value.
     * 
     * @param newValue
     */
    public void setShowSearchForm(
        boolean newValue
    ) {
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        Properties settings = application.getSettings();
        settings.setProperty(
            gridControl.getPropertyName(
                gridControl.getQualifiedReferenceName(),
                UserSettings.SHOW_SEARCH_FORM.getName()
            ),
            "" + newValue
        );
    }

    /**
     * Set showGridContentOnInit flag to given value.
     * 
     * @param newValue
     */
    public void setShowGridContentOnInit(
        boolean newValue
    ) {
        // Update settings. The alignment setting will be reused for
        // all grids of the same type
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        Properties settings = application.getSettings();
        settings.setProperty(
            gridControl.getPropertyName(
                gridControl.getQualifiedReferenceName(),
                UserSettings.SHOW_ROWS_ON_INIT.getName()
            ),
            "" + newValue
        );
    }

    /**
     * Get grid alignment.
     * 
     * @return
     */
    public short getAlignment(
    ) {
        ApplicationContext app = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        String gridAlignmentPropertyName = gridControl.getPropertyName(
            gridControl.getQualifiedReferenceName(),
            UserSettings.PAGE_ALIGNMENT.getName()
        );
        if(app.getSettings().getProperty(gridAlignmentPropertyName) != null) {
            return Short.valueOf(app.getSettings().getProperty(gridAlignmentPropertyName));
        } else if(app.getSettings().getProperty(UserSettings.GRID_DEFAULT_ALIGNMENT_IS_WIDE.getName()) != null) {
        	return Boolean.valueOf(app.getSettings().getProperty(UserSettings.GRID_DEFAULT_ALIGNMENT_IS_WIDE.getName())) 
        		? ALIGNMENT_WIDE 
        		: ALIGNMENT_NARROW;
        } else {        
            return ALIGNMENT_NARROW;
        }
    }

    /**
     * Set grid alignment to given value.
     * 
     * @param alignment
     */
    public void setAlignment(
        short alignment
    ) {
        // update settings. The alignment setting will be reused for
        // all grids of the same type
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        Properties settings = application.getSettings();
        settings.setProperty(
            gridControl.getPropertyName(
                gridControl.getQualifiedReferenceName(),
                UserSettings.PAGE_ALIGNMENT.getName()
            ),
            "" + alignment
        );
    }

    /**
     * Get showGridContentOnInit flag value.
     * 
     * @return
     */
    public boolean showGridContentOnInit(
    ) {
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        return application.getPortalExtension().showGridContentOnInit(
            gridControl, 
            application
        );
    }
    
    /**
     * Get grid alignment action.
     * 
     * @return
     */
    public Action getAlignmentAction(
    ) {
      // toggle grid alignment: WIDE -> NARROW -> WIDE
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
      short alignment = this.getAlignment();
      return new Action(
          alignment == ALIGNMENT_NARROW
              ? GridSetAlignmentWideAction.EVENT_ID
              : GridSetAlignmentNarrowAction.EVENT_ID,
          new Action.Parameter[]{
              new Action.Parameter(Action.PARAMETER_PANE, "" + gridControl.getPaneIndex()),                      
              new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
          },
          alignment == ALIGNMENT_NARROW
              ? application.getTexts().getWideGridLayoutText()
              : application.getTexts().getNarrowGridLayoutText(),
          alignment == ALIGNMENT_NARROW
              ? WebKeys.ICON_PAGE_WIDE
              : WebKeys.ICON_PAGE_NARROW,
          true
        );
    }

    /**
     * Get setShowGridContentOnInit action.
     * 
     * @return
     */
    public Action getSetShowGridContentOnInitAction(
    ) {      
      // toggle show first page: NO -> YES -> NO
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        boolean showRowsOnInit = this.showGridContentOnInit();
        return new Action(
          showRowsOnInit
              ? GridSetHideRowsOnInitAction.EVENT_ID
              : GridSetShowRowsOnInitAction.EVENT_ID,
          new Action.Parameter[]{
              new Action.Parameter(Action.PARAMETER_PANE, "" + gridControl.getPaneIndex()),                      
              new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
          },
          showRowsOnInit
              ? application.getTexts().getShowRowsOnInitTitle()
              : application.getTexts().getHideRowsOnInitTitle(),
          showRowsOnInit
              ? WebKeys.ICON_SHOW_ROWS_ON_INIT
              : WebKeys.ICON_HIDE_ROWS_ON_INIT,
          true
        );
    }

    /**
     * Get pageSize parameter.
     * 
     * @param parameterMap
     * @return
     */
    public static int getPageSizeParameter(
        Map parameterMap
    ) {
        Object[] values = (Object[]) parameterMap.get(WebKeys.REQUEST_PARAMETER_PAGE_SIZE);
        String pageSize = values == null ? null : (values.length > 0 ? (String) values[0] : null);
        return pageSize == null ? -1 : Integer.parseInt(pageSize);
    }
    
    /**
     * Get show searchForm parameter.
     * 
     * @param parameterMap
     * @return
     */
    public static boolean getShowSearchFormParameter(
        Map parameterMap
    ) {
        Object[] values = (Object[]) parameterMap.get(WebKeys.REQUEST_PARAMETER_SHOW_SEARCH_FORM);
        String showSearchForm = values == null ? null : (values.length > 0 ? (String) values[0] : null);
        return showSearchForm == null ? false : Boolean.valueOf(showSearchForm);
    }
    
    /**
	 * Retrieve totalRows.
	 *
	 * @return Returns the totalRows.
	 */
	public Integer getTotalRows(
	) {
		return this.totalRows;
	}

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    public static final int MAX_PAGE_SIZE = 500;
  
    public static final int COLUMN_TYPE_NONE = 0;
    public static final int COLUMN_TYPE_STRING = 1;
    public static final int COLUMN_TYPE_DATE = 2;
    public static final int COLUMN_TYPE_NUMBER = 3;
    public static final int COLUMN_TYPE_BOOLEAN = 4;

    public static final short ALIGNMENT_NARROW = 0;
    public static final short ALIGNMENT_WIDE = 1;
  
    public static final int MAX_COLUMNS = 20;
  
    private int currentRow = -1;
    private int currentPage = 0;
    private int currentPageSize = -1;
    private int numberOfPages = Integer.MAX_VALUE;
    private Integer totalRows = null;
    private boolean showTotalRows = false;
	private Filter currentFilter = null;
    private Map<String,String> filterValues = new HashMap<String,String>();
    // Filter features added by setColumnFilter
    private Set<String> activeFilterFeatures = new HashSet<String>();
    private String currentFilterValue;
    private final Map<String,Short> columnSortOrders;
    private boolean showRowSelectors = false;
    private final String lookupType;
    private final Filter[] filters;
    private Object[] templateRows;
    private final Action[] objectCreators;
    private final Action addObjectAction;
    private final Action removeObjectAction;
    private final Action moveUpObjectAction;
    private final Action moveDownObjectAction;
    private final Action multiDeleteAction;
    private final Action saveAction;
    private final Action setCurrentFilterAsDefaultAction;
    protected final DataBinding dataBinding;
    private final boolean isComposite;
    private final boolean isChangeable;
  
    // holders for setPage() request
    private int setPageRequestNewPage = 0;
    private int setPageRequestNewPageSize = -1;
    private boolean showRows = true;
  
}
