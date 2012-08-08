/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ObjectReference.java,v 1.38 2009/10/13 13:14:41 wfro Exp $
 * Description: ObjectReference 
 * Revision:    $Revision: 1.38 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/10/13 13:14:41 $
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
import java.util.regex.Pattern;

import javax.jdo.JDOHelper;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.view.ViewMode;
import org.openmdx.ui1.layer.application.Ui_1;

public class ObjectReference
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public ObjectReference(
        RefObject_1_0 object,
        ApplicationContext application
    ) {
	    this.object = object;
	    this.exception = null;
	    this.application = application;
    }

    //-------------------------------------------------------------------------
    public ObjectReference(
        ServiceException exception,
        ApplicationContext application
    ) {
	    this.object = null;
	    this.exception = exception;
	    this.application = application;
    }

    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
    public String getTitleEscapeQuote(
    ) {
        return Pattern.compile("'").matcher(this.getTitle()).replaceAll("\\'");
    }
  
    //-------------------------------------------------------------------------
    public String getTitle(
    ) {
    	return this.getTitle(false);
    }
	 
    //-------------------------------------------------------------------------
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
    	}
    	else if(this.object == null) {
    		return "";
    	}
    	else {
    		String title = "";
    		try {
    			RefObject_1_0 refObj = this.object;
    			title = this.application.getPortalExtension().getTitle(
    				refObj,
    				this.application.getCurrentLocaleAsIndex(),
    				this.application.getCurrentLocaleAsString(),
    				asShortTitle,
    				this.application
    			);
    			// Replace newlines by blank
    			title = title.replace('\n', ' ');
    			// Replace " by &quot;
    			int pos = 0;
    			while((pos = title.indexOf('"')) >= 0) {
    				title = title.substring(0, pos) + "&quot;" + title.substring(pos + 1); 
    			}
    		}
    		catch(Exception e) {
    			this.exception = new ServiceException(e);
    			SysLog.detail(e.getMessage(), e.getCause());
    			return this.getTitle();
    		}
    		return title;
    	}
    }

    //-------------------------------------------------------------------------
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
                this.application.getLabel(this.object.refClass().refMofId());
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
                this.application.getIconKey(this.object.refClass().refMofId());
        }
        catch(ServiceException e) {
        	SysLog.warning(e.getMessage(), e.getCause());
            return null;
        }
    }
    
    //-------------------------------------------------------------------------
    /**
     * Returns the background color of the field/value as W3C CSS color, 
     * null if not defined.
     */
    public String getBackColor(      
    ) {
        try {
            return this.object == null ?
                null :
                this.application.getBackColor(this.object.refClass().refMofId());
        }
        catch(ServiceException e) {
        	SysLog.warning(e.getMessage(), e.getCause());
            return null;
        }
    }
  
    //-------------------------------------------------------------------------
    /**
     * Returns the color of the field/value as W3C CSS color, null if not
     * defined.
     */
    public String getColor(      
    ) {
        try {
            return this.object == null ?
                null :
                this.application.getColor(this.object.refClass().refMofId());
        }
        catch(ServiceException e) {
        	SysLog.warning(e.getMessage(), e.getCause());
            return null;
        }
    }
    
    //-------------------------------------------------------------------------
    public Action getSelectObjectAction(
    ) {  
        String title = this.getTitle();
        Path retrievalPath = (this.object == null) || (this.exception != null) ? 
            null : 
            this.application.getObjectRetrievalIdentity(this.object);
        if(retrievalPath == null) {
            return new Action(
                Action.EVENT_NONE,
                null,
                title,
                this.getIconKey(),
                true
            );
        }
        return new Action(
            Action.EVENT_SELECT_OBJECT,
            new Action.Parameter[]{
                new Action.Parameter(
                    Action.PARAMETER_OBJECTXRI, 
                    retrievalPath.toXri()
                ),
            },
            title.trim().length() > 0 ? 
                title : 
                this.getLabel(),
            this.getIconKey(),
            true
        );
    }

    //-------------------------------------------------------------------------
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
            Action.EVENT_SELECT_AND_EDIT_OBJECT,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object.refMofId()),
            },
            this.getTitle(),
            this.getIconKey(),
            true
        );
    }
  
    //-------------------------------------------------------------------------
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
          Action.EVENT_RELOAD,
          new Action.Parameter[]{
              new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object.refMofId())
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
            Action.EVENT_OBJECT_GET_ATTRIBUTES, 
            new Action.Parameter[]{ 
               new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object.refMofId())
            }, 
            this.application.getTexts().getShowDetailsTitle(), 
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
            Action.EVENT_EDIT,  
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object == null ? "" : this.object.refMofId()),
                new Action.Parameter(Action.PARAMETER_MODE, mode.toString())
            },
            this.application.getTexts().getEditTitle(),
            this.application.getTexts().getEditTitle(),
            WebKeys.ICON_EDIT,
            this.application.getInspector(this.object.refClass().refMofId()).isChangeable() &&
            this.application.getPortalExtension().isEnabled(
                Ui_1.EDIT_OBJECT_OPERATION_NAME,
                this.getObject(),
                this.application
            )          
        );
    }
  
    //-------------------------------------------------------------------------
    public Action getDeleteObjectAction(
    ) throws ServiceException {
        return new Action(
            Action.EVENT_DELETE,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object == null ? "" : this.object.refMofId())
            },
            this.application.getTexts().getDeleteTitle(),
            this.application.getTexts().getDeleteTitle(),
            WebKeys.ICON_DELETE,
            this.application.getInspector(this.object.refClass().refMofId()).isChangeable() &&
            this.application.getPortalExtension().isEnabled(
                Ui_1.DELETE_OBJECT_OPERATION_NAME,
                this.getObject(),
                this.application
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
                this.application.getTexts().getNavigateToParentText(),
                WebKeys.ICON_UP,
                false
            );
        }
        else {
            Path identity = this.object.refGetPath();
            return new Action(
                Action.EVENT_SELECT_OBJECT,
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, identity.getParent().getParent().toXri()),
                    new Action.Parameter(Action.PARAMETER_REFERENCE_NAME, identity.getParent().getBase())
                },
                parentTitle != null
                    ? parentTitle
                    : this.application.getTexts().getNavigateToParentText(),
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

    //-------------------------------------------------------------------------
    public String toString(
    ) {
        return this.getTitle();
    }

    //-------------------------------------------------------------------------
    public String refMofId(
    ) {
        if(this.object != null) {
            return this.object.refMofId();
        }
        else {
            return "";
        }
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258133570026484790L;

    public static final String TITLE_PREFIX_NO_PERMISSION = "N/P";
    public static final String TITLE_PREFIX_NOT_ACCESSIBLE = "N/A";

    private RefObject_1_0 object;
    private ServiceException exception;
    private final ApplicationContext application;
}

//--- End of File -----------------------------------------------------------
