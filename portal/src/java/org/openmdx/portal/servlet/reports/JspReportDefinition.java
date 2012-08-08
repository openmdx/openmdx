/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: JspReportDefinition.java,v 1.11 2008/08/12 16:38:08 wfro Exp $
 * Description: Reports
 * Revision:    $Revision: 1.11 $
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JspReportDefinition
    extends ReportDefinition
    implements Serializable {
    
    //-----------------------------------------------------------------------
    public JspReportDefinition(
        String name,
        String locale,
        short index,
        InputStream is
    ) throws ServiceException {
        super(
            name,
            locale,
            index,
            is
        );
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            boolean isFirstForClassTag = true;
            boolean isFirstOrderTag = true;
            while(reader.ready()) {
                String l = reader.readLine();
                // Get forClass and order definitions
                int pos = 0;
                if((pos = l.indexOf(FOR_CLASS_TAG)) >= 0) {
                    if(isFirstForClassTag) {
                        this.forClass.clear();
                        isFirstForClassTag = false;
                    }
                    this.forClass.add(
                        l.substring(
                            pos + FOR_CLASS_TAG.length(),
                            l.indexOf("\"", pos + FOR_CLASS_TAG.length() + 1)
                        )
                    );
                }
                if((pos = l.indexOf(ORDER_TAG)) >= 0) {
                    if(isFirstOrderTag) {
                        this.order.clear();
                        isFirstOrderTag = false;
                    }
                    this.order.add(
                        l.substring(
                            pos + ORDER_TAG.length(),
                            l.indexOf("\"", pos + ORDER_TAG.length() + 1)
                        )
                    );
                }
    
                // Get report definition
                if((pos = l.indexOf(REPORT_TAG_START)) >= 0) {
                    StringBuffer reportDefinition = new StringBuffer(l);
                    while(reader.ready()) {
                        l = reader.readLine();
                        reportDefinition.append(l);
                        if(l.indexOf(REPORT_TAG_END) >= 0) {
                            int posStart = reportDefinition.toString().indexOf(REPORT_TAG_START);
                            int posEnd = reportDefinition.toString().indexOf(REPORT_TAG_END);
                            Reader report = new StringReader(
                                reportDefinition.substring(
                                    posStart, 
                                    posEnd + REPORT_TAG_END.length()
                                )
                            );                
                            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                            org.w3c.dom.Document document = 
                                documentBuilder.parse(new InputSource(report));               
                            org.w3c.dom.Node reportNode = document.getElementsByTagName("report").getLength() > 0
                                ? document.getElementsByTagName("report").item(0)
                                : null;
                            if(reportNode != null) {
                                Map<String,String> reportProperties = new HashMap<String,String>();
                                org.w3c.dom.Node reportElement = reportNode.getFirstChild();
                                while(reportElement != null) {
                                    if("property".equals(reportElement.getNodeName())) {
                                        org.w3c.dom.Node propertyName = reportElement.getAttributes().getNamedItem("name");
                                        reportProperties.put(
                                            propertyName.getNodeValue(),
                                            reportElement.getFirstChild().getNodeValue()
                                        );                            
                                    }
                                    else if("parameters".equals(reportElement.getNodeName())) {
                                        List<Parameter> parameters = new ArrayList<Parameter>();
                                        org.w3c.dom.Node parameterElement = reportElement.getFirstChild();
                                        while(parameterElement != null) {
                                            if("scalar-parameter".equals(parameterElement.getNodeName())) {
                                                Map<String,String> parameterProperties = new HashMap<String,String>();
                                                parameterProperties.put(
                                                    "name",
                                                    parameterElement.getAttributes().getNamedItem("name").getNodeValue()
                                                );
                                                org.w3c.dom.Node parameterProperty = parameterElement.getFirstChild();
                                                while(parameterProperty != null) {
                                                    if("property".equals(parameterProperty.getNodeName())) {
                                                        org.w3c.dom.Node parameterPropertyName = parameterProperty.getAttributes().getNamedItem("name");
                                                        parameterProperties.put(
                                                            parameterPropertyName.getNodeValue(),
                                                            parameterProperty.getFirstChild().getNodeValue()
                                                        );                                                                
                                                    }
                                                    parameterProperty = parameterProperty.getNextSibling();
                                                } 
                                                AppLog.info("scalar parameter " + parameterProperties);
                                                parameters.add(
                                                    new Parameter(
                                                        (String)parameterProperties.get("name"),
                                                        (String)parameterProperties.get("displayName"),
                                                        (String)parameterProperties.get("displayName"),
                                                        (String)parameterProperties.get("dataType"),
                                                        (String)parameterProperties.get("defaultValue")                                        
                                                    )
                                                );
                                            }
                                            parameterElement = parameterElement.getNextSibling();                            
                                        }
                                        this.parameters = (Parameter[])parameters.toArray(new Parameter[parameters.size()]);
                                    }
                                    reportElement = reportElement.getNextSibling();
                                }
                                AppLog.info("report " + reportProperties);
                                if(reportProperties.get("description") != null) {
                                    this.toolTip = (String)reportProperties.get("description");
                                }
                                if(reportProperties.get("label") != null) {
                                    this.label = (String)reportProperties.get("label");
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        catch(ParserConfigurationException e) {
            throw new ServiceException(e);
        }
        catch(SAXException e) {
            throw new ServiceException(e);
        }
        catch(IOException e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    public String getAction(
    ) {
        return "jsp/JspReport.jsp";
    }
    
    //-----------------------------------------------------------------------
    public boolean askForReportFormat(
    ) {
        return false;
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 379308474038714868L;

    private static final String FOR_CLASS_TAG = "<meta name=\"forClass\" content=\"";
    private static final String ORDER_TAG = "<meta name=\"order\" content=\"";
        
    private static final String REPORT_TAG_START = "<report>";
    private static final String REPORT_TAG_END = "</report>";

}

//--- End of File -----------------------------------------------------------
