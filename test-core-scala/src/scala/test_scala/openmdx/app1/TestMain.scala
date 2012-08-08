/*
 * ====================================================================
 * Name:        $Id: TestMain.scala,v 1.2 2010/11/27 18:29:43 wfro Exp $
 * Description: Unit test for model app1
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/11/27 18:29:43 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package test_scala.openmdx.app1;

import javax.jdo._
import javax.naming._
import org.junit._
import junit.framework._
import org.junit.Assert._
import javax.jdo.spi.PersistenceCapable;

import scala.collection.JavaConversions._

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.ejb.TransactionAttributeType;

import javax.jmi.reflect.InvalidCallException;
import javax.jmi.reflect.InvalidObjectException;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.servlet.ServletException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.rest.spi.EntityManagerProxyFactory_2;
import org.openmdx.application.rest.spi.InboundConnectionFactory_2;
import org.openmdx.application.xml.Exporter;
import org.openmdx.application.xml.Importer;
import org.openmdx.audit2.cci.AuditQueries;
import org.openmdx.audit2.jmi1.Audit2Package;
import org.openmdx.audit2.jmi1.Involvement;
import org.openmdx.audit2.jmi1.UnitOfWork;
import org.openmdx.audit2.spi.Configuration;
import org.openmdx.audit2.spi.InvolvementPersistence;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.spi.DelegatingRefObject_1_0;
import org.openmdx.base.accessor.jmi.spi.EntityManagerFactory_1;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.accessor.rest.spi.Synchronization_2_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.cci2.ExtentCapable;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Modifiable;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.rest.spi.ConnectionFactoryAdapter;
import org.openmdx.generic1.cci2.PropertyQuery;
import org.openmdx.generic1.jmi1.BooleanProperty;
import org.openmdx.generic1.jmi1.DecimalProperty;
import org.openmdx.generic1.jmi1.Generic1Package;
import org.openmdx.generic1.jmi1.IntegerProperty;
import org.openmdx.generic1.jmi1.Property;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.naming.ComponentEnvironment;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.Container;
import org.w3c.cci2.SparseArray;
import org.w3c.cci2.StringTypePredicate;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.StateAccessor;
import org.w3c.spi2.Datatypes;

import test.openmdx.app1.aop2.NaturalPerson;
import test.openmdx.app1.cci2.AddressQuery;
import test.openmdx.app1.cci2.CycleMember1Query;
import test.openmdx.app1.cci2.InvoiceHasInvoicePosition;
import test.openmdx.app1.cci2.InvoicePositionQuery;
import test.openmdx.app1.cci2.InvoiceQuery;
import test.openmdx.app1.cci2.PersonQuery;
import test.openmdx.app1.cci2.SegmentHasAddress;
import test.openmdx.app1.cci2.SegmentHasPerson;
import test.openmdx.app1.jmi1.Address;
import test.openmdx.app1.jmi1.AddressFormat;
import test.openmdx.app1.jmi1.AddressFormatAsResult;
import test.openmdx.app1.jmi1.App1Package;
import test.openmdx.app1.jmi1.CanNotFormatNameException;
import test.openmdx.app1.jmi1.CycleMember1;
import test.openmdx.app1.jmi1.CycleMember1Class;
import test.openmdx.app1.jmi1.CycleMember2;
import test.openmdx.app1.jmi1.CycleMember2Class;
import test.openmdx.app1.jmi1.Document;
import test.openmdx.app1.jmi1.DocumentClass;
import test.openmdx.app1.jmi1.EmailAddress;
import test.openmdx.app1.jmi1.EmailAddressClass;
import test.openmdx.app1.jmi1.EmailAddressSendMessageTemplateResult;
import test.openmdx.app1.jmi1.InternationalPostalAddress;
import test.openmdx.app1.jmi1.InternationalPostalAddressClass;
import test.openmdx.app1.jmi1.Invoice;
import test.openmdx.app1.jmi1.InvoiceClass;
import test.openmdx.app1.jmi1.InvoicePosition;
import test.openmdx.app1.jmi1.InvoicePositionClass;
import test.openmdx.app1.jmi1.MessageTemplate;
import test.openmdx.app1.jmi1.MessageTemplateClass;
import test.openmdx.app1.jmi1.NameFormat;
import test.openmdx.app1.jmi1.Person;
import test.openmdx.app1.jmi1.PersonClass;
import test.openmdx.app1.jmi1.PersonDateOpResult;
import test.openmdx.app1.jmi1.PersonFormatNameAsResult;
import test.openmdx.app1.jmi1.PersonGroup;
import test.openmdx.app1.jmi1.PersonGroupClass;
import test.openmdx.app1.jmi1.PostalAddress;
import test.openmdx.app1.jmi1.Product;
import test.openmdx.application.rest.http.ServletPort;

/**
 * Test Main
 */
//---------------------------------------------------------------------------
class ReadModels extends Runnable {

    def run(
    ) {
        try {
            var model = Model_1Factory.getModel();
            var i: Int = 0;
            while(i < 5000000) {
                model.getElement("org:openmdx:base:BasicObject");
                i += 1;
            }
        }
        catch {
        	case e: ServiceException =>
                System.out.println("ReadModels catched Exception. Terminating");
                System.out.println(e);
        }
        System.out.println("ReadModels terminated");
    }
}

//------------------------------------------------------------------------
// Class AbstractTest
//------------------------------------------------------------------------

/**
 * Abstract Test
 */
class AbstractTest {

    this.taskIdentifier = new Object(){

        override def toString(): String = {
            return AbstractTest.this.taskId;
        }

    };

    var entityManager: PersistenceManager = null;
    var authority: Authority = null;
    var provider: Provider = null;
    var taskId: String = null;
    var taskIdentifier: Object = null;
    val start: Date = new Date();

    @Before
    def setUp() = {
        TestMain.entityManagerFactory.getDataStoreCache().pinAll(true, classOf[AddressFormat]);
        this.entityManager = TestMain.entityManagerFactory.getPersistenceManager();
        this.authority = this.entityManager.getObjectById(
            classOf[Authority],
            App1Package.AUTHORITY_XRI
        );
        this.provider = authority.getProvider(
            false,
            TestMain.DATA_PROVIDER_NAME
        );
        UserObjects.setTaskIdentifier(
            this.entityManager, 
            this.taskIdentifier
        );
    }

    @After
    def tearDown() = {
        this.entityManager.close();
        this.entityManager = null;
    }

    def begin() = {
        this.entityManager.currentTransaction().begin();
    }

    def commit() = {
        this.entityManager.currentTransaction().commit();
    }

    def rollback() = {
        this.entityManager.currentTransaction().rollback();
    }

    def getStart(): Date = {
        return this.start;
    }
    
}

//------------------------------------------------------------------------
// Class AbstractTests
//------------------------------------------------------------------------

/**
 * Abstract Tests
 */
class RepeatableTest extends AbstractTest {

    val USES_DB_OBJECT_WITH_ID_AS_KEY: Boolean = false;
    val INSPECTION_COUNT: Int = 100;
    val N_PERSONS: Int = 100;
    val LARGE_N_PERSONS: Int = 1000;
    val TEST_PERSON_COUNT: Int = N_PERSONS - 1; // TODO one is missing for some reason
    val SIMILAR_NAME_COUNT: Int = 3; 

    var id:  Long = 0;

    def nextId(): String = {
    	val id = this.id;
    	this.id += 1;
        return "ID" + id;
    }

    def testCR20019014() = {
        val query: PropertyQuery = entityManager.newQuery(
            classOf[Property]
        ).asInstanceOf[PropertyQuery]
        PersistenceHelper.setClasses(
            query,
            classOf[IntegerProperty], classOf[DecimalProperty]
        );
    }
    
    def testCR20018667() = {
        val PERSON_AND_DESCENDANTS: Path = TestMain.DATA_SEGMENT_ID.getDescendant(
            "person",
            "%"
        ).lock();
        {
            //
            // An audit1 query 
            //
            val query: org.openmdx.compatibility.audit1.cci2.UnitOfWorkQuery = entityManager.newQuery(
                classOf[org.openmdx.compatibility.audit1.jmi1.UnitOfWork]
            ).asInstanceOf[org.openmdx.compatibility.audit1.cci2.UnitOfWorkQuery]
            query.thereExistsInvolved().elementOf(
                PersistenceHelper.getCandidates(
                    entityManager.getExtent(classOf[Person]),
                    PERSON_AND_DESCENDANTS
                )
            );
        }
        {
            //
            // ... and its negation
            //
            val query: org.openmdx.compatibility.audit1.cci2.UnitOfWorkQuery = entityManager.newQuery(
                classOf[org.openmdx.compatibility.audit1.jmi1.UnitOfWork]
            ).asInstanceOf[org.openmdx.compatibility.audit1.cci2.UnitOfWorkQuery]
            query.thereExistsInvolved().notAnElementOf(
                PersistenceHelper.getCandidates(
                    entityManager.getExtent(classOf[Person]),
                    PERSON_AND_DESCENDANTS
                )
            );
        }
    }
    
    def testCR20018726(
    ) = {
        try {
            taskId = "CR20018726";
            val segment = provider.getSegment(TestMain.SEGMENT_NAME).asInstanceOf[test.openmdx.app1.jmi1.Segment]
            val addresses: List[Address] = segment.getAddress(null);
            for(address <- addresses) {
                System.out.println("1st display of " + JDOHelper.getObjectId(address));
            }
            for(address <- addresses) {
                System.out.println("2nd display of " + JDOHelper.getObjectId(address));
            }
        } finally {
            taskId = null;
        }
    }

    def testCR20019462(
    ) = {
        try {
            taskId = "CR20019462";
            var invoice = this.entityManager.newInstance(classOf[Invoice]);
            var position = this.entityManager.newInstance(classOf[InvoicePosition]);
            var positions = invoice.getInvoicePosition();
            assertTrue(positions.isEmpty());
            invoice.addInvoicePosition(position);
            assertEquals(1, positions.size());
            positions.remove(position);
            assertTrue(positions.isEmpty());
        } finally {
            taskId = null;
        }
    }

    def testCR20018800(
    ) = {
        try {
            taskId = "CR20018800";
            var original = this.entityManager;
            var originalPrinicpals = UserObjects.getPrincipalChain(original); 
            var sibling = TestMain.entityManagerFactory.getPersistenceManager();
            var siblingPrinicpals = UserObjects.getPrincipalChain(sibling); 
            assertEquals("sibling", originalPrinicpals, siblingPrinicpals);
            var factory = original.getPersistenceManagerFactory();
            var clone = factory.getPersistenceManager();
            var clonePrinicpals = UserObjects.getPrincipalChain(clone); 
            assertEquals("clone", originalPrinicpals, clonePrinicpals);
        } finally {
            taskId = null;
        }
    }

    def testPackageAcquisition(
    ) = {
        var instance = this.entityManager.newInstance(classOf[UnitOfWork]);
        var audit2Package = instance.refImmediatePackage().asInstanceOf[Audit2Package];
        assertEquals("MOF ID", "org:openmdx:audit2:audit2", audit2Package.refMofId());
    }

    def resetAuditSegment(
    ) = {
        val authority: Authority = this.entityManager.getObjectById(
            classOf[Authority],
            Audit2Package.AUTHORITY_XRI
        );
        val provider: Provider = authority.getProvider(
            false,
            TestMain.AUDIT_PROVIDER_NAME
        ).asInstanceOf[Provider]
        var segment: org.openmdx.audit2.jmi1.Segment = provider.getSegment(
            false, 
            TestMain.SEGMENT_NAME
        ).asInstanceOf[org.openmdx.audit2.jmi1.Segment]
        if(segment != null) {
            this.begin();
            segment.refDelete();
            this.commit();
        }
        segment = this.entityManager.newInstance(classOf[org.openmdx.audit2.jmi1.Segment]);
        this.begin();
        provider.addSegment(false, TestMain.SEGMENT_NAME, segment);
        this.commit();
    }

    def resetDataSegment(
    ) = {
        var segment: test.openmdx.app1.jmi1.Segment = provider.getSegment(
            false, 
             TestMain.SEGMENT_NAME
        ).asInstanceOf[test.openmdx.app1.jmi1.Segment]
        if(segment != null) {
            this.begin();
            segment.refDelete();
            this.commit();
        }
        segment = this.entityManager.newInstance(classOf[test.openmdx.app1.jmi1.Segment]);
        this.begin();
        provider.addSegment(false,  TestMain.SEGMENT_NAME, segment);
        this.commit();
    }

