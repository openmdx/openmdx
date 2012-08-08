/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ExporterPlugin.java,v 1.9 2005/07/27 19:18:20 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/07/27 19:18:20 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 */

package org.openmdx.model1.poseidon.plugin;

import java.util.Properties;

import org.argouml.util.plugin.PoseidonModuleInstall;
import org.omg.uml.modelmanagement.UmlPackage;
import org.openmdx.kernel.log.SysLog;

import com.gentleware.poseidon.openapi.PoseidonResourceConnector;
import com.gentleware.poseidon.openapi.PoseidonUIConnector;

public class ExporterPlugin
  extends PoseidonModuleInstall
{

  /**
   * Implements <code>Seerializable</code> 
   */
  private static final long serialVersionUID = -4428943473627658752L;
  
  public ExporterPlugin(
  ) {
    super(Version.SPECIFICATION_TITLE, null, Version.SPECIFICATION_VERSION);
  }

  /** gets called on plugin installation */
  public void installed(
  ) {
    // Note: Poseidon checks whether there is a valid licence registered
    // to run this plugin

    Properties logProperties = new Properties();
    logProperties.put("ApplicationId", Version.SPECIFICATION_TITLE);

    // set openMDX log level according to Poseidon log level

    // due to an error in Poseidon (Poseidon crashes if the log level
    // DEBUG is used), openMDX relies on the commonly used environment
    // variable org.opendmx.log.level to determine the log level
    // instead of using Services.isDebugEnabled()
//    logProperties.put(
//      "LogLevel", 
//      Services.isDebugEnabled() ? "trace" : "info"
//    );
    logProperties.put(
      "LogLevel",
      System.getProperty("org.opendmx.log.level") == null ? "info" : System.getProperty("org.opendmx.log.level")
    );
    
    SysLog.setLogProperties(logProperties);

    // add openMDX logging mechanism for Poseidon; this will redirect all openMDX
    // logging messages to Poseidon's log service (writes to Poseidon.log)
    SysLog.getLogger().getMechanismManager().addMechanism(PoseidonLoggingMechanism.getInstance());

    PoseidonResourceConnector.addLanguageResource(RESOURCE_BUNDLE_NAME, RESOURCE_BUNDLE_PATH, this.getClass().getClassLoader());
    PoseidonResourceConnector.addResourceLocation(ICON_RESOURCE_PATH, this.getClass().getClassLoader());
    PoseidonUIConnector.addMenuItem(ExporterAction.class, MAIN_MENU_ENTRY);
    PoseidonUIConnector.addContextMenuItem(UmlPackage.class, ExporterAction.class);
    PoseidonUIConnector.addToolbarEntry(ExporterAction.class);
  }

  /**
   * Called when an already-installed module is restored (at IDE startup time).
   * Should perform whatever initializations are required.
   */
  public void restored(
  ) {
    installed();
  }

  /**
  * Called when the module is loaded and the version is higher than
  * by the previous load
  * The default implementation calls restored().
  * @param release The major release number of the <B>old</B> module code name or -1 if not specified.
  * @param specVersion The specification version of the this <B>old</B> module.
  */
  public void updated(
    final int release, 
    final String specVersion
  ) {
    installed();
  }

  /** Module was uninstalled. */
  public void uninstalled(
  ) {
    PoseidonUIConnector.removeContextMenuItem(UmlPackage.class, ExporterAction.class);
    PoseidonUIConnector.removeMenuItem(ExporterAction.class.getName(), MAIN_MENU_ENTRY);
    PoseidonUIConnector.removeToolbarEntry(ExporterAction.class);
    PoseidonResourceConnector.removeResourceLocation(ICON_RESOURCE_PATH);
    PoseidonResourceConnector.removeLanguageResource(RESOURCE_BUNDLE_NAME);

    // remove openMDX logging mechanism for Poseidon
    SysLog.getLogger().getMechanismManager().removeMechanism(PoseidonLoggingMechanism.getInstance().getName());
  }

  protected final static String MAIN_MENU_ENTRY = "Generation";
  public final static String RESOURCE_BUNDLE_NAME = "openMDXExporter";
  private static final String RESOURCE_BUNDLE_PATH = "org/openmdx/model1/poseidon/plugin/openMDXExporterResourceBundle";
  private static final String ICON_RESOURCE_PATH = "org/openmdx/model1/poseidon/plugin";
}