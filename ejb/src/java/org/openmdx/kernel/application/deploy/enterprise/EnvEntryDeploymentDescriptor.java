/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: EnvEntryDeploymentDescriptor.java,v 1.4 2010/06/04 22:44:59 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:44:59 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 */

package org.openmdx.kernel.application.deploy.enterprise;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.openmdx.kernel.application.deploy.spi.Report;
import org.openmdx.kernel.naming.Contexts;
import org.w3c.dom.Element;

@SuppressWarnings("unchecked")
public class EnvEntryDeploymentDescriptor
  extends AbstractConfiguration
{

  @Override
public void parseXml(
    Element element,
    Report report
  ) {
    name = getElementContent(getUniqueChild(element, "env-entry-name", report));
    type = getElementContent(getUniqueChild(element, "env-entry-type", report));
    type = type.trim();
    value = getElementContent(getOptionalChild(element, "env-entry-value", report));
  }

  @Override
public void parseOpenMdxXml(
    Element element,
    Report report
  ) {
      //
  }

  private void appendValue(
      Appendable buffer,
      CharSequence value
  ) throws IOException {
      if(value == null){
          buffer.append(" null");
      } else {
          buffer.append(
              " '"
          ).append(
              value
          ).append(
              "'"
          );
      }
  }


  public void bindEnvEntry(
      Context context,
      Report report,
      Map<String,String> applicationClientEnvEntryValues
  ) throws NamingException {
      String source = applicationClientEnvEntryValues == null ? null : applicationClientEnvEntryValues.get(this.getName());
      String type = this.getType();
      StringBuilder info = new StringBuilder("Binding ");
      try {
          if(source == null) {
              source = this.getValue();
              appendValue(
                  info.append("env-entry value"),
                  source
              );
          } else {
              appendValue(
                  info.append("command line value"),
                  value
              );
              if(this.getValue() != null) appendValue(
                  info.append(" overriding env-entry value"),
                  this.getValue()
              );
          }
          appendValue(
              info.append(" of type ").append(type).append(" to"),
              this.getName()
          );
          if (this.typeIsValid()) {
              Object value = source == null ?
                  null :
              "java.lang.String".equals(type) ?
                  source :
              "java.lang.Integer".equals(type) ?
                  new Integer(source) :
              "java.lang.Long".equals(type) ?
                  new Long(source) :
              "java.lang.Double".equals(type) ?
                  new Double(source) :
              "java.lang.Float".equals(type) ?
                  new Float(source) :
              "java.lang.Byte".equals(type) ?
                  new Byte(source) :
              "java.lang.Short".equals(type) ?
                  new Short(source) :
              "java.lang.Boolean".equals(type) ?
                  new Boolean(source) :
              "java.lang.Character".equals(type) ?
                  new Character(source.charAt(0)) :
                  (Object)null; // Should never reach this expression
              Contexts.bind(context, this.getName(), value);
              report.addInfo(info.toString());
          } else {
              report.addError(
                  info.append(" failed as its type is not supported").toString()
              );
          }
      } catch (IOException exception) {
          report.addError(
              info.append(" failed").toString(),
              exception
          );
      } catch (RuntimeException exception) {
          report.addError(
              info.append(" failed").toString(),
              exception
          );
      }
  }

  @Override
public void verify(
    Report report
  ) {
    super.verify(report);

    if (this.getName().length() == 0){
      report.addError("No value for 'env-entry-name' defined for ejb environment entry");
    }
    if (this.getType().length() == 0) {
      report.addError("No value for 'env-entry-type' defined for ejb environment entry " + this.getName());
    } else if (!this.typeIsValid()) {
      report.addError("Invalid 'env-entry-type' (" + this.getType() + ") defined for ejb environment entry " + this.getName());
    }
  }

  public String getName(
  ) {
    return this.name;
  }

  public String getType(
  ) {
    return this.type;
  }

  public String getValue(
  ) {
    return this.value;
  }

  private final boolean typeIsValid(
  ){
        return validEnvEntryTypes.contains(getType());
  }

  private String name;
  private String type;
  private String value;
  private static final Collection validEnvEntryTypes = Arrays.asList(
      "java.lang.String",
      "java.lang.Integer",
      "java.lang.Long",
      "java.lang.Double",
      "java.lang.Float",
      "java.lang.Byte",
      "java.lang.Short",
      "java.lang.Boolean",
      "java.lang.Character"
  );
}