    def testMain(
    ) = {
        this.id = 500000l;
        System.out.println("getting root package...");
        //          Authority app1 = (Authority) persistenceManager.getObjectById(new Path(App1Package.AUTHORITY_XRI));
        val app1: Authority = authority;
        val persistenceManager: PersistenceManager = entityManager;
        val rootPkg: RefPackage = app1.refOutermostPackage();
        val app1Package: App1Package = app1.refOutermostPackage().refPackage(app1.refGetPath().getBase()).asInstanceOf[App1Package]

        val generic1Package: Generic1Package = rootPkg.refPackage("org:openmdx:generic1").asInstanceOf[Generic1Package];

        // END test model functions
        // BEGIN test model functions
        val model: Model_1_0 = (rootPkg.asInstanceOf[RefRootPackage_1]).refModel();
        rootPkg.refPackage(
            "test:openmdx:state2"
        );
        val personClass = app1Package.getPerson();
        val postalAddressClass = app1Package.getInternationalPostalAddress();
        val emailAddressClass = app1Package.getEmailAddress();
        val cycleMember1Class = app1Package.getCycleMember1();
        val cycleMember2Class = app1Package.getCycleMember2();
        val messageTemplateClass = app1Package.getMessageTemplate();
        val documentClass = app1Package.getDocument();
        val personGroupClass = app1Package.getPersonGroup();

        // segment
        val provider: Provider = app1.getProvider(false, TestMain.DATA_PROVIDER_NAME);
        val segment: test.openmdx.app1.jmi1.Segment = provider.getSegment(TestMain.SEGMENT_NAME).asInstanceOf[test.openmdx.app1.jmi1.Segment];
        var startedAt: Long = 0;

        //
        // CR20018821
        //
        try {
            taskId = "CR20018821";
            val segments = persistenceManager.getObjectById(
                provider.refGetPath().getChild("segment")
            ).asInstanceOf[RefContainer[_]]
        } finally {
            taskId = null;
        }

        //
        // CR20018821
        //
        try {
            taskId = "CR20018821";
            this.begin();
            Importer.importObjects(
                Importer.asTarget(persistenceManager),
                Importer.asSource(
                    new URL("xri://+resource/test/openmdx/app1/data.xml")
                )
            );
            this.commit();
        } finally {
            taskId = null;
        }

        //
        // Test Invoice
        //
        try {
            taskId = "CR0003551";
            val invoiceClass: InvoiceClass = app1Package.getInvoice();
            val invoicePositionClass: InvoicePositionClass = app1Package.getInvoicePosition();

            val booleanProperty = generic1Package.getBooleanProperty().createBooleanProperty();

            booleanProperty.setDescription("A SparseArray Of Flags");
            booleanProperty.getBooleanValue().put(0, true);

            val invoice: Invoice = invoiceClass.createInvoice();        
            invoice.setDescription("this is an invoice for PG0");
            invoice.setProductGroupId("PG0");
            assertNull("CR0003551",TestMain.refGetPath(invoice));
            val refInvoices: RefContainer[Invoice] = segment.getInvoice().asInstanceOf[RefContainer[Invoice]]
            invoice.addProperty(false, "flag", booleanProperty);

            this.begin();
            refInvoices.refAdd(RefContainer.REASSIGNABLE, nextId(), invoice);
            // this.invoiceId = invoice.refGetPath();
            assertNotNull("CR0003551", TestMain.refGetPath(invoice));
            var i: Int = 0;
            while(i < 10) {
                val invoicePosition: InvoicePosition = invoicePositionClass.createInvoicePosition();
                invoicePosition.setDescription("this is an invoice position for P" + i);
                invoicePosition.setProductId("P" + i);
                assertNull("CR0003551",TestMain.refGetPath(invoicePosition));
                invoice.addInvoicePosition(false, nextId(), invoicePosition);
                assertNotNull("CR0003551",TestMain.refGetPath(invoicePosition));
                i += 1;
            }
            this.commit();
            
            //
            // CRCR20019372
            //
            try {
                this.taskId = "CR20019372";
                this.begin();
                val auditInvoice: Invoice = this.entityManager.newInstance(classOf[Invoice]);
                auditInvoice.setDescription("An invoice for audit tests");
                auditInvoice.setProductGroupId("PG0");
                segment.addInvoice("CR20019372", auditInvoice);
                var i: Int = 0;
                while(i < 5) {
                    val auditPosition: InvoicePosition = this.entityManager.newInstance(classOf[InvoicePosition]);
                    auditPosition.setDescription("An invoice position for audit tests");
                    auditPosition.setProductId("P" + i);
                    auditInvoice.addInvoicePosition("IP" + i, auditPosition);
                    i += 1;
                }
                this.commit();
                this.begin();
                auditInvoice.setDescription("Invoice CR20019372");
                i = 0;
                while(i < 5) {
                    val auditPosition: InvoicePosition = auditInvoice.getInvoicePosition("IP" + i);
                    auditPosition.setDescription("P" + i + ", an invoice position for audit tests");
                    i += 2;
                }
                this.commit();
            } finally {
                taskId = null;
            }
            
//                synchronized(this) {
                try {
                    System.out.println("Wait 5 sec");
                    Thread.`yield`();
                    wait(5000);
                    System.out.println("...done");
                } catch {
                	case exception: InterruptedException => 
                		System.out.println("...interrupted");
                }
//                }

            {
                System.out.println("Invoice instanceof " + invoice.getClass().getInterfaces());
                val flagId: Path = (JDOHelper.getObjectId(invoice).asInstanceOf[Path]).getDescendant(
                    "property",
                    "flag"
                );
                val flag: BooleanProperty = persistenceManager.getObjectById(flagId).asInstanceOf[BooleanProperty]
                assertNotNull("Flag", flag);
                val flags: SparseArray[java.lang.Boolean] = flag.getBooleanValue();
                assertNotNull("flags", flags);
                assertEquals("Flag[0]", true, flags.get(0));
                val m: PersistenceManager = JDOHelper.getPersistenceManager(flag);
                assertSame(
                    "Class with root parent", 
                    persistenceManager, 
                    m
                );
                if(this.isInstanceOf[LocalConnectionTest]) {
                    assertTrue("Implementation detail", flag.isInstanceOf[DelegatingRefObject_1_0]);
                    val entity: DelegatingRefObject_1_0 = flag.asInstanceOf[DelegatingRefObject_1_0];
                    val dataObject: Object = entity.openmdxjdoGetDataObject();
                    assertTrue(dataObject.isInstanceOf[PersistenceCapable]);
                    assertTrue(dataObject.isInstanceOf[RefObject_1_0]);
                    val objectView: ObjectView_1_0 = (dataObject.asInstanceOf[RefObject_1_0]).refDelegate();
                    assertNotNull(objectView);
                    assertNotSame("Made persistent", entity.openmdxjdoGetDelegate(), dataObject);
                }
                if(this.isInstanceOf[ProxyConnectionTest]) {
                    assertFalse("Implementation detail", flag.isInstanceOf[DelegatingRefObject_1_0]);
                }
            }
            {
                //
                // Read via extent
                //
                val xriPattern: String = segment.refGetPath().getDescendant("invoice",":*","invoicePosition","%").toXRI(); 
                val invoicePositionQuery: InvoicePositionQuery = PersistenceHelper.newQuery(
                	entityManager.getExtent(classOf[InvoicePosition]),
                    xriPattern
                ).asInstanceOf[InvoicePositionQuery];
                val invoicePositions: List[InvoicePosition] = segment.getExtent(invoicePositionQuery);
                assertTrue("Invoice Positions: Second Last", invoicePositions.listIterator(14).hasNext());
                assertFalse("Invoice Positions: Last", invoicePositions.listIterator(15).hasNext());
                assertEquals("Invoice Positions: Size", 15,invoicePositions.size());
            }
            {
                //
                // Read via Query
                //
                val xriPattern: Path = segment.refGetPath().getDescendant("invoice",":*","invoicePosition","%");
                val query: Query = PersistenceHelper.newQuery(
                    entityManager.getExtent(classOf[InvoicePosition]),
                    xriPattern
                );
                query.setCandidates(segment.getExtent());                
                val invoicePositions = query.execute().asInstanceOf[List[InvoicePosition]];
                assertTrue("Invoice Positions: Second Last", invoicePositions.listIterator(14).hasNext());
                assertFalse("Invoice Positions: Last", invoicePositions.listIterator(15).hasNext());
                assertEquals("Invoice Positions: Size", 15,invoicePositions.size());
            }
            {
                val invoicePositionQuery: InvoicePositionQuery = app1Package.createInvoicePositionQuery();
                // get products without price. price is an expensive derived
                // atttribute. Therefore this iteration should be much faster
                // than the next one
                var startedAt = System.currentTimeMillis();
                var allInvoicePositions: InvoiceHasInvoicePosition.InvoicePosition[InvoicePosition] = invoice.getInvoicePosition();
                val someInvoicePositions: Collection[InvoicePosition] = if(invoicePositionQuery == null)
                    allInvoicePositions else
                        allInvoicePositions.getAll(invoicePositionQuery);
                for(invoicePosition <- someInvoicePositions) {
                    val product: Product = invoicePosition.getProduct();
                    product.getDescription();
                    //                  System.out.println("product[" + i + "]");
                    //                  System.out.println("  description=" + product.getDescription());
                }
                System.out.println("time for retrieving 10 invoice positions (without price)=" + (System.currentTimeMillis() - startedAt));

                startedAt = System.currentTimeMillis();
                allInvoicePositions = invoice.getInvoicePosition();
                for(invoicePosition <- allInvoicePositions){
                    val product: Product = invoicePosition.getProduct();
                    var description: String = product.getDescription();
                    if(description == null) {
                        description = product.refGetPath().getBase();
                    }
                    System.out.println("Product " + description + " costs " + product.getPrice());
                    //                  System.out.println("product[" + i + "]");
                    //                  System.out.println("  description=" + product.getDescription());
                    //                  System.out.println("  price=" + product.getPrice());
                }
                System.out.println("time for retrieving 10 invoice positions (with price)=" + (System.currentTimeMillis() - startedAt));
            }
            if (USES_DB_OBJECT_WITH_ID_AS_KEY) {
                //
                // Complex query. 
                // Complex queries only work with DbObjectWithIdAsKey
                //
                val invoiceQuery: InvoiceQuery = persistenceManager.newQuery(classOf[Invoice]).asInstanceOf[InvoiceQuery]
                invoiceQuery.thereExistsInvoicePosition().productId().like("P.*");
                invoiceQuery.thereExistsProperty().name().equalTo("flag");
                val matchingInvoices: List[Invoice] = segment.getInvoice(invoiceQuery);
                assertEquals("Complex query", matchingInvoices.size(), 10);
            }
        } finally {
            taskId = null;
        }

        /**
         * Test Address
         */
        // get AddressFormat
        try {
            taskId = "CR20019366";
            val segmentManager: PersistenceManager = JDOHelper.getPersistenceManager(segment).getPersistenceManagerFactory().getPersistenceManager();                
            val addressFormats: Collection[AddressFormat] = (segmentManager.getObjectById(JDOHelper.getObjectId(segment)).asInstanceOf[test.openmdx.app1.jmi1.Segment]).getAddressFormat();
            for(addressFormat <- addressFormats) {
                val formatManager: PersistenceManager = JDOHelper.getPersistenceManager(addressFormat);
                assertSame("cci2.getContainer()", segmentManager, formatManager);
                System.out.println("addressFormat=" + addressFormat);
            }
        } finally {
            taskId = null;
        }

        // get NameFormat
        val nameFormats: Collection[NameFormat] = segment.getNameFormat();
        for(nameFormat <- nameFormats) {
            System.out.println("nameFormat=" + nameFormat);
        }

        // reference nameFormat has multiplicity 1..1. Test for NOT_FOUND exception
        // TODO
        //          try {
        //          segment.getNameFormat(false, "unknown");
        ////        fail("NOT_FOUND expected"); // TODO: make NOT_FOUND exception to be thrown
        //          } catch(JmiServiceException e) {
        //          assertEquals("NOT_FOUND expected", BasicException.Code.NOT_FOUND, e.getExceptionCode());
        //          }
        //          // reference addressFormat has multiplicity 0..1. Test for null
        //          assertNull("addressFormat must be null", segment.getAddressFormat("unknown"));


        // modify feature 
        for(nameFormat <- nameFormats) try {
            this.begin();
            nameFormat.refSetValue(
                "description",
                "modified description"
            );
            this.commit();
            fail("all attributes are non changeable --> object can not be updated");
        } catch {
        	case e: JDOFatalDataStoreException =>
        		System.out.println("all attributes are non changeable --> object can not be updated");
        }

        try {
            val nameFormat: NameFormat = persistenceManager.newInstance(classOf[NameFormat]);
            this.begin();
            nameFormat.setDescription(
                "a description"
            );
            segment.getNameFormat().add(nameFormat);
            this.commit();
            fail("constraint isFrozen --> object can not be updated");
        } catch {
        	case e: JDOFatalDataStoreException => 
            	System.out.println("constraint isFrozen --> object can not be updated");                
        }


        var postalAddress: InternationalPostalAddress = null;
        var emailAddress: EmailAddress = null;
        assertEquals("Initial address count", 0, segment.getAddress().size());
        var i = 0;
        while(i < 4) {
            System.out.println(
                Arrays.asList(
                    "Rollback address addition",
                    "Clear persistent address collection",
                    "Clear transient address collection",
                    "Commit address addition"
                )(i)
            );
            this.begin();
            postalAddress = postalAddressClass.createInternationalPostalAddress();
            postalAddress.setCountry("Switzerland");
            postalAddress.setCity("Zurich");
            postalAddress.setHouseNumber("57");
            postalAddress.setPostalCode("8005");
            postalAddress.setStreet("Bahnhofstr.");
            postalAddress.setAddressLine("Familie", "Muster");
            segment.addAddress(false, "0001", postalAddress);
            val postalAddressId = JDOHelper.getObjectId(postalAddress);
            // create a EmailAddress
            emailAddress = emailAddressClass.createEmailAddress();
            emailAddress.setAddress("hans.muster@app1.ch");
            segment.addAddress(false, "0002", emailAddress);
            assertEquals(
                "Transient added address count",
                2,
                segment.getAddress().size()
            );
            i match {
                case 0 => {
                    this.rollback();
                    assertEquals(
                        "Rolled back address count",
                        0,
                        segment.getAddress().size()
                    );
                };
                case 1 => {
                    this.commit();
                    assertEquals(
                        "Commited address count",
                        2,
                        segment.getAddress().size()
                    );
                    var retrievedAddress = entityManager.getObjectById(postalAddressId).asInstanceOf[InternationalPostalAddress]
                    var clonedAddress: InternationalPostalAddress = PersistenceHelper.clone(retrievedAddress);
                    assertFalse(JDOHelper.isPersistent(clonedAddress));
                    this.entityManager.retrieve(retrievedAddress);
                    this.begin();
                    segment.getAddress().clear();
                    assertEquals(
                        "Transient cleared address count",
                        0,
                        segment.getAddress().size()
                    );
                    this.commit();
                    assertEquals(
                        "Cleared persistent address count",
                        0,
                        segment.getAddress().size()
                    );
                };
                case 2 => {
                    segment.getAddress().clear();
                    assertEquals(
                        "Cleared transient address count",
                        0,
                        segment.getAddress().size()
                    );
                    this.commit();
                    assertEquals(
                        "Cleared committed address count",
                        0,
                        segment.getAddress().size()
                    );
                    assertTrue(
                        "Cleared committed address count",
                        segment.getAddress().isEmpty()
                    );
                };
                case 3 => {
                    var transientAddress: EmailAddress = emailAddressClass.createEmailAddress();
                    transientAddress.setAddress("john.player@games.net");
                    segment.addAddress(false, "0003", transientAddress);
                    assertEquals(
                        "Transient added address count",
                        3,
                        segment.getAddress().size()
                    );
                    transientAddress.refDelete(); // segment.getAddress().remove(transientAddress);
                    this.commit();
                    assertEquals(
                        "Commited address count",
                        2,
                        segment.getAddress().size()
                    );
                };
                case _ => 
                    fail("No more instructions");
            }
            i += 1;
        }
        val a1 =TestMain.refGetPath(segment).getSuffix(
           TestMain.refGetPath(segment).size() - 2
        )
        val a2 = Array("segment", TestMain.SEGMENT_NAME)
        assertTrue(
            "Identity should be available outside the unit of work",
            a1 == a2
        );
        try {
            this.begin();
            var duplicateAddress: InternationalPostalAddress = postalAddressClass.createInternationalPostalAddress();
            duplicateAddress.setCountry("Switzerland");
            duplicateAddress.setCity("Zurich");
            duplicateAddress.setHouseNumber("57");
            duplicateAddress.setPostalCode("8005");
            duplicateAddress.setStreet("Bahnhofstr.");
            duplicateAddress.setAddressLine(Arrays.asList("Familie", "Muster"));
            emailAddress.setAddress("hans.muster@app1.int");
            assertEquals(
                "Transient E-Mail-Address should have changed",
                "hans.muster@app1.int",
                emailAddress.getAddress()
            );
            segment.addAddress(false,"0001", duplicateAddress);
            this.commit();
            fail("DUPLICATE expected");
        } catch {
        	case exception: JmiServiceException =>
                assertTrue(
                    "Early duplicate recognition", 
                    persistenceManager.currentTransaction().isActive()
                );
                assertEquals(
                    "Duplicate exception expected",
                    BasicException.Code.DUPLICATE,
                    exception.getExceptionCode()
                );
                this.rollback();
        	case exception: JDOException =>
                assertFalse(
                    "Late duplicate recognition", 
                    persistenceManager.currentTransaction().isActive()
                );
                var exceptionStack: BasicException = BasicException.toExceptionStack(exception);
                assertTrue(
                    "Unit of work failure",
                    exceptionStack.getExceptionCode() == BasicException.Code.ABORT ||
                    exceptionStack.getExceptionCode() == BasicException.Code.ROLLBACK
                );
        }
        assertEquals(
            "Persistent E-Mail-Address shouldn't have changed",
            "hans.muster@app1.ch",
            emailAddress.getAddress()
        );
        assertTrue(
            "Identity should be available after unit of work failure",
            emailAddress.getIdentity().endsWith("/segment/Standard/address/0002")
        );
        //
        // CR220019366 Missing implementation
        // 
        try {
            taskId = "CR220019366";
            var original: Address = segment.getAddress("0001");
            assertEquals("Address.id()", "0001", original.getId());
            var sibling: PersistenceManager = TestMain.entityManagerFactory.getPersistenceManager();
            var xri: Path = new Path(original.refMofId()); 
            var container = sibling.getObjectById(xri.getParent()).asInstanceOf[RefContainer[Address]];
            var copy: Address = container.refGet(RefContainer.REASSIGNABLE, xri.getBase());
            assertEquals("Address.id()", "0001", copy.getId());
            assertNotSame(original, copy);
            assertEquals(original, copy);
        } finally {
            taskId = null;
        }
        //
        // CR20018768 refresh
        // 
        try {
            taskId = "CR20018768";
            this.begin();
            var i = 3;
            while(i >= 0) {
                emailAddress.setAddress("jean.\u00e9echantillon");
                assertTrue(JDOHelper.isDirty(emailAddress));
                assertEquals(
                    "jean.\u00e9echantillon",
                    emailAddress.getAddress()
                );
                i match {
                    case 3 =>
                        this.entityManager.refresh(emailAddress);
                    case 2 =>
                        this.entityManager.refreshAll(segment.getAddress());
                    case 1 =>
                        var query: AddressQuery = null;
                        this.entityManager.refreshAll(segment.getAddress(query));
                    case _ =>
                        this.entityManager.refreshAll();
                }
                assertTrue(!JDOHelper.isDirty(emailAddress));
                assertEquals(
                    "Persistent E-Mail-Address be reset",
                    "hans.muster@app1.ch",
                    emailAddress.getAddress()
                );
                i -= 1;
            }
            this.commit();
            i = 3;
            while(i >= 0) {
                i match {
                    case 3 =>
                        this.entityManager.evict(emailAddress);
                    case 2 =>
                        this.entityManager.evictAll(segment.getAddress());
                    case 1 =>
                        var query: AddressQuery  = null;
                        this.entityManager.evictAll(segment.getAddress(query));
                    case _ =>
                        this.entityManager.evictAll();
                }
                i -= 1;
            }
            i = 3;
            while(i >= 0) {
                i match {
                    case 3 => 
                        this.entityManager.retrieve(emailAddress);
                    case 2 =>
                        this.entityManager.retrieveAll(segment.getAddress());
                    case 1 =>
                        var query: AddressQuery  = null;
                        this.entityManager.retrieveAll(segment.getAddress(query));
                    case _ =>
                        // there is no retrireveAll() operation
                }
                i -= 1;
            }
        } finally {
            taskId = null;
        }
        // invoke sendMessageTemplate (struct with object reference field)
        this.begin();
        var messageTemplate: MessageTemplate = messageTemplateClass.createMessageTemplate();
        messageTemplate.setText("hello world");
        segment.addMessageTemplate(
            false,
            "template0",
            messageTemplate
        );
        this.commit();
        this.begin();
        var sendResult: EmailAddressSendMessageTemplateResult = emailAddress.sendMessageTemplate(
            app1Package.createEmailAddressSendMessageTemplateParams(
                messageTemplate,
                0,
                "hello world"
            )
        );
        assertNotNull("Send result", sendResult);
        this.commit();

        // create a person without qualifier
        var person: Person = null;

        //
        // CR20019366 Marshalling
        //
        try {
            taskId = "CR20019366";
            var people: Collection[Person]  = segment.getPerson();
            var aPerson: Person = people.iterator().next();
            assertSame(
                "segment.getPerson()",
                JDOHelper.getPersistenceManager(segment),
                JDOHelper.getPersistenceManager(aPerson)
            );
        } finally {
            taskId = null;
        }

        //
        // CR0003390 Code Accessor
        // 
        try {
            taskId = "CR0003390";
            this.begin();
            person = segment.getPerson(false,"DOE");
            var runtime: Runtime = Runtime.getRuntime();
            persistenceManager.makeTransactional(segment);
            var initialMemoryUsage: Long = runtime.totalMemory() - runtime.freeMemory();
            var i = 1;
            var limit = 10000;
            while(i < 1000) {
                person = segment.getPerson("DOE");
                var currentMemoryUsage: Long = runtime.totalMemory() - runtime.freeMemory();
                var additionalMemoryUsage: Long = currentMemoryUsage - initialMemoryUsage;
                if(additionalMemoryUsage > limit) {
                    runtime.gc();
                    currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
                    additionalMemoryUsage = currentMemoryUsage - initialMemoryUsage;
                    assertFalse(
                        "Memory used up after " + i + " failed retrievals: " + additionalMemoryUsage,
                        additionalMemoryUsage > limit
                    );
                }
                i += 1;
            }
            this.commit();
        } finally {
            taskId = null;
        }

        //
        // CR0003686 
        //
        try {
            taskId = "CR0003686";
            this.begin();
            person = personClass.createPerson();
            assertEquals(
                "Mix-in", 
                this.isInstanceOf[LocalConnectionTest], 
                person.isInstanceOf[NaturalPerson]
            );
            person.setBirthdate(Datatypes.create(classOf[XMLGregorianCalendar], "1963-01-01"));
            person.setLastName("Rossi");
            person.setSalutation("Signor");
            person.setSex(0);
            person.getGivenName().add("Alfonso");
            var age = new GregorianCalendar().get(Calendar.YEAR) - 1963;
            assertEquals("Age", age, person.getAge()); 
            segment.addPerson(false, nextId(), person);
            this.commit();
            fail("'Signor' was expected not to be supported");
        } catch {
        	case  exception: JDOFatalDataStoreException =>
            	System.out.println("Unsupported language prevents commit");
        } finally {
            taskId = null;
        }

        this.begin();
        person = personClass.createPerson();
        person.setForeignId("FX");
        person.setBirthdate(Datatypes.create(classOf[XMLGregorianCalendar], "1960-01-01"));
        person.setBirthdateAsDateTime(Datatypes.create(classOf[Date], "19600101T120000.000Z"));
        /* d = */ person.getBirthdateAsDateTime();
        person.setLastName("MusterX");
        person.setSalutation("Herr");
        person.setSex(0);
        person.getGivenName().add("Hans");
        person.getGivenName().add("Heiri");
        person.setGivenName("Hans", "Heiri");
        var additionalInfo: SparseArray[String] = person.getAdditionalInfo();
        additionalInfo.put(0, "Null");
        additionalInfo.put(2, "Zwei");
        person.getAssignedAddress().addAll(Arrays.asList(postalAddress,emailAddress));
        segment.addPerson(false, nextId(), person);
        this.commit();
        
        var personId: Path = JDOHelper.getObjectId(person).asInstanceOf[Path];
        if(!(this.isInstanceOf[ContainerManagedTransactionTest])){
            {
                this.begin();
                person.getAdditionalInfo().put(10, "Ten");
            }
            //
            // CR20019182 persistenceCapable.equals() 
            // 
            try {
                taskId = "CR20019182";
                var anotherPersistenceManager: PersistenceManager = entityManager.getPersistenceManagerFactory().getPersistenceManager();
                var anotherPerson: Person = anotherPersistenceManager.getObjectById(personId).asInstanceOf[Person];
                assertNotSame("Same person in different persistence managers", person, anotherPerson);
                assertEquals("Same person in different persistence managers", person, anotherPerson);
                var anotherInfo: SparseArray[String] = anotherPerson.getAdditionalInfo();
                assertEquals("CR20018969", 2, anotherInfo.size());
                assertEquals("CR20018969.0", "Null", anotherInfo.get(0));
                assertNull("CR20018969.1", anotherInfo.get(1));
                assertEquals("CR20018969.2", "Zwei", anotherInfo.get(2));
                assertNull("CR20018969.3", anotherInfo.get(3));
                assertTrue("CR20018969.1_2", anotherInfo.subMap(1, 2).isEmpty());
                var tail: SparseArray[String] = anotherInfo.tailMap(1);
                assertEquals("CR20018969.1_", 1, tail.size());
                var head: SparseArray[String] = anotherInfo.headMap(1);
                assertEquals("CR20018969._1", 1, head.size());
                assertEquals("CR20018969.0", "Null", head.get(0));
                assertNull("CR20018969.0", tail.get(0));
                assertNull("CR20018969.2", head.get(2));
                assertEquals("CR20018969.2", "Zwei", tail.get(2));
                anotherPersistenceManager.currentTransaction().begin();
                for(e <- anotherInfo.entrySet()) {
                    e.setValue(e.getKey().toString());
                }
                anotherPersistenceManager.currentTransaction().commit();
            } finally {
                taskId = null;
            }            
            try {
              this.commit();
              fail("CONCURRENT_ACCESS_FAILURE expected");
            } catch {
            	case exception: JDOOptimisticVerificationException =>
                    assertEquals(
                        "CONCURRENT_ACCESS_FAILURE expected",
                        BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                        BasicException.toExceptionStack(exception).getCause(null).getExceptionCode()
                    );
            }          
            {
                var anotherPersistenceManager: PersistenceManager = entityManager.getPersistenceManagerFactory().getPersistenceManager();
                var anotherPerson: Person = anotherPersistenceManager.getObjectById(personId).asInstanceOf[Person];
                var anotherInfo: SparseArray[String] = anotherPerson.getAdditionalInfo();
                assertEquals("CR20018969", 2, anotherInfo.size());
                assertEquals("CR20018969.0", "0", anotherInfo.get(0));
                assertNull("CR20018969.1", anotherInfo.get(1));
                assertEquals("CR20018969.2", "2", anotherInfo.get(2));
                assertNull("CR20018969.3", anotherInfo.get(3));
                assertTrue("CR20018969.1_2", anotherInfo.subMap(1, 2).isEmpty());
                var tail: SparseArray[String] = anotherInfo.tailMap(1);
                assertEquals("CR20018969.1_", 1, tail.size());
                var head: SparseArray[String] = anotherInfo.headMap(1);
                assertEquals("CR20018969._1", 1, head.size());
                assertEquals("CR20018969.0", "0", head.get(0));
                assertNull("CR20018969.0", tail.get(0));
                assertNull("CR20018969.2", head.get(2));
                assertEquals("CR20018969.2", "2", tail.get(2));
            }
        }

        assertEquals("person.refMofId() must be object path", 1, new Path(person.refMofId()).size() % 2);
        assertEquals("person's path must be object path", 1,TestMain.refGetPath(person).size() % 2);
        assertEquals("person.refIdentity() must corrspond to its path",TestMain.refGetPath(person).toXRI(), person.getIdentity());
        assertEquals("person.refMofId() must corrspond to its path",TestMain.refGetPath(person).toXRI(), person.refMofId());

        assertEquals(
            "Initial postal code without country code",
            "8005",
            postalAddress.getPostalCode()
        );

        
        // Add country code to postal code
        this.begin();
        person.voidOp();
        //
        // The postal address object itself is untouched 
        // but has been modified by an operation on the person object
        //
        persistenceManager.makeTransactional(postalAddress);
        this.commit();

        assertEquals(
            "voidOp should have updated the postal codes",
            "CH-8005",
            postalAddress.getPostalCode()
        );

        // get assigned addresses by index
        i = 0;
        while(i < 2) {
            // postal code not yet refreshed
            var addresses: List[Address] = person.getAssignedAddress();
            var address: Address = addresses.get(i);
            System.out.println("assigned address=" +TestMain.refGetPath(address));
            i += 1;
        }

        var additionalAddress: PostalAddress = null;
        try {
            taskId = "CR0002096";
            this.begin();
            additionalAddress = app1Package.getPostalAddress().createPostalAddress();
            additionalAddress.setCity("Zurich");
            additionalAddress.setHouseNumber("1");
            additionalAddress.setPostalCode("8050");
            additionalAddress.setStreet("Technoparkstrasse");
            // get assigned addresses by iterator
            System.out.println("adding three more addresses");
            segment.addAddress(false, "CR0002096", additionalAddress);
            person.getAssignedAddress().addAll(Arrays.asList(postalAddress,additionalAddress,emailAddress));
            this.commit();
        } finally {
            taskId = null;
        }
        var assignedAddresses: List[Address] = person.getAssignedAddress();
        for(address <- assignedAddresses) {
            // postal code refreshed
            if(TestMain.refGetPath(address).equals(TestMain.refGetPath(postalAddress))) {
                if(address.isInstanceOf[DelegatingRefObject_1_0]) {
                    assertSame(
                        "created and retrieved object should be the same",
                        address.asInstanceOf[DelegatingRefObject_1_0].openmdxjdoGetDataObject(),
                        postalAddress.asInstanceOf[DelegatingRefObject_1_0].openmdxjdoGetDataObject()
                    );
                } else {
                    assertSame(
                        "created and retrieved object should be the same",
                        address.asInstanceOf[RefObject_1_0].refDelegate(),
                        postalAddress.asInstanceOf[RefObject_1_0].refDelegate()
                    );
                }
            }
            System.out.println("assigned address=" +TestMain.refGetPath(address));
        }
        assertEquals("number of assigned addresses", 5, person.getAssignedAddress().size());

        // assignAddress by operation. This operation does not really
        // perform an assign. It is just there to see whether the operation
        // invocation works.
        this.begin();
        person.assignAddress(
            app1Package.createPersonAssignAddressParams(
                Arrays.asList(
		  postalAddress,
		  emailAddress
	      )
            )
        );
        this.commit();

        //
        // CR20018578 State After Removal
        // 
        try {
            taskId = "CR20018578";
            this.begin();
            assertTrue("Persistent", JDOHelper.isPersistent(additionalAddress));
            assertFalse("Deleted", JDOHelper.isDeleted(additionalAddress));
            assertNotNull("Object Id", JDOHelper.getObjectId(additionalAddress));
            additionalAddress.refDelete();
            assertTrue("Persistent", JDOHelper.isPersistent(additionalAddress));
            assertTrue("Deleted", JDOHelper.isDeleted(additionalAddress));
            assertNotNull("Object Id", JDOHelper.getObjectId(additionalAddress));
            var a: Address = segment.getAddress("CR0002096");
            assertNotNull("Deleted additional address", a);
            assertSame("Deleted additional address", additionalAddress, a);
            this.commit();
            assertFalse("Persistent", JDOHelper.isPersistent(additionalAddress));
            assertNull("Object Id", JDOHelper.getObjectId(additionalAddress));
            assertNotNull("Transactional Object Id", JDOHelper.getTransactionalObjectId(additionalAddress));
            assertNull("Deleted additional address", segment.getAddress("CR0002096"));
        } finally {
            taskId = null;
        }
        
        //
        // CR0002096
        // 
        try {
            taskId = "CR0002096";
            this.begin();
            var j = 0;
            assignedAddresses = person.getAssignedAddress();
            var i: Iterator[Address] = assignedAddresses.iterator();
            while(i.hasNext()) {
                // postal code refreshed
                var address: Address = null;
                try {
                    address = i.next();
                    assertNotNull("Returning null was the former behaviour", address);
                    assertNotNull("Returning null was the former behaviour", JDOHelper.getObjectId(address)); // was current object id
                    if(TestMain.refGetPath(address).equals(TestMain.refGetPath(postalAddress))) {
                        if(address.isInstanceOf[DelegatingRefObject_1_0]) {
                            assertSame(
                                "created and retrieved object should be the same",
                                address.asInstanceOf[DelegatingRefObject_1_0].openmdxjdoGetDataObject(),
                                postalAddress.asInstanceOf[DelegatingRefObject_1_0].openmdxjdoGetDataObject()
                            );
                        } else {
                            assertSame(
                                "created and retrieved object should be the same",
                                address.asInstanceOf[RefObject_1_0].refDelegate(),
                                postalAddress.asInstanceOf[RefObject_1_0].refDelegate()
                            );
                        }
                    }
                    System.out.println("Assigned address " + j + ": " +TestMain.refGetPath(address));
                } catch {
                	case exception: InvalidObjectException =>
                        i.remove();
                        System.out.println("Assigned address " + j + ": removed");
                }
                j += 1;
            }
            this.commit();
            assertEquals("number of assigned addresses", 4, person.getAssignedAddress().size());
        } finally {
            taskId = null;
        }

        //
        // CR20018837
        //
        try {
            taskId = "CR20018837";
            System.out.println("Removal test");
            var i = 0;
            while(i < 8) {
                var persistentNew: Boolean = i % 2 == 0;
                var invoiceId: String = this.taskId + ('a' + i);
                this.begin();
                var additionalInvoice: Invoice = this.entityManager.newInstance(classOf[Invoice]);
                additionalInvoice.setDescription("Invoice # " + invoiceId);
                assertFalse("Step " + i + " Additional invoice not yet deleted", JDOHelper.isDeleted(additionalInvoice));
                assertFalse("Step " + i + " Additional invoice not yet persistent", JDOHelper.isPersistent(additionalInvoice));
                assertFalse("Step " + i + " Additional invoice not yet new", JDOHelper.isNew(additionalInvoice));
                assertFalse("Step " + i + " Additional invoice not yet persistent", JDOHelper.isDirty(additionalInvoice));
                var additionalPosition: InvoicePosition = this.entityManager.newInstance(classOf[InvoicePosition]);
                assertFalse("Step " + i + " Additional position not yet deleted", JDOHelper.isDeleted(additionalPosition));
                assertFalse("Step " + i + " Additional position not yet persistent", JDOHelper.isPersistent(additionalPosition));
                assertFalse("Step " + i + " Additional position not yet new", JDOHelper.isNew(additionalPosition));
                assertFalse("Step " + i + " Additional position not yet persistent", JDOHelper.isDirty(additionalPosition));
                segment.addInvoice(false,invoiceId , additionalInvoice);
                assertSame("Step " + i + " Created invoice retrieval", additionalInvoice, segment.getInvoice(false, invoiceId));
                assertFalse("Step " + i + " Additional invoice not yet deleted", JDOHelper.isDeleted(additionalInvoice));
                assertTrue("Step " + i + " Additional invoice now persistent", JDOHelper.isPersistent(additionalInvoice));
                assertTrue("Step " + i + " Additional invoice now new", JDOHelper.isNew(additionalInvoice));
                assertTrue("Step " + i + " Additional invoice now persistent", JDOHelper.isDirty(additionalInvoice));
                additionalInvoice.addInvoicePosition(false,Integer.toString(i) , additionalPosition);
                assertSame("Step " + i + " Created position retrieval", additionalPosition, additionalInvoice.getInvoicePosition(false,Integer.toString(i)));
                assertFalse("Step " + i + " Additional invoice not yet deleted", JDOHelper.isDeleted(additionalPosition));
                assertTrue("Step " + i + " Additional invoice now persistent", JDOHelper.isPersistent(additionalPosition));
                assertTrue("Step " + i + " Additional invoice now new", JDOHelper.isNew(additionalPosition));
                assertTrue("Step " + i + " Additional invoice now persistent", JDOHelper.isDirty(additionalPosition));
                var positionId = JDOHelper.getObjectId(additionalPosition);
                if(!persistentNew) {
                    this.commit();
                    this.begin();
                }
                (i/2) match {
                    case 0 =>
                        additionalInvoice.refDelete();
                    case 1 =>
                        segment.getInvoice().remove(QualifierType.REASSIGNABLE, invoiceId);
                    case 2 =>
                        this.entityManager.deletePersistent(additionalInvoice);
                    case 3 =>
                        segment.getInvoice().remove(additionalInvoice);
                }
                assertSame("Step " + i + " Deleted invoice retrieval", additionalInvoice, segment.getInvoice(false, invoiceId));
                assertTrue("Step " + i + " Additional invoice now deleted", JDOHelper.isDeleted(additionalInvoice));
                assertTrue("Step " + i + " Additional invoice still persistent", JDOHelper.isPersistent(additionalInvoice));
                assertEquals("Step " + i + " Additional invoice might be new new", i % 2 == 0, JDOHelper.isNew(additionalInvoice));
                assertTrue("Step " + i + " Additional invoice now deleted", JDOHelper.isDirty(additionalInvoice));
                assertSame("Step " + i + " Deleted position retrieval", additionalPosition, this.entityManager.getObjectById(positionId));
                if(persistentNew) {
                    assertTrue("Step " + i + " Additional position now deleted", JDOHelper.isDeleted(additionalPosition));
                    assertTrue("Step " + i + " Additional position still persistent", JDOHelper.isPersistent(additionalPosition));
                    assertEquals("Step " + i + " Additional position might be new", i % 2 == 0, JDOHelper.isNew(additionalPosition));
                    assertTrue("Step " + i + " Additional position now deleted", JDOHelper.isDirty(additionalPosition));
                }
                this.commit();
                assertNull("Step " + i + " Deleted invoice retrieval", segment.getInvoice(false, invoiceId));
                assertFalse("Step " + i + " Additional invoice now transient", JDOHelper.isDeleted(additionalInvoice));
                assertFalse("Step " + i + " Additional invoice now transient", JDOHelper.isPersistent(additionalInvoice));
                assertFalse("Step " + i + " Additional invoice now transient", JDOHelper.isNew(additionalInvoice));
                assertFalse("Step " + i + " Additional invoice now transient", JDOHelper.isDirty(additionalInvoice));
                assertFalse("Step " + i + " Additional position now transient", JDOHelper.isDeleted(additionalPosition));
                assertFalse("Step " + i + " Additional position now transient", JDOHelper.isPersistent(additionalPosition));
                assertFalse("Step " + i + " Additional position now transient", JDOHelper.isNew(additionalPosition));
                assertFalse("Step " + i + " Additional position now transient", JDOHelper.isDirty(additionalPosition));
                i += 1;
            }
        } finally {
            taskId = null;
        }
        //
        // CR20018837
        //
        try {
            taskId = "CR20018837";
            System.out.println("Persistent-new-deleted test");
            this.begin();
            additionalAddress = app1Package.getPostalAddress().createPostalAddress();
            additionalAddress.setCity("Liestal");
            additionalAddress.setHouseNumber("2");
            additionalAddress.setPostalCode("2222");
            additionalAddress.setStreet("Nebenstrasse");
            assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
            assertFalse("Additional address not yet persistent", JDOHelper.isPersistent(additionalAddress));
            assertFalse("Additional address not yet new", JDOHelper.isNew(additionalAddress));
            segment.addAddress(false,"9002", additionalAddress);
            assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
            assertTrue("Additional address now persistent", JDOHelper.isPersistent(additionalAddress));
            assertTrue("Additional address now new", JDOHelper.isNew(additionalAddress));
            additionalAddress.refDelete();
            assertSame("Deleted address retrieval", additionalAddress, segment.getAddress(false, "9002"));
            assertTrue("Additional address now deleted", JDOHelper.isDeleted(additionalAddress));
            assertTrue("Additional address still persistent", JDOHelper.isPersistent(additionalAddress));
            assertTrue("Additional address still new", JDOHelper.isNew(additionalAddress));
            this.rollback();
            assertFalse("Additional address no longer deleted", JDOHelper.isDeleted(additionalAddress));
            assertFalse("Additional address no longer persistent", JDOHelper.isPersistent(additionalAddress));
            assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
            this.begin();
            additionalAddress = app1Package.getPostalAddress().createPostalAddress();
            additionalAddress.setCity("Liestal");
            additionalAddress.setHouseNumber("3");
            additionalAddress.setPostalCode("2222");
            additionalAddress.setStreet("Nebenstrasse");
            assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
            assertFalse("Additional address not yet persistent", JDOHelper.isPersistent(additionalAddress));
            assertFalse("Additional address not yet new", JDOHelper.isNew(additionalAddress));
            segment.addAddress(false,"9003", additionalAddress);
            assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
            assertTrue("Additional address now persistent", JDOHelper.isPersistent(additionalAddress));
            assertTrue("Additional address now new", JDOHelper.isNew(additionalAddress));
            segment.getAddress().remove(QualifierType.REASSIGNABLE, "9003");
            assertSame("Deleted address retrieval", additionalAddress, segment.getAddress(false, "9003"));
            assertTrue("Additional address now deleted", JDOHelper.isDeleted(additionalAddress));
            assertTrue("Additional address still persistent", JDOHelper.isPersistent(additionalAddress));
            assertTrue("Additional address still new", JDOHelper.isNew(additionalAddress));
            this.commit();
            assertFalse("Additional address no longer deleted", JDOHelper.isDeleted(additionalAddress));
            assertFalse("Additional address no longer persistent", JDOHelper.isPersistent(additionalAddress));
            assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
            this.begin();
            additionalAddress = app1Package.getPostalAddress().createPostalAddress();
            additionalAddress.setCity("Liestal");
            additionalAddress.setHouseNumber("4");
            additionalAddress.setPostalCode("2222");
            additionalAddress.setStreet("Nebenstrasse");
            assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
            assertFalse("Additional address not yet persistent", JDOHelper.isPersistent(additionalAddress));
            assertFalse("Additional address not yet new", JDOHelper.isNew(additionalAddress));
            segment.addAddress(false,"9004", additionalAddress);
            assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
            assertTrue("Additional address now persistent", JDOHelper.isPersistent(additionalAddress));
            assertTrue("Additional address now new", JDOHelper.isNew(additionalAddress));
            //            segment.getAddress().remove(additionalAddress);
            //            assertSame("Deleted address retrieval", additionalAddress, segment.getAddress(false, "9004"));
            //            assertTrue("Additional address now deleted", JDOHelper.isDeleted(additionalAddress));
            assertTrue("Additional address still persistent", JDOHelper.isPersistent(additionalAddress));
            assertTrue("Additional address still new", JDOHelper.isNew(additionalAddress));
            this.rollback();
            assertFalse("Additional address no longer deleted", JDOHelper.isDeleted(additionalAddress));
            assertFalse("Additional address no longer persistent", JDOHelper.isPersistent(additionalAddress));
            assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
            //
            System.out.println("Transient container test");
            var transientSegment: test.openmdx.app1.jmi1.Segment = app1Package.getSegment().createSegment();
            this.begin();
            //            additionalAddress = app1Package.getPostalAddress().createPostalAddress();
            //            additionalAddress.setCity("Frick");
            //            additionalAddress.setHouseNumber("3");
            //            additionalAddress.setPostalCode("1111");
            //            additionalAddress.setStreet("Hauptstrasse");
            //            assertFalse("Additional address not deleted", JDOHelper.isDeleted(additionalAddress));
            //            assertFalse("Additional address not persistent", JDOHelper.isPersistent(additionalAddress));
            //            assertFalse("Additional address not new", JDOHelper.isNew(additionalAddress));
            //            transientSegment.addAddress(false,"9003", additionalAddress);
            //            assertFalse("Additional address not deleted", JDOHelper.isDeleted(additionalAddress));
            //            assertFalse("Additional address not persistent", JDOHelper.isPersistent(additionalAddress));
            //            assertFalse("Additional address not new", JDOHelper.isNew(additionalAddress));
            //            assertSame("Transient address retrieval", additionalAddress, transientSegment.getAddress(false, "9003"));
            //            transientSegment.getAddress().remove(QualifierType.REASSIGNABLE, "9003");
            //            assertNull("Removed address retrieval", transientSegment.getAddress(false, "9003"));
            //            assertFalse("Additional address not deleted", JDOHelper.isDeleted(additionalAddress));
            //            assertFalse("Additional address not persistent", JDOHelper.isPersistent(additionalAddress));
            //            assertFalse("Additional address not new", JDOHelper.isNew(additionalAddress));
            this.rollback();
            this.begin();
            //            additionalAddress = app1Package.getPostalAddress().createPostalAddress();
            //            additionalAddress.setCity("Frick");
            //            additionalAddress.setHouseNumber("4");
            //            additionalAddress.setPostalCode("1111");
            //            additionalAddress.setStreet("Hauptstrasse");
            //            transientSegment.addAddress(false,"9004", additionalAddress);
            //            assertSame("Transient address retrieval", additionalAddress, transientSegment.getAddress(false, "9004"));
            //            assertTrue("Transient address removal", transientSegment.getAddress().remove(additionalAddress));
            //            assertNull("Removed address retrieval", transientSegment.getAddress(false, "9004"));
            this.rollback();
        } finally {
            taskId = null;
        }
        //
        // CR0002987
        // 
        try {
            taskId = "CR0002987";
            System.out.println("Explicit rollback test");
            this.begin();
            additionalAddress = app1Package.getPostalAddress().createPostalAddress();
            additionalAddress.setCity("Seldwyla");
            additionalAddress.setHouseNumber("0");
            additionalAddress.setPostalCode("0000");
            additionalAddress.setStreet("Kirchgasse");
            assertFalse("Additional address not yet persistent", JDOHelper.isPersistent(additionalAddress));
            assertFalse("Additional address not yet new", JDOHelper.isNew(additionalAddress));
            segment.addAddress(false,"9001", additionalAddress);
            assertTrue("Additional address now persistent", JDOHelper.isPersistent(additionalAddress));
            assertTrue("Additional address now new", JDOHelper.isNew(additionalAddress));
            this.rollback();
            assertFalse("Additional address no longer persistent", JDOHelper.isPersistent(additionalAddress));
            assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
            try {
                System.out.println("Implicit rollback test");
                this.begin();
                additionalAddress = app1Package.getPostalAddress().createPostalAddress();
                additionalAddress.setCity("Seldwyla");
                additionalAddress.setHouseNumber("0");
                additionalAddress.setPostalCode("0000");
                additionalAddress.setStreet("Kirchgasse");
                assertFalse("Additional address not yet persistent", JDOHelper.isPersistent(additionalAddress));
                assertFalse("Additional address not yet new", JDOHelper.isNew(additionalAddress));
                segment.addAddress(false,"CR0002987", additionalAddress);
                assertTrue("Additional address now persistent", JDOHelper.isPersistent(additionalAddress));
                assertTrue("Additional address now new", JDOHelper.isNew(additionalAddress));
                var jmiNameFormat: NameFormat = app1Package.getNameFormat().createNameFormat();
                jmiNameFormat.setDescription("modified description");
                segment.addNameFormat(false,nextId(), jmiNameFormat);
                this.commit();
                fail("constraint isFrozen --> object can not be updated");
            } catch {
            	case e: JDOFatalDataStoreException =>
                assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
                assertFalse(
                    "Additional address no longer persistent", 
                    JDOHelper.isPersistent(additionalAddress)
                );
            }
        } finally {
            taskId = null;
        }

        i = 0;
        while(i < 2) {
            assertNull("No TRANSIENT person expected", segment.getPerson(false, "TRANSIENT"));
            if(i==0) this.begin();
            i += 1;
        }
        // create and remove in same unit of work
        person = personClass.createPerson();
        person.setBirthdate(Datatypes.create(classOf[XMLGregorianCalendar], "1960-01-01"));
        person.setBirthdateAsDateTime(Datatypes.create(classOf[Date], "19600101T120000.000Z"));
        person.setForeignId("FX");
        person.setLastName("MusterX");
        person.setSalutation("Herr");
        person.setSex(0);
        person.getMemberOfGroup().addAll(
            Arrays.asList("group A", "group B")
        );
        person.getGivenName().addAll(
            Arrays.asList("Hans", "Heiri")
        );
        person.getAdditionalInfo().put(
            new Integer(0),
            "additional info 1"
        );
        person.getAdditionalInfo().put(
            new Integer(1),
            "additional info 2"
        );
        person.getAssignedAddress().addAll(
            Arrays.asList(postalAddress, emailAddress)
        );

        segment.addPerson(false,"TRANSIENT", person);
        segment.getPerson(false, "TRANSIENT").refDelete(); // get and remove it in same unit of work
        this.commit();

        var g0: PersonGroup = null;
        var g1: PersonGroup = null;
        var g2: PersonGroup = null;
        //
        // create some PersonGroups
        //
        try {
            taskId = "CR20019430";
            this.begin();
            g0 = personGroupClass.createPersonGroup();
            segment.addPersonGroup(
                false,
                "g0",
                g0
            );
            g1 = personGroupClass.createPersonGroup();
            segment.addPersonGroup(
                false,
                "g1",
                g1
            );
            g2 = personGroupClass.createPersonGroup();
            g2.setName("Group 2");
            segment.addPersonGroup(
                false,
                "g2",
                g2
            );
            this.entityManager.flush();
            g1.setName("Group 1");
            this.commit();
        } finally {
            taskId = null;
        }

        // create some Persons
        this.begin();
        i = 0;
        while(i <= N_PERSONS) {
            person = personClass.createPerson();
            person.setForeignId("F" + i);
            person.setBirthdate(Datatypes.create(classOf[XMLGregorianCalendar], "1960-01-01"));
            person.setBirthdateAsDateTime(Datatypes.create(classOf[Date], "19600101T120000.000Z"));
            person.setLastName("Muster" + i);
            person.setSalutation("Herr");
            person.setSex(0);
            person.getGivenName().add("Hans");
            person.getGivenName().add("Heiri");
            person.setGivenName(Arrays.asList("Hans", "Heiri"));
            person.getAssignedAddress().add(postalAddress);
            person.getPersonGroup().add(g0);
            person.getPersonGroup().add(g1);
            person.getPersonGroup().add(g2);
            if(i < N_PERSONS) {
                segment.addPerson(false, "000" + i, person);
            } else if (this.isInstanceOf[LocalConnectionTest]) try {
                //
                // CR20019192 UnsupportedOperationException in JMI collection delegate calls 
                // 
                taskId = "CRCR20019192";
                segment.addForeignPerson("F" + N_PERSONS, person);
                fail("This shared assoication is expected to be unmodifiable");
            } catch {
            	case expected: InvalidCallException =>
            		// We expect to pass this exception handler
            } finally {
                taskId = null;
            }
            i += 1;
        }
        this.commit();

        // get person on 'composite' association 'SegmentHasPerson'
        person = segment.getPerson(false,"0001");
        System.out.println("person.age=" + person.getAge());
        System.out.println("person givenName=" + person.getGivenName().get(0));
        System.out.println("person.identity=" + person.getIdentity());
        System.out.println("person.creationDateTime=" + person.getCreationDateTime());
        System.out.println("person.createdAt=" + person.getCreatedAt());

        // test unqualified feature retrieval
        assertTrue("postalAddress.address must be instance of String", emailAddress.refGetValue("address").isInstanceOf[String]);
        assertTrue("segment.address must be instance of Container", segment.refGetValue("address").isInstanceOf[RefContainer[_]]);
        assertTrue("postalAddress.address must be instance of String", emailAddress.refGetValue("address").isInstanceOf[String]);
        //
        // test performance of accessor.jmi of reading all attributes of person 1000 times
        //
        startedAt = System.currentTimeMillis();
        i = 0;
        while(i < INSPECTION_COUNT) {
            person.getLastName();
            person.getForeignId();
            person.getGivenName();
            person.getSex();
            person.getSalutation();
            person.getBirthdate();
            person.getBirthdateAsDateTime();
            person.getAdditionalInfo();
            person.getMemberOfGroup();
            person.getAge();
            person.getCreationDateTime();
            i += 1;
        }
        System.out.println("time for inspecting person " + INSPECTION_COUNT + " times [jmi]=" + (System.currentTimeMillis() - startedAt));
        //
        // test the performance of reflective JMI accesses
        //
        startedAt = System.currentTimeMillis();
        i = 0;
        while(i < INSPECTION_COUNT) {
            person.refGetValue("lastName");
            person.refGetValue("foreignId");
            person.refGetValue("givenName");
            person.refGetValue("sex");
            person.refGetValue("salutation");
            person.refGetValue("birthdate");
            person.refGetValue("birthdateAsDateTime");
            person.refGetValue("additionalInfo");
            person.refGetValue("memberOfGroup");
            person.refGetValue("age");
            person.refGetValue("creationDateTime");
            i += 1;
        }
        System.out.println("time for inspecting person " + INSPECTION_COUNT + " times [jmi reflective]=" + (System.currentTimeMillis() - startedAt));
        if(this.isInstanceOf[LocalConnectionTest]) {
            var personData = person.asInstanceOf[DelegatingRefObject_1_0].openmdxjdoGetDataObject().asInstanceOf[RefObject_1_0].refDelegate()
            startedAt = System.currentTimeMillis();
            i = 0;
            while(i < INSPECTION_COUNT) {
                personData.objGetValue("lastName");
                personData.objGetValue("foreignId");
                personData.objGetValue("givenName");
                personData.objGetValue("sex");
                personData.objGetValue("salutation");
                personData.objGetValue("birthdate");
                personData.objGetValue("birthdateAsDateTime");
                personData.objGetValue("additionalInfo");
                personData.objGetValue("memberOfGroup");
                personData.objGetValue("age");
                personData.objGetValue("creationDateTime");
                i += 1;
            }
            System.out.println("time for inspecting person " + INSPECTION_COUNT + " times [data object]=" + (System.currentTimeMillis() - startedAt));
        }
        // test refMetaObject      
        var personDef: ModelElement_1_0 = person.refMetaObject().asInstanceOf[RefMetaObject_1].getElementDef();
        model.getFeatureDef(
            personDef,
            "salutation",
            false
        );
        model.getFeatureDef(
            personDef,
            "blabla",
            false
        );
        var attributes: Map[_,_] = personDef.objGetValue("attribute").asInstanceOf[Map[_,_]];
        attributes.get("salutation");
        {
            // get person on 'none', derived association 'SegmentReferencesForeignPerson'
            person = segment.getForeignPerson("F1");
            assertNotNull("Foreign Person", person);
            var m: PersistenceManager = JDOHelper.getPersistenceManager(person);
            assertSame(
                "Derived association marshalling", 
                persistenceManager, 
                m
            );
        }
        {
            //
            // CR20018977 Dispatching to Association Impl 
            // 
            var foreignId: Path = segment.refGetPath().getDescendant("foreignPerson", "F1");
            var personByForeignId = persistenceManager.getObjectById(foreignId).asInstanceOf[Person]
            assertSame("CR20018977", personByForeignId, person);
        }

        System.out.println("person.age=" + person.getAge());
        System.out.println("person givenName=" + person.getGivenName().get(0));
        {
            var people: Int = segment.getForeignPerson().size();
            System.out.println("Number of people: " + people);
        }
        // get persons with filter 1
        var personQuery: PersonQuery = app1Package.createPersonQuery();
        personQuery.lastName().like(
            "Muster1.*"
        );
        personQuery.birthdateAsDateTime().lessThanOrEqualTo(
            new Date()
        );
        personQuery.orderByCreatedAt().ascending();
        var personCollection: SegmentHasPerson.Person[Person] = null;
        var personList: List[Person] = null;
        try {
            taskId = "CR20019366";
            personCollection = segment.getPerson();
            var personArray = new Array[Person](personCollection.size());
            personCollection.toArray(personArray);
            for(person20019366 <- personArray) {
                if(this.isInstanceOf[ProxyConnectionTest]) {
                    assertFalse("Person Proxy", person20019366.isInstanceOf[NaturalPerson]);
                } else {
                    assertTrue("Person", person20019366.isInstanceOf[NaturalPerson]);
                    assertFalse("Person", person20019366.asInstanceOf[NaturalPerson].isRetired());
                }
            }
            personList = segment.getPerson(null);
            var containerIterator: Iterator[Person] = personCollection.iterator();
            var listIterator: Iterator[Person] = personList.iterator();
            var containerElement: Person = containerIterator.next();
            var listElement: Person = listIterator.next();
            if(this.isInstanceOf[ProxyConnectionTest]) {
                assertFalse("listElement", listElement.isInstanceOf[NaturalPerson]);
                assertFalse("containerElement", containerElement.isInstanceOf[NaturalPerson]);
            } else {
                assertTrue("listElement", listElement.isInstanceOf[NaturalPerson]);
                assertFalse("listElement", listElement.asInstanceOf[NaturalPerson].isRetired());
                assertTrue("containerElement", containerElement.isInstanceOf[NaturalPerson]);
                assertFalse("containerElement", containerElement.asInstanceOf[NaturalPerson].isRetired());
            }
            for(p <- personCollection) {
                var m = JDOHelper.getPersistenceManager(p);
                assertSame(
                    "Query result marshalling", 
                    persistenceManager, 
                    m
                );
            }
        } finally {
            taskId = null;
        }

        // personList = personCollection.getAll(personQuery);
        personList = segment.getPerson(personQuery);
        var nobodyOutThere = personList.isEmpty();
        System.out.println("There are " + (if(nobodyOutThere) "no" else "some") + " people");
        assertFalse("Anybody out there", nobodyOutThere);
        for(p <- personList) {
            assertSame(
                "Query result marshalling", 
                persistenceManager, 
                JDOHelper.getPersistenceManager(p)
            );
            SysLog.trace("person", p);
        }

        // get persons with SOUNDS like filter
        personQuery = app1Package.createPersonQuery();
        personQuery.lastName().like(
            StringTypePredicate.SOUNDS,
            "Maasteer"
        );
        {
            var people: Int = segment.getPerson().size();
            assertEquals(
                "1 added by XmlImporter, 1 added with addPerson(), N_PERSONS added by addPerson()",
                N_PERSONS + 2, 
                people
            );

            var allPeople = segment.getPerson();
            var maasteer = allPeople.getAll(personQuery);
            var numberOfPersons = maasteer.size();
            assertEquals(
                "number of persons found with SOUNDS_LIKE",
                TEST_PERSON_COUNT + SIMILAR_NAME_COUNT,
                numberOfPersons
            );
            this.begin();
            maasteer.clear();
            assertEquals(
                "Container emptied",
                0,
                segment.getPerson().size()
            );
            this.rollback();
            allPeople = segment.getPerson();
            maasteer = allPeople.getAll(personQuery);
            {
                assertTrue(
                    "People found with SOUNDS_LIKE: Second Last",
                    maasteer.listIterator(TEST_PERSON_COUNT + SIMILAR_NAME_COUNT - 1).hasNext()
                );
                assertFalse(
                    "People found with SOUNDS_LIKE: Last",
                    maasteer.listIterator(TEST_PERSON_COUNT + SIMILAR_NAME_COUNT).hasNext()
                );
            }
            numberOfPersons = maasteer.size();
            assertEquals(
                "number of persons found with SOUNDS_LIKE",
                TEST_PERSON_COUNT + SIMILAR_NAME_COUNT,
                numberOfPersons
            );
            this.begin();
            maasteer.clear();
            var remaining = segment.getPerson().size(); 
            assertEquals(
                "container emptied",
                0,
                remaining
            );
            this.rollback();
        }
        //
        // CR20019185  Batching does not work for extent queries 
        //
        try {
            taskId = "CR20019185";
            //
            // Object Identity IS_LIKE Condition
            //
            personQuery = PersistenceHelper.newQuery(
                persistenceManager.getExtent(classOf[Person]),
                segment.refMofId() + "/person/($..)"
            ).asInstanceOf[PersonQuery]
            //
            // String Feature IS_LIKE Condition
            //
            personQuery.lastName().like(
                StringTypePredicate.SOUNDS,
                "Maasteer"
            );
            //
            // Reference IS_LIKE Condition
            //
            personQuery.forAllAssignedAddress().elementOf(
                PersistenceHelper.getCandidates(
                    persistenceManager.getExtent(classOf[Address]),
                    segment.refMofId() + "/address/($..)"
                )
            );
            var maasteer = segment.getExtent(personQuery);
            var numberOfPersons = 0;
            for(p <- maasteer) {
                numberOfPersons += 1;
            }
            assertEquals(
                "number of persons found with SOUNDS_LIKE",
                TEST_PERSON_COUNT + SIMILAR_NAME_COUNT,
                numberOfPersons
            );
            
        } finally {
            taskId = null;
        }
        
        // find persons with assigned address
        personQuery = app1Package.createPersonQuery();
        personQuery.thereExistsAssignedAddress().equalTo(postalAddress);
        personCollection = segment.getPerson();
        for(p <- personCollection.getAll(personQuery)) {
            SysLog.trace("person", p);
        }

        // find persons with empty filter
        personQuery = app1Package.createPersonQuery();
        personCollection = segment.getPerson();
        for(p <- personCollection.getAll(personQuery)) {
            SysLog.trace("person", p);
        }

        // 
        // Test CR0003454
        // 
        try {
            taskId = "CR0003454";
            personQuery = app1Package.createPersonQuery();
            personQuery.foreignId().like("F.");
            personQuery.orderByForeignId().ascending();
            var cr0003454 = segment.getPerson(personQuery);
            var pi = cr0003454.listIterator();
            var i = 0;
            while(i < 6) {
                assertEquals("ListIterator.nextIndex()", i, pi.nextIndex());
                var pp: Person = pi.next();
                System.out.println("person["+i+"] "+pp.getForeignId());
                i += 1;
            }
            i = 5;
            while(i >= 0) {
                assertEquals("ListIterator.previousIndex()", i, pi.previousIndex());
                var pp: Person = pi.previous();
                assertEquals("Person["+i+"].foreignId", "F"+i, pp.getForeignId());
                i -= 1;
            }
        } finally {
            taskId = null;
        }
        //
        // modify given name
        //
        try {
            taskId = "CR20019430";
            this.begin();
            assertFalse("Pre-modify state", JDOHelper.isDirty(person));
            person.getGivenName().clear();
            assertTrue("Pre-flush state", JDOHelper.isDirty(person));
            assertTrue("pre-flush attribute retrieval", person.getGivenName().isEmpty());
            this.entityManager.flush();
            assertTrue("Post-flush state", JDOHelper.isDirty(person));
            assertTrue("Post-flush attribute retrieval", person.getGivenName().isEmpty());
            person.setGivenName(Arrays.asList("Heiri"));
            this.commit();
            assertEquals("givenName", Collections.singletonList("Heiri"), person.getGivenName());
            assertFalse("Post-commit state", JDOHelper.isDirty(person));
        } finally {
            taskId = null;
        }
        //
        // keep given name
        //
        try {
            taskId = "CR20019472";
            this.begin();
            var givenName = person.getGivenName();
            givenName.set(0, new String(givenName.get(0)));
            assertTrue("Phantom modification", JDOHelper.isDirty(person));
            this.commit();
        } finally {
            taskId = null;
        }

        this.begin();
        person.getGivenName().clear();
        assertTrue("No givenName", person.getGivenName().isEmpty());
        this.commit();
        assertTrue("No givenName", person.getGivenName().isEmpty());

        // person.formatAs
        this.begin(); // isQuery() is false      
        var formattedName = person.formatNameAs(
            app1Package.createPersonFormatNameAsParams("Standard")
        );
        this.commit(); // result available after commit only               
        System.out.println("formatted name=" + formattedName.getFormattedName());
        System.out.println("formatted name as set=" + formattedName.getFormattedNameAsSet());
        System.out.println("formatted name as list=" + formattedName.getFormattedNameAsList());
        System.out.println("formatted name as sparsearray=" + formattedName.getFormattedNameAsSparseArray());

        // test optional argument
        this.begin(); // isQuery() is false               
        formattedName = person.formatNameAs(
            app1Package.createPersonFormatNameAsParams(
                null // default value is Standard
            )
        );
        this.commit(); // result available after commit only               
        System.out.println("formatted name=" + formattedName.getFormattedName());
        try {
            person.formatNameAs(
                app1Package.createPersonFormatNameAsParams(
                    "InvalidFormat"
                )
            );
            fail("CanNotFormatNameException expected");
        } catch {
        	case e: CanNotFormatNameException =>
        		System.out.println("formatNameAs() raised exception as expected \n" + e.getMessage());
        }

        // test dateOp (date and dateTime in operation parameter)
        // Test for non-query operation with result 
        this.begin();
        var dateTimeNow = new Date();
        var dateOpResult = person.dateOp(
            app1Package.createPersonDateOpParams(
                Datatypes.create(
                    classOf[XMLGregorianCalendar], 
                    DateTimeFormat.BASIC_UTC_FORMAT.format(dateTimeNow).substring(0, 8)
                ),
                dateTimeNow
            )
        );
        this.commit();
        System.out.println("dateOp.dateResult=" + dateOpResult.getDateResult());
        System.out.println("dateOp.dateTimeResult=" + dateOpResult.getDateTimeResult());

        // no more NOT_FOUND exceptions
        assertNull("Not existing person", segment.getPerson("alskdjflaksdjf"));

        // remove some persons

        System.out.println("removing person=" + segment.getPerson("0001").getLastName());
        System.out.println("removing person=" + segment.getPerson("00053").getLastName());
        System.out.println("removing person=" + segment.getPerson("00082").getLastName());

        var initialPersonCount = segment.getPerson().size();
        this.begin();
        segment.getPerson(false,"0001").refDelete();
        segment.getPerson(false,"00053").refDelete();
        segment.getPerson(false,"00082").refDelete();
        var finalPersonCount = segment.getPerson().size();
        assertEquals(
            "Transient person count",
            initialPersonCount - 3,
            finalPersonCount
        );
        this.rollback();

        finalPersonCount = segment.getPerson().size();
        assertEquals(
            "Rollback person count",
            initialPersonCount,
            finalPersonCount
        );
        this.begin();
        segment.getPerson(false,"0001").refDelete();
        segment.getPerson(false,"00053").refDelete();
        segment.getPerson(false,"00082").refDelete();
        finalPersonCount = segment.getPerson().size();
        assertEquals(
            "Transient person count",
            initialPersonCount - 3,
            finalPersonCount
        );
        this.commit();
        finalPersonCount = segment.getPerson().size();
        assertEquals(
            "Commit person count",
            initialPersonCount - 3,
            finalPersonCount
        );

        // ... and test whether they are removed
        assertNull("person 0001 not removed", segment.getPerson("0001"));

        //
        // CR0003390 Code Accessor
        //
        try {
            taskId = "CR0003390";
            this.begin();
            person = segment.getPerson("DOE");
            assertNull("DOE does not exist", person);
            person = personClass.createPerson();
            segment.addPerson(false,"DOE", person);
            assertTrue("DOE is persistent-new", JDOHelper.isNew(person));
            assertFalse("DOE is persistent-new", JDOHelper.isDeleted(person));
            person.refDelete();
            person = segment.getPerson("DOE");
            assertTrue("DOE is persistent-new-deleted", JDOHelper.isNew(person));
            assertTrue("DOE is persistent-new-deleted", JDOHelper.isDeleted(person));
            this.rollback();

            // Add after failed get
            assertNull("person NO1 exists", segment.getPerson("NO1"));

            this.begin();
            person = personClass.createPerson();
            person.setForeignId("X1");
            person.setBirthdate(Datatypes.create(classOf[XMLGregorianCalendar], "1961-11-11"));
            person.setBirthdateAsDateTime(Datatypes.create(classOf[Date], "19611111T120000.000Z"));
            person.setLastName("Muster1");
            person.setSalutation("Herr");
            person.setSex(0);
            person.getGivenName().add("Hans");
            person.getGivenName().add("Heiri");
            person.setGivenName(Arrays.asList("Hans", "Heiri"));
            person.getAssignedAddress().add(postalAddress);
            segment.addPerson(false, "NO1", person);
            this.rollback();

            // ... and test whether they are removed
            assertNull("Person N01", segment.getPerson(false,"NO1"));
            segment.getPerson().remove(QualifierType.REASSIGNABLE,"NO1");

            // A non-existent person
            assertNull("Person N02", segment.getPerson(false,"NO2"));
            segment.getPerson().remove(QualifierType.REASSIGNABLE,"NO2");

            // Add after failed removal
            this.begin();
            person = personClass.createPerson();
            person.setForeignId("X2");
            person.setBirthdate(Datatypes.create(classOf[XMLGregorianCalendar], "1961-11-11"));
            person.setBirthdateAsDateTime(Datatypes.create(classOf[Date], "19611111T120000.000Z"));
            person.setLastName("Muster1");
            person.setSalutation("Herr");
            person.setSex(0);
            person.getGivenName().add("Hans");
            person.getGivenName().add("Heiri");
            person.setGivenName(Arrays.asList("Hans", "Heiri"));
            person.getAssignedAddress().add(postalAddress);
            segment.addPerson(false,"NO2", person);
            this.rollback();

            assertNull("person 00053 not removed", segment.getPerson("00053"));
            assertNull("person 00082 not removed", segment.getPerson("00082"));

            // postalAddress.formatAs
            var formattedAddress: AddressFormatAsResult = null;
            formattedAddress = postalAddress.formatAs(
                app1Package.createAddressFormatAsParams(
                    "Standard"
                )
            );
            System.out.println("formatted address=" + formattedAddress.getFormattedAddress());

            // emailAddress.formatAs
            formattedAddress = emailAddress.formatAs(
                app1Package.createAddressFormatAsParams(
                    "Standard"
                )
            );
            System.out.println("formatted address=" + formattedAddress.getFormattedAddress());

            // get addresses by iterator
            var addresses = segment.getAddress();
            for(address: Address <- addresses) {
                System.out.println("address.id=" + address.getId());
                System.out.println("address=" + address);

                // invoke sendMessage on PostalAddress
                if(address.isInstanceOf[PostalAddress]) {
                    this.begin(); // isQuery() is false               
                    address.asInstanceOf[PostalAddress].sendMessage(
                        app1Package.createPostalAddressSendMessageParams(
                            Array(
                            	'h'.asInstanceOf[Byte], 
                            	'e'.asInstanceOf[Byte], 
                            	'l'.asInstanceOf[Byte], 
                            	'l'.asInstanceOf[Byte], 
                            	'o'.asInstanceOf[Byte]
                            )
                        )
                    );
                    this.commit();
                }
                else if(address.isInstanceOf[EmailAddress]) {
                    this.begin(); // isQuery() is false               
                    address.asInstanceOf[EmailAddress].sendMessage(
                        app1Package.createEmailAddressSendMessageParams(
                            "hello"
                        )
                    );
                    this.commit();
                }
                else {
                    fail("address format " + address.getClass().getName() + " unknown");
                }
            }
        } finally {
            taskId = null;
        }
        
        {
            //
            // test cycles
            //
            this.begin();
            var member1 = cycleMember1Class.createCycleMember1();
            member1.setDescription("this is member1");
            var member2 = cycleMember2Class.createCycleMember2();
            member2.setDescription("this is member2");
            member1.setM2(member2);
            member2.setM1(member1);
            segment.addCycleMember1(false, new BigDecimal(1), member1);
            segment.addCycleMember2(false, "member2", member2);
            this.commit();

            // verify member1, member2
            member1 = segment.getCycleMember1(new BigDecimal(1));
            member2 = member1.getM2();
            System.out.println("member1" + member1);
            System.out.println("member2" + member2);
            {
                assertNotNull("We need a member value for the next test", member2);
                var query = app1Package.createCycleMember1Query();
                query.thereExistsM2().equalTo(member2);
                query.m2().isNonNull();
                try {
                    query.thereExistsM2().equalTo(null);
                    fail("equalTo's argument must not be null");
                } catch {
                	case exception: JmiServiceException =>
                		assertEquals("equalTo(null)", BasicException.Code.BAD_PARAMETER, exception.getExceptionCode());
                }
            }
        }

        // test streams
        this.begin();

        val contentLength = 1000;
        var content = new Array[Byte](contentLength);
        i = 0;
        while(i < contentLength) {
            content(i) = (i % 256).asInstanceOf[Byte];
            i += 1;
        }
        var document = documentClass.createDocument();
        document.setContent(
            org.w3c.cci2.BinaryLargeObjects.valueOf(content)
        );
        document.setDescription(
            "a random document"
        );
        document.setKeyword(
            new HashSet[String](
                    Arrays.asList("random", "document", "junit")
            )
        );
        segment.addDocument(false, "myDoc", document);
        this.commit();

        // verify returned document
        document = segment.getDocument("myDoc");
        System.out.println("document.description=" + document.getDescription());
        System.out.println("document.keyword=" + document.getKeyword());
        var contentLo = document.getContent();
        {
            assertNotNull("BLOB", contentLo);
            var documentSize = contentLo.getLength();
            if(documentSize != null) {
                assertEquals("document size", contentLength, documentSize.longValue());
            }
            var r = 0;
            while(r < 2) {
                //
                // test with input stream method
                //
                System.out.println("verifying content (with InputStream)");
                var contentIs = contentLo.getContent();
                assertNotNull("A large object's stream", contentIs);
                var i = 0;
                while(i < contentLength) {
                    assertEquals("content at position " + i, i % 256, contentIs.read());
                    contentIs.skip(9);
                    i += 10;
                }
                contentIs.close();
                System.out.println("OK");
                r += 1;
            }
            r = 0;
            while(r < 2) {
                //
                // test with output stream
                //
                System.out.println("verifying content (with OutputStream)");
                var contentOs = new ByteArrayOutputStream();
                contentLo.getContent(contentOs, 0);
                contentOs.close();
                var contentIs = new ByteArrayInputStream(contentOs.toByteArray());
                var i = 0;
                while(i < contentLength) {
                    assertEquals("content at position " + i, i % 256, contentIs.read());
                    contentIs.skip(9);
                    i += 10;
                }
                contentIs.close();
                System.out.println("OK");
                r += 1;
            }
        }
        {
            //
            // Modify content
            //
            this.begin();
            var i = 0;
            while(i < contentLength) {
                content(i) = (i % 137).asInstanceOf[Byte];
                i += 1;
            }
            document.setContent(
                org.w3c.cci2.BinaryLargeObjects.valueOf(content)
            );
            this.commit();
        }
        {
            contentLo = document.getContent();
            assertNotNull("BLOB", contentLo);
            var documentSize = contentLo.getLength();
            if(documentSize != null) {
                assertEquals("document size", contentLength, documentSize.longValue());
            }
            var r = 0;
            while(r < 2) {
                //
                // test with input stream method
                //
                System.out.println("verifying content (with InputStream)");
                var contentIs = contentLo.getContent();
                var i = 0;
                while(i < contentLength) {
                    assertEquals("Run " + r + ": content at position " + i, i % 137, contentIs.read());
                    contentIs.skip(9);
                    i += 10;
                }
                contentIs.close();
                System.out.println("OK");
                r += 1;
            }
            r = 0;
            while(r < 2) {
                //
                // test with output stream
                //
                System.out.println("verifying content (with OutputStream)");
                var contentOs = new ByteArrayOutputStream();
                contentLo.getContent(contentOs, 0);
                contentOs.close();
                var contentIs = new ByteArrayInputStream(contentOs.toByteArray());
                var i = 0;
                while(i < contentLength) {
                    assertEquals("content at position " + i, i % 137, contentIs.read());
                    contentIs.skip(9);
                    i += 10;
                }
                contentIs.close();
                System.out.println("OK");
                r += 1;
            }
        }
        //
        // CR20018821
        //
        try {
            taskId = "CR20018821";
            this.begin();
            Importer.importObjects(
                Importer.asTarget(persistenceManager),
                Importer.asSource(
                    new URL("xri://+resource/test/openmdx/app1/data.xml")
                )
            );
            this.commit();
            var file = File.createTempFile("data", ".zip");
            Exporter.export(
                Exporter.asTarget(file, Exporter.MIME_TYPE_XML),
                persistenceManager,
                null,
                segment.refGetPath()
            );
            System.out.println(segment.refGetPath().toXRI() + " exported to " + file);
            //
            // Validate date-time values
            //
            person = persistenceManager.getObjectById(personId).asInstanceOf[Person];
            assertEquals(
                "Birthdate as date/time", 
                Datatypes.create(classOf[Date], "1960-01-01T12:00:00Z"), 
                person.getBirthdateAsDateTime()
            );
        } finally {
            taskId = null;
        }
        
        //
        // CR20018889
        //
        try {
            taskId = "CR20018889";
            var file = File.createTempFile("CR20018889.", null);
            var cardinality = 0;
            {
                var jmiPeople = segment.getPerson().asInstanceOf[Collection[Person]]
                var jpaPeople = persistenceManager.detachCopyAll(jmiPeople);
                var jpaAddresses = new ArrayList[test.openmdx.app1.cci2.Address]();
                cardinality = jpaPeople.size();
                assertEquals("people", cardinality, jmiPeople.size());
                assertTrue("Implementation detail", jpaPeople.isInstanceOf[ArrayList[_]]);
                for(p: Person <- jpaPeople) {
                    assertFalse("deleted", JDOHelper.isDeleted(p));
                    assertTrue("detached", JDOHelper.isDetached(p));
                    assertFalse("dirty", JDOHelper.isDirty(p));
                    assertFalse("new", JDOHelper.isNew(p));
                    assertFalse("persistent", JDOHelper.isPersistent(p));
                    assertFalse("transactional", JDOHelper.isTransactional(p));
                    if("F10".equals(p.getForeignId())) {
                        var jpaPersion = p.asInstanceOf[test.openmdx.app1.jpa3.Person]
                        var jpaAddress = new test.openmdx.app1.jpa3.EmailAddress();
                        StateAccessor.getInstance().setTransactionalObjectId(
                            jpaAddress, 
                            segment.refGetPath().getDescendant("address", "NoReply10")
                        );
                        var jpaAddress_Id: String = JDOHelper.getObjectId(jpaAddress).asInstanceOf[String];
                        jpaAddress.setAddress("noreply@openmdx.org");
                        jpaAddresses.add(jpaAddress);
                        jpaPersion.getGivenName().set(1, "Heinrich");
                        jpaPersion.getAssignedAddress_Id().add(jpaAddress_Id);
                        assertFalse("deleted", JDOHelper.isDeleted(jpaPersion));
                        assertTrue("detached", JDOHelper.isDetached(jpaPersion));
                        assertTrue("dirty", JDOHelper.isDirty(jpaPersion));
                        assertFalse("new", JDOHelper.isNew(jpaPersion));
                        assertFalse("persistent", JDOHelper.isPersistent(jpaPersion));
                        assertFalse("transactional", JDOHelper.isTransactional(jpaPersion));
                    }
                }
                var jpaDocument = persistenceManager.detachCopy(
                    segment.getDocument("myDoc")
                );
                var fileOutputStream = new FileOutputStream(file);
                var objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(jpaPeople);
                objectOutputStream.writeObject(jpaAddresses);
                objectOutputStream.writeObject(jpaDocument);
                objectOutputStream.close();
            }
            {
                var fileInputStream = new FileInputStream(file);
                var objectInputStream = new ObjectInputStream(fileInputStream);
                var jpaPeople = objectInputStream.readObject().asInstanceOf[Collection[test.openmdx.app1.cci2.Person]];
                var jpaAddresses = objectInputStream.readObject().asInstanceOf[Collection[test.openmdx.app1.cci2.Address]];
                var jpaDocument = objectInputStream.readObject().asInstanceOf[test.openmdx.app1.cci2.Document];
                assertEquals("people", cardinality, jpaPeople.size());
                assertEquals("addresses", 1, jpaAddresses.size());
                objectInputStream.close();
                this.begin();
                for(jpaAddress <- jpaAddresses) {
                    assertFalse("deleted", JDOHelper.isDeleted(jpaAddress));
                    assertFalse("detached", JDOHelper.isDetached(jpaAddress));
                    assertFalse("dirty", JDOHelper.isDirty(jpaAddress));
                    assertFalse("new", JDOHelper.isNew(jpaAddress));
                    assertFalse("persistent", JDOHelper.isPersistent(jpaAddress));
                    assertFalse("transactional", JDOHelper.isTransactional(jpaAddress));
                    var jmiAddress = persistenceManager.makePersistent(jpaAddress);
                    assertFalse("deleted", JDOHelper.isDeleted(jmiAddress));
                    assertFalse("detached", JDOHelper.isDetached(jmiAddress));
                    assertTrue("dirty", JDOHelper.isDirty(jmiAddress));
                    assertTrue("new", JDOHelper.isNew(jmiAddress));
                    assertTrue("persistent", JDOHelper.isPersistent(jmiAddress));
                    assertTrue("transactional", JDOHelper.isTransactional(jmiAddress));
                }
                for(jpaPerson <- jpaPeople) {
                    var dirty = "F10".equals(jpaPerson.getForeignId()); 
                    assertFalse("deleted", JDOHelper.isDeleted(jpaPerson));
                    assertTrue("detached", JDOHelper.isDetached(jpaPerson));
                    assertEquals("dirty", dirty, JDOHelper.isDirty(jpaPerson));
                    assertFalse("new", JDOHelper.isNew(jpaPerson));
                    assertFalse("new", JDOHelper.isPersistent(jpaPerson));
                    assertFalse("transactional", JDOHelper.isTransactional(jpaPerson));
                    var jmiPerson = persistenceManager.makePersistent(jpaPerson);
                    assertFalse("deleted", JDOHelper.isDeleted(jmiPerson));
                    assertFalse("detached", JDOHelper.isDetached(jmiPerson));
                    assertEquals("dirty", dirty, JDOHelper.isDirty(jmiPerson));
                    assertFalse("new", JDOHelper.isNew(jmiPerson));
                    assertTrue("persistent", JDOHelper.isPersistent(jmiPerson));
                    assertEquals("transactional", dirty, JDOHelper.isTransactional(jmiPerson));
                    if(dirty) assertEquals("Middle Name", "Heinrich", jmiPerson.getGivenName().get(1));
                }
                {
                    contentLo = jpaDocument.getContent();
                    assertNotNull("BLOB", contentLo);
                    var documentSize = contentLo.getLength();
                    if(documentSize != null) {
                        assertEquals("document size", contentLength, documentSize.longValue());
                    }
                    var r = 0;
                    while(r < 2) {
                        //
                        // test with input stream method
                        //
                        System.out.println("verifying content (with InputStream)");
                        var contentIs = contentLo.getContent();
                        var i = 0;
                        while(i < contentLength) {
                            assertEquals("Run " + r + ": content at position " + i, i % 137, contentIs.read());
                            contentIs.skip(9);
                            i += 10;
                        }
                        contentIs.close();
                        System.out.println("OK");
                        r += 1;
                    }
                    r = 0;
                    while(r < 2) {
                        //
                        // test with output stream
                        //
                        System.out.println("verifying content (with OutputStream)");
                        var contentOs = new ByteArrayOutputStream();
                        contentLo.getContent(contentOs, 0);
                        contentOs.close();
                        var contentIs = new ByteArrayInputStream(contentOs.toByteArray());
                        var i = 0;
                        while(i < contentLength) {
                            assertEquals("content at position " + i, i % 137, contentIs.read());
                            contentIs.skip(9);
                            i += 10;
                        }
                        contentIs.close();
                        System.out.println("OK");
                        r += 1;
                    }
                }
                this.commit();
            }
        } finally {
            taskId = null;
        }
        // 
        // Test CR20019184
        // 
        try {
            taskId = "CR20019184";
            this.begin();
            postalAddress = postalAddressClass.createInternationalPostalAddress();
            postalAddress.setCountry("Suisse");
            postalAddress.setCity("Gen\u00E8ve");
            postalAddress.setHouseNumber("10");
            postalAddress.setPostalCode("1201");
            postalAddress.setStreet("rue Voltaire");
            postalAddress.setAddressLine("Hotel", "Ibis Gen\u00E8ve Centre Gare");
            segment.addAddress(false, "lost+found", postalAddress);
            this.commit();
        } finally {
            taskId = null;
        }
        
    }

