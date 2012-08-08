/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ObjectReference.java,v 1.26 2008/08/15 12:14:05 wfro Exp $
 * Description: ObjectReference 
 * Revision:    $Revision: 1.26 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/15 12:14:05 $
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

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.pattern.StringExpression;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
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
        BasicException exception,
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
	            this.object.refRefresh();
	        } catch(JmiServiceException e) {
	            this.object = null;
	            this.exception = e.getExceptionStack();
	        }
	    }
    }
    
  //-------------------------------------------------------------------------
  public String getTitleEscapeQuote(
  ) {
      return StringExpression.compile("'").matcher(this.getTitle()).replaceAll("\\'");
  }
  
  //-------------------------------------------------------------------------
  public String getTitle(
  ) {
      if(this.exception != null) {
          if(this.exception.getExceptionCode() == BasicException.Code.NOT_FOUND) {
              return TITLE_PREFIX_NOT_ACCESSIBLE + " (" + this.exception.getParameter("path") + ")";
          }
          else if(this.exception.getExceptionCode() == BasicException.Code.AUTHORIZATION_FAILURE) {
              return TITLE_PREFIX_NO_PERMISSION + " (" + this.exception.getParameter("path") + ")";              
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
	          this.exception = BasicException.toStackedException(e);
	          AppLog.detail(e.getMessage(), e.getCause());
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
    if(this.object == null) {
        return "-";
    }
    return this.application.getLabel(
        this.getInspector().getForClass()    
    );
  }
  
  //-------------------------------------------------------------------------
  public String getIconKey(
  ) {
    if(this.object == null) {
        return WebKeys.ICON_MISSING;
    }
    try {
        return this.application.getIconKey(
            this.getInspector().getForClass()    
        );
    }
    catch(ServiceException e) {
        AppLog.warning(e.getMessage(), e.getCause());
        return WebKeys.ICON_MISSING;
    }
  }
    
  //-------------------------------------------------------------------------
  /**
   * Returns the background color of the field/value as W3C CSS color, 
   * null if not defined.
   */
  public String getBackColor(      
  ) {
      if(this.object == null) {
          return null;
      }
      try {
          return this.application.getBackColor(
              this.getInspector().getForClass()
          );              
      }
      catch(ServiceException e) {
          AppLog.warning(e.getMessage(), e.getCause());
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
      if(this.object == null) {
          return null;
      }
      try {
          return this.application.getColor(
              this.getInspector().getForClass()
          );              
      }
      catch(ServiceException e) {
          AppLog.warning(e.getMessage(), e.getCause());
          return null;
      }
  }
    
  //-------------------------------------------------------------------------
  public org.openmdx.ui1.jmi1.Inspector getInspector(
  ) {
      if(this.inspector == null) {
          if(this.object != null) {
    	      String forClass = object.refClass().refMofId();
              try {
                  this.inspector = this.application.getInspector(forClass);
              }
              catch(ServiceException e) {
                  AppLog.warning(e.getMessage(), e.getCause());              
              }
    	      if(this.inspector == null) {
    	          AppLog.warning("can not get inspector for object " + (this.object == null ? null : this.object.refMofId()));      
    	      }
          }
      }
      return this.inspector;
  }
  
  //-------------------------------------------------------------------------
  public Action getSelectObjectAction(
  ) {  
      String title = this.getTitle();
      Path retrievalPath = this.object == null
          ? null
          : this.application.getObjectRetrievalIdentity(this.object);
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
          title.trim().length() > 0
              ? title
              : this.getLabel(),
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
          this.getTitle(),
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
    ) {
        return this.getEditObjectAction(
            ViewMode.STANDARD
        );
    }
    
    //-------------------------------------------------------------------------
    public Action getEditObjectAction(
        ViewMode mode
    ) {
        return new Action(
            Action.EVENT_EDIT,  
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object == null ? "" : this.object.refMofId()),
                new Action.Parameter(Action.PARAMETER_MODE, mode.toString())
            },
            this.application.getTexts().getEditTitle(),
            this.application.getTexts().getEditTitle(),
            WebKeys.ICON_EDIT,
            this.getInspector().isChangeable() &&
            this.application.getPortalExtension().isEnabled(
                Ui_1.EDIT_OBJECT_OPERATION_NAME,
                this.getObject(),
                this.application
            )          
        );
    }
  
    //-------------------------------------------------------------------------
    public Action getDeleteObjectAction(
    ) {
        return new Action(
            Action.EVENT_DELETE,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.object == null ? "" : this.object.refMofId())
            },
            this.application.getTexts().getDeleteTitle(),
            this.application.getTexts().getDeleteTitle(),
            WebKeys.ICON_DELETE,
            this.getInspector().isChangeable() &&
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
  private BasicException exception;
  private final ApplicationContext application;
  private org.openmdx.ui1.jmi1.Inspector inspector = null;
}

//--- End of File -----------------------------------------------------------
