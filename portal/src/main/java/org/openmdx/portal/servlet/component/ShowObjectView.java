/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ShowObjectView 
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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.SelectLocaleAction;
import org.openmdx.portal.servlet.action.SelectObjectAction;
import org.openmdx.portal.servlet.action.SelectPerspectiveAction;
import org.openmdx.portal.servlet.control.AttributePaneControl;
import org.openmdx.portal.servlet.control.OperationPaneControl;
import org.openmdx.portal.servlet.control.ReferencePaneControl;
import org.openmdx.portal.servlet.control.ShowInspectorControl;

/**
 * ShowObjectView
 *
 */
public class ShowObjectView extends ObjectView implements Serializable {

    /**
     * Constructor.
     * 
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
     * @throws ServiceException
     */
    public ShowObjectView(
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
    ) throws ServiceException {
        super(
    		app.getPortalExtension().getControlFactory().createShowInspectorControl(
	            id,
	            app.getCurrentPerspective(),
	            app.getCurrentLocaleAsString(),
	            app.getCurrentLocaleAsIndex(),
	            app.getInspector(object.refClass().refMofId()),
	            object.refClass().refMofId(),
	            app.getWizardDefinitionFactory()
	        ),
            id, 
            containerElementId,
            object,
            app, 
            historyActions,
            nextPrevActions,
            lookupType,
            resourcePathPrefix,
            navigationTarget,
            isReadOnly
        );
        // Attribute pane
        SysLog.detail("Preparing attribute pane");
		List<AttributePane> attributePanes = new ArrayList<AttributePane>();
		for(AttributePaneControl attributePaneControl: this.control.getChildren(AttributePaneControl.class)) {
			attributePanes.add(
				attributePaneControl.newComponent(
		            this,
		            object
		        )
			);
		}
		this.attributePanes = attributePanes;
        // Operation pane
        SysLog.detail("Preparing operation panes");
        this.operationPanes = new ArrayList<OperationPane>();
        for(OperationPaneControl operationPaneControl: this.control.getChildren(OperationPaneControl.class)) {
        	this.operationPanes.add(
        		operationPaneControl.newComponent(
	                this
	            )
            );
        }
        // Reference pane
        SysLog.detail("Preparing reference panes");
        this.referencePanes = new ArrayList<ReferencePane>();
        for(ReferencePaneControl referencePaneControl: this.control.getChildren(ReferencePaneControl.class)) {
            this.referencePanes.add(
	            referencePaneControl.newComponent(
	                this,
	                lookupType
	            )
            );
        }
    }