    /**
     * Test the audit entries
     * 
     * @param run the number of completed runs
     */
    def testAudit(
        run: Int
    ) = {
        var person: Person = entityManager.getObjectById(
            entityManager.newObjectIdInstance(
                classOf[Person], 
                TestMain.DATA_SEGMENT_ID.getDescendant("person","ID500012")
            )
        ).asInstanceOf[Person]
        var invoice: Invoice = entityManager.getObjectById(
            entityManager.newObjectIdInstance(
                classOf[Invoice],
                TestMain.DATA_SEGMENT_ID.getDescendant("invoice","CR20019372")
           )
        ).asInstanceOf[Invoice];
        //
        // Touch
        //
        taskId = "CR20018820";
        this.begin();
        person.setGivenName("Rainer Maria");
        this.commit();
        //
        // By Task Id
        //
        var task = AuditQueries.getUnitOfWorkBelongingToTask(entityManager, "CR20018578"); 
        assertEquals("Size of task CR20018578", 1 * run, task.size());
        task = AuditQueries.getUnitOfWorkBelongingToTask(entityManager, "CR0002096"); 
        assertEquals("Size of task CR0002096", 2 * run, task.size());
        dumpTask("CR0002096", task);
        //
        // By Object
        //
        testAudit2(person, run, false);
        testAudit2(person, run, true);
        //
        // With Children
        //
        testAudit2(invoice, run, false);
        testAudit2(invoice, run, true);
    }

