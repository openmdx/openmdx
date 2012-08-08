/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: BirtReportDefinition.java,v 1.8 2009/06/16 17:08:27 wfro Exp $
 * Description: Reports
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/16 17:08:27 $
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.xml.sax.SAXException;

public class BirtReportDefinition
    extends ReportDefinition
    implements Serializable {
    
    //-----------------------------------------------------------------------
    public BirtReportDefinition(
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
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document document = documentBuilder.parse(is);                
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
                    else if("text-property".equals(reportElement.getNodeName())) {
                        org.w3c.dom.Node propertyName = reportElement.getAttributes().getNamedItem("name");
                        reportProperties.put(
                            propertyName.getNodeValue(),                            
                            reportElement.getFirstChild().getNodeValue()
                        );                            
                    }
                    else if("html-property".equals(reportElement.getNodeName())) {
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
                                    if(
                                        "text-property".equals(parameterProperty.getNodeName()) ||
                                        "property".equals(parameterProperty.getNodeName()) ||
                                        "expression".equals(parameterProperty.getNodeName())
                                    ) {
                                        org.w3c.dom.Node parameterPropertyName = parameterProperty.getAttributes().getNamedItem("name");
                                        parameterProperties.put(
                                            parameterPropertyName.getNodeValue(),
                                            parameterProperty.getFirstChild().getNodeValue()
                                        );                                                                
                                    }
                                    parameterProperty = parameterProperty.getNextSibling();
                                } 
                                SysLog.info("scalar parameter " + parameterProperties);
                                parameters.add(
                                    new Parameter(
                                        (String)parameterProperties.get("name"),
                                        (String)parameterProperties.get("promptText"),
                                        (String)parameterProperties.get("promptText"),
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
                SysLog.info("report " + reportProperties);
                if(reportProperties.get("title") != null) {
                    this.label = (String)reportProperties.get("title");
                }
                if(reportProperties.get("description") != null) {
                    this.toolTip = (String)reportProperties.get("description");
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
        return "run";
    }
    
    //-----------------------------------------------------------------------
    public boolean askForReportFormat(
    ) {
        return true;
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 379308474038714868L;

}

//--- End of File -----------------------------------------------------------