    /**
     * Get control casted to ShowInspectorControl.
     * 
     * @return
     */
    protected ShowInspectorControl getShowInspectorControl(
    ) {
    	return (ShowInspectorControl)this.control;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.ObjectView#refresh(boolean, boolean)
     */
    @Override
    public PersistenceManager refresh(
        boolean refreshData,
        boolean closePm
    ) throws ServiceException {
        PersistenceManager oldPm = super.refresh(
        	refreshData,
        	closePm
        );
        try {
        	for(AttributePane attributePane: this.getChildren(AttributePane.class)) {
        		attributePane.refresh(refreshData);
        	}
            for(ReferencePane referencePane: this.referencePanes) {
                referencePane.refresh(refreshData);
            }
        } catch(ServiceException e) {
        	SysLog.detail("can not refresh", e.getMessage());
        	Throwables.log(e);
        }
        return oldPm;
    }

    /**
     * Handle can not invoke operation exception.
     * 
     * @param e
     * @param operationName
     */
    public void handleCanNotInvokeOperationException(
        ServiceException e, 
        String operationName
    ) {
        BasicException e0 = e.getCause(this.app.getExceptionDomain());
        if (e0 == null) {
        	Throwable cause = e;
        	while(cause.getCause() != null) {
        		cause = cause.getCause();
        	}
            this.app.addErrorMessage(
                app.getTexts().getErrorTextCanNotInvokeOperation(), 
                new String[] { 
                    this.getObject().refMofId(),
                    operationName, 
                    cause.toString() 
                }
           );
        } else if(this.app.getTexts().getUserDefinedText(Integer.toString(e0.getExceptionCode())) != null) {
            List<String> parameters = new ArrayList<String>();
            int i = 0;
            while (e0.getParameter("param" + i) != null) {
                parameters.add(e0.getParameter("param" + i));
                i++;
            }
            this.app.addErrorMessage(
                this.app.getTexts().getUserDefinedText(
                    e0.getExceptionCode() + ""), 
                    (String[]) parameters.toArray(new String[parameters.size()]
                )
            );
        } else {
            this.app.addErrorMessage(
                this.app.getTexts().getErrorTextCanNotInvokeOperation(), 
                new String[] { 
                    this.getObject().refMofId(),
                    operationName, 
                    e.getMessage() 
                }
            );
        }
    }

    /**
     * Update back navigation history.
     * 
     * @return
     */
    public Map<Path,Action> createHistoryAppendCurrent(
    ) {
        Map<Path,Action> historyActions = new LinkedHashMap<Path,Action>();
        historyActions.putAll(this.historyActions);
        if(this.objectReference == null || this.objectReference.getObject() == null) {
        	return historyActions;
        }
        // Remove first (oldest) history entry in case MAX_HISTORY_ENTRIES is
        // reached
        if(historyActions.size() >= MAX_HISTORY_ENTRIES) {
            Iterator<Map.Entry<Path,Action>> i = historyActions.entrySet().iterator();
            i.next();
            i.remove();
        }
        // Remove current and add it again. This moves current to the end
        historyActions.remove(
            this.objectReference.getObject().refGetPath()
        );
        historyActions.put(
            this.objectReference.getObject().refGetPath(), 
            new Action(
                SelectObjectAction.EVENT_ID,
                new Action.Parameter[] { 
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.getObject().refGetPath().toXRI()),
                    new Action.Parameter(Action.PARAMETER_REQUEST_ID, this.requestId)                    
                },
                this.objectReference.getTitle() + (this.objectReference.getTitle().length() > 0 ? " - " : "") + this.objectReference.getLabel(),
                this.objectReference.getIconKey(),
                true
            )
        );
        return historyActions;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.View#handleCanNotCommitException(org.openmdx.kernel.exception.BasicException)
     */
    @Override
    public void handleCanNotCommitException(
        BasicException e
    ) {
      BasicException e0 = e.getCause(this.app.getExceptionDomain());
      if(e0 == null) {
          this.app.addErrorMessage(
            app.getTexts().getErrorTextCanNotCreateOrEditObject(),
            new String[]{e.getMessage()}
          );
      } else if(this.app.getTexts().getUserDefinedText(e0.getExceptionCode() + "") != null) {
          List<String> parameters = new ArrayList<String>();
          int i = 0;
          while(e0.getParameter("param" + i) != null) {
              parameters.add(e0.getParameter("param" + i));
              i++;
          }
          this.app.addErrorMessage(
              app.getTexts().getUserDefinedText(e0.getExceptionCode() + ""),
              parameters.toArray(new String[parameters.size()])
          );             
      } else {
          this.app.addErrorMessage(
              app.getTexts().getErrorTextCanNotCreateOrEditObject(),
              new String[]{e.getMessage()}
          );              
      }
    }
  
    /**
     * Get select locale actions.
     * 
     * @return
     */
    public Action[] getSelectLocaleAction(
    ) {
        Texts_1_0[] texts = this.app.getTextsFactory().getTextsBundles();
        Map<String,Action> actions = new TreeMap<String,Action>();
        for (int i = 0; i < texts.length; i++) {
            String locale = texts[i].getLocale();
            if ((locale != null) && !actions.keySet().contains(locale)) {
                actions.put(
                    locale, 
                    new Action(
                        SelectLocaleAction.EVENT_ID, 
                        new Action.Parameter[]{ 
                           new Action.Parameter(Action.PARAMETER_LOCALE, locale) 
                        }, 
                        texts[i].getLocaleTitle(), 
                        true
                    )
                );
            }
        }
        return actions.values().toArray(new Action[actions.size()]);
    }

    /**
     * Get select perspective actions.
     * 
     * @return
     */
    public Action[] getSelectPerspectiveAction(        
    ) {
        Path[] uiSegmentPaths = this.getApplicationContext().getUiContext().getUiSegmentPaths();
        List<Action> selectPerspectiveActions = new ArrayList<Action>();
        int ii = 0;
        for(Path uiSegmentPath: uiSegmentPaths) {
            org.openmdx.ui1.jmi1.Segment uiSegment = null;
            try {
                uiSegment = this.getApplicationContext().getUiContext().getUiSegment(ii);
            } catch(Exception ignore) {
    			SysLog.trace("Exception ignored", ignore);
            }
            boolean isRevokeShow = this.app.getPortalExtension().hasPermission(
            	uiSegment, 
            	this.app,
            	WebKeys.PERMISSION_REVOKE_SHOW 
            );
            selectPerspectiveActions.add(
                new Action(
                    SelectPerspectiveAction.EVENT_ID,
                    new Action.Parameter[]{
                        new Action.Parameter(Action.PARAMETER_ID, String.valueOf(ii))
                    },
                    uiSegmentPath.getLastSegment().toClassicRepresentation(),
                    !isRevokeShow
                )
            );
            ii++;
        }
        return selectPerspectiveActions.toArray(new Action[selectPerspectiveActions.size()]);
    }
      
    /**
     * Select reference pane.
     * 
     * @param paneIndex
     */
    public void selectReferencePane(
        int paneIndex
    ) {
        if (paneIndex < this.referencePanes.size()) {
            this.selectedReferencePaneIndex = paneIndex;
        }
    }

    /**
     * Get index of currently selected reference pane.
     * 
     * @return
     */
    public int getCurrentReferencePane(
    ) {
        return this.selectedReferencePaneIndex;
    }

    /**
     * Select reference pane by name.
     * 
     * @param referenceName
     * @return
     */
    public Grid selectReferencePane(
        String referenceName
    ) {
        if(referenceName != null) {
            for(int i = 0; i < this.referencePanes.size(); i++) {
                List<Action> selectReferenceActions = this.referencePanes.get(i).getSelectReferenceActions();
                for(int j = 0; j < selectReferenceActions.size(); j++) {
                    if(referenceName.equals(selectReferenceActions.get(j).getParameter(Action.PARAMETER_REFERENCE_NAME))) {
                        this.referencePanes.get(i).selectReference(j);
                        return this.referencePanes.get(i).getGrid();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Set filter values for filter.
     * 
     * @param filterName
     * @param filterValues
     */
    public void selectFilter(
        String filterName, 
        String filterValues
    ) {
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.ObjectView#getType()
     */
    @Override
    public String getType(
    ) {
        return ObjectView.VIEW_SHOW_OBJECT;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.ObjectView#getLookupObject()
     */
    @Override
    public RefObject_1_0 getLookupObject(
    ) {
        return this.getObjectReference().getObject();
    }

    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.ObjectView#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		if(type == OperationPane.class) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.operationPanes;
			return children;
		} else if(type == ReferencePane.class) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.referencePanes;
			return children;			
		} else {
			return super.getChildren(type);
		}
	}

    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.component.Component#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
	 */
	@Override
	public void paint(
		ViewPort p, 
		String frame, 
		boolean forEditing
	) throws ServiceException {
		this.getShowInspectorControl().paint(p, frame, forEditing);
	}

	// -------------------------------------------------------------------------
    // Members
    // -------------------------------------------------------------------------
    private static final long serialVersionUID = 3257844376976635442L;
    
    private static final int MAX_HISTORY_ENTRIES = 10;

    protected final List<OperationPane> operationPanes;
    protected final List<ReferencePane> referencePanes;
    protected int selectedReferencePaneIndex = 0;
    
}

// --- End of File -----------------------------------------------------------
