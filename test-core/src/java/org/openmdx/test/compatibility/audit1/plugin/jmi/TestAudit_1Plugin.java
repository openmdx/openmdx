/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestAudit_1Plugin.java,v 1.6 2008/11/14 10:25:19 hburger Exp $
 * Description: TestApp_1Plugin class
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/14 10:25:19 $
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
package org.openmdx.test.compatibility.audit1.plugin.jmi;

import java.util.Set;

import org.openmdx.base.accessor.jmi.cci.RefObjectFactory_1;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.transport.dispatching.Plugin_1;

//---------------------------------------------------------------------------
// TestAudit_1Plugin
//---------------------------------------------------------------------------
public class TestAudit_1Plugin
    extends Plugin_1 {

  //-------------------------------------------------------------------------
  // Plugin_1
  //-------------------------------------------------------------------------

  //-------------------------------------------------------------------------
  protected Set getDirectAccessPaths(
  ) throws ServiceException {
    Set paths = super.getDirectAccessPaths();
//    paths.add(new Path("xri:@openmdx:org.openmdx.compatibility.audit1/provider/:*/segment/:*/person/:*"));
//    paths.add(new Path("xri:@openmdx:org.openmdx.compatibility.audit1/provider/:*/segment/:*/address/:*"));
//    paths.add(new Path("xri:@openmdx:org.openmdx.compatibility.audit1/provider/:*/segment/:*/productGroup/:*"));
//    paths.add(new Path("xri:@openmdx:org.openmdx.compatibility.audit1/provider/:*/segment/:*/productGroup/:*/product/:*"));
    return paths;
  }
  
  //---------------------------------------------------------------------------
  protected ObjectFactory_1_0 getObjectFactory(
  ) throws ServiceException {
    return new RefObjectFactory_1(
      new RefRootPackage_1(
        this.getManager(),
        this.getPackageImpls(),
        null, // context
        null // binding
      ),
      this.getDirectAccessPaths()
    );
  }
  
  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------

}

//--- End of File -----------------------------------------------------------
