/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestApp_1Plugin.java,v 1.11 2004/12/11 20:30:42 hburger Exp $
 * Description: TestApp_1Plugin class
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/12/11 20:30:42 $
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
package org.openmdx.test.test.app1.plugin.jmi;

import java.util.List;
import java.util.Set;

import org.openmdx.base.accessor.jmi.cci.RefObjectFactory_1;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.dispatching.Plugin_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

//-----------------------------------------------------------------------------
// TestApp_1Jmi
//-----------------------------------------------------------------------------
public class TestApp_1Plugin
    extends Plugin_1 {

  //---------------------------------------------------------------------------
  // Plugin_1
  //---------------------------------------------------------------------------

  //---------------------------------------------------------------------------
  protected Set getDirectAccessPaths(
  ) throws ServiceException {
    Set paths = super.getDirectAccessPaths();
    paths.add(new Path("xri:@openmdx:org.openmdx.test.app1/provider/:*/segment/:*/person/:*"));
    paths.add(new Path("xri:@openmdx:org.openmdx.test.app1/provider/:*/segment/:*/address/:*"));
    paths.add(new Path("xri:@openmdx:org.openmdx.test.app1/provider/:*/segment/:*/productGroup/:*"));
    paths.add(new Path("xri:@openmdx:org.openmdx.test.app1/provider/:*/segment/:*/productGroup/:*/product/:*"));
    return paths;
  }
  
  //---------------------------------------------------------------------------
  /**
   * This plugin uses the Object_1Accessor. The classes AddressFormatImpl,
   * AddressImpl, etc. implement the application logic, i.e. the derived
   * and behavioural features.
   */
  protected ObjectFactory_1_0 getObjectFactory(
  ) throws ServiceException {
    return new RefObjectFactory_1(
      new RefRootPackage_1(
        new DelegatingObjectFactory(this.getManager()),
        this.getPackageImpls(),
        null
      ),
      this.getDirectAccessPaths()
    );
  }
  
  //---------------------------------------------------------------------------
  static class DelegatingObjectFactory 
    implements ObjectFactory_1_0 {

    //-------------------------------------------------------------------------
    public DelegatingObjectFactory(
      ObjectFactory_1_0 manager
    ) {
      this.objectFactory = manager;
    }
      
    //-------------------------------------------------------------------------
    private boolean isProduct(
      Path p
    ) {
      return
        p.size() == 9 &&
        "product".equals(p.get(p.size()-2)) && 
        "productGroup".equals(p.get(p.size()-4));
    }      
    
    //-------------------------------------------------------------------------
    private boolean isProduct(
      String typeName
    ) {
      return "org:openmdx:test:app1:Product".equals(typeName);
    }      
    
    //-------------------------------------------------------------------------
    public void close(
    ) throws ServiceException {
      this.objectFactory.close();
    }

    //-------------------------------------------------------------------------
    public Object_1_0 createObject(
      String objectClass,
      Object_1_0 initialValues
    ) throws ServiceException {
      if(this.isProduct(objectClass)) {
        throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.NOT_SUPPORTED,
          null, 
          "products can not be created"
        );
      }
      else {
        return this.objectFactory.createObject(
          objectClass,
          initialValues
        );
      }
        }

    //-------------------------------------------------------------------------
    public Object_1_0 createObject(
      String objectClass
    ) throws ServiceException {
      if(this.isProduct(objectClass)) {
        throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.NOT_SUPPORTED,
          null, 
          "products can not be created"
        );
      }
      else {
        return this.objectFactory.createObject(
          objectClass
        );
      }
        }

    //-------------------------------------------------------------------------
    public Structure_1_0 createStructure(
      String type,
      List fieldNames,
      List fieldValues
    ) throws ServiceException {
      if(this.isProduct(type)) {
        throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.NOT_SUPPORTED,
          null, 
          "products can not be created"
        );
      }
      else {
        return this.objectFactory.createStructure(
          type,
          fieldNames,
          fieldValues
        );
      }
    }

    //-------------------------------------------------------------------------
    public Object_1_0 getObject(
      Object accessPath
    ) throws ServiceException {
      if(this.isProduct((Path)accessPath)) {
        return new DelegatingProductImpl(
          (Path)accessPath
        );
      }
      else {
        return this.objectFactory.getObject(
          accessPath
        );
      }
    }

    //-------------------------------------------------------------------------
    public UnitOfWork_1_0 getUnitOfWork(
    ) throws ServiceException {
      return this.objectFactory.getUnitOfWork();
    }

    //-------------------------------------------------------------------------
    public Object marshal(
      Object source
    ) throws ServiceException {
      return this.objectFactory.marshal(source);
    }

    //-------------------------------------------------------------------------
    public Object unmarshal(
      Object source
    ) throws ServiceException {
      return this.objectFactory.unmarshal(source);
    }
    
    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
    private final ObjectFactory_1_0 objectFactory;

  }

  //---------------------------------------------------------------------------
  // Variables
  //---------------------------------------------------------------------------

}

//--- End of File -----------------------------------------------------------
