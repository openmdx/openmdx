/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ObjectReference 
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
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jdo.JDOHelper;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.action.DeleteAction;
import org.openmdx.portal.servlet.action.EditAction;
import org.openmdx.portal.servlet.action.ObjectGetAttributesAction;
import org.openmdx.portal.servlet.action.ReloadAction;
import org.openmdx.portal.servlet.action.SelectAndEditObjectAction;
import org.openmdx.portal.servlet.action.SelectObjectAction;
import org.openmdx.portal.servlet.component.ViewMode;
import org.openmdx.ui1.layer.application.Ui_1;

/**
 * ObjectReference
 *
 */
public class ObjectReference
    implements Serializable {
  
    /**
     * Constructor 
     *
     * @param object
     * @param app
     */
    public ObjectReference(
        RefObject_1_0 object,
        ApplicationContext app
    ) {
	    this.object = object;
	    this.exception = null;
	    this.app = app;
    }

    /**
     * Constructor 
     *
     * @param exception
     * @param application
     */
    public ObjectReference(
        ServiceException exception,
        ApplicationContext application
    ) {
	    this.object = null;
	    this.exception = exception;
	    this.app = application;
    }

    /**
     * Reload underlying object.
     */
    public void refresh(
    ) {
        if(this.object != null) {
	        try {
	        	JDOHelper.getPersistenceManager(this.object).refresh(this.object);
	        } 
	        catch(Exception e) {
	            this.object = null;
	            this.exception = new ServiceException(e);
	        }
	    }
    }
    
    /**
     * Get title and escape special characters such as ' and CR so that it
     * can be used as parameter for Javascript functions.
     * 
     * @return
     */
    public String getTitleAsJavascriptArg(
    ) {
    	String title = this.getTitle();
    	title = title.replace("'", "\\'");
    	title = title.replace("\n", " ");
    	return title;
    }

    /**
     * Get non-short title of underlying object.
     * 
     * @return
     */
    public String getTitle(
    ) {
    	return this.getTitle(false);
    }
	 
    /**
     * Get title of underlying object.
     * 
     * @param asShortTitle
     * @return
     */
    public String getTitle(
    	boolean asShortTitle
    ) {
    	if(this.exception != null) {
    		if(this.exception.getExceptionCode() == BasicException.Code.NOT_FOUND) {
    			return TITLE_PREFIX_NOT_ACCESSIBLE + " (" + this.exception.getCause().getParameter("object.mof.id") + ")";
    		}
    		else if(this.exception.getExceptionCode() == BasicException.Code.AUTHORIZATION_FAILURE) {
    			return TITLE_PREFIX_NO_PERMISSION + " (" + this.exception.getCause().getParameter("object.mof.id") + ")";              
    		}
    		else {
    			return this.exception.getMessage();
    		}
    	} else if(this.object == null) {
    		return "";
    	} else {
    		try {
    			return this.app.getPortalExtension().getTitle(
    				this.object,
    				this.app.getCurrentLocaleAsIndex(),
    				this.app.getCurrentLocaleAsString(),
    				asShortTitle,
    				this.app
    			);
    		} catch(Exception e) {
    			this.exception = new ServiceException(e);
    			SysLog.detail(e.getMessage(), e.getCause());
    			return this.getTitle();
    		}
    	}
    }

    /**
     * Get underlying object.
     * 
     * @return
     */
    public RefObject_1_0 getObject(
    ) {
        return this.object;
    }
    
    //-------------------------------------------------------------------------
    public String getLabel(
    ) {
        try {
            return this.object == null ?
                "-" :
                this.app.getLabel(this.object.refClass().refMofId());
        }
        catch(ServiceException e) {
        	SysLog.warning(e.getMessage(), e.getCause());
            return null;
        }
    }
  
    //-------------------------------------------------------------------------
    public String getIconKey(
    ) {
        try {
            return this.object == null ? 
                WebKeys.ICON_MISSING : 
                this.app.getIconKey(this.object.refClass().refMofId());
        }
        catch(ServiceException e) {
        	SysLog.warning(e.getMessage(), e.getCause());
            return null;
        }
    }
    
    /**
     * Returns the background color of the field/value as W3C CSS color, 
     * null if not defined.
     */
    public String getBackColor(      
    ) {
        try {
            return this.object == null ?
                null :
                this.app.getBackColor(this.object.refClass().refMofId());
        }
        catch(ServiceException e) {
        	SysLog.warning(e.getMessage(), e.getCause());
            return null;
        }
    }
  
    /**
     * Returns the color of the field/value as W3C CSS color, null if not
     * defined.
     */
    public String getColor(      
    ) {
        try {
            return this.object == null ?
                null :
                this.app.getColor(this.object.refClass().refMofId());
        }
        catch(ServiceException e) {
        	SysLog.warning(e.getMessage(), e.getCause());
            return null;
        }
    }
    
    /**
     * Get select action. Custom request parameters are added to standard parameters.
     * 
     * @param customParameters
     * @return
     */
    public Action getSelectObjectAction(
    	Action.Parameter... customParameters
    ) {
        String title = this.getTitle();
        Path retrievalPath = (this.object == null) || (this.exception != null) ? 
            null : 
            this.app.getObjectRetrievalIdentity(this.object);
        if(retrievalPath == null) {
            return new Action(
                Action.EVENT_NONE,
                null,
                title,
                this.getIconKey(),
                true
            );
        }
        List<Action.Parameter> parameters = new ArrayList<Action.Parameter>();
        parameters.add(
    		new Action.Parameter(
                Action.PARAMETER_OBJECTXRI, 
                retrievalPath.toXRI()
            )        		
        );
        if(parameters != null) {
        	parameters.addAll(Arrays.asList(customParameters));
        }
        return new Action(
            SelectObjectAction.EVENT_ID,
            parameters.toArray(new Action.Parameter[parameters.size()]),
            !title.trim().isEmpty() ? title : this.getLabel(),
            this.getIconKey(),
            true
        );
    }

    /**
     * Get select and edit action.
     * 
     * @return
     */
    public Action getSelectAndEditObjectAction(
    ) {  
        if(this.object == null) {
            return new Action(
                Action.EVENT_NONE,
                null,
                this.getTitle(),
                this.getIconKey(),
                true
            );
        }
        return new Action(
            SelectAndEditObjectAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object.refGetPath().toXRI()),
            },
            this.getTitle(),
            this.getIconKey(),
            true
        );
    }
  
    /**
     * Get reload action.
     * 
     * @return
     */
    public Action getReloadAction(
    ) {
        if(this.object == null) {
            return new Action(
                Action.EVENT_NONE,
                null,
                this.getTitle(),
                this.getIconKey(),
                true
            );
        }
      return new Action(
          ReloadAction.EVENT_ID,
          new Action.Parameter[]{
              new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object.refGetPath().toXRI())
          },
          this.getTitle(true),
          this.getIconKey(),
          true
      );
    }

    //-------------------------------------------------------------------------
    public Action getObjectGetAttributesAction(
    ) {
        return new Action(
            ObjectGetAttributesAction.EVENT_ID, 
            new Action.Parameter[]{ 
               new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object.refGetPath().toXRI())
            }, 
            this.app.getTexts().getShowDetailsTitle(), 
            true
        );
    }
          
    //-------------------------------------------------------------------------
    public Action getEditObjectAction(
    ) throws ServiceException {
        return this.getEditObjectAction(
            ViewMode.STANDARD
        );
    }
    
    //-------------------------------------------------------------------------
    public Action getEditObjectAction(
        ViewMode mode
    ) throws ServiceException {
        return new Action(
            EditAction.EVENT_ID,  
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object == null ? "" : this.object.refGetPath().toXRI()),
                new Action.Parameter(Action.PARAMETER_MODE, mode.toString())
            },
            this.app.getTexts().getEditTitle(),
            this.app.getTexts().getEditTitle(),
            WebKeys.ICON_EDIT,
            this.app.getInspector(this.object.refClass().refMofId()).isChangeable() &&
            !this.app.getPortalExtension().hasPermission(
                Ui_1.EDIT_OBJECT_OPERATION_NAME,
                this.getObject(),
                this.app,
                WebKeys.PERMISSION_REVOKE_SHOW
            ) && 
            !this.app.getPortalExtension().hasPermission(
                Ui_1.EDIT_OBJECT_OPERATION_NAME,
                this.getObject(),
                this.app,
                WebKeys.PERMISSION_REVOKE_EDIT
            )
        );
    }
  
    //-------------------------------------------------------------------------
    public Action getDeleteObjectAction(
    ) throws ServiceException {
        return new Action(
            DeleteAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object == null ? "" : this.object.refGetPath().toXRI())
            },
            this.app.getTexts().getDeleteTitle(),
            this.app.getTexts().getDeleteTitle(),
            WebKeys.ICON_DELETE,
            this.app.getInspector(this.object.refClass().refMofId()).isChangeable() &&
            !this.app.getPortalExtension().hasPermission(
                Ui_1.DELETE_OBJECT_OPERATION_NAME,
                this.getObject(),
                this.app,
                WebKeys.PERMISSION_REVOKE_SHOW
            )
        );
    }
  
    //-------------------------------------------------------------------------
    public Action getSelectParentAction(
    ) {
        return this.getSelectParentAction(null);
    }
  
    //-------------------------------------------------------------------------
    public Action getSelectParentAction(
        String parentTitle
    ) {
        if(
            (this.object == null) ||
            (this.object.refGetPath() == null) ||
            (this.object.refGetPath().size() < 7)
        ) {
            return new Action(
                Action.EVENT_NONE,
                null,
                this.app.getTexts().getNavigateToParentText(),
                WebKeys.ICON_UP,
                false
            );
        }
        else {
            Path identity = this.object.refGetPath();
            return new Action(
                SelectObjectAction.EVENT_ID,
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, identity.getParent().getParent().toXRI()),
                    new Action.Parameter(Action.PARAMETER_REFERENCE_NAME, identity.getParent().getBase())
                },
                parentTitle != null
                    ? parentTitle
                    : this.app.getTexts().getNavigateToParentText(),
                WebKeys.ICON_UP,
                true
            );
        }
    }

    //-------------------------------------------------------------------------
    public boolean isInstanceof(
        String typeName
    ) {
        if(this.object == null) {
            return false;
        }
        try {
            Model_1_0 model = ((RefPackage_1_0)this.object.refOutermostPackage()).refModel();
            boolean isSubtypeOf = model.isSubtypeOf(this.object.refClass().refMofId(), typeName);
            return isSubtypeOf;
        }
        catch(ServiceException e) {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ) {
        return this.getTitle();
    }

    /**
     * Return object's path.
     * @return
     */
    public Path getPath(
    ) {
        if(this.object != null) {
            return this.object.refGetPath();
        }
        else {
            return null;
        }
    }

    /**
     * Return objects XRI.
     * @return
     */
    public String getXRI(
    ) {
    	Path path = this.getPath();
    	return path == null ? "" : path.toXRI();
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258133570026484790L;

    public static final String TITLE_PREFIX_NO_PERMISSION = "N/P";
    public static final String TITLE_PREFIX_NOT_ACCESSIBLE = "N/A";

    private RefObject_1_0 object;
    private ServiceException exception;
    private final ApplicationContext app;
}

//--- End of File -----------------------------------------------------------
