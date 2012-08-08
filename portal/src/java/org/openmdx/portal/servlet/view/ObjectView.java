/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ObjectView.java,v 1.12 2008/05/27 23:18:51 wfro Exp $
 * Description: View 
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/27 23:18:51 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.control.ControlFactory;
import org.openmdx.portal.servlet.control.InspectorControl;
import org.openmdx.uses.org.apache.commons.collections.MapUtils;

//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public abstract class ObjectView
    extends View
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public ObjectView(
        String id,
        String containerElementId,
        RefObject_1_0 object,
        ApplicationContext application,
        Map<Path,Action> historyActions,
        String lookupType,
        Map restrictToElements,
        ControlFactory controlFactory
    ) {
        super(
            id,
            containerElementId,
            object,
            application,
            controlFactory
        );
        this.objectReference = new ObjectReference(
            object,
            application
        );      
        this.historyActions = historyActions;
        this.lookupType = lookupType;
        this.restrictToElements = restrictToElements;   
    }

    // -------------------------------------------------------------------------
    public AttributePane getAttributePane(
    ) {
        return this.attributePane;
    }

    //-------------------------------------------------------------------------
    public ObjectReference getObjectReference(
    ) {
        return this.objectReference;
    }

    //-------------------------------------------------------------------------
    public RefObject_1_0 getRefObject(
    ) {
        return (RefObject_1_0)this.getObject();      
    }
  
    //-------------------------------------------------------------------------
    public Action[] getSelectParentAction(
    ) {
        RefObject_1_0 object = this.getRefObject();
        List selectParentActions = new ArrayList();
        if((object != null) && (object.refMofId() != null)) {
            Path p = new Path(object.refMofId());
            // Starting from segment
            ObjectReference parentReference = null;
            for(int i = 5; i <= p.size(); i+=2) {
                try {
                    // Get parent selectors from control package. Getting parent selectors from
                    // the data package would result in roundtrips because each view gets a new 
                    // data package and therefore parent selectors would be never cached 
                    ObjectReference reference = new ObjectReference(
                        (RefObject_1_0)this.application.getPmControl().getObjectById(p.getPrefix(i)),
                        this.application
                    );
                    if(parentReference != null) {
                        selectParentActions.add(
                            reference.getSelectParentAction(
                                parentReference.getTitleEscapeQuote().length() > 0
                                    ? parentReference.getTitleEscapeQuote()
                                    : parentReference.getLabel()
                            )
                        );
                    }
                    parentReference = reference;
                }
                catch(Exception e) {
                    AppLog.warning("can not get parent", e);
                }
            }
            selectParentActions.add(
                this.getObjectReference().getSelectObjectAction()
            );
        }
        return (Action[])selectParentActions.toArray(new Action[selectParentActions.size()]);      
    }
  
    //-------------------------------------------------------------------------
    public Action getBackAction(
    ) {
        Action backAction = null;
        if(
            (this.historyActions != null) && 
            (this.historyActions.size() > 0)
        ) {
            for(
                Iterator i = this.historyActions.values().iterator();
                i.hasNext();
            ) {
                backAction = (Action)i.next();
            }
        }
        return backAction;
    }
  
    //-------------------------------------------------------------------------
    public Action[] getSelectRootObjectActions(
    ) {
        return this.getApplicationContext().getRootObjectActions();
    }
    
    //-------------------------------------------------------------------------
    public Action[] getHistoryAction(
    ) {
        return (Action[])this.historyActions.values().toArray(new Action[this.historyActions.size()]);
    }
  
    //-------------------------------------------------------------------------  
    public Map<Path,Action> getHistoryActions(
    ) {
        return this.historyActions;
    }
  
    //-------------------------------------------------------------------------
    /**
     * Refresh view. refreshData ==> do refresh data. Do not refresh 
     * rendering information.
     */
    public void refresh(
        boolean refreshData
    ) throws ServiceException {
        if(refreshData) {
            this.application.resetPmData();
            this.pm = this.application.getPmData();
            RefObject_1_0 object = this.getRefObject();
            if(this.object != null) {
                this.object = this.getPersistenceManager().getObjectById(
                    object.refGetPath()
                );
                this.objectReference = new ObjectReference(
                    object,
                    this.application
                );
            }
        }
    }
  
    //-------------------------------------------------------------------------
    public ObjectView getPreviousView(
        ViewsCache showViewsCache
    ) {
      if(this.historyActions != null) {
          Map<Path,Action> historyActions = MapUtils.orderedMap(new HashMap());
          historyActions.putAll(this.historyActions);
          while(!historyActions.isEmpty()) {          
    
              // Remove last history action
              Iterator<Action> i = null;
              Action previousAction = null;
              for(
                  i = historyActions.values().iterator(); 
                  i.hasNext(); 
              ) {
                  previousAction = i.next();                  
              }
              i.remove();              
              // Try to get and refresh view. If an exception occurs go back
              // the history.
              try {
                  String previousRequestId = previousAction.getParameter(Action.PARAMETER_REQUEST_ID);
                  if((showViewsCache != null) && (showViewsCache.getView(previousRequestId) != null)) {
                      return showViewsCache.getView(previousRequestId);
                  }
                  else {
                      Path previousObjectIdentity = new Path(previousAction.getParameter(Action.PARAMETER_OBJECTXRI));
                      return  new ShowObjectView(
                          this.id,
                          this.containerElementId,
                          previousObjectIdentity,
                          this.application,
                          historyActions,
                          null,
                          null,
                          this.controlFactory
                      );
                  }
              } catch(Exception e) {}          
          }
      }
      try {
          return new ShowObjectView(
              this.id,
              this.containerElementId,
              this.application.getRootObject()[0].refGetPath(),
              this.application,
              MapUtils.orderedMap(new HashMap()),
              null,
              null,
              this.controlFactory
          );
      }
      catch(Exception e) {
          return null;
      }
    }
  
    // -------------------------------------------------------------------------
    public String getLayout(
        boolean forEditing
    ) {
        return this.application.getLayout(
           this.getObjectReference().getInspector().getForClass(),
           forEditing
        );
    }
      
    // -------------------------------------------------------------------------
    public String getLookupType() {
        return this.lookupType;
    }
    
    //-------------------------------------------------------------------------
    public Map getRestrictToElements(
    ) {
        return this.restrictToElements;
    }
    
    //-------------------------------------------------------------------------
    public abstract RefObject_1_0 getLookupObject(
    );
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    protected InspectorControl inspectorControl = null;
    protected ObjectReference objectReference;
    protected AttributePane attributePane = null;
    protected String lookupType = null;
    protected Map restrictToElements = null;
  
    protected Map<Path,Action> historyActions = null;
    protected Action[] favoriteActions = null;
    protected Action[] rootObjectActions = null;
  
}

//--- End of File -----------------------------------------------------------
