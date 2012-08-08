/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RefClass_1.java,v 1.11 2008/02/08 16:51:25 hburger Exp $
 * Description: RefClass_1 class
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:51:25 $
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

/**
 * @author wfro
 */
package org.openmdx.base.accessor.jmi.spi;
// definitions for JDK
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jdo.listener.ClearCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.StoreCallback;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefEnum;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefClass_1_1;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_1;
import org.openmdx.base.event.InstanceCallbackListener;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.java.Identifier;

//---------------------------------------------------------------------------
/**
 * Standard implementation of RefClass_1_0. The implementation does not
 * support class-level features.
 * <p>
 * This implementation supports efficient serialization. The immediate
 * package is the only member. Other members are transient.
 */
public abstract class RefClass_1
  implements RefClass_1_1, Serializable {

  //-------------------------------------------------------------------------
  public RefClass_1(
    RefPackage_1_0 refPackage
  ) {
    this.refPackage = refPackage;
  }

  //-------------------------------------------------------------------------
  private ObjectFactory_1_0 refObjectFactory(
  ) {
    return ((RefRootPackage_1)this.refOutermostPackage()).refObjectFactory();
  }

  //-------------------------------------------------------------------------
  // RefClass
  //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public RefObject refCreateInstance(
        List iargs
    ) {
        return this.refCreateInstance(
            iargs,
            this
        );
    }
    
    //-------------------------------------------------------------------------
    /**
     * @param refClass refClass is supplied as class when constructing the object, i.e.
     *        the method refClass() of the constructed object returns refClass.
     */
    @SuppressWarnings("unchecked")
    public RefObject refCreateInstance(
        List iargs,
        RefClass refClass
    ) {
        List args = iargs;
        if(this.defaultImplConstructor == null) {
    
            /**
             * extract the class name from the refMofId() which contains the
             * fully qualified class name as ':' separated name components
             */
            String packageName = refMofId().substring(0, refMofId().lastIndexOf(':'));
            String className = refMofId().substring(refMofId().lastIndexOf(":") + 1);
            String bindingPackageSuffix = ((RefRootPackage_1)this.refOutermostPackage()).refBindingPackageSuffix();
            /**
             * Check whether the instance-level implementation class must be loaded 
             * from the standard location or from configured alternate location.
             */
    
            // get the interface class
            String classNameIntf =
                packageName.replace(':', '.') + "." +
                bindingPackageSuffix + "." +
                className;
            try {
                this.intfClass = Classes.getApplicationClass(classNameIntf);
            } 
            catch(Throwable t) {
                try {
                    classNameIntf =
                        packageName.replace(':', '.') + "." +
                        bindingPackageSuffix + "." +
                        Identifier.CLASS_PROXY_NAME.toIdentifier(className);
                    this.intfClass = Classes.getApplicationClass(classNameIntf);
                }
                catch(ClassNotFoundException e0) {
                    throw new JmiServiceException(
                        new ServiceException(e0)
                    );
                }
            }
 
            // get the constructor of generated instance-level implementation. This is used as default 
            // implementation when no user-defined implementation can be found. Moreover, the default 
            // instance-level objects are passed as parameter to the user-defined instance as delegation 
            // object in the tie approach.
            this.defaultImplConstructor = null;
            try {
                Class defaultImplClass = Classes.getApplicationClass(classNameIntf + "Impl");
                this.defaultImplConstructor = defaultImplClass.getConstructor(
                    new Class[]{
                        Object_1_0.class,
                        RefClass.class
                    }
                );
            } 
            // Get a generic implementation
            catch(ClassNotFoundException e) {
                try {
                    if(!Names.JMI1_PACKAGE_SUFFIX.equals(bindingPackageSuffix)) {
                        throw e;
                    }
                    this.defaultImplConstructor = Jmi1ObjectInvocationHandler.class.getConstructor(
                        new Class[]{
                            Object_1_0.class,
                            RefClass.class
                        }
                    );
                } 
                catch(ClassNotFoundException e0) {
                    throw new JmiServiceException(
                        new ServiceException(e0)
                    );
                }
                catch(NoSuchMethodException e0) {
                    throw new JmiServiceException(
                        new ServiceException(e0)
                    );
                }
            }
            catch(NoSuchMethodException e0) {
                throw new JmiServiceException(
                    new ServiceException(e0)
                );
            }
    
            // Get the user-defined implementation class if present
            this.userImplConstructor = null;
            this.userImplConstructorTie = null;
            RefRootPackage_1 rootPkg = (RefRootPackage_1)this.refOutermostPackage();
            if(rootPkg.refUseOpenMdx1ImplLookup()) {
    
                Model_1_0 model = ((RefRootPackage_1)this.refOutermostPackage()).refModel();
                ModelElement_1_0 classDef = null;
                try {
                    classDef = model.getElement(this.refMofId());
                    SysLog.trace("class definition found for", this.refMofId());
                }
                catch(ServiceException e) {
                    SysLog.trace("class definition not found for", this.refMofId());
                    throw new JmiServiceException(e);
                }
                boolean enforceTie = false;
    
                // test for possible alternate locations of implementation 
                String implPackageName = rootPkg.refImplPackageName(packageName);
                SysLog.trace("configured implementation for package", packageName + "=" + implPackageName);
                while(
                    (classDef != null) &&
                    (implPackageName != null)
                ) {
                    try {
                        SysLog.trace("testing candidate implementation", implPackageName + "." + className + "Impl");
                        Class userImplClass = Classes.getApplicationClass(implPackageName + "." + className + "Impl");
    
                        // Try to find a constructor with signature (<Interface>). In
                        // this case the user-defined class implements the tie approach.              
                        try {
                            this.userImplConstructorTie = userImplClass.getConstructor(
                                new Class[]{
                                    this.intfClass
                                }
                            );
                            break;
                        }
    
                        // Try to find a constructor with signature (Object_1_0, RefClass). In this case
                        // the user-defined class extends the generated instance-level class
                        catch(NoSuchMethodException e) {
                            if(enforceTie) {
                                throw new JmiServiceException(
                                    new ServiceException(e)
                                );
                            }
                            try {
                                this.userImplConstructor = userImplClass.getConstructor(
                                    new Class[]{
                                        Object_1_0.class,
                                        RefClass.class
                                    }
                                );
                                break;
                            }
                            catch(NoSuchMethodException e1) {
                                throw new JmiServiceException(
                                    new ServiceException(e1)
                                );
                            }
                        }
                    }
    
                    // no user-defined implementation class found. Try to find an
                    // implementation of a superclass of the requested class. If there
                    // are no superclasses fall back to default implementation.
                    catch(ClassNotFoundException e) {
                        SysLog.trace("class not found", implPackageName + "." + className + "Impl");
                        SysLog.trace("getting alternate candidate classes (superclasses defined in the same model package)");
                        List candidates = new ArrayList();
                        for(
                            Iterator i = classDef.values("supertype").iterator();
                            i.hasNext();
                        ) {
                            try {
                                ModelElement_1_0 candidate = model.getElement(i.next());
                                if(((String)candidate.values("qualifiedName").get(0)).startsWith(packageName)) {
                                    candidates.add(candidate);
                                }
                            }
                            catch(ServiceException e1) {
                                throw new JmiServiceException(e1);
                            }
                        }
                        // no superclass --> no user-defined impl 
                        if(candidates.size() == 0) {
                            SysLog.trace("no candidates found");
                            break;
                        }
                        if(candidates.size() > 1) {
                            throw new JmiServiceException(
                                new ServiceException(
                                    StackedException.DEFAULT_DOMAIN,
                                    StackedException.ASSERTION_FAILURE,
                                    new BasicException.Parameter[]{
                                        new BasicException.Parameter("class", classDef.values("qualifiedName").get(0)),
                                        new BasicException.Parameter("supertypes", candidates)
                                    },
                                    "multiple superclasses found for class within same package. multiple inheritance is not supported. To solve provide an implementation for class."
                                )
                            );
                        }
                        classDef = (ModelElement_1_0)candidates.get(0);
    
                        // get corresponding interface class
                        try {
                            String qualifiedClassName = (String)classDef.values("qualifiedName").get(0);
                            packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(':'));
                            className = (String)classDef.values("name").get(0);
                            this.intfClass = Classes.getApplicationClass(
                                packageName.replace(':', '.') + ".cci." + className
                            );
                            implPackageName = ((RefRootPackage_1)this.refOutermostPackage()).refImplPackageName(packageName);
                        }
                        catch(ClassNotFoundException e1) {
                            throw new JmiServiceException(
                                new ServiceException(e1)
                            );
                        }
                        enforceTie = true;
                    }
                }
            }
        }
    
        // Prepare argument as Object_1_0
        Object_1_0 arg = null;
        if(
            (args != null) &&
            (args.size() != 1)
        ) {
            throw new JmiServiceException(
                new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.ASSERTION_FAILURE,
                    new BasicException.Parameter [] {
                        new BasicException.Parameter("args", args)
                    },
                    "args must either be null or have exactly one element"
                )
            );
        }
        else if(args == null) {
            try {
                arg = this.refObjectFactory().createObject(
                    this.refMofId()
                );
            }
            catch(ServiceException e) {
                throw new JmiServiceException(e);
            }
        }
    
        // When Path get the object from accessor. The marshaller itself calls
        // refCreateInstance with an Object_1_0 as parameter
        else if(args.get(0) instanceof Path) {
            try {
                arg = this.refObjectFactory().getObject(
                    args.get(0)
                );
            }
            catch(ServiceException exception) {
                throw new JmiServiceException(exception);
            }
        }
        else if(args.get(0) instanceof Object_1_0) {
            arg = (Object_1_0)args.get(0);
        }
        else if(args.get(0) instanceof RefObject_1_0) {
            arg = ((RefObject_1_0)args.get(0)).refDelegate();
        }
    
        // Must be instanceof null|Path|Object_1_0|RefObject_1_0
        else {
            throw new JmiServiceException(
                new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.ASSERTION_FAILURE,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("args", args)
                    },
                    "argument must be null or instanceof [Path|Object_1_0|RefObject_1_0]"
                )
            );
        }
    
        /**
         * Create an instance with the help of the constructors in the following order:
         * <ul>
         * <li>userImplConstructor != null --> create an user instance with the standard constructor</li>
         * <li>userImplConstructorTie != null --> create an user instance with the tie constructor</li>
         * <li>defaultImplConstructor != null -> create a default instance with the standard constructor</li>
         * </ul>
         */
        try {
            RefObject_1_0 instance = null;
            if(this.userImplConstructor != null) {
                instance = (RefObject_1_0)this.userImplConstructor.newInstance(
                    new Object[]{
                        arg,
                        refClass
                    }
                );
            } 
            else {
                Object defaultInstance = this.defaultImplConstructor.newInstance(
                    new Object[]{
                        arg,
                        refClass
                    }
                );
                if(defaultInstance instanceof InvocationHandler) {
                    defaultInstance = Proxy.newProxyInstance(
                        this.getClass().getClassLoader(),
                        new Class[]{this.intfClass},
                        (InvocationHandler)defaultInstance
                    );
                }
                instance = (RefObject_1_0)defaultInstance;
                if(this.userImplConstructorTie != null) {
                    RefObject_1_0 tieInstance = instance = (RefObject_1_0)this.userImplConstructorTie.newInstance(
                        new Object[]{
                            instance
                        }
                    );
                    if(!this.intfClass.isInstance(instance)) {
                        instance = (RefObject_1_0)((RefPackage_1_1)this.refPackage).refObject(
                            tieInstance,
                            (RefObject_1_0)defaultInstance
                        );
                    }
                }
            }
            //
            // Instance Lifecycle Listener's openMDX 1 API
            // 
            synchronizationSupport(instance); 
            //
            // Instance Lifecycle Listener's openMDX 1 API
            // 
            if(instance instanceof InstanceCallbackListener) {
                instance.refDelegate().objAddEventListener(
                    null,
                    (InstanceCallbackListener)instance
                );
            }
            //
            // Instance Lifecycle Listener's openMDX 2 API
            // 
            if(
                instance instanceof ClearCallback ||
                instance instanceof StoreCallback ||
                instance instanceof DeleteCallback ||
                instance instanceof LoadCallback
            ) {
                instance.refDelegate().objAddEventListener(
                    null,
                    InstanceCallbackAdapter_1.newInstance(instance)
                );
            }
            //
            // Keep a reference to the newly created instance
            //
            if(args == null){
                ((RefRootPackage_1)this.refPackage.refOutermostPackage()).cache(arg, instance);
            }
            return instance;
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
        catch(InvocationTargetException e) {
            if(e.getTargetException() instanceof Exception) {
                throw new JmiServiceException(
                    new ServiceException(
                        (Exception)e.getTargetException()
                    )
                );
            }
            else {
                throw new JmiServiceException(new ServiceException(e));
            }
        }
        catch(IllegalAccessException e) {
            throw new JmiServiceException(
                new ServiceException(e)
            );
        }
        catch(InstantiationException e) {
            throw new JmiServiceException(
                new ServiceException(e)
            );
        }
    }

    //-------------------------------------------------------------------------
    /**
     * InstanceCallbacks_1_0 Support 
     * 
     * @deprecated
     * 
     * @param instance
     */
    private void synchronizationSupport(
        RefObject_1_0 instance
    ) throws ServiceException{
        if(instance instanceof org.openmdx.compatibility.base.accessor.object.cci.InstanceCallbacks_1_0) {
            instance.refDelegate().objRegisterSynchronization(
                (org.openmdx.compatibility.base.accessor.object.cci.InstanceCallbacks_1_0)instance
            );
        }
    }

  //-------------------------------------------------------------------------
  /**
   * RefClass_1 does not remember all created instances. Therefore this
   * operation is not supported.
   */
  @SuppressWarnings("unchecked")
  public Collection refAllOfType() {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------  
  /**
   * RefClass_1 does not remember all created instances. Therefore this 
   * operation is not supported.
   */
  @SuppressWarnings("unchecked")
  public Collection refAllOfClass() {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------  
  /**
   * The call is delegated to the refImmediatePackage().
   */
  @SuppressWarnings("unchecked")
  public RefStruct refCreateStruct(
    RefObject structType,
    List args
  ) {
    return this.refImmediatePackage().refCreateStruct(
      structType,
      args
    );
  }

  //-------------------------------------------------------------------------
  /**
   * The call is delegated to the refImmediatePackage().
   */
  @SuppressWarnings("unchecked")
public RefStruct refCreateStruct(
    String structName,
    List args
  ) {
    return this.refImmediatePackage().refCreateStruct(
      structName,
      args
    );
  }

  //-------------------------------------------------------------------------
  /**
   * The call is delegated to the refImmediatePackage().
   */
  public RefEnum refGetEnum(
    RefObject enumType,
    String literalName
  ) {
    return this.refImmediatePackage().refGetEnum(
      enumType,
      literalName
    );
  }

  //-------------------------------------------------------------------------
  /**
   * The call is delegated to the refImmediatePackage().
   */
  public RefEnum refGetEnum(
    String enumName,
    String literalName
  ) {
    return this.refImmediatePackage().refGetEnum(
      enumName,
      literalName
    );
  }

  //-------------------------------------------------------------------------
  // RefFeatured interface
  //-------------------------------------------------------------------------

  //-------------------------------------------------------------------------
  /**
   * RefClass_1 does not support class-level features. This operation
   * is not supported.
   */
  public void refSetValue(
    RefObject feature,
    Object value
  ) throws JmiException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * RefClass_1 does not support class-level features. This operation
   * is not supported.
   */
  public void refSetValue(
    String featureName,
    Object value
  ) throws JmiException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * RefClass_1 does not support class-level features. This operation
   * is not supported.
   */
  public void refSetValue(
    String featureName,
    int index,
    Object value
  ) throws JmiException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * RefClass_1 does not support class-level features. This operation
   * is not supported.
   */
  public Object refGetValue(
    RefObject feature
  ) throws JmiException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * RefClass_1 does not support class-level features. This operation
   * is not supported.
   */
  public Object refGetValue(
    RefObject feature,
    int index
  ) throws JmiException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * RefClass_1 does not support class-level features. This operation
   * is not supported.
   */
  public Object refGetValue(
    String featureName
  ) throws JmiException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * RefClass_1 does not support class-level features. This operation
   * is not supported.
   */
  public Object refGetValue(
    String featureName,
    int index
  ) throws JmiException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * RefClass_1 does not support class-level features. This operation
   * is not supported.
   */
  @SuppressWarnings("unchecked")
public Object refInvokeOperation(
    RefObject requestedOperation,
    List args
  ) throws JmiException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * RefClass_1 does not support class-level features. This operation
   * is not supported.
   */
  @SuppressWarnings("unchecked")
public Object refInvokeOperation(
    String operationName,
    List args
  ) throws JmiException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  // RefBaseObject
  //-------------------------------------------------------------------------

  //-------------------------------------------------------------------------
  /**
   * Returns the ModelElement_1_0 of this class.
   */
  public RefObject refMetaObject(
  ) {
    try {
      return new RefMetaObject_1(
        ((RefRootPackage_1)this.refOutermostPackage()).refModel().getElement(
          "org:omg:model1:Class"
        )
      );
    }
    catch(ServiceException e) {
      throw new JmiServiceException(e);
    }
  }

  //-------------------------------------------------------------------------
  public RefPackage refImmediatePackage(
  ) {
    return this.refPackage;
  }

  //-------------------------------------------------------------------------
  public RefPackage refOutermostPackage(
  ) {
    return this.refPackage.refOutermostPackage();
  }

  //-------------------------------------------------------------------------  
  /**
   * Must be implemented by the concrete subclass.
   */
  public String refMofId(
  ) throws JmiException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * RefClass_1 does not supporte class-level features. Therefore verification
   * is always successful.
   */
  @SuppressWarnings("unchecked")
public Collection refVerifyConstraints(
    boolean deepVerify
  ) {
    return new ArrayList();
  }

  //-------------------------------------------------------------------------
  // RefClass_1_1
  //-------------------------------------------------------------------------

  //-------------------------------------------------------------------------
  public Set<String> refFeaturesHavingNoImpl(
  ) {
      return this.featuresHavingNoImpl;
  }
  
  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------

  /**
   * @serial
   */
  private RefPackage_1_0 refPackage = null;
  private transient Constructor<?> defaultImplConstructor = null;
  private transient Constructor<?> userImplConstructor = null;
  private transient Constructor<?> userImplConstructorTie = null;
  private transient Class<?> intfClass = null;
  private final transient Set<String> featuresHavingNoImpl = new HashSet<String>();
}

//--- End of File -----------------------------------------------------------
