/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: UiContext.java,v 1.12 2007/12/28 10:41:32 wfro Exp $
 * Description: UiContext 
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/28 10:41:32 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.compatibility.base.naming.Path;

public class UiContext
    implements Serializable {

    //-------------------------------------------------------------------------
    public UiContext(
        Path uiSegmentPath,
        PersistenceManager pm
    ) throws ServiceException {
        this.uiSegmentPath = uiSegmentPath;
        this.pm = pm;
        this.uiPackage = (org.openmdx.ui1.jmi1.Ui1Package)((Authority)pm.getObjectById(
            Authority.class,
            org.openmdx.ui1.jmi1.Ui1Package.AUTHORITY_XRI
        )).refImmediatePackage();
        this.reset();
    }
    
    //-------------------------------------------------------------------------
    public void reset(
    ) {
        this.uiSegment = null;
        this.assertableInspectors = null;        
    }
    
    //-------------------------------------------------------------------------
    public synchronized Map getAssertableInspectors(
    ) throws ServiceException {
        if(this.assertableInspectors == null) {
            this.assertableInspectors = new HashMap();
            Collection assertableInspectors = this.getUiSegment().getAssertableInspector();
            for(Iterator i = assertableInspectors.iterator(); i.hasNext(); ) {
              org.openmdx.ui1.jmi1.AssertableInspector assertableInspector = (org.openmdx.ui1.jmi1.AssertableInspector)i.next();
              AppLog.detail("adding assertable inspector", assertableInspector.getForClass());
              this.assertableInspectors.put(
                  assertableInspector.getForClass(),
                  assertableInspector
              );
            }
        }
        return this.assertableInspectors;
    }
    
    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.AssertableInspector getAssertableInspector(
        String forClass
    ) throws ServiceException {
        return (org.openmdx.ui1.jmi1.AssertableInspector)this.getAssertableInspectors().get(forClass);
    }
  
    //-------------------------------------------------------------------------
    public String getLabel(
        String forClass,
        int localeAsIndex
    ) {
        org.openmdx.ui1.jmi1.AssertableInspector inspector = null;
        try {
            inspector = this.getAssertableInspector(forClass);
        }
        catch(ServiceException e) {
            AppLog.warning(e.getMessage(), e.getCause(), 1);
        }
        if(inspector == null) {
            AppLog.warning("can not get inspector for", forClass);
            return "N/A";
        }
        else {
            List label = inspector.getLabel();
            return localeAsIndex < label.size()
                ? (String)label.get(localeAsIndex)
                : (String)label.get(0);
        }
    }
  
      //-------------------------------------------------------------------------
      public String getIconKey(
          String forClass
      ) throws ServiceException {
          org.openmdx.ui1.jmi1.AssertableInspector inspector = this.getAssertableInspector(forClass);
          if(inspector != null) {
              String iconKey = this.getAssertableInspector(forClass).getIconKey();
              return iconKey == null
              ? WebKeys.ICON_DEFAULT
                  : iconKey.substring(iconKey.lastIndexOf(":") + 1) + WebKeys.ICON_TYPE;
          }
          else {
              return  WebKeys.ICON_DEFAULT;
          }
      }
  
    //-------------------------------------------------------------------------
    public String getBackColor(
        String forClass
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.AssertableInspector inspector = this.getAssertableInspector(forClass);
        if(inspector != null) {
            return this.getAssertableInspector(forClass).getBackColor();
        }
        else {
            return null;
        }
    }
  
    //-------------------------------------------------------------------------
    public String getColor(
        String forClass
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.AssertableInspector inspector = this.getAssertableInspector(forClass);
        if(inspector != null) {
            return this.getAssertableInspector(forClass).getColor();
        }
        else {
            return null;
        }
    }
  
    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.Segment getUiSegment(
    ) throws ServiceException {
        if(this.uiSegment == null) {
            org.openmdx.base.jmi1.Provider provider = (org.openmdx.base.jmi1.Provider)pm.getObjectById(
                this.uiSegmentPath.getParent().getParent().toXri()
            );
            this.uiSegment = (org.openmdx.ui1.jmi1.Segment)provider.getSegment(
                this.uiSegmentPath.getBase()
            );            
        }
        return this.uiSegment;
    }
    
    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.Inspector getAssertedInspector(
        String forClass
    ) {    
        // lazy init
        if(this.assertedInspectors == null) {
            this.assertedInspectors = new HashSet();
        }
        // try to get inspector without asserting
        if(this.assertedInspectors.contains(forClass)) {
            try {
                AppLog.trace("returning inspector", forClass);
                return (org.openmdx.ui1.jmi1.Inspector)this.getUiSegment().getElement(forClass);
            }
            catch(Exception e) {}
        }
        // assert it. This is done on-demand because it takes quite some CPU
        try {
            AppLog.trace("asserting inspector", forClass);
            synchronized(this.assertedInspectors) {
                this.getUiSegment().assertInspector(
                    this.uiPackage.createSegmentAssertInspectorParams(forClass)
                );
                this.assertedInspectors.add(forClass);
            }
        }
        catch(Exception e0) {
            // can not assert it
            ServiceException s0 = new ServiceException(e0);
            AppLog.warning("can not assert inspector", s0.getMessage());
            AppLog.warning(s0.getMessage(), s0.getCause(), 1);
            return null;
        }
        // try again
        try {
            return (org.openmdx.ui1.jmi1.Inspector)this.getUiSegment().getElement(forClass);
        }
        catch(Exception e0) {}
        return null;
    }

    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.Inspector getInspector(
        String forClass
    ) throws ServiceException {
        Map assertableInspectors = this.getAssertableInspectors();
        org.openmdx.ui1.jmi1.Inspector inspector = null;
        if(assertableInspectors.containsKey(forClass)) {
            inspector = this.getAssertedInspector(forClass);
            if(inspector == null) {
                inspector = this.getAssertedInspector("org:openmdx:base:BasicObject");
            }
        }
        else {
            AppLog.warning("inspector not in set of assertable inspectors. Fallback to BasicObject", forClass);
            inspector = this.getAssertedInspector("org:openmdx:base:BasicObject");              
        }
        return inspector;
    }
    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 4051043086039789875L;

    private transient org.openmdx.ui1.jmi1.Segment uiSegment = null;
    private transient Map assertableInspectors = null;  
    private transient Set assertedInspectors = null;
    private final org.openmdx.ui1.jmi1.Ui1Package uiPackage;    
    private final PersistenceManager pm;
    
    private Path uiSegmentPath;
    
}

//--- End of File -----------------------------------------------------------
