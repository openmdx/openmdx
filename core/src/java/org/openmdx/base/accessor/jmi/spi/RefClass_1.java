/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefClass_1.java,v 1.86 2011/04/12 12:12:48 hburger Exp $
 * Description: RefClass_1 class
 * Revision:    $Revision: 1.86 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/04/12 12:12:48 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.spi;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefEnum;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;

import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.jdo.listener.ConstructCallback;
import org.openmdx.kernel.exception.BasicException;

//---------------------------------------------------------------------------
/**
 * Standard implementation of RefClass_1_0. The implementation does not
 * support class-level features.
 */
@SuppressWarnings("rawtypes")
public class RefClass_1 implements Jmi1Class_1_0, Serializable {

    /**
     * Constructor 
     *
     * @param refPackage
     * @throws ServiceException 
     */
    public RefClass_1(
        String qualifiedClassName,
        RefPackage_1_0 refPackage
    ) throws ServiceException {
        this.immediatePackage = (Jmi1Package_1_0) refPackage;
        this.mapping = this.immediatePackage.refMapping().getClassMapping(qualifiedClassName);
        this.qualifiedClassName = qualifiedClassName;
    }


    //-------------------------------------------------------------------------
    // RefClass
    //-------------------------------------------------------------------------

    /**
     * Asserts that the associated persistence manager is open
     */
    public void assertOpen(
    ) throws JmiException {
        if(this.immediatePackage.refPersistenceManager().isClosed()) {
            throw new JmiServiceException(
                null,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "The persistence manager is closed"
            );
        }
    }
    
    //-------------------------------------------------------------------------
//  @Override
    public String refMofId(
    ) throws JmiException {
        return this.qualifiedClassName;
    }

