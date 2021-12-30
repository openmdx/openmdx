/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: ObjectView_1Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2019, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmdx.audit2.aop1.UnitOfWork_1;
import org.openmdx.audit2.spi.Configuration;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.DelegatingObject_1;
import org.openmdx.base.accessor.spi.DelegatingObject_1TestAccessor;
import org.openmdx.base.aop1.ExtentCapable_1;
import org.openmdx.base.aop1.PlugIn_1_0;
import org.openmdx.base.aop1.Segment_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.state2.aop1.DateState_1;
import org.openmdx.state2.aop1.StateCapable_1;
import org.openmdx.state2.cci.DateStateViews;
import org.openmdx.state2.spi.DateStateViewContext;

/**
 * ObjectView_1 Test
 * 
 * Note:
 * The audit plug-in can't be tested in openMDX/Core
 */
@ExtendWith(MockitoExtension.class)
public class ObjectView_1Test {
    
    @Mock
    private ViewManager_1_0 marshaller;
    
    @Mock
    private DataObject_1_0 dataObject;
    
    @Mock
    private PersistenceManager persistenceManager;
    
    @Mock
    private Configuration configuration;
    
    private ObjectView_1 newTestee() throws ServiceException {
        return new ObjectView_1(marshaller, dataObject) {

            /**
			 * Implements {@code Serializable}
			 */
			private static final long serialVersionUID = -3596351625947356936L;

			@Override
            public void objSetDelegate(
                DataObject_1_0 delegate
            ) throws ServiceException {
                Assertions.fail("Intermediate interceptor availability has changed!");
            }
            
        };
    }
    
    @Test
    public void initializeWithoutPlugIn() throws ServiceException {
        //
        // Arrange
        //
        Mockito.when(marshaller.getPlugIn()).thenReturn(Collections.emptyList());
        ObjectView_1 testee = newTestee();
        //
        // Act
        //
        testee.getDelegate();
        //
        // Assert
        //
        final Interceptor_1 terminalInterceptor = testee.getDelegate();
        Assertions.assertSame(Interceptor_1.class, terminalInterceptor.getClass());
        Assertions.assertSame(testee, terminalInterceptor.self);
        Assertions.assertSame(dataObject, DelegatingObject_1TestAccessor.getDelegate(terminalInterceptor));
    }    

    @Test
    public void initializeWithBasePlugIn() throws ServiceException {
        //
        // Arrange
        //
        final List<PlugIn_1_0> plugIns = Arrays.asList(
            new org.openmdx.base.aop1.PlugIn_1()
        );
        Mockito.when(marshaller.getPlugIn()).thenReturn(plugIns);
        Mockito.when(dataObject.objGetClass()).thenReturn("org:openmdx:base:Segment");
        ObjectView_1 testee = newTestee();
        //
        // Act
        //
        testee.getDelegate();
        //
        // Assert
        //
        final Interceptor_1 baseInterceptor = testee.getDelegate();
        Assertions.assertSame(testee, baseInterceptor.self);
        Assertions.assertSame(Segment_1.class, baseInterceptor.getClass());
        final Interceptor_1 terminalInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(baseInterceptor);
        Assertions.assertSame(Interceptor_1.class, terminalInterceptor.getClass());
        Assertions.assertSame(dataObject, DelegatingObject_1TestAccessor.getDelegate(terminalInterceptor));
    }    
    
    @Test
    public void initializeWithAuditPlugIn() throws ServiceException {
        //
        // Arrange
        //
        final List<PlugIn_1_0> plugIns = Arrays.asList(
            new org.openmdx.audit2.aop1.PlugIn_1(),
            new org.openmdx.base.aop1.PlugIn_1()
        );
        Mockito.when(marshaller.getPlugIn()).thenReturn(plugIns);
        Mockito.when(dataObject.objGetClass()).thenReturn("org:openmdx:base:Segment");
        Mockito.when(dataObject.jdoGetObjectId()).thenReturn(new Path("xri://@openmdx*org.openmdx.audit2/provider/Standard/segment/Standard"));
        Mockito.when(dataObject.jdoIsPersistent()).thenReturn(true);
        Mockito.when(dataObject.jdoGetPersistenceManager()).thenReturn(persistenceManager);
        Mockito.when(persistenceManager.getUserObject(Configuration.class)).thenReturn(configuration);
        Mockito.when(configuration.getAuditSegmentId(marshaller)).thenReturn(new Path("xri://@openmdx*org.openmdx.audit2/provider/Audit/segment/Standard"));
        ObjectView_1 testee = newTestee();
        //
        // Act
        //
        testee.getDelegate();
        //
        // Assert
        //
        final Interceptor_1 baseInterceptor = testee.getDelegate();
        Assertions.assertSame(Segment_1.class, baseInterceptor.getClass());
        Assertions.assertSame(testee, baseInterceptor.self);
        final Interceptor_1 terminalInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(baseInterceptor);
        Assertions.assertSame(Interceptor_1.class, terminalInterceptor.getClass());
        Assertions.assertSame(dataObject, DelegatingObject_1TestAccessor.getDelegate(terminalInterceptor));
    }    

