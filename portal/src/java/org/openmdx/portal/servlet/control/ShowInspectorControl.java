/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ShowInspectorControl.java,v 1.11 2008/05/01 21:43:55 wfro Exp $
 * Description: ShowObjectView 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/01 21:43:55 $
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.reports.ReportDefinitionFactory;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;

// ---------------------------------------------------------------------------
public class ShowInspectorControl 
    extends InspectorControl 
    implements Serializable {

    // -------------------------------------------------------------------------
    public ShowInspectorControl(
        String id, 
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory,
        WizardDefinitionFactory wizardFactory,
        ReportDefinitionFactory reportFactory,
        org.openmdx.ui1.jmi1.Inspector inspector,
        String forClass
    ) {
        super(
            id, 
            locale, 
            localeAsIndex,
            controlFactory,
            inspector
        );

        AppLog.detail("Preparing operation and reference panes");
        List<Object> paneOp = new ArrayList<Object>();
        List<Object> paneRef = new ArrayList<Object>();
        for(Iterator i = inspector.getMember().iterator(); i.hasNext();) {
            Object pane = i.next();
            if (pane instanceof org.openmdx.ui1.jmi1.OperationPane) {
                paneOp.add(pane);
            }
            else if (pane instanceof org.openmdx.ui1.jmi1.ReferencePane) {
                if(((org.openmdx.ui1.jmi1.ReferencePane)pane).getMember().size() > 0) {
                    paneRef.add(pane);
                }
            }
        }
        this.paneOp = (org.openmdx.ui1.jmi1.OperationPane[])paneOp.toArray(new org.openmdx.ui1.jmi1.OperationPane[paneOp.size()]);
        this.paneRef = (org.openmdx.ui1.jmi1.ReferencePane[])paneRef.toArray(new org.openmdx.ui1.jmi1.ReferencePane[paneRef.size()]);

        // Operation pane
        AppLog.detail("Preparing operation panes");
        List<OperationPaneControl> operationPaneControls = new ArrayList<OperationPaneControl>();
        for (int i = 0; i < this.paneOp.length; i++) {
            org.openmdx.ui1.jmi1.OperationPane pane = this.paneOp[i];
            operationPaneControls.add(
                controlFactory.createOperationPaneControl(
                    null, 
                    locale,
                    localeAsIndex,
                    pane, 
                    i,
                    forClass
                )
            );
        }
        this.operationPaneControl = (OperationPaneControl[])operationPaneControls.toArray(
            new OperationPaneControl[operationPaneControls.size()]
        );

        // Reference pane
        AppLog.detail("Preparing reference panes");
        this.referencePaneControl = new ReferencePaneControl[this.paneRef.length];
        for(int i = 0; i < this.referencePaneControl.length; i++) {
            this.referencePaneControl[i] = controlFactory.createReferencePaneControl(
                null, 
                locale, 
                localeAsIndex,
                this.paneRef[i], 
                i, 
                forClass
            );
        }

        // Reports
        AppLog.detail("Preparing reports");
        this.reportControl = controlFactory.createReportControl(
            null, 
            locale,
            localeAsIndex,
            reportFactory.findReportDefinitions(forClass, locale, null)
        );
        AppLog.detail("Preparing reports done");

        // Wizards
        AppLog.detail("Preparing wizards");
        this.wizardControl = controlFactory.createWizardControl(
            null, 
            locale,
            localeAsIndex,
            wizardFactory.findWizardDefinitions(forClass, locale, null)
        );
        AppLog.detail("Preparing wizards done");
    }

    // -------------------------------------------------------------------------
    public int getPageSizeParameter(
        Map parameterMap
    ) {
        Object[] pageSizes = (Object[]) parameterMap.get(WebKeys.REQUEST_PARAMETER_PAGE_SIZE);
        String pageSize = pageSizes == null ? null : (pageSizes.length > 0 ? (String) pageSizes[0] : null);
        return pageSize == null ? -1 : Integer.parseInt(pageSize);
    }

    // -------------------------------------------------------------------------
    public OperationPaneControl[] getOperationPaneControl(
    ) {
        return this.operationPaneControl;
    }

    // -------------------------------------------------------------------------
    public ReferencePaneControl[] getReferencePaneControl(
    ) {
        return this.referencePaneControl;
    }

    // -------------------------------------------------------------------------
    public ReportControl getReportControl(
    ) {
        return this.reportControl;
    }

    // -------------------------------------------------------------------------
    public WizardControl getWizardControl(
    ) {
        return this.wizardControl;
    }

    // -------------------------------------------------------------------------
    public void selectFilter(
        String filterName, 
        String filterValues
    ) {
    }

    //-------------------------------------------------------------------------
    @Override
    public void paint(
        HtmlPage p,
        String frame,
        boolean forEditing
    ) throws ServiceException {
        // Do not paint here. For more flexibility paint is implemented in JSP
    }
  
    // -------------------------------------------------------------------------
    // Variables
    // -------------------------------------------------------------------------
    private static final long serialVersionUID = 3257844376976635442L;
    
    protected OperationPaneControl[] operationPaneControl = null;
    protected ReferencePaneControl[] referencePaneControl = null;
    protected ReportControl reportControl = null;
    protected WizardControl wizardControl = null;
    protected org.openmdx.ui1.jmi1.OperationPane[] paneOp = null;
    protected org.openmdx.ui1.jmi1.ReferencePane[] paneRef = null;

}

// --- End of File -----------------------------------------------------------