    def testAudit2(
        invoice: Invoice,
        run: Int,
        scoped: Boolean
    ) = {
        var factor = if(scoped) 1 else run;
        var create = if(SharedObjects.getPlugInObject(this.entityManager, classOf[Configuration]).getPersistenceMode() == InvolvementPersistence.EMBEDDED) 0 else 1;
        var scope = if(scoped) " (run " + run + ")" else " (run 1.." + run + ")";
        var from = if(scoped) super.getStart() else null;
        var task = AuditQueries.getUnitOfWorkInvolvingObject(from, null, invoice); 
        var id = "Involve InvoicePosition CR20019372.";            
        dumpTask(id + scope, task);
        assertEquals(id + scope, (1 * create + 1) * factor, task.size());
        id = "Involve InvoicePosition... CR20019372.";            
        var tree = PersistenceHelper.getCandidates(
            entityManager.getExtent(classOf[ExtentCapable], true),
            invoice.refMofId() + "/($...)"
        );
        task = AuditQueries.getUnitOfWorkInvolvingObject(from, null, tree); 
        assertEquals(id + scope, (1 * create + 1) * factor, task.size());
        for(unitOfWork <- task) {
            for(involvement: Involvement <- unitOfWork.getInvolvement()) {
                var modifiable = involvement.getBeforeImage();
                System.out.println(modifiable.refMofId());
            }
        }
    }
    
