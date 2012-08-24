/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: GridControl
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.portal.servlet.view;

import java.beans.ExceptionListener;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
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
import org.openmdx.portal.servlet.PortalExtension_1_0.ConditionParser;
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

@SuppressWarnings("unchecked")
public abstract class Grid
    extends ControlState
    implements Serializable {
  
    //-------------------------------------------------------------------------
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
                    null, null, null, null,
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
	
    //-------------------------------------------------------------------------
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
        
        this.columnSortOrders = new HashMap(control.getInitialColumnSortOrders());
        this.lookupType = lookupType;
        this.showRows = this.showGridContentOnInit();
        this.dataBinding = app.getPortalExtension().getDataBinding(
            control.getObjectContainer().getDataBindingName() 
        );
        
        // Filters
        if(control.getObjectContainer().isReferenceIsStoredAsAttribute()) {
            this.filters = new Filter[]{};
        }
        else {
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

        Map objectCreators = null;
        List templateRows = null;                
        if(!this.isChangeable) {
            objectCreators = null;
            this.addObjectAction = null;
            this.removeObjectAction = null;
            this.moveUpObjectAction = null;
            this.moveDownObjectAction = null;
        }
        else if(!this.isComposite) {
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
          }
          catch(ServiceException e) {
        	  SysLog.warning(e.getMessage(), e.getCause());
          }
          this.addObjectAction = addObjectAction;
          this.removeObjectAction = removeObjectAction;
          this.moveUpObjectAction = moveUpObjectAction;
          this.moveDownObjectAction = moveDownObjectAction;
        }
        else {
            this.addObjectAction = null;
            this.removeObjectAction = null;
            this.moveUpObjectAction = null;
            this.moveDownObjectAction = null;
            objectCreators = new TreeMap(); // sorted map of creators. key is inspector label
            templateRows = new ArrayList();
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
                            Map templateRowObject = new HashMap();
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
                                    null, null, null,
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
                                            null, null, null, null,
                                            app.getPortalExtension().getDataBinding(null)
                                        ),
                                        app
                                    );
                                }
                                else {
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
            }
            catch(ServiceException e) {
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

  //-------------------------------------------------------------------------
  public GridControl getGridControl(
  ) {
      return (GridControl)this.control;
  }

    //-------------------------------------------------------------------------
    public boolean getShowRows(
    ) {
        return this.showRows;
    }
  
    //-------------------------------------------------------------------------
    public void setShowRows(
        boolean newValue
    ) {
        this.showRows = newValue;
    }
  
    //-----------------------------------------------------------------------
    public Action getAddObjectAction(
    ) {
        return this.addObjectAction;
    }

    //-----------------------------------------------------------------------
    public Action getRemoveObjectAction(
    ) {
        return this.removeObjectAction;
    }

    //-----------------------------------------------------------------------
    public Action getMoveUpObjectAction(
    ) {
    	return this.moveUpObjectAction;
    }
    
    //-----------------------------------------------------------------------
    public Action getMoveDownObjectAction(
    ) {
    	return this.moveDownObjectAction;
    }
    
    //-----------------------------------------------------------------------
    public boolean isComposite(
    ) {
        return this.isComposite;
    }

    //-----------------------------------------------------------------------
    public boolean isChangeable(
    ) {
        return this.isChangeable;
    }    
    
    //-----------------------------------------------------------------------
    /**
     * @return Returns the objectCreators.
     */
    public Action[] getObjectCreators(
    ) {
        return objectCreators;
    }

    //-------------------------------------------------------------------------
    public Object[] getTemplateRow(
    ) {
        return this.templateRows;
    }

    //-------------------------------------------------------------------------
    public Action[] getObjectCreator(
    ) {
        return this.objectCreators;
    }
  
    //-------------------------------------------------------------------------
    public Action getMultiDeleteAction(
    ) {
        return this.multiDeleteAction;
    }
  
    //-------------------------------------------------------------------------
    public Action getSaveAction(
    ) {
        return this.saveAction;
    }

    //-------------------------------------------------------------------------
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

    //-------------------------------------------------------------------------
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
      
    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
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
      
    //-------------------------------------------------------------------------
    public Action getSetCurrentFilterAsDefaultAction(
    ) {
        return this.setCurrentFilterAsDefaultAction;
    }
      
  //-------------------------------------------------------------------------
  public void refresh(
      boolean refreshData
  ) {
      ApplicationContext application = this.view.getApplicationContext();
      Model_1_0 model = application.getModel();
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
          } 
          catch(Exception e) {}          
      }
      if(this.currentFilter == null) {
          this.selectFilter(
              Filters.DEFAULT_FILTER_NAME, 
              null 
          );
      }
      else {
    	  String filterName = this.currentFilter.getName();
          this.selectFilter(
              filterName,
              this.filterValues.get(filterName) == null ? 
            	  "" : 
            		  this.filterValues.get(filterName)
          );
      }
  }
  
  //-------------------------------------------------------------------------
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
              null, // Do not store PiggyBackConditions
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
  
  //-------------------------------------------------------------------------
  /**
   * Returns true if row selectors (check boxes) should be shown for each grid 
   * row. This allows the user to select a row of the grid. 
   * The method returns true, if
   * <ul>
   *   <li>lookupType != null and the grid contains objects which are instance of lookupType</li>
   * </ul>
   * false otherwise.
   */
  public boolean showRowSelectors(
  ) {
    return this.showRowSelectors;
  }
  
  //-------------------------------------------------------------------------
  public Object[] getRows(
	  PersistenceManager pm
  ) {
      List rows = new ArrayList();      
      if(this.showRows) {
          List filteredObjects =  this.getFilteredObjects(
        	  pm,
        	  this.currentFilter
          );
          int newPageSize = this.setPageRequestNewPageSize;
          int newPage = this.setPageRequestNewPage;
          
          ApplicationContext app = this.view.getApplicationContext();
          GridControl gridControl = this.getGridControl();
          
          int currentPageSize = this.getPageSize();
          newPageSize = (newPageSize <= 0) || (newPageSize > MAX_PAGE_SIZE) ? 
        	  currentPageSize : 
        		  newPageSize;
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
          }
          else if(newPage == this.currentPage + 1) {
              firstRow = this.currentRow >= 0
                  ? this.currentRow
                  : firstRow;
          }
          else if(newPage == this.currentPage - 1) {
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
                  }
                  catch(Exception e) {
                      hasNext = false;
                  }
                  if(hasNext || (newPage == 0)) {
                      if(newPage == lastEmptyPage - 1) break;
                      newPage++;
                  }
                  else {
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
              gridControl.getShowMaxMember(),
              Math.min(
                  gridControl.getObjectContainer().getMember().size(),
                  Grid.MAX_COLUMNS
              )
          ) + 1;
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
	                      null, null, null, null,
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
	                              null, null, null, null,
	                              app.getPortalExtension().getDataBinding(null)
	                          ),
	                          app
	                      );
	                  }
	                  else {
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
	                      }
	                      catch(Exception e) {
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

  //-------------------------------------------------------------------------
  public void setPage(
      int newPage,
      int newPageSize
  ) {
      this.setPageRequestNewPage = newPage;
      this.setPageRequestNewPageSize = newPageSize;
  }
  
  //-------------------------------------------------------------------------
  public int getCurrentPage(
  ) {
    return this.currentPage;
  }

  //-------------------------------------------------------------------------
  public int getLastPage(
  ) {
    return java.lang.Math.max(0, this.numberOfPages - 1);
  }
  
  //-------------------------------------------------------------------------
  public int getPageSize(
  ) {
      ApplicationContext application = this.view.getApplicationContext();
      GridControl gridControl = this.getGridControl();
      
      if(this.currentPageSize < 0) {
          String pageSizePropertyName = this.getGridControl().getPropertyName(
              this.getGridControl().getQualifiedReferenceName(),
              UserSettings.PAGE_SIZE.getName()
          );
          if(application.getSettings().getProperty(pageSizePropertyName) != null) {
             this.currentPageSize = 
                  Short.parseShort(application.getSettings().getProperty(pageSizePropertyName));
          }
          else {
              this.currentPageSize = 
                  application.getPortalExtension().getGridPageSize(
                      gridControl.getObjectContainer().getReferencedTypeName()
                  );
          }
      }
      return this.currentPageSize;
  }
  
    //-------------------------------------------------------------------------
    abstract public List<RefObject_1_0> getFilteredObjects(
    	PersistenceManager pm,
        Filter filter
    );

    //-------------------------------------------------------------------------
    abstract public Collection<RefObject_1_0> getAllObjects(
    	PersistenceManager pm
    );
    
    //-------------------------------------------------------------------------
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
			SysLog.info("filter " + filterName + " not found");
		}
		else {
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
    						List values = new ArrayList();
    						StringTokenizer tokenizer = new StringTokenizer(filterValues, ",;");
    						while(tokenizer.hasMoreTokens()) {
    							values.add(tokenizer.nextToken());
    						}
    						if(values.size() == 0) {
    							values.add("");
    						}
    						newCondition.setValue(
    							values.toArray(new Object[values.size()])
    						);
						}
						// ${name}
						else {
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

    //-------------------------------------------------------------------------
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
    	// clear column sort indicators if new filter is set
    	if(!add) {
    		this.columnSortOrders.clear();
    		this.columnSortOrders.putAll(
    			gridControl.getInitialColumnSortOrders()
    		);
    	}    
    	// find column with given column title
    	for(
    		int i = 0; 
    		(i < gridControl.getObjectContainer().getMember().size()) && (i < MAX_COLUMNS); 
    		i++
    	) {
    		org.openmdx.ui1.jmi1.ValuedField column = (org.openmdx.ui1.jmi1.ValuedField)gridControl.getObjectContainer().getMember().get(i);
    		if(filterName.equals(column.getFeatureName())) {
    			Filter baseFilter = add ? this.currentFilter : this.getFilter("All");
    			List<Condition> conditions = new ArrayList<Condition>(
    				baseFilter.getCondition()  
    			);
    			Extension extension = baseFilter.getExtension();
    			if(extension != null) {
    				extension = extension.clone();
    			}
    			// Number
    			if(column instanceof org.openmdx.ui1.jmi1.NumberField) {
    				// Code
    				if(
    					(app.getCodes() != null) &&
    					(app.getCodes().getShortText(column.getQualifiedFeatureName(), (short)0, true, true) != null)
    				) {
    					SysLog.detail("Code filter values", filterValues);
    					Map<String,Short> shortTexts = app.getCodes().getShortTextByText(
    						column.getQualifiedFeatureName(),
    						app.getCurrentLocaleAsIndex(),
    						true
    					);
    					Map<String,Short> longTexts = app.getCodes().getLongTextByText(
    						column.getQualifiedFeatureName(),
    						app.getCurrentLocaleAsIndex(),
    						true
    					);
    					StringTokenizer andExpr = new StringTokenizer(filterValues, "&");
    					while(andExpr.hasMoreTokens()) {
    						ConditionParser parser = app.getPortalExtension().getConditionParser(
    							column, 
    							new IsInCondition(
    								Quantifier.THERE_EXISTS,
    								column.getFeatureName(),
    								true,
    								(Object[])null
    							)
    						);
    						List<Object> values = new ArrayList();
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
    									// Try to match a long text
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
    							} catch(NumberFormatException e) {}
    						}
    						if(!values.isEmpty()) {
    							condition.setValue(values.toArray());
    							conditions.add(condition);
    						}
    					}
    				}
    				// Number
    				else {
    					SysLog.detail("Number filter values", filterValues);
    					StringTokenizer andExpr = new StringTokenizer(filterValues, "&");
    					while(andExpr.hasMoreTokens()) {
    						ConditionParser parser = app.getPortalExtension().getConditionParser(
    							column, 
    							new IsInCondition(
    								Quantifier.THERE_EXISTS,
    								column.getFeatureName(),
    								true,
    								(Object[])null
    							)
    						);
    						List<Object> values = new ArrayList();
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
    							conditions.add(
    								condition
    							);
    						}
    					}
    				}
    			}
    			// Date
    			else if(column instanceof org.openmdx.ui1.jmi1.DateField) {
    				SysLog.detail("Date filter values", filterValues);
    				SimpleDateFormat dateParser = (SimpleDateFormat)SimpleDateFormat.getDateInstance(
    					java.text.DateFormat.SHORT,
    					this.getCurrentLocale()
    				);            
    				StringTokenizer andExpr = new StringTokenizer(filterValues, "&");
    				while(andExpr.hasMoreTokens()) {
    					ConditionParser parser = app.getPortalExtension().getConditionParser(
    						column, 
    						new IsBetweenCondition(
    							Quantifier.THERE_EXISTS,
    							column.getFeatureName(),
    							true,
    							null, 
    							null
    						)
    					);
    					Condition condition = null;
    					List<Object> values = new ArrayList();
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
    						conditions.add(
    							condition
    						);
    					}
    				}
    			}
    			// Boolean
    			else if(column instanceof org.openmdx.ui1.jmi1.CheckBox) {
    				SysLog.detail("Boolean filter values", filterValues);
    				List values = new ArrayList();
    				StringTokenizer tokenizer = new StringTokenizer(filterValues, ";");
    				while(tokenizer.hasMoreTokens()) {
    					String token = tokenizer.nextToken();
    					if(app.getTexts().getTrueText().equals(token)) {
    						values.add(
    							Boolean.TRUE
    						);
    					}
    					else {
    						values.add(
    							Boolean.FALSE
    						);
    					}
    				}
    				if(!values.isEmpty()) {
    					conditions.add(
    						new IsInCondition(
    							Quantifier.THERE_EXISTS,
    							column.getFeatureName(),
    							true,
    							values.toArray(new Object[values.size()])
    						)
    					);
    				}
    			}
    			// String
    			else {
    				SysLog.detail("String filter values", filterValues);
    				Pattern pattern = Pattern.compile("[\\s\\&]*(?:(?:\"([^\"]*)\")|([^\\s\"\\&]+))");
    				Matcher matcher = pattern.matcher(filterValues);
    				while(matcher.find()) {
    					for(int j = 1; j <= matcher.groupCount(); j++) {
    						String andExpr = matcher.group(j);
    						if(andExpr != null) {
    							// if getQuery() returns a query extension it must be merged with 
    							// the base query which might also contain a query extension
    							org.openmdx.base.query.Filter query = app.getPortalExtension().getQuery(
    								column,
    								andExpr,
    								extension == null ? 0 : extension.getStringParam().size(),
    								app
    							);
    							if(query != null) {
    								conditions.addAll(query.getCondition());
    								Extension queryExtension = query.getExtension();
    								if(queryExtension != null) {
    									//
    									// Merge base query with returned query
    									//
    									if(extension == null) {
    										extension = queryExtension.clone();
    									} 
    									else {
    										// Merged clause
    										extension.setClause(
    											extension.getClause() + " AND " + queryExtension.getClause()
    										);
    										// Merged string parameter
    										extension.getStringParam().addAll(
    											queryExtension.getStringParam()
    										);
    									}
    								}
    							} 
    							else {
    								ConditionParser conditionParser = app.getPortalExtension().getConditionParser(
    									column, 
    									new IsLikeCondition(
        									Quantifier.THERE_EXISTS,
        									column.getFeatureName(),
        									true,
        									(Object[])null
        								)
    								);
    								Condition condition = null;
    								List<Object> values = new ArrayList();
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
    									conditions.add(condition);
    								}
    							}
    						}
    					}
    				}
    			}          
    			this.currentFilter = new Filter(
    				column.getFeatureName(),
    				null,
    				"",
    				WebKeys.ICON_DEFAULT,
    				null,
    				conditions,
    				null, // order
    				extension,
    				this.getGridControl().getContainerId(), 
    				this.view.getApplicationContext().getCurrentUserRole(), 
    				this.view.getObjectReference().getXRI()               
    			);
    			break;
    		}
    	}
    	SysLog.detail("selected filter", this.currentFilter);
    	this.numberOfPages = Integer.MAX_VALUE;
    	this.setPage(
    		0,
    		newPageSize
    	);
    }
  
  //-------------------------------------------------------------------------
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
		  List orderSpecifier = new ArrayList(this.currentFilter.getOrderSpecifier());
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
	  SysLog.detail("order by filter", this.currentFilter);
	  this.numberOfPages = Integer.MAX_VALUE;
	  this.setPage(
		  0,
		  -1 // do not change page size
	  );
  }
  
    //-------------------------------------------------------------------------
    public short getOrder(
    	String feature
    ) {
	    Short order = (Short)this.columnSortOrders.get(feature);
	    return order == null
	      ? SortOrder.UNSORTED.code()
	      : order.shortValue();
    }

    //-------------------------------------------------------------------------
    public Filter getCurrentFilter(
    ) {
    	return this.currentFilter;
    }
  
    //-------------------------------------------------------------------------
    public boolean hasFilterValues(
    ) {
    	for(String filterValue: this.filterValues.values()) {
    		if(filterValue != null && filterValue.length() != 0) {
    			return true;
    		}
    	}
    	return false;
    }
    
    //-------------------------------------------------------------------------
    public String getFilterValue(
	    String filterName
    ) {
	    return this.filterValues.get(filterName) == null ?
	    	"" :
	    		this.filterValues.get(filterName);
    }
  
    //-------------------------------------------------------------------------
    public String getCurrentFilterValue(
    ) {
    	return this.currentFilterValue;
    }
  
    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
    public Action getSelectFilterAction(
        Filter filter
    ) {
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        HtmlEncoder_1_0 encoder = application.getHtmlEncoder();
        return
            new Action(
                GridSelectFilterAction.EVENT_ID,
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.view.getRefObject().refGetPath().toXRI()),
                    new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(gridControl.getPaneIndex())),
                    new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),
                    new Action.Parameter(Action.PARAMETER_NAME, encoder.encode(filter.getName(), false))
                },
                filter.getLabel(application.getCurrentLocaleAsIndex()),
                filter.getIconKey(),
                true
            );
    }
  
    //-------------------------------------------------------------------------
    public Filter[] getFilters(
    ) {
        return this.filters;
    }
    
    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
    public Action getTogglingColumnOrderAction(
        String forFeature
    ) {
      ApplicationContext application = this.view.getApplicationContext();  
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
              application.getTexts().getSortAscendingText(),
              application.getTexts().getSortAscendingText(),
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
              application.getTexts().getSortDescendingText(),
              application.getTexts().getSortDescendingText(),
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
              application.getTexts().getDisableSortText(),
              application.getTexts().getDisableSortText(),
              WebKeys.ICON_SORT_DOWN,
              true
          );
          default: return null; // unreachable statement 
        } 
      } catch (IllegalArgumentException exception) {
          return new Action(
              Action.EVENT_NONE,
              new Action.Parameter[]{},
              application.getTexts().getSortAscendingText(),
              application.getTexts().getSortAscendingText(),
              WebKeys.ICON_SORT_ANY,
              true
          );          
       }
      
    }

    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
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

    //-------------------------------------------------------------------------
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

    //-------------------------------------------------------------------------
    public short getAlignment(
    ) {
        // Alignment
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        String gridAlignmentPropertyName = gridControl.getPropertyName(
            gridControl.getQualifiedReferenceName(),
            UserSettings.PAGE_ALIGNMENT.getName()
        );
        if(application.getSettings().getProperty(gridAlignmentPropertyName) != null) {
            return Short.valueOf(application.getSettings().getProperty(gridAlignmentPropertyName));
        }
        else if(application.getSettings().getProperty(UserSettings.GRID_DEFAULT_ALIGNMENT_IS_WIDE.getName()) != null) {
        	return Boolean.valueOf(application.getSettings().getProperty(UserSettings.GRID_DEFAULT_ALIGNMENT_IS_WIDE.getName())) ?
        		ALIGNMENT_WIDE : 
        			ALIGNMENT_NARROW;
        } else {        
            return ALIGNMENT_NARROW;
        }
    }
    
    //-------------------------------------------------------------------------
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

    //-------------------------------------------------------------------------
    public boolean showGridContentOnInit(
    ) {
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        return application.getPortalExtension().showGridContentOnInit(
            gridControl, 
            application
        );
      }
    
    //-------------------------------------------------------------------------
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

    //-------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    public static int getPageSizeParameter(
        Map parameterMap
    ) {
        Object[] values = (Object[]) parameterMap.get(WebKeys.REQUEST_PARAMETER_PAGE_SIZE);
        String pageSize = values == null ? null : (values.length > 0 ? (String) values[0] : null);
        return pageSize == null ? -1 : Integer.parseInt(pageSize);
    }
    
    // -------------------------------------------------------------------------
    public static boolean getShowSearchFormParameter(
        Map parameterMap
    ) {
        Object[] values = (Object[]) parameterMap.get(WebKeys.REQUEST_PARAMETER_SHOW_SEARCH_FORM);
        String showSearchForm = values == null ? null : (values.length > 0 ? (String) values[0] : null);
        return showSearchForm == null ? false : Boolean.valueOf(showSearchForm);
    }
    
    //-------------------------------------------------------------------------
    // Variables
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
    private Filter currentFilter = null;
    private Map<String,String> filterValues = new HashMap<String,String>();
    private String currentFilterValue;
    private final Map columnSortOrders;
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

//--- End of File -----------------------------------------------------------
