/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: StandardRefObject_1.java,v 1.1 2009/03/03 17:23:07 hburger Exp $
 * Description: Standard RefObject Implementation 1
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/03 17:23:07 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2009, OMEX AG, Switzerland
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
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.spi.Jmi1ObjectInvocationHandler.DelegatingRefObject;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.kernel.exception.BasicException;

/**
 * Standard RefObject Implementation 1
 */
class StandardRefObject_1 
    implements RefObject_1_0, DelegatingRefObject, org.openmdx.base.persistence.spi.Cloneable<RefObject>, Serializable {

    /**
     * Constructor 
     *
     * @param delegate
     * @param refClass
     */
    StandardRefObject_1(
        Object delegate,
        RefClass refClass
    ){
        this.cciDelegate = delegate;
        this.refClass = refClass;
    }

    /**
     * Tells whether the MOF id is in XRI1 or XRI2 format
     */
    private static final boolean XRI2_MOF_ID = true; 

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 8761679181457452801L;
    
    private Object cciDelegate;
    private final RefClass refClass;        
    private RefObject metaObject = null;
    
    private final static String REFLECTIVE = 
        "This reflective method should be dispatched by the invocation " +
        "handler to its non-reflective counterpart"; 

    private final static String STANDARD = 
        "This JMI method is not supported by CCI delegates"; 

    private RefFeatured refObject (
        Path objectId
    ){
        return ((RefRootPackage_1)this.refClass.refOutermostPackage()).refObject(objectId);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refGetPath()
     */
    public Path refGetPath(
    ) {
        return (Path) JDOHelper.getObjectId(this.cciDelegate);
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefObject#refClass()
     */
    public RefClass refClass() {
        return this.refClass;
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefObject#refDelete()
     */
    public void refDelete() {
        JDOHelper.getPersistenceManager(
            this.cciDelegate
        ).deletePersistent(
            this.cciDelegate
        );
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefObject#refImmediateComposite()
     */
    public RefFeatured refImmediateComposite() {
        Path objectId = refGetPath();
        if(objectId == null) {
            return null;
        } 
        else {
            int s = objectId.size();
            return s == 1 ? this : refObject(objectId.getPrefix(s - 2));
        }
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefObject#refIsInstanceOf(javax.jmi.reflect.RefObject, boolean)
     */
    public boolean refIsInstanceOf(
        RefObject objType,
        boolean considerSubtypes
    ) {
        try {
            Model_1_0 model = ((RefPackage_1_0)this.refClass.refOutermostPackage()).refModel();
            if (model.isClassType(objType)) {
                return model.isSubtypeOf(this.refClass.refMofId(), objType.refMofId());
            } 
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "objType must be a class type",
                    new BasicException.Parameter("objType.refClass", objType.refClass().refMofId())
                );
            }
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }  
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefObject#refOutermostComposite()
     */
    public RefFeatured refOutermostComposite() {
        Path objectId = refGetPath();
        if(objectId == null) {
            return null;
        } 
        else {
            int s = objectId.size();
            return s == 1 ? this : refObject(objectId.getPrefix(1));
        }
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefFeatured#refGetValue(javax.jmi.reflect.RefObject)
     */
    public Object refGetValue(
        RefObject feature
    ) {
        throw newUnsupportedOperationException(REFLECTIVE, feature);
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefFeatured#refGetValue(java.lang.String)
     */
    public Object refGetValue(
        String featureName
    ) {
        throw newUnsupportedOperationException(REFLECTIVE, featureName);
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefFeatured#refInvokeOperation(javax.jmi.reflect.RefObject, java.util.List)
     */
    @SuppressWarnings("unchecked")
    public Object refInvokeOperation(
        RefObject requestedOperation, 
        List args
    ) throws RefException {
        throw newUnsupportedOperationException(REFLECTIVE, requestedOperation);
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefFeatured#refInvokeOperation(java.lang.String, java.util.List)
     */
    @SuppressWarnings("unchecked")
    public Object refInvokeOperation(
        String requestedOperation, 
        List args
    ) throws RefException {
        throw newUnsupportedOperationException(REFLECTIVE, requestedOperation);
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefFeatured#refSetValue(javax.jmi.reflect.RefObject, java.lang.Object)
     */
    public void refSetValue(
        RefObject feature, 
        Object value
    ) {
        throw newUnsupportedOperationException(REFLECTIVE, feature);
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefFeatured#refSetValue(java.lang.String, java.lang.Object)
     */
    public void refSetValue(
        String featureName, 
        Object value
    ) {
        throw newUnsupportedOperationException(REFLECTIVE, featureName);
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refImmediatePackage()
     */
    public RefPackage refImmediatePackage() {
        return this.refClass.refImmediatePackage();
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMetaObject()
     */
    public RefObject refMetaObject(
    ) {
        if (this.metaObject == null) try {
            this.metaObject = new RefMetaObject_1(
                ((Jmi1Package_1_0)refClass.refOutermostPackage()).refModel().getElement(this.refClass().refMofId())
            );
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        return this.metaObject;
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMofId()
     */
    public String refMofId(
    ) {
        Path objectId = refGetPath();
        return 
            objectId == null ? null : 
            XRI2_MOF_ID ? objectId.toResourceIdentifier() :
            objectId.toXri();
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refOutermostPackage()
     */
    public RefPackage refOutermostPackage(
    ) {
        return this.refClass.refOutermostPackage();
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refVerifyConstraints(boolean)
     */
    @SuppressWarnings("unchecked")
    public Collection refVerifyConstraints(
        boolean deepVerify
    ) {
        if(this.cciDelegate instanceof RefObject) {
            return ((RefObject)this.cciDelegate).refVerifyConstraints(deepVerify);
        } 
        else {
            throw newUnsupportedOperationException(
                STANDARD, "refVerifyConstraints"
            );
        }
    }

    /* (non-Javadoc)
     * org.openmdx.base.persistence.spi.Entity_2_0#openmdxjdoGetDelegate()
     */
    public Object openmdxjdoGetDelegate(
    ) {
        return this.cciDelegate;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Entity_2_0#openmdxjdoGetDataObject()
     */
    public Object openmdxjdoGetDataObject(
    ) {
        return this.cciDelegate instanceof DelegatingRefObject ?
            ((DelegatingRefObject)this.cciDelegate).openmdxjdoGetDataObject() :
            this.cciDelegate;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Entity_2_0#openmdxjdoSetDelegate(java.lang.Object)
     */
    public void openmdxjdoSetDelegate(
        Object delegate
    ) {
        this.cciDelegate = delegate;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refAddEventListener(java.lang.String, java.util.EventListener)
     */
    public void refAddEventListener(
        String feature, 
        EventListener listener
    ) throws ServiceException {
        if(this.cciDelegate instanceof RefObject_1_0) {
            ((RefObject_1_0)this.cciDelegate).refAddEventListener(
                feature,
                listener
            );
        } 
        else {
            throw newUnsupportedOperationException(
                STANDARD, "refAddEventListener"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refAddToUnitOfWork()
     */
    public void refAddToUnitOfWork(
    ) {
        JDOHelper.getPersistenceManager(
            this.cciDelegate
        ).makeTransactional(
            this.cciDelegate
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refAddValue(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public void refAddValue(
        String featureName,
        Object qualifier,
        Object value
    ) {
        throw newUnsupportedOperationException(REFLECTIVE, featureName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refContext()
     */
    public Object refContext() {
        return ((Jmi1Package_1_0) this.refOutermostPackage()).refUserContext();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refDefaultFetchGroup()
     */
    public Set<String> refDefaultFetchGroup(
    ) {
        if(this.cciDelegate instanceof RefObject_1_0) {
            return ((RefObject_1_0)this.cciDelegate).refDefaultFetchGroup();
        } 
        else {
            throw newUnsupportedOperationException(
                STANDARD,
                "refDefaultFetchGroup"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refDelegate()
     */
    public ObjectView_1_0 refDelegate(
    ) {
        throw newUnsupportedOperationException(
            STANDARD,
            "refDelegate"
        );        
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refGetEventListeners(java.lang.String, java.lang.Class)
     */
    public EventListener[] refGetEventListeners(
        String feature,
        Class<? extends EventListener> listenerType
    ) throws ServiceException {
        if(this.cciDelegate instanceof RefObject_1_0) {
            return ((RefObject_1_0)this.cciDelegate).refGetEventListeners(
                feature,
                listenerType
            );
        } 
        else {
            throw newUnsupportedOperationException(
                STANDARD,
                "refGetEventListeners"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refGetValue(javax.jmi.reflect.RefObject, java.lang.Object, boolean)
     */
    public Object refGetValue(
        RefObject feature,
        Object qualifier,
        boolean marshal
    ) {
        throw newUnsupportedOperationException(REFLECTIVE, feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refGetValue(java.lang.String, java.lang.Object, long)
     */
    public long refGetValue(
        String feature, 
        Object value, 
        long position
    ) {
        throw newUnsupportedOperationException(REFLECTIVE, feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refInitialize(javax.jmi.reflect.RefObject)
     */
    public void refInitialize(
        RefObject source
    ) {
        if(this.cciDelegate instanceof RefObject_1_0) {
            ((RefObject_1_0)this.cciDelegate).refInitialize(
                source
            );
        } 
        else {
            throw newUnsupportedOperationException(
                STANDARD,
                "refInitialize"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refInitialize(boolean, boolean)
     */
    public void refInitialize(
        boolean setRequiredToNull,
        boolean setOptionalToNull
    ) {
        if(this.cciDelegate instanceof RefObject_1_0) {
            ((RefObject_1_0)this.cciDelegate).refInitialize(
                setRequiredToNull,
                setOptionalToNull
            );
        } 
        else {
            throw newUnsupportedOperationException(
                STANDARD,
                "refInitialize"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refIsDeleted()
     */
    public boolean refIsDeleted(
    ) {
        return JDOHelper.isDeleted(this.cciDelegate);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refIsDirty()
     */
    public boolean refIsDirty(
    ) {
        return JDOHelper.isDirty(this.cciDelegate);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refIsNew()
     */
    public boolean refIsNew(
    ) {
        return JDOHelper.isNew(this.cciDelegate);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refIsPersistent()
     */
    public boolean refIsPersistent(
    ) {
        return JDOHelper.isPersistent(this.cciDelegate);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refIsWriteProtected()
     */
    public boolean refIsWriteProtected(
    ) {
        if(this.cciDelegate instanceof RefObject_1_0) {
            return ((RefObject_1_0)this.cciDelegate).refIsWriteProtected(
            );
        } 
        else {
            throw newUnsupportedOperationException(
                STANDARD,
                "refIsWriteProtected"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRefresh()
     */
    public void refRefresh(
    ) {
        JDOHelper.getPersistenceManager(
            this.cciDelegate
        ).refresh(
            this.cciDelegate
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRemoveEventListener(java.lang.String, java.util.EventListener)
     */
    public void refRemoveEventListener(
        String feature,
        EventListener listener
    ) throws ServiceException {
        if(this.cciDelegate instanceof RefObject_1_0) {
            ((RefObject_1_0)this.cciDelegate).refRemoveEventListener(
                feature,
                listener
            );
        } 
        else {
            throw newUnsupportedOperationException(
                STANDARD,
                "refRemoveEventListener"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRemoveFromUnitOfWork()
     */
    public void refRemoveFromUnitOfWork(
    ) {
        JDOHelper.getPersistenceManager(
            this.cciDelegate
        ).makeNontransactional(
            this.cciDelegate
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRemoveValue(java.lang.String, java.lang.Object)
     */
    public void refRemoveValue(
        String featureName, 
        Object qualifier
    ) {
        throw newUnsupportedOperationException(REFLECTIVE, featureName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRemoveValue(java.lang.String, javax.jmi.reflect.RefObject)
     */
    public void refRemoveValue(
        String featureName, 
        RefObject value
    ) {
        throw newUnsupportedOperationException(REFLECTIVE, featureName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refSetValue(java.lang.String, java.lang.Object, long)
     */
    public void refSetValue(
        String feature, 
        Object newValue, 
        long length
    ) {
        throw newUnsupportedOperationException(REFLECTIVE, feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refWriteProtect()
     */
    public void refWriteProtect(
    ) {
        if(this.cciDelegate instanceof RefObject_1_0) {
            ((RefObject_1_0)this.cciDelegate).refWriteProtect(
            );
        } 
        else {
            throw newUnsupportedOperationException(
                STANDARD,
                "refWriteProtect"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefObject_2_0#setDelegate(java.lang.Object)
     */
    public void setDelegate(
        Object delegate
    ) {
        this.cciDelegate = delegate;
    }

    private UnsupportedOperationException newUnsupportedOperationException(
        String message,
        String feature
    ){
        return new UnsupportedOperationException(
            newExceptionMessage(
                message + ": Feature " + feature + " in class " + this.refClass.refMofId()
            )
        );
    }

    private UnsupportedOperationException newUnsupportedOperationException(
        String message,
        RefObject feature
    ){
        return new UnsupportedOperationException(
            newExceptionMessage(
                message + ": Feature " + feature.refMofId()
            )
        );
    }

    private String newExceptionMessage(
        String message
    ){
        Object objectId = JDOHelper.getObjectId(this.cciDelegate);
        return objectId == null ?
            message :
            message + " on object " + objectId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.cciDelegate.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.cciDelegate.toString();
    }
    
    //--------------------------------------------------------------------
    // Implements Cloneable
    //--------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Cloneable#openmdxjdoClone()
     */
    public RefObject openmdxjdoClone() {
        return this.refClass.refCreateInstance(
            Collections.singletonList(
                PersistenceHelper.clone(this.cciDelegate)
            )
        );
    }
    
}