/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: UiContext.java,v 1.30 2009/06/16 17:08:26 wfro Exp $
 * Description: UiContext 
 * Revision:    $Revision: 1.30 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/16 17:08:26 $
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
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.log.SysLog;

public class UiContext
    implements Serializable {

    //-------------------------------------------------------------------------
    public UiContext(
        List<Path> uiSegmentPaths,
        PersistenceManager pm
    ) throws ServiceException {
        this.reset(
            uiSegmentPaths,
            pm
        );
    }
    
    //-------------------------------------------------------------------------
    protected static org.openmdx.ui1.jmi1.Ui1Package getUiPackage(
        PersistenceManager pm
    ) {
        org.openmdx.ui1.jmi1.Ui1Package uiPkg = null;
        try {
            uiPkg = (org.openmdx.ui1.jmi1.Ui1Package)((RefObject)pm.newInstance(org.openmdx.ui1.jmi1.Segment.class)).refImmediatePackage();
        }
        catch(UnsupportedOperationException e) {        
            uiPkg = (org.openmdx.ui1.jmi1.Ui1Package)((Authority)pm.getObjectById(
                Authority.class,
                org.openmdx.ui1.jmi1.Ui1Package.AUTHORITY_XRI
            )).refImmediatePackage();
        }
        return uiPkg;
    }
        
    //-------------------------------------------------------------------------
    public void reset(
        List<Path> uiSegmentPaths,
        PersistenceManager pm
    ) {
        this.uiSegmentPaths = uiSegmentPaths.toArray(new Path[uiSegmentPaths.size()]);
        this.pm = pm;
        this.uiPackage = UiContext.getUiPackage(this.pm);
        this.uiSegments = new org.openmdx.ui1.jmi1.Segment[this.uiSegmentPaths.length];
        this.allAssertableInspectors = null;        
        this.allAssertedInspectors = null;
    }
    
    //-------------------------------------------------------------------------
    public synchronized Map<String,org.openmdx.ui1.jmi1.AssertableInspector> getAssertableInspectors(
        int perspective
    ) {
        if(this.allAssertableInspectors == null) {
            this.allAssertableInspectors = new HashMap<Integer,Map<String,org.openmdx.ui1.jmi1.AssertableInspector>>();
        }        
        if(this.allAssertableInspectors.get(perspective) == null) {
            Map<String,org.openmdx.ui1.jmi1.AssertableInspector> assertableInspectors = new HashMap<String,org.openmdx.ui1.jmi1.AssertableInspector>();
            Collection<org.openmdx.ui1.jmi1.AssertableInspector> inspectors = this.getUiSegment(perspective).getAssertableInspector();
            for(org.openmdx.ui1.jmi1.AssertableInspector assertableInspector: inspectors) {
                assertableInspectors.put(
                    assertableInspector.getForClass(),
                    assertableInspector
                );
            }
            this.allAssertableInspectors.put(
                perspective,
                assertableInspectors
            );
        }
        return this.allAssertableInspectors.get(perspective);
    }
    
    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.AssertableInspector getAssertableInspector(
        String forClass,
        int perspective
    ) throws ServiceException {
        return this.getAssertableInspectors(perspective).get(forClass);
    }
  
    //-------------------------------------------------------------------------
    public String getLabel(
        String forClass,
        int localeAsIndex,
        int perspective
    ) {
        org.openmdx.ui1.jmi1.AssertableInspector inspector = null;
        try {
            inspector = this.getAssertableInspector(
                forClass, 
                perspective
            );
        }
        catch(ServiceException e) {
        	SysLog.warning(e.getMessage(), e.getCause());
        }
        if(inspector == null) {
        	SysLog.info("Can not get inspector for", forClass);
            return "N/A";
        }
        else {
            List label = inspector.getLabel();
            return localeAsIndex < label.size() ? 
                (String)label.get(localeAsIndex) : 
                (String)label.get(0);
        }
    }
  
    //-------------------------------------------------------------------------
    public String getIconKey(
        String forClass,
        int perspective
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.AssertableInspector inspector = this.getAssertableInspector(
            forClass, 
            perspective
        );
        if(inspector != null) {
            String iconKey = inspector.getIconKey();
            return iconKey == null ? 
                WebKeys.ICON_DEFAULT : 
                iconKey.substring(iconKey.lastIndexOf(":") + 1) + WebKeys.ICON_TYPE;
        }
        else {
            return  WebKeys.ICON_DEFAULT;
        }
    }
  
    //-------------------------------------------------------------------------
    public String getBackColor(
        String forClass,
        int perspective
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.AssertableInspector inspector = this.getAssertableInspector(
            forClass, 
            perspective
        );
        if(inspector != null) {
            return inspector.getBackColor();
        }
        else {
            return null;
        }
    }
  
    //-------------------------------------------------------------------------
    public String getColor(
        String forClass,
        int perspective
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.AssertableInspector inspector = this.getAssertableInspector(
            forClass, 
            perspective
        );
        if(inspector != null) {
            return inspector.getColor();
        }
        else {
            return null;
        }
    }
  
    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.Segment getUiSegment(
        int perspective
    ) {
        if(this.uiSegments[perspective] == null) {
            org.openmdx.base.jmi1.Provider provider = (org.openmdx.base.jmi1.Provider)this.pm.getObjectById(
                this.uiSegmentPaths[perspective].getParent().getParent()
            );
            this.uiSegments[perspective] = (org.openmdx.ui1.jmi1.Segment)provider.getSegment(
                this.uiSegmentPaths[perspective].getBase()
            );            
        }
        return this.uiSegments[perspective];
    }
    
    //-------------------------------------------------------------------------
    public Path[] getUiSegmentPaths(
    ) {
        return this.uiSegmentPaths;
    }
    
    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.Inspector getAssertedInspector(
        String forClass,
        int perspective
    ) {    
        if(this.allAssertedInspectors == null) {
            this.allAssertedInspectors = new HashMap<Integer,Set<String>>();
        }
        if(this.allAssertedInspectors.get(perspective) == null)  {
            this.allAssertedInspectors.put(
                perspective,
                new HashSet<String>()
            );
        }
        // Try to get inspector without asserting
        if(this.allAssertedInspectors.get(perspective).contains(forClass)) {
            try {
                return (org.openmdx.ui1.jmi1.Inspector)this.getUiSegment(perspective).getElement(forClass);
            }
            catch(Exception e) {}
        }
        // Assert on-demand makes start-up faster
        try {
        	SysLog.trace("Asserting inspector", forClass);
            synchronized(this.allAssertedInspectors) {
                this.getUiSegment(perspective).assertInspector(
                    this.uiPackage.createSegmentAssertInspectorParams(forClass)
                );
                this.allAssertedInspectors.get(perspective).add(forClass);
            }
        }
        catch(Exception e0) {
            // can not assert it
            ServiceException s0 = new ServiceException(e0);
            SysLog.warning("Can not assert inspector", s0.getMessage());
            SysLog.warning(s0.getMessage(), s0.getCause());
            return null;
        }
        // Try again
        try {
            return (org.openmdx.ui1.jmi1.Inspector)this.getUiSegment(perspective).getElement(forClass);
        }
        catch(Exception e0) {}
        return null;
    }

    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.Inspector getInspector(
        String forClass,
        int perspective
    ) {
        Map<String,org.openmdx.ui1.jmi1.AssertableInspector> assertableInspectors = this.getAssertableInspectors(perspective);
        org.openmdx.ui1.jmi1.Inspector inspector = null;
        if(assertableInspectors.containsKey(forClass)) {
            inspector = this.getAssertedInspector(
                forClass, 
                perspective
            );
            if(inspector == null) {
                inspector = this.getAssertedInspector(
                    "org:openmdx:base:BasicObject", 
                    perspective
                );
            }
        }
        else {
        	SysLog.warning("Inspector not in set of assertable inspectors. Fallback to BasicObject", forClass);
            inspector = this.getAssertedInspector(
                "org:openmdx:base:BasicObject",
                MAIN_PERSPECTIVE
            );              
        }
        return inspector;
    }
    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 4051043086039789875L;
    
    public static final int MAIN_PERSPECTIVE = 0;
    
    private Path[] uiSegmentPaths;
    private transient org.openmdx.ui1.jmi1.Segment[] uiSegments = null;
    private transient Map<Integer,Map<String,org.openmdx.ui1.jmi1.AssertableInspector>> allAssertableInspectors = null;  
    private transient Map<Integer,Set<String>> allAssertedInspectors = null;
    private org.openmdx.ui1.jmi1.Ui1Package uiPackage;    
    private PersistenceManager pm;
        
}

//--- End of File -----------------------------------------------------------