    @Test
    public void initializeForUnitOfWork() throws ServiceException {
        //
        // ArrangeAudit
        //
        final List<PlugIn_1_0> plugIns = Arrays.asList(
            new org.openmdx.audit2.aop1.PlugIn_1(),
            new org.openmdx.base.aop1.PlugIn_1()
        );
        Mockito.when(marshaller.getPlugIn()).thenReturn(plugIns);
        Mockito.when(dataObject.objGetClass()).thenReturn("org:openmdx:audit2:UnitOfWork");
        Mockito.when(dataObject.jdoGetObjectId()).thenReturn(new Path("xri://@openmdx*org.openmdx.audit2/provider/Audit/segment/Standard/unitOfWork/4711"));
        Mockito.when(dataObject.jdoIsPersistent()).thenReturn(true);
        Mockito.when(dataObject.jdoGetPersistenceManager()).thenReturn(persistenceManager);
        Mockito.when(persistenceManager.getUserObject(Configuration.class)).thenReturn(configuration);
        Mockito.when(configuration.getAuditSegmentId(marshaller)).thenReturn(new Path("xri://@openmdx*org.openmdx.audit2/provider/Audit/segment/Standard"));
        ObjectView_1 testee = newTestee();
        //
        // Act
        //
        testee.getDelegate();
        //
        // Assert
        //
        final Interceptor_1 auditInterceptor = testee.getDelegate();
        Assertions.assertSame(UnitOfWork_1.class, auditInterceptor.getClass());
        Assertions.assertSame(testee, auditInterceptor.self);
        final Interceptor_1 terminalInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(auditInterceptor);
        Assertions.assertSame(Interceptor_1.class, terminalInterceptor.getClass());
        Assertions.assertSame(dataObject, DelegatingObject_1TestAccessor.getDelegate(terminalInterceptor));
    }    
    
    @Test
    public void initializeWithStatePlugIn() throws ServiceException {
        //
        // Arrange
        //
        final List<PlugIn_1_0> plugIns = Arrays.asList(
            new org.openmdx.state2.aop1.PlugIn_1(),
            new org.openmdx.base.aop1.PlugIn_1()
        );
        Mockito.when(marshaller.getPlugIn()).thenReturn(plugIns);
        Mockito.when(dataObject.objGetClass()).thenReturn("org:openmdx:base:Segment");
        ObjectView_1 testee = newTestee();
        //
        // Act
        //
        testee.getDelegate();
        //
        // Assert
        //
        final Interceptor_1 baseInterceptor = testee.getDelegate();
        Assertions.assertSame(Segment_1.class, baseInterceptor.getClass());
        Assertions.assertSame(testee, baseInterceptor.self);
        final Interceptor_1 stateSegmentInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(baseInterceptor);
        Assertions.assertTrue(stateSegmentInterceptor instanceof org.openmdx.base.aop1.AbstractSegment_1);
        final Interceptor_1 stateObjectInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(stateSegmentInterceptor);
        Assertions.assertTrue(stateObjectInterceptor instanceof org.openmdx.state2.aop1.Object_1);
        final Interceptor_1 terminalInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(stateObjectInterceptor);
        Assertions.assertSame(Interceptor_1.class, terminalInterceptor.getClass());
        Assertions.assertSame(dataObject, DelegatingObject_1TestAccessor.getDelegate(terminalInterceptor));
    }    

