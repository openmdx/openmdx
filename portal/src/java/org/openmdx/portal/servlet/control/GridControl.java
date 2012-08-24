/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: GridControlDef
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.GridAddColumnFilterAction;
import org.openmdx.portal.servlet.action.GridSetColumnFilterAction;
import org.openmdx.portal.servlet.attribute.AttributeValueFactory;
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.ui1.jmi1.CheckBox;
import org.openmdx.ui1.jmi1.DateField;
import org.openmdx.ui1.jmi1.NumberField;

public class GridControl 
    extends Control
    implements Serializable {

    //-----------------------------------------------------------------------
    public GridControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.ObjectContainer objectContainer,
        AttributeValueFactory valueFactory,
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
        this.containerId = new Path(objectContainer.refMofId()).getBase();
        this.objectContainer = objectContainer;
        
        // column filters
        List<Action> columnOrderActions = new ArrayList<Action>();
        List<Action> columnSearchActions = new ArrayList<Action>();
        List<Integer> columnTypes = new ArrayList<Integer>();
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
        columnTypes.add(
            new Integer(Grid.COLUMN_TYPE_NONE)
        );
        // column filter actions and initial sort orders
        this.initialColumnSortOrders = new HashMap<String,Short>();
        for(
            int i = 0; 
            (i < objectContainer.getMember().size()) && (i < Grid.MAX_COLUMNS); 
            i++
        ) {
          org.openmdx.ui1.jmi1.ValuedField columnDef = (org.openmdx.ui1.jmi1.ValuedField)objectContainer.getMember().get(i);
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
                  columnDef.isSortable() && !objectContainer.isReferenceIsStoredAsAttribute() ? 
                	  GridSetColumnFilterAction.EVENT_ID : 
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
                  columnDef.isFilterable() && !objectContainer.isReferenceIsStoredAsAttribute() ? 
                	  GridAddColumnFilterAction.EVENT_ID : 
                	  Action.EVENT_NONE,
                  new Action.Parameter[]{
                      new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(paneIndex)),
                      new Action.Parameter(Action.PARAMETER_REFERENCE, this.id),       
                      new Action.Parameter(Action.PARAMETER_NAME, columnDef.getFeatureName())          
                  },
                  columnLabel,
                  controlFactory.getTextsFactory().getTextsBundle(locale).getSearchIncrementallyText(),
                  WebKeys.ICON_SEARCH_INC,
                  true
              )
          );
          // columnTitle.isSortable() may throw NullPointer. Ignore it
          boolean columnTitleIsSortable = false;
          try {
              columnTitleIsSortable = columnDef.isSortable(); 
          } 
          catch(Exception e) {}
          if(!columnTitleIsSortable) {
              this.initialColumnSortOrders.put(
                  columnDef.getFeatureName(),
                  new Short(Short.MIN_VALUE)              
              );
          }
          columnTypes.add(
              new Integer(
                  columnDef instanceof DateField ? 
                	  Grid.COLUMN_TYPE_DATE : 
                	  columnDef instanceof NumberField ? 
                		  Grid.COLUMN_TYPE_NUMBER : 
                		  columnDef instanceof CheckBox ? 
                			  Grid.COLUMN_TYPE_BOOLEAN : 
                			  Grid.COLUMN_TYPE_STRING
              )
          );
          this.columnDefs.add(columnDef);
        }            
        this.columnTypes = new int[columnTypes.size()];
        for(int i = 0; i < columnTypes.size(); i++) {
            this.columnTypes[i] = ((Number)columnTypes.get(i)).intValue();
        }        
        this.columnOrderActions = (Action[])columnOrderActions.toArray(new Action[columnOrderActions.size()]);
        this.columnSearchActions = (Action[])columnSearchActions.toArray(new Action[columnSearchActions.size()]);        
        
    }
    
    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static boolean getShowRowSelectors(
        String lookupType,
        ModelElement_1_0 referencedType,
        Model_1_0 model
    ) {
        boolean showRowSelectors = false;
        try {
            Set allReferencedTypes = new HashSet();
            allReferencedTypes.addAll(referencedType.objGetList("allSubtype"));
            allReferencedTypes.addAll(referencedType.objGetList("allSupertype"));
            for(Iterator i = allReferencedTypes.iterator(); i.hasNext(); ) {
                ModelElement_1_0 type = model.getElement(i.next());
                if(model.isSubtypeOf(type, lookupType)) {
                    showRowSelectors = true;
                    break;
                }
            }
        }
        catch(Exception e) {}      
        return showRowSelectors;
    }

    //-----------------------------------------------------------------------
    /**
     * @return Returns columnSearchActions.
     */
    public Action[] getColumnSearchActions(
    ) {
        return this.columnSearchActions;
    }

    //-----------------------------------------------------------------------
    /**
     * @return Returns columnOrderActions.
     */
    public Action[] getColumnOrderActions(
    ) {
        return this.columnOrderActions;
    }

    //-----------------------------------------------------------------------
    /**
     * @return Returns the initialColumnSortOrders
     */
    public Map getInitialColumnSortOrders(
    ) {
        return this.initialColumnSortOrders;
    }
    
    //-----------------------------------------------------------------------
    /**
     * @return Returns the columnTypes.
     */
    public int[] getColumnTypes(
    ) {
        return columnTypes;
    }

    //-----------------------------------------------------------------------
    public List<org.openmdx.ui1.jmi1.ValuedField> getColumnDefs(
    ) {
    	return this.columnDefs;
    }
    
    //-----------------------------------------------------------------------
    /**
     * @return Returns the objectContainer.
     */
    public org.openmdx.ui1.jmi1.ObjectContainer getObjectContainer(
    ) {
        return objectContainer;
    }

    //-----------------------------------------------------------------------
    /**
     * @return Returns the paneIndex.
     */
    public int getPaneIndex(
    ) {
        return paneIndex;
    }

    //-----------------------------------------------------------------------
    /**
     * @return Returns true if grid supports in place editing.
     */
    public boolean inPlaceEditable(
    ) {
        return this.objectContainer.isInPlaceEditable() == null 
            ? false 
            : this.objectContainer.isInPlaceEditable().booleanValue(); 
    }
    
    //-------------------------------------------------------------------------
    public int getShowMaxMember(
    ) {
        Integer showMaxMember = this.getObjectContainer().getShowMaxMember();
        // first column contains object icon --> add 1 to number of customized columns 
        return showMaxMember == null
            ? this.getColumnTypes().length
            : java.lang.Math.min(showMaxMember.intValue()+1, this.getColumnTypes().length);
    }
  
    //-------------------------------------------------------------------------
    public String getQualifiedReferenceName(
    ) {
    	if(this.qualifiedReferenceName == null) {
    		this.qualifiedReferenceName = this.containerClass == null ? 
    			this.getObjectContainer().getReferenceName() : 
    				this.containerClass + ":" + this.getObjectContainer().getReferenceName();
    	}
    	return this.qualifiedReferenceName;
    }
  
    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
    public String getContainerClass(
    ) {
        return this.containerClass;
    }
    
    //-------------------------------------------------------------------------
    public String getContainerId(
    ) {
        return this.containerId;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
    }
  
    //-----------------------------------------------------------------------   
    private static final long serialVersionUID = 4049361894123256118L;
    
    private final org.openmdx.ui1.jmi1.ObjectContainer objectContainer;
    private final Action[] columnOrderActions;
    private final Action[] columnSearchActions;
    private final Map<String,Short> initialColumnSortOrders;
    private final int[] columnTypes;
    private final List<org.openmdx.ui1.jmi1.ValuedField> columnDefs;
    private final String containerClass;
    private final String containerId;
    private final int paneIndex;
    private String qualifiedReferenceName = null;
    private String qualifiedReferenceTypeName = null;

}

//--- End of File -----------------------------------------------------------