    def testAudit2(
        person: Person,
        run: Int,
        scoped: Boolean
    ) = {
        var factor = if(scoped) 1 else run;
        var create = if(SharedObjects.getPlugInObject(this.entityManager, classOf[Configuration]).getPersistenceMode() == InvolvementPersistence.EMBEDDED) 0 else 1;
        var scope = if(scoped)  " (run " + run + ")" else " (run 1.." + run + ")";
        var from = if(scoped) super.getStart() else null;
        var task = AuditQueries.getUnitOfWorkInvolvingObject(from, null, person); 
        var id = "involve Person # ID500012";            
        dumpTask(id + scope, task);
        assertEquals(id + scope, (1 * create + 4) * factor, task.size());
        if(scoped || run == 1) {
            var lastImage: Modifiable = null;
            for(unitOfWork <- task){
                var involvement = unitOfWork.getInvolvement(
                    person.refGetPath().toString()
                );
                assertNotNull(id, involvement);
                if(lastImage != null) {
                    assertSame(id, lastImage, involvement.getBeforeImage());
                }
                lastImage = involvement.getAfterImage();
            }
            assertNotNull(id, lastImage);
        }
        if(create == 0) {
            try {
                task = AuditQueries.getUnitOfWorkCreatingObject(from, null, person); 
                fail("Create not audited in EMBEDDED mode");
            } catch {
            	case exception: UnsupportedOperationException =>
                // Create not audited in audit1 mode
            }
        } else {
            id = "create Person # ID500012"; 
            task = AuditQueries.getUnitOfWorkCreatingObject(from, null, person); 
            dumpTask(id + scope, task);
            assertEquals(id + scope, (1 * create + 0) * factor, task.size());
        }
        if(run == 1) {
            id = "touch Person # ID500012"; 
            task = AuditQueries.getUnitOfWorkTouchingObject(from, null, null, person ); 
            dumpTask(id + scope, task);
            assertEquals(id + scope, (0 * create + 4) * factor, task.size());
            id = "touch specific attributes of Person # ID500012"; 
            task = AuditQueries.getUnitOfWorkTouchingObject(
                from, 
                null, 
                Sets.asSet(
                    Arrays.asList(
                        "test:openmdx:app1:Person:assignedAddress",
                        "test:openmdx:app1:Person:additionalInfo"
                    )
                ), 
                person
            );
            dumpTask(id + scope, task);
            assertEquals(id + scope, (0 * create + 3) * factor, task.size());
        }
        if(scoped || run == 1) {
            id = "remove Person # ID500012";
            task = AuditQueries.getUnitOfWorkRemovingObject(from, null, person); 
            dumpTask(id + scope, task);
            assertEquals(id + scope, 0 * factor, task.size());
        }
        id = "all units of work"; 
        task = AuditQueries.getUnitOfWorkForTimeRange(entityManager, from, null);
        dumpTask(id + scope, task);
        assertEquals(id + scope, (16 * create + 18) * factor, task.size());
        id = "units of work involving people"; 
        task = AuditQueries.getUnitOfWorkInvolvingObject(
            from, 
            null, 
            PersistenceHelper.getCandidates(
                entityManager.getExtent(classOf[Person]),
                TestMain.DATA_SEGMENT_ID.getDescendant("person", "%")
            )
        );
        dumpTask(id + scope, task);
        assertEquals(id + scope, (3 * create + 9) * factor, task.size());
        id = "units of work involving deleted object";
        var addressId = TestMain.DATA_SEGMENT_ID.getDescendant("address","CR0002096"); 
        task = AuditQueries.getUnitOfWorkInvolvingObject(
            from, 
            null, 
            PersistenceHelper.getCandidates(
                entityManager.getExtent(classOf[Person]),
                addressId.toXRI()
            )
        );
        dumpTask(id + scope, task);
        assertEquals(id + scope, (1 * create + 1) * factor, task.size());
        id = "units of work deleting object";
        task = AuditQueries.getUnitOfWorkRemovingObject(
            from, 
            null, 
            PersistenceHelper.getCandidates(
                entityManager.getExtent(classOf[Person]),
                addressId.toXRI()
            )
        );
        dumpTask(id + scope, task);
        assertEquals(id + scope, (0 * create + 1) * factor, task.size());
        for(unitOfWork <- task) {
            for(involvement: Involvement <- unitOfWork.getInvolvement()) {
                assertNull(id + scope, involvement.getAfterImage());
            }
        }
    }
    
