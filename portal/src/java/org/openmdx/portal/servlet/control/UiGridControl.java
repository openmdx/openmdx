/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: GridControl
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.UiGridAddColumnFilterAction;
import org.openmdx.portal.servlet.action.UiGridSetColumnFilterAction;
import org.openmdx.portal.servlet.action.UiGridSwapColumnOrderAction;
import org.openmdx.portal.servlet.component.UiGrid;
import org.openmdx.portal.servlet.component.UiGrid.ColumnType;
import org.openmdx.ui1.jmi1.CheckBox;
import org.openmdx.ui1.jmi1.DateField;
import org.openmdx.ui1.jmi1.NumberField;
import org.openmdx.ui1.jmi1.ObjectReferenceField;

/**
 * GridControl
 *
 */
public class UiGridControl extends Control implements Serializable {

    /**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param gridDef
     * @param valueFactory
     * @param containerClass
     * @param paneIndex
     */
    public UiGridControl(
        String id,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.ObjectContainer gridDef,
        String containerClass,
        int paneIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
        this.containerClass = containerClass;
        this.paneIndex = paneIndex;        
        this.containerId = gridDef.refGetPath().getLastSegment().toClassicRepresentation();
        this.objectContainer = gridDef;
        // column filters
        List<Action> columnOrderActions = new ArrayList<Action>();
        List<Action> columnSearchActions = new ArrayList<Action>();
        List<ColumnType> columnTypes = new ArrayList<ColumnType>();
        this.columnDefs = new ArrayList<org.openmdx.ui1.jmi1.ValuedField>();
        // first column contains object icon
        columnOrderActions.add(
    		new Action(
				Action.EVENT_NONE,
				null,
				"",
				true
			)
		);
        columnSearchActions.add(
    		new Action(
				Action.EVENT_NONE,
				null,
				"",
				true
			)
		);
        columnTypes.add(ColumnType.NONE);
        // column filter actions and initial sort orders
        this.initialColumnSortOrders = new HashMap<String,Short>();
        for(
            int i = 0; 
            (i < gridDef.getMember().size()) && (i < UiGrid.MAX_COLUMNS); 
            i++
        ) {
          org.openmdx.ui1.jmi1.ValuedField columnDef = (org.openmdx.ui1.jmi1.ValuedField)gridDef.getMember().get(i);
          String columnLabel = 
              this.localeAsIndex < columnDef.getShortLabel().size() ? 
            	  columnDef.getShortLabel().get(this.localeAsIndex) : 
	            	  !columnDef.getShortLabel().isEmpty() ? columnDef.getShortLabel().get(0) : 
	            		  this.localeAsIndex < columnDef.getLabel().size() ?
	            			  columnDef.getLabel().get(this.localeAsIndex) :
	            				  !columnDef.getLabel().isEmpty() ? columnDef.getLabel().get(0) : "N/A";
          String columnToolTip =
              this.localeAsIndex < columnDef.getToolTip().size() ? 
            	  columnDef.getToolTip().get(this.localeAsIndex) : 
            	  !columnDef.getToolTip().isEmpty() ? columnDef.getToolTip().get(0) : "N/A";
          columnOrderActions.add(
              new Action(
                  columnDef.isSortable() && !gridDef.isReferenceIsStoredAsAttribute() ? 
                	  UiGridSetColumnFilterAction.EVENT_ID : 
                	  Action.EVENT_NONE,
                  new Action.Parameter[]{
                      new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(paneIndex)),
                      new Action.Parameter(Action.PARAMETER_REFERENCE, this.id),       
                      new Action.Parameter(Action.PARAMETER_NAME, columnDef.getFeatureName())
                  },
                  columnLabel,
                  columnToolTip,
                  columnDef.getIconKey(),
                  true
              )
          );
          columnSearchActions.add(
              new Action(
                  columnDef.isFilterable() && !gridDef.isReferenceIsStoredAsAttribute() ? 
                	  UiGridAddColumnFilterAction.EVENT_ID : 
                	  Action.EVENT_NONE,
                  new Action.Parameter[]{
                      new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(paneIndex)),
                      new Action.Parameter(Action.PARAMETER_REFERENCE, this.id),       
                      new Action.Parameter(Action.PARAMETER_NAME, columnDef.getFeatureName())          
                  },
                  columnLabel,
                  columnLabel,
                  WebKeys.ICON_SEARCH_INC,
                  true
              )
          );
          // columnTitle.isSortable() may throw NullPointer. Ignore it
          boolean columnTitleIsSortable = false;
          try {
              columnTitleIsSortable = columnDef.isSortable(); 
          } catch(Exception e) {}
          if(!columnTitleIsSortable) {
              this.initialColumnSortOrders.put(
                  columnDef.getFeatureName(),
                  new Short(Short.MIN_VALUE)              
              );
          }
          if(columnDef instanceof ObjectReferenceField) {
        	  columnTypes.add(ColumnType.OBJREF);
          } else if(columnDef instanceof DateField) {
        	  columnTypes.add(ColumnType.DATE);
          } else if(columnDef instanceof NumberField) {
        	  columnTypes.add(ColumnType.NUMBER);
          } else if(columnTypes instanceof CheckBox) {
        	  columnTypes.add(ColumnType.BOOLEAN);
          } else {
        	  columnTypes.add(ColumnType.STRING);
          }
          this.columnDefs.add(columnDef);
        }
        this.columnTypes = columnTypes;
        this.columnOrderActions = columnOrderActions;
        this.columnSearchActions = columnSearchActions;        
        
    }
    
    /**
     * Return true if row selectors should be shown.
     * 
     * @param lookupType
     * @param referencedType
     * @param model
     * @return
     */
    public static boolean getShowRowSelectors(
        String lookupType,
        ModelElement_1_0 referencedType,
        Model_1_0 model
    ) {
        boolean showRowSelectors = false;
        try {
            Set<Object> allReferencedTypes = new HashSet<Object>();
            allReferencedTypes.addAll(referencedType.objGetList("allSubtype"));
            allReferencedTypes.addAll(referencedType.objGetList("allSupertype"));
            for(Iterator<Object> i = allReferencedTypes.iterator(); i.hasNext(); ) {
                ModelElement_1_0 type = model.getElement(i.next());
                if(model.isSubtypeOf(type, lookupType)) {
                    showRowSelectors = true;
                    break;
                }
            }
        } catch(Exception e) {}      
        return showRowSelectors;
    }

    /**
     * Get column search actions.
     * 
     * @return Returns columnSearchActions.
     */
    public List<Action> getColumnSearchActions(
    ) {
        return this.columnSearchActions;
    }

    /**
     * Get column order actions.
     * 
     * @return Returns columnOrderActions.
     */
    public List<Action> getColumnOrderActions(
    ) {
        return this.columnOrderActions;
    }

    /**
     * Get toggle column order action.
     * 
     * @param i
     * @param j
     * @return
     */
    public Action getSwapColumnOrderAction(
    	int i,
    	int j
    ) {
    	return new Action(
			UiGridSwapColumnOrderAction.EVENT_ID,
			new Action.Parameter[]{
				new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(this.getPaneIndex())),
				new Action.Parameter(Action.PARAMETER_REFERENCE, this.getId()),
				new Action.Parameter(Action.PARAMETER_REFERENCE_NAME, this.getObjectContainer().getReferenceName()),
				new Action.Parameter(Action.PARAMETER_SWAP_GRID_COLUMNS, i + "," + j)
			},
			"POS: " + i + " → " + j,
			"POS: " + i + " → " + j,
			null,
			true
		);
    }

    /**
     * Get initial sort orders for columns.
     * 
     * @return Returns the initialColumnSortOrders
     */
    public Map<String,Short> getInitialColumnSortOrders(
    ) {
        return this.initialColumnSortOrders;
    }
    
    /**
     * Get column types.
     * 
     * @return Returns the columnTypes.
     */
    public List<ColumnType> getColumnTypes(
    ) {
        return columnTypes;
    }

    /**
     * Get column definitions.
     * 
     * @return
     */
    public List<org.openmdx.ui1.jmi1.ValuedField> getColumnDefs(
    ) {
    	return this.columnDefs;
    }
    
    /**
     * Get object container.
     * 
     * @return Returns the objectContainer.
     */
    public org.openmdx.ui1.jmi1.ObjectContainer getObjectContainer(
    ) {
        return objectContainer;
    }

    /**
     * Get pane index.
     * 
     * @return Returns the paneIndex.
     */
    public int getPaneIndex(
    ) {
        return paneIndex;
    }

    /**
     * Return true if grid is in-place editable.
     * 
     * @return Returns true if grid supports in place editing.
     */
    public boolean inPlaceEditable(
    ) {
        return this.objectContainer.isInPlaceEditable() == null 
            ? false 
            : this.objectContainer.isInPlaceEditable().booleanValue(); 
    }
    
    /**
     * Get show max member count.
     * 
     * @return
     */
    public int getShowMaxMember(
    ) {
        Integer showMaxMember = this.getObjectContainer().getShowMaxMember();
        // first column contains object icon --> add 1 to number of customized columns 
        return showMaxMember == null
            ? this.getColumnTypes().size()
            : java.lang.Math.min(showMaxMember.intValue() + 1, this.getColumnTypes().size());
    }
  
    /**
     * Get grid's qualified reference name.
     * 
     * @return
     */
    public String getQualifiedReferenceName(
    ) {
    	if(this.qualifiedReferenceName == null) {
    		this.qualifiedReferenceName = this.containerClass == null ? 
    			this.getObjectContainer().getReferenceName() : 
    				this.containerClass + ":" + this.getObjectContainer().getReferenceName();
    	}
    	return this.qualifiedReferenceName;
    }
  
    /**
     * Get grid's qualified type name.
     * 
     * @return
     */
    public String getQualifiedReferenceTypeName(
    ) {
    	if(this.qualifiedReferenceTypeName == null) {
    		String qualifiedName = this.getObjectContainer().refGetPath().getBase();
    		int pos = qualifiedName.indexOf(":Ref:");
    		if(pos > 0) {
    			this.qualifiedReferenceTypeName = qualifiedName.substring(0, pos) + ":" + this.objectContainer.getReferenceName();
    		} else {
    			this.qualifiedReferenceTypeName = this.getQualifiedReferenceName();
    		}
    	}
    	return this.qualifiedReferenceTypeName;
    }
    
    /**
     * Get class name of container.
     * 
     * @return
     */
    public String getContainerClass(
    ) {
        return this.containerClass;
    }
    
    /**
     * Get id of container.
     * 
     * @return
     */
    public String getContainerId(
    ) {
        return this.containerId;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.Control#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
     */
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
    }
  
	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.Control#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		return Collections.emptyList();
	}

	//-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------   
    private static final long serialVersionUID = 4049361894123256118L;
    
    private final org.openmdx.ui1.jmi1.ObjectContainer objectContainer;
    private final List<Action> columnOrderActions;
    private final List<Action> columnSearchActions;
    private final Map<String,Short> initialColumnSortOrders;
    private final List<ColumnType> columnTypes;
    private final List<org.openmdx.ui1.jmi1.ValuedField> columnDefs;
    private final String containerClass;
    private final String containerId;
    private final int paneIndex;
    private String qualifiedReferenceName = null;
    private String qualifiedReferenceTypeName = null;

}

//--- End of File -----------------------------------------------------------
