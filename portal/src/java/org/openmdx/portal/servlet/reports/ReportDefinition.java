/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ReportDefinition.java,v 1.13 2008/08/12 16:38:08 wfro Exp $
 * Description: Reports
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/12 16:38:08 $
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
package org.openmdx.portal.servlet.reports;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openmdx.base.exception.ServiceException;

public abstract class ReportDefinition
    implements Serializable {
    
    //-----------------------------------------------------------------------
    public static class Parameter
        implements Serializable {
        
        public Parameter(
            String name,
            String label,
            String toolTip,
            String dataType,
            String defaultValue
        ) {
            this.name = name;
            this.label = label;
            this.toolTip = toolTip;
            this.dataType = dataType;
            this.defaultValue = defaultValue == null ? "" : defaultValue;
        }

        public String getName() {
            return this.name;
        }
        
        public String getDataType() {
            return this.dataType;
        }
        
        public String getLabel() {
            return this.label;
        }

        public String getToolTip() {
            return this.toolTip;
        }
        
        public String getDefaultValue() {
            return this.defaultValue;
        }
        
        private static final long serialVersionUID = 6358963901233808777L;

        private final String name;
        private final String label;
        private final String toolTip;
        private final String dataType;
        private final String defaultValue;
    }
    
    //-----------------------------------------------------------------------
    public ReportDefinition(
        String name,
        String locale,
        short index,
        InputStream is
    ) throws ServiceException {

        // Report names are of the form 
        // [/]WEB-INF/config/report/locale/<class name>-<report name>.ext

        // Remove leading /
        this.name = name.startsWith("/")
            ? name.substring(1)
            : name;
        this.locale = locale;
        this.index = index;

        // Report names are of the form 
        // report/<locale>/[<class name>-]<report name>.ext
        // Extract the type from the name
        String s = name.substring(name.lastIndexOf("/") + 1);
        // As default take file name as forClass
        this.forClass = new ArrayList<String>();
        this.forClass.add(
            s.indexOf("-") > 0
                ? s.substring(0, s.indexOf("-")).replace('.', ':')
                : "org:openmdx:base:Void"
        );
        
        // Extract the type from the report name
        this.type = s.substring(0, s.indexOf("-")).replace('.', ':');        
        this.label = s;
        this.toolTip = s;
        this.order = new ArrayList<String>();
        this.order.add("0");    
        
    }
    
    //-----------------------------------------------------------------------
    public short getLocaleIndex(
    ) {
        return this.index;
    }
  
    //-----------------------------------------------------------------------
    public String getLocale(
    ) {
        return this.locale;
    }
      
    //-----------------------------------------------------------------------
    public String getName(
    ) {
        return this.name;
    }

    //-----------------------------------------------------------------------
    public String getAction(
    ) {
        return this.name;
    }
    
    //-----------------------------------------------------------------------
    public List getForClass(
    ) {
        return this.forClass;
    }

    //-----------------------------------------------------------------------
    public String getLabel(
    ) {
        return this.label;
    }

    //-----------------------------------------------------------------------
    public boolean askForReportFormat(
    ) {
        return false;
    }
    
    //-----------------------------------------------------------------------
    public Parameter[] getParameter(
    ) {
        return this.parameters;
    }

    //-----------------------------------------------------------------------
    public String getToolTip(
    ) {
        return this.toolTip;
    }

    //-----------------------------------------------------------------------
    public List getOrder(
    ) {
        return this.order;
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 1734820694768521769L;

    protected final String locale;
    protected final short index;
    protected final String name;
    protected List<String> forClass;
    protected final String type;
    protected String label;
    protected String toolTip;
    protected List<String> order;    
    protected Parameter[] parameters = new Parameter[0];
    
}

//--- End of File -----------------------------------------------------------