    def dumpTask(
        title: String,
        task: Collection[UnitOfWork]
    ) = {
        if(TestMain.DUMP) {
            System.out.println(title);
            for(unitOfWork <- task){
                System.out.println("\t" + unitOfWork.getUnitOfWorkId() + " (" + DateTimeFormat.EXTENDED_UTC_FORMAT.format(unitOfWork.getCreatedAt()) + ")");
                if(unitOfWork.isInstanceOf[org.openmdx.compatibility.audit1.jmi1.UnitOfWork]) {
                    System.out.println("\t\tinvolved");
                    for(involved <- PersistenceHelper.getFeatureReplacingObjectById(unitOfWork, "involved").asInstanceOf[(Collection[_])]){
                        System.out.println("\t\t\t" + involved.asInstanceOf[Path].toXRI());
                    }
                }
                System.out.println("\t\tinvolvement");
                for(involvement: Involvement <- unitOfWork.getInvolvement()) {
                    System.out.println("\t\t\t" + getResourceIdentifier(involvement));
                    System.out.println(
                        "\t\t\t\t objectId = " + 
                        involvement.getObjectId()
                    );
                    System.out.println(
                        "\t\t\t\t object = " + 
                        getResourceIdentifier(involvement.getObject())
                    );
                    System.out.println(
                        "\t\t\t\t beforeImage = " + 
                        getResourceIdentifier(involvement.getBeforeImage())
                    );
                    try {
                        System.out.println(
                            "\t\t\t\t afterImage = " + 
                            getResourceIdentifier(involvement.getAfterImage())
                        );
                    } catch {
                    	case exception: Exception =>
                    		System.out.println(
                                "\t\t\t\t afterImage is N/A: " +
                                exception.getMessage() 
                            );
                    }
                    try {
                        System.out.println(
                            "\t\t\t\t modifiedFeature = " + 
                            involvement.getModifiedFeature()
                        );
                    } catch {
                    	case exception: Exception =>
                            System.out.println(
                                "\t\t\t\t modifiedFeature is N/A: " +
                                exception.getMessage() 
                            );
                    }
                }
            }
        }
    }
    