    //-------------------------------------------------------------------------
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
    RefObject refCreateInstance(
        List<?> args,
        Jmi1Class_1_0 refClass
    ) {
        try {
            boolean isTerminal = this.isTerminal();            
            // Prepare argument as ObjectView_1_0
            PersistenceCapable delegateInstance;
            boolean underConstruction = args == null;
            if(underConstruction) {
                delegateInstance = isTerminal ?
                    ((DataObjectManager_1_0)this.immediatePackage.refDelegate()).newInstance(this.refMofId(), null) :
                    (PersistenceCapable)this.immediatePackage.refDelegate().newInstance(getDelegateClass());
            } 
            else if(args.size() == 1){
                Object argument = args.get(0);
                if(argument instanceof Path) {
                    Path xri = (Path) argument;
                    delegateInstance = (PersistenceCapable)(
                        !xri.isTransientObjectId() ? (PersistenceCapable)this.immediatePackage.refDelegate().getObjectById(xri) :
                        isTerminal ? ((DataObjectManager_1_0)this.immediatePackage.refDelegate()).newInstance(refMofId(), xri.toUUID()) :
                        ((RefObject)this.immediatePackage.refDelegate().getObjectById(BASE)).refOutermostPackage().refClass(refMofId()).refCreateInstance(Collections.singletonList(xri))
                    );
                } else if(isTerminal) {
                    if(argument instanceof ObjectView_1_0) {
                        delegateInstance = (ObjectView_1_0)argument;
                    } 
                    else if(argument instanceof RefObject_1_0) {
                        delegateInstance = ((RefObject_1_0)argument).refDelegate();
                    } 
                    else if(argument instanceof PersistenceCapable) {
                        delegateInstance = (ObjectView_1_0)this.immediatePackage.refDelegate().getObjectById(
                            ((PersistenceCapable)argument).jdoGetObjectId()
                        );
                    } 
                    else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            "JMI 1 compliant initialization with a list of arguments is not supported at the moment",
                            new BasicException.Parameter(
                                "args", 
                                args
                            ),
                            new BasicException.Parameter(
                                "supported", 
                                Arrays.asList(
                                    "<null>",
                                    Path.class.getName(),
                                    ObjectView_1_0.class.getName(),
                                    RefObject_1_0.class.getName(),
                                    PersistenceCapable.class.getName()
                                )
                            )
                        );
                    }
                } 
                else {
                    if (argument instanceof PersistenceCapable) {
                        delegateInstance = (PersistenceCapable)argument;
                    } 
                    else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            "JMI 1 compliant initialization with a list of arguments is not supported at the moment",
                            new BasicException.Parameter(
                                "args", 
                                args
                            ),
                            new BasicException.Parameter(
                                "supported", 
                                Arrays.asList(
                                    "<null>",
                                    Path.class.getName(),
                                    PersistenceCapable.class.getName()
                                )
                            )
                        );
                    }
                }
            } 
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "JMI 1 compliant initialization with a list of arguments is not supported at the moment",
                     new BasicException.Parameter("args", args)
                );
            }
            RefObject_1_0 instance = this.mapping.newInstance(
                refClass, 
                delegateInstance
            );
            if(underConstruction) {
                refOutermostPackage().register(delegateInstance, instance);
                if(instance instanceof ConstructCallback) {
                    ((ConstructCallback)instance).openmdxjdoPostConstruct();
                }
            }
            return instance;
        } 
        catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not remember all created instances. Therefore this
     * operation is not supported.
     */
    public Collection refAllOfType() {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------  
    /**
     * RefClass_1 does not remember all created instances. Therefore this 
     * operation is not supported.
     */
    public Collection refAllOfClass() {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------  
    /**
     * The call is delegated to the refImmediatePackage().
     */
    public final RefStruct refCreateStruct(
        RefObject structType,
        List args
    ) {
        return this.immediatePackage.refCreateStruct(
            structType,
            args
        );
    }

    //-------------------------------------------------------------------------
    /**
     * The call is delegated to the refImmediatePackage().
     */
    public final RefStruct refCreateStruct(
        String structName,
        List args
    ) {
        return this.immediatePackage.refCreateStruct(
            structName,
            args
        );
    }

    //-------------------------------------------------------------------------
    /**
     * The call is delegated to the refImmediatePackage().
     */
//  @Override
    public final RefEnum refGetEnum(
    RefObject enumType,
        String literalName
    ) {
        return this.immediatePackage.refGetEnum(
            enumType,
            literalName
        );
    }

    //-------------------------------------------------------------------------
    /**
     * The call is delegated to the refImmediatePackage().
     */
//  @Override
    public final RefEnum refGetEnum(
        String enumName,
        String literalName
    ) {
        return this.immediatePackage.refGetEnum(
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
//  @Override
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
//  @Override
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
//  @Override
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
//  @Override
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
//  @Override
    public RefObject refMetaObject(
    ) {
        try {
            return new RefMetaObject_1(
                this.immediatePackage.refModel().getElement(
                    "org:omg:model1:Class"
                )
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
//  @Override
    public final RefPackage refImmediatePackage(
    ) {
        return this.immediatePackage;
    }

    //-------------------------------------------------------------------------
//  @Override
    public final RefRootPackage_1 refOutermostPackage(
    ) {
        return (RefRootPackage_1) this.immediatePackage.refOutermostPackage();
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not support class-level features. Therefore verification
     * is always successful.
     */
    public Collection refVerifyConstraints(
        boolean deepVerify
    ) {
        return Collections.EMPTY_SET;
    }

    /**
     * Convert a model class name to the corresponding delegate class
     * 
     * @return the corresponding delegate class
     * 
     * @throws JDOFatalUserException 
     */
//  @Override
    public Class<?> getDelegateClass (
    ) {
        return this.mapping.getInstanceInterface();
    }

    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.Jmi1Class#hasLegacyDelegate()
     */
//  @Override
    public boolean isTerminal(
    ) {
        return this.immediatePackage.isTerminal();
    }

    //------------------------------------------------------------------------
//  @Override
    public StandardMarshaller getMarshaller(
    ) {
    	return this.refOutermostPackage().standardMarshaller;
    }

    //-------------------------------------------------------------------------
    // Extends Object
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return 
            obj instanceof RefClass_1 && 
            this.qualifiedClassName.equals(((RefClass_1)obj).qualifiedClassName);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.qualifiedClassName.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RefClass " + this.qualifiedClassName;
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final Path BASE = new Path("xri://@openmdx*org.openmdx.base");
    
    /**
     * Imlements <code>Serializable</code>
     */
    private static final long serialVersionUID = -9097966508977573575L;

    /**
     * @serial
     */
    private final String qualifiedClassName;
        
    /**
     * @serial
     */
    private final Jmi1Package_1_0 immediatePackage;

    /**
     * @serial
     */
    private final ClassMapping_1_0 mapping;
    
}

