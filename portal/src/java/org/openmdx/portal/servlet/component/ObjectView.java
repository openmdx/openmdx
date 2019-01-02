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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

/**
 * ObjectView
 *
 */
public abstract class ObjectView extends View implements Serializable {
  
	/**
     * Constructor.
     * 
     * @param control
     * @param id
     * @param containerElementId
     * @param object
     * @param app
     * @param historyActions
     * @param nextPrevActions
     * @param lookupType
     * @param resourcePathPrefix
     * @param navigationTarget
     * @param isReadOnly
     */
    public ObjectView(
    	InspectorControl control,
        String id,
        String containerElementId,
        RefObject_1_0 object,
        ApplicationContext app,
        Map<Path,Action> historyActions,
        Map<Path,Action> nextPrevActions,
        String lookupType,
        String resourcePathPrefix,        
        String navigationTarget,
        Boolean isReadOnly
    ) {
        super(
        	control,
            id,
            containerElementId,            
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
        this.nextPrevActions = nextPrevActions;
        this.lookupType = lookupType;
    }

    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.View#getType()
	 */
	@Override
	public String getType(
	) {
		return null;
	}

	/**
	 * Get inspector defining this view.
	 * 
	 * @return
	 */
	public org.openmdx.ui1.jmi1.Inspector getInspector(
	) {
		InspectorControl control = (InspectorControl)this.control;
		return control.getInspector();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Canvas#refresh(boolean)
	 */
	@Override
	public void refresh(
		boolean refreshData
	) throws ServiceException {
		this.refresh(refreshData, false);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Component#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		if(type == AttributePane.class) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.attributePanes;
			return children;
		} else {
			return Collections.emptyList();
		}
	}
      
    /**
     * Get object reference for current object.
     * 
     * @return
     */
    public ObjectReference getObjectReference(
    ) {
        return this.objectReference;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.View#getObject()
     */
    @Override
    public RefObject_1_0 getObject(
    ) {
        return (RefObject_1_0)this.object;     
    }

    /**
     * Close view. Also closes the persistence manager of the current object.
     * 
     */
    public void close(
    ) {
    	if(this.object instanceof RefObject_1_0) {
    		try {
	    	    this.control = null;
//	    	    this.inspector = null;    
	    	    this.objectReference = null;
	    	    this.attributePanes = null;
	    		PersistenceManager pm = JDOHelper.getPersistenceManager(this.object);
	    		pm.close();
	    		this.object = null;
    		} catch(Exception e) {}
    	}
    }
  
    /**
     * Map struct definition to map.
     * 
     * @param from
     * @param to
     * @param structDef
     * @param mapObjects
     * @throws ServiceException
     */
    public void structToMap(
    	RefStruct from,
    	Map<String,Object> to,
    	ModelElement_1_0 structDef,
    	boolean mapObjects
    ) throws ServiceException {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(this.getObjectReference().getObject());
    	Collection<ModelElement_1_0> fields = structDef.objGetMap("field").values();
    	for(ModelElement_1_0 field: fields) {
    		String fieldName = (String)field.getQualifiedName();
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
    		} catch(Exception e) {
    			to.put(fieldName, null);
    		}
    	}
    }

    /**
     * Get select parent actions.
     * 
     * @return
     */
    public Action[] getSelectParentAction(
    ) {
        RefObject_1_0 object = this.getObject();
        List<Action> selectParentActions = new ArrayList<Action>();
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
                                parentReference.getTitleAsJavascriptArg().length() > 0
                                    ? parentReference.getTitleAsJavascriptArg()
                                    : parentReference.getLabel()
                            )
                        );
                    }
                    parentReference = reference;
                } catch(Exception e) {
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

    /**
     * Get back action from history.
     * 
     * @return
     */
    public Action getBackAction(
    ) {
        Action backAction = null;
        if(
            (this.historyActions != null) && 
            (this.historyActions.size() > 0)
        ) {
            for(
                Iterator<Action> i = this.historyActions.values().iterator();
                i.hasNext();
            ) {
                backAction = i.next();
            }
        }
        return backAction;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.View#getSelectRootObjectActions()
     */
    @Override
    public Action[] getSelectRootObjectActions(
    ) {
        return this.getApplicationContext().getRootObjectActions();
    }

    /**
     * Get history actions as array.
     * 
     * @return
     */
    public Action[] getHistoryAction(
    ) {
        return (Action[])this.historyActions.values().toArray(new Action[this.historyActions.size()]);
    }
  
    /**
     * Get history actions.
     * 
     * @return
     */
    public Map<Path,Action> getHistoryActions(
    ) {
        return this.historyActions;
    }
  
    /**
     * Return next action if defined, null otherwise. The next action is defined
     * if nextPrevActions is not null and the current object is not the last.
     * 
     * @return
     */
    public Action getNextAction(
    ) {
    	if(
    		this.nextPrevActions != null && 
    		this.getObject() instanceof RefObject_1_0
    	) {
    		List<Path> orderedKeys = new ArrayList<Path>(this.nextPrevActions.keySet()); // LinkedHashMap returns ordered keys
    		Path objectIdentity = ((RefObject_1_0)this.getObject()).refGetPath();
    		int index = orderedKeys.indexOf(objectIdentity);
    		if(index >= 0 && index < orderedKeys.size() - 1) {
    			return this.nextPrevActions.get(orderedKeys.get(index + 1));
    		} else {
    			return null;
    		}
    	} else {
    		return null;
    	}
    }

    /**
     * Get previous action if defined, null otherwise. The previous action is
     * defined if nextPrevAction is not null and the current object is not the first.
     * 
     * @param customParameters
     * @return
     */
    public Action getPrevAction(	
    ) {
    	if(
    		this.nextPrevActions != null && 
    		this.getObject() instanceof RefObject_1_0
    	) {
    		List<Path> orderedKeys = new ArrayList<Path>(this.nextPrevActions.keySet()); // LinkedHashMap returns ordered keys
    		Path objectIdentity = ((RefObject_1_0)this.getObject()).refGetPath();
    		int index = orderedKeys.indexOf(objectIdentity);
    		if(index > 0) {		
    			return this.nextPrevActions.get(orderedKeys.get(index - 1));
    		} else {
    			return null;
    		}
    	} else {
    		return null;
    	}
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.View#refresh(boolean, boolean)
     */
    @Override
    public PersistenceManager refresh(
        boolean refreshData,
        boolean closePm
    ) throws ServiceException {
        if(refreshData) {
        	if(this.getObject() != null) {
        		// Close existing pm
        		RefObject_1_0 object = this.getObject();
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

    /**
     * Get previous view according to historyActions.
     * 
     * @param showViewsCache
     * @return
     */
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
    				} else {
    					Path previousObjectIdentity = new Path(previousAction.getParameter(Action.PARAMETER_OBJECTXRI));
    					return new ShowObjectView(
							this.id,
							this.containerElementId,
							(RefObject_1_0)this.app.getNewPmData().getObjectById(previousObjectIdentity),
							this.app,
							historyActions,
							null, // do not inherit nextPrevActions
							null, // lookupType
							this.getResourcePathPrefix(),
							this.getNavigationTarget(),
							this.isReadOnly()
						);
    				}
    			} catch(Exception e) {}          
    		}
    	}
    	try {
    		return new ShowObjectView(
				this.id,
				this.containerElementId,
				(RefObject_1_0)this.app.getNewPmData().getObjectById(this.app.getRootObject()[0].refGetPath()),
				this.app,
				new LinkedHashMap<Path,Action>(),
				null, // do not inherit nextPrevActions
				null, // lookupType
				this.getResourcePathPrefix(),
				this.getNavigationTarget(),
				this.isReadOnly()
			);
    	} catch(Exception e) {
    		return null;
    	}
    }
  
    /**
     * Get layout.
     * 
     * @param forEditing
     * @return
     */
    public String getLayout(
        boolean forEditing
    ) {
        return this.app.getLayout(
           this.getObjectReference().getObject().refClass().refMofId(),
           forEditing
        );
    }
      
    /**
     * Get lookup type.
     * 
     * @return
     */
    public String getLookupType(
    ) {
        return this.lookupType;
    }
    
    /**
     * Get lookup object.
     * 
     * @return
     */
    public abstract RefObject_1_0 getLookupObject(
    );
    
    /**
	 * @return the nextPrevActions
	 */
	public Map<Path, Action> getNextPrevActions(
	) {
		return this.nextPrevActions;
	}

	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
	private static final long serialVersionUID = -6735101206939523972L;

//    private org.openmdx.ui1.jmi1.Inspector inspector;    
    protected ObjectReference objectReference;
    protected List<AttributePane> attributePanes;
    protected String lookupType;  
    protected Map<Path,Action> historyActions;
    protected Map<Path,Action> nextPrevActions;
	protected Action[] favoriteActions;
    protected Action[] rootObjectActions;
  
}

//--- End of File -----------------------------------------------------------
