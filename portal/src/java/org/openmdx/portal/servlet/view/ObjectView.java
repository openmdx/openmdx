/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: View 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
 */
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefStruct;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.control.InspectorControl;

//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public abstract class ObjectView
    extends View
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public ObjectView(
        String id,
        String containerElementId,
        Object object,
        ApplicationContext app,
        Map<Path,Action> historyActions,
        String lookupType,
        String resourcePathPrefix,        
        String navigationTarget,
        Boolean isReadOnly
    ) {
        super(
            id,
            containerElementId,
            object instanceof Path ? 
                // Get a new persistence manager. This results
                // in more DB round-trips, however different views do not
                // interfere with each other.
            	app.getNewPmData().getObjectById(object) :
            		object,
            resourcePathPrefix,
            navigationTarget,
            isReadOnly,
            app
        );
        this.objectReference = new ObjectReference(
            (RefObject_1_0)this.object,
            app
        );
        this.historyActions = historyActions;
        this.lookupType = lookupType;
    }

    // -------------------------------------------------------------------------
    public AttributePane getAttributePane(
    ) {
        return this.attributePane;
    }

    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.Inspector getInspector(
    ) {
        if(this.inspector == null) {
            if(this.object != null) {
                String forClass = this.objectReference.getObject().refClass().refMofId();
                try {
                    this.inspector = this.app.getInspector(forClass);
                }
                catch(ServiceException e) {
                	SysLog.warning(e.getMessage(), e.getCause());              
                }
                if(this.inspector == null) {
                	SysLog.warning("can not get inspector for object " + (this.object == null ? null : this.objectReference.getObject().refMofId()));      
                }
            }
        }
        return this.inspector;
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
    public void close(
    ) {
    	if(this.object instanceof RefObject_1_0) {
    		try {
	    	    this.inspectorControl = null;
	    	    this.inspector = null;    
	    	    this.objectReference = null;
	    	    this.attributePane = null;
	    		PersistenceManager pm = JDOHelper.getPersistenceManager(this.object);
	    		pm.close();
	    		this.object = null;
    		} catch(Exception e) {}
    	}
    }
  
    //-------------------------------------------------------------------------
    public void structToMap(
    	RefStruct from,
    	Map to,
    	ModelElement_1_0 structDef,
    	boolean mapObjects
    ) throws ServiceException {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(this.getObjectReference().getObject());
    	for(Iterator i = ((Map)structDef.objGetValue("field")).values().iterator(); i.hasNext(); ) {
    		ModelElement_1_0 field = (ModelElement_1_0)i.next();
    		String fieldName = (String)field.objGetValue("qualifiedName");
    		try {
    			Object value = from.refGetValue(fieldName);    			
    			if(value instanceof RefObject_1_0) {
    				if(mapObjects) {
	    				to.put(
	    					fieldName,
	    					pm.getObjectById(((RefObject_1_0)value).refGetPath())
	    				);
    				}
    			} else {
    				to.put(
    					fieldName,
    					value
    				);        		  
    			}
    		}
    		catch(Exception e) {
    			to.put(fieldName, null);
    		}
    	}
    }
    
    //-------------------------------------------------------------------------
    public Action[] getSelectParentAction(
    ) {
        RefObject_1_0 object = this.getRefObject();
        List selectParentActions = new ArrayList();
        if((object != null) && (object.refMofId() != null)) {
        	PersistenceManager pm = JDOHelper.getPersistenceManager(object);
            Path p = new Path(object.refMofId());
            // Starting from segment
            ObjectReference parentReference = null;
            for(int i = 5; i <= p.size(); i+=2) {
                try {
                    ObjectReference reference = new ObjectReference(
                        (RefObject_1_0)pm.getObjectById(p.getPrefix(i)),
                        this.app
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
                    ServiceException e0 = new ServiceException(e);
                    if(e0.getExceptionCode() != BasicException.Code.AUTHORIZATION_FAILURE) {
                    	SysLog.warning("can not get parent", e);
                    }
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
    @Override
    public PersistenceManager refresh(
        boolean refreshData,
        boolean closePm
    ) throws ServiceException {
        if(refreshData) {
        	if(this.getRefObject() != null) {
        		// Close existing pm
        		RefObject_1_0 object = this.getRefObject();
        		Path identity = object.refGetPath();
        		PersistenceManager pm = JDOHelper.getPersistenceManager(object);
        		if(closePm) {
        			pm.close();
        		}
        		// Get new pm
        		PersistenceManager newPm = this.app.getNewPmData();
        		object = (RefObject_1_0)newPm.getObjectById(identity);
        		this.object = object;
                this.objectReference = new ObjectReference(
                    object,
                    this.app
                );
                return pm;
        	}
        }
        return null;
    }

    //-------------------------------------------------------------------------
    public ObjectView getPreviousView(
        ViewsCache showViewsCache
    ) {
      if(this.historyActions != null) {
          Map<Path,Action> historyActions = new LinkedHashMap<Path,Action>();
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
                          this.app,
                          historyActions,
                          null, // lookupType
                          this.getResourcePathPrefix(),
                          this.getNavigationTarget(),
                          this.isReadOnly()
                      );
                  }
              } 
              catch(Exception e) {}          
          }
      }
      try {
          return new ShowObjectView(
              this.id,
              this.containerElementId,
              this.app.getRootObject()[0].refGetPath(),
              this.app,
              new LinkedHashMap<Path,Action>(),
              null, // lookupType
              this.getResourcePathPrefix(),
              this.getNavigationTarget(),
              this.isReadOnly()
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
        return this.app.getLayout(
           this.getObjectReference().getObject().refClass().refMofId(),
           forEditing
        );
    }
      
    // -------------------------------------------------------------------------
    public String getLookupType() {
        return this.lookupType;
    }
    
    //-------------------------------------------------------------------------
    public abstract RefObject_1_0 getLookupObject(
    );
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    protected InspectorControl inspectorControl = null;
    private org.openmdx.ui1.jmi1.Inspector inspector = null;    
    protected ObjectReference objectReference;
    protected AttributePane attributePane = null;
    protected String lookupType = null;
  
    protected Map<Path,Action> historyActions = null;
    protected Action[] favoriteActions = null;
    protected Action[] rootObjectActions = null;
  
}

//--- End of File -----------------------------------------------------------