    @Test
    public void initializeForStateCapable() throws ServiceException {
        //
        // Arrange
        //
        final List<PlugIn_1_0> plugIns = Arrays.asList(
            new org.openmdx.state2.aop1.LegacyPlugIn_1(),
            new org.openmdx.base.aop1.PlugIn_1()
        );
        Mockito.when(marshaller.getPlugIn()).thenReturn(plugIns);
        Mockito.when(dataObject.objGetClass()).thenReturn("org:openmdx:state2:StateCapable"); // For JUnit test only - it's abstract!
        ObjectView_1 testee = newTestee();
        //
        // Act
        //
        testee.getDelegate();
        //
        // Assert
        //
        final Interceptor_1 baseInterceptor = testee.getDelegate();
        Assertions.assertSame(ExtentCapable_1.class, baseInterceptor.getClass());
        Assertions.assertSame(testee, baseInterceptor.self);
        final Interceptor_1 stateCapableInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(baseInterceptor);
        Assertions.assertSame(StateCapable_1.class, stateCapableInterceptor.getClass());
        final Interceptor_1 stateObjectInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(stateCapableInterceptor);
        final Interceptor_1 terminalInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(stateObjectInterceptor);
        Assertions.assertSame(Interceptor_1.class, terminalInterceptor.getClass());
        Assertions.assertSame(dataObject, DelegatingObject_1TestAccessor.getDelegate(terminalInterceptor));
    }    

    @Test
    public void initializeForDateState() throws ServiceException {
        //
        // Arrange
        //
        final List<PlugIn_1_0> plugIns = Arrays.asList(
            new org.openmdx.state2.aop1.LegacyPlugIn_1(),
            new org.openmdx.base.aop1.PlugIn_1()
        );
        Mockito.when(marshaller.getPlugIn()).thenReturn(plugIns);
        Mockito.when(dataObject.objGetClass()).thenReturn("org:openmdx:state2:StateCapable"); // For JUnit test only - it's abstract!
        Mockito.when(marshaller.getInteractionSpec()).thenReturn(DateStateViewContext.newTimePointViewContext(DateStateViews.today(), null));
        ObjectView_1 testee = newTestee();
        //
        // Act
        //
        testee.getDelegate();
        //
        // Assert
        //
        final Interceptor_1 baseInterceptor = testee.getDelegate();
        Assertions.assertSame(ExtentCapable_1.class, baseInterceptor.getClass());
        Assertions.assertSame(testee, baseInterceptor.self);
        final Interceptor_1 dateStateInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(baseInterceptor);
        Assertions.assertSame(DateState_1.class, dateStateInterceptor.getClass());
        final Interceptor_1 stateObjectInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(dateStateInterceptor);
        Assertions.assertTrue(stateObjectInterceptor instanceof org.openmdx.state2.aop1.Object_1);
        final Interceptor_1 terminalInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(stateObjectInterceptor);
        Assertions.assertSame(Interceptor_1.class, terminalInterceptor.getClass());
        Assertions.assertSame(dataObject, DelegatingObject_1TestAccessor.getDelegate(terminalInterceptor));
    }    
    
    @Test
    public void initializeForLegacy() throws ServiceException {
        //
        // Arrange
        //
        final List<PlugIn_1_0> plugIns = Arrays.asList(
            new org.openmdx.state2.aop1.PlugIn_1(),
            new org.openmdx.base.aop1.PlugIn_1()
        );
        Mockito.when(marshaller.getPlugIn()).thenReturn(plugIns);
        Mockito.when(dataObject.objGetClass()).thenReturn("org:openmdx:state2:Legacy"); // For JUnit test only - it's abstract!
        ObjectView_1 testee = newTestee();
        //
        // Act
        //
        testee.getDelegate();
        //
        // Assert
        //
        final Interceptor_1 baseInterceptor = testee.getDelegate();
        Assertions.assertSame(ExtentCapable_1.class, baseInterceptor.getClass());
        Assertions.assertSame(testee, baseInterceptor.self);
        final Interceptor_1 stateSCapableInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(baseInterceptor);
        Assertions.assertSame(StateCapable_1.class, stateSCapableInterceptor.getClass());
        final Interceptor_1 stateObjectInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(stateSCapableInterceptor);
        Assertions.assertTrue(stateObjectInterceptor instanceof org.openmdx.state2.aop1.Object_1);
        final Interceptor_1 terminalInterceptor = DelegatingObject_1TestAccessor.getDelegateAsInterceptor(stateObjectInterceptor);
        Assertions.assertSame(Interceptor_1.class, terminalInterceptor.getClass());
        Assertions.assertSame(dataObject, DelegatingObject_1TestAccessor.getDelegate((DelegatingObject_1) terminalInterceptor));
    }    

}