    def getResourceIdentifier(
        pc: Object
    ): String = {
        if(pc == null) null else
        if(JDOHelper.isPersistent(pc)) JDOHelper.getObjectId(pc).asInstanceOf[Path].toXRI() else
        JDOHelper.getTransactionalObjectId(pc).toString();
    }
    
    def testInMemoryProvider() = {
        var personClass = getPackage().getPerson();
        var segment = getSegment();
        var postalAddress = segment.getAddress(false, "0001");
        var g0 = segment.getPersonGroup(false, "g0");
        var g1 = segment.getPersonGroup(false, "g1");
        var g2 = segment.getPersonGroup(false, "g2");

        // Create persons
        var i = 0;
        while(i <= LARGE_N_PERSONS) {
            if(i % 100 == 0) {
                System.out.println(i + " persons created. Free memory " + Runtime.getRuntime().freeMemory());
            }
            this.begin();
            var person = personClass.createPerson();
            person.setForeignId("F" + i);
            person.setBirthdate(Datatypes.create(classOf[XMLGregorianCalendar], "1960-01-01"));
            person.setBirthdateAsDateTime(Datatypes.create(classOf[Date], "19600101T120000.000Z"));
            person.setLastName("Muster" + i);
            person.setSalutation("Herr");
            person.setSex(0);
            person.setGivenName(Arrays.asList("Hans", "Heiri"));
            person.getAssignedAddress().add(postalAddress);
            person.getPersonGroup().add(g0);
            person.getPersonGroup().add(g1);
            person.getPersonGroup().add(g2);
            segment.addPerson(false,"L" + (1000000 + i), person);
            this.commit();
            i += 1;
        }

        // Retrieve persons
        var ii = 0;
        var limit = 1000000;
        var runtime = Runtime.getRuntime();
        var initialMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("initial memory usage " + initialMemoryUsage);
        var allPeople = segment.getPerson();
        for(pers <- allPeople) {
            if(ii % 100 == 0) {
                var currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
                System.out.println(ii + " persons retrieved. Current memory usage " + currentMemoryUsage);
                var additionalMemoryUsage = currentMemoryUsage - initialMemoryUsage;
                if(additionalMemoryUsage > limit) {
                    runtime.gc();
                    currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
                    additionalMemoryUsage = currentMemoryUsage - initialMemoryUsage;
                    assertFalse(
                        "Memory used up after " + ii + " failed retrievals: " + additionalMemoryUsage,
                        additionalMemoryUsage > limit
                    );
                }
            }
            ii += 1;                
            limit += 3500;
        }
    }

