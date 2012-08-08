/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestApp_1Plugin.java,v 1.12 2005/07/27 19:55:26 hburger Exp $
 * Description: TestApp_1Plugin class
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/07/27 19:55:26 $
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
package org.openmdx.test.test.app1.plugin.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_1;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.event.InstanceCallbackListener;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.dispatching.Plugin_1;
import org.openmdx.compatibility.base.marshalling.CachingMarshaller;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;

public class TestApp_1Plugin
  extends Plugin_1 {
    
  //---------------------------------------------------------------------------
  // Plugin_1
  //---------------------------------------------------------------------------

  //---------------------------------------------------------------------------
  protected ObjectFactory_1_0 getObjectFactory(
  ) throws ServiceException {
    return new ObjectImplFactory(
      this.getManager()
    );
  }

  //---------------------------------------------------------------------------
  protected Set getDirectAccessPaths(
  ) throws ServiceException {
    Set paths = super.getDirectAccessPaths();
    return paths;
  }
  
  //---------------------------------------------------------------------------
  // ObjectImplFactory
  //---------------------------------------------------------------------------

  //---------------------------------------------------------------------------
  class ObjectImplFactory
    extends CachingMarshaller
    implements ObjectFactory_1_1 {

    /**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = 7133251776818261390L;

	//---------------------------------------------------------------------------
    public ObjectImplFactory(
      ObjectFactory_1_1 manager
    ) {
        super();
      this.objectFactory = manager;
    }
    
    //---------------------------------------------------------------------------
    // CachingMarshaller
    //---------------------------------------------------------------------------

    //---------------------------------------------------------------------------
    public Object createMarshalledObject(
      Object source
    ) throws ServiceException {
      String objectClass = null;
      if(source instanceof Object_1_0) try {
        objectClass = ((Object_1_0)source).objGetClass();
        String className = objectClass.substring(objectClass.lastIndexOf(":") + 1);
        Class impl = Classes.getApplicationClass(
          this.getClass().getPackage().getName() + "." + className + "Impl"
        );
        Constructor constructor = impl.getConstructor(
          new Class[]{
            Object_1_0.class,
            ObjectFactory_1_0.class,
            Marshaller.class
          }
        );
        Object target = constructor.newInstance(
          new Object[]{
            (Object_1_0)source,
            this.objectFactory,
            this
          }
        );
      	if(target instanceof  InstanceCallbackListener) {
      	  ((Object_1_0)source).objAddEventListener(
      	     null, 
      	     (InstanceCallbackListener)target
          );
      	}
        return target;
      } catch(NoSuchMethodException e) {
          throw new ServiceException(
            e,
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.TRANSFORMATION_FAILURE, 
            new BasicException.Parameter[]{
              new BasicException.Parameter("object class", objectClass)
            },
            "can not marshal object"
          );
      } catch(IllegalAccessException e) {
          throw new ServiceException(
            e,
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.TRANSFORMATION_FAILURE, 
            new BasicException.Parameter[]{
              new BasicException.Parameter("object class", objectClass)
            },
            "can not marshal object"
          );
      } catch(InstantiationException e) {
          throw new ServiceException(
            e,
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.TRANSFORMATION_FAILURE, 
            new BasicException.Parameter[]{
              new BasicException.Parameter("object class", objectClass)
            },
            "can not marshal object"
          );
      } catch(InvocationTargetException e) {
        throw new ServiceException(
          new BasicException(
            e.getTargetException(),
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.NOT_FOUND, 
            new BasicException.Parameter[]{
              new BasicException.Parameter("object class", objectClass)
            },
            "can not marshal object"
          )
        );
      } catch(ClassNotFoundException e) {
        return new ObjectImpl(
          (Object_1_0)source,
          this.objectFactory,
          this
        );
      } else {
        return source;
      }
    }
  
    //---------------------------------------------------------------------------
    public Object unmarshal(
      Object source
    ) {
      if(source instanceof ObjectImpl) {
        return ((ObjectImpl)source).getDelegate();
      }
      else {
        return source;
      }
    }

    //---------------------------------------------------------------------------
    // ObjectFactory_1_0
    //---------------------------------------------------------------------------

    //---------------------------------------------------------------------------
    public void close(
    ) throws ServiceException {
      return;
    }
  
    //---------------------------------------------------------------------------
    public UnitOfWork_1_0 getUnitOfWork(
    ) throws ServiceException {
      return this.objectFactory.getUnitOfWork();
    }
  
    //---------------------------------------------------------------------------
    public Object_1_0 getObject(
      Object accessPath
    ) throws ServiceException {
      return (Object_1_0)this.marshal(
        objectFactory.getObject(accessPath)
      );
    }
  
    //---------------------------------------------------------------------------
    public Object_1_0 createObject(
      String objectClass
    ) throws ServiceException {
      return (Object_1_0)this.marshal(
        objectFactory.createObject(objectClass)
      );
    }
  
    //---------------------------------------------------------------------------
    public Object_1_0 createObject(
      String objectClass,
      Object_1_0 initialValues
    ) throws ServiceException {
      return (Object_1_0)this.marshal(
        objectFactory.createObject(objectClass, initialValues)
      );
    }
  
    //---------------------------------------------------------------------------
    public Structure_1_0 createStructure(
      String type,
      List fieldNames,
      List fieldValues
    ) throws ServiceException {
      return this.objectFactory.createStructure(
        type,
        fieldNames,
        fieldValues
      );
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
    private final ObjectFactory_1_1 objectFactory;

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_1#createObject(java.lang.String, java.lang.String, org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    public Object_1_0 createObject(String roleClass, String roleId, Object_1_0 roleCapable) throws ServiceException {
        return (Object_1_0)this.marshal(
             objectFactory.createObject(roleClass, roleId, (Object_1_0)this.unmarshal(roleCapable))
        );
    }

  }
      
}

//--- End of File ------------------------------------------------------------
