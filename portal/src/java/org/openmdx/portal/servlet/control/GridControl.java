/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Name:        $Id: GridControl.java,v 1.43 2009/03/08 18:03:25 wfro Exp $
 * Description: GridControlDef
 * Revision:    $Revision: 1.43 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:03:25 $
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
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.WebKeys;
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
        List<Action> columnFilterSetActions = new ArrayList<Action>();
        List<Action> columnFilterAddActions = new ArrayList<Action>();
        List<Integer> columnTypes = new ArrayList<Integer>();
    
        // first column contains object icon
        columnFilterSetActions.add(
          new Action(
            Action.EVENT_NONE,
            null,
            "",
            true
          )
        );
        columnFilterAddActions.add(
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
          org.openmdx.ui1.jmi1.ValuedField columnTitle = (org.openmdx.ui1.jmi1.ValuedField)objectContainer.getMember().get(i);
          String columnLabel = 
              this.localeAsIndex < columnTitle.getShortLabel().size()
                  ? columnTitle.getShortLabel().get(this.localeAsIndex)
                  : columnTitle.getShortLabel().size() > 0 ? columnTitle.getShortLabel().get(0) : "N/A";
          String columnToolTip =
              this.localeAsIndex < columnTitle.getToolTip().size()
                  ? columnTitle.getToolTip().get(this.localeAsIndex)
                  : columnTitle.getToolTip().size() > 0 ? columnTitle.getToolTip().get(0) : "N/A";
          columnFilterSetActions.add(
              new Action(
                  columnTitle.isFilterable() && !objectContainer.isReferenceIsStoredAsAttribute() 
                      ? Action.EVENT_SET_COLUMN_FILTER 
                      : Action.EVENT_NONE,
                  new Action.Parameter[]{
                      new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex),
                      new Action.Parameter(Action.PARAMETER_REFERENCE, this.id),       
                      new Action.Parameter(Action.PARAMETER_NAME, columnTitle.getFeatureName())
                  },
                  columnLabel,
                  columnToolTip,
                  columnTitle.getIconKey(),
                  true
              )
          );
          columnFilterAddActions.add(
              new Action(
                  columnTitle.isFilterable() && !objectContainer.isReferenceIsStoredAsAttribute() 
                      ? Action.EVENT_ADD_COLUMN_FILTER 
                      : Action.EVENT_NONE,
                  new Action.Parameter[]{
                      new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex),
                      new Action.Parameter(Action.PARAMETER_REFERENCE, this.id),       
                      new Action.Parameter(Action.PARAMETER_NAME, columnTitle.getFeatureName())          
                  },
                  columnLabel,
                  controlFactory.getTextsFactory().getTexts(locale).getSearchIncrementallyText(),
                  WebKeys.ICON_SEARCH_INC,
                  true
              )
          );
          // columnTitle.isSortable() may throw NullPointer. Ignore it
          boolean columnTitleIsSortable = false;
          try {
              columnTitleIsSortable = columnTitle.isSortable(); 
          } 
          catch(Exception e) {}
          if(!columnTitleIsSortable) {
              this.initialColumnSortOrders.put(
                  columnTitle.getFeatureName(),
                  new Short(Short.MIN_VALUE)              
              );
          }
          columnTypes.add(
              new Integer(
                  columnTitle instanceof DateField
                      ? Grid.COLUMN_TYPE_DATE
                      : columnTitle instanceof NumberField
                          ? Grid.COLUMN_TYPE_NUMBER
                          : columnTitle instanceof CheckBox
                              ? Grid.COLUMN_TYPE_BOOLEAN
                              : Grid.COLUMN_TYPE_STRING
              )
          );
        }
            
        this.columnTypes = new int[columnTypes.size()];
        for(int i = 0; i < columnTypes.size(); i++) {
            this.columnTypes[i] = ((Number)columnTypes.get(i)).intValue();
        }        
        this.columnFilterSetActions = (Action[])columnFilterSetActions.toArray(new Action[columnFilterSetActions.size()]);
        this.columnFilterAddActions = (Action[])columnFilterAddActions.toArray(new Action[columnFilterAddActions.size()]);        
        
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
     * @return Returns the columnFilterAddActions.
     */
    public Action[] getColumnFilterAddActions() {
        return columnFilterAddActions;
    }

    //-----------------------------------------------------------------------
    /**
     * @return Returns the columnFilterSetActions.
     */
    public Action[] getColumnFilterSetActions() {
        return columnFilterSetActions;
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
        return this.containerClass == null
            ? this.getObjectContainer().getReferenceName()
            : this.containerClass + ":" + this.getObjectContainer().getReferenceName();      
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
        HtmlPage p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
    }
  
    //-----------------------------------------------------------------------   
    private static final long serialVersionUID = 4049361894123256118L;
    
    public static final String PROPERTY_DEFAULT_FILTER = "Filter.Default";  
    public static final String PROPERTY_PAGE_SIZE = "Page.Size";
      
    private final org.openmdx.ui1.jmi1.ObjectContainer objectContainer;
    private final Action[] columnFilterSetActions;
    private final Action[] columnFilterAddActions;
    private final Map<String,Short> initialColumnSortOrders;
    private final int[] columnTypes;
    private final String containerClass;
    private final String containerId;
    private final int paneIndex;      

}

//--- End of File -----------------------------------------------------------