    def getSegment(
    ): test.openmdx.app1.jmi1.Segment = {
        return entityManager.getObjectById(
            TestMain.DATA_SEGMENT_ID
        ).asInstanceOf[test.openmdx.app1.jmi1.Segment];
    }

    def getPackage(): App1Package = {
        return (
               entityManager.newInstance(classOf[test.openmdx.app1.jmi1.Segment])
        ).asInstanceOf[RefObject].refImmediatePackage().asInstanceOf[App1Package]
    }

}

/**
 * Object id accessor
 * 
 * @param refObject
 * 
 * @return the object id as path
 */


//------------------------------------------------------------------------
// Class LocalConnectionTest
//------------------------------------------------------------------------

/**
 * 1st Run
 */
class LocalConnectionTest extends RepeatableTest {

    @Test
    def run(
    ) = {
//          super.testInMemoryProvider();
        super.resetAuditSegment();
        super.resetDataSegment();
        super.testCR20019462();
        super.testCR20018726();
        super.testCR20018667();
        super.testCR20019014();
        super.testMain();
        super.testAudit(1);
    }

}


//------------------------------------------------------------------------
// Class ProxyConnectionSetUp
//------------------------------------------------------------------------

/**
 * Replaces the entity manager factory
 */
class ProxyConnectionSetUp {
 
    /**
     * Tells whether a servlet connection shall be used
     * @return
     */
    def useServlet(): Boolean = {
        return true;
    }
    
    /**
     * Switch to proxy set-up
     * 
     * @throws ResourceException 
     * @throws ServletException 
     */
    @Test
   def reInitialize() = {
        var inboundConnectionFactory = if(useServlet()) new ConnectionFactoryAdapter(
            new ServletPort(
                Collections.singletonMap(
                    "entity-manager-factory-name",
                    "jdo:test-Main-EntityManagerFactory"
                )
            ),
            true, // supportsLocalTransactionDemarcation
            TransactionAttributeType.NEVER
        ) else InboundConnectionFactory_2.newInstance(
            "jdo:test-Main-EntityManagerFactory"
        );
        var dataManagerProxyConfiguration = new HashMap[String,Object]();
        dataManagerProxyConfiguration.put(
            ConfigurableProperty.ConnectionFactory.qualifiedName(),
            inboundConnectionFactory
        );
        dataManagerProxyConfiguration.put(
            ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(),
            classOf[EntityManagerProxyFactory_2].getName()
        );    
        var outboundConnectionFactory = JDOHelper.getPersistenceManagerFactory(
            dataManagerProxyConfiguration
        );

        var entityManagerConfiguration = new HashMap[String,Object]();
        entityManagerConfiguration.put(
            ConfigurableProperty.ConnectionFactory.qualifiedName(),
            outboundConnectionFactory
        );
        entityManagerConfiguration.put(
            ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(),
            classOf[EntityManagerFactory_1].getName()
        );    
        TestMain.entityManagerFactory = JDOHelper.getPersistenceManagerFactory(
            entityManagerConfiguration
        );
        
    }
    
}


//------------------------------------------------------------------------
// Class AbstractContainerManagedTransactionSetUp
//------------------------------------------------------------------------

/**
 * Replaces the entity manager factory
 */
abstract class AbstractContainerManagedTransactionSetUp {
 
    /**
     * Defines whether the unit of work is optimistic or pessimistic
     * 
     * @return <code>true</code> if the unit of work is optimistic
     */
    def isOptimistic(): Boolean;
    
    /**
     * Switch container managed transaction set-up
     * 
     * @throws ResourceException 
     * @throws ServletException 
     */
    @Test
    def reInitialize() = {
        var `override` = new HashMap[String, Object]();
        `override`.put(
            ConfigurableProperty.TransactionType.qualifiedName(), 
            Constants.JTA
        );
        `override`.put(
            ConfigurableProperty.ContainerManaged.qualifiedName(),
            java.lang.Boolean.TRUE.toString()
        );
        `override`.put(
            ConfigurableProperty.Optimistic.qualifiedName(),
            this.isOptimistic().toString()
        );
        TestMain.entityManagerFactory = JDOHelper.getPersistenceManagerFactory(
            `override`,
            "test-Main-EntityManagerFactory"
        );
    }
    
}

//------------------------------------------------------------------------
// Class ContainerManagedOptimisticTransactionSetUp
//------------------------------------------------------------------------

/**
 * Replaces the entity manager factory
 */
class ContainerManagedOptimisticTransactionSetUp extends AbstractContainerManagedTransactionSetUp {

    /* (non-Javadoc)
     * @see test.openmdx.app1.TestMain.AbstractContainerManagedTransactionSetUp#isOptimistic()
     */
    override def isOptimistic(): Boolean = {
        return true;
    }
 
    
}

//------------------------------------------------------------------------
// Class ContainerManagedPessimisticTransactionSetUp
//------------------------------------------------------------------------

/**
 * Replaces the entity manager factory
 */
class ContainerManagedPessimisticTransactionSetUp extends AbstractContainerManagedTransactionSetUp {

    /* (non-Javadoc)
     * @see test.openmdx.app1.TestMain.AbstractContainerManagedTransactionSetUp#isOptimistic()
     */
    override def isOptimistic(): Boolean = {
        return false;
    }
 
    
}

//------------------------------------------------------------------------
// Class ContainerManagedTransactionTest
//------------------------------------------------------------------------

/**
 * 3rd Run
 */
   class ContainerManagedTransactionTest extends LocalConnectionTest {

        var userTransaction: UserTransaction = null;
        
        @Override
        override def run(
        ) = {
            this.userTransaction = ComponentEnvironment.lookup(
                classOf[UserTransaction]
            );
            this.userTransaction.begin();
            super.resetAuditSegment();
            super.resetDataSegment();
            super.testCR20019462();
            super.testCR20018726();
            super.testCR20018667();
            super.testCR20019014();
            super.testMain();
            this.userTransaction.rollback();
        }

        /* (non-Javadoc)
     * @see test.openmdx.app1.TestMain.AbstractTest#begin()
     */
    @Override
    override def begin() = {
        try {
            if(this.userTransaction.getStatus() == Status.STATUS_ACTIVE) {
                this.userTransaction.rollback();
            }
            this.userTransaction.begin();
        } catch {
        	case exception: Exception =>
        		throw new RuntimeException(exception);
        }
        entityManager.currentTransaction().asInstanceOf[Synchronization_2_0].afterBegin();
    }

    /* (non-Javadoc)
     * @see test.openmdx.app1.TestMain.AbstractTest#commit()
     */
    @Override
    override def commit() = {
        try {
            try {
                entityManager.currentTransaction().asInstanceOf[Synchronization_2_0].beforeCompletion();
            } catch {
            	case commitException: JDOException => 
                    try {
                        this.userTransaction.rollback();
                    } catch {
                    	case rollbackException: Exception =>
	                        throw new JDOFatalDataStoreException(
	                            "Container managed rollback failed",
	                            rollbackException
	                        );
                    } finally {
                        entityManager.currentTransaction().asInstanceOf[Synchronization_2_0].afterCompletion(Status.STATUS_ROLLEDBACK);
                    }
                    throw commitException;
            }
            try {
                this.userTransaction.commit();
            } catch {
            	case commitException: Exception =>
                    entityManager.currentTransaction().asInstanceOf[Synchronization_2_0].afterCompletion(Status.STATUS_ROLLEDBACK);
                    throw new JDOFatalDataStoreException(
                        "Container managed commit failed",
                        commitException
                    );
            }
            entityManager.currentTransaction().asInstanceOf[Synchronization_2_0].afterCompletion(Status.STATUS_COMMITTED);
        } finally {
            try {
                this.userTransaction.begin();
            } catch {
            	case exception: Exception =>
                    throw new RuntimeException(
                        "Could not start container managed read-only transaction",
                        exception
                    );
            }
        }
    }

    /* (non-Javadoc)
     * @see test.openmdx.app1.TestMain.AbstractTest#rollback()
     */
    override def rollback() = {
        try {
            this.userTransaction.rollback();
        } catch {
        	case exception: Exception =>
                throw new RuntimeException(exception);
        } finally {
            entityManager.currentTransaction().asInstanceOf[Synchronization_2_0].afterCompletion(Status.STATUS_ROLLEDBACK);
        }
        try {
            this.userTransaction.begin();
        } catch {
        	case exception: Exception =>
        		throw new RuntimeException(exception);
        }
    }

}

//------------------------------------------------------------------------
// Class ProxyConnectionTest
//------------------------------------------------------------------------

/**
 * 2nd Run
 */
   class ProxyConnectionTest extends RepeatableTest {

        @Test
        def run(
        ) = {
            super.resetDataSegment();
            super.testPackageAcquisition();
            super.testCR20019462();
            super.testCR20018800();
            super.testMain();
// TODO     super.testAudit(2);
    }

}

object TestMain {

    val DUMP: Boolean = false;
    val AUDIT_PROVIDER_NAME: String = "Audit";
    val DATA_PROVIDER_NAME: String = "Data";
    val SEGMENT_NAME: String = "Standard";
    var entityManagerFactory: PersistenceManagerFactory = null;

    val DATA_SEGMENT_ID: Path = new Path(
        "xri://@openmdx*test.openmdx.app1/provider"
    ).getDescendant(
        DATA_PROVIDER_NAME,
        "segment",
        SEGMENT_NAME
    ).lock();

	var suite = new TestSuite(
		classOf[LocalConnectionTest],
		classOf[ProxyConnectionSetUp],
		classOf[ProxyConnectionTest],
		classOf[ContainerManagedOptimisticTransactionSetUp],
		classOf[ContainerManagedTransactionTest],
		classOf[ContainerManagedPessimisticTransactionSetUp],
		classOf[ContainerManagedTransactionTest]
	);
	var testResult = new TestResult();
	suite.run(testResult);
	
	def refGetPath(
	    refObject: RefObject
	): Path = {
	    var objectId = JDOHelper.getObjectId(refObject);
	    if(objectId.isInstanceOf[Path]) objectId.asInstanceOf[Path] else
	        if(objectId == null) null else
	            new Path(objectId.toString());
	}
	
    @BeforeClass
    def initialize(
    ) = {
        if(!NamingManager.hasInitialContextFactoryBuilder()) {
            NonManagedInitialContextFactoryBuilder.install(null);
        }
        entityManagerFactory = JDOHelper.getPersistenceManagerFactory(
            "test-Main-EntityManagerFactory"
        );
    }
    
}
