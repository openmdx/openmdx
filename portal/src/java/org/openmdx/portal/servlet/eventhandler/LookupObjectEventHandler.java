/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: LookupObjectEventHandler.java,v 1.26 2010/11/28 13:23:09 wfro Exp $
 * Description: LookupObjectEventHandler 
 * Revision:    $Revision: 1.26 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/11/28 13:23:09 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.eventhandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.ui1.jmi1.FeatureDefinition;
import org.openmdx.ui1.jmi1.StructuralFeatureDefinition;

public class LookupObjectEventHandler {

    //-----------------------------------------------------------------------
    public static HandleEventResult handleEvent(
        int event,
        ObjectView view,
        HttpServletRequest request,
        HttpServletResponse response,
        ApplicationContext application,
        String parameter,
        Map parameterMap
    ) throws IOException, ServletException {

        ObjectView nextView = null;
        if(view instanceof ShowObjectView) {
            ShowObjectView currentView = (ShowObjectView)view;
            
            switch(event) {
            
                case Action.EVENT_FIND_OBJECT:
                    String qualifiedElementName = null;
                    try {
                        qualifiedElementName = Action.getParameter(parameter, Action.PARAMETER_REFERENCE);
                        String id = Action.getParameter(parameter, Action.PARAMETER_ID);
                        SysLog.detail("Find object", Arrays.asList(Action.PARAMETER_REFERENCE, qualifiedElementName, Action.PARAMETER_ID, id));
                        Model_1_0 model = application.getModel();
                        ModelElement_1_0 lookupType = null;
                        // Get lookup type from model
                        try {
                            ModelElement_1_0 elementDef = model.getElement(qualifiedElementName);
                            lookupType = model.isReferenceType(elementDef) ?
                            	 model.getElement(elementDef.objGetValue("type")) :
                            		 elementDef;
                        }
                        catch(Exception e) {
                            try {
                                // Fallback to customized feature definitions
                                FeatureDefinition lookupFeature = application.getFeatureDefinition(qualifiedElementName);
                                if(lookupFeature instanceof StructuralFeatureDefinition) {
                                    lookupType = model.getElement(((StructuralFeatureDefinition)lookupFeature).getType());
                                }
                            }
                            catch(Exception e0) {}
                        }
                        Object[] parameterValues = (Object[]) parameterMap.get(WebKeys.REQUEST_PARAMETER_FILTER_VALUES);
                        String filterValues = parameterValues == null ? null : (parameterValues.length > 0 ? (String) parameterValues[0] : null);
                        nextView = application.getPortalExtension().getLookupView(
                            id, 
                            lookupType, 
                            currentView.getRefObject(),
                            filterValues,
                            application
                        );
                    }
                    catch (Exception e) {
                        new ServiceException(e).log();
                        application.addErrorMessage(
                            application.getTexts().getErrorTextCanNotLookupObject(), 
                            new String[] { 
                                qualifiedElementName,
                                e.getMessage() 
                            }
                        );
                    }
                    break;
                            
            }
        }
        
        else if(view instanceof EditObjectView) {
            EditObjectView currentView = (EditObjectView)view;           
            switch(event) {
            	
              case Action.EVENT_FIND_OBJECT:
                ModelElement_1_0 lookupType = null;
                try {
                  String referenceName = Action.getParameter(parameter, Action.PARAMETER_REFERENCE);
                  Model_1_0 model = application.getModel();
                  // Try to get lookup type from model
                  try {
                      ModelElement_1_0 reference = model.getElement(referenceName);
                      lookupType = model.getElement(reference.objGetValue("type"));
                  }
                  catch(Exception e) {
                      try {
                          FeatureDefinition featureDef = application.getFeatureDefinition(referenceName);
                          if(featureDef instanceof StructuralFeatureDefinition) {
                              lookupType = model.getElement(((StructuralFeatureDefinition)featureDef).getType());
                          }
                      }
                      catch(Exception e0) {}
                  }
                  String id = Action.getParameter(parameter, Action.PARAMETER_ID);
                  
                  // start lookup navigation either on parent or object itself
                  PersistenceManager pm = application.getNewPmData();
                  RefObject_1_0 startWith = currentView.getParentObject() != null ? 
                      (RefObject_1_0)pm.getObjectById(currentView.getParentObject().refGetPath()) :
                    	  currentView.getEditObjectIdentity() != null ?
                        	  (RefObject_1_0)pm.getObjectById(currentView.getEditObjectIdentity()) :
                        		  (RefObject_1_0)pm.getObjectById(currentView.getRefObject().refGetPath()); 
                  Object[] parameterValues = (Object[]) parameterMap.get(WebKeys.REQUEST_PARAMETER_FILTER_VALUES);
                  String filterValues = parameterValues == null ? null : (parameterValues.length > 0 ? (String) parameterValues[0] : null);                  
                  nextView = application.getPortalExtension().getLookupView(
                      id,
                      lookupType,
                      startWith,
                      filterValues,
                      application
                  );
                }
                catch(Exception e) {
                  new ServiceException(e).log();
                  String lookupTypeName = null;
                  try {
                      lookupTypeName = lookupType == null ? 
                          null : 
                          (String)lookupType.objGetValue("qualifiedName");
                  } 
                  catch(Exception e0) {}
                  application.addErrorMessage(
                    application.getTexts().getErrorTextCanNotLookupObject(),
                    new String[]{lookupTypeName, e.getMessage()}
                  );
                }
                break;
            }            
        }
        return new HandleEventResult(
            nextView
        );
    }

    //-------------------------------------------------------------------------
    public static boolean acceptsEvent(
        int event
    ) {
        return
            (event == Action.EVENT_FIND_OBJECT);
    }

}
